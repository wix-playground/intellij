/*
 * Copyright 2016 The Bazel Authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.idea.blaze.java.sync.model;

import com.google.common.base.Objects;
import com.google.devtools.intellij.model.ProjectData;
import com.google.idea.blaze.base.ideinfo.ArtifactLocation;
import com.google.idea.blaze.base.ideinfo.LibraryArtifact;
import com.google.idea.blaze.base.model.BlazeLibrary;
import com.google.idea.blaze.base.model.LibraryKey;
import com.google.idea.blaze.base.sync.workspace.ArtifactLocationDecoder;
import com.google.idea.blaze.java.libraries.AttachedSourceJarManager;
import com.google.idea.blaze.java.libraries.JarCache;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.OrderRootType;
import com.intellij.openapi.roots.libraries.Library;
import com.intellij.openapi.roots.libraries.ui.RootDetector;
import com.intellij.openapi.roots.ui.configuration.LibrarySourceRootDetectorUtil;
import com.intellij.openapi.vfs.JarFileSystem;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/** An immutable reference to a .jar required by a rule. */
@Immutable
public final class BlazeJarLibrary extends BlazeLibrary {

  private static final Logger logger = Logger.getInstance(BlazeJarLibrary.class);

  public final LibraryArtifact libraryArtifact;

  public BlazeJarLibrary(LibraryArtifact libraryArtifact) {
    super(LibraryKey.fromArtifactLocation(libraryArtifact.jarForIntellijLibrary()));
    this.libraryArtifact = libraryArtifact;
  }

  public static BlazeJarLibrary fromProto(ProjectData.BlazeLibrary proto) {
    return new BlazeJarLibrary(
        LibraryArtifact.fromProto(proto.getBlazeJarLibrary().getLibraryArtifact()));
  }

  @Override
  public ProjectData.BlazeLibrary toProto() {
    return super.toProto()
        .toBuilder()
        .setBlazeJarLibrary(
            ProjectData.BlazeJarLibrary.newBuilder()
                .setLibraryArtifact(libraryArtifact.toProto())
                .build())
        .build();
  }

  @Override
  public void modifyLibraryModel(
      Project project,
      ArtifactLocationDecoder artifactLocationDecoder,
      Library.ModifiableModel libraryModel) {
    JarCache jarCache = JarCache.getInstance(project);
    File jar = jarCache.getCachedJar(artifactLocationDecoder, this);
    if (jar != null) {
      libraryModel.addRoot(pathToUrl(jar), OrderRootType.CLASSES);
    } else {
      logger.error("No local jar file found for " + libraryArtifact.jarForIntellijLibrary());
    }

    AttachedSourceJarManager sourceJarManager = AttachedSourceJarManager.getInstance(project);
    if (!sourceJarManager.hasSourceJarAttached(key)) {
      return;
    }
    for (ArtifactLocation srcJar : libraryArtifact.getSourceJars()) {
      File sourceJar = jarCache.getCachedSourceJar(artifactLocationDecoder, srcJar);

      if (sourceJar != null) {
        detectSourceRoots(sourceJar).forEach(root -> {
          libraryModel.addRoot(root, OrderRootType.SOURCES);
        });
      }
    }
  }

  private List<VirtualFile> detectSourceRoots(File sourceJar) {
    List<VirtualFile> roots = new ArrayList<>();

    VirtualFile srcFile = LocalFileSystem.getInstance().refreshAndFindFileByIoFile(sourceJar);
    if (srcFile == null) {
      return roots;
    }

    VirtualFile jarRoot = JarFileSystem.getInstance().getJarRootForLocalFile(srcFile);
    if (jarRoot == null) {
      return roots;
    }

    List<RootDetector> detectors = LibrarySourceRootDetectorUtil.JAVA_SOURCE_ROOT_DETECTOR.getExtensionList();

    return detect(detectors, jarRoot);
  }

  private List<VirtualFile> detect(List<RootDetector> detectors, VirtualFile jarRoot) {
    List<VirtualFile> roots = new ArrayList<>();

    for (RootDetector detector : detectors) {
      // fixme: needs to be done as async cancelable task
      EmptyProgressIndicator dummyIndicator = new EmptyProgressIndicator();
      roots.addAll(detector.detectRoots(jarRoot, dummyIndicator));
    }

    return roots;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(super.hashCode(), libraryArtifact);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (!(other instanceof BlazeJarLibrary)) {
      return false;
    }

    BlazeJarLibrary that = (BlazeJarLibrary) other;

    return super.equals(other) && Objects.equal(libraryArtifact, that.libraryArtifact);
  }
}
