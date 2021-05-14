import java.net.Socket;

public class room {
	int room_number;
	Socket sck[];
	int member_number;
	
	room(){
		room_number = 0;
		sck = new Socket[6];
		member_number = 0;
	}
}
