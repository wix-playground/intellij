package com.google.idea.blaze.base.search;

import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.sync.data.BlazeProjectDataManager;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.project.Project;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.GlobalSearchScopesCore;
import com.intellij.psi.search.PredefinedSearchScopeProviderImpl;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BazelOptionalTestScopeSearchProvider extends PredefinedSearchScopeProviderImpl {

	@Override
	public List<SearchScope> getPredefinedScopes(@NotNull Project project, @Nullable DataContext dataContext,
	                                             boolean suggestSearchInLibs, boolean prevSearchFiles,
	                                             boolean currentSelection, boolean usageView, boolean showEmptyScopes) {
		List<SearchScope> predefinedScopes = super.getPredefinedScopes(project, dataContext, suggestSearchInLibs, prevSearchFiles, currentSelection, usageView, showEmptyScopes);

		BlazeProjectData projectData =
				BlazeProjectDataManager.getInstance(project).getBlazeProjectData();

		boolean hasTestTargets = Optional.ofNullable(projectData)
				.map(data -> data
						.getTargetMap()
						.targets()
						.stream()
						.anyMatch(targetIdeInfo -> targetIdeInfo.getKind().getKindString().endsWith("_test"))
				).orElse(false);
		if (hasTestTargets) {
			int index = predefinedScopes.indexOf(GlobalSearchScope.projectScope(project));
			index = index == -1 ? 2 : index; // just leave both scopes at the top of the list

			predefinedScopes.add(index + 1, GlobalSearchScopesCore.projectProductionScope(project));
			predefinedScopes.add(index + 2, GlobalSearchScopesCore.projectTestScope(project));
		}

		return predefinedScopes;
	}
}
