package com.nearix.powersupply;

import java.util.ArrayList;

public class DataMonitorThread extends Thread {

	public static ArrayList<Double> voltageList = new ArrayList<Double>();
	public static ArrayList<Double> ampereList = new ArrayList<Double>();
	public static int counter = 0;

	public static boolean run = false;
	long lasttime;

	public void run() {
		System.out.println("Thread started!");
		DataMonitor.xax.setRange(0, 2001);
		while (true) {
			while (run) {
				if ((System.currentTimeMillis() - lasttime) > 100) {
					voltageList.add(Interface.voltage);
					ampereList.add(Interface.ampere);
					
					DataMonitor.serVoltage.add(counter, Interface.voltage);
					DataMonitor.serAmpere.add(counter, Interface.ampere);
					DataMonitor.serWatt.add(counter, Interface.voltage*Interface.ampere);
					lasttime = System.currentTimeMillis();
					counter++;
					if(counter >= 2000) {
						DataMonitor.xax.setRange(counter - 2000, counter + 1);
					}
				}
			}
			// If thread should not run, sleep for 50ms
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}