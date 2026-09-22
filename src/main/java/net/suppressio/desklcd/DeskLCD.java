package net.suppressio.desklcd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeskLCD extends Composite {

    /** Tooltip text for each screen, taken from "lcdproc --help". */
    private static final String[] SCREEN_TOOLTIPS = {
            "detailed CPU usage",
            "CPU usage overview (one line per CPU)",
            "CPU usage histogram",
            "load histogram",
            "memory & swap usage",
            "biggest processes size",
            "filling level of mounted file systems",
            "network interface usage",
            "battery status",
            "time & date information",
            "old time screen",
            "uptime screen",
            "big clock",
            "minimal clock",
            "credits page"
    };

    private final LcdProcClientProcess clientProcess = new LcdProcClientProcess();

    private Button[] screenChecks;
    private Label screensStatusLabel;

    /**
     * Create the composite.
     * @param parent
     * @param style
     */
    public DeskLCD(Composite parent, int style) {
        super(parent, style);
        setLayout(new GridLayout(1, false));

        TabFolder tabFolder = new TabFolder(this, SWT.NONE);
        GridData gd_tabFolder = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
        gd_tabFolder.heightHint = 372;
        tabFolder.setLayoutData(gd_tabFolder);

        TabItem statusTab = new TabItem(tabFolder, SWT.NONE);
        statusTab.setText("Stato");
        statusTab.setControl(new StatusPanel(tabFolder, SWT.NONE, clientProcess));

        TabItem screensTab = new TabItem(tabFolder, SWT.NONE);
        screensTab.setText("Schermate LCD");
        screensTab.setControl(buildScreensTab(tabFolder));

        TabItem customTab = new TabItem(tabFolder, SWT.NONE);
        customTab.setText("Custom");
        customTab.setControl(new Composite(tabFolder, SWT.NONE));
    }

    private Composite buildScreensTab(TabFolder tabFolder) {
        Group group = new Group(tabFolder, SWT.NONE);

        String[] names = LcdProcConfigFile.SCREENS;
        screenChecks = new Button[names.length];

        int col1X = 10, col2X = 147, rowH = 25, y0 = 10;
        for (int i = 0; i < names.length; i++) {
            int col = i / 8;
            int row = i % 8;
            Button check = new Button(group, SWT.CHECK);
            check.setText(names[i]);
            check.setToolTipText(SCREEN_TOOLTIPS[i]);
            check.setBounds((col == 0 ? col1X : col2X), y0 + row * rowH, 120, 19);
            screenChecks[i] = check;
        }

        Label separator = new Label(group, SWT.SEPARATOR | SWT.VERTICAL);
        separator.setBounds(125, 10, 16, 200);

        Label separator2 = new Label(group, SWT.SEPARATOR | SWT.VERTICAL);
        separator2.setBounds(272, 10, 16, 200);

        Button btnReload = new Button(group, SWT.NONE);
        btnReload.setBounds(295, 10, 100, 29);
        btnReload.setText("Ricarica");
        btnReload.addListener(SWT.Selection, e -> loadScreenStates());

        Button btnSave = new Button(group, SWT.NONE);
        btnSave.setBounds(295, 45, 100, 29);
        btnSave.setText("Salva");
        btnSave.addListener(SWT.Selection, e -> saveScreenStates());

        Button btnConfig = new Button(group, SWT.NONE);
        btnConfig.setBounds(295, 90, 100, 29);
        btnConfig.setText("Connessione...");
        btnConfig.setToolTipText("Altre impostazioni del client lcdproc (in arrivo)");
        btnConfig.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                Shell shell = new Shell(getDisplay());
                shell.setText("Connessione LCDd");
                shell.setLayout(new FillLayout());
                new LCDprocConfig(shell, SWT.NONE);
                shell.pack();
                shell.open();
            }
        });

        screensStatusLabel = new Label(group, SWT.WRAP);
        screensStatusLabel.setBounds(295, 130, 220, 80);

        loadScreenStates();

        return group;
    }

    private void loadScreenStates() {
        try {
            Map<String, Boolean> states = LcdProcConfigFile.readScreenStates();
            String[] names = LcdProcConfigFile.SCREENS;
            for (int i = 0; i < names.length; i++) {
                Boolean active = states.get(names[i]);
                screenChecks[i].setSelection(Boolean.TRUE.equals(active));
            }
            screensStatusLabel.setText("Caricato da " + LcdProcConfigFile.PATH);
        } catch (IOException ex) {
            screensStatusLabel.setText("Errore lettura config:\n" + ex.getMessage());
        }
    }

    private void saveScreenStates() {
        Map<String, Boolean> states = new LinkedHashMap<>();
        String[] names = LcdProcConfigFile.SCREENS;
        for (int i = 0; i < names.length; i++) {
            states.put(names[i], screenChecks[i].getSelection());
        }
        try {
            LcdProcConfigFile.writeScreenStates(states);
            screensStatusLabel.setText("Salvato in " + LcdProcConfigFile.PATH);
            offerClientRestart();
        } catch (IOException | InterruptedException ex) {
            screensStatusLabel.setText("Errore salvataggio:\n" + ex.getMessage());
        }
    }

    private void offerClientRestart() {
        if (!clientProcess.isRunningHere() && !LcdProcClientProcess.isRunningAnywhere()) {
            return;
        }
        MessageBox box = new MessageBox(getShell(), SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        box.setText("Riavviare il client?");
        box.setMessage("Il client lcdproc legge la configurazione solo all'avvio.\n"
                + "Riavviarlo ora per mostrare le nuove schermate sull'LCD?");
        if (box.open() == SWT.YES) {
            clientProcess.stop();
            try {
                clientProcess.start();
            } catch (IOException ex) {
                MessageBox err = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                err.setText("Errore");
                err.setMessage("Impossibile riavviare il client lcdproc:\n" + ex.getMessage());
                err.open();
            }
        }
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
