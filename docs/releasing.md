# Releasing

Publishing a GitHub Release builds the SDK and the example and attaches the artifacts
to the release automatically. This is the whole process.

## What a release produces

Publishing a release fires the `release: published` event, which runs
[`.github/workflows/sdk.yml`](../.github/workflows/sdk.yml): its `build` job builds the
SDK and the HelloAlpha example, and its `release-assets` job attaches two files to the
release:

- `ubtechalpha2robot-release.aar` — the SDK (drop into an app's `libs/`).
- `hello-alpha.apk` — the HelloAlpha example app.

## Steps

1. **Merge to `main` first.** A release is cut from `main`, so make sure what you want
   to ship is merged and its CI is green.

2. **Cut the release.** A release is a git tag plus a GitHub Release, created in one step
   with the `gh` CLI:

   ```bash
   gh release create v0.1.0 --target main \
     --title "Alpha2OpenSdk v0.1.0" \
     --notes-file notes.md
   ```

   - `--target main` tags `main`'s current HEAD.
   - Publishing (i.e. **not** `--draft`) is what triggers the build + attach automation.
   - Notes are optional: use `--notes-file notes.md`, `--generate-notes` to derive them
     from the commits, or `--notes "..."` for a short inline note.

   You can also use the web UI: **Releases → Draft a new release → Publish** — same trigger.

3. **Wait for CI (~1–2 min).** The release page shows only the auto-generated source
   archives at first; the `.aar` and `.apk` appear once the `build` job finishes and the
   `release-assets` job attaches them.

4. **Verify:**

   ```bash
   gh release view v0.1.0 --json assets --jq '.assets[].name'
   # ubtechalpha2robot-release.aar
   # hello-alpha.apk
   ```

## Notes

- **Versioning.** Use a `v`-prefixed tag (`v0.1.0`, `v1.0.0`, …); the `release-assets`
  job keys off the release's tag name.
- **A release does not rebuild the CI toolchain image.** That image
  (`davesnowdon/alpha2build`) is rebuilt only when `docker/**` changes on `main` — see
  [../docker/README.md](../docker/README.md).
- **No extra secrets needed.** The `release-assets` job attaches assets with the built-in
  `GITHUB_TOKEN` (`contents: write`).
