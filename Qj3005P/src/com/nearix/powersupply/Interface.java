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
import javax.swing.filechooser.FileNameExtensionFilter;

import com.fazecast.jSerialComm.SerialPort;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

public class Interface {

	// TODO: CSV: INformationen in Excel speichern

	private JFrame frmNetzteilKonfiguration;
	public static SerialPort serPort;
	public static SerialPort[] serList;
	static JComboBox<String> comboBox = new JComboBox<String>();

	Thread thread = new Thread(new SerialReaderThread());
	Thread reqThread = new Thread(new RequestThread());
	Thread monThread = new Thread(new DataMonitorThread());

	int baudrate = 9600;
	static boolean serOpen = false;
	int index = -1;
	int outputstate = 0;

	public static double prefVoltage = 0.0;
	public static double prefAmpere = 0.5;
	public static double ampere = 0.0;
	public static double voltage = 0.0;

	static JLabel lbSpannunngA = new JLabel("0.00");
	static JLabel lbStaerkeA = new JLabel("0.000");
	static JLabel lbPA = new JLabel("0.00");

	static JSpinner spinnerI = new JSpinner();
	static JSpinner spinnerU = new JSpinner();

	static JButton btnSend = new JButton("Apply");
	static JButton btnOpen = new JButton("Open");

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
		monThread.start();
		DataMonitor.Datamonitor();

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
		panel_3.setBounds(321, 21, 242, 143);
		panel.add(panel_3);
		panel_3.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(0, 0, 242, 143);
		panel_3.add(scrollPane);
		JLabel lbSpannung = new JLabel("0.00");
		lbSpannung.setHorizontalAlignment(SwingConstants.RIGHT);
		lbSpannung.setFont(new Font("Tahoma", Font.BOLD, 21));
		lbSpannung.setBounds(140, 23, 92, 26);
		panel_2.add(lbSpannung);

		JLabel lbStaerke = new JLabel("0.500");
		lbStaerke.setHorizontalAlignment(SwingConstants.RIGHT);
		lbStaerke.setFont(new Font("Tahoma", Font.BOLD, 21));
		lbStaerke.setBounds(140, 76, 92, 26);
		panel_2.add(lbStaerke);

		JSlider slider = new JSlider();
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				lbSpannung.setText(String.format(Locale.US, "%.2f", slider.getValue() / 100.0));
				spinnerU.setValue(slider.getValue() / 100.0);
			}
		});

		slider.setValue(0);
		slider.setMaximum(3000);
		slider.setBounds(260, 20, 410, 32);
		panel_2.add(slider);
		slider.setMajorTickSpacing(2);
		slider.setMinorTickSpacing(1);

		JSlider slider_1 = new JSlider();
		slider_1.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				lbStaerke.setText(String.format(Locale.US, "%.3f", slider_1.getValue() / 1000.0));
				spinnerI.setValue(slider_1.getValue() / 1000.0);
			}
		});

		slider_1.setMaximum(5000);
		slider_1.setValue(500);
		slider_1.setBounds(260, 73, 410, 32);
		panel_2.add(slider_1);
		slider_1.setMajorTickSpacing(2);
		slider_1.setMinorTickSpacing(1);

		JButton btnScan = new JButton("Refresh");
		btnScan.setBackground(new Color(211, 211, 211));
		btnScan.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnScan.setBounds(573, 20, 141, 35);
		btnScan.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				SerialReaderThread.run = false;
				RequestThread.run = false;
				DataMonitorThread.run = false;
				if (serPort != null) {
					serPort.closePort();
				}
				index = -1;
				comboBox.removeAllItems();
				for (int i = 0; i < (serList = SerialPort.getCommPorts()).length; i++) {
					comboBox.addItem(serList[i].toString());
				}
				serOpen = false;
				serPort = null;
				btnOpen.setText("Open");
				btnSend.setEnabled(false);

			}
		});
		panel.add(btnScan);

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
							DataMonitorThread.run = true;
						} else {
							serOpen = false;
							btnSend.setEnabled(false);
							SerialReaderThread.run = false;
							RequestThread.run = false;
							DataMonitorThread.run = false;
							btnOpen.setText("Open");
						}
					} else {
						SerialReaderThread.run = false;
						RequestThread.run = false;
						DataMonitorThread.run = false;
						btnSend.setEnabled(false);
						serOpen = false;
						serPort.closePort();
						btnOpen.setText("Open");
					}
				}

			}
		});
		btnOpen.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnOpen.setBounds(573, 60, 141, 35);
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
		btnSend.setBounds(573, 140, 141, 35);
		panel.add(btnSend);

		JLabel lblSerialportAuswhlen = new JLabel("SerialPort Ausw\u00E4hlen");
		lblSerialportAuswhlen.setHorizontalAlignment(SwingConstants.CENTER);
		lblSerialportAuswhlen.setFont(new Font("Tahoma", Font.PLAIN, 19));
		lblSerialportAuswhlen.setBounds(46, 21, 223, 35);
		panel.add(lblSerialportAuswhlen);

		comboBox.setBackground(Color.WHITE);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				index = comboBox.getSelectedIndex();
				if (index != -1) {
					serPort = serList[index];
				}
			}
		});
		comboBox.setBounds(21, 59, 270, 42);
		panel.add(comboBox);

		JButton btnOutput = new JButton("Enable Output");
		btnOutput.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (serOpen) {
					if (outputstate == 0) {
						btnOutput.setText("Disable Output");
						outputstate = 1;
					} else {
						btnOutput.setText("Enable Output");
						outputstate = 0;
					}
					String output = "OUTPUT" + outputstate + "\n";
					System.out.println("Sending: " + output);
					serPort.writeBytes(output.getBytes(), output.getBytes().length);
				}
			}
		});
		btnOutput.setBackground(new Color(211, 211, 211));
		btnOutput.setBounds(573, 100, 141, 35);
		panel.add(btnOutput);

		JButton btnDatalogger = new JButton("Datalogger");
		btnDatalogger.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String output = "Voltage;Ampere\n";
				for (int i = 0; i < DataMonitorThread.counter; i++) {
					output += String.format("%.2f", DataMonitorThread.voltageList.get(i)) + ";"
							+ String.format("%.3f", DataMonitorThread.ampereList.get(i)) + "\n";
				}
				JFileChooser chooser = new JFileChooser();
				chooser.setDialogType(JFileChooser.SAVE_DIALOG);
				chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				chooser.setFileFilter(new FileNameExtensionFilter("Comma-separated values (.csv)", ".csv"));
				if (chooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
					if (chooser.getSelectedFile().getAbsolutePath().endsWith(File.separator)) {
						writeFile(output, chooser.getSelectedFile().getPath() + "unnamed.csv");
					} else if (!chooser.getSelectedFile().getAbsolutePath().endsWith(".csv")) {
						writeFile(output, chooser.getSelectedFile().getPath() + ".csv");
					}
					else {
						writeFile(output, chooser.getSelectedFile().getPath());
					}

				}
			}
		});
		btnDatalogger.setBackground(new Color(211, 211, 211));
		btnDatalogger.setFont(new Font("Tahoma", Font.BOLD, 15));
		btnDatalogger.setBounds(21, 129, 125, 35);
		panel.add(btnDatalogger);

		JButton btnDatamonitor = new JButton("Datamonitor");
		btnDatamonitor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				DataMonitor.show();
			}
		});
		btnDatamonitor.setBackground(new Color(211, 211, 211));
		btnDatamonitor.setFont(new Font("Tahoma", Font.BOLD, 14));
		btnDatamonitor.setBounds(165, 129, 125, 35);
		panel.add(btnDatamonitor);

		JPanel panel_1 = new JPanel();
		panel_1.setBounds(21, 260, 732, 116);
		frmNetzteilKonfiguration.getContentPane().add(panel_1);
		panel_1.setLayout(null);

		JLabel lblSpannungAktuell = new JLabel("Spannung Aktuell");
		lblSpannungAktuell.setHorizontalAlignment(SwingConstants.CENTER);
		lblSpannungAktuell.setBounds(21, 10, 205, 26);
		panel_1.add(lblSpannungAktuell);

		JLabel lblStrkeAktuell = new JLabel("Staerke Aktuell");
		lblStrkeAktuell.setHorizontalAlignment(SwingConstants.CENTER);
		lblStrkeAktuell.setBounds(291, 10, 149, 26);
		panel_1.add(lblStrkeAktuell);

		JLabel lblAktuell = new JLabel("Elektrische Leistung");
		lblAktuell.setHorizontalAlignment(SwingConstants.CENTER);
		lblAktuell.setBounds(516, 10, 205, 26);
		panel_1.add(lblAktuell);
		lbSpannunngA.setHorizontalAlignment(SwingConstants.RIGHT);

		lbSpannunngA.setForeground(Color.BLACK);
		lbSpannunngA.setFont(new Font("Cambria Math", Font.BOLD, 50));
		lbSpannunngA.setBackground(Color.BLACK);
		lbSpannunngA.setBounds(-16, 47, 161, 59);
		panel_1.add(lbSpannunngA);
		lbStaerkeA.setHorizontalAlignment(SwingConstants.RIGHT);

		lbStaerkeA.setForeground(Color.BLACK);
		lbStaerkeA.setFont(new Font("Cambria Math", Font.BOLD, 50));
		lbStaerkeA.setBackground(Color.BLACK);
		lbStaerkeA.setBounds(249, 47, 161, 59);
		panel_1.add(lbStaerkeA);
		lbPA.setHorizontalAlignment(SwingConstants.RIGHT);

		lbPA.setForeground(Color.BLACK);
		lbPA.setFont(new Font("Cambria Math", Font.BOLD, 50));
		lbPA.setBackground(Color.BLACK);
		lbPA.setBounds(477, 47, 161, 59);
		panel_1.add(lbPA);

		JLabel lblV = new JLabel("V");
		lblV.setForeground(Color.BLACK);
		lblV.setBackground(Color.BLACK);
		lblV.setFont(new Font("Tahoma", Font.BOLD, 40));
		lblV.setBounds(166, 55, 48, 38);
		panel_1.add(lblV);

		JLabel lblA = new JLabel("A");
		lblA.setForeground(Color.BLACK);
		lblA.setBackground(Color.BLACK);
		lblA.setFont(new Font("Tahoma", Font.BOLD, 40));
		lblA.setBounds(429, 55, 48, 38);
		panel_1.add(lblA);

		JLabel lblW = new JLabel("W");
		lblW.setForeground(Color.BLACK);
		lblW.setFont(new Font("Tahoma", Font.BOLD, 40));
		lblW.setBounds(659, 55, 48, 38);
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
		spinnerU.setBounds(43, 20, 70, 32);
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
		spinnerI.setBounds(43, 73, 70, 32);
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
		lbPA.setText(String.format(Locale.US, "%.2f", voltage * ampere));
	}

	public static void writeFile(String text, String file) {
		System.out.println(file);
		BufferedWriter f;
		try {
			f = new BufferedWriter(new FileWriter(file));
			f.write(text);
			f.close();
		} catch (IOException e) {
			System.err.println(e.toString());
		}
	}

	public static String getString(double value, boolean isAmpere) {
		String output = "";
		if (!isAmpere) {
			if (value >= 9.995) {
				output = String.format(Locale.US, "%.2f", value);
			} else {
				output = "0" + String.format(Locale.US, "%.2f", value);
			}
		} else {
			output = String.format(Locale.US, "%.3f", value);
		}
		return output;
	}
}
