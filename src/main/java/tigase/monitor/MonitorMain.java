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
package tigase.monitor;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.jfree.ui.ApplicationFrame;
import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;
import tigase.monitor.panel.DataChange;
import tigase.monitor.panel.DataChangeListener;
import tigase.monitor.panel.TigaseMonitor;
import tigase.monitor.util.AlertDialogDisplay;
import tigase.monitor.util.BackgroundSaver;
import tigase.monitor.util.MFileChooser;
import tigase.stats.JavaJMXProxyOpt;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author kobit
 */
public class MonitorMain
		extends ApplicationFrame
		implements ActionListener {

	protected static final long serialVersionUID = 1L;
	protected static final String EXPORT_TO_PNG_CMD = "export-to-png";
	protected static final String EXPORT_TO_JPG_CMD = "export-to-jpg";
	protected static final String AUTO_EXPORT_TIMER_CMD = "auto-export-timer";
	protected static final String EXPORT_DIRECTORY_CMD = "export-directory";

	protected static final String EXIT_CMD = "exit";
	private static final String PROP_FILENAME_DEF = "etc/monitor.properties";

	private static final String PROP_FILENAME_KEY = "--init";
	public static List<TigaseMonitor> monitors = new LinkedList<>();
	private static AlertDialogDisplay alertDialogDisplay = null;
	private static Configuration config = null;
	private static DataChange notifier = null;
	private static List<JavaJMXProxyOpt> proxies = new LinkedList<>();
	private static Timer timer = new Timer("Statistics updater", true);

	static {
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Tigase Monitor");
		System.setProperty("apple.awt.application.name", "Tigase Monitor");

	}

	private JTextField dir = null;

	public static boolean addMonitor(TigaseMonitor tigaseMonitor) {
		return monitors.add(tigaseMonitor);
	}

	public static void main(String[] args) {
		System.setProperty("apple.laf.useScreenMenuBar", "true");

		String propFile = PROP_FILENAME_DEF;
		if (args != null && args.length > 1) {
			if (args[0].equals(PROP_FILENAME_KEY)) {
				propFile = args[1];
			}
		}
		try {
			config = new Configuration(propFile);
		} catch (Exception e) {
			System.out.println("init.properties file missing, using defaults.");
			e.printStackTrace();
		}

		try {
			UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			// ... otherwise just use the system look and feel
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		StandardChartTheme theme = (StandardChartTheme) StandardChartTheme.createDarknessTheme();
		theme.setChartBackgroundPaint(Color.DARK_GRAY);
		theme.setPlotBackgroundPaint(new Color(0.15f, 0.15f, 0.15f));
		ChartFactory.setChartTheme(theme);

		MonitorMain app = new MonitorMain(config);

		TigaseWindowAbstract windowMain = new WindowMain(config, app);

		if (config.customWindow()) {
			new WindowCustom(config, app);

//            windowMain.addPanels(config.getCustomTitle(), new TigasePanelCustom(config,app));
		}

		if (config.displayAlarm()) {
			alertDialogDisplay = new AlertDialogDisplay(app, config.getAlarmFileName());
			alertDialogDisplay.setDaemon(true);
			alertDialogDisplay.start();

			notifier = new AlertDataChange();
		}

		List<NodeConfig> nodeConfigs = config.getNodeConfigs();

		for (NodeConfig nodeConfig : nodeConfigs) {
			JavaJMXProxyOpt proxy = new JavaJMXProxyOpt(nodeConfig.getDescription(), nodeConfig.getHostname(),
														nodeConfig.getPort(), nodeConfig.getUserName(),
														nodeConfig.getPassword(), 1000, config.getUpdaterate() * 1000,
														config.getLoadHistory());

			proxies.add(proxy);

			if (notifier != null) {
				proxy.addJMXProxyListener(notifier);
			}

			for (TigaseMonitor monitor : monitors) {
				proxy.addJMXProxyListener(monitor.getDataChangeListener());
			}

			proxy.start();
		}

		timer.schedule(new TimerTask() {
			@Override
			public void run() {
//                System.out.println("==\nUpdate... " + new Date());
				for (JavaJMXProxyOpt javaJMXProxy : proxies) {
					try {
						if (javaJMXProxy.isInitialized()) {
							if (notifier != null) {
								notifier.update(javaJMXProxy.getId(), javaJMXProxy);
							}
							// System.out.println("Update proxy: " +
							// javaJMXProxy.getId());
							for (TigaseMonitor monitor : monitors) {
								if (monitor.isReady(javaJMXProxy.getId())) {
									// System.out.println("Update monitor: " +
									// monitor.getTitle());
									monitor.getDataChangeListener().update(javaJMXProxy.getId(), javaJMXProxy);
								} else {
									System.out.println("Update monitor - monitor not ready: " + monitor.getTitle());
								}
							}
						}
					} catch (Exception e) {
						System.err.println("Updated problem: " + e);
					}
				}
			}
		}, 1000, config.getUpdaterate() * 1000L);
	}

	public static boolean removeMonitor(TigaseMonitor tigaseMonitor) {
		return monitors.remove(tigaseMonitor);
	}

	public MonitorMain(Configuration config) {
		super(config.getMainTitle());
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		System.out.println(command);

		switch (command) {
			case EXIT_CMD:
				attemptExit();
				break;
			case EXPORT_TO_PNG_CMD:
				exportTo(".png");
				break;
			case EXPORT_TO_JPG_CMD:
				exportTo(".jpg");
				break;
			case AUTO_EXPORT_TIMER_CMD:
				autoExportTimer();
				break;
			case EXPORT_DIRECTORY_CMD:
				dir = directoryChooser();
				break;
		}
	}

	private void attemptExit() {
		String title = "Confirm";
		String message = "Are you sure you want to exit?";
		int result = JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION,
												   JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.YES_OPTION) {
			dispose();
			System.exit(0);
		}
	}

	private void exportTo(String ext) {
		MFileChooser fc = new MFileChooser("Directory to store all charts as " + ext + " files");

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = fc.showSaveDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File dir_res = fc.getSelectedFile();

			saveAllTo(dir_res, ext, false, 0, 1, 0);
		}
	}

	private void saveAllTo(File dir, String ext, boolean exitOnComplete, long delay, int repeat, long interval) {
		Thread thread = new BackgroundSaver(this, dir, ext, exitOnComplete, delay, repeat, interval);

		thread.start();
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

		dirButton.setActionCommand(EXPORT_DIRECTORY_CMD);
		dirButton.addActionListener(this);
		dirPanel.add(dirButton);

		int result = JOptionPane.showOptionDialog(this,
												  new Object[]{message, label, delay, repeat_lab, repeat, interval_lab,
															   interval, pngExport, jpgExport, exitOnComplete, labelDir,
															   dirPanel}, "Login", JOptionPane.OK_CANCEL_OPTION,
												  JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (result == JOptionPane.OK_OPTION) {
			long delayLong = -1;
			int repeatInt = 1;
			long intervalLong = -1;

			try {
				delayLong = Long.parseLong(delay.getText()) * 1000;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Incorrect delay value: " + delay.getText(), "Error",
											  JOptionPane.ERROR_MESSAGE);

				return;
			}

			if ((repeat.getText() != null) && !repeat.getText().isEmpty()) {
				try {
					repeatInt = Integer.parseInt(repeat.getText());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "Incorrect repeat value: " + repeat.getText(), "Error",
												  JOptionPane.ERROR_MESSAGE);

					return;
				}
			}

			if (repeatInt > 1) {
				try {
					intervalLong = Long.parseLong(interval.getText()) * 1000;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this, "Incorrect interval value: " + interval.getText(), "Error",
												  JOptionPane.ERROR_MESSAGE);

					return;
				}
			}

			if ((dir.getText() == null) || dir.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please enter valid directory name.", "Error",
											  JOptionPane.ERROR_MESSAGE);

				return;
			}

			boolean exit = exitOnComplete.isSelected();

			if (pngExport.isSelected()) {
				saveAllTo(new File(dir.getText()), ".png", exit && !jpgExport.isSelected(), delayLong, repeatInt,
						  intervalLong);
			}

			if (jpgExport.isSelected()) {
				saveAllTo(new File(dir.getText()), ".jpg", exit, delayLong + 1000, repeatInt, intervalLong);
			}
		}
	}

	private JTextField directoryChooser() {
		JTextField dir_result = new JTextField();
		MFileChooser fc = new MFileChooser("Directory to store all charts as images");

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = fc.showSaveDialog(this);

		if (result == JFileChooser.APPROVE_OPTION) {
			File dir_res = fc.getSelectedFile();

			dir_result.setText(dir_res.toString());
		}

		return dir_result;

	}

	private static class AlertDataChange
			extends DataChange {

		private float errorThreshold = config.getErrorThreshold();
		private float warningThreshold = config.getWarningThreshold();

		public AlertDataChange() {
			super(null, false, false, DataChangeListener.CPU_USAGE, DataChangeListener.HEAP_USAGE,
				  DataChangeListener.NONHEAP_USAGE);
		}

		public void update(String id, JavaJMXProxyOpt bean) {
			for (String dataId : getDataIds()) {
				Object metricData = bean.getMetricData(dataId);
				if (null != metricData) {
					float val = (Float) metricData;
					if (val > errorThreshold) {
						alertDialogDisplay.wakeup(JOptionPane.ERROR_MESSAGE, "Resource near limit!",
												  "Critical " + msgMapping.get(dataId) + " usage on " + id +
														  " machine: " + bean.getHostname());
					}
					if (val > warningThreshold) {
						alertDialogDisplay.wakeup(JOptionPane.WARNING_MESSAGE, "High resource usage!",
												  "High " + msgMapping.get(dataId) + " usage on " + id + " machine: " +
														  bean.getHostname());
					}
				}
			}
		}

	}
}
