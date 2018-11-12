//Usually you will require both swing and awt packages
// even if you are working with just swings.
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.net.InetAddress;

public class ChatWindow extends JFrame {
	
	private JTextArea chatTextArea;
	private JTextField chatTextField;
	private JButton sendButton;
	private InetAddress destinationIP;
	private int destinationPort;
	private Socket socket;
	
	public ChatWindow(Socket connectionSocket, String ipAddr, String port) {
		
		this.socket = connectionSocket;
		
		try {
			this.destinationIP = InetAddress.getByName(ipAddr);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		try {
			this.destinationPort = Integer.parseInt(port);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(-1);
		}
		
		//Creating the frame
		setSize(500, 500);
		setLocationRelativeTo(null);
		setResizable(false);
		setTitle(ipAddr + ":" + port);
		createView();
		setVisible(true);
        
	}
	
	private void createView() {
		
		//Main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		getContentPane().add(mainPanel);
		
		//Chat Display
		chatTextArea = new JTextArea();
		chatTextArea.setEditable(false);
		chatTextArea.setLineWrap(true);
		chatTextArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(chatTextArea);
		mainPanel.add(scrollPane);
		
		//Jtextfield and send button panel
		JPanel northPanel = new JPanel(new BorderLayout());
		mainPanel.add(northPanel, BorderLayout.SOUTH);
		chatTextField = new JTextField();
		
		//Actionlistener for jtextfield when pressed enter
		chatTextField.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!chatTextField.getText().isEmpty()){
					String message = chatTextField.getText() + "\n";
					socket.send(message, destinationIP, destinationPort);
					chatTextArea.append("Me: " + message);
					chatTextField.setText("");
				}
			}
		});
		
		northPanel.add(chatTextField);
		
		sendButton = new JButton("SEND");
		
		//Send button actionlistener
		sendButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!chatTextField.getText().isEmpty()){
					String message = chatTextField.getText() + "\n";
					socket.send(message, destinationIP, destinationPort);
					chatTextArea.append("Me: " + message);
					chatTextField.setText("");
				}
			}
		});
		
		northPanel.add(sendButton, BorderLayout.EAST);
		
	}
	
	//Append incoming message to chatTextArea
	public void appendMessageToChatTextArea(String message) {
		System.out.println(message);
		chatTextArea.append(message);
	}
	
	//Get the IP:Port as string
	public String getIpPort() {
		return this.destinationIP.toString().substring(1)+":"+ Integer.toString(this.destinationPort);
	}
	
}
