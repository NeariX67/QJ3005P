package com.nearix.powersupply;

public class RequestThread extends Thread {
	public static boolean run = false;
	public static int reqIndex = 0;
	long lasttime;
	
	public void run() {
		System.out.println("Thread started!");
		while (true) {
			while (run) {
				if((System.currentTimeMillis() - lasttime) > 75) {
					SerialReaderThread.outputbuffer = "";
					if(reqIndex == 0) { //Requesting Voltage if index is 0
//						System.out.println("Requesting voltage");
						Interface.serPort.writeBytes("VOUT1?\\n".getBytes(), "VOUT1?\\n".getBytes().length);
					}
					else { //Requesting Ampere if index is 1 (or != 0)
//						System.out.println("Requesting ampere");
						Interface.serPort.writeBytes("IOUT1?\\n".getBytes(), "IOUT1?\\n".getBytes().length);
					}
					try {
						Thread.sleep(30);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					String outputbuffer = SerialReaderThread.outputbuffer;
					switch (reqIndex) {
					case 0:
						if(outputbuffer.length() >= 4) {
							Interface.voltage = getValue(outputbuffer);
						}
						reqIndex = 1;
						break;
					case 1:
						if(outputbuffer.length() >= 5) {
							Interface.ampere = getValue(outputbuffer);
						}
						reqIndex = 0;
						break;
					default:
						System.out.println("Error: wrong requestIndex (" + RequestThread.reqIndex + ")");
						break;
					}
					Interface.refresh();
					lasttime = System.currentTimeMillis();
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public double getValue(String input) {
		if(input.contains(".")) {
			if(input.charAt(0) == '0') {
				input = input.replaceFirst("0", "");
			}
			return Double.parseDouble(input);
		}
		return -1.0;
	}
}
