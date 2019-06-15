import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class MultiUserChatServer {

	static List<ClientHandler> clientsList = new CopyOnWriteArrayList();
	
	static void broadcastToClients(String name, String message) {
		clientsList.parallelStream().forEach(clientHandler -> clientHandler.sendMessageToClient(name, message));
	}
	
	static class ClientHandler extends Thread {
		Socket socket;
		PrintStream socketOutput;
		//BufferedReader socketInput;

		//BufferedReader userInput;
		//PrintStream userOutput;
		
		public ClientHandler(Socket s) throws IOException{
				this.socket = s;			
				//this.userInput = new BufferedReader(new InputStreamReader(System.in));
				//this.userOutput = System.out;			
		}
		
		public void sendMessageToClient(String name, String message) {
			if(socketOutput != null) {
				String tabSpace = "\n    ";
				if(name.equals(getName())){
					//String moreTabSpace = "\t\t\t\t";
					String moreTabSpace = "                                                ";
					tabSpace = tabSpace + moreTabSpace;
					name = moreTabSpace + name;
				}
				message = tabSpace + message;
				if(message.contains("|")){
					message = message.replace("|", tabSpace);
				}
				socketOutput.println(name+": " + message);
			}
		}
		
		public void run() {
			try (	BufferedReader socketInput = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					PrintStream socketOutput = new PrintStream(socket.getOutputStream())
				){
				this.socketOutput = socketOutput;
				setName(socketInput.readLine().trim());
				broadcastToClients(getName(),"<ENTERED CHAT>");

				boolean active = true;
				while(active){
					try {
						if(socketInput != null && socketInput.ready()) {
							String message = socketInput.readLine();
							//userOutput.println("Received from "+getName()+":"+message);
							if(message.equals("bye")) {
								active = false;
								clientsList.remove(this);
								broadcastToClients(getName(),"<LEFT CHAT>");
								continue;
							}else {
								broadcastToClients(getName(),message);
							}
						}
						
					}catch(Exception e2) {
						e2.printStackTrace();
					}
				}
			}catch(Exception e) {
				e.printStackTrace();
			}finally {
				try {
					Thread.sleep(5000);
					if(socket != null && ! socket.isClosed()) {
						socket.close();
					}
				} catch (Exception e3) {
					e3.printStackTrace();
				}
			}
			
		}
	}

	public static void main(String[] args) throws IOException {
		try (ServerSocket server = new ServerSocket(3000)){
			System.out.println("Created ServerSocket at IPAddress:"+server.getLocalSocketAddress() + " and Port:"+server.getLocalPort());
			System.out.println("Waiting for Connections");
			while(true) {
				try {
					Socket socket = server.accept();
					ClientHandler clientHandler = new ClientHandler(socket);
					clientsList.add(clientHandler);
					clientHandler.start();
				}catch(Exception e2) {
					e2.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

}
