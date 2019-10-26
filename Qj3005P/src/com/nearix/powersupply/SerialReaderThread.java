package com.nearix.powersupply;

public class SerialReaderThread extends Thread {

	public static String outputbuffer = "";
	public static double voltage = 0.0;
	public static double ampere = 0.0;
	public static boolean run = false;
	long lasttime;

	public void run() {
		System.out.println("Thread started!");
		while (true) {
			while (run) {
				try {
					while (Interface.serPort.bytesAvailable() == 0) {
						Thread.sleep(1);
					}

					byte[] readBuffer = new byte[Interface.serPort.bytesAvailable()];
					Interface.serPort.readBytes(readBuffer, readBuffer.length);
					outputbuffer += new String(readBuffer);
					
//					System.out.println(outputbuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}