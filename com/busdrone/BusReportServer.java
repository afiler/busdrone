package com.busdrone;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import com.busdrone.NextBusFetcher;
import com.busdrone.BusViewFetcher;

public class BusReportServer extends WebSocketServer {
	public JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");
	
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
		
		new NextBusFetcher(s).start();
		new BusViewFetcher(s).start();
		new WSFerryFetcher(s).start();
		new OBAFetcher(s).start();
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
		//this.sendToAll( message );
		//System.out.println( conn + ": " + message );
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
				conn.send(db.get("nextbus"));
				conn.send(db.get("wsferry"));
				conn.send(db.get("com.busdrone.reports.onebusaway"));
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
