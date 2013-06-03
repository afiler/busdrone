package com.busdrone;

import java.util.Map;

public class Reaper extends Fetcher {

	public Reaper(BusReportServer s) {
		super();
		server = s;
		sleepSecs = 60; // XXX XXXXXXXX
	}

	@Override
	public void runOnce() throws Exception {
		for (Map.Entry<String, BusReport> entry : server.reportStore.entrySet()) {
			BusReport report = entry.getValue();
			if (report.isDeletable()) this.syncAndSendReport(report);
		}
	}

}
