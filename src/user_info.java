import java.net.Socket;

public class user_info {
	Socket sck;
	String Thumbnail;
	String name;
	String user_id;
	boolean is_liar;
	int vote_num;
	
	user_info(){
		sck = new Socket();
		Thumbnail = new String();
		name = new String();
		user_id = new String();
		is_liar = false;
		vote_num = 0;
	}
}
