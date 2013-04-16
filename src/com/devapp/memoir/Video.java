package com.devapp.memoir;

import android.content.Context;

public class Video {
	public int key;
	public String path;
	public String thumbnailPath;
	public long date;
	public boolean selected;
	public int length;
	
	public Video(int k, long d, String p, String tp, boolean s, int l) {
		this.key = k;
		this.path = p;
		this.thumbnailPath = tp;
		this.date = d;
		this.selected = s;
		this.length = l;
	}

	public Video(int k, long d, String p, boolean s, int l) {
		this.key = k;
		this.path = p;
		this.date = d;
		this.selected = s;
		this.length = l;
	}

}
