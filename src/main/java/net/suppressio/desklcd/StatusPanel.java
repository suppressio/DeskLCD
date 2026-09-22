package net.suppressio.desklcd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

import java.io.IOException;

/**
 * Shows the current state of the LCDd systemd service and of the lcdproc
 * client process, with basic start/stop/restart controls for both.
 */
public class StatusPanel extends Composite {

    private final LcdProcClientProcess clientProcess;

    private Label lcddActiveValue;
    private Label lcddEnabledValue;
    private Label lcddSinceValue;
    private Label clientStateValue;

    private Button btnLcddStart;
    private Button btnLcddStop;
    private Button btnLcddRestart;
    private Button btnClientToggle;

    public StatusPanel(Composite parent, int style, LcdProcClientProcess clientProcess) {
        super(parent, style);
        this.clientProcess = clientProcess;

        setLayout(new GridLayout(1, false));

        buildLcddGroup();
        buildClientGroup();

        Button btnRefresh = new Button(this, SWT.NONE);
        btnRefresh.setText("Aggiorna");
        btnRefresh.addListener(SWT.Selection, e -> refresh());

        refresh();
        schedulePoll();
    }

    private void buildLcddGroup() {
        Group group = new Group(this, SWT.NONE);
        group.setText("Servizio LCDd (systemd: " + LcdProcSystemService.UNIT + ")");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        new Label(group, SWT.NONE).setText("Attivo:");
        lcddActiveValue = new Label(group, SWT.NONE);
        lcddActiveValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(group, SWT.NONE).setText("Abilitato all'avvio:");
        lcddEnabledValue = new Label(group, SWT.NONE);
        lcddEnabledValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(group, SWT.NONE).setText("Attivo dal:");
        lcddSinceValue = new Label(group, SWT.NONE);
        lcddSinceValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite buttons = new Composite(group, SWT.NONE);
        buttons.setLayout(new GridLayout(3, true));
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        btnLcddStart = new Button(buttons, SWT.NONE);
        btnLcddStart.setText("Avvia");
        btnLcddStart.addListener(SWT.Selection, e -> runElevated(LcdProcSystemService::start, "avvio"));

        btnLcddStop = new Button(buttons, SWT.NONE);
        btnLcddStop.setText("Ferma");
        btnLcddStop.addListener(SWT.Selection, e -> runElevated(LcdProcSystemService::stop, "arresto"));

        btnLcddRestart = new Button(buttons, SWT.NONE);
        btnLcddRestart.setText("Riavvia");
        btnLcddRestart.addListener(SWT.Selection, e -> runElevated(LcdProcSystemService::restart, "riavvio"));
    }

    private void buildClientGroup() {
        Group group = new Group(this, SWT.NONE);
        group.setText("Client lcdproc (mostra le informazioni sull'LCD)");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        new Label(group, SWT.NONE).setText("Stato:");
        clientStateValue = new Label(group, SWT.NONE);
        clientStateValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        btnClientToggle = new Button(group, SWT.NONE);
        btnClientToggle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        btnClientToggle.addListener(SWT.Selection, e -> toggleClient());
    }

    private void toggleClient() {
        try {
            if (clientProcess.isRunningHere()) {
                clientProcess.stop();
            } else {
                clientProcess.start();
            }
        } catch (IOException ex) {
            showError("Impossibile avviare il client lcdproc", ex);
        }
        refresh();
    }

    @FunctionalInterface
    private interface ElevatedAction {
        void run() throws IOException, InterruptedException;
    }

    private void runElevated(ElevatedAction action, String label) {
        setLcddButtonsEnabled(false);
        try {
            action.run();
        } catch (Exception ex) {
            showError("Errore durante " + label + " del servizio LCDd", ex);
        } finally {
            setLcddButtonsEnabled(true);
            refresh();
        }
    }

    private void setLcddButtonsEnabled(boolean enabled) {
        if (isDisposed()) {
            return;
        }
        btnLcddStart.setEnabled(enabled);
        btnLcddStop.setEnabled(enabled);
        btnLcddRestart.setEnabled(enabled);
    }

    private void showError(String message, Exception ex) {
        if (isDisposed()) {
            return;
        }
        MessageBox box = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
        box.setText("Errore");
        box.setMessage(message + ":\n" + ex.getMessage());
        box.open();
    }

    public void refresh() {
        if (isDisposed()) {
            return;
        }

        LcdProcSystemService.Status status = LcdProcSystemService.queryStatus();
        lcddActiveValue.setText(status.active ? "si', in esecuzione" : "no, fermo");
        lcddEnabledValue.setText(status.enabled ? "si'" : "no");
        lcddSinceValue.setText(status.since);

        boolean runningHere = clientProcess.isRunningHere();
        boolean runningAnywhere = LcdProcClientProcess.isRunningAnywhere();

        if (runningHere) {
            clientStateValue.setText("in esecuzione (avviato da questa finestra)");
            btnClientToggle.setText("Ferma");
            btnClientToggle.setEnabled(true);
        } else if (runningAnywhere) {
            clientStateValue.setText("in esecuzione (avviato esternamente)");
            btnClientToggle.setText("Avvia");
            btnClientToggle.setEnabled(false);
        } else {
            clientStateValue.setText("fermo");
            btnClientToggle.setText("Avvia");
            btnClientToggle.setEnabled(true);
        }

        layout(true, true);
    }

    private void schedulePoll() {
        Display display = getDisplay();
        display.timerExec(4000, new Runnable() {
            @Override
            public void run() {
                if (isDisposed()) {
                    return;
                }
                refresh();
                display.timerExec(4000, this);
            }
        });
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
