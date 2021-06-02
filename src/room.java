
public class room {
	int room_number;
	user_info info[];
	int member_number;
	
	room(){
		room_number = 0;
		member_number = 0;
		for(int i = 0; i < 6; i++) {
			info[i] = new user_info();
		}
	}
}