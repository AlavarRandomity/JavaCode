package com.bp.randomlines;
import java.awt.Point;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.wb.swt.SWTResourceManager;

public class RandomLines {

	private static Canvas canvas;
	private static Image bufferImage = null;
	private static GC bufferGC = null;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display, SWT.CLOSE);
		shell.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
		shell.setSize(320, 200);
		
		bufferImage = new Image(display, 320, 200);
		bufferGC = new GC(bufferImage);
		
		canvas = new Canvas(shell, SWT.DOUBLE_BUFFERED);
		canvas.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent arg0) {
				Point p1 = new Point();
				Point p2 = new Point();
				p1.x = (int) (Math.random() * canvas.getSize().x);
				p1.y = (int) (Math.random() * canvas.getSize().y);
				p2.x = (int) (Math.random() * canvas.getSize().x);
				p2.y = (int) (Math.random() * canvas.getSize().y);
				int r = (int) (Math.random() * 255);
				int g = (int) (Math.random() * 255);
				int b = (int) (Math.random() * 255);
				
				bufferGC.setForeground(SWTResourceManager.getColor(r, g, b));
				bufferGC.drawLine(p1.x, p1.y, p2.x, p2.y);
				arg0.gc.drawImage(bufferImage, 0, 0);
			}
		});
		canvas.setBounds(0, 0, 320, 200);
		
		shell.open();
		shell.setLayout(null);
		
		while (!shell.isDisposed()) {
			long last_time = 0;
			try {
				if (!display.readAndDispatch())
					display.sleep();
				else if (!shell.isDisposed())
				{
					if (System.currentTimeMillis() > (last_time+500)) {
						last_time = System.currentTimeMillis();
						randomLines();
					} else {
						display.sleep();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		display.dispose();
	}

	private static void randomLines() {
		// redraw canvas TODO
		canvas.redraw();
	}
}
