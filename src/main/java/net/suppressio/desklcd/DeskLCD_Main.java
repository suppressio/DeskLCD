package net.suppressio.desklcd;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class DeskLCD_Main {

	public static void main(String Args[]) {

		Display display = new Display();
	    final Shell shell = new Shell(display);

	    shell.setLayout(new FillLayout());
	    shell.setText("DeskLCD");

	    DeskLCD deskLcd = new DeskLCD(shell, 1);
	    shell.addListener(SWT.Dispose, e -> deskLcd.stopBackgroundProcesses());
	    new TrayIconManager(shell);

	    shell.pack();
	    shell.open();
	    while (!shell.isDisposed()) {
	    	if (!display.readAndDispatch()) display.sleep();
       }
	    display.dispose();
	}

}
