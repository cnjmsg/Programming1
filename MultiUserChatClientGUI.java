import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.WindowConstants;
//import java.awt.event.WindowEvent;
//import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiUserChatClientGUI //extends WindowAdapter 
		implements ActionListener {

	Socket socket;
	BufferedReader socketInput;
	PrintStream socketOutput;
	
	BufferedReader userInput;
	PrintStream userOutput;
	boolean active;
	MessageHandler messageHandler = null;
	
	public final String newLine = System.lineSeparator();
	public JFrame chatWindow = new JFrame("Teams/Whatsapp/Slack");
	public JTextArea textArea = new JTextArea(20,40);
	public JTextArea inputField = new JTextArea(3,40);
	public JButton sendButton = new JButton("Send");

	public MultiUserChatClientGUI() throws IOException {
		userInput = new BufferedReader(new InputStreamReader(System.in));
		userOutput = new PrintStream(System.out);
		
		socket = new Socket("localhost", 3000);
		socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		socketOutput = new PrintStream(socket.getOutputStream());

		textArea.setEditable(false);
		//textArea.setEnabled(false);
		//textArea.setBackground(Color.lightGray);
		textArea.setBackground(Color.black);
		textArea.setForeground(Color.white);
		//inputField.setBackground(Color.yellow);
		chatWindow.add(textArea, BorderLayout.CENTER);
		JPanel inputPanel = new JPanel();
		inputPanel.add(inputField);
		inputPanel.add(sendButton);
		chatWindow.add(inputPanel, BorderLayout.SOUTH);

		chatWindow.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		chatWindow.pack();
		//chatWindow.setSize(1200,600);
		chatWindow.setLocation(50,20);
		chatWindow.setVisible(true);
		
		String message = "Type your name:";
		//userOutput.print(message);
		//String name = userInput.readLine();	
		String name = JOptionPane.showInputDialog(message);
		//textArea.append(message+newLine);
		//String name = inputField.getText();
		//textArea.append(name+newLine);
		socketOutput.println(name);
		
		active = true;
		messageHandler = new MessageHandler(socketInput);
		messageHandler.setName(name);
		messageHandler.start();
		message = "Type your messages to everyone (bye to exit)";
		//userOutput.println(message);
		textArea.append(message+newLine);
		
		sendButton.addActionListener(this);

	}

	public void showMessage(String message) {
		//userOutput.println(message);
		textArea.append(message+newLine);
	}
	
	public void close() {
		try {
			//Thread.sleep(2000);
			if(socket != null && ! socket.isClosed()) {
				socket.close();
			}
			if(chatWindow != null) {
				userOutput.println("Closing Window...");
				chatWindow.dispose();
			}
			System.exit(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent event) {
		try {
			//String msg = userInput.readLine();
			String msg = inputField.getText();
			msg = msg.replace("\n","|");
			inputField.setText("");
			//userOutput.println("Sending:"+msg);
			if(msg.equals("bye")){
				active = false;
				String message = "You are leaving the chat...";
				//userOutput.println(message);
				textArea.append(message+newLine);
				socketOutput.println(msg);
				Thread.sleep(1000);
				close();
			}else {
				socketOutput.println(msg);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	public void windowClosing(WindowEvent event){
		close();
	}
	
	*/
	
	class MessageHandler extends Thread {
		
		BufferedReader socketInput;
		
		public MessageHandler(BufferedReader socketInput){	
			this.socketInput = socketInput;
			
		}
		
		public void run() {
			while(active) {
				try {
					if(socketInput != null && socketInput.ready()) {
						String message = socketInput.readLine();
						//userOutput.println("Received:"+message);
						showMessage(message);
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	public static void main(String[] args) {
		MultiUserChatClientGUI client = null;
		try {
			client = new MultiUserChatClientGUI();
//			client.start();
//			client.join();
			client.messageHandler.join();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(client != null) {
				client.close();
			}
		}

	}

}
