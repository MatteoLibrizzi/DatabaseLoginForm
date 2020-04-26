import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.Buffer;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Base64;

//to run: java -cp ./h2-1.4.200.jar Server.java     need to copy the .class file of crypt and reveiver every time you edit them into h2...

public class Server {
	public static final int port = 1024;
	private static Base64.Encoder encoder=Base64.getEncoder();//to transform data from string to byte and viceversa
	private static Base64.Decoder decoder=Base64.getDecoder();
	private static Crypt crypto=new Crypt("RSA","AES","SHA-256");//object to handle everything that has to do with cryptography

	public static void main(String[] args) throws IOException {
		ServerSocket ss = new ServerSocket(port);
		System.out.println("Server running on port " + port);

		try(Connection conn = DriverManager.getConnection("jdbc:h2:~/test","Matteo","Matteo")){//first thing the server connects to the db creating the table if it doesn't exist
			String sqlCreate="CREATE TABLE IF NOT EXISTS users (ID int auto_increment primary key, username char(50),password char(260),salt char(10),mail char(100));";
			int i;
			do{
				Statement stmt=conn.createStatement();
				i=stmt.executeUpdate(sqlCreate);//creating the table if doesn't exist

			}while(i!=0);
			boolean restart=false;
			while(true){
				//Client interface
				Socket socket=ss.accept();
				System.out.println("New connection!");

				PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
				BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				
				boolean loggedin=false;
				String op;
				while(!restart){
					while(!loggedin){
						pw.println("Send any char to go to the menu");//making sure the user doesn't go back to the menu making the output more clear
						br.readLine();

						pw.println("To login type in: 'LOGIN'\nTo signin type in: 'SIGNIN'\nTo get a list of the registered users type in: 'USERS'\nTo get a list of the registered mails type in: 'MAILS'\nTo quit type in: 'QUIT'");//sends the user his options
						
						op = br.readLine();//reads the response
						
						if(!op.equals("LOGIN")&&!op.equals("SIGNIN")&&!op.equals("USERS")&&!op.equals("MAILS")&&!op.equals("QUIT")){//checking if the chosen option is valid
							pw.println("Try typing the word correctly");//if not an error messagge is sent
						}else{
							if(op.equals("LOGIN")){//if user chose login the login function will allow him to do it
								System.out.println("User chose Login");

								boolean b=login(pw,br,conn);
								if(b){
									loggedin=true;//if login returns true (the user is authenticated) the while ends
									pw.println("Congrats you logged in!");
								}else{
									pw.println("Username and/or Password are incorrect!");
								}
							}else{
								if(op.equals("SIGNIN")){//if user chose signin he does so, and afterwards has to login
									signin(pw,br,conn);
								}else{
									if(op.equals("USERS")){
										users(pw,br,conn);
									}else{
										if(op.equals("MAILS")){
											mails(pw,br,conn);
										}else{
											socket.close();
										}										
									}
								}
							}
						}
					}
					pw.println("To log out type in: LOGOUT\nTo close the connection type in: QUIT");
					if(br.readLine().equals("LOGOUT")){
						loggedin=false;
					}else{
						if(br.readLine().equals("QUIT")){
							socket.close();
							restart=true;
						}
					}
				}
			}
		}catch(SQLException e){
			e.printStackTrace();
		}catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	public static boolean login(PrintWriter pw,BufferedReader br,Connection conn) throws IOException, NoSuchAlgorithmException,
			SQLException {
		pw.println("Type in your username: ");
		
		String username=br.readLine();//saves username
		
		pw.println("Type in your password: ");//asks for password
		
		String sPlainPassword=br.readLine();//receiving the password in plain char

		String sqlGetSalt="SELECT salt,password FROM users where '"+username+"'=username";//returns the salt if the username matches the one from the user
		
		try(Statement stmt=conn.createStatement()){//no need to use the prepared statements because we are only getting the salt that gets hashed so it wouldn't be extractable
			System.out.println("Running the following query: "+sqlGetSalt);

			ResultSet rs=stmt.executeQuery(sqlGetSalt);//running the query
			StringBuilder sb=new StringBuilder();

			String[] rows;
			int rown=1;

			while(rs.next()){
				addRow(sb,rs);//this fills the string builder with the results, dividing the single records with "\n" and the single columns with ":" 
				rown++;//keeping track of the number of records (rows)
			}
			String s=sb.toString();//toStringing the whole output of the query

			rows=s.split("\n",rown);//splitting the output of the query in single records (that are divided by the char "\n")
			for(int i=0;i<rows.length-1;i++){

				String[] columns=rows[i].split(":",2);//splitting the single rows in columns, the single fields are divided by ":"

				String sSaltedPassword=sPlainPassword+columns[0];//concatenating the password and the salt
				byte[] bSaltedPassword=sSaltedPassword.getBytes("UTF-8");//getting the bytes...
				byte[] bHashedPassword=crypto.hash(bSaltedPassword);//hashing it
				String sHashedPassword=encoder.encodeToString(bHashedPassword);//getting the string of the hashed password+salt
				if(sHashedPassword.equals(columns[1])){//checking if it equals the saved one, if not, it goes on to the next matching username
					return true;
				}
			}//once the matching username are finished it returns false, because the password doesn't match any record
		}
		return false;
	}

	public static void signin(PrintWriter pw,BufferedReader br,Connection conn) throws IOException, NoSuchAlgorithmException, SQLException {
		pw.println("Type in a username: ");
		String username=br.readLine();//saves username

		pw.println("Type in your mail");
		String mail=br.readLine();//saves mail

		String salt=new String(crypto.saltGen());//creates random salt

		pw.println("Now type a secure password: ");
		String sPlainPassword=br.readLine();//saves the password
		String sSaltedPassword=sPlainPassword+salt;//new var with the salt added

		pw.println("Type your password again: ");

		byte[] bHashedPassword=crypto.hash(sSaltedPassword.getBytes("UTF-8"));//salted password gets hashed
		String sHashedPassword=encoder.encodeToString(bHashedPassword);//and encoded to string

		if(!br.readLine().equals(sPlainPassword)){//reads the second password and if they aren't the same an error messagge is printed out and he goes back to the main chosing menu
			pw.println("Passwords are not the same! Try again");
			return;
		}else{
			String sqlInsert="INSERT INTO users(username,password,salt,mail) VALUES(?,?,?,?);";//if they are the same the information are inserted in a select
			int j;
			try(PreparedStatement prepared=conn.prepareStatement(sqlInsert)){//using a prepared statement to make it safe from sql injection
				
				prepared.setString(1,username);
				prepared.setString(2,sHashedPassword);
				prepared.setString(3,salt);
				prepared.setString(4,mail);
				j=prepared.executeUpdate();//insert runs

				if(j!=1){//1 is returned if the update went fine
					pw.println("Something went wrong!");
				}else{
					pw.println("You were successfully registered!\nYou may now login");
				}
			}
		}
	}

	public static void users(PrintWriter pw,BufferedReader br,Connection conn) throws SQLException {

		String sqlGetUsernames="SELECT username from users";//select to output the usernames

		try(Statement stmt=conn.createStatement()){

			System.out.println("Running the following query: "+sqlGetUsernames);

			ResultSet rs=stmt.executeQuery(sqlGetUsernames);//running the query
			StringBuilder sb=new StringBuilder();

			String[] rows;
			int rown=1;

			while(rs.next()){
				addRow(sb,rs);//filling the string builder with the values separated by \n for each row (in this case there is only one column so : are not used)
				rown++;//keeping track of the number of rows
			}
			String s=sb.toString();//getting the values in string format
			sb=new StringBuilder();//emptying the string builder (delete exist but the docs was complicated tbh and I don't have much time)

			rows=s.split("\n",rown);//splitting each row in a separated string in a string array
			for(int i=0;i<rows.length-1;i++){
				sb.append((i+1)+" | "+rows[i]+"\n");//string building the output
			}
			s=sb.toString();
			pw.println(s);//sending the username list to the user
		}
	}

	public static void mails(PrintWriter pw,BufferedReader br,Connection conn) throws SQLException {

		String sqlGetUsernames="SELECT mail from users";//select to output the mails

		try(Statement stmt=conn.createStatement()){

			System.out.println("Running the following query: "+sqlGetUsernames);

			ResultSet rs=stmt.executeQuery(sqlGetUsernames);//running the query
			StringBuilder sb=new StringBuilder();

			String[] rows;
			int rown=1;

			while(rs.next()){
				addRow(sb,rs);//filling the string builder with the values separated by \n for each row (in this case there is only one column so : are not used)
				rown++;//keeping track of the number of rows
			}
			String s=sb.toString();//getting the values in string format
			sb=new StringBuilder();//emptying the string builder (delete exist but the docs was complicated tbh)

			rows=s.split("\n",rown);//splitting each row in a separated string in a string array
			for(int i=0;i<rows.length-1;i++){
				sb.append((i+1)+" | "+rows[i]+"\n");//string building the output
			}
			s=sb.toString();
			pw.println(s);//sending the username list to the user
		}
	}

	private static void addRow(StringBuilder sb, ResultSet rs) throws SQLException {//slightly modified teacher's function
        // Get metadata in order to get the column count
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();

		// Append columns
        for (int i = 1; i <= nrOfColumns; ++i) {
			if(i!=1){//if this the for is running for the first time we don't want the separator obv
				sb.append(":");
			}
            sb.append(rs.getString(i));
        }
		sb.append('\n');
    }

}