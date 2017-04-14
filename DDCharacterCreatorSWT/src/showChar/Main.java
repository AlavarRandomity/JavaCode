package showChar;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class Main {
	private static Text txtCharCnt;

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setSize(317, 149);
		shell.setText("DDCharacter");
		
		Button btnAThing = new Button(shell, SWT.NONE);
		btnAThing.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				String command = "java -jar C:\\Users\\Student\\Desktop\\DDChar.jar " + txtCharCnt.getText();
				try {
					String s;
					Process p = Runtime.getRuntime().exec(command);
					p.waitFor();
					BufferedReader stdInput = new BufferedReader(new InputStreamReader(
							p.getInputStream()));
					BufferedReader stdError = new BufferedReader(new InputStreamReader(
							p.getErrorStream()));
					
					while ((s = stdInput.readLine()) !=null) {
						System.out.println(s);
					}
					
					while ((s = stdError.readLine()) != null) {
						System.out.println(s);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		});
		btnAThing.setBounds(113, 10, 75, 25);
		btnAThing.setText("A thing");
		
		txtCharCnt = new Text(shell, SWT.BORDER);
		txtCharCnt.setText("5");
		txtCharCnt.setBounds(97, 57, 103, 25);
		
		shell.open();
		
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
