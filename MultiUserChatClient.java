import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

class MessageHandler extends Thread {
	BufferedReader socketInput;
	
	public MessageHandler(BufferedReader socketInput){	
		this.socketInput = socketInput;
		
	}
	
	public void run() {
		while(MultiUserChatClient.active) {
			try {
				if(socketInput != null && socketInput.ready()) {
					String message = socketInput.readLine();
					System.out.println(message);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	
}

public class MultiUserChatClient extends Thread {

	public static boolean active;

	public static void main(String[] args) {
		String serverAddress = "localhost";
		if(args.length > 0){
			serverAddress = args[0];
		}
		try( 
			BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
			PrintStream userOutput = new PrintStream(System.out);
			
			Socket socket = new Socket(serverAddress, 3000);
			BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			PrintStream socketOutput = new PrintStream(socket.getOutputStream());
			){

			userOutput.print("Type your name:");
			String name = userInput.readLine();
			socketOutput.println(name);
			
			active = true;
			new MessageHandler(socketInput).start();
			userOutput.println("Type your messages to everyone (bye to exit)");
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
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			/*
			try {
				//Thread.sleep(2000);
				if(socket != null && ! socket.isClosed()) {
					socket.close();
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
			*/
		}

	}

}
