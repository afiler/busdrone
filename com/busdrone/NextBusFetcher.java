package com.busdrone;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import com.google.gson.Gson;

import com.busdrone.Fetcher;

public class NextBusFetcher extends Fetcher {
	public static String operator = "metro.kingcounty.gov";
	public static String dataProvider = "com.nextbus";
	public static String vehicleType = "streetcar";
	public static String endpointUrl = "http://webservices.nextbus.com/service/publicXMLFeed?command=vehicleLocations&a=seattle-sc&r=southlakeunion&t=0";
	public static String typeId = "slu";
	Gson gson = new Gson();
	
	public NextBusFetcher(ReportServer s) {
		super();
		server = s;
		sleepSecs = 4;
	}

	@Override
	public void runOnce() throws Exception {
		//ArrayList<BusReport> reports = new ArrayList<BusReport>();
		
		Builder parser = new Builder();
		Document doc = parser.build(endpointUrl);
		
		//long now = System.currentTimeMillis();
		long reportTimestamp = Long.parseLong(((Element)(doc.query("//lastTime").get(0))).getAttribute("time").getValue());
		
		Nodes vehicles = doc.query("//vehicle");
		
		for(int i=0; i<vehicles.size(); i++) {
			try {
				Element vehicle = (Element)vehicles.get(i);
				VehicleReport report = new VehicleReport();
				
				report.operator = operator;
				report.dataProvider = dataProvider;
				report.vehicleType = vehicleType;
				report.coach = vehicle.getAttribute("id").getValue();
				report.vehicleId = typeId+'_'+report.coach;
				try {
					report.destination = vehicle.getAttribute("dirTag").getValue();
				} catch (Exception e) {
					report.destination = "?";
				}
				
				//report.route = vehicle.getAttribute("routeTag").getValue();
				report.route = "SLU";
				report.lat = Double.parseDouble(vehicle.getAttribute("lat").getValue());
				report.lon = Double.parseDouble(vehicle.getAttribute("lon").getValue());
				report.heading = Double.parseDouble(vehicle.getAttribute("heading").getValue());
				report.age = Long.parseLong(vehicle.getAttribute("secsSinceReport").getValue()) * 1000;
				report.timestamp = reportTimestamp - (report.age);
		        if (report.coach.equals("1")) report.color = "#b2df0000"; //"rgba(223,0,0,0.7)";
		        else if (report.coach.equals("2")) report.color = "#b2df7f00"; //"rgba(223,127,0,0.7)";
		        else if (report.coach.equals("3")) report.color = "#b27f00df"; //"rgba(127,0,223,0.7)";
		        
		        report.inService = true;

		        report.color = report.color + "";
		        report.route = report.route + "";
				
		        syncAndSendReport(report);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
