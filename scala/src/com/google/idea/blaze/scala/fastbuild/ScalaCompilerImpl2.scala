package com.google.idea.blaze.scala.fastbuild

import sbt.internal.inc._
import sbt.internal.inc.classpath.ClassLoaderCache
import sbt.util.{Level, Logger}
import xsbti.compile.{CompilerCache, ScalaInstance, _}

import java.io.File
import java.net.URLClassLoader
import java.nio.file.Path
import java.util
import java.util.Optional
import scala.collection.JavaConverters._
import ScalaCompilerImpl2._
object ScalaCompilerImpl2 {

  private val classloaderCache = new ClassLoaderCache(new URLClassLoader(Array()))

  private val compilerCache = CompilerCache.fresh

  private var lastCompiler: AnyRef = null

  //TODO better logging?
  private val logger = new Logger {
    override def trace(t: => Throwable): Unit = {}

    override def success(message: => String): Unit = {}

    override def log(level: Level.Value, message: => String): Unit = {}
  }

}

final class NoOpPerClasspathEntryLookup(analyses: Path => Option[CompileAnalysis]) extends PerClasspathEntryLookup {
  override def analysis(classpathEntry: File): Optional[CompileAnalysis] =
    analyses(classpathEntry.toPath).fold(Optional.empty[CompileAnalysis])(Optional.of(_))
  override def definesClass(classpathEntry: File): DefinesClass =
    Locate.definesClass(classpathEntry)
}
final class HardcodedScalaInstance(val allJars: Array[File]) extends ScalaInstance {

  override def version: String = actualVersion
  override lazy val actualVersion: String = "2.12.13"

  override def compilerJar: File = null

  override def otherJars = Array.empty[File]

  override lazy val loader: URLClassLoader = {
    new URLClassLoader(allJars.map(_.toURI.toURL), null)
  }

  override def loaderLibraryOnly: ClassLoader = null

  override def libraryJars(): Array[File] = allJars.filter(_.getName.contains("scala-library"))
}

class ScalaCompilerImpl2 extends ScalaCompiler {
  override def compile(files: util.Set[File], classpath: util.List[File], destination: File, bootClasses: util.List[File], scalaSdkJars: util.List[File], compilerBridge: File): Unit = {

    val sources = files.asScala.toSet
    val classpathSeq = classpath.asScala.toSeq
    val scalaSdkJarsSeq = scalaSdkJars.asScala.toSeq

    val previousResult = PreviousResult.of(Optional.empty[CompileAnalysis](), Optional.empty[MiniSetup]())


    val scalaInstance = new HardcodedScalaInstance(scalaSdkJarsSeq.toArray)

    val scalaCompilerPlugins = Seq[String]() // ???
    val scalaCompilerOptions = Seq[String]() // ???
    val javaCompilerOptions = Seq[String]() // ???
    val compileOptions =
      CompileOptions.create
        .withSources(sources.map(_.getAbsoluteFile).toArray)
        .withClasspath((destination +: classpathSeq).toArray) // + deps
        .withClassesDirectory(destination)
        .withJavacOptions(javaCompilerOptions.toArray)
        .withScalacOptions(
          Array.concat(
            scalaCompilerPlugins /*.map(p => s"-Xplugin:$p")*/ .toArray,
            scalaCompilerOptions.toArray
          )
        )

    val compilers = {
      val scalaCompiler: AnalyzingCompiler = ZincUtil
        .scalaCompiler(scalaInstance, compilerBridge)
        .withClassLoaderCache(classloaderCache)
      lastCompiler = scalaCompiler
      ZincUtil.compilers(scalaInstance, ClasspathOptionsUtil.boot, None, scalaCompiler)
    }

    val lookup = new NoOpPerClasspathEntryLookup(file => None)
    val setup = {
      val incOptions = IncOptions.create()
      val reporter = new LoggedReporter(0, logger)
      val skip = false
      val file: File = null

      Setup.create(lookup, skip, file, compilerCache, incOptions, reporter, Optional.empty[CompileProgress](), Array.empty)
    }

    val inputs = Inputs.of(compilers, compileOptions, setup, previousResult)

    // compile
    val incrementalCompiler = new IncrementalCompilerImpl()
    val compileResult =
      try incrementalCompiler.compile(inputs, logger)
      catch {
        case _: CompileFailed => sys.exit(-1)
        case e: ClassFormatError =>
          System.err.println(e)
          println("You may be missing a `macro = True` attribute.")
          sys.exit(1)
      }

  }

}
