package rtmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

public class Server {

	void handShake() throws UnknownHostException, IOException {
		ServerSocket serverSocket = null;
		try {
			serverSocket = new ServerSocket(5080);
		} catch (IOException e) {
			System.err.println("Could not listen on port: 5080.");
			System.exit(1);
		}

		Socket clientSocket = null;
		try {
			System.out.println("Listening...");
			clientSocket = serverSocket.accept();
			System.out.println("Connection made !");
		} catch (IOException e) {
			System.err.println("Accept failed.");
			System.exit(1);
		}
		
		OutputStream out = clientSocket.getOutputStream();
		InputStream in = clientSocket.getInputStream();
		
		int c0 = in.read();
		System.out.println("C0 read");
		int s0 = 3;
		out.write(s0);
		System.out.println("S0 was sent");
		Random randomizer = new Random();

		byte[] c1 = new byte[1536];
		in.read(c1);
		
		System.out.println("C1 read");

		
		byte[] s1 = new byte[1536];
		
		randomizer.nextBytes(s1);
		for (int i = 0; i < 4; i++)
			s1[i] = c1[i];
		for (int i = 4; i < 8; i++)
			s1[i] = 0;

		out.write(s1);
		System.out.println("S1 was sent");
	
		byte[] c2 = new byte[1536];
		in.read(c2);
		System.out.println("C2 read");

		byte[] s2 = new byte[1536];

		for (int i = 0; i < 4; i++)
			s2[i] = c1[i];
		for (int i = 4; i < 8; i++)
			s2[i] = s1[i];
		for (int i = 8; i < s2.length; i++)
			s2[i] = c1[i];

		out.write(s2);

		System.out.println("S2 was sent");
		
		for (int i=0 ;i<100 ; i++){
			System.out.println("No:"+i+" Data: "+in.read());
		}

		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();
	}

	public static void main(String args[]) throws UnknownHostException,
			IOException {
		new Server().handShake();
	}

}
