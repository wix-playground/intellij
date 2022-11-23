package com.google.idea.blaze.java.fastbuild;

import com.intellij.openapi.extensions.ExtensionPointName;

import java.io.File;
import java.util.List;
import java.util.Set;

public interface FastBuildCompilerExtensionPoint {

    ExtensionPointName<FastBuildCompilerExtensionPoint> EP_NAME =
            ExtensionPointName.create("com.google.idea.blaze.FastBuildCompilerExtensionPoint");

    FastBuildCompiler getCompiler(List<File> javacJars, List<File> bootClassPathJars, String sourceVersion, String targetVersion)
        throws FastBuildException;

    boolean canCreateCompilerFor(Set<File> filesToCompile);

}
