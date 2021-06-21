import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.List;
import java.util.Random;
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
	room in_room;
	boolean is_moderator;
	String thickness;
	String color;
	String big;
	String small;
	
	public int getIndex() {
		int i;
		for(i = 0; i < in_room.member_number; i++) {
			if(in_room.info[i].user_id.equals(this.user_id)) {
				break;
			}
		}
		return i;
	}
	
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
			System.out.println("안드로이드에한테 보내는거" + message);
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
			}
			
			while(true) {
				System.out.println("[system]" + nickname + " : waiting input...");
				line = reader.readLine();
				if (line == null) {
					break;
				}
				System.out.println("명령어 도착: " + line);
				String[] info = line.split("\\|");
				switch(info[0]) {

				case "nickname":
					System.out.println("nickname");
					this.nickname = info[1];
					send_info("nickname|S|" + this.nickname, this.writer);
					System.out.println("success nickname");
					break;
					
				case "make_room":
					synchronized(this) {
					is_moderator = true;
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
						String temp_string = String.format("%06d", rand_number);
						send_info("make_room|" + "S|" + temp_string, this.writer);
						System.out.println("Success - make room");
						in_room = newroom;
					}
					}
					break;
					
				case "enter_room":
					System.out.println("enter room");
					synchronized(this) {
					is_moderator = false;
					boolean success = false;
					room temp_room = null;
					for(int i = 0; i < room_list.size(); i++) {
						if(room_list.get(i).room_number == Integer.parseInt(info[1]) ) {
							in_room = room_list.get(i);
							temp_room = room_list.get(i);
							if(room_list.get(i).member_number == 6){	
								System.out.println("Error - over 6 people");
								break;
							}
							else {
								System.out.println("success - enter room");
								room_list.get(i).info[room_list.get(i).member_number].sck = this.sck;
								room_list.get(i).info[room_list.get(i).member_number].name = this.nickname;
								room_list.get(i).info[room_list.get(i).member_number].Thumbnail = this.Thumbnail;
								room_list.get(i).info[room_list.get(i).member_number].user_id = this.user_id;
								room_list.get(i).member_number++;
								int roomnumber = room_list.get(i).room_number;

								String temp_string = String.format("%06d", roomnumber);
								send_info("enter_room|S|" + temp_string, this.writer);
								room_number = Integer.parseInt(info[1]);
								success = true;
							}
						}
					}
					if(!success) {
						System.out.println("Error - No such room_number");
						send_info("enter_room|" + "E", this.writer);
						break;
					}
					
					String send_line1 = "enter_room|" + this.nickname + "::" + this.user_id + "::" + this.Thumbnail;
					BufferedWriter t_write;
					for(int i = 0; i < temp_room.member_number - 1; i++) {
						if((Socket) temp_room.info[i].sck == this.sck) continue;
						Socket temp = (Socket) temp_room.info[i].sck;
						t_write = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(send_line1, t_write);
					}
					}
					break;
					
				case "message":
					System.out.println("message");
					synchronized(this) {
					room temp_room = null;
					for(int i = 0; i < room_list.size(); i++) {
						if(room_list.get(i).room_number == room_number) {
							temp_room = room_list.get(i);
						}
					}
					String send_line = "message|" + this.nickname + "|" + info[1];
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
					System.out.println("room info");
					synchronized(this) {
					room temp_room2 = null;
					for(int i = 0; i < room_list.size(); i++) {
						if(room_list.get(i).room_number == room_number) {
							System.out.println(room_list.get(i).room_number);
							System.out.println(room_number);
							temp_room2 = room_list.get(i);
							break;
						}
					}
					String send_line2 = "room_info|";
					for(int i = 0; i < temp_room2.member_number; i++) {
						send_line2 = send_line2 + temp_room2.info[i].name + "::" + temp_room2.info[i].user_id + "::" + temp_room2.info[i].Thumbnail + "|";
					}
					send_info(send_line2, this.writer);
					}
					break;
					
				case "quit":					
					synchronized(this) {
						String temp_string = "quit_room|" + in_room.info[getIndex()].user_id;
						BufferedWriter t_write;
						for(int i = 0; i < in_room.member_number; i++) {
							if(this.sck == (Socket)in_room.info[i].sck) continue;
							Socket temp = (Socket)in_room.info[i].sck;
							t_write = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
							send_info(temp_string, t_write);
						}
						for(int i = getIndex(); i < in_room.member_number - 1; i++) {
							in_room.info[i].sck = in_room.info[i + 1].sck;
							in_room.info[i].name = in_room.info[i + 1].name;
							in_room.info[i].Thumbnail = in_room.info[i + 1].Thumbnail;
							in_room.info[i].user_id = in_room.info[i + 1].user_id;
						}
						in_room.member_number--;
					}
					break;
							
				case "topic":
					BufferedWriter t_write6;
					String send_line;
					
					if(in_room.info[getIndex()].is_liar) {
						send_line = "topic|" + in_room.big + "|false";
						Socket temp = (Socket)in_room.info[getIndex()].sck;
						t_write6 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(send_line, t_write6);
					}
					else {
						Socket temp = (Socket)in_room.info[getIndex()].sck;
						t_write6 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_line = "topic|" + in_room.big + "|" + in_room.small;
						send_info(send_line, t_write6);
					}
					
					break;
				case "thickness":
					BufferedWriter t_write;
					for(int i = 0; i < in_room.member_number; i++) {
						if(in_room.info[i].sck == this.sck) continue;
						Socket temp = (Socket)in_room.info[i].sck;
						t_write = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(line, t_write);
					}
					System.out.println("thickness success");
					break;
					
				case "color":
					BufferedWriter t_write1;
					for(int i = 0; i < in_room.member_number; i++) {
						if(in_room.info[i].sck == this.sck) continue;
						Socket temp = (Socket)in_room.info[i].sck;
						t_write1 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(line, t_write1);
					}
					System.out.println("color success");
					break;
					
				case "draw_up":
					BufferedWriter t_write2;
					for(int i = 0; i < in_room.member_number; i++) {
						if(in_room.info[i].sck == this.sck) continue;
						Socket temp = (Socket)in_room.info[i].sck;
						t_write2 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(line, t_write2);
					}
					break;
					
				case "draw_down":
					BufferedWriter t_write3;
					for(int i = 0; i < in_room.member_number; i++) {
						if(in_room.info[i].sck == this.sck) continue;
						Socket temp = (Socket)in_room.info[i].sck;
						t_write3 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(line, t_write3);
					}
					break;
					
				case "draw_move":
					BufferedWriter t_write4;
					for(int i = 0; i < in_room.member_number; i++) {
						if(in_room.info[i].sck == this.sck) continue;
						Socket temp = (Socket)in_room.info[i].sck;
						t_write4 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(line, t_write4);
					}
					break;
					
				case "clear":
					BufferedWriter t_write5;
					for(int i = 0; i < in_room.member_number; i++) {
						if(in_room.info[i].sck == this.sck) continue;
						Socket temp = (Socket)in_room.info[i].sck;
						t_write5 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(line, t_write5);
					}
					break;
					
				case "game_start":
					Random rand = new Random();
					int rand_liar = rand.nextInt(in_room.member_number);	
					for(int i = 0; i < in_room.member_number; i++) {
						in_room.info[i].is_liar = false;
					}	
					in_room.info[rand_liar].is_liar = true;
					int random1 = rand.nextInt(6);
					int random2 = rand.nextInt(3);
					if(random1 == 0 && random2 == 0) {
						this.big = "동물";
						this.small = "호랑이";
					}
					else if(random1 == 0 && random2 == 1) {
						this.big = "동물";
						this.small = "사슴";
					}
					else if(random1 == 0 && random2 == 2) {
						this.big = "동물";
						this.small = "고양이";
					}
					
					else if(random1 == 1 && random2 == 0) {
						this.big = "장소";
						this.small = "학교";
					}
					else if(random1 == 1 && random2 == 1) {
						this.big = "장소";
						this.small = "병원";
					}
					else if(random1 == 1 && random2 == 2) {
						this.big = "장소";
						this.small = "대형마트";
					}
					
					else if(random1 == 2 && random2 == 0) {
						this.big = "직업";
						this.small = "어부";
					}
					else if(random1 == 2 && random2 == 1) {
						this.big = "직업";
						this.small = "선생님";
					}
					else if(random1 == 2 && random2 == 2) {
						this.big = "직업";
						this.small = "선생님";
					}
					
					else if(random1 == 3 && random2 == 0) {
						this.big = "먹을 것";
						this.small = "불고기";
					}
					else if(random1 == 3 && random2 == 1) {
						this.big = "먹을 것";
						this.small = "햄버거";
					}
					else if(random1 == 3 && random2 == 2) {
						this.big = "먹을 것";
						this.small = "치킨";
					}
					else if(random1 == 4 && random2 == 0) {
						this.big = "탈 것";
						this.small = "비행기";
					}
					else if(random1 == 4 && random2 == 1) {
						this.big = "탈 것";
						this.small = "기차";
					}
					else if(random1 == 4 && random2 == 2) {
						this.big = "탈 것";
						this.small = "자동차";
					}
					
					else if(random1 == 5 && random2 == 0) {
						this.big = "감정";
						this.small = "기쁨";
					}
					else if(random1 == 5 && random2 == 1) {
						this.big = "감정";
						this.small = "슬픔";
					}
					else if(random1 == 5 && random2 == 2) {
						this.big = "감정";
						this.small = "화남";
					}
					
					in_room.big = this.big;
					in_room.small = this.small;				
					String send_line2 = "game_start";
					BufferedWriter t_write7;
					for(int i = 0; i < in_room.member_number; i++) {
						Socket temp = (Socket)in_room.info[i].sck;
						t_write7 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						send_info(send_line2, t_write7);
					}
					timer_thread timer_thread = new timer_thread(in_room);
					timer_thread.start();
					break;
					
				case "vote":
					if(in_room.vote_thread1 == null) {
						System.out.println("현재 null임");
						in_room.vote_thread1 = new vote_thread(in_room);
						in_room.vote_thread1.start();
					}
					in_room.vote_num++;
					in_room.info[Integer.parseInt(info[1])].vote_num++;
					System.out.println("보트 넘버: " + in_room.vote_num);
					System.out.println("멤버 넘버: " + in_room.member_number);
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