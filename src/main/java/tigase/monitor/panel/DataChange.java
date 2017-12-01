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
package tigase.monitor.panel;

import tigase.stats.JavaJMXProxyOpt;
import tigase.util.DataTypes;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

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
	protected List<String> dataIds = new CopyOnWriteArrayList<>();
	protected boolean countDelta = false;
	protected boolean loadHistory = true;

	public DataChange(TigaseMonitor monitor, boolean countDelta, boolean loadHistory,
			String... dataIds) {
		this.monitor = monitor;
		this.countDelta = countDelta;
		this.loadHistory = loadHistory;
		if (this.monitor != null) {
			this.monitor.setDataChangeListener(this);
		}
		if (dataIds != null) {
			this.dataIds.addAll(Arrays.asList(dataIds));
		}
	}

	public String[] getDataIds() {
		String[] array = new String[ dataIds.size() ];
		dataIds.toArray(array);
		return array;
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
