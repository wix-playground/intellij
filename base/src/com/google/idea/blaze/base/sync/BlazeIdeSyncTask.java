package com.google.idea.blaze.base.sync;

import com.google.idea.blaze.base.settings.BlazeImportSettings;
import com.intellij.openapi.project.Project;

final class BlazeIdeSyncTask extends BlazeSyncTask {
    BlazeIdeSyncTask(Project project, BlazeImportSettings importSettings, BlazeSyncParams syncParams) {
        super(project, importSettings, syncParams);
    }
}
