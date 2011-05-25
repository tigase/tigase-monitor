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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import org.jfree.chart.JFreeChart;
import tigase.monitor.conf.NodeConfig;
import tigase.stats.StatisticsProviderMBean;

/**
 * Created: Sep 12, 2009 11:52:57 AM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 6 $
 */
public class TigaseTextMonitor extends TigaseMonitor {

	private String id = null;
	private TitledBorder title = null;
	JPopupMenu contextMenu = null;
	private JLabel cpu = null;
	private JLabel mem = null;
	private JLabel smPackets = null;
	private JLabel queues = null;
	private JLabel conns = null;
	private JLabel overflows = null;
	private JLabel clPackets = null;
	private JLabel clCache = null;
	private JTextArea details = null;
	private JPanel panel = null;
	
	public TigaseTextMonitor(String id, List<NodeConfig> nodeConfigs) {
		this.id = id;
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		title = BorderFactory.createTitledBorder(loweredetched, "initialization...",
				TitledBorder.LEFT, TitledBorder.CENTER);
		panel = new JPanel();
		panel.setBorder(title);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBackground(new Color(0.15f, 0.15f, 0.15f));
		contextMenu = new JPopupMenu("Nodes");
		contextMenu.setBackground(Color.GRAY);
		for (NodeConfig nodeConfig : nodeConfigs) {
			JMenuItem item = new JMenuItem(nodeConfig.toString());
			item.setForeground(nodeConfig.getColor());
			item.setBackground(Color.GRAY);
			item.setActionCommand(nodeConfig.getDescription());
			item.addActionListener(new PopupListener(nodeConfig));
			contextMenu.add(item);
			if (id.equals(nodeConfig.getDescription())) {
				title.setTitle(nodeConfig.toString());
				title.setTitleColor(nodeConfig.getColor());
			}
		}
		panel.setComponentPopupMenu(contextMenu);

		Color labelCol = Color.GRAY;
		JPanel mainStats = new JPanel(new GridLayout(4, 2, 20, 5));
		mainStats.setBackground(new Color(0.15f, 0.15f, 0.15f));
		mainStats.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		cpu = new JLabel("CPU: -%");
		Font myLabelfont = new Font(cpu.getFont().getName(), Font.BOLD, cpu.getFont().getSize()-1);
		cpu.setFont(myLabelfont);
		cpu.setForeground(labelCol);
		mainStats.add(cpu);

		mem = new JLabel("Mem: -% / -%");
		mem.setFont(myLabelfont);
		mem.setForeground(labelCol);
		mainStats.add(mem);

		smPackets = new JLabel("SM Packets: - / -");
		smPackets.setFont(myLabelfont);
		smPackets.setForeground(labelCol);
		mainStats.add(smPackets);

		queues = new JLabel("Queues: -");
		queues.setFont(myLabelfont);
		queues.setForeground(labelCol);
		mainStats.add(queues);

		conns = new JLabel("Connections: -");
		conns.setFont(myLabelfont);
		conns.setForeground(labelCol);
		mainStats.add(conns);

		clCache = new JLabel("CL Cache: -");
		clCache.setFont(myLabelfont);
		clCache.setForeground(labelCol);
		mainStats.add(clCache);

		clPackets = new JLabel("CL Packets: - / -");
		clPackets.setFont(myLabelfont);
		clPackets.setForeground(labelCol);
		mainStats.add(clPackets);

		overflows = new JLabel("Overflow: -");
		overflows.setFont(myLabelfont);
		overflows.setForeground(labelCol);
		mainStats.add(overflows);

		panel.add(mainStats);

		details = new JTextArea("Waiting for data from the server", 50, 100);
		Font detailsFont = new Font(details.getFont().getName(), details.getFont().getStyle(),
				details.getFont().getSize()-2);
		details.setFont(detailsFont);
		details.setBackground(new Color(0.15f, 0.15f, 0.15f));
		details.setForeground(Color.LIGHT_GRAY);
		details.setEditable(false);
		panel.add(details);
		panel.setMaximumSize(new Dimension(1920/5, 600));
	}

	@Override
	public void update(String id, StatisticsProviderMBean servBean) {
		Color labelCol = Color.LIGHT_GRAY;
		if (id.equals(this.id)) {
			details.setText(servBean.getSystemDetails());
			String s = MessageFormat.format("CPU: {0,number,#.#}%", servBean.getCPUUsage());
			cpu.setText(s);
			if (servBean.getCPUUsage() > 60f) {
				cpu.setForeground(Color.red);
			} else {
				cpu.setForeground(labelCol);
			}
			s = MessageFormat.format("SM Packets: {0,number,#} / {1,number,#}",
					servBean.getSMPacketsNumberPerSec(), servBean.getSMQueueSize());
			smPackets.setText(s);
			if (servBean.getSMPacketsNumberPerSec() > 10000 || servBean.getSMQueueSize() > 1000) {
				smPackets.setForeground(Color.red);
			} else {
				smPackets.setForeground(labelCol);
			}
			s = MessageFormat.format("Connections: {0,number,#}",
					servBean.getConnectionsNumber());
			conns.setText(s);
			if (servBean.getConnectionsNumber() > 100000) {
				conns.setForeground(Color.YELLOW);
			} else {
				conns.setForeground(labelCol);
			}
			s = MessageFormat.format("CL Packets: {0,number,#} / {1,number,#} / {2,number,#}",
					servBean.getClusterPacketsPerSec(), servBean.getCLQueueSize(), servBean.getCLIOQueueSize());
			clPackets.setText(s);
			if (servBean.getClusterPacketsPerSec() > 10000 || servBean.getCLQueueSize() > 1000 ||
					servBean.getCLIOQueueSize() > 10000) {
				clPackets.setForeground(Color.red);
			} else {
				clPackets.setForeground(labelCol);
			}
			s = MessageFormat.format("Mem: {0,number,#.#}% / {1,number,#.#}%",
					servBean.getHeapMemUsage(), servBean.getNonHeapMemUsage());
			mem.setText(s);
			if (servBean.getHeapMemUsage() > 60f || servBean.getNonHeapMemUsage() > 60) {
				mem.setForeground(Color.red);
			} else {
				mem.setForeground(labelCol);
			}
			s = MessageFormat.format("Queues: {0,number,#}", servBean.getQueueSize());
			queues.setText(s);
			if (servBean.getQueueSize() > 1000) {
				queues.setForeground(Color.red);
			} else {
				queues.setForeground(labelCol);
			}
			s = MessageFormat.format("Overflow: {0,number,#}", servBean.getQueueOverflow());
			overflows.setText(s);
			if (servBean.getQueueOverflow() > 0) {
				overflows.setForeground(Color.red);
			} else {
				overflows.setForeground(labelCol);
			}
			s = MessageFormat.format("CL Cache: {0,number,#}", servBean.getClusterCacheSize());
			clCache.setText(s);
			if (servBean.getClusterCacheSize() > 1000000) {
				clCache.setForeground(Color.YELLOW);
			} else {
				clCache.setForeground(labelCol);
			}
		}
	}

	@Override
	public List<JFreeChart> getCharts() {
		return null;
	}

	@Override
	public JPanel getPanel() {
		return panel;
	}

	class PopupListener implements ActionListener {

		private NodeConfig config = null;

		public PopupListener(NodeConfig nodeConfig) {
			this.config = nodeConfig;
		}

		public void actionPerformed(ActionEvent e) {
			id = config.getDescription();
			title.setTitle(config.toString());
			title.setTitleColor(config.getColor());
			panel.repaint();
		}

	}

}
