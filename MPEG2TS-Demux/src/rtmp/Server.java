package rtmp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import rtmp.chunking.ChunkException;
import rtmp.chunking.ControlMessages;
import rtmp.chunking.UnsupportedFeature;

import demux.Demultiplexer;
import demux.FLVTag;
import demux.FlvDemux;
import demux.Frame;

import utilities.Utils;

import java.util.Calendar;
import java.util.Date;
public class Server {
	
	private final int port = 1935;
	private final int socketRetryTime = 1000; // msec
	private ServerSocket serverSocket = null;
	private int timestamp = 0;
	private int streamID = 60;

	public Server() throws InterruptedException, UnknownHostException,
			IOException {
		init();
	}

	private void init() throws InterruptedException {
		try {
			serverSocket = new ServerSocket(port);
		} catch (IOException e) {
			System.err.println("Could not listen on port: " + port
					+ "\nTrying again in: " + (float) socketRetryTime / 1000
					+ " sec.");
			Thread.sleep(socketRetryTime);
			init();
		}
	}

	void run() throws UnknownHostException, IOException, InterruptedException, ChunkException, UnsupportedFeature {
		Socket clientSocket = null;
		try {
			System.out.println("Listening...");
			clientSocket = serverSocket.accept();
			System.out.println("Connection made !");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Accept failed.");
			System.exit(1);
		}
		OutputStream out = clientSocket.getOutputStream();
		InputStream in = clientSocket.getInputStream();

		//int c0 = in.read();
		//System.out.println("C0 read");
		//System.out.println("C0 = " + c0);

	//	byte[] c0c1 = new byte[in.available()];
		//in.read(c0c1);
		//Utils.printStream(c0c1);
		//System.out.println("c0c1 read: " + c0c1.length + " bytes.");
		
		RTMPHandshake hs = new RTMPHandshake();
		byte [] handshake = hs.generateResponse(in);
		
		out.write(handshake);
		//out.write(Utils.readFile("wowoza/hs"));
		System.out.println("Handshake sent"+" size = "+handshake.length);

		Utils.waitForStream(in);
		byte[] c2 = new byte[1536];
		System.out.println("No of bytes in c2:" + in.read(c2));
		System.out.println("C2 read");
		Utils.printStream(c2);
		
/*
		out.write(Utils.readFile("wowoza/server_bw")); //  first stream id
		out.write(Utils.readFile("wowoza/client_bw")); // first stream id
		out.write(Utils.readFile("wowoza/ping1"));
		out.write(Utils.readFile("wowoza/chunk_size"));
*/
	//	int i = 10;
	//	while (i-- > 0) {
		//	System.out.println("\nwaiting ..." + (10 - i));

			Utils.waitForStream(in);
			byte[] arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
			System.out.println(arr.length + " bytes were read");
			String msg = new String(arr);
			
			if (msg.indexOf("connect") > 0) {
				System.out.println("on connect");
				out.write(ControlMessages.windowAck(20000, timestamp++));
				out.write(ControlMessages.peerBW(20000, timestamp++));
				//out.write(Utils.readFile("wowoza/invoke1")); // first stream id
			}
			
			Utils.waitForStream(in);
			arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
			System.out.println(arr.length + " bytes were read");
			msg = new String(arr);
			
			out.write(ControlMessages.userControlMessage(timestamp++, Utils.intToByteArray(streamID), ControlMessages.STREAM_BEGIN));
			out.write(Utils.readFile("bw_ping_invoke.dat"));
			
			System.out.println("\nwaiting ...");
			Utils.waitForStream(in);
			arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
			System.out.println(arr.length + " bytes were read");
			msg = new String(arr);

/*
			if (msg.indexOf("createStream") > 0) {
				System.out.println("on createStream");
				out.write(Utils.readFile("wowoza/result")); // should be new stream id
			}
			if (msg.indexOf("play") > 0) {
				System.out.println("on play");
				out.write(Utils.readFile("wowoza/invoke2"));
				out.write(Utils.readFile("wowoza/ping1"));
				out.write(Utils.readFile("wowoza/ping1"));
				out.write(Utils.readFile("wowoza/invoke3"));
				out.write(Utils.readFile("wowoza/notify"));

				arr = new byte[in.available()];
				in.read(arr);
			Utils.printStream(arr);
*/
			//	break;
			//}

		//}
		File file = new File("video.mpg");
		File flvFile = new File("sample.flv");
		Demultiplexer demux = new Demultiplexer(file);

		FlvDemux dem = new FlvDemux(flvFile);
		int i = 0;
		Frame f = null;
		FLVTag tag= null;
		do {
			tag = dem.getNextTag();
			//f = demux.getNext();
			//if (f == null)
			//	break;
			
			byte[] data = utilities.Serializer.createAMFVideoData(tag.getData(),
					tag.getTimeStamp());
			
			//byte [] data = utilities.Serializer.rtmpVideoMessage(tag.getData(), tag.getTimeStamp());
			
			System.out.println("Sending chunk: " + (i++) + " Size is: "
					+ (data.length)+" Timestamp="+tag.getTimeStamp());
			try {
				out.write(data);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			Thread.sleep(10);
			// Listening for client (for responses).
			arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
		} while (tag != null);

		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();
	}

	public static void main(String args[]) throws UnknownHostException,
			IOException, InterruptedException, ChunkException, UnsupportedFeature {
		Server server = new Server();
		while (true) {
			try {
				server.run();
			} catch (Exception e) {
				e.printStackTrace();
				server.run();
			}
		}
	}

}
