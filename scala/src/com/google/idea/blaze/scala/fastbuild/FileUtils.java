package com.google.idea.blaze.scala.fastbuild;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

import com.intellij.openapi.diagnostic.Logger;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;

public class FileUtils {
  private static final Logger logger = Logger.getInstance(FileUtils.class);

  public static void copyFolderRecursively(Path source, Path target)
      throws IOException {
    Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
          throws IOException {
        logger.debugValues("visiting dir ", Collections.singletonList(dir));
        Files.createDirectories(target.resolve(source.relativize(dir)));
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
          throws IOException {
        logger.debugValues("visiting file ", Collections.singletonList(file));
        Files.copy(file, target.resolve(source.relativize(file)), REPLACE_EXISTING);
        return FileVisitResult.CONTINUE;
      }
    });
  }
}
