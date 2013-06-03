package com.busdrone;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.java_websocket.WebSocket;
import org.java_websocket.WebSocketImpl;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


import com.busdrone.NextBusFetcher;
import com.busdrone.BusViewFetcher;

public class ReportServer extends WebSocketServer {
	public HashMap<String,VehicleReport> reportStore = new HashMap<String,VehicleReport>();
	
	public static void main( String[] args ) throws InterruptedException , IOException {
		WebSocketImpl.DEBUG = false;
		int port = 28737;
		try {
			port = Integer.parseInt( args[ 0 ] );
		} catch ( Exception ex ) {
		}
		ReportServer s = new ReportServer( port );
		s.start();
		System.out.println( "Server started on port " + s.getPort() );
		
		new NextBusFetcher(s).start();
		new BusViewFetcher(s).start();
		new WSFerryFetcher(s).start();
		new OBAFetcher(s).start();
		new Reaper(s).start();
	}

	public void sendToAll( String text ) {
		if (text == null) return;
		//System.out.println(text);
		Collection<WebSocket> con = connections();
		synchronized ( con ) {
			for( WebSocket c : con ) {
				c.send( text );
			}
		}
	}
	
	public ReportServer( int port ) throws UnknownHostException {
		super( new InetSocketAddress( port ) );
	}

	public ReportServer( InetSocketAddress address ) {
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
		
		Event event = new Event("init");
		
		for (Map.Entry<String, VehicleReport> entry : reportStore.entrySet()) {
			event.addVehicle(entry.getValue());
		}
		
		String json = event.toJson();
		conn.send(json);
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
