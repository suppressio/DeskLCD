GUI for LCDd/LCDproc (java, swt)
========================================

[🇬🇧 English](#english) · [🇮🇹 Italiano](#italiano)

---

English
=======

A small SWT desktop app to check the status of the LCDproc service and
control which information screens (`CPU`, `Memory`, `TimeDate`, ...) are
shown on the LCD, without editing `/etc/lcdproc.conf` by hand.

Installing
----------

Every push of a tag `vX.Y.Z` (and every manual run from the Actions tab)
builds and publishes two files to the
[Releases](https://github.com/suppressio/DeskLCD/releases) page:

- **`desklcd_<version>_amd64.deb`** — a self-contained Debian package (built
  with `jpackage`), bundling its own Java runtime.
- **`desklcd-<version>.jar`** — a standalone jar with all dependencies
  bundled in, for any Linux x86_64 system that already has Java.

**Option A — `.deb` (recommended):**

    sudo dpkg -i desklcd_<version>_amd64.deb

Installs the app under `/opt/desklcd` and adds a menu entry. Does not
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

Features
--------

- **Stato** tab: whether the `LCDd` systemd service is running/enabled, with
  Start/Stop/Restart; whether the `lcdproc` client (the one that actually
  draws the info screens) is running, with Start/Stop; a checkbox to launch
  DeskLCD itself automatically at login.
- **Schermate LCD** tab: checkboxes for each info screen (CPU, Memory, Disk,
  TimeDate, ...), read from and written back to `/etc/lcdproc.conf`.

Building
--------

This is a Maven project targeting Linux x86_64 (it depends on the GTK build
of SWT).

    mvn package

This produces two jars in `target/`:

- `swt-lcdproc-client-<version>.jar` — the project's own classes only.
- `swt-lcdproc-client-<version>-shaded.jar` — a standalone, runnable jar with
  all dependencies (lcdjava, slf4j, SWT) bundled in — this is what the CI
  pipeline renames and publishes as `desklcd-<version>.jar`.

Run the standalone jar directly:

    java -jar target/swt-lcdproc-client-<version>-shaded.jar

During development, `mvn exec:java` also works (runs `DeskLCD_Main` against
the project's own classpath).

The `org.lcdproc.lcdjava:lcdjava:0.6.0-SNAPSHOT` dependency is not published
to any public Maven repository. Its jar is vendored under `local-repo/`
(declared as a repository in `pom.xml`) so the build works standalone,
without needing the sibling `lcdjava` project checked out and installed
first.

Testing
-------

    mvn test

Parsing/formatting logic (which screens are active in `/etc/lcdproc.conf`,
the autostart `.desktop` file content) has real unit tests. Everything that
talks to the live system — the systemd service, the `lcdproc` process,
`pkexec` prompts, the SWT UI itself — isn't unit tested (there's no
meaningful way to mock a display and a running LCDd without more machinery
than the app itself); it's checked by hand by running the app and looking
at it.

Releases (CI pipeline)
-----------------------

`.github/workflows/release.yml` builds the app on GitHub Actions and
publishes the two files listed under **Installing** above. It runs
automatically when a tag matching `vX.Y.Z` is pushed, and can also be
triggered manually from the Actions tab (`workflow_dispatch`) to just build
and check the artifacts without cutting a release.

---

Italiano
========

Una piccola app desktop SWT per controllare lo stato del servizio LCDproc e
decidere quali schermate informative (`CPU`, `Memory`, `TimeDate`, ...)
mostrare sull'LCD, senza modificare a mano `/etc/lcdproc.conf`.

Installazione
-------------

Ogni push di un tag `vX.Y.Z` (e ogni esecuzione manuale dalla tab Actions)
builda e pubblica due file nella pagina
[Releases](https://github.com/suppressio/DeskLCD/releases):

- **`desklcd_<versione>_amd64.deb`** — pacchetto Debian autonomo (generato
  con `jpackage`), con una propria JVM inclusa.
- **`desklcd-<versione>.jar`** — jar autonomo con tutte le dipendenze
  incluse, per qualsiasi sistema Linux x86_64 che abbia già Java installato.

**Opzione A — `.deb` (consigliata):**

    sudo dpkg -i desklcd_<versione>_amd64.deb

Installa l'app in `/opt/desklcd` e aggiunge una voce di menu. Non richiede
Java installato separatamente.

**Opzione B — jar autonomo:** scarica `desklcd-<versione>.jar` ed eseguilo
con `java -jar desklcd-<versione>.jar` (serve un runtime Java 11+). Per
aggiungere una voce di menu manualmente:

    mkdir -p ~/.local/share/applications
    cat > ~/.local/share/applications/desklcd.desktop <<EOF
    [Desktop Entry]
    Type=Application
    Name=DeskLCD
    Comment=GUI per LCDd/LCDproc
    Exec=java -jar /percorso/a/desklcd-<versione>.jar
    Terminal=false
    Categories=System;
    EOF

Avviare DeskLCD automaticamente all'accesso *non* richiede di impostare a
mano nulla in `~/.config/autostart`: è una checkbox nella tab "Stato"
dell'app stessa.

Requisiti a runtime
--------------------

- `lcdproc` installato (LCDd + il client `lcdproc`) — su Debian/Ubuntu:
  `sudo apt install lcdproc`.
- Una sessione desktop attiva (X11 o Wayland) con un agente polkit — le tab
  "Stato" e "Schermate LCD" usano `pkexec` per controllare il servizio
  systemd `LCDd` e per scrivere `/etc/lcdproc.conf`, il che fa comparire il
  normale prompt grafico di autenticazione. Le sessioni KDE e GNOME hanno
  già un agente polkit attivo di default.

Funzionalità
------------

- Tab **Stato**: se il servizio systemd `LCDd` è attivo/abilitato, con
  Avvia/Ferma/Riavvia; se il client `lcdproc` (quello che disegna davvero le
  schermate info) è in esecuzione, con Avvia/Ferma; una checkbox per avviare
  DeskLCD stesso automaticamente all'accesso.
- Tab **Schermate LCD**: checkbox per ogni schermata info (CPU, Memory,
  Disk, TimeDate, ...), lette e scritte su `/etc/lcdproc.conf`.

Build
-----

È un progetto Maven per Linux x86_64 (dipende dalla build GTK di SWT).

    mvn package

Produce due jar in `target/`:

- `swt-lcdproc-client-<versione>.jar` — solo le classi del progetto.
- `swt-lcdproc-client-<versione>-shaded.jar` — jar autonomo (tutte le
  dipendenze incluse: lcdjava, slf4j, SWT) — è quello che la pipeline CI
  rinomina e pubblica come `desklcd-<versione>.jar`.

Per lanciare direttamente il jar autonomo:

    java -jar target/swt-lcdproc-client-<versione>-shaded.jar

In sviluppo funziona anche `mvn exec:java` (esegue `DeskLCD_Main` sul
classpath del progetto).

La dipendenza `org.lcdproc.lcdjava:lcdjava:0.6.0-SNAPSHOT` non è pubblicata
su nessun repository Maven pubblico. Il suo jar è vendorizzato in
`local-repo/` (dichiarata come repository in `pom.xml`), così la build
funziona da sola, senza bisogno del progetto `lcdjava` gemello scaricato e
installato a parte.

Test
----

    mvn test

La logica di parsing/formattazione (quali schermate sono attive in
`/etc/lcdproc.conf`, il contenuto del file `.desktop` di autostart) ha test
unitari veri. Tutto ciò che parla col sistema reale — il servizio systemd,
il processo `lcdproc`, i prompt `pkexec`, l'interfaccia SWT stessa — non è
testato in modo automatico (non ha senso mockare un display e un LCDd in
esecuzione con più impalcatura dell'app stessa); si verifica a mano
lanciando l'app e guardandola.

Release (pipeline CI)
----------------------

`.github/workflows/release.yml` builda l'app su GitHub Actions e pubblica i
due file elencati sopra in **Installazione**. Parte automaticamente quando
viene pushato un tag `vX.Y.Z`, e può anche essere lanciata manualmente dalla
tab Actions (`workflow_dispatch`) per buildare e controllare gli artefatti
senza tagliare una release.

---

`CLAUDE.md` has additional notes aimed at AI coding assistants working in
this repo — skip it if you're human, it won't tell you anything this README
doesn't already cover better. / `CLAUDE.md` contiene note aggiuntive per
assistenti IA che lavorano in questo repo — se sei umano puoi ignorarlo,
non aggiunge nulla che questo README non copra già meglio.
