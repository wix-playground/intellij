package com.google.idea.blaze.base.sync.inmemory;

import com.google.common.collect.*;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.devtools.intellij.ideinfo.IntellijIdeInfo;
import com.google.idea.blaze.base.async.executor.BlazeExecutor;
import com.google.idea.blaze.base.async.process.ExternalTask;
import com.google.idea.blaze.base.async.process.LineProcessingOutputStream;
import com.google.idea.blaze.base.bazel.BuildSystemProvider;
import com.google.idea.blaze.base.command.BlazeCommand;
import com.google.idea.blaze.base.command.BlazeCommandName;
import com.google.idea.blaze.base.command.BlazeFlags;
import com.google.idea.blaze.base.command.BlazeInvocationContext;
import com.google.idea.blaze.base.command.buildresult.BuildResultHelper;
import com.google.idea.blaze.base.command.buildresult.BuildResultHelperProvider;
import com.google.idea.blaze.base.command.info.BlazeConfigurationHandler;
import com.google.idea.blaze.base.command.info.BlazeInfo;
import com.google.idea.blaze.base.console.BlazeConsoleLineProcessorProvider;
import com.google.idea.blaze.base.filecache.FileDiffer;
import com.google.idea.blaze.base.ideinfo.TargetIdeInfo;
import com.google.idea.blaze.base.ideinfo.TargetKey;
import com.google.idea.blaze.base.ideinfo.TargetMap;
import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.BlazeVersionData;
import com.google.idea.blaze.base.model.SyncState;
import com.google.idea.blaze.base.model.primitives.Kind;
import com.google.idea.blaze.base.model.primitives.LanguageClass;
import com.google.idea.blaze.base.model.primitives.TargetExpression;
import com.google.idea.blaze.base.model.primitives.WorkspaceType;
import com.google.idea.blaze.base.projectview.ProjectViewSet;
import com.google.idea.blaze.base.projectview.section.sections.AdditionalLanguagesSection;
import com.google.idea.blaze.base.projectview.section.sections.TargetSection;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.scope.scopes.TimingScope;
import com.google.idea.blaze.base.settings.Blaze;
import com.google.idea.blaze.base.settings.BlazeImportSettings;
import com.google.idea.blaze.base.sync.BlazeSyncParams;
import com.google.idea.blaze.base.sync.BlazeSyncPlugin;
import com.google.idea.blaze.base.sync.BlazeSyncTask;
import com.google.idea.blaze.base.sync.aspects.BlazeIdeInterface;
import com.google.idea.blaze.base.sync.aspects.BuildResult;
import com.google.idea.blaze.base.sync.aspects.strategy.AspectStrategy;
import com.google.idea.blaze.base.sync.projectview.ImportRoots;
import com.google.idea.blaze.base.sync.projectview.WorkspaceLanguageSettings;
import com.google.idea.blaze.base.sync.sharding.ShardedTargetList;
import com.google.idea.blaze.base.sync.workspace.ArtifactLocationDecoder;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.project.Project;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public final class BlazeInMemorySyncTask extends BlazeSyncTask {

    public BlazeInMemorySyncTask(Project project, BlazeImportSettings importSettings, BlazeSyncParams syncParams) {
        super(project, importSettings, syncParams);
    }

    @Override
    public void run(ProgressIndicator indicator) {
        syncProject(new BlazeContext());
    }

    @Override
    protected BlazeIdeInterface.IdeResult getIdeQueryResult(
            Project project,
            BlazeContext parentContext,
            ProjectViewSet projectViewSet,
            BlazeInfo blazeInfo,
            BlazeVersionData blazeVersionData,
            BlazeConfigurationHandler configHandler,
            ShardedTargetList shardedTargets,
            WorkspaceLanguageSettings workspaceLanguageSettings,
            ArtifactLocationDecoder artifactLocationDecoder,
            SyncState.Builder syncStateBuilder,
            @Nullable SyncState previousSyncState,
            boolean mergeWithOldState,
            @Nullable TargetMap oldTargetMap
    ) {
        AspectStrategy aspectStrategy = AspectStrategy.getInstance(blazeVersionData.buildSystem());

        Collection<File> fileList = getFiles(
                parentContext,
                projectViewSet,
                blazeInfo,
                aspectStrategy,
                workspaceLanguageSettings
        );
        List<File> newFiles = Lists.newArrayList();
        List<File> removedFiles = Lists.newArrayList();
        ImmutableMap<File, Long> fileState =
                FileDiffer.updateFiles(null, fileList, newFiles, removedFiles);
        if (fileState == null) {
            return new BlazeIdeInterface.IdeResult(oldTargetMap, BuildResult.FATAL_ERROR);
        }

        ImportRoots importRoots =
                ImportRoots.builder(this.workspaceRoot, Blaze.getBuildSystem(project))
                        .add(projectViewSet)
                        .build();

        AtomicLong totalSizeLoaded = new AtomicLong(0);
        Set<LanguageClass> ignoredLanguages = Sets.newConcurrentHashSet();
        ListeningExecutorService executor = BlazeExecutor.getInstance().getExecutor();


        // Read protos from any new files
        List<ListenableFuture<TargetFilePair>> futures = Lists.newArrayList();
        for (File file : newFiles) {
            futures.add(executor.submit(() -> {
                totalSizeLoaded.addAndGet(file.length());
                IntellijIdeInfo.TargetIdeInfo message = aspectStrategy.readAspectFile(file);
                TargetIdeInfo target = protoToTarget(workspaceLanguageSettings, importRoots, message, ignoredLanguages);
                return new TargetFilePair(file, target);
            }));
        }

        Map<TargetKey, TargetIdeInfo> targetMap = Maps.newHashMap();
        try {
            for (TargetFilePair targetFilePair : Futures.allAsList(futures).get()) {
                if (targetFilePair.target != null) {
                    File file = targetFilePair.file;
                    String config = configHandler.getConfigurationPathComponent(file);
                    TargetKey key = targetFilePair.target.getKey();
                    if (targetMap.putIfAbsent(key, targetFilePair.target) != null) {
                        if (Objects.equals(config, configHandler.defaultConfigurationPathComponent)) {
                            targetMap.put(key, targetFilePair.target);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new BlazeIdeInterface.IdeResult(new TargetMap(ImmutableMap.copyOf(targetMap)), BuildResult.SUCCESS);

    }

    @Override
    protected BlazeInfo getBlazeInfo(BlazeContext context, List<String> syncFlags, BlazeProjectData oldBlazeProjectData) {
        return oldBlazeProjectData.getBlazeInfo();
    }

    @Nullable
    private static TargetIdeInfo protoToTarget(
            WorkspaceLanguageSettings languageSettings,
            ImportRoots importRoots,
            IntellijIdeInfo.TargetIdeInfo message,
            Set<LanguageClass> ignoredLanguages) {
        Kind kind = Kind.fromProto(message);
        if (kind == null) {
            return null;
        }
        if (languageSettings.isLanguageActive(kind.getLanguageClass())) {
            return TargetIdeInfo.fromProto(message);
        }
        TargetKey key = message.hasKey() ? TargetKey.fromProto(message.getKey()) : null;
        if (key != null && importRoots.importAsSource(key.getLabel())) {
            ignoredLanguages.add(kind.getLanguageClass());
        }
        return null;
    }

    private Collection<File> getFiles(
            BlazeContext parentContext,
            ProjectViewSet projectViewSet,
            BlazeInfo blazeInfo,
            AspectStrategy aspectStrategy,
            WorkspaceLanguageSettings workspaceLanguageSettings
    ) {
        Collection<File> fileList = null;

        try (BuildResultHelper buildResultHelper =
                     BuildResultHelperProvider.forFilesForSync(
                             project, blazeInfo, aspectStrategy.getAspectOutputFilePredicate())) {


            List<TargetExpression> targets = Lists.newArrayList();
            Collection<TargetExpression> projectViewTargets = projectViewSet.listItems(TargetSection.KEY);
            if (!projectViewTargets.isEmpty()) {
                targets.addAll(projectViewTargets);
            }

            BlazeCommand.Builder builder =
                    BlazeCommand.builder(getBinaryPath(project), BlazeCommandName.BUILD)
                            .addTargets(targets)
                            .addBlazeFlags(BlazeFlags.KEEP_GOING)
                            .addBlazeFlags(buildResultHelper.getBuildFlags())
                            .addBlazeFlags(
                                    BlazeFlags.blazeFlags(
                                            project,
                                            projectViewSet,
                                            BlazeCommandName.BUILD,
                                            BlazeInvocationContext.SYNC_CONTEXT));

            aspectStrategy.addAspectAndOutputGroups(
                    builder,
                    AspectStrategy.OutputGroup.INFO,
                    getLanguageSet(workspaceLanguageSettings, projectViewSet)
            );

            ExternalTask.builder(workspaceRoot)
                    .addBlazeCommand(builder.build())
                    .context(parentContext)
                    .stderr(
                            LineProcessingOutputStream.of(
                                    BlazeConsoleLineProcessorProvider.getAllStderrLineProcessors(parentContext)))
                    .build()
                    .run(new TimingScope("ExecuteBlazeCommand", TimingScope.EventType.BlazeInvocation));
            try {
                fileList = buildResultHelper.getBuildArtifacts();
            } catch (BuildResultHelper.GetArtifactsException e) {
                e.printStackTrace();
            }
        }
        return fileList;
    }

    private static ImmutableSet<LanguageClass> getLanguageSet(
            WorkspaceLanguageSettings workspaceLanguageSettings,
            ProjectViewSet projectViewSet
    ) {
        WorkspaceType workspaceType = workspaceLanguageSettings.getWorkspaceType();
        ImmutableSet.Builder<LanguageClass> activeLanguages =
                ImmutableSet.<LanguageClass>builder()
                        .addAll(workspaceType.getLanguages())
                        .addAll(projectViewSet.listItems(AdditionalLanguagesSection.KEY))
                        .add(LanguageClass.GENERIC);
        Arrays.stream(BlazeSyncPlugin.EP_NAME.getExtensions())
                .forEach(plugin -> activeLanguages.addAll(plugin.getAlwaysActiveLanguages()));
        return activeLanguages.build();
    }

    private static String getBinaryPath(Project project) {
        BuildSystemProvider buildSystemProvider = Blaze.getBuildSystemProvider(project);
        return buildSystemProvider.getSyncBinaryPath(project);
    }

    private static class TargetFilePair {
        private final File file;
        private final TargetIdeInfo target;

        TargetFilePair(File file, TargetIdeInfo target) {
            this.file = file;
            this.target = target;
        }
    }
}
