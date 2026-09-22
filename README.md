GUI for LCDd/LCDproc (java, swt)
========================================

A small SWT desktop app to check the status of the LCDproc service and
control which information screens (`CPU`, `Memory`, `TimeDate`, ...) are
shown on the LCD, without editing `/etc/lcdproc.conf` by hand.

Building
--------

This is a Maven project targeting Linux x86_64 (it depends on the GTK build
of SWT).

    mvn package

This produces two jars in `target/`:

- `swt-lcdproc-client-<version>.jar` — the project's own classes only.
- `swt-lcdproc-client-<version>-shaded.jar` — a standalone, runnable jar with
  all dependencies (lcdjava, slf4j, SWT) bundled in.

Run the standalone jar directly:

    java -jar target/swt-lcdproc-client-<version>-shaded.jar

During development, `mvn exec:java` also works (runs `DeskLCD_Main` against
the project's own classpath).

The `org.lcdproc.lcdjava:lcdjava:0.6.0-SNAPSHOT` dependency is not published
to any public Maven repository. Its jar is vendored under `local-repo/`
(declared as a repository in `pom.xml`) so the build works standalone,
without needing the sibling `lcdjava` project checked out and installed
first.

Releases (CI pipeline)
-----------------------

`.github/workflows/release.yml` builds the app on GitHub Actions and
publishes two artifacts:

- the standalone jar (see above), and
- a `.deb` package built with `jpackage`, bundling a private Java runtime —
  no JDK/JRE needs to be installed on the target machine.

It runs automatically when a tag matching `vX.Y.Z` is pushed (and publishes
a GitHub Release with both files attached), and can also be triggered
manually from the Actions tab (`workflow_dispatch`) to just build and check
the artifacts without cutting a release.

Installing
----------

**Option A — `.deb` (recommended):** download `desklcd_<version>_amd64.deb`
from the [Releases](https://github.com/suppressio/DeskLCD/releases) page and
install it:

    sudo dpkg -i desklcd_<version>_amd64.deb

This installs the app under `/opt/desklcd`, adds a menu entry, and does not
require Java to be installed separately.

**Option B — standalone jar:** download `desklcd-<version>.jar` and run it
with `java -jar desklcd-<version>.jar` (requires a Java 11+ runtime). To add
a menu entry manually:

    mkdir -p ~/.local/share/applications
    cat > ~/.local/share/applications/desklcd.desktop <<EOF
    [Desktop Entry]
    Type=Application
    Name=DeskLCD
    Comment=GUI per LCDd/LCDproc
    Exec=java -jar /path/to/desklcd-<version>.jar
    Terminal=false
    Categories=System;
    EOF

Starting DeskLCD automatically at login does *not* need any manual
`~/.config/autostart` setup: it's a checkbox in the app's own "Stato" tab.

Runtime requirements
---------------------

- `lcdproc` (LCDd + the `lcdproc` client) installed — on Debian/Ubuntu:
  `sudo apt install lcdproc`.
- A running desktop session (X11 or Wayland) with a polkit agent — the
  "Stato" and "Schermate LCD" tabs use `pkexec` to control the `LCDd`
  systemd service and to write `/etc/lcdproc.conf`, which pops up the
  standard graphical authentication prompt. KDE and GNOME sessions already
  run a polkit agent by default.
