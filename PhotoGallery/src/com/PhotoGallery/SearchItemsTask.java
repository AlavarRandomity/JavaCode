package com.PhotoGallery;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class SearchItemsTask implements Callable<ArrayList<GalleryItem>> {
	private String query = "";
	
	private String access_token = "";
	
	public String getQuery() {
		return query;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public SearchItemsTask(String access_token) {
		this.access_token = access_token;
	}
	
	// call() func
	//  it takes no params
	//  throws Exceptions
	// return:
	//   ArrayList<GalleryItem>
	@Override
	public ArrayList<GalleryItem> call() throws Exception {
		ArrayList<GalleryItem> ret = new ArrayList<GalleryItem>();
		ret = new FlickrFetchr().searchItems(query);
		return ret;
	}
}
