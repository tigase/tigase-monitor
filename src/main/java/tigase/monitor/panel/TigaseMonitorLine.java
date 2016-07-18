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
package tigase.monitor.panel;

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

import javax.swing.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * Created: Sep 9, 2009 11:47:00 PM
 * 
 * @author <a href="mailto:artur.hefczyc@tigase.org">Artur Hefczyc</a>
 * @version $Rev: 11 $
 */
public class TigaseMonitorLine extends TigaseMonitor {

	private String yTitle = null;
	private double yAxisMax = 100;
	private boolean countTotals = false;
	private boolean countPerSec = true;
	private boolean approximate = false;
	private int timeline = 24 * 360;

	private long max = 0;
	private JPanel panel = null;
	private Map<String, LastVal> lastVals = new LinkedHashMap<String, LastVal>();
	private Map<String, double[]> lastResults = new LinkedHashMap<String, double[]>();
	// private double[] lastResults = { 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d, 0d,
	// 0d, 0d };

	// private XYSeriesCollection data = null;
	private Map<String, TimeSeries> series_map = new LinkedHashMap<String, TimeSeries>(10);
	private TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();
	private Map<String, Number> last_vals = new LinkedHashMap<String, Number>(64);
	private List<JFreeChart> charts = new LinkedList<JFreeChart>();
	private DateAxis xAxis = null;
	private SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
	private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private Map<String, Double> oldVal = new LinkedHashMap<String, Double>(4);
	private Map<String, Boolean> first = new LinkedHashMap<String, Boolean>(4);

	public TigaseMonitorLine(String title, String yTitle, double yAxisMax,
			boolean countTotals, boolean countPerSec, boolean approximate, int timeline,
			int updaterate, int serverUpdaterare) {

		super(title, updaterate, serverUpdaterare);
		this.yTitle = yTitle;
		this.yAxisMax = yAxisMax;
		this.countTotals = countTotals;
		this.countPerSec = countPerSec;
		this.approximate = approximate;
		setTimeline(timeline);
		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();

		renderer.setBaseShapesVisible(false);

		JFreeChart chart2 = createChart(renderer);

		charts.add(chart2);

		ChartPanel chartPanel2 = new ChartPanel(chart2);

		panel = new JPanel(new BorderLayout());
		panel.setBackground(Color.DARK_GRAY);

		panel.add(chartPanel2);
	}

	public void setTimeline(int timeline) {
		this.timeline = timeline;
		for (TimeSeries series : series_map.values()) {
			series.setMaximumItemCount(timeline);
		}
	}

	public boolean countTotals() {
		return countTotals;
	}

	public double getYAxisMax() {
		return yAxisMax;
	}

	public String getYTitle() {
		return yTitle;
	}

	public void addSeries(String key, Paint color) {
		DataChange dc = (DataChange) getDataChangeListener();
		Paint c = color;
		for (int i = 1; i <= dc.getDataIds().length; i++) {
			String sKey = key + "-" + i;
			TimeSeries series = new TimeSeries(sKey);

			series.setMaximumItemCount(timeline);
			timeSeriesCollection.addSeries(series);
			series_map.put(sKey, series);
			setColor(sKey, c);
			c = ((Color) c).darker();
			lastVals.put(sKey, new LastVal(0d));
			lastResults.put(sKey, new double[12]);
		}
	}

	public synchronized void addValue(String key, double val) {
		// If the connection to the server is broken we can get zero's here
		// but we do not want to show zeros to the end-user, we better wait for
		// connection
		// re-established and real values
		// Zero is OK if there was no oldVal set yet, otherwise zero is not OK
		if (val == 0 && oldVal.get(key) != null) {
			val = oldVal.get(key);
			oldVal.put(key, 0.0);
		}
		// Saving current non-zero value for later use
		if (val != 0) {
			oldVal.put(key, val);
		}

		TimeSeries series = series_map.get(key);

		if (series != null) {
			addValue(key, System.currentTimeMillis(), val, true, series);
			xAxis.setLabel(dateFormat.format(new Date()));
		} else {
			System.err.println("Can't find series! " + key);
		}
	}

	public synchronized void addValueDelta(String key, double p_val) {
		double val = p_val;
		if (val == 0 && oldVal.get(key) != null) {
			val = oldVal.get(key);
			oldVal.put(key, 0.0);
		}
		if (val != 0) {
			oldVal.put(key, val);
		}

		TimeSeries series = series_map.get(key);

		if (series != null) {
			long time = System.currentTimeMillis();
			addValue(key, time, nextDelta(key, time, val), true, series);
			xAxis.setLabel(dateFormat.format(new Date()));
		} else {
			System.err.println("Can't find series! " + key);
		}
	}

	public List<JFreeChart> getCharts() {
		return charts;
	}

	public JPanel getPanel() {
		return panel;
	}

	public synchronized void loadHistory(String id, Float[] history) {
		double[] vals = new double[history.length];
		for (int i = 0; i < history.length; i++) {
			vals[i] = history[i];
		}
		loadHistory(id, vals, false);
	}

	public synchronized void loadHistory(String id, Integer[] history) {
		double[] vals = new double[history.length];
		for (int i = 0; i < history.length; i++) {
			vals[i] = history[i];
		}
		loadHistory(id, vals, false);
	}

	public synchronized void loadHistory(String id, Long[] history) {
		double[] vals = new double[history.length];
		for (int i = 0; i < history.length; i++) {
			vals[i] = history[i];
		}
		loadHistory(id, vals, false);
	}

	public synchronized void loadHistory(String id, double[] history, boolean calcDelta) {
		super.loadHistory(id, history, calcDelta);
		TimeSeries series = series_map.get(id);

		int updateStep = getUpdaterate() / getServerUpdaterate();

		if (series != null) {
			int max_history = Math.min(timeline, history.length);
			int start =
					((history.length > max_history * updateStep) ? history.length - max_history
							* updateStep : 0);

			long currentTime = System.currentTimeMillis();
			long time =
					(currentTime - ((getServerUpdaterate() * (history.length - start)) * 1000 + getServerUpdaterate() * 1000));
			initDelta(id, time, history[start]);

			double val = 0.0;
			for (int i = start; i < history.length; i += updateStep) {
				if (i < history.length) {
					time =
							(currentTime - ((getServerUpdaterate() * (history.length - i)) * 1000 + getServerUpdaterate() * 1000));
					val = history[i];
					if (calcDelta) {
						val = nextDelta(id, time, history[i]);
					}
					addValue(id, time, val, false, series);
				}
			}
			if (getTitle() == "SM Traffic" && id.equalsIgnoreCase("yellow-1")) {
				System.err.println("Title: " + getTitle() + ", ID: " + id + " last value added: "
						+ val);
			}
			oldVal.put(id, val);

			series.fireSeriesChanged();
		} else {
			System.err.println("Can't find series! " + id);
		}
		first.put(id, true);
	}

	public void initDelta(String id, long time, double val) {
		lastVals.put(id, new LastVal(time, val));
	}

	public double nextDelta(String key, long time, double val) {
		LastVal lastVal = lastVals.get(key);
		double update = countPerSec ? getUpdaterate() : 1;
		double approxTime = (time - lastVal.time) / 1000;
		if (approxTime == 0) {
			approxTime = 1;
		}
		double result = (val - lastVal.val) / (approxTime);
		lastVals.put(key, new LastVal(time, val));
		if (approximate) {
			result = calcApproximate(key, result);
		}
		return result;
	}

	private double calcApproximate(String key, double val) {
		double result = val;
		double[] lastRes = lastResults.get(key);
		for (int i = 0; i < lastRes.length; i++) {
			result += lastRes[i];
			if (i < lastRes.length - 1) {
				lastRes[i] = lastRes[i + 1];
			} else {
				lastRes[i] = val;
			}
		}
		result = result / (lastRes.length + 1);
		return result;
	}

	public void setColor(String key, Paint color) {
		TimeSeries series = series_map.get(key);

		if (series != null) {
			int idx = timeSeriesCollection.indexOf(series);

			for (JFreeChart chart : charts) {
				XYLineAndShapeRenderer renderer =
						(XYLineAndShapeRenderer) chart.getXYPlot().getRenderer();

				renderer.setSeriesPaint(idx, color);
			}
		} else {
			System.err.println("Can't find series! " + key);
		}
	}

	private void addValue(String key, long time, double val, boolean notify,
			TimeSeries series) {
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

		JFreeChart chart =
				new JFreeChart(getTitle(), JFreeChart.DEFAULT_TITLE_FONT, plot, false);

		ChartUtilities.applyCurrentTheme(chart);

		return chart;
	}

	private class LastVal {
		private long time = 0;
		private double val = 0;

		private LastVal(long t, double v) {
			time = t;
			val = v;
		}

		private LastVal(double v) {
			this(System.currentTimeMillis(), v);
		}
	}

}
