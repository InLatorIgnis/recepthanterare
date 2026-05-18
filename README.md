# Recepthanterare

A Java recipe and weekly menu manager built with Gradle.

## Build

```bash
./gradlew build
```

## Create a distributable package

```bash
./gradlew distZip
```

The generated zip is available at:

```bash
build/distributions/recepthanterare.zip
```

## Run the packaged application

Unzip the distribution and use the launcher script:

```bash
unzip build/distributions/recepthanterare.zip -d dist
./dist/recepthanterare/bin/recepthanterare
```

On Windows, use:

```bat
build\distributions\recepthanterare.zip
\dist\recepthanterare\bin\recepthanterare.bat
```

## Notes

- The Gradle distribution includes all runtime dependencies.
- If you need the source or want to recompile, use `./gradlew build`.
