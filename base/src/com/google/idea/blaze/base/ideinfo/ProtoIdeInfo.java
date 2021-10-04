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
package com.google.idea.blaze.base.ideinfo;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.devtools.intellij.ideinfo.IntellijIdeInfo;

import javax.annotation.Nullable;
import java.util.Objects;

/** Ide info specific to proto rules. */
public final class ProtoIdeInfo implements ProtoWrapper<IntellijIdeInfo.ProtoIdeInfo> {
  private final ImmutableList<ArtifactLocation> sources;
  @Nullable private final String sourceRoot;
  @Nullable private final String importPrefix;
  @Nullable private final String stripImportPrefix;

  private ProtoIdeInfo(
      ImmutableList<ArtifactLocation> sources,
      @Nullable String sourceRoot,
      @Nullable String importPrefix,
      @Nullable String stripImportPrefix) {
    this.sources = sources;
    this.sourceRoot = sourceRoot;
    this.importPrefix = importPrefix;
    this.stripImportPrefix = stripImportPrefix;
  }

  static ProtoIdeInfo fromProto(IntellijIdeInfo.ProtoIdeInfo proto) {
    return new ProtoIdeInfo(
        ProtoWrapper.map(proto.getSourcesList(), ArtifactLocation::fromProto),
        Strings.emptyToNull(proto.getSourceRoot()),
        Strings.emptyToNull(proto.getImportPrefix()),
        Strings.emptyToNull(proto.getStripImportPrefix()));
  }

  @Override
  public IntellijIdeInfo.ProtoIdeInfo toProto() {
    IntellijIdeInfo.ProtoIdeInfo.Builder builder =
        IntellijIdeInfo.ProtoIdeInfo.newBuilder().addAllSources(ProtoWrapper.mapToProtos(sources));
    ProtoWrapper.setIfNotNull(builder::setSourceRoot, sourceRoot);
    ProtoWrapper.setIfNotNull(builder::setImportPrefix, importPrefix);
    ProtoWrapper.setIfNotNull(builder::setStripImportPrefix, stripImportPrefix);
    return builder.build();
  }

  public ImmutableList<ArtifactLocation> getSources() {
    return sources;
  }

  @Nullable
  public String getSourceRoot() {
    return sourceRoot;
  }

  @Nullable
  public String getImportPrefix() {
    return importPrefix;
  }

  @Nullable
  public String getStripImportPrefix() {
    return stripImportPrefix;
  }

  public static Builder builder() {
    return new Builder();
  }

  /** Builder for proto info */
  public static class Builder {
    @Nullable String sourceRoot;
    @Nullable String importPrefix;
    @Nullable String stripImportPrefix;

    public Builder setSourceRoot(String sourceRoot) {
      this.sourceRoot = sourceRoot;
      return this;
    }

    public Builder setImportPrefix(String importPrefix) {
      this.importPrefix = importPrefix;
      return this;
    }

    public Builder setStripImportPrefix(String stripImportPrefix) {
      this.stripImportPrefix = stripImportPrefix;
      return this;
    }

    public ProtoIdeInfo build() {
      return new ProtoIdeInfo(ImmutableList.of(), sourceRoot, importPrefix, stripImportPrefix);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ProtoIdeInfo that = (ProtoIdeInfo) o;
    return Objects.equals(sources, that.sources)
        && Objects.equals(sourceRoot, that.sourceRoot)
        && Objects.equals(importPrefix, that.importPrefix)
        && Objects.equals(stripImportPrefix, that.stripImportPrefix);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sources, sourceRoot, importPrefix, stripImportPrefix);
  }
}
