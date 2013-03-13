package com.devapp.memoir;

public class Video {
	public int key;
	public String path;
	public long date;
	public boolean selected;
	public int length;
	
	public Video(int k, long d, String p, boolean s, int l) {
		this.key = k;
		this.path = p;
		this.date = d;
		this.selected = s;
		this.length = l;
	}

}
