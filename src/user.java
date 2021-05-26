import java.net.Socket;

public class user {
	int room_number;
	Socket sck;
	
	user(){
		room_number = 0;
		sck = new Socket();
	}
}
