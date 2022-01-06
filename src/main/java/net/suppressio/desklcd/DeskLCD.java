package net.suppressio.desklcd;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class DeskLCD extends Composite {

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
		
		TabItem tbtmNewItem = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem.setText("LCD Proc Standard");
		
		Group group = new Group(tabFolder, SWT.NONE);
		tbtmNewItem.setControl(group);
		
		Button btnCheckButton = new Button(group, SWT.CHECK);
		btnCheckButton.setToolTipText("Detailed CPU usage");
		btnCheckButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnCheckButton.setBounds(10, 10, 109, 19);
		btnCheckButton.setText("CPU");
		
		Button btnCheckButton_1 = new Button(group, SWT.CHECK);
		btnCheckButton_1.setToolTipText("CPU usage overview (one line per CPU)");
		btnCheckButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnCheckButton_1.setBounds(10, 35, 109, 19);
		btnCheckButton_1.setText("SMP-CPU");
		
		Button btnCheckButton_2 = new Button(group, SWT.CHECK);
		btnCheckButton_2.setToolTipText("CPU usage histogram");
		btnCheckButton_2.setBounds(10, 60, 109, 19);
		btnCheckButton_2.setText("CPUGraph");
		
		Button btnCheckButton_3 = new Button(group, SWT.CHECK);
		btnCheckButton_3.setToolTipText("Load histogram");
		btnCheckButton_3.setBounds(10, 85, 109, 19);
		btnCheckButton_3.setText("Load");
		
		Button btnCheckButton_4 = new Button(group, SWT.CHECK);
		btnCheckButton_4.setToolTipText("Memory & swap usage");
		btnCheckButton_4.setBounds(10, 110, 109, 19);
		btnCheckButton_4.setText("Memory");
		
		Button btnCheckButton_5 = new Button(group, SWT.CHECK);
		btnCheckButton_5.setBounds(10, 135, 109, 19);
		btnCheckButton_5.setText("ProcSize");
		
		Button btnCheckButton_6 = new Button(group, SWT.CHECK);
		btnCheckButton_6.setBounds(10, 160, 109, 19);
		btnCheckButton_6.setText("Disk");
		
		Button btnCheckButton_7 = new Button(group, SWT.CHECK);
		btnCheckButton_7.setBounds(10, 185, 109, 19);
		btnCheckButton_7.setText("Iface");
		
		Label label = new Label(group, SWT.SEPARATOR | SWT.VERTICAL);
		label.setBounds(125, 10, 16, 200);
		
		Button btnCheckButton_8 = new Button(group, SWT.CHECK);
		btnCheckButton_8.setBounds(147, 10, 109, 19);
		btnCheckButton_8.setText("Battery");
		
		Button btnCheckButton_9 = new Button(group, SWT.CHECK);
		btnCheckButton_9.setBounds(147, 35, 109, 19);
		btnCheckButton_9.setText("TimeDate");
		
		Button btnCheckButton_10 = new Button(group, SWT.CHECK);
		btnCheckButton_10.setBounds(147, 60, 109, 19);
		btnCheckButton_10.setText("OldTime");
		
		Button btnCheckButton_11 = new Button(group, SWT.CHECK);
		btnCheckButton_11.setBounds(147, 85, 109, 19);
		btnCheckButton_11.setText("Uptime");
		
		Button btnCheckButton_12 = new Button(group, SWT.CHECK);
		btnCheckButton_12.setBounds(147, 110, 109, 19);
		btnCheckButton_12.setText("BigClock");
		
		Button btnCheckButton_13 = new Button(group, SWT.CHECK);
		btnCheckButton_13.setBounds(147, 135, 109, 19);
		btnCheckButton_13.setText("MiniClock");
		
		Button btnCheckButton_14 = new Button(group, SWT.CHECK);
		btnCheckButton_14.setBounds(147, 160, 109, 19);
		btnCheckButton_14.setText("About");
		
		
		Label label2 = new Label(group, SWT.SEPARATOR | SWT.VERTICAL);
		label2.setBounds(250, 10, 16, 200);
		
		Button btnNewButton = new Button(group, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnNewButton.setBounds(272, 45, 91, 29);
		btnNewButton.setText("Start/Stop");
		
		Button btnNewButton_1 = new Button(group, SWT.NONE);
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {

				final Shell shell = new Shell(getDisplay());
			    shell.setLayout(new FillLayout());
			    
			    shell.setLocation(e.x, e.y);
			    new LCDprocConfig(shell,1);
							    
			    shell.pack();
			    shell.open();
				
			}
		});
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
			}
		});
		btnNewButton_1.setBounds(272, 10, 91, 29);
		btnNewButton_1.setText("Config");
		
		
		TabItem tbtmNewItem_1 = new TabItem(tabFolder, SWT.NONE);
		tbtmNewItem_1.setText("Custom");
		


	}

	private void tab1() {
		
	}
	
	public Point getLocation() {
		return this.getLocation();
	}
	
	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}
}
