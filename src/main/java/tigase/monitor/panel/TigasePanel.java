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

import tigase.monitor.MonitorMain;
import tigase.monitor.conf.ChartConfig;
import tigase.monitor.conf.Configuration;

import javax.swing.*;
import java.awt.*;

/**
 * Created by wojtek on 19/07/16.
 */
public class TigasePanel
		extends JPanel {

	protected Configuration config = null;
	protected JFrame mainFrame = null;
	protected float row1_height_factor = 0.30f;
	protected float row1_width_factor = 0.5f;
	protected float row2_height_factor = 0.30f;
	protected float row2_width_factor = 0.33f;
	protected float row3_height_factor = 0.40f;
	protected float row3_width_factor = 0.2f;

	protected static JPanel addRow(JPanel panel) {
		JPanel row = new JPanel(null);

		row.setLayout(new BoxLayout(row, BoxLayout.LINE_AXIS));
		row.setBackground(Color.DARK_GRAY);
		panel.add(row);
		return row;
	}

	public TigasePanel(Configuration conf, JFrame mainFrame) {
		this.config = conf;
		this.mainFrame = mainFrame;

	}

	protected TigaseMonitorLine addChart(Configuration config, String xTitle, String yTitle, double yAxisMax,
										 int height, int width, JPanel row, String... dataIds) {

		TigaseMonitorLine monitor = new TigaseMonitorLine(xTitle, yTitle, yAxisMax, false, false, true,
														  config.getTimeline(), config.getUpdaterate(),
														  config.getServerUpdaterate());
		new DataChange(monitor, false, config.getLoadHistory(), dataIds);

		MonitorMain.addMonitor(monitor);

		monitor.getPanel().setPreferredSize(new Dimension(width, height));
		row.add(monitor.getPanel());
		return monitor;

	}

	protected TigaseMonitorLine addCustomChart(Configuration config, int height, int width, JPanel row, int panelId) {
		ChartConfig conf = config.getChartConfig(panelId);
		TigaseMonitorLine monitor = new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
														  conf.countTotals(), conf.countPerSec(), conf.approximate(),
														  config.getTimeline(), config.getUpdaterate(),
														  config.getServerUpdaterate());
		new DataChange(monitor, conf.countDelta(), true, conf.getSeries());

		MonitorMain.addMonitor(monitor);

		monitor.getPanel().setPreferredSize(new Dimension(width, height));
		row.add(monitor.getPanel());
		return monitor;
	}

}
