package com.busdrone;

public abstract class Fetcher extends Thread {
	int sleepSecs = 0;
	BusReportServer server;

	@Override
	public void run() {
		while (true) {
			try {
				this.runOnce(server.eventStore);
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					Thread.sleep(this.sleepSecs * 1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public abstract void runOnce(EventStore eventStore) throws Exception;
}