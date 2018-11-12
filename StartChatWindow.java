import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.DatagramPacket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class StartChatWindow extends JFrame {
	
	private JTextField ipAddrTextField, portTextField;
	private JButton connectButton;
	private Socket socket;
	private Map<String, ChatWindow> listOfActiveChat = new ConcurrentHashMap<String, ChatWindow>();
	
	public StartChatWindow(int port) {
		
		//Initialize socket
		this.socket = new Socket(port);
		
		//Creating the frame
		setDefaultCloseOperation(EXIT_ON_CLOSE);	
		setTitle("Initiate Chat");
		setSize(400, 150);
		setResizable(false);
		
		//Creating a view
		
		//Main panel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		getContentPane().add(mainPanel);
		
		//Form Panel
		JPanel panelForm = new JPanel(new GridBagLayout());
		mainPanel.add(panelForm);
		
		GridBagConstraints gc = new GridBagConstraints();
		gc.gridx = 0;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_END;
		 
		panelForm.add(new JLabel("IP Address: "), gc);
		gc.gridy++;
		
		panelForm.add(new JLabel("Port: "), gc);
		
		gc.gridx = 1;
		gc.gridy = 0;
		gc.anchor = GridBagConstraints.LINE_START;
		
		//IP label and textfield
		ipAddrTextField = new JTextField(15);
		panelForm.add(ipAddrTextField, gc);
		gc.gridy++;
		
		//Port label and text field
		portTextField = new JTextField(8);
		panelForm.add(portTextField, gc);
		gc.gridy++;
		
		//Connect button and action
		connectButton = new JButton("Connect");
		connectButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!ipAddrTextField.getText().isEmpty() && !portTextField.getText().isEmpty() &&
						!listOfActiveChat.containsKey(ipAddrTextField.getText()+":"+portTextField.getText())) {
							listOfActiveChat.put(ipAddrTextField.getText()+":"+portTextField.getText(), 
													new ChatWindow(socket, ipAddrTextField.getText(), portTextField.getText()));
					ipAddrTextField.setText("");
					portTextField.setText("");
				}
			}
		});
		panelForm.add(connectButton, gc);
		
		setVisible(true);
		
		//Keep it running and deal with the packets that are being received
		Thread removeWindowThread = new Thread(
				new Runnable() {
					public void run() {
						while(true) {
							receivedPacket();
							if (!listOfActiveChat.isEmpty()) {
								removeWindowFromHashMap();
							}
						}
					}
				});
		removeWindowThread.start();
		
	}
	
	//Deal with all the incoming packets and distribute the message to correct chat windows 
	private void receivedPacket() {
		DatagramPacket packet;
		
		do {
			packet = socket.receive();
			if (packet != null) {
				String message = new String(packet.getData()).trim();
				
				//Create new chat window if there's no active chat session
				String ipPort = new String(packet.getAddress().toString().substring(1)+":"+Integer.toString(packet.getPort()));
 				if (!listOfActiveChat.containsKey(ipPort)) {
					listOfActiveChat.put(ipPort, new ChatWindow(socket, 
																packet.getAddress().toString().substring(1),
																Integer.toString(packet.getPort())));
				}
				//Append the incoming message to chattextarea of the chatwindow
				listOfActiveChat.get(ipPort).appendMessageToChatTextArea(packet.getAddress().getHostAddress()
																		+": "+ 
																		message 
																		+ "\n");
			}
		} while(packet != null);
	}
	
	//Remove the chat window from listOfActiveChat when you close the chat window
	private void removeWindowFromHashMap() {
		for(ChatWindow chatWindow: listOfActiveChat.values()) {
			if(!chatWindow.isVisible()) {
				listOfActiveChat.remove(chatWindow.getIpPort());
			}
		}
	}

}
