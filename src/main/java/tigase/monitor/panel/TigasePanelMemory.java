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

package tigase.monitor.panel;

import tigase.monitor.MonitorMain;
import tigase.monitor.conf.Configuration;
import tigase.monitor.conf.Configuration.UNITS;
import tigase.monitor.conf.NodeConfig;
import tigase.stats.JavaJMXProxyOpt;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static tigase.monitor.panel.DataChangeListener.*;

public class TigasePanelMemory extends TigasePanel {

    private UNITS unitFactor = Configuration.UNITS.KB;

    public TigasePanelMemory(Configuration conf, JFrame mainFrame) {
        super(conf, mainFrame);

        unitFactor = conf.getMemoryUnit();

        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel row1 = addRow(this);

        int width, height;
        width = Math.round(config.getWidth() - 5);
        height = Math.round(config.getHeight() * row1_height_factor);

        // We want detailed statistics, and max HEAP size may change, hence displaying usage vs max is better.
        TigaseMonitorLine monitorTotalHeap = addChart(config, "Total HEAP usage", unitFactor.toString(), 100, height, width, row1, HEAP_TOTAL_USAGE_USED, HEAP_TOTAL_USAGE_MAX);

        JPanel row2 = addRow(this);

        width = Math.round(config.getWidth() * row2_width_factor - 5);
        height = Math.round(config.getHeight() * row2_height_factor);

        TigaseMonitorLine monitorEdenUsage = addChart(config, "Eden usage", unitFactor.toString(), 50, height, width, row2, HEAP_EDEN_USAGE_USED, HEAP_EDEN_USAGE_MAX);

        TigaseMonitorLine monitorSurvivorUsage = addChart(config, "Survivor usage", unitFactor.toString(), 20, height, width, row2, HEAP_SURVIVOR_USAGE_USED, HEAP_SURVIVOR_USAGE_MAX);

        TigaseMonitorLine monitorTenuredUsage = addChart(config, "Tenured usage", unitFactor.toString(), 100, height, width, row2, HEAP_OLD_USAGE_USED, HEAP_OLD_USAGE_MAX);


        JPanel row3 = addRow(this);

        int cnt = 0;

        List<NodeConfig> nodeConfigs = config.getNodeConfigs();

        for (NodeConfig nodeConfig : nodeConfigs) {
            monitorTotalHeap.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
            monitorSurvivorUsage.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
            monitorTenuredUsage.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
            monitorEdenUsage.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());


            if (cnt < 5) {
                TigaseTextMemoryMonitor textMonitor =
                        new TigaseTextMemoryMonitor(nodeConfig.getDescription(), nodeConfigs,
                                config.getUpdaterate(), config.getServerUpdaterate(), unitFactor);

                new DataChange(textMonitor, false, false, textMonitor.getMetricsKeys()) {
                    public void update(String id, JavaJMXProxyOpt bean) {
                        monitor.update(id, bean);
                    }
                };

                MonitorMain.monitors.add(textMonitor);
                Dimension dim = new Dimension(width, height);
                textMonitor.getPanel().setPreferredSize(dim);
                row3.add(textMonitor.getPanel());

                if (cnt < 4) {
                    row3.add(Box.createRigidArea(new Dimension(5, 0)));
                }

                ++cnt;
            }
        }
    }

    @Override
    protected TigaseMonitorLine addChart(Configuration config, String xTitle, String yTitle, double yAxisMax, int height, int width, JPanel row, String... dataIds) {
        TigaseMonitorLine monitor =
                new TigaseMonitorLine(xTitle, yTitle, yAxisMax, false, false, true,
                        config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate()) {
                    @Override
                    protected int getDivisionFactor() {
                        return unitFactor.getFactor();
                    }
                };
        new DataChange(monitor, false, config.getLoadHistory(), dataIds);

        MonitorMain.addMonitor(monitor);

        monitor.getPanel().setPreferredSize(new Dimension(width, height));
        row.add(monitor.getPanel());
        return monitor;

    }
}
