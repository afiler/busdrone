package com.busdrone;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import its.app.busview.BusReport;
import its.app.busview.BusReportSet;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URLConnection;
import java.net.URL;
import java.util.Vector;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.cedarsoftware.util.io.JsonWriter;

public class BusReportServer extends WebSocketServer {
	public static String endpointUrl = "http://trolley.its.washington.edu/applet/AvlServer";
	public JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
	
	static final String WGS84_PARAM = "+title=long/lat:WGS84 +proj=longlat +datum=WGS84 +units=degrees";
	static final String WA_N_PARAM = "+proj=lcc +lat_1=48.73333333333333 +lat_2=47.5 +lat_0=47 +lon_0=-120.8333333333333 +x_0=500000.0001016001 +y_0=0 +ellps=GRS80 +datum=NAD83 +to_meter=0.3048006096012192 +no_defs";
	private static final CoordinateTransformFactory ctFactory = new CoordinateTransformFactory();
	private static final CRSFactory crsFactory = new CRSFactory();
	private static final CoordinateReferenceSystem WGS84 = crsFactory.createFromParameters("WGS84", WGS84_PARAM);
	private static final CoordinateReferenceSystem WA_N = crsFactory.createFromParameters("WA_N", WA_N_PARAM);
	private static final CoordinateTransform trans = ctFactory.createTransform(WA_N, WGS84);

	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = false;
		int port = 28737;
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		BusReportServer s = new BusReportServer( port );
		s.start();
		System.out.println( "Server started on port " + s.getPort() );

		URLConnection connection = new URL(endpointUrl).openConnection();
		InputStream response = connection.getInputStream();
		ObjectInputStream ois = new ObjectInputStream(response);
		
		Jedis db = new Jedis("localhost");
		
		ProjCoordinate pout = new ProjCoordinate();

		while(true) {
			try {
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
						s.sendToAll(JsonWriter.objectToJson(busReports.toArray()));
					}
				}
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		}
	}

	public void sendToAll( String text ) {
		//System.out.println(text);
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}
	
	public BusReportServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}

	public BusReportServer( InetSocketAddress address ) {
		super( address );
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		this.sendToAll( message );
		System.out.println( conn + ": " + message );
	}
	
	@Override
	public void onOpen(WebSocket conn, ClientHandshake handshake) {		
		System.out.println( "new connection: " + handshake.getResourceDescriptor() );
		/*for (String key : busReportDb.hkeys("buses")) {
			//String foo = busReportDb.get(key);
			//System.out.println(key + ": " + foo);
			//conn.send(busReportDb.get(key));
			System.out.println("Dumping stored set");
			synchronized (conn) {
				conn.send(busReportDb.get(key));
			}
		}*/

		Jedis db = jedisPool.getResource();
		try {	
			StringBuilder builder = new StringBuilder();
			builder.append("[");		
			for (String key : db.hkeys("buses")) {
				if (builder.length() > 1) builder.append(",");
				builder.append(db.get(key));
			}
			builder.append("]");
			synchronized (conn) {
				conn.send(builder.toString());
			}
		} finally {
			jedisPool.returnResource(db);
		}
	}

	@Override
	public void onClose(WebSocket conn, int code, String reason, boolean remote) {
		System.out.println( "disconnection: " + conn);
	}

	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}
}
