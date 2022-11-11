package com.google.idea.blaze.scala.libraries;

import com.intellij.ide.util.projectWizard.importSources.JavaSourceRootDetectionUtil;
import com.intellij.openapi.fileEditor.impl.LoadTextUtil;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.util.SystemInfo;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileVisitor;
import com.intellij.util.containers.ContainerUtil;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.plugins.scala.ScalaFileType;

/*
  Note: copy from IntelliJ source with a change to support ScalaFileType
 */
public class ScalaVfsSourceRootDetectionUtil {

  private ScalaVfsSourceRootDetectionUtil() {
  }

  @NotNull
  public static List<VirtualFile> suggestRoots(
      @NotNull VirtualFile dir, @NotNull final ProgressIndicator progressIndicator
  ) {
    if (!dir.isDirectory()) {
      return ContainerUtil.emptyList();
    }

    final FileTypeManager typeManager = FileTypeManager.getInstance();
    final ArrayList<VirtualFile> foundDirectories = new ArrayList<>();
    try {
      VfsUtilCore.visitChildrenRecursively(dir, new VirtualFileVisitor() {
        @NotNull
        @Override
        public Result visitFileEx(@NotNull VirtualFile file) {
          progressIndicator.checkCanceled();

          if (file.isDirectory()) {
            if (typeManager.isFileIgnored(file) || StringUtil
                .startsWithIgnoreCase(file.getName(), "testData")) {
              return SKIP_CHILDREN;
            }
          } else {
            FileType type = typeManager.getFileTypeByFileName(file.getNameSequence());

            // Change: instead of JAVA, looks for ScalaFileType
            if (ScalaFileType.INSTANCE == type) {
              VirtualFile root = suggestRootForJavaFile(file);
              if (root != null) {
                foundDirectories.add(root);
                return skipTo(root);
              }
            }
          }

          return CONTINUE;
        }
      });
    } catch (ProcessCanceledException ignore) {
    }

    return foundDirectories;
  }

  @Nullable
  private static VirtualFile suggestRootForJavaFile(VirtualFile javaFile) {
    if (javaFile.isDirectory()) {
      return null;
    }

    CharSequence chars = LoadTextUtil.loadText(javaFile);

    String packageName = JavaSourceRootDetectionUtil.getPackageName(chars);
    if (packageName != null) {
      VirtualFile root = javaFile.getParent();
      int index = packageName.length();

      VirtualFile lastRoot = null;
      while (index > 0) {
        int index1 = packageName.lastIndexOf('.', index - 1);
        String token = packageName.substring(index1 + 1, index);
        String dirName = root.getName();
        final boolean equalsToToken = SystemInfo.isFileSystemCaseSensitive ? dirName.equals(token)
            : dirName.equalsIgnoreCase(token);
        if (!equalsToToken) {
          // return last package part matching root
          return lastRoot;
        }
        root = root.getParent();
        if (root == null) {
          return null;
        }
        lastRoot = root;
        index = index1;
      }
      return root;
    }

    return null;
  }
}
