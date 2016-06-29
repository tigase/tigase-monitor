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


import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;

import tigase.monitor.conf.ChartConfig;
import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;
//import tigase.monitor.panel.ConnectionsDistribution;
import tigase.monitor.panel.DataChange;
import tigase.monitor.panel.TigaseMonitorLine;

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
	private float row3_height_factor = 0.40f;
	private float row3_width_factor = 0.2f;

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

		JPanel row1 = new JPanel(null);

		row1.setLayout(new BoxLayout(row1, BoxLayout.LINE_AXIS));
		row1.setBackground(Color.DARK_GRAY);
		liveView.add(row1);

		int width_distr = Math.round(config.getHeight() * row1_height_factor);
		int height = Math.round(config.getHeight() * row1_height_factor);
		Dimension dim = new Dimension(width_distr, height);

		ChartConfig conf = config.getChartConfig(1);

		TigaseMonitorLine cpu =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), conf.countPerSec(), conf.approximate(),
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(cpu, conf.countDelta(), true, conf.getSeries());
		MonitorMain.monitors.add(cpu);

		int width = Math.round((config.getWidth() - width_distr) * row1_width_factor - 10);

		height = Math.round(config.getHeight() * row1_height_factor);
		dim = new Dimension(width, height);
		cpu.getPanel().setPreferredSize(dim);
		row1.add(cpu.getPanel());

		conf = config.getChartConfig(2);
		TigaseMonitorLine mem =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), conf.countPerSec(), conf.approximate(),
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(mem, conf.countDelta(), true, conf.getSeries());

		MonitorMain.monitors.add(mem);
		dim = new Dimension(width, height);
		mem.getPanel().setPreferredSize(dim);
		row1.add(mem.getPanel());
		width = Math.round(config.getWidth() * row2_width_factor - 5);
		height = Math.round(config.getHeight() * row2_height_factor);

		JPanel row2 = new JPanel(null);

		row2.setLayout(new BoxLayout(row2, BoxLayout.LINE_AXIS));
		row2.setBackground(Color.DARK_GRAY);
		liveView.add(row2);

		conf = config.getChartConfig(3);
		TigaseMonitorLine conns =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), conf.countPerSec(), conf.approximate(),
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(conns, conf.countDelta(), true, conf.getSeries());

		MonitorMain.monitors.add(conns);
		dim = new Dimension(width, height);
		conns.getPanel().setPreferredSize(dim);
		row2.add(conns.getPanel());

		conf = config.getChartConfig(4);
		TigaseMonitorLine sm =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), conf.countPerSec(), conf.approximate(),
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(sm, conf.countDelta(), true, conf.getSeries());

		MonitorMain.monitors.add(sm);
		dim = new Dimension(width, height);
		sm.getPanel().setPreferredSize(dim);
		row2.add(sm.getPanel());

		conf = config.getChartConfig(5);
		TigaseMonitorLine cl =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), conf.countPerSec(), conf.approximate(),
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(cl, conf.countDelta(), true, conf.getSeries());

		MonitorMain.monitors.add(cl);
		dim = new Dimension(width, height);
		cl.getPanel().setPreferredSize(dim);
		row2.add(cl.getPanel());

		JPanel row3 = new JPanel(null);

		row3.setLayout(new BoxLayout(row3, BoxLayout.LINE_AXIS));
		row3.setBackground(Color.DARK_GRAY);
		liveView.add(row3);
		width = Math.round(config.getWidth() * row3_width_factor - 5);
		height = Math.round(config.getHeight() * row3_height_factor - 100);

		List<NodeConfig> nodeConfigs = config.getNodeConfigs();

		for (NodeConfig nodeConfig : nodeConfigs) {
			// distr.setValue(nodeConfig.getDescription(), 0);
			// distr.setColor(nodeConfig.getDescription(), nodeConfig.getColor());
			cpu.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			mem.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			sm.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			cl.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			conns.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
		}

		return liveView;
	}

}
