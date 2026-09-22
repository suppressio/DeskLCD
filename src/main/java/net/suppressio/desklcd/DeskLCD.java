package net.suppressio.desklcd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class DeskLCD extends Composite {

    /** Message keys for each screen's tooltip, in the same order as LcdProcConfigFile.SCREENS. */
    private static final String[] SCREEN_TOOLTIP_KEYS = {
            "screen.tooltip.cpu",
            "screen.tooltip.smpCpu",
            "screen.tooltip.cpuGraph",
            "screen.tooltip.load",
            "screen.tooltip.memory",
            "screen.tooltip.procSize",
            "screen.tooltip.disk",
            "screen.tooltip.iface",
            "screen.tooltip.battery",
            "screen.tooltip.timeDate",
            "screen.tooltip.oldTime",
            "screen.tooltip.uptime",
            "screen.tooltip.bigClock",
            "screen.tooltip.miniClock",
            "screen.tooltip.about"
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
        statusTab.setText(Messages.get("deskLcd.tab.status"));
        statusTab.setControl(new StatusPanel(tabFolder, SWT.NONE, clientProcess));

        TabItem screensTab = new TabItem(tabFolder, SWT.NONE);
        screensTab.setText(Messages.get("deskLcd.tab.screens"));
        screensTab.setControl(buildScreensTab(tabFolder));

        TabItem customTab = new TabItem(tabFolder, SWT.NONE);
        customTab.setText(Messages.get("deskLcd.tab.custom"));
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
            check.setToolTipText(Messages.get(SCREEN_TOOLTIP_KEYS[i]));
            check.setBounds((col == 0 ? col1X : col2X), y0 + row * rowH, 120, 19);
            screenChecks[i] = check;
        }

        Label separator = new Label(group, SWT.SEPARATOR | SWT.VERTICAL);
        separator.setBounds(125, 10, 16, 200);

        Label separator2 = new Label(group, SWT.SEPARATOR | SWT.VERTICAL);
        separator2.setBounds(272, 10, 16, 200);

        Button btnReload = new Button(group, SWT.NONE);
        btnReload.setBounds(295, 10, 100, 29);
        btnReload.setText(Messages.get("screens.button.reload"));
        btnReload.addListener(SWT.Selection, e -> loadScreenStates());

        Button btnSave = new Button(group, SWT.NONE);
        btnSave.setBounds(295, 45, 100, 29);
        btnSave.setText(Messages.get("screens.button.save"));
        btnSave.addListener(SWT.Selection, e -> saveScreenStates());

        Button btnConfig = new Button(group, SWT.NONE);
        btnConfig.setBounds(295, 90, 100, 29);
        btnConfig.setText(Messages.get("screens.button.connection"));
        btnConfig.setToolTipText(Messages.get("screens.button.connection.tooltip"));
        btnConfig.addSelectionListener(new org.eclipse.swt.events.SelectionAdapter() {
            @Override
            public void widgetSelected(org.eclipse.swt.events.SelectionEvent e) {
                Shell shell = new Shell(getDisplay());
                shell.setText(Messages.get("screens.connectionDialog.title"));
                shell.setLayout(new FillLayout());
                new LCDprocConfig(shell, SWT.NONE, clientProcess);
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
            screensStatusLabel.setText(Messages.get("screens.status.loaded", LcdProcConfigFile.PATH));
        } catch (IOException ex) {
            screensStatusLabel.setText(Messages.get("screens.status.loadError", ex.getMessage()));
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
            screensStatusLabel.setText(Messages.get("screens.status.saved", LcdProcConfigFile.PATH));
            ClientRestartPrompt.offer(getShell(), clientProcess);
        } catch (IOException | InterruptedException ex) {
            screensStatusLabel.setText(Messages.get("screens.status.saveError", ex.getMessage()));
        }
    }

    /** Stops any background process started by this GUI (the lcdproc client). Call before exiting. */
    public void stopBackgroundProcesses() {
        clientProcess.stop();
    }

    @Override
    protected void checkSubclass() {
        // Disable the check that prevents subclassing of SWT components
    }
}
