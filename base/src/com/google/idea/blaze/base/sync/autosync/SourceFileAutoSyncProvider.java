package com.google.idea.blaze.base.sync.autosync;

import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.primitives.TargetExpression;
import com.google.idea.blaze.base.model.primitives.WorkspacePath;
import com.google.idea.blaze.base.settings.BlazeUserSettings;
import com.google.idea.blaze.base.sync.BlazeSyncParams;
import com.google.idea.blaze.base.sync.SyncMode;
import com.google.idea.blaze.base.sync.data.BlazeProjectDataManager;
import com.google.idea.blaze.base.syncstatus.SyncStatusContributor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import javax.annotation.Nullable;
import java.io.File;

public class SourceFileAutoSyncProvider implements AutoSyncProvider {

    @Override
    public boolean isSyncSensitiveFile(Project project, VirtualFile file) {
        if (!BlazeUserSettings.getInstance().getSourceAutoSync()) {
            return false;
        }
        if (file.getExtension() == null) {
            return false;
        }
        
        // todo hack: compare this against file type
        return (file.getExtension().endsWith("java") || file.getExtension().endsWith("scala"))
                && SyncStatusContributor.isUnsynced(project, file);
    }

    @Nullable
    private static WorkspacePath getWorkspacePath(Project project, VirtualFile file) {
        BlazeProjectData projectData =
                BlazeProjectDataManager.getInstance(project).getBlazeProjectData();
        return projectData != null
                ? projectData.getWorkspacePathResolver().getWorkspacePath(new File(file.getPath()))
                : null;
    }

    @Nullable
    @Override
    public BlazeSyncParams getAutoSyncParamsForFile(Project project, VirtualFile modifiedFile) {
        if (!isSyncSensitiveFile(project, modifiedFile)) {
            return null;
        }

        WorkspacePath path = getWorkspacePath(project, modifiedFile);
        if (path == null || path.getParent() == null) {
            return null;
        }

        return new BlazeSyncParams.Builder(AUTO_SYNC_TITLE, SyncMode.NO_BUILD)
                .addTargetExpression(TargetExpression.allFromPackageNonRecursive(path.getParent()))
                .setBackgroundSync(true)
                .build();

    }
}
