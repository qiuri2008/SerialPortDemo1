/*
 * MainFrame.java
 *
 */

package serialport.ui;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import serialport.exception.NoSuchPort;
import serialport.exception.NotASerialPort;
import serialport.exception.PortInUse;
import serialport.exception.SendDataToSerialPortFailure;
import serialport.exception.SerialPortOutputStreamCloseFailure;
import serialport.exception.SerialPortParameterFailure;
import serialport.exception.TooManyListeners;
import serialport.manage.SerialPortManager;
import serialport.utils.ByteUtils;
import serialport.utils.ShowUtils;

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class MainFrame extends JFrame {

	public static final int WIDTH = 500;
	public static final int HEIGHT = 360;

	private JTextArea dataView = new JTextArea();
	private JScrollPane scrollDataView = new JScrollPane(dataView);

	// 串口设置面板
	private JPanel serialPortPanel = new JPanel();
	private JLabel serialPortLabel = new JLabel("串口");
	private JLabel baudrateLabel = new JLabel("波特率");
	private JComboBox comPort = new JComboBox();
	private JComboBox comBitRate = new JComboBox();

	// 操作面板
	private JPanel operatePanel = new JPanel();
	private JTextField dataInput = new JTextField();
	private JButton btnSerialPortSwitch = new JButton("打开串口");
	private JButton btnSendData = new JButton("发送数据");

	private List<String> commList = null;
	private SerialPort serialport;

	public MainFrame() {
		initView();
		initComponents();
		actionListener();
		initData();
	}

	private void initView() {
		// 关闭程序
		setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
		// 禁止窗口最大化
		setResizable(false);

		// 设置程序窗口居中显示
		Point p = GraphicsEnvironment.getLocalGraphicsEnvironment().getCenterPoint();
		setBounds(p.x - WIDTH / 2, p.y - HEIGHT / 2, WIDTH, HEIGHT);
		this.setLayout(null);

		setTitle("串号系统-SN V1.0");
		dataInput.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == KeyEvent.VK_ENTER) // 按回车键执行相应操作;
				{
					System.out.println("dskfj");
				}
			}
		});

	}

	private void initComponents() {
		// 数据显示
		dataView.setFocusable(false);
		scrollDataView.setBounds(10, 10, 475, 200);
		add(scrollDataView);

		// 串口设置
		serialPortPanel.setBorder(BorderFactory.createTitledBorder("串口设置"));
		serialPortPanel.setBounds(10, 220, 170, 100);
		serialPortPanel.setLayout(null);
		add(serialPortPanel);

		serialPortLabel.setForeground(Color.gray);
		serialPortLabel.setBounds(10, 25, 40, 20);
		serialPortPanel.add(serialPortLabel);

		comPort.setFocusable(false);
		comPort.setBounds(60, 25, 100, 20);
		serialPortPanel.add(comPort);

		baudrateLabel.setForeground(Color.gray);
		baudrateLabel.setBounds(10, 60, 40, 20);
		serialPortPanel.add(baudrateLabel);

		comBitRate.setFocusable(false);
		comBitRate.setBounds(60, 60, 100, 20);
		serialPortPanel.add(comBitRate);

		// 操作
		operatePanel.setBorder(BorderFactory.createTitledBorder("操作"));
		operatePanel.setBounds(200, 220, 285, 100);
		operatePanel.setLayout(null);
		add(operatePanel);

		dataInput.setBounds(25, 25, 235, 20);
		operatePanel.add(dataInput);

		btnSerialPortSwitch.setFocusable(false);
		btnSerialPortSwitch.setBounds(45, 60, 90, 20);
		operatePanel.add(btnSerialPortSwitch);

		btnSendData.setFocusable(false);
		btnSendData.setBounds(155, 60, 90, 20);
		operatePanel.add(btnSendData);
	}


	@SuppressWarnings("unchecked")
	private void initData() {
		commList = SerialPortManager.findPort();
		// 检查是否有可用串口，有则加入选项中
		if (commList == null || commList.size() < 1) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			for (String s : commList) {
				comPort.addItem(s);
			}
		}

		comBitRate.addItem("9600");
		comBitRate.addItem("19200");
		comBitRate.addItem("38400");
		comBitRate.addItem("57600");
		comBitRate.addItem("115200");
	}

	private void actionListener() {

		btnSerialPortSwitch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if ("打开串口".equals(btnSerialPortSwitch.getText()) && serialport == null) {
					openSerialPort(e);
				} else {
					closeSerialPort(e);
				}
			}
		});

		btnSendData.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				sendData(e);
			}
		});
	}

	/**
	 * 点击事件-打开串口
	 */
	private void openSerialPort(java.awt.event.ActionEvent evt) {
		// 获取串口名称
		String commName = (String) comPort.getSelectedItem();
		// 获取波特率
		int baudrate = 9600;
		String bps = (String) comBitRate.getSelectedItem();
		baudrate = Integer.parseInt(bps);

		// 检查串口名称是否获取正确
		if (commName == null || commName.equals("")) {
			ShowUtils.warningMessage("没有搜索到有效串口！");
		} else {
			try {
				serialport = SerialPortManager.openPort(commName, baudrate);
				if (serialport != null) {
					dataView.setText("串口已打开" + "\r\n");
					btnSerialPortSwitch.setText("关闭串口");
				}
			} catch (SerialPortParameterFailure e) {
				e.printStackTrace();
			} catch (NotASerialPort e) {
				e.printStackTrace();
			} catch (NoSuchPort e) {
				e.printStackTrace();
			} catch (PortInUse e) {
				e.printStackTrace();
				ShowUtils.warningMessage("串口已被占用！");
			}
		}

		try {
			SerialPortManager.addListener(serialport, new SerialListener());
		} catch (TooManyListeners e) {
			e.printStackTrace();
		}
	}

	/**
	 * 点击事件-关闭串口
	 */
	private void closeSerialPort(java.awt.event.ActionEvent evt) {
		SerialPortManager.closePort(serialport);
		serialport = null;
		dataView.setText("串口已关闭" + "\r\n");
		btnSerialPortSwitch.setText("打开串口");
	}

	/**
	 * 点击事件-发送数据
	 */
	private void sendData(java.awt.event.ActionEvent evt) {
		String data = dataInput.getText().toString();
		try {
			SerialPortManager.sendToPort(serialport, ByteUtils.hexStr2Byte(data));
			// SerialPortManager.sendToPort(serialport,ByteUtils.Str2Byte(data));
		} catch (SendDataToSerialPortFailure e) {
			e.printStackTrace();
		} catch (SerialPortOutputStreamCloseFailure e) {
			e.printStackTrace();
		}
	}

	private class SerialListener implements SerialPortEventListener {
		/**
		 * 处理监控到的串口事件
		 */
		public void serialEvent(SerialPortEvent serialPortEvent) {

			switch (serialPortEvent.getEventType()) {

			case SerialPortEvent.BI: // 10 通讯中断
				ShowUtils.errorMessage("与串口设备通讯中断");
				break;

			case SerialPortEvent.OE: // 7 溢位（溢出）错误

			case SerialPortEvent.FE: // 9 帧错误

			case SerialPortEvent.PE: // 8 奇偶校验错误

			case SerialPortEvent.CD: // 6 载波检测

			case SerialPortEvent.CTS: // 3 清除待发送数据

			case SerialPortEvent.DSR: // 4 待发送数据准备好了

			case SerialPortEvent.RI: // 5 振铃指示

			case SerialPortEvent.OUTPUT_BUFFER_EMPTY: // 2 输出缓冲区已清空
				break;

			case SerialPortEvent.DATA_AVAILABLE: // 1 串口存在可用数据
				byte[] data = null;
				try {
					if (serialport == null) {
						ShowUtils.errorMessage("串口对象为空！监听失败！");
					} else {
						// 读取串口数据
						data = SerialPortManager.readFromPort(serialport);
						dataView.append(ByteUtils.byteArray2Str(data) + "\r\n");
						// dataView.append(ByteUtils.byteArrayToHexString(data,
						// true) + "\r\n");
					}
				} catch (Exception e) {
					ShowUtils.errorMessage(e.toString());
					// 发生读取错误时显示错误信息后退出系统
					System.exit(0);
				}
				break;
			}
		}
	}

	public static void main(String args[]) {
		java.awt.EventQueue.invokeLater(new Runnable() {
			public void run() {
				new MainFrame().setVisible(true);
			}
		});
	}
}