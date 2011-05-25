/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2008 "Artur Hefczyc" <artur.hefczyc@tigase.org>
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
 * $Rev: 6 $
 * Last modified by $Author: kobit $
 * $Date: 2009-09-29 13:37:21 +0100 (Tue, 29 Sep 2009) $
 */

package tigase.monitor.panel;

import tigase.stats.StatisticsProviderMBean;

/**
 * Created: Sep 10, 2009 10:37:40 AM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 6 $
 */
public class SMTrafficMonitor extends TigaseMonitorLine {

	@Override
	public String getYTitle() {
		return "Packets/sec";
	}

	@Override
	public String getTitle() {
		return "SM Traffic";
	}

	@Override
	public void update(String id, StatisticsProviderMBean servBean) {
		addValue(id, servBean.getSMPacketsNumberPerSec());
	}

	@Override
	public double getYAxisMax() {
		return 50;
	}

	@Override
	public boolean countTotals() {
		return true;
	}

	@Override
	public void connected(String id, StatisticsProviderMBean servBean) {
		float[] history = servBean.getSMPacketsPerSecHistory();
		if (history != null && history.length > 0) {
			loadHistory(id, history);
		}
		super.connected(id, servBean);
	}

}
