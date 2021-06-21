import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class timer_thread extends Thread{
	room nowroom;
	int count_turn;
	
	public timer_thread(room temp_room) {
		this.nowroom = temp_room;
		this.count_turn = 0;
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
		for(int j = 0; j < 2; j++) {
	    	while(count_turn != nowroom.member_number) {
				BufferedWriter t_write4 = null;
				for(int i = 0; i < nowroom.member_number; i++) {
					String line = "now_turn|" + nowroom.info[count_turn].name;
					Socket temp = (Socket)nowroom.info[i].sck;
					try {
						t_write4 = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						send_info(line, t_write4);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					if(count_turn == i) {
						String line2 = "your_turn|";
						Socket temp2 = (Socket)nowroom.info[i].sck;
						try {
							t_write4 = new BufferedWriter(new OutputStreamWriter(temp2.getOutputStream(), "EUC-KR"));
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
						try {
							send_info(line2, t_write4);
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				try {
					Thread.sleep(40000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				count_turn++;
	    	}
	    	System.out.println(j + "회차 종료");
	    	count_turn = 0;
		}
    	BufferedWriter t_write = null;
    	for(int i = 0; i < nowroom.member_number; i++) {
    		Socket temp = (Socket)nowroom.info[i].sck;
    		try {
				t_write = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		try {
				send_info("vote", t_write);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
	}
}