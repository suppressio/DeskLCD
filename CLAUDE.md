# DeskLCD (lcdjava-swt)

GUI SWT per controllare LCDd/LCDproc: stato del servizio, e quali schermate
info (CPU, Memory, Disk, ...) mostrare sull'LCD. Parla con il sistema reale
(systemd, `/etc/lcdproc.conf`, processi) — non ci sono livelli di
astrazione/mock, tutto ciò che la GUI mostra è letto dal sistema live.

## Concetti chiave (da non confondere)

- **`lcdproc.service`** (systemd) avvia solo **LCDd**, il server/driver
  hardware. Gestito da `LcdProcSystemService.java` (systemctl via `pkexec`
  per start/stop/restart, lettura senza privilegi per lo stato).
- **`lcdproc`** (il binario client) è quello che mostra davvero le
  informazioni sull'LCD, leggendo `/etc/lcdproc.conf`. Non è un servizio
  systemd — va gestito come processo figlio (`LcdProcClientProcess.java`).
- **`/etc/lcdproc.conf`**: sezioni `[NomeSchermata]` con `Active=True/False`
  corrispondono 1:1 alle checkbox nella tab "Schermate LCD". Parsing/scrittura
  in `LcdProcConfigFile.java`; la scrittura richiede root → `pkexec tee`.

## Build

`mvn package` produce due jar in `target/`:
- `swt-lcdproc-client-<versione>.jar` — solo le classi del progetto.
- `swt-lcdproc-client-<versione>-shaded.jar` — jar autonomo (tutte le
  dipendenze incluse: lcdjava, slf4j, SWT nativo), lanciabile con `java -jar`.

`org.lcdproc.lcdjava:lcdjava:0.6.0-SNAPSHOT` non è pubblicato su nessun repo
pubblico (progetto sorgente fermo dal 2016, vedi `~/Dev/lcdjava`). Il jar è
**vendorizzato** in `local-repo/` (repository Maven locale dentro il
progetto, dichiarata in `pom.xml`). Attenzione: `.gitignore` esclude `*.jar`
globalmente, con un'eccezione esplicita `!/local-repo/**/*.jar` — se si
vendorizza un altro jar altrove va aggiunta un'eccezione simile o non finirà
nel commit.

## Pipeline di release

`.github/workflows/release.yml`: su push di tag `vX.Y.Z` (o
`workflow_dispatch` manuale) builda e pubblica come Release GitHub sia il jar
standalone sia un `.deb` autonomo generato con `jpackage` (JVM inclusa).

## Testing — approccio TDD per la logica pura

Per la logica pura (parsing/trasformazioni di testo, regole di business)
si scrive il test prima o insieme all'implementazione. Esempio:
`LcdProcConfigFile.parseScreenStates`/`applyScreenStates` e
`AutostartManager.buildDesktopEntry` sono metodi **package-private** pensati
apposta per essere testati direttamente, separati dall'I/O reale
(file/processi). I test vivono in `src/test/java/net/suppressio/desklcd/`
(stesso package del main, per accedere ai membri package-private) — non nel
vecchio `net.suppressio.test.desklcd` usato dallo smoke test storico.

Per le parti che parlano col sistema reale (systemctl, pkexec, processi,
SWT/display) **non si scrivono mock** — costerebbe più dell'app stessa.
Si verifica con uno smoke test manuale:

```
mvn package
java -jar target/swt-lcdproc-client-<versione>-shaded.jar &
DISPLAY=:0 import -window root /tmp/screenshot.png   # imagemagick
```

Non ci sono `xdotool`/`wmctrl` disponibili in questo ambiente per automatizzare
i click. Per verificare una tab diversa da quella di default, scrivere un
piccolo harness Java temporaneo (in `/tmp` o nello scratchpad, mai nel
repo) che apre `DeskLCD` e seleziona la tab via codice
(`TabFolder.setSelection(n)`), poi screenshottare quello — non tentare di
simulare i click da bash.

## Note

- Ogni azione privilegiata passa da `pkexec` (mai regole sudoers
  hardcoded): scrittura `/etc/lcdproc.conf` e `systemctl start/stop/restart`.
- `lcdjava` (la libreria vendorizzata) è ferma dal 2016: non esiste una
  versione più recente da tirare, nessun aggiornamento possibile lì.
