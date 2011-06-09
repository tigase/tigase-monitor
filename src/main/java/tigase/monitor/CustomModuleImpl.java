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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;

import tigase.monitor.conf.ChartConfig;
import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;
//import tigase.monitor.panel.ConnectionsDistribution;
import tigase.monitor.panel.DataChange;
import tigase.monitor.panel.TigaseMonitor;
import tigase.monitor.panel.TigaseMonitorLine;
import tigase.monitor.panel.TigaseTextMonitor;
import tigase.monitor.util.MFileChooser;
import tigase.stats.JavaJMXProxyOpt;

//~--- classes ----------------------------------------------------------------

/**
 * Created: Sep 23, 2009 2:24:40 PM
 * 
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 4 $
 */
public class CustomModuleImpl implements ActionListener {
	private static final String EXPORT_TO_PNG_CMD = "export-to-png";
	private static final String EXPORT_TO_JPG_CMD = "export-to-jpg";
	private static final String AUTO_EXPORT_TIMER_CMD = "auto-export-timer";
	private static Timer timer = new Timer("Statistics updater", true);

	private Configuration config = null;
	private JTextField dir = null;
	private JFrame mainFrame = null;
	private JMenu menu = null;
	private List<JavaJMXProxyOpt> proxies = new LinkedList<JavaJMXProxyOpt>();
	private Map<String, JPanel> panels = new LinkedHashMap<String, JPanel>();
	private List<TigaseMonitor> monitors = new LinkedList<TigaseMonitor>();
	private float row1_height_factor = 0.30f;
	private float row1_width_factor = 0.5f;
	private float row2_height_factor = 0.30f;
	private float row2_width_factor = 0.33f;
	private float row3_height_factor = 0.40f;
	private float row3_width_factor = 0.2f;

	// private DataChange notifier = null;

	/**
	 * Constructs ...
	 * 
	 */
	public CustomModuleImpl() {
	}

	/**
	 * Method description
	 * 
	 * 
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals(EXPORT_TO_PNG_CMD)) {
			exportTo(".png");
		} else {
			if (command.equals(EXPORT_TO_JPG_CMD)) {
				exportTo(".jpg");
			} else {
				if (command.equals(AUTO_EXPORT_TIMER_CMD)) {
					autoExportTimer();
				} else {
					if (command.equals("dir")) {
						MFileChooser fc = new MFileChooser("Directory to store all charts as images");

						fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

						int result = fc.showSaveDialog(mainFrame);

						if (result == JFileChooser.APPROVE_OPTION) {
							File dir_res = fc.getSelectedFile();

							dir.setText(dir_res.toString());
						}
					}
				}
			}
		}
	}

	// /**
	// * Method description
	// *
	// *
	// * @return
	// */
	// public Map<String, BackendService> getBackendServices() {
	// return null;
	// }

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
	public void init(Configuration conf, JFrame mainFrame) {
		this.config = conf;
		this.mainFrame = mainFrame;

		JPanel panel = createPanel(config);

		panels.put("Live View", panel);
		menu = createMenu();

		List<NodeConfig> nodeConfigs = config.getNodeConfigs();

		for (NodeConfig nodeConfig : nodeConfigs) {
			JavaJMXProxyOpt proxy =
					new JavaJMXProxyOpt(nodeConfig.getDescription(), nodeConfig.getHostname(),
							nodeConfig.getPort(), nodeConfig.getUserName(), nodeConfig.getPassword(),
							1000, 1000, config.getLoadHistory());

			proxies.add(proxy);

			for (TigaseMonitor monitor : monitors) {
				proxy.addJMXProxyListener(monitor.getDataChangeListener());
			}

			proxy.start();
		}

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				// System.out.println("Update... " + new Date());
				for (JavaJMXProxyOpt javaJMXProxy : proxies) {
					if (javaJMXProxy.isInitialized()) {
						// notifier.update(javaJMXProxy.getId(), javaJMXProxy);
						// System.out.println("Update proxy: " + javaJMXProxy.getId());
						for (TigaseMonitor monitor : monitors) {
							if (monitor.isReady(javaJMXProxy.getId())) {
								// System.out.println("Update monitor: " + monitor.getTitle());
								monitor.getDataChangeListener()
										.update(javaJMXProxy.getId(), javaJMXProxy);
							} else {
								System.out.println("Update monitor - monitor not ready: "
										+ monitor.getTitle());
							}
						}
					}
				}
			}
		}, 1000, config.getUpdaterate() * 1000l);
	}

	private void autoExportTimer() {
		String message = "Please select options for chart export.";
		JLabel label = new JLabel("Timer delay in seconds");
		JTextField delay = new JTextField();

		delay.setText("3600");

		JLabel repeat_lab = new JLabel("Repeat saving images");
		JTextField repeat = new JTextField();

		repeat.setText("1");

		JLabel interval_lab = new JLabel("Interval if repeat greater than 1");
		JTextField interval = new JTextField();

		interval.setText("3600");

		JCheckBox pngExport = new JCheckBox("Export to PNG", true);
		JCheckBox jpgExport = new JCheckBox("Export to JPG", false);
		JCheckBox exitOnComplete = new JCheckBox("Exit on complete", false);
		JLabel labelDir = new JLabel("Directory");
		JPanel dirPanel = new JPanel();

		dirPanel.setLayout(new BoxLayout(dirPanel, BoxLayout.X_AXIS));
		dir = new JTextField();
		dirPanel.add(dir);

		JButton dirButton = new JButton("Directory...");

		dirButton.setActionCommand("dir");
		dirButton.addActionListener(this);
		dirPanel.add(dirButton);

		int result =
				JOptionPane.showOptionDialog(mainFrame, new Object[] { message, label, delay,
						repeat_lab, repeat, interval_lab, interval, pngExport, jpgExport,
						exitOnComplete, labelDir, dirPanel }, "Login", JOptionPane.OK_CANCEL_OPTION,
						JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (result == JOptionPane.OK_OPTION) {
			long delayLong = -1;
			int repeatInt = 1;
			long intervalLong = -1;

			try {
				delayLong = Long.parseLong(delay.getText()) * 1000;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(mainFrame,
						"Incorrect delay value: " + delay.getText(), "Error",
						JOptionPane.ERROR_MESSAGE);

				return;
			}

			if ((repeat.getText() != null) && !repeat.getText().isEmpty()) {
				try {
					repeatInt = Integer.parseInt(repeat.getText());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(mainFrame,
							"Incorrect repeat value: " + repeat.getText(), "Error",
							JOptionPane.ERROR_MESSAGE);

					return;
				}
			}

			if (repeatInt > 1) {
				try {
					intervalLong = Long.parseLong(interval.getText()) * 1000;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(mainFrame, "Incorrect interval value: "
							+ interval.getText(), "Error", JOptionPane.ERROR_MESSAGE);

					return;
				}
			}

			if ((dir.getText() == null) || dir.getText().isEmpty()) {
				JOptionPane.showMessageDialog(mainFrame, "Please enter valid directory name.",
						"Error", JOptionPane.ERROR_MESSAGE);

				return;
			}

			boolean exit = exitOnComplete.isSelected();

			if (pngExport.isSelected()) {
				saveAllTo(new File(dir.getText()), ".png", exit && !jpgExport.isSelected(),
						delayLong, repeatInt, intervalLong);
			}

			if (jpgExport.isSelected()) {
				saveAllTo(new File(dir.getText()), ".jpg", exit, delayLong + 1000, repeatInt,
						intervalLong);
			}
		}
	}

	private JMenu createMenu() {
		JMenu monitorMenu = new JMenu("Moitor", true);

		monitorMenu.setMnemonic('M');

		JMenuItem item = null;

		item = new JMenuItem("Export to PNG...", 'p');
		item.setActionCommand(EXPORT_TO_PNG_CMD);
		item.addActionListener(this);
		monitorMenu.add(item);
		item = new JMenuItem("Export to JPG...", 'p');
		item.setActionCommand(EXPORT_TO_JPG_CMD);
		item.addActionListener(this);
		monitorMenu.add(item);
		monitorMenu.addSeparator();
		item = new JMenuItem("Auto export timer...", 'p');
		item.setActionCommand(AUTO_EXPORT_TIMER_CMD);
		item.addActionListener(this);
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

		// ConnectionsDistribution distr = new ConnectionsDistribution();
		// new DataChange(distr, false, false, C2S_CONNECTIONS) {
		//
		// public void update(String id, JavaJMXProxyOpt bean) {
		// monitor.update(id, bean);
		// }
		//
		// };
		//
		// monitors.add(distr);

		int width_distr = Math.round(config.getHeight() * row1_height_factor);
		int height = Math.round(config.getHeight() * row1_height_factor);
		Dimension dim = new Dimension(width_distr, height);
		//
		// distr.getPanel().setPreferredSize(dim);
		// row1.add(distr.getPanel());

		ChartConfig conf = config.getChartConfig(1);

		TigaseMonitorLine cpu =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), config.getTimeline(), config.getUpdaterate(),
						config.getServerUpdaterate());
		new DataChange(cpu, conf.countDelta(), true, conf.getSeries());
		monitors.add(cpu);

		int width = Math.round((config.getWidth() - width_distr) * row1_width_factor - 10);

		height = Math.round(config.getHeight() * row1_height_factor);
		dim = new Dimension(width, height);
		cpu.getPanel().setPreferredSize(dim);
		row1.add(cpu.getPanel());

		conf = config.getChartConfig(2);
		TigaseMonitorLine mem =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), config.getTimeline(), config.getUpdaterate(),
						config.getServerUpdaterate());
		new DataChange(mem, conf.countDelta(), true, conf.getSeries());

		monitors.add(mem);
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
						conf.countTotals(), config.getTimeline(), config.getUpdaterate(),
						config.getServerUpdaterate());
		new DataChange(conns, conf.countDelta(), true, conf.getSeries());

		monitors.add(conns);
		dim = new Dimension(width, height);
		conns.getPanel().setPreferredSize(dim);
		row2.add(conns.getPanel());

		conf = config.getChartConfig(4);
		TigaseMonitorLine sm =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), config.getTimeline(), config.getUpdaterate(),
						config.getServerUpdaterate());
		new DataChange(sm, conf.countDelta(), true, conf.getSeries());

		monitors.add(sm);
		dim = new Dimension(width, height);
		sm.getPanel().setPreferredSize(dim);
		row2.add(sm.getPanel());

		conf = config.getChartConfig(5);
		TigaseMonitorLine cl =
				new TigaseMonitorLine(conf.getXTitle(), conf.getYTitle(), conf.getMaxY(),
						conf.countTotals(), config.getTimeline(), config.getUpdaterate(),
						config.getServerUpdaterate());
		new DataChange(cl, conf.countDelta(), true, conf.getSeries());

		monitors.add(cl);
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

	private void exportTo(String ext) {
		MFileChooser fc =
				new MFileChooser("Directory to store all charts as " + ext + " files");

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = fc.showSaveDialog(mainFrame);

		if (result == JFileChooser.APPROVE_OPTION) {
			File dir_res = fc.getSelectedFile();

			saveAllTo(dir_res, ext, false, 0, 1, 0);
		}
	}

	private void saveAllTo(File dir, String ext, boolean exitOnComplete, long delay,
			int repeat, long interval) {
		Thread thread =
				new BackgroundSaver(dir, ext, exitOnComplete, delay, repeat, interval);

		thread.start();
	}

	// ~--- inner classes --------------------------------------------------------

	class BackgroundSaver extends Thread {
		private long delay = 0;
		private File dir = null;
		private String ext = null;
		private boolean exitOnComplete = false;
		private long interval = 0;
		private int repeat = 1;

		// ~--- constructors -------------------------------------------------------

		private BackgroundSaver(File dir, String ext, boolean exitOnComplete, long delay,
				int repeat, long interval) {
			this.dir = dir;
			this.ext = ext;
			this.exitOnComplete = exitOnComplete;
			this.delay = delay;
			this.repeat = (repeat < 1) ? 1 : repeat;
			this.interval = interval;
		}

		// ~--- methods ------------------------------------------------------------

		/**
		 * Method description
		 * 
		 */
		@Override
		public void run() {
			if (delay > 0) {
				try {
					sleep(delay);
				} catch (Exception e) {
				}
			}

			if (!dir.exists()) {
				dir.mkdirs();
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss_");

			for (int j = 0; j < repeat; j++) {
				String datetime = sdf.format(new Date());

				for (TigaseMonitor monitor : monitors) {
					int i = 0;
					int w = monitor.getPanel().getWidth();
					int h = monitor.getPanel().getHeight();
					List<JFreeChart> charts = monitor.getCharts();

					if (charts != null) {
						for (JFreeChart chart : charts) {
							File file =
									new File(dir, datetime + (++i) + "_" + chart.getTitle().getText() + ext);

							try {
								JFreeChart ch = (JFreeChart) chart.clone();

								if (ext == ".png") {
									ChartUtilities.saveChartAsPNG(file, ch, w, h);
								}

								if (ext == ".jpg") {
									ChartUtilities.saveChartAsJPEG(file, ch, w, h);
								}
							} catch (Exception e) {
								System.err.println("Can't save file: " + file);
							}
						}
					}
				}

				if ((interval > 0) && (j + 1 < repeat)) {
					try {
						sleep(interval);
					} catch (Exception e) {
					}
				}
			}

			if (exitOnComplete) {
				mainFrame.dispose();
				System.exit(0);
			}
		}
	}
}
