import java.net.Socket;

public class room {
	int room_number;
	Socket sck[];
	String Thumbnail[];
	String name[];
	int member_number;
	
	room(){
		room_number = 0;
		sck = new Socket[7];
		member_number = 0;
		Thumbnail = new String[7];
		name = new String[7];
	}
}
