package com.busdrone;

import its.app.busview.BusReport;
import its.app.busview.BusReportSet;

import java.io.InputStream;
import java.io.ObjectInputStream;
//import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;
import java.net.URL;
import java.util.Vector;

import com.cedarsoftware.util.io.JsonWriter;

public class BusView {
	public static String endpointUrl = "http://trolley.its.washington.edu/applet/AvlServer";
	
	public static void main(String[] args) {
		try {
			URLConnection connection = new URL(endpointUrl).openConnection();
			InputStream response = connection.getInputStream();
			//FileInputStream fis = new FileInputStream("AvlServer.2.dump");
			//ObjectInputStream ois = new ObjectInputStream(fis);
			ObjectInputStream ois = new ObjectInputStream(response);

			while(true) {
				read(ois);
			}

			//ois.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void read(ObjectInputStream ois) {
		Object o = null;
		
		try {
			o = ois.readObject();
			if ((o instanceof String)) {
				System.out.println("String " + (String) o);
			} else if ((o instanceof BusReportSet)) {
				BusReportSet set = (BusReportSet) o;
				Vector v = set.array();
				if (v != null) {
					System.out.println("Got BusReportSet: "+JsonWriter.objectToJson(v.toArray()));
				} else {
					System.out.println("Got empty BusReportSet.");
				}
			} else {
				System.out.println(o);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	

	}
}