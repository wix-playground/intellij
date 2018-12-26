package com.google.idea.blaze.base.ui.problems;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.idea.blaze.base.ui.problems.ImportIssueConstants.*;
import static com.google.idea.blaze.base.ui.problems.ImportLineUtils.ImportType.*;
import static com.google.idea.blaze.base.ui.problems.ImportProblemContainerService.EMPTY_STRING;

public class ImportLineUtils {
    private static final Logger logger = Logger.getLogger(ImportLineUtils.class.getName());

    @NotNull
    public static Optional<String> getPackageName(String originalLine) {
        ImportType importType = getImportType(originalLine);
        switch (importType) {
            case REGULAR:
            case JAVA_WILDCARD:
            case SCALA_WILDCARD:
            case MULTIPLE_SCALA:
            case SCALA_ALIAS:
                String importWithoutKeywords = getImportLineWithoutKeywords(originalLine);
                String packageName = importWithoutKeywords.substring(0, importWithoutKeywords.lastIndexOf(PACKAGE_SEPARATOR));
                return Optional.of(packageName);
            default:
                logger.log(Level.SEVERE, "Unsupported import type for line ["+originalLine+"]");
                return Optional.empty();
        }
    }

    @NotNull
    private static String getImportLineWithoutKeywords(String originalLine) {
        return originalLine.
                replace(IMPORT_KEYWORD, EMPTY_STRING).
                replace(STATIC_KEYWORD, EMPTY_STRING).
                replace(JAVA_EOFL_IDENTIFIER, EMPTY_STRING).
                trim();
    }

    private static boolean isMultipleClassesImport(String importWithoutKeywords) {
        return containsCurlyBraces(importWithoutKeywords) && doesNotContainArrow(importWithoutKeywords);
    }

    private static boolean containsCurlyBraces(String importWithoutKeywords) {
        return Pattern.compile(".*\\{.*\\}.*").matcher(importWithoutKeywords).find();
    }

    private static boolean doesNotContainArrow(String importWithoutKeywords) {
        return Pattern.compile("^((?!=>).)*$").matcher(importWithoutKeywords).find();
    }

    private static boolean isScalaAlias(String importWithoutKeywords) {
        return containsCurlyBracesAndArrowInside(importWithoutKeywords);
    }

    private static boolean containsCurlyBracesAndArrowInside(String importWithoutKeywords) {
        return Pattern.compile(".*\\{.*=>.*\\}.*").matcher(importWithoutKeywords).find();
    }

    public static boolean isWildCardImportLineJava(String importLine) {
        String lineWithoutDotComma = importLine.replace(JAVA_EOFL_IDENTIFIER, "");
        return lineWithoutDotComma.endsWith(JAVA_WILDCARD_KEYWORD);
    }

    public static boolean isWildCardImportLineScala(String importLine) {
        String lineWithoutDotComma = importLine.replace(JAVA_EOFL_IDENTIFIER, "");
        return lineWithoutDotComma.endsWith(SCALA_WILDCARD_KEYWORD);
    }


    private static boolean isRegularImport(long numberOfPartsThatStartWithUpperCase, boolean classNameLast) {
        return thereIsOneClassName(numberOfPartsThatStartWithUpperCase) && classNameLast;
    }

    private static boolean isClassNameLast(String[] parts) {
        return startsWithUpperCase(parts[parts.length - 1]);
    }

    private static boolean thereIsOneClassName(long partsThatStartWithUpperCase) {
        return partsThatStartWithUpperCase == 1;
    }

    private static boolean startsWithUpperCase(String part) {
        return Character.isUpperCase(part.charAt(0));
    }


    public static boolean isWildCardImportLine(String importLine) {
        return isWildCardImportLineJava(importLine) || isWildCardImportLineScala(importLine);
    }

    public static ImportType getImportType(String originalLine) {
        String importWithoutKeywords = getImportLineWithoutKeywords(originalLine);

        String[] importLinePartsArray = importWithoutKeywords.split("\\.");
        List<String> importLineParts = Arrays.asList(importLinePartsArray);
        Stream<String> partsThatStartWithCapital = importLineParts.stream().filter(part -> startsWithUpperCase(part));
        long numberOfPartsThatStartWithUpperCase = partsThatStartWithCapital.count();

        if (isWildCardImportLineJava(importWithoutKeywords)) {
            return JAVA_WILDCARD;
        } else if (isWildCardImportLineScala(importWithoutKeywords)) {
            return SCALA_WILDCARD;
        } else if (isScalaAlias(importWithoutKeywords)) {
            return SCALA_ALIAS;
        } else if (isMultipleClassesImport(importWithoutKeywords)) {
            return MULTIPLE_SCALA;
        } else if (isRegularImport(numberOfPartsThatStartWithUpperCase, isClassNameLast(importLinePartsArray))) {
            return REGULAR;
        } else {
            return UNSUPPORTED;
        }
    }

    public static List<String> getClassNames(String originalLine, ImportType importType) {

        switch (importType){
            case MULTIPLE_SCALA:
                return getClassNamesForMultipleScala(originalLine);
            case SCALA_ALIAS:
                return getClassNamesForScalaAlias(originalLine);
            default:
                throw new RuntimeException(
                        "Trying to parse multiple scala classes when import type is [" + importType.name() + "], and import line [" + originalLine + "]"
                );
        }
    }

    private static List<String> getClassNamesForScalaAlias(String originalLine) {
        String importWithoutKeywords = getImportLineWithoutKeywords(originalLine);

        List<String> classNamesListTrimmed = getMultipleClassNamesFromImportLine(importWithoutKeywords);
        List<String> originalClassNamesOnly =
                classNamesListTrimmed.stream().map(className -> className.split("=>")[0].trim()).collect(Collectors.toList());
        return originalClassNamesOnly;
    }

    @NotNull
    private static List<String> getMultipleClassNamesFromImportLine(String importWithoutKeywords) {
        String[] classNames = importWithoutKeywords.substring(importWithoutKeywords.lastIndexOf(PACKAGE_SEPARATOR) + 1).
                replace(SCALA_IMPORT_CURLY_BRACE_START_IDENTIFIER, EMPTY_STRING).
                replace(SCALA_IMPORT_CURLY_END_IDENTIFIER, EMPTY_STRING).
                split(SCALA_MULTIPLE_CLASS_SEPARATOR);

        List<String> classNamesList = Arrays.asList(classNames);
        return classNamesList.stream().map(String::trim).collect(Collectors.toList());
    }

    @NotNull
    private static List<String> getClassNamesForMultipleScala(String originalLine) {
        String importWithoutKeywords = getImportLineWithoutKeywords(originalLine);

        List<String> classNamesListTrimmed = getMultipleClassNamesFromImportLine(importWithoutKeywords);
        return classNamesListTrimmed;
    }

    public enum ImportType {
        REGULAR, JAVA_WILDCARD, SCALA_WILDCARD, STATIC, SCALA_ALIAS, MULTIPLE_SCALA, SCALA_OBJECTS, UNSUPPORTED
    }
}

