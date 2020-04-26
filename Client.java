import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Base64;

//Client to connect to the server, should only send, received messagges should be handled by a separate thread
public class Client {
	public static final int port=1024;
	private static Base64.Encoder encoder=Base64.getEncoder();
	private static Base64.Decoder decoder=Base64.getDecoder();
	private static Crypt crypto=new Crypt("RSA","AES","SHA-256");

	public static void main(String[] args){
		String answer="NO";
		try{
			while(answer.equals("NO")){
				System.out.println("Do you want to connect to the server?\nYES - NO");//this happens before connecting to the server
				answer=System.console().readLine();
			}
			Socket socket=new Socket("localhost",port);//client socket creation
            //I/O Streams
            PrintWriter pw=new PrintWriter(socket.getOutputStream(),true);
			BufferedReader br=new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			Receiver r=new Receiver(br);//obj to print the received messagges from the server
			r.start();

			while(true){
				String msg=System.console().readLine();//because the receiving is handled by a different thread all this has to do is send messagges

				if(msg.equals("QUIT")){
					pw.println(msg);
					r.interrupt();
					socket.close();
					break;
				}else{
						pw.println(msg);
				}
			}
			System.out.println("OVER");
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}