package com.PhotoGallery;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class InstaFetchr {
	public static final String TAG = "InstaFetchr"; // for debug

//	https://api.instagram.com/oauth/authorize/?client_id=CLIENT-ID&redirect_uri=REDIRECT-URI&response_type=token
//	http://your-redirect-uri#access_token=ACCESS-TOKEN
		
	private static final String ENDPOINT = "https://api.instagram.com";
	public static final String CLIENT_ID = "77ed756da156470b97c9b00c198a0372";
	public static final String CLIENT_SECRET = "99023c6819c54060b047dd45a07c4c31";
	public static final String REDIRECT_URI = "http://localhost:1500/";
	private static final String SCOPES = "&scope=basic+public_content";
	private static final String METHOD_GET_RECENT = "/v1/tags/nofilter/media/recent";
	private static final String METHOD_GET_RECENT_COUNT = "&count=100";
	private static final String METHOD_GET_RECENT_MINID = "&min_tag_id=";
	private static final String METHOD_GET_RECENT_MAXID = "&max_tag_id=";	

	public static final String AUTH_URI = "https://api.instagram.com/oauth/authorize/?client_id=" + 
			CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&response_type=code" + SCOPES;

	public static final String AT_REQUEST = ENDPOINT + "/oauth/access_token";
	public static final String RECENT_URI = ENDPOINT + METHOD_GET_RECENT + "?access_token=";
	
	private static final Logger Log = Logger.getLogger(InstaFetchr.class.getName());
	// -----

	// param: String urlSpec
	// returning byte array, or null on fail
	public byte[] getUrlBytes(String urlSpec) throws IOException {
		URL url = new URL(urlSpec); // construct a url
		// open that url
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
//		connection.setRequestProperty("Host", "api.instagram.com");
//		connection.setRequestProperty("X-Target-URI", "https://api.instagram.com");
//		connection.setRequestProperty("Connection", "Keep-Alive");
//		connection.setRequestMethod("GET");

		// try block
		try {
			// river of bytes
			ByteArrayOutputStream out = new ByteArrayOutputStream();

			if (connection.getResponseCode() != 200)  // 200 is GOOD
			{
				InputStream err = connection.getErrorStream();
				String response="";    
				String line;
				BufferedReader br=new BufferedReader(new InputStreamReader(err));
				while ((line=br.readLine()) != null) {
					response+=line;
				}
				if (response.length() > 2)
					System.out.println(connection.getResponseMessage() + " ... Response: "+response);
			}
			
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
	
	// postATUrl
	// HttpURLConnection with POST data to get access_token from instagram
	String postATUrl(String urlSpec, String code) throws IOException {
		String ret = "";
        URL url = new URL(urlSpec);
        Map<String,Object> params = new LinkedHashMap<>();
        params.put("client_id", this.CLIENT_ID);
        params.put("client_secret", this.CLIENT_SECRET);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", this.REDIRECT_URI);
        params.put("code", code);        

        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String,Object> param : params.entrySet()) {
            if (postData.length() != 0) postData.append('&');
            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
            postData.append('=');
            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
        }
        byte[] postDataBytes = postData.toString().getBytes("UTF-8");

        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
        conn.setDoOutput(true);
        conn.getOutputStream().write(postDataBytes);

        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
        for ( int c = in.read(); c != -1; c = in.read() )
            ret += (char)c;
        return ret;
	}

	// return array of GalleryItems or empty items on fail
	public ArrayList<GalleryItem> fetchItems(String access_token) {
		// alloc items array
		ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();

		try {
			// form a uri
			URI uri = URI.create(RECENT_URI + access_token + 
					METHOD_GET_RECENT_COUNT + METHOD_GET_RECENT_MINID + METHOD_GET_RECENT_MAXID);
			String xmlString = getUrl(uri.toString());
			// some logging
			
			System.out.println(xmlString);
			
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

//			if (eventType == XMLStreamReader.START_ELEMENT &&
//					XML_PHOTO.equals(name.toString())) {
//				String id = reader.getAttributeValue(null, "id");
//				String caption = reader.getAttributeValue(null, "title");
//				String smallUrl = reader.getAttributeValue(null, EXTRA_SMALL_URL);
//
//				// create now GalleryItem
//				GalleryItem item = new GalleryItem();
//				item.setId(id);
//				item.setCaption(caption);
//				item.setUrl(smallUrl);
//				// add that to items Array
//				items.add(item);
//			}

			// get the next element
			eventType = reader.next();
		}
	}
}	
