package com.google.idea.blaze.scala.libraries;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.vfs.VirtualFile;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

public class ScalaRootsDetector extends RootDetector {

  protected ScalaRootsDetector() {
    super(OrderRootType.SOURCES, false, "sources");
  }

  @NotNull
  @Override
  public Collection<VirtualFile> detectRoots(
      @NotNull VirtualFile rootCandidate, @NotNull ProgressIndicator progressIndicator
  ) {
    return ScalaVfsSourceRootDetectionUtil.suggestRoots(rootCandidate, progressIndicator);
  }
}
