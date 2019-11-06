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
package com.google.idea.blaze.scala.run.producers;

import static com.google.common.truth.Truth.assertThat;

import com.google.idea.blaze.base.ideinfo.JavaIdeInfo;
import com.google.idea.blaze.base.ideinfo.TargetIdeInfo;
import com.google.idea.blaze.base.ideinfo.TargetMapBuilder;
import com.google.idea.blaze.base.model.MockBlazeProjectDataBuilder;
import com.google.idea.blaze.base.model.MockBlazeProjectDataManager;
import com.google.idea.blaze.base.model.primitives.TargetExpression;
import com.google.idea.blaze.base.model.primitives.WorkspacePath;
import com.google.idea.blaze.base.run.producer.BlazeRunConfigurationProducerTestCase;
import com.google.idea.blaze.base.settings.BuildSystem;
import com.google.idea.blaze.base.sync.data.BlazeProjectDataManager;
import com.intellij.execution.application.ApplicationConfiguration;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.psi.PsiFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration tests for {@link GenerateDeployableJarTaskProvider}. */
@RunWith(JUnit4.class)
public class GenerateDeployableJarTaskProviderTest
    extends BlazeRunConfigurationProducerTestCase {

  @Override
  protected BuildSystem buildSystem() {
    return BuildSystem.Bazel;
  }

  @Test
  public void testCorrectMainAppAndTargetAndBeforeLaunchStepChosen() {
    MockBlazeProjectDataBuilder builder = MockBlazeProjectDataBuilder.builder(workspaceRoot);
    builder.setTargetMap(
        TargetMapBuilder.builder()
            .addTarget(
                TargetIdeInfo.builder()
                    .setKind("scala_library")
                    .setLabel("//com/google/library:SomeLibrary")
                    .addSource(sourceRoot("com/google/library/SomeLibrary.scala"))
                    .setJavaInfo(JavaIdeInfo.builder())
                    .build())
            .build());

    registerProjectService(
        BlazeProjectDataManager.class, new MockBlazeProjectDataManager(builder.build()));


    PsiFile scalaFile =
        createAndIndexFile(
            WorkspacePath.createIfValid("com/google/library/SomeLibrary.scala"),
            "package com.google.library {",
            "  object Foo {",
            "    def main(args: Array[String]) {}",
            "  }",
            "}",
            "package scala { final class Array[T] {} }",
            "package java.lang { public final class String {} }");

    RunConfiguration config = createConfigurationFromLocation(scalaFile);
    ApplicationConfiguration appConfig = assertApplicationConfiguration(config);

    assertMainClassIs("com.google.library.Foo", appConfig);
    assertTargetLabelIs("//com/google/library:SomeLibrary", appConfig);
    assertBeforeLaunchSingleStepIs("GenerateDeployableJarTarget", appConfig);
  }

  private ApplicationConfiguration assertApplicationConfiguration(RunConfiguration config) {
    assertThat(config).isInstanceOf(ApplicationConfiguration.class);
    ApplicationConfiguration appConfig = (ApplicationConfiguration) config;
    assertThat(appConfig).isNotNull();
    return appConfig;
  }

  private void assertMainClassIs(String mainClassName, ApplicationConfiguration appConfig) {
    assertThat(appConfig.getMainClass()).isNotNull();
    assertThat(appConfig.getMainClass().getQualifiedName()).isEqualTo(mainClassName);
  }

  private void assertTargetLabelIs(String targetLabel, ApplicationConfiguration appConfig) {
    assertThat(appConfig.getUserData(DeployableJarRunConfigurationProducer.TARGET_LABEL))
        .isNotNull();

    assertThat(appConfig.getUserData(DeployableJarRunConfigurationProducer.TARGET_LABEL))
        .isEqualTo(TargetExpression.fromStringSafe(targetLabel));
  }

  private void assertBeforeLaunchSingleStepIs(String stepName, ApplicationConfiguration appConfig) {
    assertThat(appConfig.getBeforeRunTasks()).isNotNull();
    assertThat(appConfig.getBeforeRunTasks().size()).isEqualTo(1);
    assertThat(appConfig.getBeforeRunTasks().get(0).getProviderId().toString()).isEqualTo(stepName);
  }
}
