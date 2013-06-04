package com.busdrone;

import java.util.ArrayList;

import com.google.gson.Gson;

public class Event {
	public String type;
	public ArrayList<VehicleReport> vehicles;
	public VehicleReport vehicle;
	public String vehicle_uid;
	public String polyline;
	
	transient Gson gson = new Gson();

	public Event(String t) {
		type = t;
	}
	
	public String toJson() {
		return gson.toJson(this);
	}
	
	public void addVehicle(VehicleReport vehicleReport) {
		if (vehicles == null) vehicles = new ArrayList<VehicleReport>();
		vehicles.add(vehicleReport);
	}

}
