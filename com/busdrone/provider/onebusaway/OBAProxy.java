package com.busdrone.provider.onebusaway;

import nu.xom.Builder;
import nu.xom.Document;

import com.busdrone.Event;
import com.busdrone.Proxy;
import com.busdrone.Request;

public class OBAProxy extends Proxy {
	public static String tripUrlFmt = "http://api.onebusaway.org/api/where/trip-details/%s.xml?key=TEST";
	public static String shapeUrlFmt = "http://api.onebusaway.org/api/where/shape/%s.xml?key=TEST";
	Builder parser = new Builder();
	
	public OBAProxy() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void processRequest(Request req) {
		System.out.println("OBAProxy processing request: "+String.format(tripUrlFmt, req.getTripUid()));
		
		Event event = new Event("trip_polyline");
		event.trip_uid = req.getTripUid();
		
		String tripId = req.getTripId();
		if (!tripId.matches("^\\d+_\\d+$")) return;
		
		try {
			Document tripDoc = parser.build(String.format(tripUrlFmt, tripId));
			String shapeIdQuery = String.format("//response/data/references/trips/trip/id[text()='%s']", tripId);
			String shapeId = tripDoc.query(shapeIdQuery).get(0).getParent().query("shapeId").get(0).getValue();			
			Document shapeDoc = parser.build(String.format(shapeUrlFmt, shapeId));
			event.polyline = shapeDoc.query("/response/data/entry/points").get(0).getValue();
			req.getConn().send(event.toJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
