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

import tigase.stats.JavaJMXProxyOpt;

/**
 * Created: Sep 9, 2009 10:01:51 PM
 * 
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 4 $
 */
public abstract class TigaseMonitor {

	private Map<String, Boolean> ids = new LinkedHashMap<String, Boolean>();
	private DataChangeListener dataListener = null;
	private int updaterate = 10;
	private int serverUpdaterate = 10;
	private String title = null;

	public TigaseMonitor(String title, int updaterate, int serverUpdaterate) {
		this.updaterate = updaterate;
		this.title = title;
		this.serverUpdaterate = serverUpdaterate;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void connected(String id, JavaJMXProxyOpt servBean) {
		ids.put(id, Boolean.TRUE);
	}

	public void disconnected(String id) {
		ids.put(id, Boolean.FALSE);
	}

	public boolean isReady(String id) {
		return ids.get(id) != null && ids.get(id);
	}

	public abstract List<JFreeChart> getCharts();

	public abstract JPanel getPanel();
	
	public void setDataChangeListener(DataChangeListener dataListener) {
		this.dataListener  = dataListener;
	}
	
	public DataChangeListener getDataChangeListener() {
		return dataListener;
	}

	public void setUpdate(int updaterate) {
		this.updaterate = updaterate;
	}

	public int getUpdaterate() {
		return updaterate;
	}

	public int getServerUpdaterate() {
		return serverUpdaterate;
	}

	/**
	 * @param id
	 * @param bean
	 */
	public void update(String id, JavaJMXProxyOpt bean) {
	}
}
