#!/bin/bash

# Set the project directory (change this to the path of your project)
PROJECT_DIR="/Users/ahmadsaid/sources/tracker-explorer/"

# Navigate to the project directory
cd "$PROJECT_DIR"

# Run the Gradle application
./gradlew clean build

# Launch the application (update with your application's main class or task)
./gradlew run

