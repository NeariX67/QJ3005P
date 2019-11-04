package com.nearix.powersupply;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

public class DataMonitor {

	private static JFrame frame;
	
	//JFreeChart variables b
	static XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
	static NumberAxis xax = new NumberAxis("Zeit");
	static NumberAxis yax = new NumberAxis("Daten");
	public static XYSeries serVoltage = new XYSeries("Voltage");
	public static XYSeries serAmpere = new XYSeries("Ampere");
	public static XYSeries serWatt = new XYSeries("Watt");
	static XYSeriesCollection data = new XYSeriesCollection();
	static XYPlot plot = new XYPlot(data, xax, yax, renderer);
	static JFreeChart chart = new JFreeChart(plot);
	
	public static void Datamonitor() {
		EventQueue.invokeLater(new Runnable() {
			@SuppressWarnings("unused")
			public void run() {
				try {
					DataMonitor window = new DataMonitor();
					DataMonitor.frame.setVisible(false);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	
	public DataMonitor() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 450, 300);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(10, 11, 414, 239);
		frame.getContentPane().add(panel);
		
		renderer.setSeriesShapesVisible(0, false);
		renderer.setSeriesShapesVisible(1, false);
		renderer.setSeriesShapesVisible(2, false);
		data.addSeries(serVoltage);
		data.addSeries(serAmpere);
		data.addSeries(serWatt);
		ChartPanel chartpanel = new ChartPanel(chart);
		frame.setContentPane(chartpanel);
		frame.pack();
		frame.setVisible(false);
		
		
	}
	
	public static void show() {
		frame.setVisible(true);
	}
}
