package com.nearix.powersupply;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Locale;

import com.fazecast.jSerialComm.*;

public class GUI extends Application {

	public static SerialPort serPort;
	public static SerialPort[] serList;
	long lasttime;

	Thread thread = new Thread(new SerialReaderThread());

	ObservableList<String> Serialports = FXCollections.observableArrayList();
	ComboBox<String> cbPorts = new ComboBox<String>(Serialports);

	Button btnRefresh = new Button("Refresh");
	Button btnOpen = new Button("Open");
	Button btnTest = new Button("Send");
	TextField tfInput = new TextField();
	TextField tfVoltage = new TextField();
	Label lbAusgabe = new Label();
	public static Label lbAusgabe2 = new Label();

	Slider slVoltage = new Slider(0.0, 30.0, 5.0);
	double voltage = 5.00;

	HBox hbox = new HBox();
	VBox vbox = new VBox();
	int index = -1;
	boolean serOpen = false;
	int baudrate = 9600;

	public static void main(String[] args) {

		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		for (int i = 0; i < (serList = SerialPort.getCommPorts()).length; i++) {
			Serialports.add(serList[i].toString());
		}

		thread.start();

		stage.setTitle("JavaFX Test application");
		stage.setWidth(800);
		stage.setHeight(600);
		stage.show();
		Pane pane = new Pane();
		hbox.setPadding(new Insets(15, 10, 15, 10));
		hbox.setSpacing(20);

		vbox.setPadding(new Insets(55, 10, 55, 10));
		vbox.setSpacing(20);

		lbAusgabe.setText("5.00");
		hbox.getChildren().addAll(cbPorts, btnRefresh, btnOpen, tfInput, btnTest);
		vbox.getChildren().addAll(slVoltage, lbAusgabe, lbAusgabe2);
		pane.getChildren().addAll(hbox, vbox);
		stage.setScene(new Scene(pane, 800, 600));

		btnRefresh.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				index = -1;
				Serialports.clear();
				for (int i = 0; i < (serList = SerialPort.getCommPorts()).length; i++) {
					Serialports.add(serList[i].toString());
				}
			}
		});

		btnOpen.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				if (index != -1) {
					if (btnOpen.getText() == "Open") {
						if (serPort.openPort()) {
							serOpen = true;
							serPort.setBaudRate(baudrate);
							btnOpen.setText("Close");
							getVoltage(serPort);
//							SerialReaderThread.run = true;
						} else {
							serOpen = false;
							SerialReaderThread.run = false;
							btnOpen.setText("Open");
						}
					} else {
						SerialReaderThread.run = false;
						serOpen = false;
						serPort.closePort();
						btnOpen.setText("Open");
					}
				}
			}
		});

		btnTest.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				if (serOpen) {
					String output = tfInput.getText();
					System.out.println("Sending: " + output);
					serPort.writeBytes(output.getBytes(), output.getBytes().length);
				}
			}
		});

		cbPorts.valueProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(@SuppressWarnings("rawtypes") ObservableValue ov, String t, String t1) {
				for (int i = 0; i < serList.length; i++) {
					if (Serialports.size() > 0) {
						if (Serialports.get(i).toString() == t1) {
							index = i;
							serPort = serList[i];
						}
					}
				}
			}
		});

		slVoltage.valueProperty().addListener(new ChangeListener<Number>() {
			public void changed(ObservableValue<? extends Number> ov, Number old_val, Number new_val) {
				voltage = (double) new_val;
				lbAusgabe.setText(String.format(Locale.US, "%.2f", voltage));

				if (serOpen) {
					if ((System.currentTimeMillis() - lasttime) > 50) {
						String output = "";
						if (voltage >= 9.995) {
							output = String.format(Locale.US, "%.2f", voltage);
						} else {
							output = "0" + String.format(Locale.US, "%.2f", voltage);
						}
						String output2 = "VSET1:" + output + "\\\\n";
						System.out.println("Sending: " + output2);
						serPort.writeBytes(output2.getBytes(), output2.getBytes().length);
					}
				}

			}
		});
		
//		serPort.addDataListener(new SerialPortDataListener() {
//			@Override
//			public void serialEvent(SerialPortEvent arg0) {
//				
//			}
//			@Override
//			public int getListeningEvents() {
//				return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
//			}
//		});
		
		
		stage.setOnCloseRequest(event -> {
			serPort.closePort();
			SerialReaderThread.run = false;
		});
	}

	public static void setLabelText(String text) {
		lbAusgabe2.setText(text);
	}

	public static double getVoltage(SerialPort port) {
		port.writeBytes(("VOUT1?\\n").getBytes(), ("VOUT1?\\n").getBytes().length);
		while (port.bytesAvailable() == 0) {
			
		}
		
		String outputbuffer = "";
		byte[] readBuffer = new byte[port.bytesAvailable()];
		port.readBytes(readBuffer, readBuffer.length);
		outputbuffer += new String(readBuffer);
		System.out.println(outputbuffer);
		System.out.println(outputbuffer.length());
		return 0.0;
	}

}
