import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class main_server {

	public static void main(String[] args) {
		
		Vector<Socket> user_list = new Vector<>(20);
		Vector<Vector <Socket>> room_list = new Vector<>();
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
                        	user_list.trimToSize();
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
