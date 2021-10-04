/*
 * Copyright 2017 The Bazel Authors. All rights reserved.
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
package com.google.idea.blaze.aspect.proto.pl;

import com.google.devtools.intellij.IntellijAspectTestFixtureOuterClass.IntellijAspectTestFixture;
import com.google.devtools.intellij.ideinfo.IntellijIdeInfo.TargetIdeInfo;
import com.google.idea.blaze.BazelIntellijAspectTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;

/** Tests for proto_library. */
@RunWith(JUnit4.class)
public class ProtoLibraryTest extends BazelIntellijAspectTest {

  @Test
  public void testProtoLibrary() throws Exception {
    IntellijAspectTestFixture fixture = loadTestFixture(":fixture");

    TargetIdeInfo aProto = findTarget(fixture, ":a_proto");
    assertThat(aProto).isNotNull();
    assertThat(aProto.hasProtoIdeInfo()).isTrue();
    assertThat(relativePathsForArtifacts(aProto.getProtoIdeInfo().getSourcesList()))
        .containsExactly(testRelative("a.proto"));
    assertThat(aProto.getProtoIdeInfo().getSourceRoot())
        .isEqualTo(
            "bazel-out/darwin-fastbuild/bin/aspect/testing/tests/src/com/google/idea/blaze/aspect/proto/pl/_virtual_imports/a_proto");
    assertThat(aProto.getProtoIdeInfo().getImportPrefix()).isEqualTo("test");
    assertThat(aProto.getProtoIdeInfo().getStripImportPrefix())
        .isEqualTo("/aspect/testing/tests/src/com/google/idea/blaze/aspect/proto/pl");

    TargetIdeInfo bProto = findTarget(fixture, ":b_proto");
    assertThat(bProto).isNotNull();
    assertThat(bProto.hasProtoIdeInfo()).isTrue();
    assertThat(relativePathsForArtifacts(bProto.getProtoIdeInfo().getSourcesList()))
        .containsExactly(testRelative("b.proto"));
    assertThat(bProto.getProtoIdeInfo().getSourceRoot()).isEqualTo(".");
    assertThat(bProto.getProtoIdeInfo().getImportPrefix()).isEmpty();
    assertThat(bProto.getProtoIdeInfo().getStripImportPrefix()).isEmpty();
    assertThat(dependenciesForTarget(bProto)).contains(dep(aProto));

    assertThat(getOutputGroupFiles(fixture, "intellij-info-proto"))
        .containsExactly(
            testRelative(intellijInfoFileName(aProto.getKey())),
            testRelative(intellijInfoFileName(bProto.getKey())));

    assertThat(getOutputGroupFiles(fixture, "intellij-compile-proto"))
        .containsExactly(
            testRelative("a_proto-descriptor-set.proto.bin"),
            testRelative("b_proto-descriptor-set.proto.bin"));

    assertThat(getOutputGroupFiles(fixture, "intellij-resolve-proto"))
        .containsExactly(
            testRelative("a_proto-descriptor-set.proto.bin"),
            testRelative("b_proto-descriptor-set.proto.bin"));
  }
}
