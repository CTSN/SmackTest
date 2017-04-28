package com.xmg.smacktest;

import android.app.Application;

public class AppGlobal extends Application{

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
}
