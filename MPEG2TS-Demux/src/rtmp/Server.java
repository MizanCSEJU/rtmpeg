package rtmp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import demux.Demultiplexer;
import demux.Frame;

import utilities.Utils;

public class Server {
	
	private final int port = 1935;
	private final int socketRetryTime = 1000; // msec
	private ServerSocket serverSocket = null;
	
	public Server() throws InterruptedException, UnknownHostException, IOException {
		init();
	}
	
	private void init() throws InterruptedException{
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: "+port+"\nTrying again in: "+(float)socketRetryTime/1000+" sec.");
			Thread.sleep(socketRetryTime);
			init();
		}
	}
	
	void run() throws UnknownHostException, IOException, InterruptedException {
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

		byte[] c1 = new byte[in.available()];
		in.read(c1);
		Utils.printStream(c1);
		System.out.println("C1 read: " + c1.length + " bytes.");
		out.write(Utils.readFile("wowoza/hs"));
		System.out.println("Handshake sent");
		
		Utils.waitForStream(in);
		byte[] c2 = new byte[1536];
		System.out.println("No of bytes in c2:" + in.read(c2));
		System.out.println("C2 read");
		Utils.printStream(c2);

		out.write(Utils.readFile("wowoza/server_bw"));
		out.write(Utils.readFile("wowoza/client_bw"));
		out.write(Utils.readFile("wowoza/ping1"));
		out.write(Utils.readFile("wowoza/chunk_size"));
		
		
		int i=10;
		while (i-->0){
			System.out.println("\nwaiting ..." + (10-i));
	
			Utils.waitForStream(in);
			byte[] arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
			String msg = new String(arr);

			if (msg.indexOf("connect") > 0) {
				System.out.println("on connect");
				out.write(Utils.readFile("wowoza/invoke1"));
	
			}
			
			if (msg.indexOf("createStream") > 0){
				System.out.println("on createStream");
				out.write(Utils.readFile("wowoza/result"));
			}
			if (msg.indexOf("play") > 0){
				System.out.println("on play");
				out.write(Utils.readFile("wowoza/invoke2"));
				out.write(Utils.readFile("wowoza/ping1"));
				out.write(Utils.readFile("wowoza/ping1"));
				out.write(Utils.readFile("wowoza/invoke3"));
				out.write(Utils.readFile("wowoza/notify"));
				
				arr = new byte[in.available()];
				in.read(arr);
				Utils.printStream(arr);
				
				break;
			}
		
		}
		File file = new File("video.mpg");
		Demultiplexer demux = new Demultiplexer(file);
		
		i=0;
		Frame f = null;
		
		do {
			f = demux.getNext();
			if (f==null)
				break;
			byte [] data = utilities.Serializer.createAMFVideoData(f.getFrame(),(int)f.getTimeStamp());
			System.out.println("Sending chunk: "+(i++)+" Size is: "+(data.length));
			try {
			out.write(data);
			}
			catch (Exception e){
				return;
			}
			Thread.sleep(1);			
			// Listening for client (for responses).
			byte[] arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
		} while (f  != null);
		
		
		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();
	}

	public static void main(String args[]) throws UnknownHostException,
			IOException, InterruptedException {
		Server server = new Server();
		while (true){
			try{
		server.run();
			}
			catch (Exception e){
				server.run();
			}
		}
	}

}
