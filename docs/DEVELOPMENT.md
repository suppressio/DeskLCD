🌐 **English** | [Italiano](DEVELOPMENT.it.md)

# Development

How to build and test DeskLCD from source, and how releases are made. To install and use the app, see the [README](../README.md).

## Build

A Maven project for Linux x86_64 (it depends on the GTK build of SWT).

```bash
mvn package
```

This produces two jars in `target/`:

- `swt-lcdproc-client-<version>.jar`: the project's own classes only.
- `swt-lcdproc-client-<version>-shaded.jar`: a standalone, runnable jar with all dependencies (lcdjava, slf4j, SWT) bundled in. The CI pipeline publishes it as `desklcd-<version>.jar`.

Run it with `java -jar target/swt-lcdproc-client-<version>-shaded.jar`. During development `mvn exec:java` also works (runs `DeskLCD_Main` on the project's classpath).

The `org.lcdproc.lcdjava:lcdjava:0.6.0-SNAPSHOT` dependency is not published to any public Maven repository. Its jar is vendored under `local-repo/` (declared as a repository in `pom.xml`), so the build works on its own, without the `lcdjava` project checked out and installed first.

## Tests

```bash
mvn test
```

The parsing and formatting logic (which screens are active in `/etc/lcdproc.conf`, the content of the autostart `.desktop` file) has real unit tests. Everything that talks to the live system (the systemd service, the `lcdproc` process, `pkexec` prompts, the SWT interface) is not unit tested: there is no meaningful way to mock a display and a running LCDd without more machinery than the app itself. It is checked by hand, by running the app and looking at it.

## Releases

`.github/workflows/release.yml` builds the app on GitHub Actions and publishes the `.deb` (built with `jpackage`, Java runtime included) and the standalone jar. It runs when a tag `vX.Y.Z` is pushed; it can also be started by hand from the Actions tab (`workflow_dispatch`) to build and check the files without making a release.
