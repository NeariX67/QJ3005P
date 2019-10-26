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
	public static double prefAmpere = 0.0;
	public static double ampere = 0.0;
	public static double voltage = 0.0;


	static JLabel lbSpannunngA = new JLabel("0.00");
	static JLabel lbStaerkeA = new JLabel("0.00");
	static JLabel lbPA = new JLabel("0.00");
	
	
	
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
				lbStaerke.setText(String.format(Locale.US, "%.2f", slider_1.getValue() / 1000.0));
			}
		});

		slider_1.setMaximum(5000);
		slider_1.setValue(0);
		slider_1.setBounds(366, 73, 313, 32);
		panel_2.add(slider_1);
		slider_1.setMajorTickSpacing(2);
		slider_1.setMinorTickSpacing(1);

		JButton btnScan = new JButton("Scan");
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
							SerialReaderThread.run = true;
							RequestThread.run = true;
						} else {
							serOpen = false;
							SerialReaderThread.run = false;
							RequestThread.run = false;
							btnOpen.setText("Open");
						}
					} else {
						SerialReaderThread.run = false;
						RequestThread.run = false;
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

		JButton btnSend = new JButton("Send");
		btnSend.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (serOpen) {
					String output = lbSpannung.getText();
					serPort.writeBytes(output.getBytes(), output.getBytes().length);
				}
			}
		});
		btnSend.setBackground(Color.LIGHT_GRAY);
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
				serPort = serList[index];
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

		JSpinner spinnerU = new JSpinner();
		spinnerU.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				lbSpannung.setText((String) spinnerU.getValue());

			}
		});
		spinnerU.setModel(new SpinnerNumberModel(0.0, 0.0, 30.0, 1.0));
		spinnerU.setBackground(Color.WHITE);
		spinnerU.setBounds(43, 20, 165, 32);
		panel_2.add(spinnerU);

		JSpinner spinnerI = new JSpinner();
		spinnerI.setModel(new SpinnerNumberModel(0.0, 0.0, 5.0, 1.0));
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
		lbPA.setText(String.format(Locale.US, "%.3f", voltage*ampere));
	}
}
