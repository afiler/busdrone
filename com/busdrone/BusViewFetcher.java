package com.busdrone;

import its.app.busview.BusReport;
import its.app.busview.BusReportSet;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.busdrone.Fetcher;
import com.cedarsoftware.util.io.JsonWriter;

public class BusViewFetcher extends Fetcher {
	public static String endpointUrl = "http://trolley.its.washington.edu/applet/AvlServer";
	public JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
	
	static final String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +datum=WGS84 +units=degrees";
	static final String WA_N_PARAM = "+proj=lcc +lat_1=48.73333333333333 +lat_2=47.5 +lat_0=47 +lon_0=-120.8333333333333 +x_0=500000.0001016001 +y_0=0 +ellps=GRS80 +datum=NAD83 +to_meter=0.3048006096012192 +no_defs";
	private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
	private static final CRSFactory crsFactory = new CRSFactory();
	private static final CoordinateReferenceSystem WGS84 = crsFactory.createFromParameters("WGS84", WGS84_PARAM);
	private static final CoordinateReferenceSystem WA_N = crsFactory.createFromParameters("WA_N", WA_N_PARAM);
	private static final CoordinateTransform trans = ctFactory.createTransform(WA_N, WGS84);
	
	ProjCoordinate pout = new ProjCoordinate();
	
	URLConnection connection;
	InputStream response;
	ObjectInputStream ois;

	public BusViewFetcher(BusReportServer s) {
		super();
		server = s;
	}
	
	public void runOnce(Jedis db) throws Exception {
		if (connection == null) {
			connection = new URL(endpointUrl).openConnection();
			response = connection.getInputStream();
			ois = new ObjectInputStream(response);
		}
		
		Object o = ois.readObject();
		if ((o instanceof BusReportSet)) {
			BusReportSet set = (BusReportSet) o;
			Vector<BusReport> busReports = set.array();
			if (busReports != null) {
				//String json = JsonWriter.objectToJson(busReports.toArray());
				//s.sendToAll(json);
				//s.sendToAll(JsonWriter.objectToJson(busReports.toArray()));
				for (BusReport busReport : busReports) {
					trans.transform(new ProjCoordinate(busReport.x, busReport.y), pout);
					busReport.lat = pout.y;
					busReport.lon = pout.x;
					
					String json = JsonWriter.objectToJson(busReport);
					//s.sendToAll(json);
					String key = String.valueOf(busReport.coach);
					db.set(key, json);
					db.hset("buses", key, String.valueOf(busReport.timestamp));
				}
				server.sendToAll(JsonWriter.objectToJson(busReports.toArray()));
			}
		}
	}

}
