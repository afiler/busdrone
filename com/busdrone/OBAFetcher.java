package com.busdrone;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import com.cedarsoftware.util.io.JsonWriter;
import com.google.gson.Gson;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;

import redis.clients.jedis.Jedis;

public class OBAFetcher extends Fetcher {
	public static String dataProvider = "com.onebusaway";
	
	public static String endpointUrlFmt = "http://api.onebusaway.org/api/where/vehicles-for-agency/%s.xml?key=TEST%s";
	//public static String[] agencyIds = {"1", "3", "19", "KMD", "40", "35", "23", "sch", "29"};
	public static String[] agencyIds = {"1", "3", "19"}; //{"3", "40", "29"};
	
	public Hashtable<String, BusReport> busReports = new Hashtable<String, BusReport>();
	
	public Hashtable<String, String> tripIdsRoutes = new Hashtable<String, String>();
	public Hashtable<String, String> tripIdsRouteIds = new Hashtable<String, String>();
	public Hashtable<String, String> routeIdsRoutes = new Hashtable<String, String>();
	public Hashtable<String, String> tripIdsDestinations = new Hashtable<String, String>();
	
	public int runCount = 0;
	public int refreshReferenceInterval = 6;

	Gson gson = new Gson();
	
	public OBAFetcher(BusReportServer s) {
		super();
		server = s;
		sleepSecs = 10; // XXX XXXXXXXX
	}
	
	@Override
	public void runOnce(HashMap<String,Event> eventStore) throws Exception {
		int updated=0;
		ArrayList<BusReport> reports = new ArrayList<BusReport>();
		Builder parser = new Builder();
		String includeReferences = runCount == 0 ? "" : "&includeReferences=false";
		
		for(String agencyID : agencyIds) {
			String endpointUrl = String.format(endpointUrlFmt, agencyID, includeReferences);
			Document doc = parser.build(endpointUrl);
			
			//long now = System.currentTimeMillis();
			long reportTimestamp = Long.parseLong(((Element)(doc.query("/response/currentTime").get(0))).getValue());

			
			Nodes routes = doc.query("//response/data/references/routes/route");
			for(int i=0; i<routes.size(); i++) {
				try {
					Element route = (Element)routes.get(i);
					routeIdsRoutes.put(
							route.query("id").get(0).getValue(),
							route.query("shortName").get(0).getValue());
				} catch (IndexOutOfBoundsException e) {}
			}
			
		    Nodes trips = doc.query("//response/data/references/trips/trip");
		    for(int i=0; i<trips.size(); i++) {
				try {
					Element trip = (Element)trips.get(i);
					tripIdsRouteIds.put(
							trip.query("id").get(0).getValue(),
							trip.query("routeId").get(0).getValue());
					tripIdsDestinations.put(
							trip.query("id").get(0).getValue(),
							trip.query("tripHeadsign").get(0).getValue());
				} catch (IndexOutOfBoundsException e) {}
		    }
			
			Nodes vehicleStatuses = doc.query("//response/data/list/vehicleStatus");
			for(int i=0; i<vehicleStatuses.size(); i++) {
				try {
					BusReport report = new BusReport();
					Element vehicleStatus = (Element)vehicleStatuses.get(i);
					//System.out.println(vehicleStatus.getValue());
					report.vehicleType = "bus";
					report.color = "#b27094ff";
					report.dataProvider = dataProvider;
					report.vehicleId = vehicleStatus.query("vehicleId").get(0).getValue();
					report.tripId = vehicleStatus.query("tripId").get(0).getValue();
					report.routeId = tripIdsRouteIds.get(report.tripId)+"";
					report.route = routeIdsRoutes.get(report.routeId)+"";
					report.destination = tripIdsDestinations.get(report.tripId)+"";
					report.lat = Double.parseDouble(vehicleStatus.query("location/lat").get(0).getValue());
					report.lon = Double.parseDouble(vehicleStatus.query("location/lon").get(0).getValue());
					report.heading = Double.parseDouble(vehicleStatus.query("tripStatus/orientation").get(0).getValue());
					report.timestamp = Long.parseLong(vehicleStatus.query("lastUpdateTime").get(0).getValue());
					report.age = reportTimestamp - report.timestamp;
					
					report.inService = true;

					//if (runCount == 0 || !report.equals(busReports.get(report.vehicleId))) {
					//	reports.add(report.cleanup()); updated++;
					//}
					
					//busReports.put(report.vehicleId, report);
					server.sendToAll(report.syncAndDump(eventStore));
					
				} catch (Exception e) {
					//System.out.println(vehicleStatuses.get(i));
					//e.printStackTrace();
				}

			}
		}
		
		//String json = gson.toJson(reports.toArray());
		//server.sendToAll(json);
		
		//if (runCount == 0)
		//	db.set("com.busdrone.reports.onebusaway", JsonWriter.objectToJson(reports.toArray()));

		//if (++runCount >= refreshReferenceInterval) runCount = 0;
		
		//System.out.println("OBAFetcher complete. "+updated+" updated");
	}
}
