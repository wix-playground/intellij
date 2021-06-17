package com.google.idea.blaze.scala.fastbuild;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ScalaCompilerImpl implements ScalaCompiler {

    private final File SCALA_SDK = new File("/Users/domasma/jdks/scala-2.13.6/bin/scalac");

    public void compile(
            Set<File> files,
            List<File> classpath,
            File destination,
            List<File> bootClasses
    ) throws IOException {

        String classpathArg = classpath.stream().map(File::getAbsolutePath).collect(Collectors.joining(":"));
        String bootClassesArg = bootClasses.stream().map(File::getAbsolutePath).collect(Collectors.joining(":"));


        List<String> commands = new ArrayList<>();
        commands.addAll(Arrays.asList(
                SCALA_SDK.getAbsolutePath(),
                "-d", destination.getPath(),
                "-target:8",
                "-classpath", classpathArg,
                "-g:source",
                "-bootclasspath", bootClassesArg
        ));

        commands.addAll(
                files.stream()
                        .map(File::getAbsolutePath)
                        .collect(Collectors.toList()));

        ProcessBuilder pb = new ProcessBuilder()
                .command(commands);

        try {
            pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
            Process p = pb.start();


//            Scanner stdio = new Scanner(p.getInputStream()).useDelimiter("\\A");
//            Scanner stderr = new Scanner(p.getErrorStream()).useDelimiter("\\A");
//
//            while (stdio.hasNext()) {
//                System.out.println(stdio.next());
//            }
//
//            while (stderr.hasNext()) {
//                System.out.println(stderr.next());
//            }

//            int result = p.waitFor();
//            System.out.println(result);

            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

    }

}
