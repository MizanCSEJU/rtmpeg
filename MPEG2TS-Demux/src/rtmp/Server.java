package rtmp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Random;

import utilities.Utils;

public class Server {

	void run() throws UnknownHostException, IOException {
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
		System.out.println("C0 = " + c0);
		
		System.out.println("S0 was sent");
		//Random randomizer = new Random();

		byte[] c1 = new byte[1537];
		in.read(c1);

		System.out.println("C1 read");
		out.write(Utils.readFile("handshake.dat"));

		byte[] c2 = new byte[1536];
		System.out.println("No of bytes in c2:"+in.read(c2));

		System.out.println("C2 read");
		
		System.out.println(in.available());

		byte[] arr = new byte[in.available()];

		in.read(arr);
		Utils.printStream(arr);
		
		out.write(Utils.readFile("invoke_1.dat"));
		out.write(Utils.readFile("server_bw_1.dat"));
		out.write(Utils.readFile("ping_1.dat"));
		out.write(Utils.readFile("invoke_2.dat"));
		
		System.out.println("\nwaiting 1...");
		
		Utils.waitForStream(in);
		
		arr = new byte[in.available()];
		in.read(arr);

		
		System.out.println("\nClient response to bwPingInvoke: ");
		Utils.printStream(arr);
		
		out.write(Utils.readFile("invoke_1.dat"));

		out.write(Utils.readFile("server_bw_1.dat"));

		out.write(Utils.readFile("ping_1.dat"));

		out.write(Utils.readFile("invoke_2.dat"));
		System.out.println("\nwaiting 2...");
		
		Utils.waitForStream(in);
		
		arr = new byte[in.available()];
		in.read(arr);

		System.out.println("\nClient response to second invoke: ");
		Utils.printStream(arr);
		
		out.write(Utils.readFile("invoke_3.dat"));
	//	out.write(Utils.readFile("notify_1"));
		//out.write(Utils.readFile("ping_2"));
	//	out.write(Utils.readFile("ping_2"));
	//	out.write(Utils.readFile("invoke_5"));
		//out.write(Utils.readFile("video_1"));
	


		System.out.println("\nwaiting 3...");
		
		Utils.waitForStream(in);
		
		arr = new byte[in.available()];
		in.read(arr);

		System.out.println("\nClient response to third invoke: ");
		Utils.printStream(arr);


		
/*
		System.out.println("\nwaiting 2...");
		while (in.available() == 0) {
		}

		System.out.println("\nAvailable: " + in.available());
		arr = new byte[in.available()];

		in.read(arr);
		System.out.println("\nCreate Stream command: ");
		System.out.println(new String(arr));

		// Should now send Command Message (_result - createStream response)
		// page 25 in RTMP Commands Messages
		//out.write(invokePayload);
		
		f = new File("cs_res.dat");
		byte[] createStreamResponsePayload = new byte[(int) f.length()];
		InputStream csResponsePayload = new FileInputStream(f);
		csResponsePayload.read(createStreamResponsePayload);
		
		out.write(createStreamResponsePayload);
		System.out.println("\nwaiting...");
		while (in.available() == 0) {
		}

		System.out.println("\nAvailable: " + in.available());
		arr = new byte[in.available()];
		in.read(arr);

		System.out.println("\nPlay command: \n"+new String(arr));
		for (int i = 0; i < arr.length; i++) {
			Integer h = ((arr[i] & 0xf0) >> 4);
			Integer l = (arr[i] & 0x0f);
			System.out.print(Integer.toHexString(h) + Integer.toHexString(l));
		}
		
		
		byte [] serverBw = {0x02,0x0,0x0,0x0,0x0,0x0,0x04,0x05,0x0,0x0,0x0,0x0,0x0,0x2,0x0,0x0};
		byte [] ping = {0x42,0x0,0x0,0x0,0x0,0x0,0x06,0x04,0x0,0x0,0x0,0x0,0x0,0x0};
		System.out.println("\nPing: "+new String(ping));
		f = new File("invoke2.dat");
		byte[] invoke = new byte[(int) f.length()];
		InputStream is = new FileInputStream(f);
		
		is.read(invoke);
		out.write(serverBw);
		out.write(ping);
		out.write(invoke);
		f = new File("invoke4.dat");
		invoke = new byte[(int) f.length()];
		is.read(invoke);
		out.write(invoke);
		
		
		System.out.println("\nwaiting...");
		while (in.available() == 0) {
		}
		System.out.println("\nAvailable: " + in.available());
		arr = new byte[in.available()];
		in.read(arr);
		System.out.println("\nClient reply to serverBw: \n"+new String(arr));
		for (int i = 0; i < arr.length; i++) {
			Integer h = ((arr[i] & 0xf0) >> 4);
			Integer l = (arr[i] & 0x0f);
			System.out.print(Integer.toHexString(h) + Integer.toHexString(l));
		}
		
		*/
		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();
	}

	public static void main(String args[]) throws UnknownHostException,
			IOException {
		new Server().run();
	}

}
