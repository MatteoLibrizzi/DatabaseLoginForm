import java.io.BufferedReader;
import java.io.IOException;
import java.lang.String;
import java.net.Socket;
import java.net.SocketException;

public class Receiver extends Thread{
    BufferedReader br;

    public Receiver(BufferedReader br){
        this.br=br;
    }

    @Override
    public void run(){
        try{
            while(true){

                //Client receiver, always listening and printing right after, all the controls on the messagge are done by the server
                String J=String.format(br.readLine());
                System.out.println(String.format("%n"+J));
            }
		}catch(SocketException e){
			//does nothing so the user  doesn't see the exception once the socket is closed
		}catch(IOException e){
            e.printStackTrace();
        }catch(java.lang.NullPointerException e){}
    }
}