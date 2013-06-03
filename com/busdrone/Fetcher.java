package com.busdrone;

public abstract class Fetcher extends Thread {
	int sleepSecs = 0;
	ReportServer server;

	@Override
	public void run() {
		while (true) {
			try {
				this.runOnce();
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
	
	public void syncAndSendReport(VehicleReport report) {		
		report.cleanup();

		String key = "com.busdrone.reports/"+report.uid;
		
		VehicleReport oldBus = (VehicleReport)server.reportStore.get(key);
		
		if (report.isDeletable()) {
			if (oldBus != null) {
				Event event = new Event("remove_vehicle");
				event.uid = report.uid;
				server.reportStore.remove(key);
				String json = event.toJson();
				System.out.println(json);
				server.sendToAll(json);
			}
			return;
		}
				
		if (oldBus == null || !oldBus.equals(report)) {
			server.reportStore.put(key, report);
			//server.sendToAll(report.toEventJson());
			String json = report.toEventJson();
			//System.out.println(json);
			server.sendToAll(json);
		}
	}
	
	public abstract void runOnce() throws Exception;
}