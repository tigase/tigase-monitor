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

package tigase.monitor;

import static tigase.monitor.panel.DataChangeListener.CPU_USAGE;
import static tigase.monitor.panel.DataChangeListener.HEAP_USAGE;
import static tigase.monitor.panel.DataChangeListener.NONHEAP_USAGE;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import org.tigase.licence.Licence;
import org.tigase.licence.LicenceLoader;
import org.tigase.licence.LicenceLoaderFactory;

import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;
import tigase.monitor.panel.DataChange;
import tigase.monitor.panel.TigaseMonitor;
import tigase.monitor.util.MFileChooser;
import tigase.stats.JavaJMXProxyOpt;

/**
 * 
 * @author kobit
 */
public class MonitorMain extends ApplicationFrame implements ActionListener {

	protected static final long serialVersionUID = 1L;
	protected static final String EXPORT_TO_PNG_CMD = "export-to-png";
	protected static final String EXPORT_TO_JPG_CMD = "export-to-jpg";
	protected static final String AUTO_EXPORT_TIMER_CMD = "auto-export-timer";

	protected static final String EXIT_CMD = "exit";
	private static final String PROP_FILENAME_DEF = "etc/monitor.properties";
	private static final File LICENCE_FILE_DEF = new File("etc/monitor.licence");

	private static final String PROP_FILENAME_KEY = "--init";
	private static Configuration config = null;
	private static List<JavaJMXProxyOpt> proxies = new LinkedList<JavaJMXProxyOpt>();
	protected static List<TigaseMonitor> monitors = new LinkedList<TigaseMonitor>();
	private static DataChange notifier = null;
	private static Timer timer = new Timer("Statistics updater", true);
	private static DialogDisplay dialogDisplay = null;

	private ClientModuleImpl moduleImpl = null;
	private JTextField dir = null;

	public MonitorMain(Configuration config) {
		super(config.getMainTitle());
		// System.out.println(sun.net.InetAddressCachePolicy.get());
		moduleImpl = new ClientModuleImpl();
		moduleImpl.init(config, this);
		setContentPane(createContent());
		setJMenuBar(createMenuBar());
		setPreferredSize(moduleImpl.getPreferredSize());
	}

	private JComponent createContent() {

		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.DARK_GRAY);

		JTabbedPane tabs = new JTabbedPane();
		tabs.setBackground(Color.DARK_GRAY);
		Map<String, JPanel> panels = moduleImpl.getJPanels();
		for (Map.Entry<String, JPanel> panelEntry : panels.entrySet()) {
			tabs.add(panelEntry.getKey(), panelEntry.getValue());
		}
		content.add(tabs);
		return content;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu monitorMenu = moduleImpl.getJMenu(menuBar);

		monitorMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem("Exit", 'x');
		exitItem.setActionCommand(EXIT_CMD);
		exitItem.addActionListener(this);
		monitorMenu.add(exitItem);

		menuBar.add(monitorMenu);
		return menuBar;
	}

	/**
	 * @param args
	 *          the command line arguments
	 */
	public static void main(String[] args) {
		final Licence lic;
		try {
			final LicenceLoader loader = LicenceLoaderFactory.create();
			if (!LICENCE_FILE_DEF.exists()) {
				System.err.println("Licence file doesn't exists!");
				System.exit(10);
			}
			lic = loader.loadLicence(LICENCE_FILE_DEF);

			switch (lic.check()) {
				case invalidDates:
					System.err.println("Licence is expired.");
					System.exit(13);
					return;
				case invalidSignature:
					System.err.println("Invalid or modified licence file");
					System.exit(11);
					return;
			}

			String appId = lic.getPropertyAsString("app-id");
			if (appId == null || !appId.equals("tigase-monitor")) {
				System.err.println("This is not licence for Tigase Monitor!");
				System.exit(14);
				return;
			}

		} catch (Exception e) {
			System.err.println("Can't load licence file. Error: " + e.getMessage());
			System.exit(12);
			return;
		}

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
		StandardChartTheme theme =
				(StandardChartTheme) StandardChartTheme.createDarknessTheme();
		theme.setChartBackgroundPaint(Color.DARK_GRAY);
		theme.setPlotBackgroundPaint(new Color(0.15f, 0.15f, 0.15f));
		ChartFactory.setChartTheme(theme);
		MonitorMain app = new MonitorMain(config);
		app.pack();
		RefineryUtilities.centerFrameOnScreen(app);
		app.setVisible(true);
		if (config.customWindow()) {
			CustomWindow customWindow = new CustomWindow(config, app);
			customWindow.pack();
			RefineryUtilities.centerFrameOnScreen(customWindow);
			customWindow.setVisible(true);
		}

		if (config.displayAlarm()) {
			dialogDisplay = app.new DialogDisplay(config.getAlarmFileName());
			dialogDisplay.setDaemon(true);
			dialogDisplay.start();

			notifier =
					new DataChange(null, false, false, CPU_USAGE, HEAP_USAGE, NONHEAP_USAGE) {

						private float warningThreshold = config.getWarningThreshold();
						private float errorThreshold = config.getErrorThreshold();

						public void update(String id, JavaJMXProxyOpt bean) {
							for (String dataId : getDataIds()) {
								float val = (Float) bean.getMetricData(dataId);
								if (val > errorThreshold) {
									dialogDisplay.wakeup(JOptionPane.ERROR_MESSAGE, "Resource near limit!",
											"Critical " + msgMapping.get(dataId) + " usage on " + id
													+ " machine: " + bean.getHostname());
								}
							}
							for (String dataId : getDataIds()) {
								float val = (Float) bean.getMetricData(dataId);
								if (val > warningThreshold) {
									dialogDisplay.wakeup(JOptionPane.WARNING_MESSAGE,
											"High resource usage!", "High " + msgMapping.get(dataId)
													+ " usage on " + id + " machine: " + bean.getHostname());
								}
							}
						}

					};
		}

		List<NodeConfig> nodeConfigs = config.getNodeConfigs();

		for (NodeConfig nodeConfig : nodeConfigs) {
			JavaJMXProxyOpt proxy =
					new JavaJMXProxyOpt(nodeConfig.getDescription(), nodeConfig.getHostname(),
							nodeConfig.getPort(), nodeConfig.getUserName(), nodeConfig.getPassword(),
							1000, config.getUpdaterate() * 1000, config.getLoadHistory());

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
				// System.out.println("Update... " + new Date());
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
									monitor.getDataChangeListener().update(javaJMXProxy.getId(),
											javaJMXProxy);
								} else {
									System.out.println("Update monitor - monitor not ready: "
											+ monitor.getTitle());
								}
							}
						}
					} catch (Exception e) {
						System.err.println("Updated problem: " + e);
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
				JOptionPane.showOptionDialog(this, new Object[] { message, label, delay,
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
				JOptionPane.showMessageDialog(this, "Incorrect delay value: " + delay.getText(),
						"Error", JOptionPane.ERROR_MESSAGE);

				return;
			}

			if ((repeat.getText() != null) && !repeat.getText().isEmpty()) {
				try {
					repeatInt = Integer.parseInt(repeat.getText());
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this,
							"Incorrect repeat value: " + repeat.getText(), "Error",
							JOptionPane.ERROR_MESSAGE);

					return;
				}
			}

			if (repeatInt > 1) {
				try {
					intervalLong = Long.parseLong(interval.getText()) * 1000;
				} catch (Exception e) {
					JOptionPane.showMessageDialog(this,
							"Incorrect interval value: " + interval.getText(), "Error",
							JOptionPane.ERROR_MESSAGE);

					return;
				}
			}

			if ((dir.getText() == null) || dir.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this, "Please enter valid directory name.",
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

	/**
	 * Method description
	 * 
	 * 
	 * @param e
	 */
	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();

		if (command.equals(EXIT_CMD)) {
			attemptExit();
		} else if (command.equals(EXPORT_TO_PNG_CMD)) {
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

						int result = fc.showSaveDialog(this);

						if (result == JFileChooser.APPROVE_OPTION) {
							File dir_res = fc.getSelectedFile();

							dir.setText(dir_res.toString());
						}
					}
				}
			}
		}
	}

	private void attemptExit() {
		String title = "Confirm";
		String message = "Are you sure you want to exit the demo?";
		int result =
				JOptionPane.showConfirmDialog(this, message, title, JOptionPane.YES_NO_OPTION,
						JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.YES_OPTION) {
			dispose();
			System.exit(0);
		}
	}

	private void exportTo(String ext) {
		MFileChooser fc =
				new MFileChooser("Directory to store all charts as " + ext + " files");

		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int result = fc.showSaveDialog(this);

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

	class BackgroundSaver extends Thread {
		private long delay = 0;
		private File dir = null;
		private String ext = null;
		private boolean exitOnComplete = false;
		private long interval = 0;
		private int repeat = 1;

		private BackgroundSaver(File dir, String ext, boolean exitOnComplete, long delay,
				int repeat, long interval) {
			this.dir = dir;
			this.ext = ext;
			this.exitOnComplete = exitOnComplete;
			this.delay = delay;
			this.repeat = (repeat < 1) ? 1 : repeat;
			this.interval = interval;
		}

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
							// new File(dir, datetime + (++i) + "_" +
							// chart.getTitle().getText() + ext);
									new File(dir, datetime + (++i) + "_"
											+ chart.getTitle().getText().replaceAll("[^\\d\\w\\s]", "_") + ext);

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
				MonitorMain.this.dispose();
				System.exit(0);
			}
		}
	}

	class DialogDisplay extends Thread implements LineListener {

		private int type = Integer.MIN_VALUE;
		private String title = null;
		private String message = null;
		private Clip clip = null;
		private File soundFile = new File("alarm1.wav");

		public DialogDisplay(String alarmFile) {
			super();
			soundFile = new File(alarmFile);
			try {
				// get and play sound
				Line.Info linfo = new Line.Info(Clip.class);
				Line line = AudioSystem.getLine(linfo);
				clip = (Clip) line;
				clip.addLineListener(this);
				// clip.start();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		public void wakeup(int type, String title, String message) {
			this.type = type;
			this.title = title;
			this.message = message;
			synchronized (this) {
				this.notifyAll();
			}
		}

		public void run() {
			while (true) {
				try {
					synchronized (this) {
						this.wait();
					}
					// Guard from spontaneous awaking from wait.
					if (title != null) {
						if (type == JOptionPane.ERROR_MESSAGE) {
							try {
								AudioInputStream ais = AudioSystem.getAudioInputStream(soundFile);
								clip.open(ais);
								clip.loop(Clip.LOOP_CONTINUOUSLY);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						JOptionPane.showMessageDialog(MonitorMain.this, message, title, type);
						title = null;
						if (type == JOptionPane.ERROR_MESSAGE) {
							try {
								clip.stop();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}
				} catch (InterruptedException ex) {
				}
			}
		}

		public void update(LineEvent le) {
			LineEvent.Type type = le.getType();
			if (type == LineEvent.Type.OPEN) {
				// System.out.println("OPEN");
			} else if (type == LineEvent.Type.CLOSE) {
				// System.out.println("CLOSE");
				// System.exit(0);
			} else if (type == LineEvent.Type.START) {
				// System.out.println("START");
				// playingDialog.setVisible(true);
			} else if (type == LineEvent.Type.STOP) {
				// System.out.println("STOP");
				// playingDialog.setVisible(false);
				clip.close();
			}

		}

	}

}
