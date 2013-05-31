package com.busdrone;

import java.util.ArrayList;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import redis.clients.jedis.Jedis;

import com.cedarsoftware.util.io.JsonWriter;

import com.busdrone.Fetcher;

public class NextBusFetcher extends Fetcher {
	public static String endpointUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=seattle-sc&r=southlakeunion&t=0";
	public static String typeId = "slu";
	
	public NextBusFetcher(BusReportServer s) {
		super();
		server = s;
		sleepSecs = 4;
	}

	@Override
	public void runOnce(Jedis db) throws Exception {
		ArrayList<BusReport> reports = new ArrayList<BusReport>();
		
		Builder parser = new Builder();
		Document doc = parser.build(endpointUrl);
		
		//long now = System.currentTimeMillis();
		long reportTimestamp = Long.parseLong(((Element)(doc.query("//lastTime").get(0))).getAttribute("time").getValue());
		
		Nodes vehicles = doc.query("//vehicle");
		
		for(int i=0; i<vehicles.size(); i++) {
			try {
				Element vehicle = (Element)vehicles.get(i);
				BusReport report = new BusReport();
				
				report.coach = vehicle.getAttribute("id").getValue();
				report.vehicleId = typeId+'_'+report.coach;
				report.destination = vehicle.getAttribute("dirTag").getValue();
				//report.route = vehicle.getAttribute("routeTag").getValue();
				report.route = "SLU";
				report.lat = Double.parseDouble(vehicle.getAttribute("lat").getValue());
				report.lon = Double.parseDouble(vehicle.getAttribute("lon").getValue());
				report.heading = Double.parseDouble(vehicle.getAttribute("heading").getValue());
				report.age = Long.parseLong(vehicle.getAttribute("secsSinceReport").getValue());
				report.timestamp = reportTimestamp - (report.age * 1000);
		        if (report.coach.equals("1")) report.color = "rgba(223,0,0,0.7)";
		        else if (report.coach.equals("2")) report.color = "rgba(223,127,0,0.7)";
		        else if (report.coach.equals("3")) report.color = "rgba(127,0,223,0.7)";

		        report.color = report.color + "";
		        report.route = report.route + "";
				
				reports.add(report);
				//busReports.put(report.vehicleId, report);
			} catch (Exception e) {
				
			}
		}

		String json = JsonWriter.objectToJson(reports.toArray());
		server.sendToAll(json);
	}
}
