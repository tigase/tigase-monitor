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
 * $Rev: 4 $
 * Last modified by $Author: kobit $
 * $Date: 2009-09-23 19:38:59 +0100 (Wed, 23 Sep 2009) $
 */

package tigase.monitor.panel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;
import tigase.stats.JMXProxyListener;
import tigase.stats.StatisticsProviderMBean;

/**
 * Created: Sep 9, 2009 10:01:51 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 4 $
 */
public abstract class TigaseMonitor implements JMXProxyListener {

	private Map<String, Boolean> ids = new LinkedHashMap<String, Boolean>();

	@Override
	public void connected(String id, StatisticsProviderMBean servBean) {
		ids.put(id, Boolean.TRUE);
	}

	@Override
	public void disconnected(String id) {
		ids.put(id, Boolean.FALSE);
	}

	public boolean isReady(String id) {
		return ids.get(id) != null && ids.get(id);
	}

	public abstract void update(String id, StatisticsProviderMBean servBean);

	public abstract List<JFreeChart> getCharts();

	public abstract JPanel getPanel();

}
