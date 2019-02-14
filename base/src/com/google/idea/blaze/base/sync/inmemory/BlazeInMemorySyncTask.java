package com.google.idea.blaze.base.sync.inmemory;

import com.google.idea.blaze.base.command.info.BlazeConfigurationHandler;
import com.google.idea.blaze.base.command.info.BlazeInfo;
import com.google.idea.blaze.base.ideinfo.TargetMap;
import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.BlazeVersionData;
import com.google.idea.blaze.base.model.SyncState;
import com.google.idea.blaze.base.projectview.ProjectViewSet;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.settings.BlazeImportSettings;
import com.google.idea.blaze.base.sync.BlazeSyncParams;
import com.google.idea.blaze.base.sync.BlazeSyncTask;
import com.google.idea.blaze.base.sync.aspects.BlazeIdeInterface;
import com.google.idea.blaze.base.sync.projectview.WorkspaceLanguageSettings;
import com.google.idea.blaze.base.sync.sharding.ShardedTargetList;
import com.google.idea.blaze.base.sync.workspace.ArtifactLocationDecoder;
import com.intellij.openapi.project.Project;
import org.apache.commons.lang.NotImplementedException;

import javax.annotation.Nullable;
import java.util.List;

final class BlazeInMemorySyncTask extends BlazeSyncTask {
    BlazeInMemorySyncTask(Project project, BlazeImportSettings importSettings, BlazeSyncParams syncParams) {
        super(project, importSettings, syncParams);
    }

    @Override
    protected BlazeIdeInterface.IdeResult getIdeQueryResult(Project project, BlazeContext parentContext, ProjectViewSet projectViewSet, BlazeInfo blazeInfo, BlazeVersionData blazeVersionData, BlazeConfigurationHandler configHandler, ShardedTargetList shardedTargets, WorkspaceLanguageSettings workspaceLanguageSettings, ArtifactLocationDecoder artifactLocationDecoder, SyncState.Builder syncStateBuilder, @Nullable SyncState previousSyncState, boolean mergeWithOldState, @Nullable TargetMap oldTargetMap) {
        // todo update the map
        throw new NotImplementedException();

    }

    @Override
    protected BlazeInfo getBlazeInfo(BlazeContext context, List<String> syncFlags, BlazeProjectData oldBlazeProjectData) {
        return oldBlazeProjectData.getBlazeInfo();
    }
}
