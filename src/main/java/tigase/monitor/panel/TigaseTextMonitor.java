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

import org.jfree.chart.JFreeChart;
import tigase.monitor.conf.NodeConfig;
import tigase.stats.JavaJMXProxyOpt;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.List;

import static tigase.monitor.panel.DataChangeListener.*;

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
	private JLabel memGenName = null;
	private JLabel memMax = null;
	private JLabel memUsed = null;
	private JLabel smPackets = null;
	private JLabel queues = null;
	private JLabel conns = null;
	private JLabel overflows = null;
	private JLabel clPackets = null;
	private JLabel clCache = null;
	private JTextArea details = null;
	private JPanel panel = null;
	private String[] metrics = { CPU_USAGE, HEAP_USAGE, NONHEAP_USAGE, HEAP_REGION_MAX, HEAP_REGION_USED, SM_TRAFFIC_R,
			SM_TRAFFIC_S, QUEUE_WAIT, QUEUE_OVERFLOW, C2S_CONNECTIONS, CL_TRAFFIC_R,
			CL_TRAFFIC_S, CL_CACHE_SIZE, SM_QUEUE_WAIT, CL_QUEUE_WAIT, CL_IO_QUEUE_WAIT };
	private long old_sm_traffic = 0;
	private long old_cl_traffic = 0;

	public String[] getMetricsKeys() {
		return metrics;
	}

	public TigaseTextMonitor(String id, List<NodeConfig> nodeConfigs, int updaterate,
			int serverUpdaterare) {
		super("Text: " + id, updaterate, serverUpdaterare);
		this.id = id;
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		title =
				BorderFactory.createTitledBorder(loweredetched, "initialization...",
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
		JPanel mainStats = new JPanel(new GridLayout(6, 2, 5, 5));
		mainStats.setBackground(new Color(0.15f, 0.15f, 0.15f));
		mainStats.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		cpu = new JLabel("CPU: -%");
		Font myLabelfont =
				new Font(cpu.getFont().getName(), Font.BOLD, cpu.getFont().getSize() - 1);
		cpu.setFont(myLabelfont);
		cpu.setForeground(labelCol);
		mainStats.add(cpu);

		mem = new JLabel("Mem: -% / -%");
		mem.setFont(myLabelfont);
		mem.setForeground(labelCol);
		mainStats.add(mem);

		JLabel l = new JLabel("-");
		l.setFont(myLabelfont);
		l.setForeground(labelCol);
		mainStats.add(l);

		memGenName = new JLabel("Region: -");
		memGenName.setFont(myLabelfont);
		memGenName.setForeground(labelCol);
		mainStats.add(memGenName);

		memMax = new JLabel("Mem (max): -");
		memMax.setFont(myLabelfont);
		memMax.setForeground(labelCol);
		mainStats.add(memMax);

		memUsed = new JLabel("Mem (used): -");
        memUsed.setFont(myLabelfont);
        memUsed.setForeground(labelCol);
		mainStats.add(memUsed);

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
		Font detailsFont =
				new Font(details.getFont().getName(), details.getFont().getStyle(), details
						.getFont().getSize() - 2);
		details.setFont(detailsFont);
		details.setBackground(new Color(0.15f, 0.15f, 0.15f));
		details.setForeground(Color.LIGHT_GRAY);
		details.setEditable(false);
		panel.add(details);
		panel.setMaximumSize(new Dimension(1920 / 5, 600));
	}

	public void update(String id, JavaJMXProxyOpt servBean) {
		Color labelCol = Color.LIGHT_GRAY;
		if (id.equals(this.id)) {
			details.setText(servBean.getSystemDetails());
			float tmp = (Float) servBean.getMetricData(CPU_USAGE);
			String s = MessageFormat.format("CPU: {0,number,#.#}%", tmp);
			cpu.setText(s);
			if (tmp > 60f) {
				cpu.setForeground(Color.red);
			} else {
				if (tmp > 30f) {
					cpu.setForeground(Color.YELLOW);
				} else {
					cpu.setForeground(labelCol);
				}
			}
			long sm_traffic =
					(Long) servBean.getMetricData(SM_TRAFFIC_R)
							+ (Long) servBean.getMetricData(SM_TRAFFIC_S);
			long sm_traffic_delta = (sm_traffic - old_sm_traffic) / getUpdaterate();
			old_sm_traffic = sm_traffic;
			int sm_queue = (Integer) servBean.getMetricData(SM_QUEUE_WAIT);
			s =
					MessageFormat.format("SM Packets: {0,number,#} / {1,number,#}",
							sm_traffic_delta, sm_queue);
			smPackets.setText(s);
			if (sm_traffic_delta > 10000 || sm_queue > 1000) {
				if (sm_queue > 1000) {
					smPackets.setForeground(Color.red);
				} else {
					smPackets.setForeground(Color.YELLOW);
				}
			} else {
				smPackets.setForeground(labelCol);
			}
			int c2s_conns = (Integer) servBean.getMetricData(C2S_CONNECTIONS);
			s = MessageFormat.format("Connections: {0,number,#}", c2s_conns);
			conns.setText(s);
			if (c2s_conns > 100000) {
				conns.setForeground(Color.YELLOW);
			} else {
				conns.setForeground(labelCol);
			}
			long cl_traffic =
					(Long) servBean.getMetricData(CL_TRAFFIC_R)
							+ (Long) servBean.getMetricData(CL_TRAFFIC_S);
			long cl_traffic_delta = (cl_traffic - old_cl_traffic) / getUpdaterate();
			old_cl_traffic = cl_traffic;
			int cl_queue = (Integer) servBean.getMetricData(CL_QUEUE_WAIT);
			int cl_io_queue = (Integer) servBean.getMetricData(CL_IO_QUEUE_WAIT);
			s =
					MessageFormat.format("CL Packets: {0,number,#} / {1,number,#} / {2,number,#}",
							cl_traffic_delta, cl_queue, cl_io_queue);
			clPackets.setText(s);
			if (cl_traffic_delta > 10000 || cl_queue > 1000 || cl_io_queue > 10000) {
				if (cl_queue > 1000 || cl_io_queue > 10000) {
					clPackets.setForeground(Color.red);
				} else {
					clPackets.setForeground(Color.YELLOW);
				}
			} else {
				clPackets.setForeground(labelCol);
			}


			float mem_usage = (Float) servBean.getMetricData(HEAP_USAGE);
			float nh_usage = (Float) servBean.getMetricData(NONHEAP_USAGE);
			mem.setText(s);
			if (mem_usage > 60f || nh_usage > 60) {
				mem.setForeground(Color.red);
			} else {
				if (mem_usage > 30f || nh_usage > 30) {
					mem.setForeground(Color.YELLOW);
				} else {
					mem.setForeground(labelCol);
				}
			}


			String region = (String)servBean.getMetricData(HEAP_REGION_NAME);
			s = MessageFormat.format("Region: {0}", region);
			memGenName.setText(s);
			memGenName.setForeground(labelCol);


			String mem_heap_max = (String) servBean.getMetricData(HEAP_REGION_MAX);
			s = MessageFormat.format("Mem (max): {0}", mem_heap_max);
            memMax.setText(s);
            memMax.setForeground(labelCol);

            String mem_heap_used = (String) servBean.getMetricData(HEAP_REGION_USED);
            s = MessageFormat.format("Mem (used): {0}", mem_heap_used);
            memUsed.setText(s);
            memUsed.setForeground(labelCol);

			int queue_wait = (Integer) servBean.getMetricData(QUEUE_WAIT);
			s = MessageFormat.format("Queues: {0,number,#}", queue_wait);
			queues.setText(s);
			if (queue_wait > 1000) {
				queues.setForeground(Color.red);
			} else {
				queues.setForeground(labelCol);
			}
			long queue_overflow = (Long) servBean.getMetricData(QUEUE_OVERFLOW);
			s = MessageFormat.format("Overflow: {0,number,#}", queue_overflow);
			overflows.setText(s);
			if (queue_overflow > 0) {
				overflows.setForeground(Color.red);
			} else {
				overflows.setForeground(labelCol);
			}
			int cl_cache_size = (Integer) servBean.getMetricData(CL_CACHE_SIZE);
			s = MessageFormat.format("CL Cache: {0,number,#}", cl_cache_size);
			clCache.setText(s);
			if (cl_cache_size > 1000000) {
				clCache.setForeground(Color.YELLOW);
			} else {
				clCache.setForeground(labelCol);
			}
		}
	}

	@Override
	public void disconnected(String id) {
		super.disconnected(id);
		if (id.equals(this.id)) {
			details.setText("Waiting for data from the server");
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

		@Override
		public void actionPerformed(ActionEvent e) {
			id = config.getDescription();
			title.setTitle(config.toString());
			title.setTitleColor(config.getColor());
			panel.repaint();
		}

	}

}
