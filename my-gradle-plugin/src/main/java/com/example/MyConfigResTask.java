package com.example;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.*;
import java.util.Map;
import java.util.TreeMap;

public class MyConfigResTask extends DefaultTask {

    private File jsonFile;
    private File genResFolder;

    @InputFile
    @Optional
    public File getJsonFile() {
        return jsonFile;
    }

    public void setJsonFile(File jsonFile) {
        this.jsonFile = jsonFile;
    }

    @OutputDirectory
    public File getGenResFolder() {
        return genResFolder;
    }

    public void setGenResFolder(File genResFolder) {
        this.genResFolder = genResFolder;
    }

    @TaskAction
    public void action() throws IOException {
        Map<String, String> resValues = null;
        try {
            resValues = getJsonContent(jsonFile);
        } catch (IOException | NullPointerException e) {
            getProject().getLogger().warn("Could not find config.json");
        }

        // write the values file.
        File values = new File(genResFolder, "values");
        if (!values.exists() && !values.mkdirs()) {
            throw new GradleException("Failed to create folder: " + values);
        }

        PrintWriter fileWriter = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(new File(values, "values.xml")), "UTF-8"));
        fileWriter.print("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<resources>\n");
        if (resValues != null) {
            fileWriter.print(mapToString(resValues));
        }
        fileWriter.print("</resources>\n");
        fileWriter.flush();
        fileWriter.close();
    }

    private static Map<String, String> getJsonContent(File jsonFile) throws IOException, NullPointerException {
        JsonElement root = new JsonParser().parse(new BufferedReader(new InputStreamReader(
                new FileInputStream(jsonFile), "UTF-8")));
        JsonObject rootObject = root.getAsJsonObject();
        Map<String, String> resValues = new TreeMap<>();
        // set value map from config json
        for (String key : rootObject.keySet()) {
            resValues.put(key, rootObject.get(key).getAsString());
        }
        return resValues;
    }

    private static String mapToString(Map<String, String> values) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String name = entry.getKey();
            sb.append("    <string name=\"").append(name).append("\" translatable=\"false\">")
                    .append(entry.getValue()).append("</string>\n");
        }
        return sb.toString();
    }
}