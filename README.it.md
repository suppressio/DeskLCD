🌐 [English](README.md) | **Italiano**

# DeskLCD

Una piccola app desktop per Linux che gestisce un display [LCDproc](https://lcdproc.org): avvia e ferma il servizio `LCDd` e il client `lcdproc`, e sceglie quali schermate informative (CPU, memoria, dischi, orologio, ...) compaiono sull'LCD, senza modificare a mano `/etc/lcdproc.conf`.

- Interfaccia in italiano e inglese (segue la lingua del sistema).
- Resta nell'area di notifica: chiudendo la finestra continua a funzionare, *Esci* è nel menu dell'icona.
- Può avviarsi da sola all'accesso, con una casella.

---

## Installazione

Serve `lcdproc` (LCDd e il client `lcdproc`: `sudo apt install lcdproc` su Debian/Ubuntu) e una sessione desktop (X11 o Wayland) con un agente polkit, che KDE e GNOME hanno già attivo.

Scarica da [Releases](https://github.com/suppressio/DeskLCD/releases):

- **`desklcd_<versione>_amd64.deb`** (consigliato): include un proprio runtime Java, si installa in `/opt/desklcd` e aggiunge una voce di menu.

  ```bash
  sudo apt install ./desklcd_<versione>_amd64.deb
  ```

- **`desklcd-<versione>.jar`**: un unico jar per qualsiasi Linux x86_64 con Java 11 o successivo: `java -jar desklcd-<versione>.jar`. Per aggiungere a mano una voce di menu:

  ```bash
  mkdir -p ~/.local/share/applications
  cat > ~/.local/share/applications/desklcd.desktop <<EOF
  [Desktop Entry]
  Type=Application
  Name=DeskLCD
  Comment=GUI per LCDd/LCDproc
  Exec=java -jar /percorso/di/desklcd-<versione>.jar
  Terminal=false
  Categories=System;
  EOF
  ```

## Uso

La finestra ha tre schede.

**Stato**
- **Servizio LCDd**: se il servizio systemd è attivo e abilitato all'avvio, da quando, con *Avvia*, *Ferma* e *Riavvia*.
- **Client lcdproc**: il programma che disegna davvero le schermate sull'LCD. Mostra se è in esecuzione (avviato da DeskLCD o dall'esterno), con un pulsante per avviarlo o fermarlo.
- **Avvio automatico**: lancia DeskLCD automaticamente all'accesso.
- **Moduli attivi**: accende o spegne le schede *Client lcdproc* e *Client Custom*.

**Client lcdproc**
- Una casella per schermata (CPU, CPU SMP, grafico CPU, carico, memoria, dimensione dei processi, dischi, interfacce di rete, batteria, data e ora, orologi, uptime, crediti); passando sopra una casella compare la descrizione.
- *Ricarica* rilegge `/etc/lcdproc.conf`, *Salva* lo scrive. Il client legge la configurazione solo all'avvio, quindi dopo il salvataggio DeskLCD propone di riavviarlo.
- *Connessione...*: server e porta di LCDd, e le impostazioni di esecuzione del client (livello di report, syslog, primo piano, file PID, nome del display).

**Client Custom**: riservata a un client proprio; per ora è vuota.

Controllare il servizio e salvare `/etc/lcdproc.conf` richiedono root: DeskLCD lo chiede tramite `pkexec`, quindi compare la solita richiesta grafica della password.

---

## Sviluppo

Build dai sorgenti, test e pipeline di release: [docs/DEVELOPMENT.it.md](docs/DEVELOPMENT.it.md).
