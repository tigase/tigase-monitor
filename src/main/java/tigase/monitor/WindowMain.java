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

import tigase.monitor.conf.Configuration;
import tigase.monitor.panel.TigasePanel;
import tigase.monitor.panel.TigasePanelCustom;
import tigase.monitor.panel.TigasePanelMain;
import tigase.monitor.panel.TigasePanelMemory;

import javax.swing.*;
import java.awt.*;

/**
 * @author Artur Hefczyc Created Jun 9, 2011
 */
public class WindowMain extends TigaseWindowAbstract {

    private static final long serialVersionUID = 1L;
    private MonitorMain parent = null;

    public WindowMain(Configuration conf, MonitorMain parent) {
        super(conf, parent);

        panels.put("Live View", new TigasePanelMain(config, mainFrame));

        if (config.isMemoryTabEnabled()) {
            panels.put("Live Memory View", new TigasePanelMemory(config, mainFrame));
        }

        init();

    }

    protected JMenu createMenu() {
        JMenu monitorMenu = super.createMenu();

        addMenuItem(monitorMenu, "Export to PNG...", MonitorMain.EXPORT_TO_PNG_CMD, 'p');
        addMenuItem(monitorMenu, "Export to JPG...", MonitorMain.EXPORT_TO_JPG_CMD, 'j');
        monitorMenu.addSeparator();
        addMenuItem(monitorMenu, "Auto export timer...", MonitorMain.AUTO_EXPORT_TIMER_CMD, 'a');

        return monitorMenu;
    }
}
