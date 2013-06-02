package com.busdrone;

import java.util.HashMap;
import java.util.Map;

public class Reaper extends Fetcher {

	public Reaper(BusReportServer s) {
		super();
		server = s;
		sleepSecs = 60; // XXX XXXXXXXX
	}

	@Override
	public void runOnce(HashMap<String, BusReport> reportStore) throws Exception {
		for (Map.Entry<String, BusReport> entry : reportStore.entrySet()) {
			BusReport report = entry.getValue();
			if (report.isDeletable()) server.sendToAll(report.syncAndDump(reportStore));
		}
	}

}
