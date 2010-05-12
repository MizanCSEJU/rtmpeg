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

		System.out.println("C1 read: " + c1.length + " bytes.");
		out.write(Utils.readFile("vlc_hs2"));
		Utils.waitForStream(in);

		byte[] c2 = new byte[in.available()];
		System.out.println("No of bytes in c2:" + in.read(c2));

		System.out.println("C2 read");
		Utils.printStream(c2);

		out.write(Utils.readFile("vlc_invoke1_done"));
		out.write(Utils.readFile("vlc_serverbw"));
		out.write(Utils.readFile("vlc_ping"));
		out.write(Utils.readFile("vlc_invoke2"));

		System.out.println("\nwaiting 1...");

		Utils.waitForStream(in);

		byte[] arr = new byte[in.available()];
		in.read(arr);

		System.out.println("\nClient response to bwPingInvoke: ");
		Utils.printStream(arr);
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
