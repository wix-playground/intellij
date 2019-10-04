package com.google.idea.blaze.base.search;

import com.google.common.collect.ImmutableList;
import com.google.idea.blaze.base.model.primitives.Label;
import com.google.idea.blaze.base.run.targetfinder.TargetFinder;
import com.google.idea.blaze.base.targetmaps.SourceToTargetMap;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.TestSourcesFilter;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BazelTestSourcesFilter extends TestSourcesFilter {

	@Override
	public boolean isTestSource(@NotNull VirtualFile file, @NotNull Project project) {
		ImmutableList<Label> targetsToBuildForSourceFile = SourceToTargetMap.getInstance(project)
				.getTargetsToBuildForSourceFile(VfsUtilCore.virtualToIoFile(file));
		if (!targetsToBuildForSourceFile.isEmpty()) {
			return Objects.requireNonNull(TargetFinder.findTargetInfo(project, targetsToBuildForSourceFile.get(0))).kindString.endsWith("_test");
		} else {
			return false;
		}
	}
}
