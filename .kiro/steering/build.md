# Build and Run Commands

All build, test, and gradle commands MUST be run inside the Docker container. Never run gradle directly on the host.

## Docker Image

Build the image (only needed once or after Dockerfile changes):

```
docker build -t b0aty-hcim-guide .
```

## Running Commands

Use this pattern for all gradle commands:

```
docker run --rm -v "${PWD}:/workspace" b0aty-hcim-guide gradle <command>
```

## Common Commands

- Build: `docker run --rm -v "${PWD}:/workspace" b0aty-hcim-guide gradle build`
- Test: `docker run --rm -v "${PWD}:/workspace" b0aty-hcim-guide gradle test`
- Clean: `docker run --rm -v "${PWD}:/workspace" b0aty-hcim-guide gradle clean`
- Compile only: `docker run --rm -v "${PWD}:/workspace" b0aty-hcim-guide gradle compileJava`

## Rules

- Never run `gradle` directly on the host machine
- Always mount the workspace with `-v "${PWD}:/workspace"`
- Always use `--rm` to clean up the container after execution
- The container uses Amazon Corretto 17 and Gradle 8.5
