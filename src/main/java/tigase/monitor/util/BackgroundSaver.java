/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2017 "Tigase, Inc." <office@tigase.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 */

package tigase.monitor.util;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import tigase.monitor.MonitorMain;
import tigase.monitor.panel.TigaseMonitor;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by wojtek on 19/07/16.
 */
public class BackgroundSaver
		extends Thread {

	private long delay = 0;
	private File dir = null;
	private boolean exitOnComplete = false;
	private String ext = null;
	private long interval = 0;
	private MonitorMain monitorMain;
	private int repeat = 1;

	public BackgroundSaver(MonitorMain monitorMain, File dir, String ext, boolean exitOnComplete, long delay,
						   int repeat, long interval) {
		this.monitorMain = monitorMain;
		this.dir = dir;
		this.ext = ext;
		this.exitOnComplete = exitOnComplete;
		this.delay = delay;
		this.repeat = (repeat < 1) ? 1 : repeat;
		this.interval = interval;
	}

	@Override
	public void run() {
		if (delay > 0) {
			try {
				sleep(delay);
			} catch (Exception e) {
			}
		}

		if (!dir.exists()) {
			dir.mkdirs();
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss_");

		for (int j = 0; j < repeat; j++) {
			String datetime = sdf.format(new Date());

			for (TigaseMonitor monitor : MonitorMain.monitors) {
				int i = 0;
				int w = monitor.getPanel().getWidth();
				int h = monitor.getPanel().getHeight();
				List<JFreeChart> charts = monitor.getCharts();

				if (charts != null) {
					for (JFreeChart chart : charts) {
						File file =
								// new File(dir, datetime + (++i) + "_" +
								// chart.getTitle().getText() + ext);
								new File(dir, datetime + (++i) + "_" +
										chart.getTitle().getText().replaceAll("[^\\d\\w\\s]", "_") + ext);

						try {
							JFreeChart ch = (JFreeChart) chart.clone();

							if (ext == ".png") {
								ChartUtilities.saveChartAsPNG(file, ch, w, h);
							}

							if (ext == ".jpg") {
								ChartUtilities.saveChartAsJPEG(file, ch, w, h);
							}
						} catch (Exception e) {
							System.err.println("Can't save file: " + file);
						}
					}
				}
			}

			if ((interval > 0) && (j + 1 < repeat)) {
				try {
					sleep(interval);
				} catch (Exception e) {
				}
			}
		}

		if (exitOnComplete) {
			monitorMain.dispose();
			System.exit(0);
		}
	}
}
