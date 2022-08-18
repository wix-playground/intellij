package com.google.idea.blaze.scala.fastbuild;

import com.google.common.collect.ImmutableList;
import com.google.idea.blaze.base.command.info.BlazeInfo;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.java.fastbuild.FastBuildCompiler;
import com.google.idea.blaze.java.fastbuild.FastBuildCompilerExtensionPoint;
import com.google.idea.blaze.java.fastbuild.FastBuildException;

import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;


public class
FastBuildScalaCompilerExtensionPoint implements FastBuildCompilerExtensionPoint {

  private static final String FAST_BUILD_SCALAC_CLASS = "com.google.idea.blaze.scala.fastbuild.ScalaCompilerImpl2";
  private static final String FAST_BUILD_JAVAC_CLASS = "com.google.idea.blaze.java.fastbuild.FastBuildCompilerFactoryImpl";

  private static final Path FAST_BUILD_JAVAC_JAR =
      Paths.get("lib", "fast_build_scalac2_deploy.jar");
  private static final Path FAST_BUILD_JAVAC_JAR_OLD =
      Paths.get("lib", "fast_build_scalac2.jar");

  private static File findFastBuildJavacJar() {
    IdeaPluginDescriptor blazePlugin =
        PluginManager.getPlugin(
            PluginManager.getPluginByClassName(
                FAST_BUILD_JAVAC_CLASS)); //Why scalac doesn't work? unclear
    return Paths.get(blazePlugin.getPath().getAbsolutePath())
        .resolve(FAST_BUILD_JAVAC_JAR)
        .toFile();
  }

  @Override
  public FastBuildCompiler getCompiler(List<File> javacJars, List<File> bootClassPathJars,
      String sourceVersion, String targetVersion, BlazeInfo blazeInfo) {
    try {
      final File fastBuildJavacJar = findFastBuildJavacJar();
      Class<?> scalacClass = loadScalacClass(
          FAST_BUILD_SCALAC_CLASS,
          ImmutableList.<File>builder()
              .addAll(javacJars)
              .add(fastBuildJavacJar)
              .build());
              //.addAll(Arrays.asList(fastBuildJavacJar, fastBuildJavacJar.toPath().resolveSibling("fast_build_scalac2_java.jar").toFile()))

      Constructor<?> createMethod = scalacClass.getConstructor();
      Object scalacInstance = createMethod.newInstance();

      Path externalRepositoriesPath = blazeInfo.getExecutionRoot().toPath().resolve("external");
      List<File> scalaSdkJars = Arrays.asList(
          externalRepositoriesPath.resolve("org_scala_lang_scala_library")
              .resolve("scala-library-2.12.13.jar").toFile(),
          externalRepositoriesPath.resolve("org_scala_lang_scala_compiler")
              .resolve("scala-compiler-2.12.13.jar").toFile(),
          externalRepositoriesPath.resolve("org_scala_lang_scala_reflect")
              .resolve("scala-reflect-2.12.13.jar").toFile()
      );
      IdeaPluginDescriptor blazePlugin = PluginManager.getPlugin(PluginManager.getPluginByClassName("com.google.idea.blaze.java.fastbuild.FastBuildCompilerFactoryImpl"));
      File compilerBridge = Paths.get(blazePlugin.getPath().getAbsolutePath()).resolve("lib").resolve("compiler_bridge_2_12.jar").toFile();

      return new Scalac(scalacInstance, bootClassPathJars, scalaSdkJars, compilerBridge);
    } catch (MalformedURLException | ReflectiveOperationException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  static class Scalac implements FastBuildCompiler {

    private final Object compiler;
    private final List<File> bootClasses;

    private final List<File> scalaSdkJars;
    private final File compilerBridge;

    Scalac(Object compiler, List<File> bootClasses, List<File> scalaSdkJars, File compilerBridge) {
      this.compiler = compiler;
      this.bootClasses = bootClasses;
      this.scalaSdkJars = scalaSdkJars;
      this.compilerBridge = compilerBridge;
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
            bootClasses,
            scalaSdkJars,
            compilerBridge
        );
      } catch (Exception e) {
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
