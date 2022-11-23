/*
 * Copyright 2018 The Bazel Authors. All rights reserved.
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
package com.google.idea.blaze.java.fastbuild;

import static com.google.common.base.Preconditions.checkState;

import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.primitives.Label;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.scope.output.IssueOutput;
import com.google.idea.blaze.base.scope.output.IssueOutput.Category;
import com.google.idea.blaze.base.scope.output.StatusOutput;
import com.google.idea.blaze.base.sync.data.BlazeProjectDataManager;
import com.google.idea.blaze.common.PrintOutput;
import com.google.idea.blaze.java.fastbuild.FastBuildBlazeData.JavaToolchainInfo;
import com.intellij.openapi.project.Project;
import com.intellij.serviceContainer.NonInjectable;
import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class FastBuildCompilerFactoryImpl implements FastBuildCompilerFactory {

  private final BlazeProjectDataManager projectDataManager;

  @NonInjectable
  private FastBuildCompilerFactoryImpl(
      BlazeProjectDataManager projectDataManager) {
    this.projectDataManager = projectDataManager;
  }

  FastBuildCompilerFactoryImpl(Project project) {
    this(
        BlazeProjectDataManager.getInstance(project)
    );
  }

  static FastBuildCompilerFactoryImpl createForTest(
      BlazeProjectDataManager projectDataManager) {
    return new FastBuildCompilerFactoryImpl(
        projectDataManager);
  }

  @Override
  public FastBuildCompiler getCompilerFor(Label label, Map<Label, FastBuildBlazeData> blazeData, Set<File> filesToCompile)
      throws FastBuildException {
    JavaToolchainInfo javaToolchain = getJavaToolchain(label, blazeData);

    BlazeProjectData projectData = projectDataManager.getBlazeProjectData();
    checkState(projectData != null, "not a blaze project");
    List<File> javacJars =
        projectData.getArtifactLocationDecoder().decodeAll(javaToolchain.javacJars());
    List<File> bootJars =
        projectData.getArtifactLocationDecoder().decodeAll(javaToolchain.bootClasspathJars());

    final FastBuildCompilerExtensionPoint fastBuildCompilerExtensionPoint =
        Arrays.stream(FastBuildCompilerExtensionPoint.EP_NAME.getExtensions())
        .filter(ep -> ep.canCreateCompilerFor(filesToCompile))
        .findFirst()
        .orElseThrow(() -> new RuntimeException("Couldn't find a FastBuildCompiler for needed files " + filesToCompile));

    return fastBuildCompilerExtensionPoint
        .getCompiler(javacJars, bootJars, javaToolchain.sourceVersion(), javaToolchain.targetVersion());
  }

  public JavaToolchainInfo getJavaToolchain(Label label, Map<Label, FastBuildBlazeData> blazeData)
      throws FastBuildException {
    FastBuildBlazeData targetData = blazeData.get(label);
    Set<JavaToolchainInfo> javaToolchains = new HashSet<>();
    if (targetData.javaToolchainInfo().isPresent()) {
      javaToolchains.add(targetData.javaToolchainInfo().get());
    }
    for (Label dependency : targetData.dependencies()) {
      FastBuildBlazeData depInfo = blazeData.get(dependency);
      if (depInfo != null && depInfo.javaToolchainInfo().isPresent()) {
        javaToolchains.add(depInfo.javaToolchainInfo().get());
      }
    }
    if (javaToolchains.isEmpty()) {
      throw new FastBuildException(
          "Couldn't find a Java toolchain for target " + targetData.label());
    }
    if (javaToolchains.size() > 1) {
      throw new FastBuildException(
          "Found multiple Java toolchains for target " + targetData.label());
    }
    return javaToolchains.iterator().next();
  }

}
