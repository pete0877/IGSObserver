package com.gobaduchi.igsobserver.util;

import android.util.Log;

public class LogProxy {

	private String tag;
	
	public LogProxy(String tag) {
		this.tag = tag;
	}
	
	public void error(String msg) {
		Log.e(tag, msg);
	}

	public void error(String msg, Exception error) {
		Log.e(tag, msg, error);
	}

	
	public void warn(String msg) {
		Log.w(tag, msg);
	}
	
	public void debug(String msg) {
		Log.d(tag, msg);
	}
	
	
}
