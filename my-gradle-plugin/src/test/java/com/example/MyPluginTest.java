package com.example;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assertThat;

public class MyPluginTest {

    @Test
    public void testNoFlavor() {
        List<String> output = toStringList(MyGradlePlugin.getJsonLocations("release"));
        assertThat(output).contains("src/release");
    }

    @Test
    public void testOneFlavor() {
        List<String> output =
                toStringList(MyGradlePlugin.getJsonLocations("flavor/release"));
        assertThat(output)
                .containsAllOf(
                        "src/release",
                        "src/flavorRelease",
                        "src/flavor",
                        "src/flavor/release",
                        "src/release/flavor");
    }

    @Test
    public void testMultipleFlavors() {
        List<String> output =
                toStringList(MyGradlePlugin.getJsonLocations("flavorTest/release"));
        assertThat(output)
                .containsAllOf(
                        "src/release",
                        "src/flavorRelease",
                        "src/flavor",
                        "src/flavor/release",
                        "src/release/flavorTest",
                        "src/flavorTest",
                        "src/flavorTestRelease",
                        "src/flavor/test/release",
                        "src/flavor/testRelease");
    }

    // This is necessary because some of the strings are actually groovy string implementations
    // which fail equality tests with java strings during testing
    private static List<String> toStringList(List<String> input) {
        ArrayList<String> strings = new ArrayList<>(input.size());
        for (Object oldString : input) {
            strings.add(oldString.toString());
        }
        return strings;
    }
}
