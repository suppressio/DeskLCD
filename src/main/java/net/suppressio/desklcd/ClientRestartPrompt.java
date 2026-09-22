package net.suppressio.desklcd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

import java.io.IOException;

/**
 * Shared "restart the lcdproc client to apply changes?" prompt, shown after
 * saving configuration the client only reads once, at startup.
 */
final class ClientRestartPrompt {

    private ClientRestartPrompt() {
    }

    static void offer(Shell shell, LcdProcClientProcess clientProcess) {
        if (!clientProcess.isRunningHere() && !LcdProcClientProcess.isRunningAnywhere()) {
            return;
        }
        MessageBox box = new MessageBox(shell, SWT.ICON_QUESTION | SWT.YES | SWT.NO);
        box.setText(Messages.get("client.restartPrompt.title"));
        box.setMessage(Messages.get("client.restartPrompt.message"));
        if (box.open() == SWT.YES) {
            clientProcess.stop();
            try {
                clientProcess.start();
            } catch (IOException ex) {
                MessageBox err = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                err.setText(Messages.get("error.title"));
                err.setMessage(Messages.get("client.restartError", ex.getMessage()));
                err.open();
            }
        }
    }
}
