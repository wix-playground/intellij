package com.google.idea.blaze.scala.fastbuild;

import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.java.fastbuild.FastBuildCompiler;
import com.google.idea.blaze.java.fastbuild.FastBuildCompilerExtensionPoint;
import com.google.idea.blaze.java.fastbuild.FastBuildException;
import com.google.idea.blaze.java.fastbuild.FastBuildIncrementalCompileException;
import java.io.File;
import java.util.List;
import java.util.Set;


public class FastBuildScalaCompilerExtensionPoint implements FastBuildCompilerExtensionPoint {

    @Override
    public FastBuildCompiler getCompiler(List<File> javacJars, List<File> bootClassPathJars, String sourceVersion, String targetVersion) {
            return new ScalacBsp();
    }

    @Override
    public boolean canCreateCompilerFor(Set<File> filesToCompile) {
        return !filesToCompile.isEmpty() && filesToCompile.stream().allMatch(f -> f.getName().endsWith(".scala") || f.getName().endsWith(".java"));
    }

    static class ScalacBsp implements FastBuildCompiler {
        @Override
        public void compile(BlazeContext context, CompileInstructions instructions)
            throws FastBuildException {
            try {
                final BloopProject bloopProject = new BloopProject(instructions.outputDirectory(), instructions.filesToCompile(), instructions.classpath());
                bloopProject.outputToDisk();
                final BspFastCompile bspFastCompile = new BspFastCompile(instructions.outputDirectory(), bloopProject, context);
                bspFastCompile.compileWithBsp();
            } catch (Exception e) {
                throw new FastBuildIncrementalCompileException(e);
            }
        }
    }


}
