package net.suppressio.desklcd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

/**
 * Minimizes the given shell to a system tray icon instead of closing it when
 * the window's close button is pressed. The tray icon's context menu is the
 * only way to actually quit; clicking the icon restores the window.
 */
public class TrayIconManager {

    public TrayIconManager(Shell shell) {
        Display display = shell.getDisplay();
        Tray tray = display.getSystemTray();
        if (tray == null) {
            // No system tray on this desktop; fall back to the normal close behavior.
            return;
        }

        Image icon = createIcon(display);

        TrayItem trayItem = new TrayItem(tray, SWT.NONE);
        trayItem.setToolTipText("DeskLCD");
        trayItem.setImage(icon);

        Menu menu = new Menu(shell, SWT.POP_UP);
        MenuItem showItem = new MenuItem(menu, SWT.NONE);
        showItem.setText(Messages.get("tray.menu.show"));
        showItem.addListener(SWT.Selection, e -> restore(shell));

        MenuItem exitItem = new MenuItem(menu, SWT.NONE);
        exitItem.setText(Messages.get("tray.menu.exit"));
        exitItem.addListener(SWT.Selection, e -> shell.dispose());

        trayItem.addListener(SWT.Selection, e -> restore(shell));
        trayItem.addListener(SWT.MenuDetect, e -> menu.setVisible(true));

        shell.addListener(SWT.Close, e -> {
            e.doit = false;
            shell.setVisible(false);
        });
        shell.addListener(SWT.Dispose, e -> {
            trayItem.dispose();
            icon.dispose();
        });
    }

    private static void restore(Shell shell) {
        shell.setVisible(true);
        shell.setMinimized(false);
        shell.setActive();
    }

    private static Image createIcon(Display display) {
        int size = 16;
        Image image = new Image(display, size, size);
        GC gc = new GC(image);
        Color background = new Color(display, 0x2E, 0x6D, 0xA4);
        gc.setAntialias(SWT.ON);
        gc.setBackground(background);
        gc.fillRoundRectangle(0, 0, size, size, 4, 4);
        gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
        gc.drawText("L", 4, 1, true);
        gc.dispose();
        background.dispose();
        return image;
    }
}
