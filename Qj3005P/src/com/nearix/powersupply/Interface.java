package com.nearix.powersupply;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JSlider;
import javax.swing.JPanel;
import java.awt.Color;
import javax.swing.JButton;
import java.awt.Font;
import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.fazecast.jSerialComm.SerialPort;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JScrollPane;

public class Interface {

	private JFrame frmNetzteilKonfiguration;
	public static SerialPort serPort;
	public static SerialPort[] serList;
	static JComboBox<String> comboBox = new JComboBox<String>();

	Thread thread = new Thread(new SerialReaderThread());
	Thread reqThread = new Thread(new RequestThread());
	
	
	int baudrate = 9600;
	boolean serOpen = false;
	int index = -1;
	
	public static double prefVoltage = 0.0;
	public static double prefAmpere = 0.5;
	public static double ampere = 0.0;
	public static double voltage = 0.0;


	static JLabel lbSpannunngA = new JLabel("0.00");
	static JLabel lbStaerkeA = new JLabel("0.000");
	static JLabel lbPA = new JLabel("0.000");

	static JSpinner spinnerI = new JSpinner();
	static JSpinner spinnerU = new JSpinner();
	
	static JButton btnSend = new JButton("Apply");
	
	
	
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Interface window = new Interface();
					window.frmNetzteilKonfiguration.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		for (int i = 0; i < (serList = SerialPort.getCommPorts()).length; i++) {
			comboBox.addItem(serList[i].toString());
		}
		
		
	}


	public Interface() {
		initialize();
	}


	private void initialize() {
		frmNetzteilKonfiguration = new JFrame();
		frmNetzteilKonfiguration.setTitle("Netzteil Konfiguration");
		frmNetzteilKonfiguration.getContentPane().setBackground(Color.WHITE);
		frmNetzteilKonfiguration.setBounds(100, 100, 800, 600);
		frmNetzteilKonfiguration.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmNetzteilKonfiguration.getContentPane().setLayout(null);
		
		thread.start();
		reqThread.start();

		JPanel panel_2 = new JPanel();
		panel_2.setBounds(21, 382, 732, 126);
		frmNetzteilKonfiguration.getContentPane().add(panel_2);
		panel_2.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBounds(21, 21, 732, 203);
		frmNetzteilKonfiguration.getContentPane().add(panel);
		panel.setLayout(null);

		JPanel panel_3 = new JPanel();
		panel_3.setBackground(Color.WHITE);
		panel_3.setBounds(321, 21, 242, 161);
		panel.add(panel_3);
		panel_3.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 242, 161);
		panel_3.add(scrollPane);
		JLabel lbSpannung = new JLabel("");
		lbSpannung.setFont(new Font("Tahoma", Font.BOLD, 21));
		lbSpannung.setBounds(229, 23, 92, 26);
		panel_2.add(lbSpannung);

		JLabel lbStaerke = new JLabel("");
		lbStaerke.setFont(new Font("Tahoma", Font.BOLD, 21));
		lbStaerke.setBounds(229, 76, 92, 26);
		panel_2.add(lbStaerke);

		JSlider slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lbSpannung.setText(String.format(Locale.US, "%.2f", slider.getValue() / 100.0));
				spinnerU.setValue(slider.getValue()/100.0);
			}
		});
		
		slider.setValue(0);
		slider.setMaximum(3000);
		slider.setBounds(366, 20, 313, 32);
		panel_2.add(slider);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);

		JSlider slider_1 = new JSlider();
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lbStaerke.setText(String.format(Locale.US, "%.3f", slider_1.getValue() / 1000.0));
				spinnerI.setValue(slider_1.getValue()/1000.0);
			}
		});

		slider_1.setMaximum(5000);
		slider_1.setValue(500);
		slider_1.setBounds(366, 73, 313, 32);
		panel_2.add(slider_1);
		slider_1.setMajorTickSpacing(2);
		slider_1.setMinorTickSpacing(1);

		JButton btnScan = new JButton("Refresh");
		btnScan.setBackground(new Color(211, 211, 211));
		btnScan.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnScan.setBounds(570, 38, 141, 35);
		btnScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				index = -1;
				comboBox.removeAllItems();
				for (int i = 0; i < (serList = SerialPort.getCommPorts()).length; i++) {
					comboBox.addItem(serList[i].toString());
				}

			}
		});
		panel.add(btnScan);

		JButton btnOpen = new JButton("Open");
		btnOpen.setBackground(new Color(211, 211, 211));
		btnOpen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (index != -1) {
					if (btnOpen.getText() == "Open") {
						if (serPort.openPort()) {
							serOpen = true;
							serPort.setBaudRate(baudrate);
							btnOpen.setText("Close");
							btnSend.setEnabled(true);
							SerialReaderThread.run = true;
							RequestThread.run = true;
						} else {
							serOpen = false;
							btnSend.setEnabled(false);
							SerialReaderThread.run = false;
							RequestThread.run = false;
							btnOpen.setText("Open");
						}
					} else {
						SerialReaderThread.run = false;
						RequestThread.run = false;
						btnSend.setEnabled(false);
						serOpen = false;
						serPort.closePort();
						btnOpen.setText("Open");
					}
				}

			}
		});
		btnOpen.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnOpen.setBounds(570, 84, 141, 35);
		panel.add(btnOpen);

		btnSend.setEnabled(false);
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (serOpen) {
					String output = "VSET1:" + getString(prefVoltage, false) + "\\n";
					System.out.println("Sending: " + output);
					serPort.writeBytes(output.getBytes(), output.getBytes().length);
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					output = "ISET1:" + getString(prefAmpere, true) + "\\n";
					System.out.println("Sending: " + output);
					serPort.writeBytes(output.getBytes(), output.getBytes().length);
				}
			}
		});
		btnSend.setBackground(new Color(211, 211, 211));
		btnSend.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnSend.setBounds(570, 129, 141, 35);
		panel.add(btnSend);

		JLabel lblSerialportAuswhlen = new JLabel("SerialPort Ausw\u00E4hlen");
		lblSerialportAuswhlen.setFont(new Font("Tahoma", Font.PLAIN, 19));
		lblSerialportAuswhlen.setBounds(46, 21, 223, 35);
		panel.add(lblSerialportAuswhlen);

		comboBox.setBackground(Color.WHITE);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				index = comboBox.getSelectedIndex();
				if(index != -1) {
					serPort = serList[index];
				}
			}
		});
		comboBox.setBounds(21, 59, 270, 42);
		panel.add(comboBox);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(21, 260, 732, 116);
		frmNetzteilKonfiguration.getContentPane().add(panel_1);
		panel_1.setLayout(null);

		JLabel lblSpannungAktuell = new JLabel("Spannung Aktuell");
		lblSpannungAktuell.setBounds(21, 10, 205, 26);
		panel_1.add(lblSpannungAktuell);

		JLabel lblStrkeAktuell = new JLabel("Stärke Aktuell");
		lblStrkeAktuell.setBounds(291, 10, 149, 26);
		panel_1.add(lblStrkeAktuell);

		JLabel lblAktuell = new JLabel("Elektrische Leistung");
		lblAktuell.setBounds(516, 10, 205, 26);
		panel_1.add(lblAktuell);

		lbSpannunngA.setForeground(Color.BLACK);
		lbSpannunngA.setFont(new Font("Cambria Math", Font.BOLD, 55));
		lbSpannunngA.setBackground(Color.BLACK);
		lbSpannunngA.setBounds(31, 48, 161, 59);
		panel_1.add(lbSpannunngA);

		lbStaerkeA.setForeground(Color.BLACK);
		lbStaerkeA.setFont(new Font("Cambria Math", Font.BOLD, 55));
		lbStaerkeA.setBackground(Color.BLACK);
		lbStaerkeA.setBounds(291, 46, 161, 59);
		panel_1.add(lbStaerkeA);

		lbPA.setForeground(Color.BLACK);
		lbPA.setFont(new Font("Cambria Math", Font.BOLD, 55));
		lbPA.setBackground(Color.BLACK);
		lbPA.setBounds(516, 46, 161, 59);
		panel_1.add(lbPA);

		JLabel lblV = new JLabel("V");
		lblV.setForeground(Color.BLACK);
		lblV.setBackground(Color.BLACK);
		lblV.setFont(new Font("Tahoma", Font.BOLD, 40));
		lblV.setBounds(153, 54, 48, 38);
		panel_1.add(lblV);

		JLabel lblA = new JLabel("A");
		lblA.setForeground(Color.BLACK);
		lblA.setBackground(Color.BLACK);
		lblA.setFont(new Font("Tahoma", Font.BOLD, 40));
		lblA.setBounds(419, 54, 48, 38);
		panel_1.add(lblA);

		JLabel lblW = new JLabel("W");
		lblW.setForeground(Color.BLACK);
		lblW.setFont(new Font("Tahoma", Font.BOLD, 40));
		lblW.setBounds(647, 54, 48, 38);
		panel_1.add(lblW);
		spinnerU.setModel(new SpinnerNumberModel(0.0, 0.0, 30.0, 0.01));
		spinnerU.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent e) {
	        	prefVoltage = (double) spinnerU.getValue();
				lbSpannung.setText(String.format(Locale.US, "%.2f", prefVoltage));
				slider.setValue((int) Math.round((prefVoltage * 100)));
	        }
	    });
		spinnerU.setBackground(Color.WHITE);
		spinnerU.setBounds(43, 20, 165, 32);
		panel_2.add(spinnerU);

		spinnerI.addChangeListener(new ChangeListener() {
	        @Override
	        public void stateChanged(ChangeEvent e) {
				lbStaerke.setText(String.format(Locale.US, "%.3f", spinnerI.getValue()));
				prefAmpere = Double.parseDouble(lbStaerke.getText());
				slider_1.setValue((int) (prefAmpere * 1000));
	        }
	    });
		spinnerI.setModel(new SpinnerNumberModel(0.5, 0.0, 5.0, 0.001));
		spinnerI.setBackground(Color.WHITE);
		spinnerI.setBounds(43, 73, 165, 32);
		panel_2.add(spinnerI);
		
		JLabel lblU = new JLabel("U:");
		lblU.setBounds(10, 23, 92, 26);
		panel_2.add(lblU);

		JLabel lblI = new JLabel("I:");
		lblI.setBounds(10, 73, 92, 26);
		panel_2.add(lblI);

	}
	
	public static void refresh() {
		lbSpannunngA.setText(String.format(Locale.US, "%.2f", voltage));
		lbStaerkeA.setText(String.format(Locale.US, "%.3f", ampere));
		lbPA.setText(String.format(Locale.US, "%.2f", voltage*ampere));
	}
	
	public static void writeFile(String text, String file) {

	    BufferedWriter f;
	    try {
	      f = new BufferedWriter(new FileWriter(file));
	      f.write(text);
	      f.close();
	    }
	    catch (IOException e) {
	      System.err.println(e.toString());
	    }		
	}
	public static String getString(double value, boolean isAmpere) {
		String output = "";
		if(!isAmpere) {
			if(value >= 9.995) {
				output = String.format(Locale.US, "%.2f", value);
			}
			else {
				output = "0" + String.format(Locale.US, "%.2f", value);
			}
		}
		else {
			output = String.format(Locale.US, "%.3f", value);
		}
		return output;
	}
}
