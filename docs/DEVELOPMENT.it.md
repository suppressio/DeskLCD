🌐 [English](DEVELOPMENT.md) | **Italiano**

# Sviluppo

Come compilare e testare DeskLCD dai sorgenti, e come si fanno le release. Per installare e usare l'app, vedi il [README](../README.it.md).

## Build

Un progetto Maven per Linux x86_64 (dipende dalla build GTK di SWT).

```bash
mvn package
```

Produce due jar in `target/`:

- `swt-lcdproc-client-<versione>.jar`: solo le classi del progetto.
- `swt-lcdproc-client-<versione>-shaded.jar`: jar autonomo ed eseguibile con tutte le dipendenze incluse (lcdjava, slf4j, SWT). La pipeline CI lo pubblica come `desklcd-<versione>.jar`.

Si avvia con `java -jar target/swt-lcdproc-client-<versione>-shaded.jar`. In sviluppo funziona anche `mvn exec:java` (esegue `DeskLCD_Main` sul classpath del progetto).

La dipendenza `org.lcdproc.lcdjava:lcdjava:0.6.0-SNAPSHOT` non è pubblicata su nessun repository Maven pubblico. Il suo jar è incluso in `local-repo/` (dichiarato come repository in `pom.xml`), così la build funziona da sola, senza dover prima scaricare e installare il progetto `lcdjava`.

## Test

```bash
mvn test
```

La logica di parsing e formattazione (quali schermate sono attive in `/etc/lcdproc.conf`, il contenuto del file `.desktop` di avvio automatico) ha test unitari veri. Tutto ciò che parla con il sistema reale (il servizio systemd, il processo `lcdproc`, le richieste `pkexec`, l'interfaccia SWT) non ha test unitari: non ha senso simulare un display e un LCDd in esecuzione con più impalcatura dell'app stessa. Si verifica a mano, avviando l'app e guardandola.

## Release

`.github/workflows/release.yml` compila l'app su GitHub Actions e pubblica il `.deb` (generato con `jpackage`, runtime Java incluso) e il jar autonomo. Parte quando viene pushato un tag `vX.Y.Z`; si può anche avviare a mano dalla scheda Actions (`workflow_dispatch`) per compilare e controllare i file senza fare una release.
