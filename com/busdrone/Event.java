package com.busdrone;

import java.util.ArrayList;

import com.google.gson.Gson;

public class Event {
	public String type;
	public ArrayList<BusReport> vehicles;
	public BusReport vehicle;
	public String uid;
	public String polyline;
	
	transient Gson gson = new Gson();

	public Event(String t) {
		type = t;
	}
	
	public String toJson() {
		return gson.toJson(this);
	}

}
