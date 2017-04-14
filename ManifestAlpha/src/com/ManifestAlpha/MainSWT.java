package com.ManifestAlpha;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.SWT;

public class MainSWT {
		
	public static void main(String[] args) {
		openShell();
	}

	public static void openShell() {
		Display display = new Display();
		Shell shell = new Shell(display);
		
		shell.open();
		shell.setLayout(null);
		
		Composite GameIntro = new Composite(shell, SWT.NONE);
		GameIntro.setBounds(0, 0, 434, 261);
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
