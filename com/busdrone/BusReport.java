package com.busdrone;

import java.util.HashMap;

import redis.clients.jedis.Jedis;

public class BusReport {
	String uid;
	String dataProvider;
	String operator;
	String vehicleType;
	String vehicleId;
	String prevStop;
	String nextStop;
	String coach;
	String name;
	String routeId;
	String route;
	String tripId;
	String destination;
	String color;
	int speedMph;
	int speedKmh;
	double lat;
	double lon;
	double heading;
	boolean inService = true; // XXX
	long timestamp = java.lang.Long.MIN_VALUE;
	long age = java.lang.Long.MIN_VALUE;
	
	transient Jedis db;
	
	public void cleanup() {
		if (vehicleId == null) vehicleId = coach;
		if (coach == null) coach = vehicleId;
		if (uid == null) uid = dataProvider+'/'+vehicleId;
		if (speedMph == 0 && speedKmh != 0) speedMph = (int) (0.621371 * speedKmh);
		if (speedKmh == 0 && speedMph != 0) speedKmh = (int) (1.609344 * speedMph);
		//if (timestamp = java.lang.Long.MIN_VALUE) 
	}

	public Event toEvent() {
		cleanup();
		Event event = new Event("update_vehicle");
		event.vehicle = this;
		return event;
	}
	
	public String toEventJson() {
		return toEvent().toJson();
	}
	
	public boolean isDeletable() {
		//return Math.abs(lat) > 89 || Math.abs(lon) > 89 || !inService || age >= 1000*60*10;
		
		boolean retval = Math.abs(lat) > 89 || Math.abs(lon) > 179 || !inService || age >= 1000*60*10;
		
		/*if (retval) {
			System.out.println("["+uid+"] "+
					" Math.abs(lat) > 89:"+(Math.abs(lat) > 89)+
					" Math.abs(lon) > 179:"+(Math.abs(lon) > 179) +
					" !inService:"+!inService+
					" age >= 1000*60*10:"+(age >= 1000*60*10));
		}*/
		
		return retval;
	}
	
	public String syncAndDump(HashMap<String,BusReport> reportStore) {		
		cleanup();

		String key = "com.busdrone.reports/"+uid;
		
		BusReport oldBus = (BusReport)reportStore.get(key);
		
		if (isDeletable()) {
			if (oldBus != null) {
				Event event = new Event("remove_vehicle");
				event.uid = uid;
				reportStore.remove(key);
				String json = event.toJson();
				System.out.println(json);
				return json;
			} else {
				return null;
			}
		}
				
		if (oldBus == null || !oldBus.equals(this)) {
			reportStore.put(key, this);
			return this.toEventJson();
		}

		return null;
	}
	
	@Override public boolean equals(Object aThat) {		
		if ( this == aThat ) return true;
		if ( !(aThat instanceof BusReport) ) return false;
		BusReport that = (BusReport)aThat;
		
		//System.out.println("equals? "+this.uid+":["+this.lat+","+this.lon+"] "+that.uid+": ["+that.lat+","+that.lon+"]");
		
		boolean retValx = (
			this.vehicleId == that.vehicleId &&
			this.coach     == that.coach &&
			this.routeId   == that.routeId &&
			this.route     == that.route &&
			this.tripId    == that.tripId &&
			this.destination == that.destination &&
			(Math.abs(this.lat-that.lat) < 0.00001) &&
			(Math.abs(this.lon-that.lon) < 0.00001) &&
			(Math.abs(this.heading-that.heading) < 1));

		boolean retVal = (
				(Math.abs(this.lat-that.lat) < 0.00001) &&
				(Math.abs(this.lon-that.lon) < 0.00001) &&
				(Math.abs(this.heading-that.heading) < 1));
			
		
		//System.out.println("=>"+retVal);
		return retVal;
	}
}
