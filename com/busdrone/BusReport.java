package com.busdrone;

public class BusReport {
	String vehicleId;
	String routeId;
	String route;
	String tripId;
	double lat;
	double lon;
	long timestamp;
	

	@Override public boolean equals(Object aThat) {
		if ( this == aThat ) return true;
		if ( !(aThat instanceof BusReport) ) return false;
		BusReport that = (BusReport)aThat;
		
		return (
			this.vehicleId == that.vehicleId &&
			this.routeId   == that.routeId &&
			this.route     == that.route &&
			this.tripId    == that.tripId &&
			Double.doubleToLongBits(this.lat) == Double.doubleToLongBits(that.lat) &&
			Double.doubleToLongBits(this.lon) == Double.doubleToLongBits(that.lon));
	}
}
