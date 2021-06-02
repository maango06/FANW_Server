import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
//import java.util.concurrent.locks.Lock;
import java.lang.Math;


public class Client_thread extends Thread {
	
	Socket sck;
	List user_list;
	List<room> room_list;
	BufferedWriter writer;
	BufferedReader reader;
	String nickname;
	String user_id;
	String Thumbnail;
	int room_number;
	int index_in_room;
	
	
	public Client_thread(Socket socket, List user_list, List<room> room_list) {
		this.user_list = user_list;
		this.sck = socket;
		this.room_list = room_list;
	}
	
	public void send_info(String message, BufferedWriter writer) throws IOException {
		synchronized(this) {
			writer.write(message);
			writer.newLine();
			writer.flush();
		}
	}
	
	@Override
	public void run() {
		try {
			reader = new BufferedReader(new InputStreamReader(sck.getInputStream(), "EUC_KR"));
			writer = new BufferedWriter(new OutputStreamWriter(sck.getOutputStream(), "EUC-KR"));
			String line;
			line = reader.readLine();
			System.out.println(line);
			String[] information = line.split("\\|");
			switch(information[0]) {
			case "login":
				this.user_id = information[1];
				this.nickname = information[2];
				this.Thumbnail = information[3];
				send_info("login success", this.writer);
				/*writer.write("login success");
				writer.newLine();
				writer.flush();*/
			}
			
			while(true) {
				System.out.println("[system]" + nickname + " : waiting input...");
				line = reader.readLine();
				if (line == null) {
					break;
				}
				System.out.println(line);
				String[] info = line.split("\\|");
				switch(info[0]) {
				
				// 방 갯수 무한대로 바꾸기
				case "nickname":
					this.nickname = info[1];
					send_info("nickname|S|" + this.nickname, this.writer);
					System.out.println("success nickname");
					break;
					
				case "make_room":
					synchronized(this) {
					if(room_list.size() == 4) {
						send_info("make_room|E|", this.writer);
						System.out.println("Error - make room");
					}
					else {
						//난수 중복 확인
						System.out.println("make room");
						boolean flag = true;
						int rand_number = 0;
						while(flag) {
							double dValue = Math.random();
							rand_number = (int)(dValue*1000000);
							int i;
							for(i = 0; i < room_list.size(); i++) {
								if(room_list.get(i).room_number == rand_number) {
									break;
								}
							}
							if(i == room_list.size()) {
								flag = false;
							}
						}
						
						room_number = rand_number;
						room newroom = new room();
						newroom.room_number = rand_number;
						newroom.info[0].sck = this.sck;
						newroom.info[0].Thumbnail = this.Thumbnail;
						newroom.info[0].name = this.nickname;
						newroom.info[0].user_id = this.user_id;
						newroom.member_number++;
						room_list.add(newroom);
						//String temp_string = Integer.toString(rand_number);
						String temp_string = String.format("%06d", rand_number);
						send_info("make_room|" + "S|" + temp_string, this.writer);
						System.out.println("Success - make room");
					}
					}
					break;
				case "enter_room":
					System.out.println("enter room");
					synchronized(this) {
					boolean success = false;
					int k = 0;
					for(int i = 0; i < room_list.size(); i++) {
						if(room_list.get(i).room_number == Integer.parseInt(info[1]) ) {
							if(room_list.get(i).member_number == 6){
								System.out.println("Error - over 6 people");
								send_info("make_room|" + "E", this.writer);
								break;
							}
							else {
								System.out.println("success - enter room");
								room_list.get(i).info[room_list.get(i).member_number].sck = this.sck;
								room_list.get(i).info[room_list.get(i).member_number].name = this.nickname;
								room_list.get(i).info[room_list.get(i).member_number].Thumbnail = this.Thumbnail;
								room_list.get(i).info[room_list.get(i).member_number].user_id = this.user_id;
								index_in_room = room_list.get(i).member_number;
								room_list.get(i).member_number++;
								
								int roomnumber = room_list.get(i).room_number;
								//String temp_string = Integer.toString(roomnumber);
								String temp_string = String.format("%06d", roomnumber);
								send_info("make_room|" + "S|" + temp_string, this.writer);
								room_number = Integer.parseInt(info[1]);
								room_list.get(i).member_number++;
								k++;
								success = true;
							}
						}
					}
					if(!success && k == 4) {
						System.out.println("Error - No such room_number");
						send_info("enter_room|" + "E", this.writer);
					}
					
					room temp_room1 = null;
					for(int i = 0; i < 4; i++) {
						if(room_list.get(i).room_number == room_number) {
							temp_room1 = room_list.get(i);
							break;
						}
					}
					String send_line1 = "enter_room|" + this.nickname + "::" + this.user_id + "::" + this.Thumbnail;
					BufferedWriter t_write;
					for(int i = 0; i < temp_room1.member_number; i++) {
						Socket temp = (Socket) temp_room1.info[i].sck;
						t_write = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(send_line1, t_write);
					}
					}
					break;
					
				case "message":
					synchronized(this) {
					room temp_room = null;
					for(int i = 0; i < room_list.size(); i++) {
						if(room_list.get(i).room_number == room_number) {
							temp_room = room_list.get(i);
						}
					}
					String send_line = "message|" + this.nickname + "|" + info[2];
					BufferedWriter t_write;
					for(int i = 0; i < temp_room.member_number; i++) {
						Socket temp = (Socket) temp_room.info[i].sck;
						if (temp.isClosed()) {
							user_list.remove(i);
	                    	i--;
	                    	System.out.println("[system]Someone get out");
						} else {
							t_write = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
							send_info(send_line, t_write);
						}
					}
					System.out.println("[system]" + nickname + " : finished");
					}
					break;
					
				case "room_info":
					synchronized(this) {
					room temp_room2 = null;
					for(int i = 0; i < room_list.size(); i++) {
						if(room_list.get(i).room_number == room_number) {
							temp_room2 = room_list.get(i);
							break;
						}
					}
					String send_line2 = "room_info|";
					for(int i = 0; i < temp_room2.member_number - 1; i++) { // -1했음
						send_line2 = send_line2 + temp_room2.info[i].name + "::" + temp_room2.info[i].user_id + "::" + temp_room2.info[i].Thumbnail + "|";
					}
					send_info(send_line2, this.writer);
					}
					break;
				case "quit_room":
					synchronized(this) {
						
					}
					break;
				default:
					System.out.println("unknown message" + this.nickname + " : " + line);
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