package com.google.idea.blaze.scala.fastbuild;

import com.intellij.openapi.diagnostic.Logger;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BloopProject {

  private static final Logger logger = Logger.getInstance(BloopProject.class);
  private final static Path bloopConfigDir = Paths.get(System.getProperty("java.io.tmpdir"),
      "ijwb_fast_test_config",".bloop");
  private static final String DIR_NAME_SEPARATOR = "_";

  static {
    try {
      Files.createDirectories(bloopConfigDir);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private final File destination;
  private final Set<File> files;
  private final List<File> classpath;
  private final String name;

  public BloopProject(File destination, Set<File> files, List<File> classpath) {
    this.destination = destination;
    this.files = files;
    this.classpath = classpath;
    this.name = randomString() + DIR_NAME_SEPARATOR + ZonedDateTime.now().toInstant().toEpochMilli();
  }

  public void outputToDisk() {
    log();
    String modifiedBloopConfig = bloopProject(name,
        mkStringFromStreamOfFiles(files.stream()),
        mkStringFromStreamOfFiles(classpath.stream()),
        destination.getAbsolutePath());
    try {
      Files.write(Paths.get(bloopConfigDir + "/" + name + ".json"), modifiedBloopConfig.getBytes());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void log() {
    logger.debugValues("Pre compilation " +
            "changed files, " +
            "uber jar, " +
            "target, " +
            "bloop project name, " +
            "bloop config dir, ",
        Arrays.asList(files,
            classpath,
            destination,
            getName(),
            getConfigDir())
    );

  }

  public Path getConfigDir() {
    return bloopConfigDir;
  }
  public Path getWorkspaceDir() {
    return bloopConfigDir.getParent();
  }

  public String getName() {
    return name;
  }

  private static String mkStringFromStreamOfFiles(Stream<File> files) {
    return files.map(f -> "\"" + f.toString() + "\"").collect(Collectors.joining(","));
  }

  private static String randomString() {
    int leftLimit = 48; // numeral '0'
    int rightLimit = 122; // letter 'z'
    int targetStringLength = 10;
    Random random = new Random();

    String generatedString = random.ints(leftLimit, rightLimit + 1)
        .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
        .limit(targetStringLength)
        .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
        .toString();
    return generatedString;
  }

  private static String bloopProject(String name, String sources, String classpath, String out) {
    return String.join(System.lineSeparator(),
        "{",
        "  \"version\" : \"1.0.0\",",
        "  \"project\" : {",
        "    \"name\" : \"" + name + "\",",
        "    \"directory\" : \"/tmp\",",
        "    \"sources\" : [" + sources + "],",
        "    \"dependencies\" : [],",
        "    \"classpath\" : [" + classpath + "],",
        "    \"out\" : \"" + out + "\",",
        "    \"classesDir\" : \"/tmp\",",
        "    \"test\": {",
        "      \"frameworks\": [",
        "        {",
        "          \"names\": [",
        "            \"org.specs2.runner.Specs2Framework\",",
        "            \"com.novocode.junit.JUnitFramework\"",
        "          ]",
        "        }",
        "      ],",
        "      \"options\": {",
        "        \"excludes\": [],",
        "        \"arguments\": [",
        "          {",
        "            \"args\": [",
        "              \"--add-opens=java.base/jdk.internal.loader=ALL-UNNAMED\",",
        "              \"--add-opens=java.base/java.lang=ALL-UNNAMED\"",
        "            ],",
        "            \"framework\": {",
        "              \"names\": [",
        "                \"org.specs2.runner.Specs2Framework\",",
        "                \"com.novocode.junit.JUnitFramework\"",
        "              ]",
        "            }",
        "          }",
        "        ]",
        "      }",
        "    }",
        "  }",
        "}");
  }
}
