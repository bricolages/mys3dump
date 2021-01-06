# mys3dump

Dumps MySQL table to S3 parallely.

## Build & Test

Following command builds executable JAR file with dependencies.
```
% gradle build
```

## Packaging

```
% gradle shadowJar
```
This generates mys3dump-X.X.X-all.jar in the build/libs/ directory.
