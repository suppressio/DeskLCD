package net.suppressio.desklcd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;

import java.io.IOException;

/**
 * Edits the [lcdproc] section of /etc/lcdproc.conf: which LCDd server/port
 * the lcdproc client connects to, and its runtime options. Separate from
 * "which screens to show" (see DeskLCD's Schermate LCD tab).
 */
public class LCDprocConfig extends Composite {

    private final LcdProcClientProcess clientProcess;

    private Text serverText;
    private Text portText;
    private Text reportLevelText;
    private Button reportToSyslogCheck;
    private Button foregroundCheck;
    private Text pidFileText;
    private Text displayNameText;
    private Label statusLabel;

    public LCDprocConfig(Composite parent, int style, LcdProcClientProcess clientProcess) {
        super(parent, style);
        this.clientProcess = clientProcess;
        setLayout(new GridLayout(2, false));

        serverText = addTextRow("connection.label.server");
        portText = addTextRow("connection.label.port");
        reportLevelText = addTextRow("connection.label.reportLevel");
        reportToSyslogCheck = addCheckRow("connection.label.reportToSyslog");
        foregroundCheck = addCheckRow("connection.label.foreground");
        pidFileText = addTextRow("connection.label.pidfile");
        displayNameText = addTextRow("connection.label.displayName");

        Composite buttons = new Composite(this, SWT.NONE);
        buttons.setLayout(new GridLayout(2, true));
        buttons.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        Button btnReload = new Button(buttons, SWT.NONE);
        btnReload.setText(Messages.get("screens.button.reload"));
        btnReload.addListener(SWT.Selection, e -> load());

        Button btnSave = new Button(buttons, SWT.NONE);
        btnSave.setText(Messages.get("screens.button.save"));
        btnSave.addListener(SWT.Selection, e -> save());

        statusLabel = new Label(this, SWT.WRAP);
        GridData statusData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        statusData.widthHint = 260;
        statusLabel.setLayoutData(statusData);

        load();
    }

    private Text addTextRow(String labelKey) {
        Label label = new Label(this, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText(Messages.get(labelKey));

        Text text = new Text(this, SWT.BORDER);
        text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
        return text;
    }

    private Button addCheckRow(String labelKey) {
        Label label = new Label(this, SWT.NONE);
        label.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
        label.setText(Messages.get(labelKey));

        Button check = new Button(this, SWT.CHECK);
        check.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
        return check;
    }

    private void load() {
        try {
            LcdProcConfigFile.ConnectionSettings settings = LcdProcConfigFile.readConnectionSettings();
            serverText.setText(settings.server);
            portText.setText(settings.port);
            reportLevelText.setText(settings.reportLevel);
            reportToSyslogCheck.setSelection(settings.reportToSyslog);
            foregroundCheck.setSelection(settings.foreground);
            pidFileText.setText(settings.pidFile);
            displayNameText.setText(settings.displayName);
            statusLabel.setText(Messages.get("screens.status.loaded", LcdProcConfigFile.PATH));
        } catch (IOException ex) {
            statusLabel.setText(Messages.get("screens.status.loadError", ex.getMessage()));
        }
    }

    private void save() {
        String port = portText.getText().trim();
        String reportLevel = reportLevelText.getText().trim();
        if (!port.matches("\\d+")) {
            showValidationError(Messages.get("connection.error.invalidPort"));
            return;
        }
        if (!reportLevel.matches("\\d+")) {
            showValidationError(Messages.get("connection.error.invalidReportLevel"));
            return;
        }

        LcdProcConfigFile.ConnectionSettings settings = new LcdProcConfigFile.ConnectionSettings();
        settings.server = serverText.getText().trim();
        settings.port = port;
        settings.reportLevel = reportLevel;
        settings.reportToSyslog = reportToSyslogCheck.getSelection();
        settings.foreground = foregroundCheck.getSelection();
        settings.pidFile = pidFileText.getText().trim();
        settings.displayName = displayNameText.getText().trim();

        try {
            LcdProcConfigFile.writeConnectionSettings(settings);
            statusLabel.setText(Messages.get("screens.status.saved", LcdProcConfigFile.PATH));
            ClientRestartPrompt.offer(getShell(), clientProcess);
        } catch (IOException | InterruptedException ex) {
            statusLabel.setText(Messages.get("screens.status.saveError", ex.getMessage()));
        }
    }

    private void showValidationError(String message) {
        MessageBox box = new MessageBox(getShell(), SWT.ICON_WARNING | SWT.OK);
        box.setText(Messages.get("error.title"));
        box.setMessage(message);
        box.open();
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
