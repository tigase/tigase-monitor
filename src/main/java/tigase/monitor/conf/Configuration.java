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
package tigase.monitor.conf;

import java.awt.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created: Sep 9, 2009 6:24:08 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 6 $
 */
public class Configuration {

	private static final String HEIGHT_DEF = "1100";
	private static final String HEIGHT_KEY = "height";
	private static final String JMX_PASSWORD_DEF = "admin_pass";
	private static final String JMX_PASSWORD_KEY = "jmx-pass";
	private static final String JMX_USER_NAME_DEF = "admin";
	private static final String JMX_USER_NAME_KEY = "jmx-user";
	private static final String LOAD_HISTORY_DEF = "true";
	private static final String LOAD_HISTORY_KEY = "load-history";
	private static final String NODES_DEF = "pink:pink.tigase.org,green:green.tigase.org," +
			"blue:blue.tigase.org,white:black1.tigase.org,yellow:black2.tigase.org";
	private static final String NODES_KEY = "nodes";
	private static final String TIMELINE_DEF = "8640";
	private static final String TIMELINE_KEY = "timeline";
	private static final String UPDATERATE_DEF = "10";
	private static final String UPDATERATE_KEY = "updaterate";
	private static final String SERVER_UPDATERATE_KEY = "server-updaterate";
	private static final String SERVER_UPDATERATE_DEF = "10";
	private static final String WIDTH_DEF = "1850";
	private static final String WIDTH_KEY = "width";
	private static final String WARNING_THRESHOLD_KEY = "warning-threshold";
	private static final String WARNING_THRESHOLD_DEF = "50";
	private static final String ERROR_THRESHOLD_KEY = "error-threshold";
	private static final String ERROR_THRESHOLD_DEF = "80";
	private static final String ALARM_FILE_NAME_KEY = "alarm-file";
	private static final String ALARM_FILE_NAME_DEF = "alarm1.wav";
	private static final String CUSTOM_WINDOW_KEY = "custom-window";
	private static final String CUSTOM_WINDOW_DEF = "false";
	private static final String CUSTOM_WINDOW_TIT_KEY = "custom-window.title";
	private static final String CUSTOM_WINDOW_TIT_DEF = "Custom data charts";
	private static final String CUSTOM_CHART_KEY = "custom-window.chart.";
	private static final String MAIN_WINDOW_TIT_KEY = "main-window.title";
	private static final String MAIN_WINDOW_TIT_DEF = "Tigase Monitor";
	private static final String MEMORY_TAB_KEY = "memory-tab";
	private static final String MEMORY_TAB_DEF = "true";
	private static final String MEMORY_UNIT_KEY = MEMORY_TAB_KEY + "." + "memory-display-unit";
	private static final String MEMORY_UNIT_VAL = "KB";
	private static final String APPROXIMATE_TRAFFIC_KEY = "approximate-traffic";
	private static final String APPROXIMATE_TRAFFIC_DEF = "false";
	private static final String DISPLAY_ALARM_KEY = "display-alarm";
	private static final String DISPLAY_ALARM_DEF = "true";

	private static final Queue<String> colours = new LinkedList<>();

	static {
		colours.offer("blue");
		colours.offer("green");
		colours.offer("red");
		colours.offer("white");
		colours.offer("black");
		colours.offer("orange");
		colours.offer("pink");
		colours.offer("cyan");
		colours.offer("dark_gray");
		colours.offer("gray");
		colours.offer("light_gray");
		colours.offer("magenta");
	}

	public enum UNITS {
		KB(1),
		MB(1024),
		GB(1024 * 1024);

		private int factor = 1;

		UNITS(int factor) {
			this.factor = factor;
		}

		public int getFactor() {
			return factor;
		}
	}
	private String alarmFileName = ALARM_FILE_NAME_DEF;
	private boolean approximateTraffic = false;
	private Map<Integer, ChartConfig> customCharts = new LinkedHashMap<>();
	private boolean customWindow = false;
	private String customWindowTitle = CUSTOM_WINDOW_TIT_DEF;
	private boolean displayAlarm = true;
	private float errorTh = 80f;
	private int height = 1100;
	private boolean loadHistory = true;
	private String mainWindowTitle = MAIN_WINDOW_TIT_DEF;
	private boolean memoryTab = false;
	private UNITS memoryUnit = UNITS.KB;
	private List<NodeConfig> nodes = null;
	private Properties props = null;
	private int server_updaterate = 10;
	private int timeline = 24 * 3600;
	private int updaterate = 10;
	private String userName = "admin";
	private String userPass = "admin_pass";
	private float warningTh = 50f;
	private int width = 1850;

	public Configuration(String filename) throws IOException {
		props = new Properties();
		props.load(new FileInputStream(filename));

		updaterate = Integer.parseInt(props.getProperty(UPDATERATE_KEY, UPDATERATE_DEF));
		server_updaterate = Integer.parseInt(props.getProperty(SERVER_UPDATERATE_KEY, SERVER_UPDATERATE_DEF));
		userName = props.getProperty(JMX_USER_NAME_KEY, JMX_USER_NAME_DEF);
		userPass = props.getProperty(JMX_PASSWORD_KEY, JMX_PASSWORD_DEF);

		String[] nodes_arr = props.getProperty(NODES_KEY, NODES_DEF).split(",");

		nodes = new LinkedList<NodeConfig>();

		for (String node : nodes_arr) {
			if (!node.trim().isEmpty()) {
				int port = 9050;
				String[] node_arr = node.split(":");
				String user = userName;
				String colour;
				String hostname;

				if (node_arr.length > 1) {
					colour = node_arr[0];
					hostname = node_arr[1];
				} else {
					colour = (colours.peek() != null ? colours.poll().toString() : "blue");
					hostname = node_arr[0];
				}

				if (node_arr.length > 2) {
					user = node_arr[2];
				}

				String pass = userPass;

				if (node_arr.length > 3) {
					pass = node_arr[3];
				}

				if (node_arr.length > 4) {
					String port_no = node_arr[4];
					try {
						port = Integer.parseInt(port_no);
					} catch (NumberFormatException nfe) {
						port = 9050;
					}
				}

				nodes.add(new NodeConfig(colour, colour, hostname, port, user, pass));
			}
		}

		String height_str = props.getProperty(HEIGHT_KEY, HEIGHT_DEF);

		height = Integer.parseInt(height_str);

		String width_str = props.getProperty(WIDTH_KEY, WIDTH_DEF);

		width = Integer.parseInt(width_str);

		String timeline_str = props.getProperty(TIMELINE_KEY, TIMELINE_DEF);

		timeline = Integer.parseInt(timeline_str);

		String loadHistoryStr = props.getProperty(LOAD_HISTORY_KEY, LOAD_HISTORY_DEF);

		loadHistory = loadHistoryStr.equals("true");

		warningTh = Float.parseFloat(props.getProperty(WARNING_THRESHOLD_KEY, WARNING_THRESHOLD_DEF));
		errorTh = Float.parseFloat(props.getProperty(ERROR_THRESHOLD_KEY, ERROR_THRESHOLD_DEF));
		alarmFileName = props.getProperty(ALARM_FILE_NAME_KEY, ALARM_FILE_NAME_DEF);
		customWindow = Boolean.parseBoolean(props.getProperty(CUSTOM_WINDOW_KEY, CUSTOM_WINDOW_DEF));
		memoryTab = Boolean.parseBoolean(props.getProperty(MEMORY_TAB_KEY, MEMORY_UNIT_VAL));

		try {
			memoryUnit = UNITS.valueOf(props.getProperty(MEMORY_UNIT_KEY, MEMORY_UNIT_VAL));
		} catch (IllegalArgumentException e) {
			memoryUnit = UNITS.KB;
			System.out.println("Wrong memory UNIT, using default: " + memoryUnit);
		}
		approximateTraffic = Boolean.parseBoolean(props.getProperty(APPROXIMATE_TRAFFIC_KEY, APPROXIMATE_TRAFFIC_DEF));
		customWindowTitle = props.getProperty(CUSTOM_WINDOW_TIT_KEY, CUSTOM_WINDOW_TIT_DEF);
		mainWindowTitle = props.getProperty(MAIN_WINDOW_TIT_KEY, MAIN_WINDOW_TIT_DEF);
		displayAlarm = Boolean.parseBoolean(props.getProperty(DISPLAY_ALARM_KEY, DISPLAY_ALARM_DEF));

		for (String key : props.stringPropertyNames()) {
			if (key.startsWith(CUSTOM_CHART_KEY)) {
				String prop_full_key = key.substring(CUSTOM_CHART_KEY.length());
				int idx = prop_full_key.indexOf('.');
				String prop_idx_str = prop_full_key.substring(0, idx);
				Integer prop_idx = Integer.decode(prop_idx_str);
				String prop_key = prop_full_key.substring(idx + 1);
				ChartConfig chartConfig = customCharts.get(prop_idx);
				if (chartConfig == null) {
					chartConfig = new ChartConfig();
					customCharts.put(prop_idx, chartConfig);
				}
				chartConfig.addProperty(prop_key, props.getProperty(key));
			}
		}
	}

	public boolean isApproximateTraffic() {
		return approximateTraffic;
	}

	public float getWarningThreshold() {
		return warningTh;
	}

	public float getErrorThreshold() {
		return errorTh;
	}

	public int getHeight() {
		return height;
	}

	public boolean getLoadHistory() {
		return loadHistory;
	}

	public List<NodeConfig> getNodeConfigs() {
		return nodes;
	}

	public int getTimeline() {
		return timeline;
	}

	public int getUpdaterate() {
		return updaterate;
	}

	public int getServerUpdaterate() {
		return server_updaterate;
	}

	public int getWidth() {
		return width;
	}

	public String getAlarmFileName() {
		return alarmFileName;
	}

	public boolean customWindow() {
		return customWindow;
	}

	public String getCustomTitle() {
		return customWindowTitle;
	}

	public String getMainTitle() {
		return mainWindowTitle;
	}

	public UNITS getMemoryUnit() {
		return memoryUnit;
	}

	public ChartConfig getChartConfig(int i) {
		ChartConfig conf = customCharts.get(i);
		if (conf == null) {
			conf = new ChartConfig();
		}
		return conf;
	}

	public boolean isMemoryTabEnabled() {
		return memoryTab;
	}

	public boolean displayAlarm() {
		return displayAlarm;
	}
}
