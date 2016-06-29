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

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tigase.monitor.conf.Configuration;

/**
 * @author Artur Hefczyc Created Jun 9, 2011
 */
public class CustomWindow extends JFrame {

	private static final long serialVersionUID = 1L;
	private CustomModuleImpl moduleImpl = null;
	private MonitorMain parent = null;

	public CustomWindow(Configuration config, MonitorMain parent) {
		super(config.getCustomTitle());
		this.parent = parent;
		moduleImpl = new CustomModuleImpl();
		moduleImpl.init(config, this);
		setContentPane(createContent());
		//setJMenuBar(createMenuBar());
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

}
