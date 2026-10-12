🌐 **English** | [Italiano](README.it.md)

# DeskLCD

A small Linux desktop app to manage an [LCDproc](https://lcdproc.org) display: start and stop the `LCDd` service and the `lcdproc` client, and choose which information screens (CPU, memory, disks, clock, ...) appear on the LCD, without editing `/etc/lcdproc.conf` by hand.

- English and Italian interface (follows the system language).
- Lives in the system tray: closing the window keeps it running, *Exit* is in the tray menu.
- Can start itself at login, with a checkbox.

---

## Install

You need `lcdproc` (LCDd and the `lcdproc` client: `sudo apt install lcdproc` on Debian/Ubuntu) and a desktop session (X11 or Wayland) with a polkit agent, which KDE and GNOME already run.

Download from [Releases](https://github.com/suppressio/DeskLCD/releases):

- **`desklcd_<version>_amd64.deb`** (recommended): includes its own Java runtime, installs under `/opt/desklcd` and adds a menu entry.

  ```bash
  sudo apt install ./desklcd_<version>_amd64.deb
  ```

- **`desklcd-<version>.jar`**: a single jar for any Linux x86_64 system with Java 11 or later: `java -jar desklcd-<version>.jar`. To add a menu entry by hand:

  ```bash
  mkdir -p ~/.local/share/applications
  cat > ~/.local/share/applications/desklcd.desktop <<EOF
  [Desktop Entry]
  Type=Application
  Name=DeskLCD
  Comment=GUI for LCDd/LCDproc
  Exec=java -jar /path/to/desklcd-<version>.jar
  Terminal=false
  Categories=System;
  EOF
  ```

## Usage

The window has three tabs.

**Status**
- **LCDd service**: whether the systemd service is running and enabled at boot, since when, with *Start*, *Stop* and *Restart*.
- **lcdproc client**: the program that actually draws the screens on the LCD. Shows whether it is running (started from DeskLCD or from outside), with a start/stop button.
- **Startup**: launch DeskLCD automatically at login.
- **Active modules**: turn the *lcdproc Client* and *Custom Client* tabs on or off.

**lcdproc Client**
- One checkbox per screen (CPU, SMP CPU, CPU graph, load, memory, process size, disk, network interfaces, battery, time and date, clocks, uptime, about); hover a checkbox for a description.
- *Reload* reads `/etc/lcdproc.conf` again, *Save* writes it. The client reads its configuration only at startup, so after saving DeskLCD offers to restart it.
- *Connection...*: LCDd server and port, and the client's runtime settings (report level, syslog, foreground, PID file, display name).

**Custom Client**: reserved for a client of your own; empty for now.

Controlling the service and saving `/etc/lcdproc.conf` need root: DeskLCD asks through `pkexec`, so the usual graphical password prompt appears.

---

## Development

Building from source, tests and the release pipeline: [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md).

## License

[MIT](LICENSE) © 2022 Daniele 'suppressio'.
