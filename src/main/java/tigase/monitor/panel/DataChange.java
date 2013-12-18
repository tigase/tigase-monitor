/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2013 "Tigase, Inc." <office@tigase.com>
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
 */
package tigase.monitor.panel;

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
					if (countDelta) {
						((TigaseMonitorLine) monitor).addValueDelta(ser_id, value);
					} else {
						((TigaseMonitorLine) monitor).addValue(ser_id, value);
					}
				}
			}
		}
	}

	@Override
	public void disconnected(String id) {
		if (monitor != null) {
			monitor.disconnected(id);
		}
	}
}
