package com.busdrone;

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
	boolean inService; // XXX
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
	
	public String syncAndDump(EventStore eventStore) {		
		cleanup();
		String key = "com.busdrone.reports/"+uid;
		
		BusReport oldBus = (BusReport)eventStore.get(key);
		
		if (oldBus == null || !oldBus.equals(this)) {
			eventStore.put(key, this);
			return this.toEventJson();
		} else {
			return null;
		}
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
