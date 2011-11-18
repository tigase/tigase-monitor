/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2011 "Artur Hefczyc" <artur.hefczyc@tigase.org>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. Look for COPYING file in the top folder.
 * If not, see http://www.gnu.org/licenses/.
 *
 * $Rev: 2411 $
 * Last modified by $Author: kobit $
 * $Date: 2010-10-27 20:27:58 -0600 (Wed, 27 Oct 2010) $
 * 
 */
package tigase.monitor.panel;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import tigase.stats.JavaJMXProxyOpt;
import tigase.util.DataTypes;

/**
 * @author Artur Hefczyc Created May 28, 2011
 */
public class DataChange implements DataChangeListener {
	public static final Map<String, String> msgMapping =
			new LinkedHashMap<String, String>();

	static {
		msgMapping.put(CPU_USAGE, "CPU");
		msgMapping.put(HEAP_USAGE, "HEAP Memory");
		msgMapping.put(NONHEAP_USAGE, "Non-Heap Memory");
	}

	protected TigaseMonitor monitor = null;
	private String[] dataIds = null;
	private boolean countDelta = false;
	private boolean loadHistory = true;

	public DataChange(TigaseMonitor monitor, boolean countDelta, boolean loadHistory,
			String... dataIds) {
		this.monitor = monitor;
		this.countDelta = countDelta;
		this.loadHistory = loadHistory;
		if (this.monitor != null) {
			this.monitor.setDataChangeListener(this);
		}
		this.dataIds = dataIds;
	}

	public String[] getDataIds() {
		return dataIds;
	}

	public void connected(String id, JavaJMXProxyOpt bean) {
		if (monitor != null) {
			if (loadHistory) {
				int idx = 1;
				if (!monitor.historyLoaded(id + "-" + idx)) {
					// int delta = (countDelta ? monitor.getServerUpdaterate() : 1);
					for (String dataId : dataIds) {
						double[] history = null;
						switch (DataTypes.decodeTypeIdFromName(dataId)) {
							case 'L': {
								Long[] h = (Long[]) bean.getMetricHistory(dataId);
								// if (monitor.getTitle().equals("Presence traffic") &&
								// id.equals("green")) {
								// long last = h[0];
								// long[] testA = new long[h.length];
								// for(int i = 0; i<h.length; i++) {
								// testA[i] = h[i] - last;
								// last = h[i];
								// }
								// System.out.println("Presences delta: " +
								// Arrays.toString(testA));
								// System.out.println("Presences: " + Arrays.toString(h));
								// }
								if (h != null && h.length > 0) {
									history = new double[h.length];
									for (int i = 0; i < h.length; i++) {
										history[i] = h[i];
									}
								}
								break;
							}
							case 'F': {
								Float[] h = (Float[]) bean.getMetricHistory(dataId);
								if (h != null && h.length > 0) {
									history = new double[h.length];
									for (int i = 0; i < h.length; i++) {
										history[i] = h[i];
									}
								}
								break;
							}
							case 'I': {
								Integer[] h = (Integer[]) bean.getMetricHistory(dataId);
								if (h != null && h.length > 0) {
									history = new double[h.length];
									for (int i = 0; i < h.length; i++) {
										history[i] = h[i];
									}
								}
								break;
							}
							case 'D': {
								Double[] h = (Double[]) bean.getMetricHistory(dataId);
								if (h != null && h.length > 0) {
									history = new double[h.length];
									for (int i = 0; i < h.length; i++) {
										history[i] = h[i];
									}
								}
								break;
							}
						}
						if (history != null) {
							monitor.loadHistory(id + "-" + (idx++), history, countDelta);
						}
					}
				} else {
					// Do nothing for now
				}
			}
			monitor.connected(id, bean);
		}
	}

	public void update(String id, JavaJMXProxyOpt bean) {
		if (monitor != null) {
			int idx = 1;
			for (String dataId : dataIds) {
				Double value = null;
				switch (DataTypes.decodeTypeIdFromName(dataId)) {
					case 'L':
						value = ((Long) bean.getMetricData(dataId)).doubleValue();
						break;
					case 'F':
						value = ((Float) bean.getMetricData(dataId)).doubleValue();
						break;
					case 'I':
						value = ((Integer) bean.getMetricData(dataId)).doubleValue();
						break;
					case 'D':
						value = (Double) bean.getMetricData(dataId);
						break;
				}
				if (value != null) {
					String ser_id = id + "-" + (idx++);
					// if (monitor.getTitle().equals("Presence traffic") &&
					// id.equals("green")) {
					// System.out.println(monitor.getTitle() + ", " + ser_id + " value: "
					// + value);
					// }
					if (countDelta) {
						((TigaseMonitorLine) monitor).addValueDelta(ser_id, value);
					} else {
						((TigaseMonitorLine) monitor).addValue(ser_id, value);
					}
				}
			}
		}
	}

	public void disconnected(String id) {
		if (monitor != null) {
			monitor.disconnected(id);
		}
	}

	// public void connectedDelta(String id, JavaJMXProxyOpt bean, Long[] history)
	// {
	// if (history != null && history.length > 0) {
	// double[] long_hist = new double[history.length];
	// for (int i = 0; i < history.length; i++) {
	// long_hist[i] = history[i] / monitor.getServerUpdaterate();
	// }
	// ((TigaseMonitorLine) monitor).loadHistory(id, long_hist, true);
	// }
	// monitor.connected(id, bean);
	// }
	//
	// public void updateDelta(String id, long val) {
	// double value = val / monitor.getUpdaterate();
	// ((TigaseMonitorLine) monitor).addValueDelta(id, value);
	// }

}
