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
import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;
import tigase.stats.JavaJMXProxyOpt;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static tigase.monitor.panel.DataChangeListener.*;

public class TigaseTextMemoryMonitor
		extends TigaseMonitor {

	JPopupMenu contextMenu = null;
	JTable memoryTable = null;
	private JTextArea details = null;
	private String id = null;
	private String[] metrics = {HEAP_EDEN_NAME, HEAP_EDEN_USAGE_USED, HEAP_EDEN_USAGE_MAX, HEAP_EDEN_PEAK_USAGE_USED,
								HEAP_EDEN_PEAK_USAGE_MAX, HEAP_EDEN_COLLECTION_USAGE_USED,
								HEAP_EDEN_COLLECTION_USAGE_MAX, HEAP_SURVIVOR_NAME, HEAP_SURVIVOR_USAGE_USED,
								HEAP_SURVIVOR_USAGE_MAX, HEAP_SURVIVOR_PEAK_USAGE_USED, HEAP_SURVIVOR_PEAK_USAGE_MAX,
								HEAP_SURVIVOR_COLLECTION_USAGE_USED, HEAP_SURVIVOR_COLLECTION_USAGE_MAX, HEAP_OLD_NAME,
								HEAP_OLD_USAGE_USED, HEAP_OLD_USAGE_MAX, HEAP_OLD_PEAK_USAGE_USED,
								HEAP_OLD_PEAK_USAGE_MAX, HEAP_OLD_COLLECTION_USAGE_USED, HEAP_OLD_COLLECTION_USAGE_MAX,
								GC_STATISTICS};
	private JPanel panel = null;
	private TitledBorder title = null;
	private Configuration.UNITS unitFactor = Configuration.UNITS.KB;

	public TigaseTextMemoryMonitor(String id, List<NodeConfig> nodeConfigs, int updaterate, int serverUpdaterare,
								   Configuration.UNITS unitFactor) {
		super("Text: " + id, updaterate, serverUpdaterare);
		this.id = id;
		Border loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);

		this.unitFactor = unitFactor;

		title = BorderFactory.createTitledBorder(loweredetched, "initialization...", TitledBorder.LEFT,
												 TitledBorder.CENTER);
		panel = new JPanel();
		panel.setBorder(title);
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		Color bgDarkGrey = new Color(0.15f, 0.15f, 0.15f);
		panel.setBackground(bgDarkGrey);
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
				title.setTitle(nodeConfig.toString() + " (waiting for data)");
				title.setTitleColor(nodeConfig.getColor());
			}
		}
		panel.setComponentPopupMenu(contextMenu);

		Object[][] memoryCells = new Object[][]{{"Usage/Used", 0L, 0L, 0L}, {"Usage/Max", 0L, 0L, 0L},
												{"Collection/Used", 0L, 0L, 0L}, {"Collection/Max", 0L, 0L, 0L},
												{"Peak/Used", 0L, 0L, 0L}, {"Peak/Max", 0L, 0L, 0L}};

		String[] colNames = new String[]{unitFactor.name(), "Eden", "Survivor", "Old"};
		memoryTable = new JTable(new MyAbstractTableModel(colNames, memoryCells)) {
			@Override
			public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
				Component stamp = super.prepareRenderer(renderer, row, column);
				stamp.setForeground(Color.GRAY);
				if (row % 2 == 0) {
					stamp.setBackground(bgDarkGrey.brighter());
				} else {
					stamp.setBackground(bgDarkGrey);
				}
				return stamp;
			}
		};

		TableColumn column = memoryTable.getColumnModel().getColumn(0);

		memoryTable.setRowSelectionAllowed(true);
		memoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		column.setMinWidth(100);

		Dimension size = new Dimension(300, 70);
		memoryTable.setMinimumSize(size);
		JScrollPane scrollPane = new JScrollPane(memoryTable);
		scrollPane.setMinimumSize(size);
		memoryTable.setFillsViewportHeight(true);
		scrollPane.setPreferredSize(size);

		memoryTable.setBackground(bgDarkGrey);

		panel.add(scrollPane);

		details = new JTextArea("Waiting for data from the server", 5, 100);
		Font detailsFont = new Font(details.getFont().getName(), details.getFont().getStyle(),
									details.getFont().getSize() - 1);
		details.setLineWrap(true);
		details.setWrapStyleWord(true);
		details.setFont(detailsFont);
		details.setBackground(bgDarkGrey);
		details.setForeground(Color.LIGHT_GRAY);
		details.setEditable(false);
		details.setMinimumSize(new Dimension(1920 / 5, 250));
		details.setMaximumSize(new Dimension(1920 / 5, 600));
		JScrollPane detailsScrollPane = new JScrollPane(details);
		detailsScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		detailsScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		detailsScrollPane.setMinimumSize(new Dimension(1920 / 5, 150));
		panel.add(detailsScrollPane);
		panel.setMaximumSize(new Dimension(1920 / 5, 600));
	}

	public String[] getMetricsKeys() {
		return metrics;
	}

	public void update(String id, JavaJMXProxyOpt servBean) {
		Color labelCol = Color.LIGHT_GRAY;
		if (id.equals(this.id)) {

			title.setTitle(title.getTitle().replaceAll("\\(.*\\)", "(✔)"));

			String tmp = (String) servBean.getMetricData(HEAP_EDEN_NAME);
			memoryTable.getTableHeader().getColumnModel().getColumn(1).setHeaderValue(tmp);
			tmp = (String) servBean.getMetricData(HEAP_SURVIVOR_NAME);
			memoryTable.getTableHeader().getColumnModel().getColumn(2).setHeaderValue(tmp);
			tmp = (String) servBean.getMetricData(HEAP_OLD_NAME);
			memoryTable.getTableHeader().getColumnModel().getColumn(3).setHeaderValue(tmp);
			panel.repaint();

			addValue(servBean, 0, 1, HEAP_EDEN_USAGE_USED);
			addValue(servBean, 0, 2, HEAP_SURVIVOR_USAGE_USED);
			addValue(servBean, 0, 3, HEAP_OLD_USAGE_USED);

			addValue(servBean, 1, 1, HEAP_EDEN_USAGE_MAX);
			addValue(servBean, 1, 2, HEAP_SURVIVOR_USAGE_MAX);
			addValue(servBean, 1, 3, HEAP_OLD_USAGE_MAX);

			addValue(servBean, 2, 1, HEAP_EDEN_COLLECTION_USAGE_USED);
			addValue(servBean, 2, 2, HEAP_SURVIVOR_COLLECTION_USAGE_USED);
			addValue(servBean, 2, 3, HEAP_OLD_COLLECTION_USAGE_USED);

			addValue(servBean, 3, 1, HEAP_EDEN_COLLECTION_USAGE_MAX);
			addValue(servBean, 3, 2, HEAP_SURVIVOR_COLLECTION_USAGE_MAX);
			addValue(servBean, 3, 3, HEAP_OLD_COLLECTION_USAGE_MAX);

			addValue(servBean, 4, 1, HEAP_EDEN_PEAK_USAGE_USED);
			addValue(servBean, 4, 2, HEAP_SURVIVOR_PEAK_USAGE_USED);
			addValue(servBean, 4, 3, HEAP_OLD_PEAK_USAGE_USED);

			addValue(servBean, 5, 1, HEAP_EDEN_PEAK_USAGE_MAX);
			addValue(servBean, 5, 2, HEAP_SURVIVOR_PEAK_USAGE_MAX);
			addValue(servBean, 5, 3, HEAP_OLD_PEAK_USAGE_MAX);

			Map<String, Map<String, String>> collectors = new ConcurrentHashMap<>();

			StringBuilder sb = new StringBuilder();

			String gcCollectors = (String) servBean.getMetricData(GC_STATISTICS);

			if (gcCollectors != null && !gcCollectors.trim().isEmpty()) {
				String[] tmp_collectors = gcCollectors.split("\\|");

				for (String coll : tmp_collectors) {

					String gc = coll.substring(1, coll.length() - 1);

					if (gc != null) {

						Map<String, String> col = new HashMap<>();
						for (String s : gc.split(";")) {
							String[] split = s.split("=");
							col.put(split[0], split[1]);
							if ("name".equals(split[0])) {
								collectors.put(split[1], col);
							}

						}
					}

				}
			}

			for (Map.Entry<String, Map<String, String>> stringMapEntry : collectors.entrySet()) {
				sb.append(stringMapEntry.getKey());
				sb.append("\t");
				sb.append(stringMapEntry.getValue().get("pools"));
				sb.append(":\n");
				sb.append("     ");
				sb.append("Count: ");
				sb.append(stringMapEntry.getValue().get("count"));
				sb.append("     ");
				sb.append("Total time: ");
				sb.append(stringMapEntry.getValue().get("time")).append("ms");
				sb.append("     ");
				sb.append("Average Time: ");
				sb.append(stringMapEntry.getValue().get("avgTime")).append("ms");
				sb.append("\n\n");
			}
			details.setText(sb.toString());

		}

	}

	@Override
	public void disconnected(String id) {
		super.disconnected(id);
		if (id.equals(this.id)) {
			title.setTitle(title.getTitle().replaceAll("\\(.*\\)", "(waiting for data)"));
			details.setText("Waiting for data from the server");
			panel.repaint();
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

	private void addValue(JavaJMXProxyOpt servBean, int idxRow, int idxCol, String metric) {
		long value = 0;
		value = (long) servBean.getMetricData(metric);
		value = value / unitFactor.getFactor();
		memoryTable.setValueAt(value, idxRow, idxCol);
	}

	private static class MyAbstractTableModel
			extends AbstractTableModel {

		private String[] columnNames;
		private Object[][] data;

		public MyAbstractTableModel(String[] colNames, Object[][] cells) {
			columnNames = colNames;
			data = cells;
		}

		@Override
		public int getRowCount() {
			return data.length;
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			data[rowIndex][columnIndex] = aValue;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return data[rowIndex][columnIndex];
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		public boolean isCellEditable(int row, int col) {
			return false;
		}
	}

	class PopupListener
			implements ActionListener {

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
