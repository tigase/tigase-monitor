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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
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
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.UIManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;
import tigase.monitor.panel.CPUMonitor;
import tigase.monitor.panel.ClusterTrafficMonitor;
import tigase.monitor.panel.ConnectionsDistribution;
import tigase.monitor.panel.ConnectionsMonitor;
import tigase.monitor.panel.MemMonitor;
import tigase.monitor.panel.SMTrafficMonitor;
import tigase.monitor.panel.TigaseMonitor;
import tigase.monitor.panel.TigaseTextMonitor;
import tigase.monitor.util.MFileChooser;
import tigase.stats.JavaJMXProxy;

/**
 *
 * @author kobit
 */
public class Main extends ApplicationFrame implements ActionListener {

	private static final String PROP_FILENAME_DEF = "init.properties";
	private static final String PROP_FILENAME_KEY = "--init";

	private static final String EXPORT_TO_PNG_CMD = "export-to-png";
	private static final String EXPORT_TO_JPG_CMD = "export-to-jpg";
	private static final String AUTO_EXPORT_TIMER_CMD = "auto-export-timer";
	private static final String EXIT_CMD = "exit";

	private static List<JavaJMXProxy> proxies = new LinkedList<JavaJMXProxy>();
	private static List<TigaseMonitor> monitors = new LinkedList<TigaseMonitor>();
	private static Timer timer = new Timer("Statistics updater", true);
	private float row1_height_factor = 0.33f;
	private float row1_width_factor = 0.5f;
	private float row2_height_factor = 0.30f;
	private float row2_width_factor = 0.33f;
	private float row3_height_factor = 0.36f;
	private float row3_width_factor = 0.2f;


	public Main(Configuration config) {
		super("Tigase Monitor");
		setContentPane(createContent(config));
		setJMenuBar(createMenuBar());
	}

	private JComponent createContent(Configuration config) {
		StandardChartTheme theme = 
				(StandardChartTheme)StandardChartTheme.createDarknessTheme();
		theme.setChartBackgroundPaint(Color.DARK_GRAY);
		//theme.setBaselinePaint(Color.LIGHT_GRAY);
		theme.setPlotBackgroundPaint(new Color(0.15f, 0.15f, 0.15f));
		ChartFactory.setChartTheme(theme);

		JPanel content = new JPanel(new BorderLayout());
		content.setBackground(Color.DARK_GRAY);

//		GridBagLayout gbl = new GridBagLayout();
//		GridBagConstraints c = new GridBagConstraints();
//		c.fill = GridBagConstraints.BOTH;
//		c.weighty = 1;
//		c.weightx = 1;

		//JPanel liveView = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 5));
		JPanel liveView = new JPanel(null);
		liveView.setLayout(new BoxLayout(liveView, BoxLayout.PAGE_AXIS));
		liveView.setBackground(Color.DARK_GRAY);
		liveView.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JPanel row1 = new JPanel(null);
		row1.setLayout(new BoxLayout(row1, BoxLayout.LINE_AXIS));
		row1.setBackground(Color.DARK_GRAY);
		liveView.add(row1);

		ConnectionsDistribution distr = new ConnectionsDistribution();
		monitors.add(distr);
		int width_distr = Math.round(config.getHeight() * row1_height_factor);
		int height = Math.round(config.getHeight() * row1_height_factor);
		Dimension dim = new Dimension(width_distr, height);
		distr.getPanel().setPreferredSize(dim);
		row1.add(distr.getPanel());

//		c.gridwidth = 3;
//		c.gridx = 0;
//		c.gridy = 0;
//		liveView.add(distr.getPanel(), c);

		CPUMonitor cpu = new CPUMonitor();
		monitors.add(cpu);
		int width = Math.round((config.getWidth() - width_distr) * row1_width_factor - 10);
		height = Math.round(config.getHeight() * row1_height_factor);
		dim = new Dimension(width, height);
		cpu.getPanel().setPreferredSize(dim);
		row1.add(cpu.getPanel());

//		c.gridwidth = 6;
//		c.gridx = GridBagConstraints.RELATIVE;
//		c.gridy = 0;
//		liveView.add(cpu.getPanel(), c);

		MemMonitor mem = new MemMonitor();
		monitors.add(mem);
		dim = new Dimension(width, height);
		mem.getPanel().setPreferredSize(dim);
		row1.add(mem.getPanel());

//		c.gridx = GridBagConstraints.RELATIVE;
//		c.gridy = 0;
//		c.gridwidth = GridBagConstraints.REMAINDER;
//		liveView.add(mem.getPanel(), c);

		width = Math.round(config.getWidth() * row2_width_factor - 5);
		height = Math.round(config.getHeight() * row2_height_factor);

		JPanel row2 = new JPanel(null);
		row2.setLayout(new BoxLayout(row2, BoxLayout.LINE_AXIS));
		row2.setBackground(Color.DARK_GRAY);
		liveView.add(row2);

		ConnectionsMonitor conns = new ConnectionsMonitor();
		monitors.add(conns);
		dim = new Dimension(width, height);
		conns.getPanel().setPreferredSize(dim);
		row2.add(conns.getPanel());

//		c.gridwidth = 5;
//		c.gridx = 0;
//		c.gridy = 1;
//		liveView.add(conns.getPanel(), c);

		SMTrafficMonitor sm = new SMTrafficMonitor();
		monitors.add(sm);
		dim = new Dimension(width, height);
		sm.getPanel().setPreferredSize(dim);
		row2.add(sm.getPanel());

//		c.gridx = GridBagConstraints.RELATIVE;
//		c.gridy = 1;
//		liveView.add(sm.getPanel(), c);

		ClusterTrafficMonitor cl = new ClusterTrafficMonitor();
		monitors.add(cl);
		dim = new Dimension(width, height);
		cl.getPanel().setPreferredSize(dim);
		row2.add(cl.getPanel());

//		c.gridx = GridBagConstraints.RELATIVE;
//		c.gridy = 1;
//		c.gridwidth = GridBagConstraints.REMAINDER;
//		liveView.add(cl.getPanel(), c);

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
			cpu.addSeries(nodeConfig.getDescription());
			cpu.setColor(nodeConfig.getDescription(), nodeConfig.getColor());
			mem.addSeries(nodeConfig.getDescription());
			mem.setColor(nodeConfig.getDescription(), nodeConfig.getColor());
			sm.addSeries(nodeConfig.getDescription());
			sm.setColor(nodeConfig.getDescription(), nodeConfig.getColor());
			cl.addSeries(nodeConfig.getDescription());
			cl.setColor(nodeConfig.getDescription(), nodeConfig.getColor());
			conns.addSeries(nodeConfig.getDescription());
			conns.setColor(nodeConfig.getDescription(), nodeConfig.getColor());
			if (cnt < 5) {
				TigaseTextMonitor textMonitor =
						new TigaseTextMonitor(nodeConfig.getDescription(), nodeConfigs);
				monitors.add(textMonitor);
				dim = new Dimension(width, height);
				textMonitor.getPanel().setPreferredSize(dim);
				row3.add(textMonitor.getPanel());
				if (cnt < 4) {
					row3.add(Box.createRigidArea(new Dimension(5,0)));
				}
				++cnt;
			}
		}

		JTabbedPane tabs = new JTabbedPane();
		tabs.setBackground(Color.DARK_GRAY);
		tabs.add("Live view", liveView);
		content.add(tabs);
		return content;
	}

	private JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();

		JMenu fileMenu = new JMenu("File", true);
		fileMenu.setMnemonic('F');

		JMenuItem item = null;
//		JMenuItem item = new JMenuItem("Export to PDF...", 'p');
//		item.setActionCommand(EXPORT_TO_PDF_CMD);
//		item.addActionListener(this);
//		fileMenu.add(item);

		item = new JMenuItem("Export to PNG...", 'p');
		item.setActionCommand(EXPORT_TO_PNG_CMD);
		item.addActionListener(this);
		fileMenu.add(item);

		item = new JMenuItem("Export to JPG...", 'p');
		item.setActionCommand(EXPORT_TO_JPG_CMD);
		item.addActionListener(this);
		fileMenu.add(item);

		fileMenu.addSeparator();

		item = new JMenuItem("Auto export timer...", 'p');
		item.setActionCommand(AUTO_EXPORT_TIMER_CMD);
		item.addActionListener(this);
		fileMenu.add(item);

		fileMenu.addSeparator();

		JMenuItem exitItem = new JMenuItem("Exit", 'x');
		exitItem.setActionCommand(EXIT_CMD);
		exitItem.addActionListener(this);
		fileMenu.add(exitItem);

		menuBar.add(fileMenu);
		return menuBar;
	}

	/**
     * @param args the command line arguments
     */
	public static void main(String[] args) {
			
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		String propFile = PROP_FILENAME_DEF;
		if (args != null && args.length > 1) {
			if (args[0].equals(PROP_FILENAME_KEY)) {
				propFile = args[1];
			}
		}
			
		Configuration config = null;
		try {
			config = new Configuration(propFile);
		} catch (Exception e) {
			System.out.println("init.properties file missing, using defaults.");
		}

		try {
			UIManager.setLookAndFeel(
					"com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception e) {
			// ... otherwise just use the system look and feel
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		Main app = new Main(config);
		app.setPreferredSize(new Dimension(config.getWidth(), config.getHeight()));
		app.pack();
		RefineryUtilities.centerFrameOnScreen(app);
		app.setVisible(true);
		List<NodeConfig> nodeConfigs = config.getNodeConfigs();
		for (NodeConfig nodeConfig : nodeConfigs) {
			JavaJMXProxy proxy = new JavaJMXProxy(nodeConfig.getDescription(),
					nodeConfig.getHostname(), nodeConfig.getPort(),
					nodeConfig.getUserName(),
					nodeConfig.getPassword(), 1000, 1000, config.getLoadHistory());
			proxies.add(proxy);
			for (TigaseMonitor monitor : monitors) {
				proxy.addJMXProxyListener(monitor);
			}
			proxy.start();
		}
		timer.schedule(new TimerTask() {
				@Override
				public void run() {
					for (JavaJMXProxy javaJMXProxy : proxies) {
						if (javaJMXProxy.isInitialized()) {
							for (TigaseMonitor monitor : monitors) {
								if (monitor.isReady(javaJMXProxy.getId())) {
									monitor.update(javaJMXProxy.getId(), javaJMXProxy);
								}
							}
						}
					}
				}
		}, 1000, 1000);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals(EXPORT_TO_PNG_CMD)) {
			exportTo(".png");
		} else if (command.equals(EXPORT_TO_JPG_CMD)) {
			exportTo(".jpg");
		} else if (command.equals(AUTO_EXPORT_TIMER_CMD)) {
			autoExportTimer();
		} else if (command.equals(EXIT_CMD)) {
			attemptExit();
		} else if (command.equals("dir")) {
			MFileChooser fc = new MFileChooser("Directory to store all charts as images");
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fc.showSaveDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File dir_res = fc.getSelectedFile();
				dir.setText(dir_res.toString());
			}
		}
	}

	private void attemptExit() {
		String title = "Confirm";
		String message = "Are you sure you want to exit the demo?";
		int result = JOptionPane.showConfirmDialog(
				this, message, title, JOptionPane.YES_NO_OPTION,
				JOptionPane.QUESTION_MESSAGE);
		if (result == JOptionPane.YES_OPTION) {
			dispose();
			System.exit(0);
		}
	}

	private void exportTo(String ext) {
		MFileChooser fc = new MFileChooser("Directory to store all charts as " +
				ext + " files");
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = fc.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File dir_res = fc.getSelectedFile();
			saveAllTo(dir_res, ext, false, 0, 1, 0);
		}
	}

	private void saveAllTo(File dir, String ext, boolean exitOnComplete, long delay,
			int repeat, long interval) {
		Thread thread = new BackgroundSaver(dir, ext, exitOnComplete, delay, repeat, interval);
		thread.start();
	}

	private JTextField dir = null;

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
		int result = JOptionPane.showOptionDialog(this,
				new Object[]{message, label, delay, 
					repeat_lab, repeat,
					interval_lab, interval,
					pngExport, jpgExport, exitOnComplete,
					labelDir, dirPanel},
				"Login", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.QUESTION_MESSAGE,
				null, null, null);
		if (result == JOptionPane.OK_OPTION) {
			long delayLong = -1;
			int repeatInt = 1;
			long intervalLong = -1;
			try {
				delayLong = Long.parseLong(delay.getText()) * 1000;
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, 
						"Incorrect delay value: " + delay.getText(), "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			if (repeat.getText() != null && !repeat.getText().isEmpty()) {
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
			if (dir.getText() == null || dir.getText().isEmpty()) {
				JOptionPane.showMessageDialog(this,
						"Please enter valid directory name.", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			boolean exit = exitOnComplete.isSelected();
			if (pngExport.isSelected()) {
				saveAllTo(new File(dir.getText()), ".png", exit && !jpgExport.isSelected(), 
						delayLong, repeatInt, intervalLong);
			}
			if (jpgExport.isSelected()) {
				saveAllTo(new File(dir.getText()), ".jpg", exit, 
						delayLong + 1000, repeatInt, intervalLong);
			}
		}
	}

	class BackgroundSaver extends Thread {

		private File dir = null;
		private String ext = null;
		private boolean exitOnComplete = false;
		private long delay = 0;
		private int repeat = 1;
		private long interval = 0;

		private BackgroundSaver(File dir, String ext, boolean exitOnComplete,
				long delay, int repeat, long interval) {
			this.dir = dir;
			this.ext = ext;
			this.exitOnComplete = exitOnComplete;
			this.delay = delay;
			this.repeat = repeat < 1 ? 1 : repeat;
			this.interval = interval;
		}

		@Override
		public void run() {
			if (delay > 0) {
				try {
					sleep(delay);
				} catch (Exception e) {	}
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
							File file = new File(dir, datetime + (++i) + "_" +
									chart.getTitle().getText() + ext);
							try {
								JFreeChart ch = (JFreeChart)chart.clone();
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
				if (interval > 0 && j+1 < repeat) {
					try {
						sleep(interval);
					} catch (Exception e) {	}
				}
			}
			if (exitOnComplete) {
				dispose();
				System.exit(0);
			}
		}

	}

}
