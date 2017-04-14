package com.bp.criminalintent;
import java.util.Calendar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;

public class CriminalIntentSWT {
	// --- Fields/Member Variables
	private static Text txtCaseTitle;
	private static Crime crime;
	private static CrimeListSingleton cls;
	private static Composite compositeCrimeList;
	private static List listCrimes;
	
	private static boolean newCrime = false;
	private static DateTime dateCaseDate;
	private static DateTime dateCaseTime;
	private static CoreJavaSound cjs = null;

	// --- Methods/Member Functions
	public static void updateCrimeList()
	{
		listCrimes.removeAll();
		for (int i = 0; i < cls.getSize(); ++i)
			listCrimes.add(cls.getCrime(i).getmTitle() + 
					" (" + cls.getCrime(i).getmDate().getTime() + ") " + 
					(cls.getCrime(i).ismSolved() ? "solved" : ""));
		
		listCrimes.select(cls.getSize() - 1);
	}
	
	public static void updateDates()
	{
		Calendar d = Calendar.getInstance();
		d.set(Calendar.YEAR, dateCaseDate.getYear());
		d.set(Calendar.MONTH, dateCaseDate.getMonth());
		d.set(Calendar.DAY_OF_MONTH, dateCaseDate.getDay());
		int milHour = dateCaseTime.getHours();
		if (milHour > 12) {
			milHour -= 12;
			d.set(Calendar.AM_PM, Calendar.PM);
		}
		d.set(Calendar.HOUR, milHour);
		d.set(Calendar.MINUTE, dateCaseTime.getMinutes());
		d.set(Calendar.SECOND, dateCaseTime.getSeconds());

		crime.setmDate(d);		
	}
			
	public static void main(String[] args) {
		Display display = new Display();
		Shell shlCaseLogger = new Shell(display);
		crime = new Crime();
		shlCaseLogger.setText("Case Logger");
		shlCaseLogger.setSize(415, 313);
		shlCaseLogger.setLayout(new StackLayout());
		
		Composite compositeSingleCrime = new Composite(shlCaseLogger, SWT.NONE);

		txtCaseTitle = new Text(compositeSingleCrime, SWT.BORDER);
		txtCaseTitle.setLocation(10, 10);
		txtCaseTitle.setSize(377, 21);
		txtCaseTitle.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent arg0) {
				//System.out.println(text.getText().toString());
				crime.setmTitle(txtCaseTitle.getText().toString());
			}
		});
		txtCaseTitle.setToolTipText("Enter a title for the Crime");

		dateCaseDate = new DateTime(compositeSingleCrime, SWT.BORDER | SWT.CALENDAR);
		dateCaseDate.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateDates();				
			}
		});
		dateCaseDate.setLocation(10, 37);
		dateCaseDate.setSize(241, 158);

		dateCaseTime = new DateTime(compositeSingleCrime, SWT.BORDER | SWT.TIME);
		dateCaseTime.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				updateDates();
			}
		});
		dateCaseTime.setLocation(257, 37);
		dateCaseTime.setSize(130, 24);

		Button btnCaseSolved = new Button(compositeSingleCrime, SWT.CHECK);
		btnCaseSolved.setLocation(20, 201);
		btnCaseSolved.setSize(130, 21);
		btnCaseSolved.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				//System.out.printf("solved? : %b\n", btnSolved.getSelection());
				crime.setmSolved(btnCaseSolved.getSelection());
				
				// play law n order sound byte
				if (btnCaseSolved.getSelection())
				cjs.start();
			}
		});
		btnCaseSolved.setToolTipText("Is the crime Solved?");
		btnCaseSolved.setText("Solved?");

		Button btnBack = new Button(compositeSingleCrime, SWT.NONE);
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				StackLayout sl = (StackLayout) shlCaseLogger.getLayout();
				sl.topControl = compositeCrimeList;
				shlCaseLogger.layout();		
				
				if (newCrime)
					cls.addCrime(crime);

				updateCrimeList();				
			}
		});
		btnBack.setBounds(312, 199, 75, 25);
		btnBack.setText("Back");

		compositeCrimeList = new Composite(shlCaseLogger, SWT.NONE);

		listCrimes = new List(compositeCrimeList, SWT.BORDER | SWT.V_SCROLL);
		listCrimes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				StackLayout sl = (StackLayout) shlCaseLogger.getLayout();
				sl.topControl = compositeSingleCrime;
				shlCaseLogger.layout();
				
				// not a new crime
				newCrime = false;
				int idx = listCrimes.getSelectionIndex();
				// get existing crime
				crime = cls.getCrime(idx);				

				// fill out Title
				String str = "";
				str += cls.getCrime(idx).getmTitle();
				txtCaseTitle.setText(str);
				
				// fill out Date and Time
				Calendar d = cls.getCrime(idx).getmDate();
				dateCaseDate.setDate(d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DAY_OF_MONTH));
				dateCaseTime.setTime(d.get(Calendar.HOUR_OF_DAY), d.get(Calendar.MINUTE), d.get(Calendar.SECOND));
				
				// fill out Solved
				boolean tmp = cls.getCrime(idx).ismSolved();
				btnCaseSolved.setSelection(tmp);
			}
		});
		listCrimes.setBounds(10, 10, 379, 204);
		
		Button btnNewCrime = new Button(compositeCrimeList, SWT.NONE);
		btnNewCrime.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				StackLayout sl = (StackLayout) shlCaseLogger.getLayout();
				sl.topControl = compositeSingleCrime;
				shlCaseLogger.layout(); // goto crime entry for this new crime				

				// get a new id
				crime = new Crime();
				newCrime = true;

				// reset out Title
				String str = "";
				txtCaseTitle.setText(str);
				
				// reset out Date and Time
				Calendar d = Calendar.getInstance();
				dateCaseDate.setDate(d.get(Calendar.YEAR), d.get(Calendar.MONTH), d.get(Calendar.DAY_OF_MONTH));
				dateCaseTime.setTime(d.get(Calendar.HOUR_OF_DAY), d.get(Calendar.MINUTE), d.get(Calendar.SECOND));
				
				// reset out Solved
				boolean tmp = false;
				btnCaseSolved.setSelection(tmp);
			}
		});
		btnNewCrime.setBounds(314, 220, 75, 25);
		btnNewCrime.setText("New Crime");
		
		cls = CrimeListSingleton.getInstance();
		cls.CreateOneHundred();
		updateCrimeList();
				
		Menu menu = new Menu(shlCaseLogger, SWT.BAR);
		shlCaseLogger.setMenuBar(menu);
		
		MenuItem mntmFile = new MenuItem(menu, SWT.CASCADE);
		mntmFile.setText("File");
		
		Menu menu_1 = new Menu(mntmFile);
		mntmFile.setMenu(menu_1);
		
		MenuItem mntmExit = new MenuItem(menu_1, SWT.NONE);
		mntmExit.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				shlCaseLogger.dispose(); // exit the app from File > Exit
			}
		});
		mntmExit.setText("Exit");
		
		MenuItem mntmView = new MenuItem(menu, SWT.CASCADE);
		mntmView.setText("View");
		
		Menu menu_2 = new Menu(mntmView);
		mntmView.setMenu(menu_2);
		
		MenuItem mntmCase = new MenuItem(menu_2, SWT.CHECK);
		mntmCase.setText("Case");

		((StackLayout)shlCaseLogger.getLayout()).topControl = 
				compositeCrimeList;
		
		Button btnLoad = new Button(compositeCrimeList, SWT.NONE);
		btnLoad.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				CrimeListIO.loadCrimes();
			}
		});
		btnLoad.setBounds(10, 220, 75, 25);
		btnLoad.setText("Load");
		
		Button btnSave = new Button(compositeCrimeList, SWT.NONE);
		btnSave.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				CrimeListIO.saveCrimes();
			}
		});
		btnSave.setBounds(91, 220, 75, 25);
		btnSave.setText("Save");
		
		try {
			cjs = new CoreJavaSound();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
		shlCaseLogger.open();
		
		while (!shlCaseLogger.isDisposed()) {
			if (!display.readAndDispatch()) 
				display.sleep();
		}
		display.dispose();
	}
}
