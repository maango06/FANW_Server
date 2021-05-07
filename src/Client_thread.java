import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

public class Client_thread extends Thread {
	
	Socket sck;
	Vector user_list;
	Vector<Vector<Socket>> room_list;
	BufferedWriter writer;
	BufferedReader reader;
	String name;
	String user_id;
	
	
	
	public Client_thread(Socket socket, Vector user_list, Vector<Vector<Socket>> room_list) {
		this.user_list = user_list;
		this.sck = socket;
		this.room_list = room_list;
	}
	
	@Override
	public void run() {
		try {
			reader = new BufferedReader(new InputStreamReader(sck.getInputStream(), "EUC_KR"));
			writer = new BufferedWriter(new OutputStreamWriter(sck.getOutputStream(), "EUC-KR"));
			String line;
			line = reader.readLine();
			System.out.println(line);
			String[] information = line.split("/");
			switch(information[0]) {
			case "login":
				this.user_id = information[1];
				this.name = information[2];
				writer.write("login success");
				writer.newLine();
				writer.flush();
			}
			
			while(true) {
				System.out.println("[system]" + name + " : waiting input...");
				line = reader.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
				String[] info = line.split("/");
				switch(info[0]) {
				case "make_room":
					System.out.println("make room plz");
					break;
				case "message":
					line = name + " : " + info[1];
					System.out.println(line);
					BufferedWriter t_write;
					for(int i = 0; i < user_list.size(); i++) {
						Socket temp = (Socket) user_list.get(i);
						if (temp.isClosed()) {
							user_list.remove(i);
	                    	user_list.trimToSize();
	                    	i--;
	                    	System.out.println("[system]Someone get out");
						} else {
							
							t_write = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
							t_write.write(line);
							t_write.newLine();
							t_write.flush();
						}
					}
					System.out.println("[system]" + name + " : finished");
					break;
				default:
					System.out.println("unknown message" + this.name + " : " + line);
				}
				
				
			}
		} catch (IOException e) {
			System.out.println("socket close");
		} finally {
			try {
				sck.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
	}
	
	
}
