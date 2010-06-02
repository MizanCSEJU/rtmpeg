package rtmp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import rtmp.chunking.ChunkCreator;
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
import java.util.Random;
public class Server {
	
	private final int port = 1935;
	private final int socketRetryTime = 1000; // msec
	private ServerSocket serverSocket = null;
	private int timestamp = 0;
	private int streamID = 0;
	private int createStreamID = 1;
	private int recordedStream = 101;

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

		int c0 = in.read();
		System.out.println("C0 read");
		System.out.println("C0 = " + c0);

		byte[] c1 = new byte[in.available()];
		in.read(c1);
		//Utils.printStream(c1);
		System.out.println("c1 read: " + c1.length + " bytes.");
		
		//RTMPHandshake hs = new RTMPHandshake();
		out.write(3);
		byte [] s1 = new byte[1536];//hs.generateResponse(in);
		Random randomizer = new Random();
		randomizer.nextBytes(s1);
		for (int i=0 ; i<8 ; i++)
			s1[i] = 0;
		
		out.write(s1);
		
		byte [] s2 = c1;
		for (int i=4 ; i<8 ; i++)
			s2[i] = 0;
		
		out.write(s2);
		//out.write(Utils.readFile("wowoza/hs"));
		//System.out.println("Handshake sent"+" size = "+handshake.length);

		Utils.waitForStream(in);
		byte[] c2 = new byte[1536];
		System.out.println("No of bytes in c2:" + in.read(c2));
		System.out.println("C2 read");
		//Utils.printStream(c2);

		
		System.out.println(" =================== HANDSHAKE DONE =======================");
		

		Utils.waitForStream(in);
		byte[] arr = new byte[in.available()];
		in.read(arr);
		System.out.println("<<< CONNECT MESSAGE START <<<");
		Utils.printStream(arr);
		System.out.println(arr.length + " bytes were read");
		String msg = new String(arr);
		System.out.println("<<< CONNECT MESSAGE END <<<");

		System.out.println(">>> Sending Window Ack (SERVER BW) >>>");
		byte [] windowAck = ControlMessages.windowAck(2500000, timestamp);
		Utils.printStream(windowAck);
		out.write(windowAck);
		System.out.println(">>> Sending Window Ack (SERVER BW) END >>>");
		System.out.println(">>> SENDING SET PEER BW >>>");
		byte [] windowMessage = ControlMessages.peerBW(2500000, timestamp);
		Utils.printStream(windowMessage);
		out.write(windowMessage);
		System.out.println(">>> SENDING SET PEER BW END >>>");

		
			System.out.println("<<< WINDOW ACK <<<");
			Utils.waitForStream(in);
			arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
			System.out.println(arr.length + " bytes were read");
			msg = new String(arr);
			System.out.println("<<< WINDOW ACK <<<\n\n");
			
			
		
			System.out.println(">>> SENDING STREAM BIGIN >>>"); // in the sniffer this message is identified by ping :P
			byte [] ping = ControlMessages.userControlMessage(timestamp, Utils.intToByteArray(streamID) , ControlMessages.STREAM_BEGIN);
			Utils.printStream(ping);
			out.write(ping);
			System.out.println(">>> SENDING STREAM BIGIN - END >>>");
			
			/*
			System.out.println("\n\n>>> SENDING SET CHUNK SIZE >>>");
			byte [] setChunkSize = ControlMessages.setChunkSize(65536, timestamp);
			Utils.printStream(setChunkSize);
			out.write(setChunkSize);
			System.out.println(">>> END OF SET CHUNK SIZE >>>\n\n\n");
			*/
			
			System.out.println(">>> SENDING _Result >>>");
			byte [] result = Utils.readFile("objects/result_flazer");
			Utils.printStream(result);
			out.write(result);
			System.out.println(">>> END OF SENDING _Result >>>\n\n\n");
			
			
			System.out.println(">>> SENDING BW_DONE >>>");
			byte [] bwDone = Utils.readFile("objects/bw_done");
			Utils.printStream(bwDone);
			out.write(bwDone);
			System.out.println(">>> END OF SENDING BW_DONE >>>\n\n\n");

			
		
			
			System.out.println("\n<<< createStream start <<<");
			Utils.waitForStream(in);
			arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
			System.out.println(arr.length + " bytes were read");
			msg = new String(arr);
			
			System.out.println("<<< createStream End <<<");
			
			
			System.out.println("\n\n>>> SENDING RESULT MESSAGE >>>");
			byte [] resultMessage = Utils.readFile("objects/result_flazer2");
			Utils.printStream(resultMessage);
			out.write(resultMessage);
			System.out.println(">>> END OF RESULT MESSAGE >>>");

			
			
			System.out.println("\n\n<<< play message <<<");
			Utils.waitForStream(in);
			arr = new byte[in.available()];
			in.read(arr);
			Utils.printStream(arr);
			System.out.println(arr.length + " bytes were read");
			msg = new String(arr);
			System.out.println("\n\n<<< play message End <<<");
			
			System.out.println("\n\n>>> SENDING SET CHUNK SIZE >>>");
			byte [] setChunkSize = ControlMessages.setChunkSize(65536, timestamp);
			Utils.printStream(setChunkSize);
			out.write(setChunkSize);
			System.out.println(">>> END OF SET CHUNK SIZE >>>\n\n\n");

			
			System.out.println("\n\n>>> SENDING stream is recorded >>>");
			byte [] streamRecorded = ControlMessages.userControlMessage(timestamp, Utils.intToByteArray(createStreamID), ControlMessages.STREAM_IS_RECORDED);
			Utils.printStream(streamRecorded);
			out.write(streamRecorded);
			System.out.println(">>> END SENDING stream is recorded >>>");
			
			System.out.println("\n\n>>> SENDING stream begin >>>");
			byte [] streamBegin = ControlMessages.userControlMessage(timestamp, Utils.intToByteArray(createStreamID), ControlMessages.STREAM_BEGIN);
			Utils.printStream(streamBegin);
			out.write(streamBegin);
			System.out.println(">>> END SENDING stream is begin >>>");
			
			
			System.out.println("\n\n>>> SENDING onStatus1 >>>");
			byte [] onStatus = Utils.readFile("objects/onstatus1");
			Utils.printStream(onStatus);
			out.write(onStatus);
			System.out.println(">>> END OF onStatus1 >>>");
			
			
			
			System.out.println("\n\n>>> SENDING onStatus2 >>>");
			byte [] onStatus2 = Utils.readFile("objects/onstatus2");
			Utils.printStream(onStatus2);
			out.write(onStatus2);
			System.out.println(">>> END OF onStatus2 >>>");
			/*
			System.out.println("\n\n>>> SENDING RTMP SAMPLE ACCESS >>>");
			byte [] sampleAccess = Utils.readFile("wowoza/notify");
			Utils.printStream(sampleAccess);
			out.write(sampleAccess);
			System.out.println(">>> END RTMP SAMPLE ACCESS >>>");
			
			System.out.println("\n\n>>> SENDING OBJECT 8 >>>");
			byte [] object8 = Utils.readFile("objects/objec8");
			Utils.printStream(object8);
			out.write(object8);
			System.out.println(">>> END OBJECT 8 >>>");
			
			
			System.out.println("\n\n>>> Sending onStatus 3 >>>");
			byte [] onStatus3 = Utils.readFile("objects/onstatus3");
			Utils.printStream(onStatus3);
			out.write(onStatus3);
			System.out.println(">>> END of onStatus 3 >>>");
			
			
			System.out.println("\n\n>>> Sending onMetaData >>>");
			byte [] onMetaData = Utils.readFile("objects/onmetadata");
			Utils.printStream(onMetaData);
			out.write(onMetaData);
			System.out.println(">>> END of onMetaData >>>");
			
			System.out.println("\n\n>>> Sending xml >>>");
			byte [] xml = Utils.readFile("objects/xml");
			Utils.printStream(xml);
			out.write(xml);
			System.out.println(">>> END of xml >>>");
			
			System.out.println("\n\n>>> Sending 1 >>>");
			byte [] byte1 = Utils.readFile("objects/1");
			Utils.printStream(byte1);
			out.write(byte1);
			System.out.println(">>> END of 1 >>>");
			
			System.out.println("\n\n>>> Sending unkown >>>");
			byte [] un = Utils.readFile("objects/unkown");
			Utils.printStream(un);
			out.write(un);
			System.out.println(">>> END of unkown >>>");
			out.write(Utils.readFile("objects/2"));
			out.write(Utils.readFile("objects/3"));
			out.write(Utils.readFile("objects/4"));
			out.write(Utils.readFile("objects/5"));
			out.write(Utils.readFile("objects/6"));
			out.write(Utils.readFile("objects/7"));
				arr = new byte[in.available()];
				in.read(arr);
			Utils.printStream(arr);
	*/
			//	break;
			//}

		//}

		FlvDemux dem = new FlvDemux("9.flv");
		File f = new File("video.mpg");
		Demultiplexer d= new Demultiplexer(f);
		int i = 0;
		Frame ff = d.getNext();
		FLVTag tag= dem.getNextVideoTag();
		
	//	Utils.printStream(tag.getData());
		
		do {
			//f = demux.getNext();
			//if (f == null)
			//	break;
			
			//byte[] data = utilities.Serializer.createAMFVideoData(ff.getFrame(),
			//	0);
			
			
			//System.out.println("Sending chunk: " + (i++) + " Size is: "
			//		+ (data.length)+" Timestamp="+tag.getTimeStamp());
			//byte [] chunk = utilities.Serializer.rtmpVideoMessage(tag.getData(), tag.getTimeStamp(), createStreamID);
			byte [] header = ChunkCreator.createChunk(5, 0, timestamp, tag.getDataSize(), (byte)9, createStreamID);
			byte [] data = tag.getData();
			try {
				out.write(header);
				out.write(data);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
			Thread.sleep(10);
			// Listening for client (for responses).
			arr = new byte[in.available()];
			if (arr.length > 0){
				in.read(arr);
				Utils.printStream(arr);
				Thread.sleep(5000);
			}
			
			ff = d.getNext();
			tag= dem.getNextTag();
		} while (tag != null || ff !=null);

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
