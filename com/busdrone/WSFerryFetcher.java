package com.busdrone;

import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;

import redis.clients.jedis.Jedis;

public class WSFerryFetcher extends Fetcher {
	public static String endpointUrl = "http://www.wsdot.com/ferries/vesselwatch/Vessels.ashx";
	
	public WSFerryFetcher(BusReportServer s) {
		super();
		server = s;
	}
	
	@Override
	public void runOnce(Jedis db) throws Exception {
		URLConnection connection = new URL(endpointUrl).openConnection();
		try (Scanner scanner = new Scanner(connection.getInputStream(),"UTF-8")) {
			String json = scanner.useDelimiter("\\A").next();
			server.sendToAll(json);
			db.set("wsferry", json);
		}

		Thread.sleep(10000);
	}
}
