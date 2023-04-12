package com.google.idea.blaze.scala.fastbuild;

import ch.epfl.scala.bsp4j.BuildTargetIdentifier;
import ch.epfl.scala.bsp4j.CompileParams;
import ch.epfl.scala.bsp4j.CompileResult;
import ch.epfl.scala.bsp4j.DiagnosticSeverity;
import ch.epfl.scala.bsp4j.Position;
import ch.epfl.scala.bsp4j.PublishDiagnosticsParams;
import ch.epfl.scala.bsp4j.StatusCode;
import ch.epfl.scala.bsp4j.TaskFinishParams;
import com.google.gson.JsonObject;
import com.google.idea.blaze.base.io.VfsUtils;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.scope.output.StatusOutput;
import com.google.idea.blaze.scala.fastbuild.BloopProject;
import com.google.idea.blaze.scala.fastbuild.FileUtils;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.DocumentUtil;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.jetbrains.bsp.protocol.BspCommunication;
import org.jetbrains.bsp.protocol.BspNotifications.BspNotification;
import org.jetbrains.bsp.protocol.BspNotifications.PublishDiagnostics;
import org.jetbrains.bsp.protocol.BspNotifications.TaskFinish;
import org.jetbrains.bsp.protocol.session.BspSession.BspServer;
import org.jetbrains.bsp.protocol.session.BspSession.BuildServerInfo;
import org.jetbrains.bsp.settings.BspProjectSettings.BloopConfig$;
import org.jetbrains.plugins.scala.build.BuildReporter;
import org.jetbrains.plugins.scala.build.ConsoleReporter;
import scala.Function1;
import scala.Function2;
import scala.runtime.BoxedUnit;

public class BspFastCompile {

  private static final String FILE_PROTOCOL_PREFIX = "file://";
  private static final String CLIENT_DIR_FILE_PROTOCOL_PREFIX = "file://";
  private final File destination;
  private final BloopProject bloopProject;
  private final BlockingQueue<TaskFinishParams> finishParams;
  private static final Logger logger = Logger.getInstance(BspFastCompile.class);
  private final BspCompileRunner bspCompileRunner;
  private final BspNotificationsHandler bspNotificationsHandler;
  private final BuildReporter reporter;

  public BspFastCompile(File destination, BloopProject bloopProject,
      BlazeContext context) {
    this.destination = destination;
    this.bloopProject = bloopProject;
    finishParams = new LinkedBlockingQueue<>();
    bspCompileRunner = new BspCompileRunner(bloopProject);
    bspNotificationsHandler = new BspNotificationsHandler(finishParams, context);
    reporter = new ConsoleReporter("Fast test");

  }

  void compileWithBsp() throws IOException, InterruptedException {
    final BspCommunication bspCommunication =
        BspCommunication.forWorkspace(bloopProject.getWorkspaceDir().toFile(),
            BloopConfig$.MODULE$);
    // for some reason the BspJop<CompileResult> doesn't return the actual data
    // so we're using a queue to communicate back the results
    bspCommunication.run(bspCompileRunner, bspNotificationsHandler, BspFastCompile::log, reporter);

    TaskFinishParams taskFinishParams = finishParams.poll(3, TimeUnit.MINUTES);
    if (taskFinishParams.getStatus() == StatusCode.OK) {
      copyCompilationOutputs(taskFinishParams);
    } else {
      throw new RuntimeException("Bsp fast scala compilation");
    }

  }

  private void copyCompilationOutputs(TaskFinishParams taskFinishParams) throws IOException {
    JsonObject jsonObject = (JsonObject) taskFinishParams.getData();
    String clientDir = jsonObject.get("clientDir").getAsString();
    //clientDir starts with file protocol prefix which needs to be removed
    Path clientDirPath = Paths.get(clientDir.substring(CLIENT_DIR_FILE_PROTOCOL_PREFIX.length()));
    FileUtils.copyFolderRecursively(clientDirPath, destination.toPath());
  }

  private static class BspCompileRunner implements
      Function2<BspServer, BuildServerInfo, CompletableFuture<CompileResult>> {

    private final BloopProject bloopProject;

    private BspCompileRunner(BloopProject bloopProject) {
      this.bloopProject = bloopProject;
    }

    @Override
    public CompletableFuture<CompileResult> apply(BspServer bspServer,
        BuildServerInfo v2) {
      return bspServer.buildTargetCompile(new CompileParams(
          Collections.singletonList(new BuildTargetIdentifier(FILE_PROTOCOL_PREFIX + bloopProject
              .getWorkspaceDir() + "?id=" + bloopProject.getName())))
      );
    }
  }

  private static class BspNotificationsHandler implements Function1<BspNotification, BoxedUnit> {

    private final BlockingQueue<TaskFinishParams> finishParams;
    private final BlazeContext context;

    private BspNotificationsHandler(BlockingQueue<TaskFinishParams> finishParams,
        BlazeContext context) {
      this.finishParams = finishParams;
      this.context = context;
    }

    @Override
    public BoxedUnit apply(BspNotification bspNotification) {
      switch (bspNotification.getClass().getSimpleName()) {
        case "PublishDiagnostics": {
          PublishDiagnosticsParams params = ((PublishDiagnostics) bspNotification).params();

          final String fileLocationWithoutFileProtocolPrefix = params.getTextDocument().getUri()
              .substring(FILE_PROTOCOL_PREFIX.length());
          final File file = Paths.get(fileLocationWithoutFileProtocolPrefix).toFile();
          final VirtualFile virtualFile = VfsUtils.resolveVirtualFile(file, false);
          params.getDiagnostics().stream()
              .filter(diagnostic -> diagnostic.getSeverity() == DiagnosticSeverity.ERROR)
              .map(diagnostic -> {
                final Position start = diagnostic.getRange().getStart();
                final Document document = FileDocumentManager.getInstance().getDocument(
                    Objects.requireNonNull(virtualFile));
                final String lineWithError = Objects.requireNonNull(document)
                    .getText(DocumentUtil.getLineTextRange(document, start.getLine()));
                final StatusOutput lineWithErrorStatusOutput = new StatusOutput(lineWithError);
                final StatusOutput locationStatusOutput = new StatusOutput(
                    fileLocationWithoutFileProtocolPrefix + ":" + (start.getLine() + 1) + ":" + (
                        start.getCharacter() + 1) + System.lineSeparator());
                final StatusOutput messageStatusOutput = new StatusOutput(diagnostic.getMessage());
                return Arrays.asList(lineWithErrorStatusOutput, locationStatusOutput, messageStatusOutput);
              }).forEach(statusOutputs -> statusOutputs.forEach(context::output));
          break;
        }
        case "TaskFinish": {
          TaskFinishParams params = ((TaskFinish) bspNotification).params();
          finishParams.add(params);
          break;
        }
        default: {
      /*
        LogMessage
        ShowMessage
        TaskStart
        TaskProgress
        DidChangeBuildTarget
       */
        }
      }
      return BoxedUnit.UNIT;
    }

  }

  private static BoxedUnit log(String message) {
    logger.debug(message);
    return BoxedUnit.UNIT;
  }
}
