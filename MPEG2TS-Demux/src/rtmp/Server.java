package rtmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
		File file = new File("handshake.dat");
		OutputStream out = clientSocket.getOutputStream();
		InputStream in = clientSocket.getInputStream();
		InputStream fileIn = new FileInputStream(file);
		byte[] arrIn = new byte[3073];
	//	arrIn[0]=3;
	//	for (int i=1 ; i< arrIn.length ; i++)
	//		arrIn[i]=0xf;
		fileIn.read(arrIn);
		int c0 = in.read();
		System.out.println("C0 read");
		System.out.println("C0 = "+c0);
		int s0 = 3;
		//out.write(s0);
		System.out.println("S0 was sent");
		Random randomizer = new Random();

		byte[] c1 = new byte[1537];
		in.read(c1);
		
		
		System.out.println("C1 read");
		out.write(arrIn);
		
		//byte[] s1 = new byte[1536];
		
	/*	randomizer.nextBytes(s1);
		for (int i = 0; i < 4; i++)
			s1[i] = c1[i];
		for (int i = 4; i < 8; i++)
			s1[i] = 0;*/

		//out.write(s1);
		//System.out.println("S1 was sent");
	
		byte[] c2 = new byte[1536];
		in.read(c2);
		
		/*for (int i=0 ; i<4 ;i++){
			if (c2[i]!=s1[i])
				System.out.println("failed");
		}
		*/
		System.out.println("C2 read");
/*
		byte[] s2 = new byte[1536];

		for (int i = 0; i < 4; i++)
			s2[i] = c1[i];
		for (int i = 4; i < 8; i++)
			s2[i] = s1[i];
		for (int i = 8; i < s2.length; i++)
			s2[i] = c1[i];*/

		//out.write(s2);
		
//	System.out.println("S2 was sent");
		System.out.println(in.available());
		
			byte [] arr = new byte[in.available()];
			//Byte x = new Byte((byte) in.read());
			
			
			in.read(arr);
			for (int i=0 ; i<arr.length ; i++){
				Integer h = ((arr[i] & 0xf0) >> 4);
				Integer l = (arr[i] & 0x0f);
				System.out.print(Integer.toHexString(h) + Integer.toHexString(l));
			}
				
			//File f = new File("out.txt");
			//OutputStream fileOut = new FileOutputStream(f);
			//fileOut.write(arr);
		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();
	}

	public static void main(String args[]) throws UnknownHostException,
			IOException {
		//Byte x = 0xf;
		//System.out.println("0xf prints as: "+x);
		new Server().handShake();
	}

}
