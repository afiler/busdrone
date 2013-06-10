package com.busdrone;

import java.util.concurrent.LinkedBlockingQueue;

public abstract class Proxy extends Thread {
	public ReportServer server;
	private LinkedBlockingQueue<Request> queue = new LinkedBlockingQueue<Request>();

	@Override
	public void run() {
		System.out.println("Proxy started");
		while (true) {
			try {
				this.processRequest(queue.take());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void submitRequest(Request req) {
		System.out.println("Submitting request");
		this.queue.add(req);
	}
	
	public abstract void processRequest(Request req);
}
