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

import java.awt.*;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import tigase.monitor.conf.Configuration;
import tigase.monitor.panel.TigasePanel;
import tigase.monitor.panel.TigasePanelCustom;

/**
 * @author Artur Hefczyc Created Jun 9, 2011
 */
public class WindowCustom extends TigaseWindowAbstract {

    private static final long serialVersionUID = 1L;
    private MonitorMain parent = null;

    public WindowCustom(Configuration config, MonitorMain parent) {
        super(config,parent);

        TigasePanel panel = new TigasePanelCustom(config, this);
        panels.put("Custom Panel", panel);

        init();

    }



}
