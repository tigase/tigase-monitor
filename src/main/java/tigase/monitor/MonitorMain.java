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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.StandardChartTheme;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author kobit
 */
public class MonitorMain extends ApplicationFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	
	private static final String EXIT_CMD = "exit";
	private static final String PROP_FILENAME_DEF = "etc/monitor.properties";
	private static final String PROP_FILENAME_KEY = "--init";

	private ClientModuleImpl moduleImpl = null;

	public MonitorMain(String configFile) {
		super("Tigase Monitor");
		//System.out.println(sun.net.InetAddressCachePolicy.get());
		moduleImpl = new ClientModuleImpl();
		moduleImpl.init(configFile, this);
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
		StandardChartTheme theme =
				(StandardChartTheme)StandardChartTheme.createDarknessTheme();
		theme.setChartBackgroundPaint(Color.DARK_GRAY);
		theme.setPlotBackgroundPaint(new Color(0.15f, 0.15f, 0.15f));
		ChartFactory.setChartTheme(theme);
		MonitorMain app = new MonitorMain(propFile);
		app.pack();
		RefineryUtilities.centerFrameOnScreen(app);
		app.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		String command = e.getActionCommand();
		if (command.equals(EXIT_CMD)) {
			attemptExit();
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

}
