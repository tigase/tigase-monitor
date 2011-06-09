/*
 * Tigase Jabber/XMPP Server
 * Copyright (C) 2004-2011 "Artur Hefczyc" <artur.hefczyc@tigase.org>
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
 * $Rev: 2411 $
 * Last modified by $Author: kobit $
 * $Date: 2010-10-27 20:27:58 -0600 (Wed, 27 Oct 2010) $
 * 
 */
package tigase.monitor.conf;

import java.util.ArrayList;
import java.util.Properties;

/**
 * @author Artur Hefczyc Created Jun 9, 2011
 */
public class ChartConfig {
	private static final String CUSTOM_CHART_STRING_DEF = "Not set";
	private static final String CUSTOM_CHART_INT_DEF = "100";
	private static final String CUSTOM_CHART_BOOL_DEF = "false";
	private static final String CUSTOM_CHART_XTIT_KEY = "x-title";
	private static final String CUSTOM_CHART_YTIT_KEY = "y-title";
	private static final String CUSTOM_CHART_MAXY_KEY = "max-x";
	private static final String CUSTOM_CHART_CNT_TOTALS_KEY = "count-totals";
	private static final String CUSTOM_CHART_CNT_DELTA_KEY = "count-delta";
	private static final String CUSTOM_CHART_SERIES_KEY = "series.";

	private Properties props = new Properties();
	private ArrayList<String> series = new ArrayList<String>();

	/**
	 * @param prop_key
	 * @param property
	 */
	public void addProperty(String prop_key, String property) {
		props.put(prop_key, property);
		if(prop_key.startsWith(CUSTOM_CHART_SERIES_KEY)) {
			series.add(property);
		}
	}

	public String getXTitle() {
		return props.getProperty(CUSTOM_CHART_XTIT_KEY, CUSTOM_CHART_STRING_DEF);
	}

	public String getYTitle() {
		return props.getProperty(CUSTOM_CHART_YTIT_KEY, CUSTOM_CHART_STRING_DEF);
	}

	public int getMaxY() {
		String val_str = props.getProperty(CUSTOM_CHART_MAXY_KEY, CUSTOM_CHART_INT_DEF);
		return Integer.parseInt(val_str);
	}

	/**
	 * @return
	 */
	public boolean countTotals() {
		String val_str = props.getProperty(CUSTOM_CHART_CNT_TOTALS_KEY, CUSTOM_CHART_BOOL_DEF);
		return Boolean.parseBoolean(val_str);
	}
	
	public boolean countDelta() {
		String val_str = props.getProperty(CUSTOM_CHART_CNT_DELTA_KEY, CUSTOM_CHART_BOOL_DEF);
		return Boolean.parseBoolean(val_str);
	}
	
	public String[] getSeries() {
		return series.toArray(new String[series.size()]);
	}

}