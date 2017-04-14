package com.PhotoGallery;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class FetchItemsTask implements Callable<ArrayList<GalleryItem>> {
	private String access_token = "";
	
	public FetchItemsTask(String access_token) {
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
		ret = new FlickrFetchr().fetchItems();
//		if (access_token.length() > 1)
//			ret.addAll(new InstaFetchr().fetchItems(access_token));
		return ret;
	}
}
