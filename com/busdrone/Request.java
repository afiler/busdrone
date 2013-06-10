package com.busdrone;

import org.java_websocket.WebSocket;

public class Request {
	private String type;
	private String vehicle_uid;
	private String trip_id;
	private String trip_uid;
	private String provider;
	private String id;
	private WebSocket conn;
	
	public Request() {
	}
	
	public String getType() {
		return type;
	}
	
	public WebSocket getConn() {
		return conn;
	}
	
	public Request setConn(WebSocket theConn) {
		conn = theConn;
		return this;
	}
	
	public String getVehicleUid() {
		return vehicle_uid;
	}
	
		
	public String getTripUid() {
		return trip_uid;
	}
	
	public String getTripId() {
		if (trip_id != null) return trip_id;
		
		String[] parts = trip_uid.split("/");
		provider = parts[0];
		trip_id = parts[1];
		
		return trip_id;
	}

	public String getProvider() {
		if (provider != null) return provider;
		
		String[] parts = trip_uid.split("/");
		provider = parts[0];
		trip_id = parts[1];
		
		return provider;
	}

	public String getId() {
		if (id != null) return id;
		
		String[] parts = vehicle_uid.split("/");
		provider = parts[0];
		id = parts[1];
		
		return id;
	}

}
