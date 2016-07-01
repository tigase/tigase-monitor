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
package tigase.monitor;


import tigase.monitor.conf.ChartConfig;
import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;
import tigase.monitor.panel.DataChange;
import tigase.monitor.panel.TigaseMonitorLine;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//import tigase.monitor.panel.ConnectionsDistribution;

//~--- classes ----------------------------------------------------------------

/**
 * Created: Sep 23, 2009 2:24:40 PM
 * 
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 4 $
 */
public class CustomModuleImpl {

	private Configuration config = null;
	private JFrame mainFrame = null;
	private JMenu menu = null;
	private Map<String, JPanel> panels = new LinkedHashMap<String, JPanel>();
	private float row1_height_factor = 0.30f;
	private float row1_width_factor = 0.5f;
	private float row2_height_factor = 0.30f;
	private float row2_width_factor = 0.33f;

	public CustomModuleImpl() {
	}

	public JMenu getJMenu(JMenuBar menuBar) {
		return menu;
	}

	public Map<String, JPanel> getJPanels() {
		return panels;
	}

	public Dimension getPreferredSize() {
		return new Dimension(config.getWidth(), config.getHeight());
	}

	public void init(Configuration conf, JFrame mainFrame) {
		this.config = conf;
		this.mainFrame = mainFrame;

		JPanel panel = createPanel(config);

		panels.put("Live View", panel);

	}

	private JPanel createPanel(Configuration config) {
		JPanel liveView = new JPanel(null);

		liveView.setLayout(new BoxLayout(liveView, BoxLayout.PAGE_AXIS));
		liveView.setBackground(Color.DARK_GRAY);
		liveView.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JPanel row1 = addRow(liveView);

		int width = Math.round(config.getWidth() * row1_width_factor - 5);
		int height = Math.round(config.getHeight() * row1_height_factor);

		TigaseMonitorLine custom1 = addChart(config, height, width, row1, 1);

		TigaseMonitorLine custom2 = addChart(config, height, width, row1, 2);

		JPanel row2 = addRow(liveView);

		width = Math.round(config.getWidth() * row2_width_factor - 5);
		height = Math.round(config.getHeight() * row2_height_factor);

		TigaseMonitorLine custom3 = addChart(config, height, width, row2, 3);

		TigaseMonitorLine custom4 = addChart(config, height, width, row2, 4);

		TigaseMonitorLine custom5 = addChart(config, height, width, row2, 5);

		List<NodeConfig> nodeConfigs = config.getNodeConfigs();

		for (NodeConfig nodeConfig : nodeConfigs) {
			custom1.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			custom2.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			custom4.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			custom5.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			custom3.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
		}

		return liveView;
	}

	private JPanel addRow(JPanel liveView) {
		JPanel row2 = new JPanel(null);

		row2.setLayout(new BoxLayout(row2, BoxLayout.LINE_AXIS));
		row2.setBackground(Color.DARK_GRAY);
		liveView.add(row2);
		return row2;
	}

	private TigaseMonitorLine addChart(Configuration config, int height, int width, JPanel row2, int id) {
		ChartConfig conf;
		Dimension dim;
		conf = config.getChartConfig(id);
		TigaseMonitorLine custom4 =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), conf.countPerSec(), conf.approximate(),
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(custom4, conf.countDelta(), true, conf.getSeries());

		MonitorMain.monitors.add(custom4);
		dim = new Dimension(width, height);
		custom4.getPanel().setPreferredSize(dim);
		row2.add(custom4.getPanel());
		return custom4;
	}

}
