package com.google.idea.blaze.base.sync.autosync;

import com.google.common.collect.ImmutableSet;
import com.google.idea.blaze.base.async.executor.ProgressiveTaskWithProgressIndicator;
import com.google.idea.blaze.base.settings.Blaze;
import com.google.idea.blaze.base.settings.BlazeImportSettings;
import com.google.idea.blaze.base.settings.BlazeImportSettingsManager;
import com.google.idea.blaze.base.sync.BlazeSyncParams;
import com.google.idea.blaze.base.sync.BlazeSyncTask;
import com.google.idea.blaze.base.sync.SyncMode;
import com.google.idea.blaze.base.sync.inmemory.BlazeInMemorySyncTask;
import com.google.idea.blaze.base.sync.status.BlazeSyncStatus;
import com.google.idea.common.experiments.BoolExperiment;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileEvent;
import com.intellij.openapi.vfs.VirtualFileListener;
import com.intellij.openapi.vfs.VirtualFileManager;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Future;

public class InMemorySyncHandler implements ProjectComponent {
    private static final BoolExperiment autoSyncEnabled =
            new BoolExperiment("blaze.in.memory.auto.sync.enabled", true);
    private final Project project;

    private final PendingChangesHandler<VirtualFile> pendingChangesHandler =
            new PendingChangesHandler<VirtualFile>(/* delayMillis= */ 2000) {
                @Override
                boolean runTask(ImmutableSet<VirtualFile> changes) {
                    if (BlazeSyncStatus.getInstance(project).syncInProgress()) {
                        return false;
                    }
                    queueAutomaticSync(changes);
                    return true;
                }
            };

    private void queueAutomaticSync(ImmutableSet<VirtualFile> changes) {
        BlazeImportSettings importSettings = BlazeImportSettingsManager.getInstance(project).getImportSettings();
        if (importSettings == null) {
            throw new IllegalStateException(
                    String.format("Attempt to sync non-%s project.", Blaze.buildSystemName(project))
            );
        }
        BlazeSyncParams params =
                new BlazeSyncParams.Builder("In-memory update", SyncMode.NO_BUILD)
                        .setBackgroundSync(true)
                        .build();

        submitTask(new BlazeInMemorySyncTask(project, importSettings, params));
    }

    private void submitTask(BlazeSyncTask task) {
        @SuppressWarnings("unused") // go/futurereturn-lsc
                Future<?> possiblyIgnoredError =
                ProgressiveTaskWithProgressIndicator.builder(project, "Syncing Project")
                        .submitTask(task);
    }

    public InMemorySyncHandler(Project project) {
        this.project = project;

        // listen for changes to the VFS
        VirtualFileManager.getInstance().addVirtualFileListener(new FileListener(), project);
    }

    private class FileListener implements VirtualFileListener {
        @Override
        public void fileCreated(@NotNull VirtualFileEvent event) {
            VirtualFile file = event.getFile();
            if (autoSyncEnabled.getValue()) {
                pendingChangesHandler.queueChange(file);
            }
        }
    }
}