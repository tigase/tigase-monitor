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

import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.NodeConfig;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Created by wojtek on 19/07/16.
 */
public class TigasePanelCustom
		extends TigasePanel {

	public TigasePanelCustom(Configuration conf, JFrame mainFrame) {
		super(conf, mainFrame);

		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBackground(Color.DARK_GRAY);
		setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

		JPanel row1 = addRow(this);

		int width = Math.round(config.getWidth() * row1_width_factor - 5);
		int height = Math.round(config.getHeight() * row1_height_factor);

		TigaseMonitorLine custom1 = addCustomChart(config, height, width, row1, 1);

		TigaseMonitorLine custom2 = addCustomChart(config, height, width, row1, 2);

		JPanel row2 = addRow(this);

		width = Math.round(config.getWidth() * row2_width_factor - 5);
		height = Math.round(config.getHeight() * row2_height_factor);

		TigaseMonitorLine custom3 = addCustomChart(config, height, width, row2, 3);

		TigaseMonitorLine custom4 = addCustomChart(config, height, width, row2, 4);

		TigaseMonitorLine custom5 = addCustomChart(config, height, width, row2, 5);

		List<NodeConfig> nodeConfigs = config.getNodeConfigs();

		for (NodeConfig nodeConfig : nodeConfigs) {
			custom1.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			custom2.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			custom4.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			custom5.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
			custom3.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
		}

	}

}
