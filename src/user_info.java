import java.net.Socket;

public class user_info {
	Socket sck;
	String Thumbnail;
	String name;
	String user_id;
	
	user_info(){
		sck = new Socket();
		Thumbnail = new String();
		name = new String();
		user_id = new String();
	}
}
