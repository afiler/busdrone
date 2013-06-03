package com.busdrone;

import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;



public class WSFerryFetcher extends Fetcher {
	public static String endpointUrl = "http://www.wsdot.com/ferries/vesselwatch/Vessels.ashx";
	public static String dataProvider = "com.wsdot.vesselwatch";
	public static String operator = "com.wsdot";
	public static String vehicleType = "ferry";
	public static String color = "#b2017359"; //"rgba(1,115,89,0.7)";
	
	public static SimpleDateFormat wsfDatetimeFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
	public static SimpleDateFormat vesselDatetimeFormat = new SimpleDateFormat("M/d   H:m");

	Gson gson = new Gson();
	
	public WSFerryFetcher(ReportServer s) {
		super();
		server = s;
		sleepSecs = 10;
	}
	
	@Override
	public void runOnce() throws Exception {
		URLConnection connection = new URL(endpointUrl).openConnection();
		JsonParser parser = new JsonParser();
		
		JsonObject jsonResponse = parser.parse(new InputStreamReader(connection.getInputStream()))
				.getAsJsonObject();
		
		long mainTimestamp = wsfDatetimeFormat.parse(jsonResponse.get("timestamp").getAsString()).getTime();
		
		for (JsonElement element : jsonResponse.getAsJsonArray("vessellist")) {
			if (!element.isJsonObject()) continue;
			
			try {
				JsonObject o = element.getAsJsonObject();
				VehicleReport report = new VehicleReport();
				
				report.dataProvider = dataProvider;
				report.operator = operator;
				report.vehicleType = vehicleType;
				report.color = color;
				
				report.inService = o.get("inservice").getAsString().equals("True");
				report.coach = o.get("vesselID").getAsString();
				report.vehicleId = "40_"+report.coach;
				report.name = o.get("name").getAsString();
				report.route = o.get("route").getAsString();
				report.prevStop = o.get("lastdock").getAsString();
				report.nextStop = o.get("aterm").getAsString();
				report.destination = o.get("aterm").getAsString();
				report.lat = o.get("lat").getAsDouble();
				report.lon = o.get("lon").getAsDouble();
				report.speedMph = o.get("speed").getAsInt();
				report.heading = o.get("head").getAsInt();
				report.timestamp = parseVesselDatetime(o.get("datetime").getAsString());
				report.age = mainTimestamp - report.timestamp;
				
				syncAndSendReport(report);
				
			} catch (Exception e) {
				
			}
			
		}

	}
	
	@SuppressWarnings("deprecation")
	public long parseVesselDatetime(String s) {
		Date now = new Date();
		try {
			Date d = vesselDatetimeFormat.parse(s);
			int year = now.getYear();
			if (d.getMonth() == 12 && d.getDay() == 31 && now.getMonth() == 1)
				year--;
			d.setYear(year);
			return d.getTime();
		} catch (ParseException e) {
			return 0;
		}
	}
}
