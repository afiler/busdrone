package com.busdrone;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;

import redis.clients.jedis.Jedis;

//import COM.NextBus.HttpMapClient.Response;

import COM.NextBus.HttpMapClient.Response;
import COM.NextBus.HttpMapClient.ResponseComponent;
import COM.NextBus.HttpMapClient.Update;

import com.cedarsoftware.util.io.JsonWriter;

import com.busdrone.Fetcher;

public class NextBusFetcher extends Fetcher {
	public static String endpointUrl = "http://www.nextbus.com/service/map/main/update?v=6.0&c=a%3Dseattle-sc%26t%3D0%26nr%3Dsouthlakeunion";
	
	public NextBusFetcher(BusReportServer s) {
		super();
		server = s;
	}
	
	@Override
	public void runOnce(Jedis db) throws Exception {
		URLConnection connection = new URL(endpointUrl).openConnection();
		InputStream response = connection.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(response);
		Object o = ois.readObject();

		if (o instanceof Response) {
			Response r = (Response)o;
			//server.sendToAll(JsonWriter.objectToJson(Collections.list(r.a()).toArray()));
			for(Object e : Collections.list(r.a())) {
				if (e instanceof ResponseComponent) {
					Object array = ((ResponseComponent)e).d();
					if (array instanceof Update) {
						Update u = (Update)array;
						String json = JsonWriter.objectToJson(u.b().toArray());
						server.sendToAll(json);
						db.set("nextbus", json);
					}
				}
			}

		}
		Thread.sleep(4000);
	}
}
