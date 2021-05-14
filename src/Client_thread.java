import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.lang.Math;

public class Client_thread extends Thread {
	
	Socket sck;
	List user_list;
	List<room> room_list;
	BufferedWriter writer;
	BufferedReader reader;
	String name;
	String user_id;
	int num_room = 0;
	
	
	
	public Client_thread(Socket socket, List user_list, List<room> room_list) {
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
					if(room_list.size() == 4) {
						writer.write("make_room/" + "E/");
						System.out.println("Error - make room");
					}
					else {
						System.out.println("make room");
						double dValue = Math.random();
						int rand_number = (int)(dValue*1000000);
						room newroom = new room();
						newroom.room_number = rand_number;
						newroom.sck[newroom.member_number] = this.sck;
						newroom.member_number++;
						room_list.add(newroom);
						num_room++;
						writer.write("make_room/" + "S/" + Integer.toString(rand_number));
						System.out.println("Success - make room");
					}
					
				case "enter_room":
					System.out.println("enter room");
					boolean success = false;
					int k = 0;
					for(int i = 0; i < 3; i++) {
						if(room_list.get(i).room_number == Integer.parseInt(info[1]) ) {
							if(room_list.get(i).member_number == 6){
								System.out.println("Error - over 6 people");
								writer.write("make_room/" + "E");
								break;
							}
							else {
								System.out.println("success - enter room");
								room_list.get(i).sck[room_list.get(i).member_number + 1] = this.sck;
								room_list.get(i).member_number++;
								writer.write("make_room/" + "S/" + Integer.toString(room_list.get(i).room_number));
								k++;
								success = true;
							}
						}
					}
					if(!success && k == 3) {
						System.out.println("Error - No such room_number");
						writer.write("make_room/" + "E");
					}
					
				case "message":
					line = name + " : " + info[1];
					System.out.println(line);
					BufferedWriter t_write;
					for(int i = 0; i < user_list.size(); i++) {
						Socket temp = (Socket) user_list.get(i);
						if (temp.isClosed()) {
							user_list.remove(i);
	                    	//user_list.trimToSize();
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