package com.example

import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.regex.Matcher
import java.util.regex.Pattern

class MyGradlePlugin implements Plugin<Project> {
    public final static String JSON_FILE_NAME = "config.json"
    public final static Pattern VARIANT_PATTERN = ~/(?:([^\p{javaUpperCase}]+)((?:\p{javaUpperCase}[^\p{javaUpperCase}]*)*)\/)?([^\/]*)/
    public final static Pattern FLAVOR_PATTERN = ~/(\p{javaUpperCase}[^\p{javaUpperCase}]*)/

    @Override
    void apply(Project project) {
        project.android.applicationVariants.all { variant ->
            handleVariantOpenFile(project, variant)
        }
    }

    static handleVariantOpenFile(Project project, def variant) {
        File foundedJsonFile = null
        File productionFile = null
        File developmentFile = null
        List<String> fileLocations = getJsonLocations("$variant.dirName")

        for (String location : fileLocations) {
            File jsonFile = project.file(location + '/' + JSON_FILE_NAME)

            if (jsonFile.isFile()) {
                foundedJsonFile = jsonFile
                break
            }
        }

        if (foundedJsonFile == null) {
            File jsonFile = project.file(JSON_FILE_NAME)
            if (jsonFile.isFile()) {
                foundedJsonFile = jsonFile
            }
        }

        File outputDir = project.file("$project.buildDir/generated/res/my-gradle-plugin/$variant.dirName")

        MyConfigResTask task = project.tasks.create("${variant.name.capitalize()}MyConfigResTask", MyConfigResTask)

        task.jsonFile = foundedJsonFile
        task.genResFolder = outputDir

        // This is necessary for backwards compatibility with versions of gradle that do not support
        // this new API.
        if (variant.respondsTo("registerGeneratedResFolders")) {
            task.ext.generatedResFolders = project.files(outputDir).builtBy(task)
            variant.registerGeneratedResFolders(task.generatedResFolders)
            if (variant.respondsTo("getMergeResourcesProvider")) {
                variant.mergeResourcesProvider.configure { dependsOn(task) }
            } else {
                variant.mergeResources.dependsOn(task)
            }
        } else {
            variant.registerResGeneratingTask(task, outputDir)
        }
    }

    static List<String> getJsonLocations(String variantDirname) {
        Matcher variantMatcher = VARIANT_PATTERN.matcher(variantDirname)
        List<String> fileLocations = new ArrayList<>()
        if (!variantMatcher.matches()) {
            fileLocations.add("src/$variantDirname")
            return fileLocations
        }
        List<String> flavorNames = new ArrayList<>()
        if (variantMatcher.group(1) != null) {
            flavorNames.add(variantMatcher.group(1).toLowerCase())
        }
        flavorNames.addAll(splitVariantNames(variantMatcher.group(2)))
        String buildType = variantMatcher.group(3)
        String flavorName = "${variantMatcher.group(1)}${variantMatcher.group(2)}"
        fileLocations.add("src/$flavorName/$buildType")
        fileLocations.add("src/$buildType/$flavorName")
        fileLocations.add("src/$flavorName")
        fileLocations.add("src/$buildType")
        fileLocations.add("src/$flavorName${buildType.capitalize()}")
        fileLocations.add("src/$buildType")
        String fileLocation = "src"
        for (String flavor : flavorNames) {
            fileLocation += "/$flavor"
            fileLocations.add(fileLocation)
            fileLocations.add("$fileLocation/$buildType")
            fileLocations.add("$fileLocation${buildType.capitalize()}")
        }
        fileLocations.unique().sort { a, b -> countSlashes(b) <=> countSlashes(a) }
        return fileLocations
    }

    private static List<String> splitVariantNames(String variant) {
        if (variant == null) {
            return Collections.emptyList()
        }
        List<String> flavors = new ArrayList<>()
        Matcher flavorMatcher = FLAVOR_PATTERN.matcher(variant)
        while (flavorMatcher.find()) {
            String match = flavorMatcher.group(1)
            if (match != null) {
                flavors.add(match.toLowerCase())
            }
        }
        return flavors
    }

    private static long countSlashes(String input) {
        return input.codePoints().filter { x -> x == '/' }.count()
    }
}
