/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2016 "Tigase, Inc." <office@tigase.com>
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

import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RefineryUtilities;
import tigase.monitor.conf.Configuration;
import tigase.monitor.panel.TigasePanel;
import tigase.monitor.panel.TigasePanelMain;
import tigase.monitor.panel.TigasePanelMemory;

import javax.imageio.plugins.jpeg.JPEGHuffmanTable;
import javax.swing.*;
import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by wojtek on 19/07/16.
 */
public abstract class TigaseWindowAbstract extends ApplicationFrame {

    protected Configuration config = null;
    protected MonitorMain mainFrame = null;
    protected Dimension preferredSize;
    protected JMenu menu = null;
    protected boolean initiated = false;
    protected Map<String, JPanel> panels = new LinkedHashMap<>();

    protected static final String EXIT_CMD = "exit";


    public JMenu getJMenu() {
        return menu;
    }

    public Map<String, JPanel> getJPanels() {
        return panels;
    }

    public void addPanels (String title, JPanel panel) {
        panels.put(title, panel);
        init();
    }

    public Dimension getPreferredSize() {
        Dimension dimension = new Dimension(config.getWidth(), config.getHeight());
        return dimension;
    }

    public TigaseWindowAbstract(Configuration conf, MonitorMain parent) {
        super(conf.getMainTitle());
        this.config = conf;
        this.mainFrame = parent;
        this.menu = createMenu();
        preferredSize = new Dimension(config.getWidth(), config.getHeight());
    }

    public void init() {
        setJMenuBar(createMenuBar());
        setContentPane(createContent());
        setPreferredSize(preferredSize);

        pack();
        RefineryUtilities.centerFrameOnScreen(this);
        setVisible(true);

        initiated = true;
    }

    protected JMenu createMenu() {
        JMenu monitorMenu = new JMenu("Monitor", true);

        monitorMenu.setMnemonic('M');


        return monitorMenu;
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        JMenu monitorMenu = getJMenu();

        if (!initiated) {
            monitorMenu.addSeparator();
            addMenuItem(monitorMenu, "Exit", MonitorMain.EXIT_CMD, 'x');
        }

        menuBar.add(monitorMenu);
        return menuBar;
    }


    protected void addMenuItem(JMenu monitorMenu, String text, String exportToPngCmd, char accelerator) {
        JMenuItem item;
        item = new JMenuItem(text, accelerator);
        item.setActionCommand(exportToPngCmd);
        item.addActionListener(mainFrame);
        monitorMenu.add(item);
    }


    protected JComponent createContent() {

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Color.DARK_GRAY);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setBackground(Color.DARK_GRAY);

        for (Map.Entry<String, JPanel> panelEntry : panels.entrySet()) {
            tabs.add(panelEntry.getKey(), panelEntry.getValue());
        }
        content.add(tabs);
        return content;
    }


}
