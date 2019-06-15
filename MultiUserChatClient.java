import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class MultiUserChatClient extends Thread {

	Socket socket;
	BufferedReader socketInput;
	PrintStream socketOutput;
	
	BufferedReader userInput;
	PrintStream userOutput;
	boolean active;
	
	public MultiUserChatClient() throws IOException {
		userInput = new BufferedReader(new InputStreamReader(System.in));
		userOutput = new PrintStream(System.out);
		
		socket = new Socket("localhost", 3000);
		socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		socketOutput = new PrintStream(socket.getOutputStream());

		userOutput.print("Type your name:");
		String name = userInput.readLine();
		socketOutput.println(name);
		
		active = true;
		new MessageHandler(socketInput).start();
		userOutput.println("Type your messages to everyone (bye to exit)");

	}

	public void run() {
		try {
			while(active) {
				try {
					String msg = userInput.readLine();
					//userOutput.println("Sending:"+msg);
					if(msg.equals("bye")){
						active = false;
						userOutput.println("You are leaving the chat...");
						socketOutput.println(msg);
						continue;
					}else {
						socketOutput.println(msg);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}								
			}
		}finally {
			close();
		}

	}
	
	public void showMessage(String message) {
		userOutput.println(message);	
	}
	
	public void close() {
		try {
			//Thread.sleep(2000);
			if(socket != null && ! socket.isClosed()) {
				socket.close();
			}
		}catch(Exception e) {
			e.printStackTrace();
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
		MultiUserChatClient client = null;
		try {
			client = new MultiUserChatClient();
			client.start();
			client.join();
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(client != null) {
				client.close();
			}
		}

	}

}
