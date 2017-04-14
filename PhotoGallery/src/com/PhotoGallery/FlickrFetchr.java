package com.PhotoGallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class FlickrFetchr {
	public static final String TAG = "FlickrFetchr"; // for debug

	private static final String ENDPOINT = "https://api.flickr.com/services/rest/";
	public static final String API_KEY = "69acb75e1ed5dd9189f8cdce95253f76";
	private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
	private static final String METHOD_SEARCH = "flickr.photos.search";
	private static final String METHOD_ECHO = "flickr.test.echo";
	private static final String PARAM_EXTRAS = "extras";
	private static final String EXTRA_SMALL_URL = "url_s";
	private static final String XML_PHOTO = "photo";

	private static final String RECENT_URI = ENDPOINT + "?method=" + METHOD_GET_RECENT + 
			"&api_key=" + API_KEY + "&" + PARAM_EXTRAS + "=" + EXTRA_SMALL_URL; 
	
	
	private static final String SEARCH_URI = ENDPOINT + "?method=" + METHOD_SEARCH + 
			"&api_key=" + API_KEY + "&" + PARAM_EXTRAS + "=" + EXTRA_SMALL_URL + "&text=";
	
	
	private static final String ECHO_URI = ENDPOINT + "?method=" + METHOD_ECHO +
			"&api_key=" + API_KEY;

	private static final Logger Log = Logger.getLogger(FlickrFetchr.class.getName());
	// -----

	// param: String urlSpec
	// returning byte array, or null on fail
	public byte[] getUrlBytes(String urlSpec) throws IOException {
		URL url = new URL(urlSpec); // construct a url
		// open that url
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();

		// try block
		try {
			// river of bytes
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			// river of bytes other way
			InputStream in = connection.getInputStream();

			// connection check if it's good
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
				return null;
			}

			// create a byte array of 1024
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			while ((bytesRead = in.read(buffer)) > 0) {
				// write out the connections bytesteam to out
				out.write(buffer, 0, bytesRead);
			}
			out.close();
			// return byte array
			return out.toByteArray();
		} finally {
			// disconnect no matter way
			connection.disconnect();
		}
	}

	// getUrl:
	// taking a urlSpec calling getUrlBytes 
	// returning that bytestream as a String
	String getUrl(String urlSpec) throws IOException {
		return new String(getUrlBytes(urlSpec));
	}

	public ArrayList<GalleryItem> downloadItems(String str) {
		// alloc items array
		ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();

		try {
			// form a uri
			URI uri = URI.create(str);
			String xmlString = getUrl(uri.toString());
			// some logging
			Log.info(TAG + " - Received xml: " + xmlString);

			// reader for xml
			XMLInputFactory factory = XMLInputFactory.newInstance();
			XMLStreamReader reader = factory.createXMLStreamReader(
						new StringReader(xmlString));
						
			// parse out different items in that reader
			parseItems(items, reader);
		} catch (IOException ioe) {
			Log.severe(TAG + " - Failed to fetch items: " +  ioe.toString());
		} catch (XMLStreamException se) {
			Log.severe(TAG + " - Failed to fetch items: " + se.toString());
		}
		return items;		
	}
	
	// return array of GalleryItems or empty items on fail
	public ArrayList<GalleryItem> fetchItems() {
		System.out.println("fetch");
		return downloadItems(RECENT_URI);
	}
	
	public ArrayList<GalleryItem> searchItems(String query) {
		System.out.println(query);
		
		return downloadItems(SEARCH_URI + query);
	}	
	
	public boolean testConnection() {
		boolean good = false;
		try {
			// form a uri
			URI uri = URI.create(ECHO_URI);
			String xmlString = getUrl(uri.toString());
			// some logging
			Log.info(TAG + " - Received echo: " + xmlString);
			good = true;
		} catch (IOException ioe) {
			Log.severe(TAG + " - Failed connection: " +  ioe.toString());
		}
		return good;		
	}

	// parse the XML into array of gallery items
	// return is in first param items
	void parseItems(ArrayList<GalleryItem> items, XMLStreamReader reader) throws XMLStreamException, IOException {
		int eventType = reader.next();
		
		// loop over steam until the end
		while (eventType != XMLStreamReader.END_DOCUMENT) {
			
			QName name = null;
			try {
				name = reader.getName();
			} catch (Exception e) {
				//e.printStackTrace();
			}
			
			if (eventType == XMLStreamReader.START_ELEMENT &&
					XML_PHOTO.equals(name.toString())) {
				String id = reader.getAttributeValue(null, "id");
				String caption = reader.getAttributeValue(null, "title");
				String smallUrl = reader.getAttributeValue(null, EXTRA_SMALL_URL);

				// create now GalleryItem
				GalleryItem item = new GalleryItem();
				item.setId(id);
				item.setCaption(caption);
				item.setUrl(smallUrl);
				// add that to items Array
				items.add(item);
			}

			// get the next element
			eventType = reader.next();
		}
	}
}
