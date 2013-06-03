package com.busdrone;

import java.util.Map;

public class Reaper extends Fetcher {

	public Reaper(ReportServer s) {
		super();
		server = s;
		sleepSecs = 60; // XXX XXXXXXXX
	}

	@Override
	public void runOnce() throws Exception {
		for (Map.Entry<String, VehicleReport> entry : server.reportStore.entrySet()) {
			VehicleReport report = entry.getValue();
			if (report.isDeletable()) this.syncAndSendReport(report);
		}
	}

}
