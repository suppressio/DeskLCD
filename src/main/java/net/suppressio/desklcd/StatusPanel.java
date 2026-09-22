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
    private final Composite lcdprocTabContent;
    private final Composite customTabContent;

    private Label lcddActiveValue;
    private Label lcddEnabledValue;
    private Label lcddSinceValue;
    private Label clientStateValue;

    private Button btnLcddStart;
    private Button btnLcddStop;
    private Button btnLcddRestart;
    private Button btnClientToggle;
    private Button btnAutostart;

    public StatusPanel(Composite parent, int style, LcdProcClientProcess clientProcess,
            Composite lcdprocTabContent, Composite customTabContent) {
        super(parent, style);
        this.clientProcess = clientProcess;
        this.lcdprocTabContent = lcdprocTabContent;
        this.customTabContent = customTabContent;

        setLayout(new GridLayout(1, false));

        buildLcddGroup();
        buildClientGroup();
        buildAutostartGroup();
        buildModulesGroup();

        Button btnRefresh = new Button(this, SWT.NONE);
        btnRefresh.setText(Messages.get("status.button.refresh"));
        btnRefresh.addListener(SWT.Selection, e -> refresh());

        refresh();
        schedulePoll();
    }

    private void buildLcddGroup() {
        Group group = new Group(this, SWT.NONE);
        group.setText(Messages.get("status.lcdd.group", LcdProcSystemService.UNIT));
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        new Label(group, SWT.NONE).setText(Messages.get("status.lcdd.active.label"));
        lcddActiveValue = new Label(group, SWT.NONE);
        lcddActiveValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(group, SWT.NONE).setText(Messages.get("status.lcdd.enabled.label"));
        lcddEnabledValue = new Label(group, SWT.NONE);
        lcddEnabledValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(group, SWT.NONE).setText(Messages.get("status.lcdd.since.label"));
        lcddSinceValue = new Label(group, SWT.NONE);
        lcddSinceValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Composite buttons = new Composite(group, SWT.NONE);
        buttons.setLayout(new GridLayout(3, true));
        buttons.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        btnLcddStart = new Button(buttons, SWT.NONE);
        btnLcddStart.setText(Messages.get("status.button.start"));
        btnLcddStart.addListener(SWT.Selection, e -> runElevated(LcdProcSystemService::start, "status.lcdd.error.start"));

        btnLcddStop = new Button(buttons, SWT.NONE);
        btnLcddStop.setText(Messages.get("status.button.stop"));
        btnLcddStop.addListener(SWT.Selection, e -> runElevated(LcdProcSystemService::stop, "status.lcdd.error.stop"));

        btnLcddRestart = new Button(buttons, SWT.NONE);
        btnLcddRestart.setText(Messages.get("status.button.restart"));
        btnLcddRestart.addListener(SWT.Selection, e -> runElevated(LcdProcSystemService::restart, "status.lcdd.error.restart"));
    }

    private void buildClientGroup() {
        Group group = new Group(this, SWT.NONE);
        group.setText(Messages.get("status.client.group"));
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        new Label(group, SWT.NONE).setText(Messages.get("status.client.label"));
        clientStateValue = new Label(group, SWT.NONE);
        clientStateValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        btnClientToggle = new Button(group, SWT.NONE);
        btnClientToggle.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        btnClientToggle.addListener(SWT.Selection, e -> toggleClient());
    }

    private void buildAutostartGroup() {
        Group group = new Group(this, SWT.NONE);
        group.setText(Messages.get("status.autostart.group"));
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        btnAutostart = new Button(group, SWT.CHECK);
        btnAutostart.setText(Messages.get("status.autostart.checkbox"));
        btnAutostart.setSelection(AutostartManager.isEnabled());
        btnAutostart.addListener(SWT.Selection, e -> toggleAutostart());
    }

    private void buildModulesGroup() {
        Group group = new Group(this, SWT.NONE);
        group.setText(Messages.get("status.modules.group"));
        group.setLayout(new GridLayout(1, false));
        group.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

        Button lcdprocModule = new Button(group, SWT.CHECK);
        lcdprocModule.setText(Messages.get("status.modules.lcdproc"));
        lcdprocModule.setSelection(lcdprocTabContent.getEnabled());
        lcdprocModule.addListener(SWT.Selection, e -> lcdprocTabContent.setEnabled(lcdprocModule.getSelection()));

        Button customModule = new Button(group, SWT.CHECK);
        customModule.setText(Messages.get("status.modules.custom"));
        customModule.setSelection(customTabContent.getEnabled());
        customModule.addListener(SWT.Selection, e -> customTabContent.setEnabled(customModule.getSelection()));
    }

    private void toggleAutostart() {
        try {
            if (btnAutostart.getSelection()) {
                AutostartManager.enable();
            } else {
                AutostartManager.disable();
            }
        } catch (IOException ex) {
            btnAutostart.setSelection(AutostartManager.isEnabled());
            showError(Messages.get("status.autostart.error"), ex);
        }
    }

    private void toggleClient() {
        try {
            if (clientProcess.isRunningHere()) {
                clientProcess.stop();
            } else {
                clientProcess.start();
            }
        } catch (IOException ex) {
            showError(Messages.get("status.client.error"), ex);
        }
        refresh();
    }

    @FunctionalInterface
    private interface ElevatedAction {
        void run() throws IOException, InterruptedException;
    }

    private void runElevated(ElevatedAction action, String errorMessageKey) {
        setLcddButtonsEnabled(false);
        try {
            action.run();
        } catch (Exception ex) {
            showError(Messages.get(errorMessageKey), ex);
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
        box.setText(Messages.get("error.title"));
        box.setMessage(Messages.get("error.detail", message, ex.getMessage()));
        box.open();
    }

    public void refresh() {
        if (isDisposed()) {
            return;
        }

        LcdProcSystemService.Status status = LcdProcSystemService.queryStatus();
        lcddActiveValue.setText(Messages.get(status.active ? "status.value.activeYes" : "status.value.activeNo"));
        lcddEnabledValue.setText(Messages.get(status.enabled ? "status.value.yes" : "status.value.no"));
        lcddSinceValue.setText(status.since);

        boolean runningHere = clientProcess.isRunningHere();
        boolean runningAnywhere = LcdProcClientProcess.isRunningAnywhere();

        if (runningHere) {
            clientStateValue.setText(Messages.get("status.client.running.here"));
            btnClientToggle.setText(Messages.get("status.button.stop"));
            btnClientToggle.setEnabled(true);
        } else if (runningAnywhere) {
            clientStateValue.setText(Messages.get("status.client.running.external"));
            btnClientToggle.setText(Messages.get("status.button.start"));
            btnClientToggle.setEnabled(false);
        } else {
            clientStateValue.setText(Messages.get("status.client.stopped"));
            btnClientToggle.setText(Messages.get("status.button.start"));
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
