
public class room {
	int room_number;
	user_info info[] = new user_info[6];
	int member_number;
	String big;
	String small;
	int vote_num;
	vote_thread vote_thread1;
	
	room(){
		room_number = 0;
		member_number = 0;
		for(int i = 0; i < 6; i++) {
			info[i] = new user_info();
		}
		vote_num = 0;
		vote_thread1 = null;
	}
}