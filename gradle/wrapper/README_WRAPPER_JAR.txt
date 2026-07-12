gradle-wrapper.jar could not be included here because generating it
requires either network access to Gradle's distribution servers or a
local Gradle install, and neither is available in this environment.

To finish setting up the wrapper, do ONE of the following on your own
machine (both take under a minute):

Option A - Easiest (Android Studio):
  Open this project in Android Studio. It detects the wrapper
  properties and will offer to download/generate the missing jar
  automatically (or run: File > Sync Project with Gradle Files).

Option B - Command line (requires a local Gradle install):
  gradle wrapper --gradle-version 8.7
  This regenerates gradlew, gradlew.bat, and gradle-wrapper.jar to
  match gradle/wrapper/gradle-wrapper.properties (already set to 8.7).
  Delete this README afterward.

The gradlew and gradlew.bat scripts are already included and do not
need to be regenerated - only the jar is missing.
