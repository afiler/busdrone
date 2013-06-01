package com.busdrone;

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
	
	public BusReport cleanup() {
		if (vehicleId == null) vehicleId = coach;
		if (coach == null) coach = vehicleId;
		if (uid == null) uid = dataProvider+'/'+vehicleId;
		if (speedMph == 0 && speedKmh != 0) speedMph = (int) (0.621371 * speedKmh);
		if (speedKmh == 0 && speedMph != 0) speedKmh = (int) (1.609344 * speedMph);
		//if (timestamp = java.lang.Long.MIN_VALUE) 
		
		
		return this;
	}

	@Override public boolean equals(Object aThat) {
		if ( this == aThat ) return true;
		if ( !(aThat instanceof BusReport) ) return false;
		BusReport that = (BusReport)aThat;
		
		return (
			this.vehicleId == that.vehicleId &&
			this.coach     == that.coach &&
			this.routeId   == that.routeId &&
			this.route     == that.route &&
			this.tripId    == that.tripId &&
			this.destination == that.destination &&
			Double.doubleToLongBits(this.lat) == Double.doubleToLongBits(that.lat) &&
			Double.doubleToLongBits(this.lon) == Double.doubleToLongBits(that.lon) &&
			Double.doubleToLongBits(this.heading) == Double.doubleToLongBits(that.heading));
	}
}
