import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class main_server {

	public static void main(String[] args) {
		
		List<Socket> user_list = new ArrayList<Socket>(20);
		List<room> room_list = new ArrayList<room>(4);
		ServerSocket server_socket = null;
		
		try {
			server_socket = new ServerSocket(8989);
			
			while(true) {
				System.out.println("================");
				System.out.println("Waiting client...");
				
				Socket client_socket = server_socket.accept();
				
				try {
                    for (int i = 0; i < user_list.size(); i++) {
                        Socket s = (Socket) user_list.get(i);
                        if (s == null) {
                        	user_list.remove(i);
                     
                        	//user_list.trimToSize();
                        	System.out.println("Someone get out");
                        }
                    }
                } catch (Exception e) {
                    System.out.println(e.toString());
                }
				
				user_list.add(client_socket);
				
				Client_thread client_thread = new Client_thread(client_socket, user_list, room_list);
				client_thread.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				server_socket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
