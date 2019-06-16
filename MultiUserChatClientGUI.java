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
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiUserChatClientGUI extends JFrame 
		implements ActionListener {

	final String newLine = System.lineSeparator();
	JTextArea textArea = new JTextArea(30,40);
	JTextArea inputField = new JTextArea(3,40);
	JButton sendButton = new JButton("Send");

	//BufferedReader userInput;
	PrintStream userOutput;
	
	Socket socket;
	BufferedReader socketInput;
	PrintStream socketOutput;
	
	boolean active;
	MessageHandler messageHandler;
	
	public MultiUserChatClientGUI() throws IOException {
		setTitle("Teams/Whatsapp/Slack");

		//userInput = new BufferedReader(new InputStreamReader(System.in));
		userOutput = new PrintStream(System.out);
		
		socket = new Socket("localhost", 3000);
		socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		socketOutput = new PrintStream(socket.getOutputStream());

		textArea.setEditable(false);
		textArea.setBackground(Color.black);
		textArea.setForeground(Color.white);
		this.add(textArea, BorderLayout.CENTER);

		JPanel inputPanel = new JPanel();
		inputPanel.add(inputField);
		inputPanel.add(sendButton);
		this.add(inputPanel, BorderLayout.SOUTH);

		sendButton.addActionListener(this);
		//this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent event){
				close();
			}
		});
		
		this.pack();
		//this.setSize(1200,600);
		this.setLocation(50,20);
		this.setVisible(true);
		
		String message = "Type your name:";
		String name = JOptionPane.showInputDialog(message);
		socketOutput.println(name);
		
		active = true;
		messageHandler = new MessageHandler(socketInput);
		messageHandler.setName(name);
		messageHandler.start();
		message = "Type your messages to everyone (bye to exit)";
		showMessage(message);

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
			userOutput.println("Closing Window...");
			this.dispose();
			System.exit(0);
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void actionPerformed(ActionEvent event) {
		try {
			String msg = inputField.getText();
			msg = msg.replace("\n","|");
			inputField.setText("");
			if(msg.equals("bye")){
				active = false;
				String message = "You are leaving the chat...";
				showMessage(message);
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

	
	public static void main(String[] args) {
		MultiUserChatClientGUI client = null;
		try {
			client = new MultiUserChatClientGUI();
			client.messageHandler.join();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(client != null) {
				client.close();
			}
		}

	}

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
						showMessage(message);
					}
				}catch(Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
