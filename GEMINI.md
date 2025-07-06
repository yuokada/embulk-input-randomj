# Gemini Code Assistant Project Context

This document provides context for the `embulk-input-randomj` project to help Gemini Code Assistant understand the project and assist more effectively.

## Project Overview

`embulk-input-randomj` is an Embulk input plugin for generating dummy records, written in Java. It is a fork of the original `embulk-input-random` plugin, with added features and enhancements.

The plugin allows users to define a schema for generating random data, with options to control the number of rows, threads, and specific characteristics of the generated data, such as data types, lengths, value ranges, and null rates. It also supports generating JSON data and provides fine-grained control over timestamp generation.

## Key Technologies

- **Language:** Java 8
- **Build Tool:** Gradle
- **Dependency Management:** Gradle
- **Testing Framework:** JUnit 5
- **Code Analysis:** Checkstyle, SpotBugs

## Directory Structure

```
.
├── build.gradle               # Gradle build script
├── src
│   ├── main
│   │   └── java                 # Main application source code
│   │       └── io/github/yuokada/embulk/input/randomj
│   │           ├── RandomjInputPlugin.java  # Main plugin class
│   │           ├── PluginTask.java          # Plugin configuration
│   │           └── visitor                  # Column visitors
│   └── test
│       └── java                 # Test source code
│           └── io/github/yuokada/embulk/input/randomj
│               └── TestRandomjInputPlugin.java # Plugin tests
├── example
│   └── config.yml             # Example configuration file
└── README.md                  # Project documentation
```

## Common Commands

- **Build the project:** `./gradlew build`
- **Run tests:** `./gradlew test`
- **Run the plugin with an example configuration:** `embulk run -I lib example/config.yml`
- **Create the gem:** `./gradlew gem`
