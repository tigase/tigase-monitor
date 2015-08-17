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

import static tigase.monitor.panel.DataChangeListener.BOSH_CONNECTIONS;
import static tigase.monitor.panel.DataChangeListener.C2S_CONNECTIONS;
import static tigase.monitor.panel.DataChangeListener.CL_TRAFFIC_R;
import static tigase.monitor.panel.DataChangeListener.CL_TRAFFIC_S;
import static tigase.monitor.panel.DataChangeListener.CPU_USAGE;
import static tigase.monitor.panel.DataChangeListener.HEAP_USAGE;
import static tigase.monitor.panel.DataChangeListener.NONHEAP_USAGE;
import static tigase.monitor.panel.DataChangeListener.S2S_CONNECTIONS;
import static tigase.monitor.panel.DataChangeListener.SM_TRAFFIC_R;
import static tigase.monitor.panel.DataChangeListener.SM_TRAFFIC_S;

import java.awt.Color;
import java.awt.Dimension;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;
import tigase.monitor.panel.ConnectionsDistribution;
import tigase.monitor.panel.DataChange;

import static tigase.monitor.panel.DataChangeListener.*;

import tigase.monitor.panel.TigaseMonitorLine;
import tigase.monitor.panel.TigaseTextMonitor;
import tigase.stats.JavaJMXProxyOpt;

/**
 * Created: Sep 23, 2009 2:24:40 PM
 * 
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 4 $
 */
public class ClientModuleImpl {

	private Configuration config = null;
	private MonitorMain mainFrame = null;
	private JMenu menu = null;
	private Map<String, JPanel> panels = new LinkedHashMap<String, JPanel>();
	private float row1_height_factor = 0.30f;
	private float row1_width_factor = 0.5f;
	private float row2_height_factor = 0.30f;
	private float row2_width_factor = 0.33f;
	private float row3_height_factor = 0.40f;
	private float row3_width_factor = 0.2f;

	/**
	 * Constructs ...
	 * 
	 */
	public ClientModuleImpl() {
	}

	/**
	 * Method description
	 * 
	 * 
	 * @param menuBar
	 * 
	 * @return
	 */
	public JMenu getJMenu(JMenuBar menuBar) {
		return menu;
	}

	/**
	 * Method description
	 * 
	 * 
	 * @return
	 */
	public Map<String, JPanel> getJPanels() {
		return panels;
	}

	/**
	 * Method description
	 * 
	 * 
	 * @return
	 */
	public Dimension getPreferredSize() {
		return new Dimension(config.getWidth(), config.getHeight());
	}

	/**
	 * Method description
	 * 
	 * 
	 * @param configFile
	 * @param mainFrame
	 */
	public void init(Configuration conf, MonitorMain mainFrame) {
		this.config = conf;
		this.mainFrame = mainFrame;

		JPanel panel = createPanel(config);
		panels.put("Live View", panel);
		menu = createMenu();

	}

	private JMenu createMenu() {
		JMenu monitorMenu = new JMenu("Moitor", true);

		monitorMenu.setMnemonic('M');

		JMenuItem item = null;

		item = new JMenuItem("Export to PNG...", 'p');
		item.setActionCommand(MonitorMain.EXPORT_TO_PNG_CMD);
		item.addActionListener(mainFrame);
		monitorMenu.add(item);
		item = new JMenuItem("Export to JPG...", 'p');
		item.setActionCommand(MonitorMain.EXPORT_TO_JPG_CMD);
		item.addActionListener(mainFrame);
		monitorMenu.add(item);
		monitorMenu.addSeparator();
		item = new JMenuItem("Auto export timer...", 'p');
		item.setActionCommand(MonitorMain.AUTO_EXPORT_TIMER_CMD);
		item.addActionListener(mainFrame);
		monitorMenu.add(item);

		return monitorMenu;
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

		ConnectionsDistribution distr = new ConnectionsDistribution();
		new DataChange(distr, false, false, C2S_CONNECTIONS) {

			public void update(String id, JavaJMXProxyOpt bean) {
				monitor.update(id, bean);
			}

		};

		MonitorMain.monitors.add(distr);

		int width_distr = Math.round(config.getHeight() * row1_height_factor);
		int height = Math.round(config.getHeight() * row1_height_factor);
		Dimension dim = new Dimension(width_distr, height);

		distr.getPanel().setPreferredSize(dim);
		row1.add(distr.getPanel());

		TigaseMonitorLine cpu =
				new TigaseMonitorLine("CPU Load", "CPU %", 100, false, false, true,
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(cpu, false, true, CPU_USAGE);

		MonitorMain.monitors.add(cpu);

		int width = Math.round((config.getWidth() - width_distr) * row1_width_factor - 10);

		height = Math.round(config.getHeight() * row1_height_factor);
		dim = new Dimension(width, height);
		cpu.getPanel().setPreferredSize(dim);
		row1.add(cpu.getPanel());

		TigaseMonitorLine mem =
				new TigaseMonitorLine("Memory Usage", "MEM %", 100, false, false, true,
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(mem, false, true, HEAP_USAGE, NONHEAP_USAGE);

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

		TigaseMonitorLine conns =
				new TigaseMonitorLine("Connections", "Connections per node", 10, true, false,
						false, config.getTimeline(), config.getUpdaterate(),
						config.getServerUpdaterate());
		new DataChange(conns, false, true, C2S_CONNECTIONS, WS2S_CONNECTIONS, BOSH_CONNECTIONS, S2S_CONNECTIONS);

		MonitorMain.monitors.add(conns);
		dim = new Dimension(width, height);
		conns.getPanel().setPreferredSize(dim);
		row2.add(conns.getPanel());

		TigaseMonitorLine sm =
				new TigaseMonitorLine("SM Traffic", "Packets/sec", 50, true, true, true,
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(sm, true, true, SM_TRAFFIC_R, SM_TRAFFIC_S);

		MonitorMain.monitors.add(sm);
		dim = new Dimension(width, height);
		sm.getPanel().setPreferredSize(dim);
		row2.add(sm.getPanel());

		TigaseMonitorLine cl =
				new TigaseMonitorLine("Cluster Traffic", "Packets/sec", 50, true, true, true,
						config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
		new DataChange(cl, true, true, CL_TRAFFIC_R, CL_TRAFFIC_S);

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

		int cnt = 0;
		List<NodeConfig> nodeConfigs = config.getNodeConfigs();

		for (NodeConfig nodeConfig : nodeConfigs) {
			distr.setValue(nodeConfig.getDescription(), 0);
			distr.setColor(nodeConfig.getDescription(), nodeConfig.getColor());
			cpu.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			mem.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			sm.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			cl.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			conns.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());

			if (cnt < 5) {
				TigaseTextMonitor textMonitor =
						new TigaseTextMonitor(nodeConfig.getDescription(), nodeConfigs,
								config.getUpdaterate(), config.getServerUpdaterate());
				new DataChange(textMonitor, false, false, textMonitor.getMetricsKeys()) {

					public void update(String id, JavaJMXProxyOpt bean) {
						monitor.update(id, bean);
					}

				};

				MonitorMain.monitors.add(textMonitor);
				dim = new Dimension(width, height);
				textMonitor.getPanel().setPreferredSize(dim);
				row3.add(textMonitor.getPanel());

				if (cnt < 4) {
					row3.add(Box.createRigidArea(new Dimension(5, 0)));
				}

				++cnt;
			}
		}

		return liveView;
	}

}
