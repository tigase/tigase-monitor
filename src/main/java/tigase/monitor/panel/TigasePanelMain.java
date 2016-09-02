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
import tigase.monitor.conf.NodeConfig;
import tigase.stats.JavaJMXProxyOpt;

import javax.swing.*;
import java.awt.*;
import java.util.*;

import static tigase.monitor.panel.DataChangeListener.*;

public class TigasePanelMain extends TigasePanel {


    public TigasePanelMain(Configuration conf, JFrame mainFrame) {
        super(conf, mainFrame);


        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        setBackground(Color.DARK_GRAY);
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        JPanel row1 = addRow(this);

        ConnectionsDistribution distr = new ConnectionsDistribution();
        new DataChange(distr, false, false, C2S_CONNECTIONS, BOSH_CONNECTIONS, WS2S_CONNECTIONS) {

            public void update(String id, JavaJMXProxyOpt bean) {
                monitor.update(id, bean);
            }

        };

        MonitorMain.monitors.add(distr);

        int width_distr = Math.round(config.getHeight() * row1_height_factor);
        int height = Math.round(config.getHeight() * row1_height_factor);



        Dimension dim;
        dim = new Dimension(width_distr, height);

        distr.getPanel().setPreferredSize(dim);
        row1.add(distr.getPanel());

        int width;
        width = Math.round((config.getWidth() - width_distr) * row1_width_factor - 10);
        height = Math.round(config.getHeight() * row1_height_factor);

        TigaseMonitorLine cpu =
                new TigaseMonitorLine("CPU Load", "CPU %", 100, false, false, true,
                        config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
        new DataChange(cpu, false, true, CPU_USAGE);

        MonitorMain.monitors.add(cpu);

        dim = new Dimension(width, height);
        cpu.getPanel().setPreferredSize(dim);
        row1.add(cpu.getPanel());

        TigaseMonitorLine mem =
                new TigaseMonitorLine("Memory Usage", "MEM %", 100, false, false, true,
                        config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
        new DataChange(mem, false, true, HEAP_USAGE, NONHEAP_USAGE, HEAP_REGION_NAME);

        MonitorMain.monitors.add(mem);
        dim = new Dimension(width, height);
        mem.getPanel().setPreferredSize(dim);
        row1.add(mem.getPanel());
        width = Math.round(config.getWidth() * row2_width_factor - 5);
        height = Math.round(config.getHeight() * row2_height_factor);

        JPanel row2 = addRow(this);

        TigaseMonitorLine conns =
                new TigaseMonitorLine("Connections", "Connections per node", 10, true, false, false,
                        config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
        new DataChange(conns, false, true, C2S_CONNECTIONS, WS2S_CONNECTIONS, BOSH_CONNECTIONS, S2S_CONNECTIONS);

        MonitorMain.monitors.add(conns);
        dim = new Dimension(width, height);
        conns.getPanel().setPreferredSize(dim);
        row2.add(conns.getPanel());

        TigaseMonitorLine sm =
                new TigaseMonitorLine("SM Traffic", "Packets/sec", 50, true, true, conf.isApproximateTraffic(),
                        config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
        new DataChange(sm, true, true, SM_TRAFFIC_R, SM_TRAFFIC_S);

        MonitorMain.monitors.add(sm);
        dim = new Dimension(width, height);
        sm.getPanel().setPreferredSize(dim);
        row2.add(sm.getPanel());

        TigaseMonitorLine cl =
                new TigaseMonitorLine("Cluster Traffic", "Packets/sec", 50, true, true, conf.isApproximateTraffic(),
                        config.getTimeline(), config.getUpdaterate(), config.getServerUpdaterate());
        new DataChange(cl, true, true, CL_TRAFFIC_R, CL_TRAFFIC_S);

        MonitorMain.monitors.add(cl);
        dim = new Dimension(width, height);
        cl.getPanel().setPreferredSize(dim);
        row2.add(cl.getPanel());

        JPanel row3 = addRow(this);

        width = Math.round(config.getWidth() * row3_width_factor - 5);
        height = Math.round(config.getHeight() * row3_height_factor - 100);

        int cnt = 0;
        java.util.List<NodeConfig> nodeConfigs = config.getNodeConfigs();

        for (NodeConfig nodeConfig : nodeConfigs) {
            distr.setValue(nodeConfig.getDescription(), 0);
            distr.setColor(nodeConfig.getDescription(), nodeConfig.getColor());
            cpu.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
            mem.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
            sm.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
            cl.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());
            conns.addSeries(nodeConfig.getDescription(), nodeConfig.getColor());

            if (cnt < 5) {
                TigaseTextMonitor textMonitor =
                        new TigaseTextMonitor(nodeConfig.getDescription(), nodeConfigs,
                                config.getUpdaterate(), config.getServerUpdaterate());

                new DataChange(textMonitor, false, false, textMonitor.getMetricsKeys()) {
                    public void update(String id, JavaJMXProxyOpt bean) {
                        monitor.update(id, bean);
                    }
                };

                MonitorMain.monitors.add(textMonitor);
                dim = new Dimension(width, height);
                textMonitor.getPanel().setPreferredSize(dim);
                row3.add(textMonitor.getPanel());

                if (cnt < 4) {
                    row3.add(Box.createRigidArea(new Dimension(5, 0)));
                }

                ++cnt;
            }
        }

    }
}
