import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class main_server {
	
	public static List<Socket> user_list = Collections.synchronizedList(new ArrayList<Socket>(20));
	public static List<room> room_list = Collections.synchronizedList(new ArrayList<room>(4));
	public static ServerSocket server_socket = null;
	
	private static class Client_thread extends Thread {
		
		Socket sck;
		BufferedWriter writer;
		BufferedReader reader;
		String nickname;
		String user_id;
		int room_number = 0;
		String Thumbnail;
		
		public Client_thread(Socket socket) {
			this.sck = socket;
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
						if(room_list.size() == 4) {
							send_info("make_room|" + "E|", this.writer);
							/*writer.write("make_room/" + "E/");
							writer.newLine();
							writer.flush();*/
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
							room newroom = new room();
							newroom.room_number = rand_number;
							newroom.sck[0] = this.sck;
							newroom.Thumbnail[0] = this.Thumbnail;
							newroom.name[0] = this.nickname;
							newroom.member_number++;
							room_list.add(newroom);
							String temp_string = Integer.toString(rand_number);
							if(rand_number < 100000 && rand_number > 10000) {
								temp_string = "0" + temp_string;
							}
							if(rand_number < 10000 && rand_number > 1000) {
								temp_string = "00" + temp_string;
							}
							if(rand_number < 1000 && rand_number > 100) {
								temp_string = "000" + temp_string;
							}
							if(rand_number < 100 && rand_number > 10) {
								temp_string = "0000" + temp_string;
							}
							if(rand_number < 10 && rand_number > 1) {
								temp_string = "00000" + temp_string;
							}
							if(rand_number == 0) {
								temp_string = "000000";
							}
							send_info("make_room|" + "S|" + temp_string, this.writer);
							/*writer.write("make_room/" + "S/" + Integer.toString(rand_number));
							writer.newLine();
							writer.flush();*/
							System.out.println("Success - make room");
						}
						break;
					case "enter_room":
						System.out.println("enter room");
						boolean success = false;
						int k = 0;
						for(int i = 0; i < 4; i++) {
							if(room_list.get(i).room_number == Integer.parseInt(info[1]) ) {
								if(room_list.get(i).member_number == 6){
									System.out.println("Error - over 6 people");
									send_info("make_room|" + "E", this.writer);
									/*writer.write("make_room/" + "E");
									writer.newLine();
									writer.flush();*/
									break;
								}
								else {
									System.out.println("success - enter room");
									room_list.get(i).sck[room_list.get(i).member_number] = this.sck;
									room_list.get(i).name[room_list.get(i).member_number] = this.nickname;
									room_list.get(i).Thumbnail[room_list.get(i).member_number] = this.Thumbnail;
									room_list.get(i).member_number++;
									
									int roomnumber = room_list.get(i).room_number;
									String temp_string = Integer.toString(roomnumber);
									if(roomnumber < 100000 && roomnumber > 10000) {
										temp_string = "0" + temp_string;
									}
									if(roomnumber < 10000 && roomnumber > 1000) {
										temp_string = "00" + temp_string;
									}
									if(roomnumber < 1000 && roomnumber > 100) {
										temp_string = "000" + temp_string;
									}
									if(roomnumber < 100 && roomnumber > 10) {
										temp_string = "0000" + temp_string;
									}
									if(roomnumber < 10 && roomnumber > 1) {
										temp_string = "00000" + temp_string;
									}
									if(roomnumber == 0) {
										temp_string = "000000";
									}
									
									send_info("make_room|" + "S|" + temp_string, this.writer);
									room_number = Integer.parseInt(info[1]);
									room_list.get(i).member_number++;
									/*writer.write("make_room/" + "S/" + Integer.toString(room_list.get(i).room_number));
									writer.newLine();
									writer.flush();*/
									k++;
									success = true;
								}
							}
						}
						if(!success && k == 4) {
							System.out.println("Error - No such room_number");
							send_info("enter_room|" + "E", this.writer);
							/*writer.write("make_room/" + "E");
							writer.newLine();
							writer.flush();*/
						}
						
						room temp_room1 = null;
						for(int i = 0; i < 4; i++) {
							if(room_list.get(i).room_number == room_number) {
								temp_room1 = room_list.get(i);
								break;
							}
						}
						String send_line1 = "enter_room|";
						for(int i = 0; i < temp_room1.member_number; i++) {
							send_line1 = send_line1 + temp_room1.name[i] + "::" + temp_room1.Thumbnail[i] + "|";
						}
						send_info(send_line1, this.writer);
						break;
						
					case "message":
						room temp_room = null;
						for(int i = 0; i < 4; i++) {
							if(room_list.get(i).room_number == room_number) {
								temp_room = room_list.get(i);
							}
						}
						String send_line = "message|" + this.nickname + "|" + info[2];
						BufferedWriter t_write;
						for(int i = 0; i < temp_room.member_number; i++) {
							Socket temp = (Socket) temp_room.sck[i];
							if (temp.isClosed()) {
								user_list.remove(i);
		                    	//user_list.trimToSize();
		                    	i--;
		                    	System.out.println("[system]Someone get out");
							} else {
								t_write = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
								send_info(send_line, t_write);
								/*t_write.write(line);
								t_write.newLine();
								t_write.flush();*/
							}
						}
						System.out.println("[system]" + nickname + " : finished");
						break;
						
					case "room_info":
						room temp_room2 = null;
						for(int i = 0; i < 4; i++) {
							if(room_list.get(i).room_number == room_number) {
								temp_room2 = room_list.get(i);
								break;
							}
						}
						String send_line2 = "room_info|";
						for(int i = 0; i < temp_room2.member_number; i++) {
							send_line2 = send_line2 + temp_room2.name[i] + "::" + temp_room2.Thumbnail[i] + "|";
						}
						send_info(send_line2, this.writer);
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

	public static void main(String[] args) {
		
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
				
				Client_thread client_thread = new Client_thread(client_socket);
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
