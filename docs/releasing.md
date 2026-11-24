# Releasing

Coordinates: `io.github.shakeabdulrahman:keystone-llm-<module>:<version>`, published to
Maven Central with [`com.vanniktech.maven.publish`](https://github.com/vanniktech/gradle-maven-publish-plugin)
through the Central Portal. Releases are made by CI from a git tag; nothing is uploaded from a
laptop.

## One-time setup

1. **Central Portal account.** Sign in at https://central.sonatype.com with your GitHub
   account. The namespace `io.github.shakeabdulrahman` is verified automatically for that
   login (check it under *Namespaces*).
2. **Publishing token.** In the portal: *View Account → Generate User Token*. It gives a
   username and a password (not your login).
3. **Signing key** (Maven Central requires signed artifacts):
   ```bash
   gpg --full-generate-key                       # RSA, 4096 bits, your name and email
   gpg --list-secret-keys --keyid-format=long    # note the key id after "rsa4096/"
   gpg --keyserver keyserver.ubuntu.com --send-keys <KEY_ID>
   gpg --armor --export-secret-keys <KEY_ID>     # the whole block, for the secret below
   ```
4. **GitHub secrets** (*Settings → Secrets and variables → Actions*):

   | Secret | Value |
   |---|---|
   | `MAVEN_CENTRAL_USERNAME` | token username from step 2 |
   | `MAVEN_CENTRAL_PASSWORD` | token password from step 2 |
   | `SIGNING_KEY` | the armored private key block from step 3 |
   | `SIGNING_KEY_PASSWORD` | the key's passphrase |

5. **API docs site:** *Settings → Pages → Source: GitHub Actions*.

Keys and tokens live only in GitHub secrets (or your `~/.gradle/gradle.properties` for a
manual publish). Never commit them.

## Each release

1. Make sure `main` is green in CI.
2. In `CHANGELOG.md`, rename **Unreleased** to the version and date, and add a fresh
   **Unreleased** section.
3. If the public API changed, `./gradlew apiDump` and commit the dumps.
4. Tag and push:
   ```bash
   git tag v0.1.0
   git push origin v0.1.0
   ```
5. The *Release* workflow tests, publishes a staged deployment to the Central Portal, creates
   the GitHub Release and updates the API docs.
6. In the portal, open *Deployments*, check the files and press **Publish**. Artifacts appear
   on Maven Central within about 30 minutes (search can take longer).

`VERSION_NAME` in `gradle.properties` stays at the next `-SNAPSHOT`; the release version
comes from the tag.

## Versioning

- [Semantic Versioning](https://semver.org). In `0.x`, minor versions may break the API;
  breaking changes are listed under **Changed** or **Removed** in the changelog.
- `explicitApi()` makes every public declaration deliberate, and the API dumps in `api/`
  make every public API change visible in review (`apiCheck` runs in CI).
- `@InternalKeystoneLlmApi` marks cross-module internals (opt-in required, level ERROR); they
  are excluded from the API dumps and carry no compatibility promise.
