package com.busdrone;

public abstract class Fetcher extends Thread {
	public int sleepSecs = 0;
	public ReportServer server;

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
		syncAndSendReport(report, false);
	}
	
	public void syncAndSendReport(VehicleReport report, boolean force) {		
		report.cleanup();

		//String key = "com.busdrone.reports/"+report.uid;
		String key = report.uid;
		
		VehicleReport oldBus = (VehicleReport)server.reportStore.get(key);
		
		if (report.isDeletable()) {
			if (oldBus != null) {
				Event event = new Event("remove_vehicle");
				event.vehicle_uid = report.uid;
				server.reportStore.remove(key);
				String json = event.toJson();
				server.sendToAll(json);
			}
			return;
		}
				
		if (force || oldBus == null || !oldBus.equals(report)) {
			server.reportStore.put(key, report);
			//server.sendToAll(report.toEventJson());
			String json = report.toEventJson();
			//System.out.println(json);
			server.sendToAll(json);
		}
	}
	
	public abstract void runOnce() throws Exception;
}