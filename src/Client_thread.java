import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Vector;

public class Client_thread extends Thread {
	
	Socket sck;
	Vector user_list;
	BufferedWriter writer;
	String name;
	String user_id;
	
	
	
	public Client_thread(Vector user_list, Socket socket) {
		this.user_list = user_list;
		this.sck = socket;
	}
	
	@Override
	public void run() {
		
		try {
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(sck.getInputStream(), "EUC_KR"));
			String line;
			line = reader.readLine();
			String[] information = line.split("/");
			switch(information[0]) {
			case "login":
				user_id = information[1];
				name = information[2];
			}
			
			while(true) {
				System.out.println("[system]" + name + " : waiting input...");
				line = reader.readLine();
				if (line == null) {
					break;
				}
				line = name + " : " + line;
				System.out.println(line);
				for(int i = 0; i < user_list.size(); i++) {
					Socket temp = (Socket) user_list.get(i);
					if (temp.isClosed()) {
						user_list.remove(i);
                    	user_list.trimToSize();
                    	i--;
                    	System.out.println("[system]Someone get out");
					} else {
						writer = new BufferedWriter(new OutputStreamWriter(temp.getOutputStream(), "EUC-KR"));
						writer.write(line);
						writer.newLine();
						writer.flush();
					}
				}
				System.out.println("[system]" + name + " : finished");
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
