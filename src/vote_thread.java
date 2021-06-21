import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;


public class vote_thread extends Thread{
	room nowroom;
	
	public vote_thread(room temp_room) {
		this.nowroom = temp_room;
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
		System.out.println("while 시작");
		
		while(nowroom.vote_num != nowroom.member_number) {
			Thread.yield();
		}
		
		System.out.println("while 끝");
		
		int vote = 0;
		int index = 0;
		for(int i = 0; i < nowroom.member_number; i++) {
			if(nowroom.info[i].vote_num > vote) { 
				vote = nowroom.info[i].vote_num;
				index = i;
			}
		}
		
		System.out.println("제일 투포 많이 받은 사람" + nowroom.info[index].name);
		
		String fake = "";
		for(int i = 0; i < nowroom.member_number; i++) {
			if(nowroom.info[i].is_liar) {
				fake = nowroom.info[i].name;
			}
		}
		
		System.out.println("가짜인 사람 사람" + fake);
		
		String send_line3 = "game_result|" + nowroom.info[index].name + "|" + fake;
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
				send_info(send_line3, t_write);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		nowroom.vote_num = 0;
		for(int i = 0; i < nowroom.member_number; i++) {
			nowroom.info[i].vote_num = 0;
		}
		nowroom.vote_thread1 = null;
	}
}
