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

//~--- non-JDK imports --------------------------------------------------------

import tigase.stats.StatisticsProviderMBean;

//~--- JDK imports ------------------------------------------------------------

import java.awt.Color;
import java.awt.Paint;

//~--- classes ----------------------------------------------------------------

/**
 * Created: Sep 10, 2009 11:23:37 AM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 6 $
 */
public class ConnectionsMonitor extends TigaseMonitorLine {

	/**
	 * Method description
	 *
	 *
	 * @param key
	 */
	@Override
	public void addSeries(String key) {
		super.addSeries(key);
		super.addSeries(key + "-2");
	}

	/**
	 * Method description
	 *
	 *
	 * @param id
	 * @param servBean
	 */
	@Override
	public void connected(String id, StatisticsProviderMBean servBean) {
		int[] history = servBean.getConnectionsNumberHistory();

		if ((history != null) && (history.length > 0)) {
			loadHistory(id, history);
		}

		history = servBean.getServerConnectionsHistory();

		if ((history != null) && (history.length > 0)) {
			loadHistory(id + "-2", history);
		}

		super.connected(id, servBean);
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	@Override
	public boolean countTotals() {
		return true;
	}

	//~--- get methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	@Override
	public String getTitle() {
		return "Connections";
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	@Override
	public double getYAxisMax() {
		return 10;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	@Override
	public String getYTitle() {
		return "Connections per node";
	}

	//~--- set methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param key
	 * @param color
	 */
	@Override
	public void setColor(String key, Paint color) {
		super.setColor(key, color);
		super.setColor(key + "-2", ((Color) color).darker().darker());
	}

	//~--- methods --------------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param id
	 * @param servBean
	 */
	@Override
	public void update(String id, StatisticsProviderMBean servBean) {
		addValue(id, servBean.getConnectionsNumber());
		addValue(id + "-2", servBean.getServerConnections());
	}
}


//~ Formatted in Sun Code Convention


//~ Formatted by Jindent --- http://www.jindent.com
