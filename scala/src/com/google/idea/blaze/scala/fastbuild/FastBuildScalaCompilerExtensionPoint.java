package com.google.idea.blaze.scala.fastbuild;

import com.google.common.collect.ImmutableList;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.java.fastbuild.FastBuildCompiler;
import com.google.idea.blaze.java.fastbuild.FastBuildCompilerExtensionPoint;
import com.google.idea.blaze.java.fastbuild.FastBuildException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;


public class FastBuildScalaCompilerExtensionPoint implements FastBuildCompilerExtensionPoint {

    private static final String FAST_BUILD_SCALAC_CLASS = "com.google.idea.blaze.scala.fastbuild.ScalaCompilerImpl";

    // TODO Should be dynamically loaded from plugin's directory
    private static final File FAST_BUILD_SCALAC_LIB =
            new File("/Users/domasma/Library/Caches/JetBrains/IntelliJIdea2020.1/plugins-sandbox/plugins/ijwb/lib/libfast_build_scalac.jar");

    @Override
    public FastBuildCompiler getCompiler(List<File> javacJars, List<File> bootClassPathJars, String sourceVersion, String targetVersion) {
        try {
            Class<?> javacClass = loadScalacClass(
                    FAST_BUILD_SCALAC_CLASS,
                    ImmutableList.<File>builder()
                            .addAll(javacJars)
                            .add(FAST_BUILD_SCALAC_LIB)
                            .build());

            Constructor<?> createMethod = javacClass.getConstructor();
            Object scalacInstance = createMethod.newInstance();

            return new Scalac(scalacInstance, bootClassPathJars);
        } catch (MalformedURLException | ReflectiveOperationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    static class Scalac implements FastBuildCompiler {

        private final Object compiler;
        private final List<File> bootClasses;

        Scalac(Object compiler, List<File> bootClasses) {
            this.compiler = compiler;
            this.bootClasses = bootClasses;
        }

        @Override
        public void compile(BlazeContext context, CompileInstructions instructions) throws FastBuildException {
            try {
                Method compileMethod = Arrays.stream(this.compiler.getClass().getMethods())
                        .filter(m -> m.getName().equals("compile"))
                        .findAny()
                        .get();

                // Bazel complains when interface is used
                // Invoke scala/src/com/google/idea/blaze/scala/fastbuild/ScalaCompiler.java:10
                compileMethod.invoke(
                        this.compiler,
                        instructions.filesToCompile(),
                        instructions.classpath(),
                        instructions.outputDirectory(),
                        bootClasses
                );
            } catch (Exception e ) {
                e.printStackTrace();
                throw new RuntimeException(e); // TODO handle
            }
        }
    }




    // Copy Paste vvvv
    private Class<?> loadScalacClass(String scalaCompilerClass, List<File> jars)
            throws MalformedURLException, ClassNotFoundException {
        URL[] urls = new URL[jars.size()];
        for (int i = 0; i < jars.size(); ++i) {
            urls[i] = jars.get(i).toURI().toURL();
        }
        URLClassLoader urlClassLoader = new URLClassLoader(urls, platformClassLoader());
        return urlClassLoader.loadClass(scalaCompilerClass);
    }

    private static ClassLoader platformClassLoader() {
        try {
            return (ClassLoader) ClassLoader.class.getMethod("getPlatformClassLoader").invoke(null);
        } catch (ReflectiveOperationException e) {
            // Java 8
            return null;
        }
    }
}
