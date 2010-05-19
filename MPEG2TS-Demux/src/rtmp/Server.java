package rtmp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

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

		byte[] c1 = new byte[in.available()];
		in.read(c1);
		Utils.printStream(c1);
		System.out.println("C1 read: " + c1.length + " bytes.");
		out.write(Utils.readFile("ruby/hs1"));
		Utils.waitForStream(in);
		System.out.println("Handshake sent");

		byte[] c2 = new byte[1536];
		System.out.println("No of bytes in c2:" + in.read(c2));

		System.out.println("C2 read");
		Utils.printStream(c2);

		out.write(Utils.readFile("ruby/server_bw"));
		out.write(Utils.readFile("ruby/client_bw"));
		int i=10;
		while (i-->0){
			System.out.println("\nwaiting ..." + i);
	
			Utils.waitForStream(in);
			byte[] arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
			String msg = new String(arr);

			if (msg.indexOf("connect") > 0) {
				System.out.println("on connect");
				out.write(Utils.readFile("ruby/invoke2"));
	
			}
			else
			if (msg.indexOf("createStream") > 0){
				System.out.println("on createStream");
				out.write(Utils.readFile("ruby/result"));
			}else
			if (msg.indexOf("play") > 0){
				System.out.println("on play");
				out.write(Utils.readFile("ruby/set_chunk"));
				out.write(Utils.readFile("ruby/AAA"));
				out.write(Utils.readFile("ruby/BBB"));
				out.write(Utils.readFile("ruby/play_res"));
			}
			else{
				System.out.println("on ping");

			}
		
		}
	
		
	/*
		String response = new String(arr);
		if (response.indexOf("play") > 0) {
			System.out.println("A\n");
			out.write(Utils.readFile("vlc_invoke3"));
			out.write(Utils.readFile("vlc_ping2"));
			out.write(Utils.readFile("vlc_ping2"));
			out.write(Utils.readFile("vlc_invoke4"));
			out.write(Utils.readFile("vlc_invoke5"));
			out.write(Utils.readFile("vlc_notify"));
		} else {
			System.out.println("\nwaiting 2...");

			Utils.waitForStream(in);

			arr = new byte[in.available()];
			in.read(arr);

			System.out.println("\nClient response to second invoke: ");
			Utils.printStream(arr);
			System.out.println("B\n");
			out.write(Utils.readFile("vlc_invoke3"));
			out.write(Utils.readFile("vlc_ping2"));
			out.write(Utils.readFile("vlc_ping2"));
			out.write(Utils.readFile("vlc_invoke4"));
			out.write(Utils.readFile("vlc_invoke5"));
			out.write(Utils.readFile("vlc_notify"));
		}

		System.out.println("\nwaiting 3...");

		for (int i = 1; i <= 100; i++)
			out.write(Utils.readFile("video+audio/"+i));

		Utils.waitForStream(in);

		arr = new byte[in.available()];
		in.read(arr);

		System.out.println("\nClient response to third invoke: ");
		Utils.printStream(arr);
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
