# Gradle-Plugin

a gradle plugin to read configuration from json file and generate string resources to use in android application

### Usage
```
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath 'com.example:my-gradle-plugin:1.0.0'
  }
}

apply plugin: 'com.example.my-gradle-plugin'
```
