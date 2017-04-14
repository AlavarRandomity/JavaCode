package com.PhotoGallery;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.logging.Logger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationAdapter;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.browser.ProgressAdapter;
import org.eclipse.swt.browser.ProgressEvent;

public class PhotoGallerySWT {
    private static ArrayList<GalleryItem> mItems;
    private static Shell shlPhotogalleryswt;
    private static final Logger Log = Logger.getLogger(PhotoGallerySWT.class.getName());
	private static Device display;
	private static String current_url;
	private static int m_index = 0;
	private static StackLayout stacklayout;
	private static Browser browser;
	private static HttpServer httpserver;
	private static Label lblCode;
	private static Label lblAccesstoken;
	private static Label lblUserid;
	private static Label lblUsername;
	private static Label lblLoc;
	private static Composite InstagramLogin;
	private static Button btnLogout;
	private static Composite FlickrLogin;
	private static boolean do_once;
	private static ExecutorService exec;
	private static FutureTask<ArrayList<GalleryItem>> futuretask;
	private static Canvas cnvPhoto;
	private static Image img;
	private static Text txtSearch;
	private static SearchItemsTask search;
	private static boolean isFetching;
	private static boolean isSearching;
	private static Composite Thumb;
	private static ArrayList<Label> thumbs = null;
	private static Composite thumbsNails;
	private static ScrolledComposite thumbsScroll;
	private static Composite webBrowser;
	private static Browser browserMySite;
	private static ProgressBar progressWebB;
    
    // -----
    
	public static void main(String[] args) {
		do_once = true;
		isFetching = false;
		isSearching = false;
		
		// start an httpserver for instagram OAuth stuff
		httpserver = new HttpServer();
		Thread httpthread = new Thread(httpserver);
		httpthread.start();

		// View related stuffs
		Display display = new Display();
		shlPhotogalleryswt = new Shell(display);
		shlPhotogalleryswt.setSize(348, 524);

		shlPhotogalleryswt.setText("My Photo Gallery");
		stacklayout = new StackLayout();
		shlPhotogalleryswt.setLayout(stacklayout);
		
		Composite Main = new Composite(shlPhotogalleryswt, SWT.NONE);
		Main.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite MainGrid = new Composite(Main, SWT.NONE);
		GridLayout gl_MainGrid = new GridLayout(1, false);
		gl_MainGrid.verticalSpacing = 0;
		gl_MainGrid.marginWidth = 0;
		gl_MainGrid.marginHeight = 0;
		gl_MainGrid.horizontalSpacing = 0;
		MainGrid.setLayout(gl_MainGrid);
		
		Composite searchComposite = new Composite(MainGrid, SWT.NONE);
		searchComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 0, 1));
		RowLayout rl_searchComposite = new RowLayout(SWT.HORIZONTAL);
		rl_searchComposite.marginTop = 0;
		rl_searchComposite.marginRight = 0;
		rl_searchComposite.marginLeft = 0;
		rl_searchComposite.marginBottom = 0;
		searchComposite.setLayout(rl_searchComposite);
		
		Label lblSearch = new Label(searchComposite, SWT.NONE);
		lblSearch.setText("Search:");
		
		txtSearch = new Text(searchComposite, SWT.BORDER);
		txtSearch.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR)
				{
					if (exec != null && !exec.isTerminated())
						exec.shutdownNow();
					m_index = 0;
					search = new SearchItemsTask(""); // TODO: add instagram access_token
					search.setQuery(txtSearch.getText());  // doing the SEARCH!
					futuretask = new FutureTask<ArrayList<GalleryItem>>(search);
					isSearching = true;
					exec = Executors.newFixedThreadPool(1);
					exec.execute(futuretask);					
				}
			}
		});
		txtSearch.setLayoutData(new RowData(150, SWT.DEFAULT));
		
		Composite composite = new Composite(MainGrid, SWT.NONE);
		GridLayout gl_composite = new GridLayout(3, false);
		gl_composite.verticalSpacing = 0;
		gl_composite.marginWidth = 0;
		gl_composite.marginHeight = 0;
		gl_composite.horizontalSpacing = 0;
		composite.setLayout(gl_composite);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button btnPrev = new Button(composite, SWT.NONE);
		btnPrev.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (m_index > 0)
					m_index--;
				showURL(mItems.get(m_index).getUrl());
			}
		});
		btnPrev.setText("p");
		
		cnvPhoto = new Canvas(composite, SWT.NONE);
		cnvPhoto.addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				if (img != null)
					img = resize(img, cnvPhoto.getBounds().width, cnvPhoto.getBounds().height);
			}
		});
		cnvPhoto.addPaintListener(new PaintListener() {
			public void paintControl(PaintEvent arg0) {
				if (img != null)
					arg0.gc.drawImage(img, 0, 0);
			}
		});
		cnvPhoto.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Button btnNext = new Button(composite, SWT.NONE);
		btnNext.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (m_index < mItems.size())
					m_index++;
				showURL(mItems.get(m_index).getUrl());				
			}
		});
		btnNext.setText("n");
		
		Composite LoginButtons = new Composite(MainGrid, SWT.NONE);
		LoginButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false, 1, 1));
		LoginButtons.setBounds(0, 0, 64, 64);
		RowLayout rl_LoginButtons = new RowLayout(SWT.HORIZONTAL);
		rl_LoginButtons.marginTop = 0;
		rl_LoginButtons.marginRight = 5;
		rl_LoginButtons.marginLeft = 0;
		rl_LoginButtons.marginBottom = 0;
		LoginButtons.setLayout(rl_LoginButtons);
		
		progressWebB = new ProgressBar(LoginButtons, SWT.NONE);
		progressWebB.setLayoutData(new RowData(75, 15));
		progressWebB.setVisible(false);
		
		Button btnWebB = new Button(LoginButtons, SWT.NONE);
		btnWebB.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				progressWebB.setVisible(true);
				progressWebB.update();
				btnWebB.setEnabled(false);
				
				browserMySite.setUrl("http://wordp-bpicar.rhcloud.com/");
			}
		});
		btnWebB.setText("Web");
		
		Button btnThumbs = new Button(LoginButtons, SWT.NONE);
		btnThumbs.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				setupGrid();
				
				stacklayout.topControl = Thumb;
				shlPhotogalleryswt.layout();
			}
		});
		btnThumbs.setText("Thumbs");
		
		Label lblFlickrLogin = new Label(LoginButtons, SWT.NONE);
		lblFlickrLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				stacklayout.topControl = FlickrLogin;
				shlPhotogalleryswt.layout();				
			}
		});
		lblFlickrLogin.setImage(SWTResourceManager.getImage(PhotoGallerySWT.class, "/com/PhotoGallery/res/flickr.png"));
		
		Label lblInstaGramLogin = new Label(LoginButtons, SWT.NONE);
		lblInstaGramLogin.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				stacklayout.topControl = InstagramLogin;
				shlPhotogalleryswt.layout();
			}
		});
		lblInstaGramLogin.setImage(SWTResourceManager.getImage(PhotoGallerySWT.class, "/com/PhotoGallery/res/instag.png"));
		
		Composite LoginButtonsGood = new Composite(MainGrid, SWT.NONE);
		LoginButtonsGood.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		RowLayout rl_LoginButtonsGood = new RowLayout(SWT.HORIZONTAL);
		rl_LoginButtonsGood.marginTop = 0;
		rl_LoginButtonsGood.marginRight = 5;
		rl_LoginButtonsGood.marginLeft = 0;
		rl_LoginButtonsGood.marginBottom = 1;
		LoginButtonsGood.setLayout(rl_LoginButtonsGood);
		
		Label lblFlickrLoginGood = new Label(LoginButtonsGood, SWT.NONE);
		lblFlickrLoginGood.setImage(SWTResourceManager.getImage(PhotoGallerySWT.class, "/com/PhotoGallery/res/bad.png"));
		
		Label lblInstaGramLoginGood = new Label(LoginButtonsGood, SWT.NONE);
		lblInstaGramLoginGood.setImage(SWTResourceManager.getImage(PhotoGallerySWT.class, "/com/PhotoGallery/res/bad.png"));
		
		Thumb = new Composite(shlPhotogalleryswt, SWT.NONE);
		Thumb.setLayout(new GridLayout(1, false));
		
		thumbsScroll = new ScrolledComposite(Thumb, SWT.V_SCROLL);
		thumbsScroll.setLayout(new FillLayout(SWT.HORIZONTAL));
		thumbsScroll.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		thumbsNails = new Composite(thumbsScroll, SWT.NONE);
		thumbsNails.setLayout(new GridLayout(5, false));
		
		Button btnBackToMain = new Button(Thumb, SWT.NONE);
		btnBackToMain.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				stacklayout.topControl = Main;
				shlPhotogalleryswt.layout();				
			}
		});
		btnBackToMain.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		btnBackToMain.setText("Back");
		
		InstagramLogin = new Composite(shlPhotogalleryswt, SWT.NONE);
		InstagramLogin.setLayout(new FillLayout(SWT.HORIZONTAL));

		Composite InstagramLoginGrid = new Composite(InstagramLogin, SWT.NONE);
		InstagramLoginGrid.setLayout(new GridLayout(1, false));
		
		browser = new Browser(InstagramLoginGrid, SWT.NONE);
		browser.addLocationListener(new LocationAdapter() {
			@Override
			public void changed(LocationEvent event) {
				if (httpserver.getCode().length() > 2)
				{
					String access_token = "";
					String user_id = "";
					String user_name = "";

					InstaFetchr quickfetch = new InstaFetchr();
					try {
						String response = quickfetch.postATUrl(InstaFetchr.AT_REQUEST, httpserver.getCode());
						//System.out.println(response);
						
						String delims = "{}:\",";
						StringTokenizer st = new StringTokenizer(response, delims);
						while (st.hasMoreElements()) {
							String elem = (String) st.nextElement();
							if (elem.compareTo("access_token") == 0)
								access_token = (String) st.nextElement();
							else if (elem.compareTo("id") == 0)
								user_id = (String) st.nextElement();
							else if (elem.compareTo("username") == 0)
								user_name = (String) st.nextElement();
							//System.out.println("st Output: " + elem);
							
							do_once = true;
							getPhotoThreads(access_token);
						}						
					} catch (IOException e) {
						//e.printStackTrace();
					}
					
					setInstagramData(httpserver.getCode(), access_token, user_id, user_name);
					
					if (access_token.length() > 2) {
						// hide browser
						browser.setVisible(false);
						// change instagram icon to have green underline (good)
						lblInstaGramLoginGood.setImage(SWTResourceManager.getImage(PhotoGallerySWT.class, "/com/PhotoGallery/res/good.png"));
					} else {
						// change instagram icon to have red underline (bad)
						lblInstaGramLoginGood.setImage(SWTResourceManager.getImage(PhotoGallerySWT.class, "/com/PhotoGallery/res/bad.png"));
					}
				}
				//lblLoc.setText(event.location);
			}
			@Override
			public void changing(LocationEvent event) {
				//lblLoc.setText(event.location);
			}
		});
		GridData gd_browser = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_browser.heightHint = 250;
		gd_browser.widthHint = 261;
		browser.setLayoutData(gd_browser);
		browser.setSize(157, 486);
		browser.setUrl(InstaFetchr.AUTH_URI);
		
		Label lblClientid = new Label(InstagramLoginGrid, SWT.NONE);
		lblClientid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblClientid.setText("client_id: " + InstaFetchr.CLIENT_ID);
		
		Label lblClientsecret = new Label(InstagramLoginGrid, SWT.NONE);
		lblClientsecret.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblClientsecret.setText("client_secret: " + InstaFetchr.CLIENT_SECRET);
		
		Label lblRedirecturl = new Label(InstagramLoginGrid, SWT.NONE);
		lblRedirecturl.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblRedirecturl.setText("redirect_url: " + InstaFetchr.REDIRECT_URI);
		
		lblCode = new Label(InstagramLoginGrid, SWT.NONE);
		lblCode.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblCode.setText("code: ");
		
		lblAccesstoken = new Label(InstagramLoginGrid, SWT.NONE);
		lblAccesstoken.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblAccesstoken.setText("access_token: ");
		
		lblUserid = new Label(InstagramLoginGrid, SWT.NONE);
		lblUserid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblUserid.setText("user_id:");
		
		lblUsername = new Label(InstagramLoginGrid, SWT.NONE);
		lblUsername.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblUsername.setText("user_name:");
		
		lblLoc = new Label(InstagramLoginGrid, SWT.NONE);
		lblLoc.setAlignment(SWT.CENTER);
		lblLoc.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Composite instagrambtncomposite = new Composite(InstagramLoginGrid, SWT.NONE);
		instagrambtncomposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		instagrambtncomposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		btnLogout = new Button(instagrambtncomposite, SWT.NONE);
		btnLogout.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (btnLogout.getText().compareTo("Logout") == 0)
				{
					browser.setVisible(false);
					browser.setUrl("https://instagram.com/accounts/logout/");
					Browser.clearSessions(); // this should clear cookies
					if (browser.getBrowserType().compareTo("ie") == 0)
					{
						// do some *special* cookie clearing for IEcrap!!
						try {
							Runtime.getRuntime().exec("RunDll32.exe InetCpl.cpl,ClearMyTracksByProcess 2");
						} catch (IOException e1) {
							e1.printStackTrace();
						}
					}

					btnLogout.setText("Login");
				} else {
					browser.setUrl(InstaFetchr.AUTH_URI);
					browser.redraw();
					browser.setVisible(true);
					
					btnLogout.setText("Logout");					
				}
			}
		});
		btnLogout.setText("Logout");
		
		Button btnBack = new Button(instagrambtncomposite, SWT.NONE);
		btnBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				stacklayout.topControl = Main;
				shlPhotogalleryswt.layout();				
			}
		});
		btnBack.setText("Back");
		browser.redraw();

		FlickrLogin = new Composite(shlPhotogalleryswt, SWT.NONE);
		FlickrLogin.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Composite FlickrLoginGrid = new Composite(FlickrLogin, SWT.NONE);
		FlickrLoginGrid.setLayout(new GridLayout(1, false));
		
		Label lblFlickr = new Label(FlickrLoginGrid, SWT.NONE);
		lblFlickr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblFlickr.setText("Flickr");
		
		Label lblapikey = new Label(FlickrLoginGrid, SWT.NONE);
		lblapikey.setText("API key: " + FlickrFetchr.API_KEY);
		
		Label lblSpacer = new Label(FlickrLoginGrid, SWT.NONE);
		lblSpacer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 1, 1));
		
		Composite flickrbtncomposite = new Composite(FlickrLoginGrid, SWT.NONE);
		flickrbtncomposite.setLayout(new RowLayout(SWT.HORIZONTAL));
		flickrbtncomposite.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		
		Button btnNewButton = new Button(flickrbtncomposite, SWT.NONE);
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				stacklayout.topControl = Main;
				shlPhotogalleryswt.layout();				
			}
		});
		btnNewButton.setText("Back");
		
		// test Flickr connection
		FlickrFetchr tmp = new FlickrFetchr();
		if (tmp.testConnection())
			lblFlickrLoginGood.setImage(SWTResourceManager.getImage(PhotoGallerySWT.class, "/com/PhotoGallery/res/good.png"));
		
		getPhotoThreads("");

		stacklayout.topControl = Main;	
		
		webBrowser = new Composite(shlPhotogalleryswt, SWT.NONE);
		webBrowser.setLayout(new GridLayout(1, false));
		
		browserMySite = new Browser(webBrowser, SWT.NONE);
		browserMySite.addProgressListener(new ProgressAdapter() {
			@Override
			public void changed(ProgressEvent event) {
				//event.current;
				//event.total;
				progressWebB.setSelection(event.current);
				progressWebB.setMaximum(event.total);
				progressWebB.update();
			}
			@Override
			public void completed(ProgressEvent event) {
				stacklayout.topControl = webBrowser;
				shlPhotogalleryswt.layout();
			}
		});
		GridData gd_browserMySite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_browserMySite.heightHint = 151;
		gd_browserMySite.widthHint = 193;
		browserMySite.setLayoutData(gd_browserMySite);
		
		Composite webBrowserButtons = new Composite(webBrowser, SWT.NONE);
		webBrowserButtons.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		webBrowserButtons.setLayout(new GridLayout(1, false));
		
		Button btnBBack = new Button(webBrowserButtons, SWT.NONE);
		btnBBack.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				btnWebB.setEnabled(true);
				progressWebB.setVisible(false);
				
				
				stacklayout.topControl = Main;
				shlPhotogalleryswt.layout();
			}
		});
		btnBBack.setText("Back");
		shlPhotogalleryswt.open();

		try {
			while (!shlPhotogalleryswt.isDisposed()) {
				if (!display.readAndDispatch()) 
					display.sleep();

				// if futuretask is done AND only do this once!
				if (futuretask.isDone() && (do_once || isFetching || isSearching)) {
					try {
						mItems = futuretask.get(); // actual get of items
						if (mItems != null && mItems.size() > 0) {
							current_url = mItems.get(m_index).getUrl();
							showURL(current_url);
						}
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					}

					do_once = false;
					isFetching = false;
					isSearching = false;
					exec.shutdown();
				}
			}
		} catch (Exception e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		httpserver.setStopFlag(true);
		try {
			httpthread.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		display.dispose();
	}

	private static void getPhotoThreads(String access_token) {
		FetchItemsTask fetch = new FetchItemsTask(access_token);
		futuretask = new FutureTask<ArrayList<GalleryItem>>(fetch);
		
		isFetching = true;
		
		exec = Executors.newFixedThreadPool(1);
		exec.execute(futuretask);
	}
	
	private static void setInstagramData(String code, String access, String user_id, String user_name) {
		lblCode.setText("code: " + code);
		lblAccesstoken.setText("access_token: " + access);
		lblUserid.setText("user_id: " + user_id);
		lblUsername.setText("user_name: " + user_name);	
	}
	
	public static void setupGrid() {
		if (mItems == null || mItems.size() < 1)
			return;
		if (thumbs == null) {
			thumbs = new ArrayList<Label>();
		}
		thumbs.removeAll(thumbs);
		for (int i = 0; i < mItems.size(); i++) {
			thumbs.add(new Label(thumbsNails, SWT.NONE));
			
			// change picture of this canvas to one in mItems
			URL tmpUrl;
			try {
				String url_s = mItems.get(i).getUrl();
				String url = "";
	        	// take out _m and replace with _s (for Square)
	        	String tmp = url_s.substring(url_s.length()-6, url_s.length()-4);
	        	if (tmp.contains("_m"))
	        		url = url_s.substring(0, url_s.length()-6) + "_s" +
	        			url_s.substring(url_s.length()-4, url_s.length());
	        	// ---				

	        	System.out.printf("grid %d: %s\n", i, url);
				
				tmpUrl = new URL(url);
				Image tmp_img = new Image(display, tmpUrl.openStream());
				thumbs.get(i).setImage(tmp_img);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		thumbsScroll.setContent(thumbsNails);
		thumbsScroll.setMinSize(thumbsNails.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		thumbsNails.pack();
		thumbsNails.layout();
	}

    public static void showURL(String url_s) {
        URL myUrl;
        String url = url_s; // use url for the "true" image (not thumbnail)

        if (url_s == null)
            return;
        try {
        	// take out _m and the like
        	String tmp = url_s.substring(url_s.length()-6, url_s.length()-4);
        	if (tmp.contains("_m"))
        		url = url_s.substring(0, url_s.length()-6) +
        			url_s.substring(url_s.length()-4, url_s.length());
        	// ---
        	
            Log.info("url: " + url);
            myUrl = new URL(url);
            
            img = new Image(display, myUrl.openStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        img = resize(img, cnvPhoto.getBounds().width, cnvPhoto.getBounds().height);
        cnvPhoto.redraw();
    }	
	
    private static Image resize(Image image, int width, int height) {
        // stolen from http://aniszczyk.org/2007/08/09/resizing-images-using-swt/
        Image scaled = new Image(Display.getDefault(), width, height);
        GC gc = new GC(scaled);
        gc.setAntialias(SWT.ON);
        gc.setInterpolation(SWT.HIGH);
        gc.drawImage(image, 0, 0, 
                     image.getBounds().width, image.getBounds().height, 
                     0, 0, width, height);
        gc.dispose();
        image.dispose(); // don't forget about me!
        return scaled;
    }  	
}
