package com.nearix.powersupply;

public class SerialReaderThread extends Thread {

	String outputbuffer = "";
	public static boolean run = false;
	
	public void run() {
		System.out.println("Thread started!");
		while (true) {
			while (run) {
				try {
					while (GUI.serPort.bytesAvailable() == 0) {
						Thread.sleep(1);
					}

					byte[] readBuffer = new byte[GUI.serPort.bytesAvailable()];
					int numRead = GUI.serPort.readBytes(readBuffer, readBuffer.length);
					System.out.println(new String(readBuffer));
					outputbuffer += new String(readBuffer);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}