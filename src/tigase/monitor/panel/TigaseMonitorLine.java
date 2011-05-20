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
 * $Rev: 11 $
 * Last modified by $Author: kobit $
 * $Date: 2009-10-03 01:18:34 +0100 (Sat, 03 Oct 2009) $
 */

package tigase.monitor.panel;

//~--- non-JDK imports --------------------------------------------------------

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.RangeType;
import org.jfree.data.time.RegularTimePeriod;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.ui.RectangleInsets;

//~--- JDK imports ------------------------------------------------------------

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

//~--- classes ----------------------------------------------------------------

/**
 * Created: Sep 9, 2009 11:47:00 PM
 *
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 11 $
 */
public abstract class TigaseMonitorLine extends TigaseMonitor {
	private long max = 0;

	// By default max item age is 1 day
	private long maxItemAge = 60 * 60 * 24;
	private int max_history_size = 14400;
	private JPanel panel = null;
	private int refresh_cnt = 0;

	// private XYSeriesCollection data = null;
	private Map<String, TimeSeries> series_map = new LinkedHashMap<String, TimeSeries>(4096);
	private TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
	private Map<String, Number> last_vals = new LinkedHashMap<String, Number>(4096);
	private List<JFreeChart> charts = new LinkedList<JFreeChart>();
	private DateAxis xAxis = null;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	//~--- constructors ---------------------------------------------------------

	/**
	 * Constructs ...
	 *
	 */
	public TigaseMonitorLine() {

//  data = new XYSeriesCollection();
//  XYLineAndShapeRenderer renderer = new XYSplineRenderer();
//  renderer.setBaseShapesVisible(false);
//  JFreeChart chart1 = createChart(renderer);
//  charts.add(chart1);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		renderer.setBaseShapesVisible(false);

		JFreeChart chart2 = createChart(renderer);

		charts.add(chart2);

//  ChartPanel chartPanel1 = new ChartPanel(chart1);
		ChartPanel chartPanel2 = new ChartPanel(chart2);

//  JTabbedPane tabs = new JTabbedPane();
//  tabs.add("Splines", chartPanel1);
//  tabs.add("Lines", chartPanel2);
		panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.DARK_GRAY);

//  panel.add(tabs);
		panel.add(chartPanel2);
	}

	//~--- methods --------------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public abstract boolean countTotals();

	//~--- get methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public abstract String getTitle();

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public abstract double getYAxisMax();

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public abstract String getYTitle();

	//~--- methods --------------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param key
	 */
	public void addSeries(String key) {
		TimeSeries series = new TimeSeries(key);

		series.setMaximumItemAge(maxItemAge);
		timeSeriesCollection.addSeries(series);
		series_map.put(key, series);
	}

	/**
	 * Method description
	 *
	 *
	 * @param key
	 * @param val
	 */
	public synchronized void addValue(String key, double val) {
		TimeSeries series = series_map.get(key);

		if (series != null) {
			if (++refresh_cnt >= 10 * series_map.size()) {
				addValue(key, System.currentTimeMillis(), val, true, series);
				refresh_cnt = 0;
				xAxis.setLabel(dateFormat.format(new Date()));
			} else {
				addValue(key, System.currentTimeMillis(), val, false, series);
			}
		} else {
			System.err.println("Can't find series! " + key);
		}
	}

	//~--- get methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public List<JFreeChart> getCharts() {
		return charts;
	}

	/**
	 * Method description
	 *
	 *
	 * @return
	 */
	public JPanel getPanel() {
		return panel;
	}

	//~--- methods --------------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param id
	 * @param history
	 */
	public synchronized void loadHistory(String id, float[] history) {
		TimeSeries series = series_map.get(id);

		if (series != null) {
			int max_history = (int) Math.min(max_history_size, maxItemAge);
			int start = ((history.length > max_history_size) ? history.length - max_history : 0);
			long currentTime = System.currentTimeMillis();

			for (int i = start; i < history.length; i++) {
				addValue(id, (currentTime - (history.length - i) * 1000), history[i], false, series);
			}

			series.fireSeriesChanged();
		} else {
			System.err.println("Can't find series! " + id);
		}
	}

	/**
	 * Method description
	 *
	 *
	 * @param id
	 * @param history
	 */
	public synchronized void loadHistory(String id, int[] history) {
		TimeSeries series = series_map.get(id);

		if (series != null) {
			int max_history = (int) Math.min(max_history_size, maxItemAge);
			int start = ((history.length > max_history_size) ? history.length - max_history : 0);
			long currentTime = System.currentTimeMillis();

			for (int i = start; i < history.length; i++) {
				addValue(id, (currentTime - (history.length - i) * 1000), history[i], false, series);
			}

			series.fireSeriesChanged();
		} else {
			System.err.println("Can't find series! " + id);
		}
	}

	//~--- set methods ----------------------------------------------------------

	/**
	 * Method description
	 *
	 *
	 * @param key
	 * @param color
	 */
	public void setColor(String key, Paint color) {
		TimeSeries series = series_map.get(key);

		if (series != null) {
			int idx = timeSeriesCollection.indexOf(series);

			for (JFreeChart chart : charts) {
				XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();

				renderer.setSeriesPaint(idx, color);
			}
		} else {
			System.err.println("Can't find series! " + key);
		}
	}

	//~--- methods --------------------------------------------------------------

	private void addValue(String key, long time, double val, boolean notify, TimeSeries series) {
		RegularTimePeriod rtp = new Second(new Date(time));
		Number n = series.getValue(rtp);

		if (n == null) {
			series.add(rtp, val, notify);
		}

		if (countTotals()) {
			last_vals.put(key, val);

			long total = 0;

			for (Number num : last_vals.values()) {
				total += num.longValue();
			}

			if (max < total) {
				max = total;
			}

			if (notify) {
				for (JFreeChart jFreeChart : charts) {
					jFreeChart.setTitle(getTitle() + ", Total: " + total + ", Max: " + max);
				}
			}
		}
	}

	private JFreeChart createChart(XYLineAndShapeRenderer renderer) {

		// create plot...
//  NumberAxis xAxis = new NumberAxis("Seconds");
//  xAxis.setAutoRangeIncludesZero(false);
//  xAxis.setRangeType(RangeType.POSITIVE);
//  xAxis.setLowerBound(0);
//  xAxis.setAutoRangeMinimumSize(1800);
//  xAxis.setAutoRange(true);
		xAxis = new DateAxis(dateFormat.format(new Date()));
		xAxis.setAutoRange(true);
		xAxis.setDateFormatOverride(timeFormat);
		xAxis.setLowerMargin(0.0);
		xAxis.setUpperMargin(0.0);
		xAxis.setTickLabelsVisible(true);

		NumberAxis yAxis = new NumberAxis(getYTitle());

		yAxis.setAutoRangeIncludesZero(false);
		yAxis.setRangeType(RangeType.POSITIVE);
		yAxis.setLowerBound(0);
		yAxis.setAutoRangeMinimumSize(getYAxisMax());
		yAxis.setAutoRange(true);

		XYPlot plot = new XYPlot(timeSeriesCollection, xAxis, yAxis, renderer);

		plot.setBackgroundPaint(Color.lightGray);
		plot.setDomainGridlinePaint(Color.white);
		plot.setRangeGridlinePaint(Color.white);
		plot.setAxisOffset(new RectangleInsets(4, 4, 4, 4));

		JFreeChart chart = new JFreeChart(getTitle(), JFreeChart.DEFAULT_TITLE_FONT, plot, false);

		ChartUtilities.applyCurrentTheme(chart);

		return chart;
	}
}


//~ Formatted in Sun Code Convention


//~ Formatted by Jindent --- http://www.jindent.com
