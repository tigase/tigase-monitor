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

import java.util.LinkedHashMap;
import java.util.Map;

import tigase.stats.JavaJMXProxyOpt;

/**
 * @author Artur Hefczyc Created May 28, 2011
 */
public abstract class DataChange implements DataChangeListener {
	public static final Map<String, String> msgMapping =
			new LinkedHashMap<String, String>();

	static {
		msgMapping.put(CPU_USAGE, "CPU");
		msgMapping.put(HEAP_USAGE, "HEAP Memory");
		msgMapping.put(NONHEAP_USAGE, "Non-Heap Memory");
	}

	protected TigaseMonitor monitor = null;
	private String[] dataIds = null;

	public DataChange(TigaseMonitor monitor, String... dataIds) {
		this.monitor = monitor;
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
			monitor.connected(id, bean);
		}
	}

	public void disconnected(String id) {
		if (monitor != null) {
			monitor.disconnected(id);
		}
	}

	public void connectedDelta(String id, JavaJMXProxyOpt bean, Long[] history) {
		if (history != null && history.length > 0) {
			double[] long_hist = new double[history.length];
			for (int i = 0; i < history.length; i++) {
				long_hist[i] = history[i]/monitor.getServerUpdaterate();
			}
			((TigaseMonitorLine) monitor).loadHistory(id, long_hist, true);
		}
		monitor.connected(id, bean);
	}

	public void updateDelta(String id, long val) {
		double value = val/monitor.getUpdaterate();
		((TigaseMonitorLine) monitor).addValueDelta(id, value);
	}

}
