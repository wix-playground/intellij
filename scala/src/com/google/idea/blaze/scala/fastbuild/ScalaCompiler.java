package com.google.idea.blaze.scala.fastbuild;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface ScalaCompiler {

    void compile(
            Set<File> files,
            List<File> classpath,
            File destination,
            List<File> bootClasses
    ) throws IOException;

}
