# CI build image

The `Dockerfile` in this directory builds `davesnowdon/alpha2build`, the container image
used by the GitHub Actions workflow
([`.github/workflows/sdk.yml`](../.github/workflows/sdk.yml)) to build the SDK and the
HelloAlpha example.

It is based on Eclipse Temurin JDK 8 and installs the Android command-line tools plus the
components needed to build against the Alpha2's target:

- `platforms;android-22` (the robot's API level) and `platforms;android-25` (compile SDK)
- `build-tools;22.0.1` and `build-tools;30.0.2`
- `platform-tools`

## When it is rebuilt

[`.github/workflows/docker-image.yml`](../.github/workflows/docker-image.yml) rebuilds the
image on any push to `main` that changes `docker/**` or that workflow file, and pushes it
to Docker Hub. Pull requests build the image **without** pushing, as a Dockerfile smoke
test (no Docker Hub credentials required).

Images are tagged `sha-<short-commit>`. The SDK workflow pins a specific `sha-…` image, so
after a new image is built, bump that pin in `sdk.yml` to adopt it.

## Build locally

```bash
docker build -t davesnowdon/alpha2build ./docker
```
