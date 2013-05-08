package com.busdrone;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

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
	public JedisPool jedisPool;

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
		
		Jedis busReportDb = new Jedis("localhost");

		while(true) {
			try {
				Object o = ois.readObject();
				if ((o instanceof BusReportSet)) {
					BusReportSet set = (BusReportSet) o;
					Vector<BusReport> busReports = set.array();
					if (busReports != null) {
						//String json = JsonWriter.objectToJson(busReports.toArray());
						//s.sendToAll(json);
						s.sendToAll(JsonWriter.objectToJson(busReports.toArray()));
						for (BusReport busReport : busReports) {
							String json = JsonWriter.objectToJson(busReport);
							//s.sendToAll(json);
							String key = String.valueOf(busReport.coach);
							busReportDb.set(key, json);
							busReportDb.hset("buses", key, String.valueOf(busReport.timestamp));
						}
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
		jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
	}

	public BusReportServer( InetSocketAddress address ) {
		super( address );
		jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
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

		Jedis busReportDb = jedisPool.getResource();
		try {	
			StringBuilder builder = new StringBuilder();
			builder.append("[");		
			for (String key : busReportDb.hkeys("buses")) {
				if (builder.length() > 1) builder.append(",");
				builder.append(busReportDb.get(key));
			}
			builder.append("]");
			synchronized (conn) {
				conn.send(builder.toString());
			}
		} finally {
			jedisPool.returnResource(busReportDb);
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
