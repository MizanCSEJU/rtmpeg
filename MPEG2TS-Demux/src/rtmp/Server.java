package rtmp;

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

import demux.FLVTag;
import demux.FlvDemux;

import utilities.Utils;

import java.util.Random;

import javax.swing.text.Utilities;

public class Server {

	private static final byte FUNCTION_CALL = 0x14;
	private final int port = 1935;
	private int windowSize = 2500000;
	private final int socketRetryTime = 1000; // msec
	private ServerSocket serverSocket = null;
	private int timestamp = 0;
	private int streamID = 0;
	private int createStreamID = 1;
	private OutputStream out;
	private InputStream in;
	private Socket clientSocket;

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

	void run() throws UnknownHostException, IOException, InterruptedException,
			ChunkException, UnsupportedFeature {

		initConnection();

		handshake();

		System.out
				.println(" =================== HANDSHAKE DONE =======================");

		
		//parseUserMessages();
		
		Utils.waitForStream(in);
		byte[] arr = new byte[in.available()];
		in.read(arr);
		parse(arr);
		Utils.waitForStream(in);
		arr = new byte[in.available()];
		in.read(arr);
		parse(arr);
		Utils.waitForStream(in);
		arr = new byte[in.available()];
		in.read(arr);
		parse(arr);
		


		System.out.println("\n\n>>> SENDING SET CHUNK SIZE >>>");
		byte[] setChunkSize = ControlMessages.setChunkSize(65536, timestamp);
		Utils.printStream(setChunkSize);
		out.write(setChunkSize);
		System.out.println(">>> END OF SET CHUNK SIZE >>>\n\n\n");

		sendVideo("9.flv");

		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();
	}

	private void parseUserMessages() {
		// TODO Auto-generated method stub
		
	}

	private void parse(byte[] arr) throws IOException, ChunkException, UnsupportedFeature {
		int headerNumber = (arr[0] & 0xC0) >> 6;
		int headerSize = getHeaderSize(headerNumber);
		
		byte [] messageLengthArr= new byte[4];
		for (int i=1 ; i<4 ;i++)
			messageLengthArr[i] = arr[i+3];
		int size = utilities.Utils.byteArrayToInt(messageLengthArr);
		byte [] message = new byte[size];
		
		if (headerSize == 12 && ((arr[0] & 0xF) == 3 || arr[0] == 8) && arr[7] == FUNCTION_CALL ) { // AMF message Function Call
			
			System.arraycopy(arr, headerSize, message, 0, size);
			String messageContent = new String(message);
			if (messageContent.indexOf("connect")!=-1)
				onConnect();
			if (messageContent.indexOf("createStream")!=-1)
				onCreateStream();
			if (messageContent.indexOf("play")!=-1)
				onPlay();
			parseRemaineder(arr,message,headerSize);
		}
		
		else if (headerSize == 12 && arr[7] == ControlMessages.WINDOW_ACK_SIZE) {
			sendStreamBegin();
			parseRemaineder(arr,message,headerSize);
		}
		else {
			String s = new String(arr);
			if (s.indexOf("play")!=-1)
				onPlay();
		}

	}
	private void onCreateStream() {
		
	}

	private void onPlay() {
		
	}

	private void parseRemaineder(byte [] arr, byte [] message,int headerSize) throws IOException, ChunkException, UnsupportedFeature{
		if (arr.length > message.length + headerSize+1){
			byte [] left = new byte[arr.length-message.length-headerSize];
			System.arraycopy(arr, message.length + headerSize, left, 0, left.length);
			parse(left);
			return;
		}
	}
	private void sendStreamBegin() throws ChunkException, UnsupportedFeature, IOException {
		System.out.println(">>> SENDING STREAM BIGIN >>>"); // in the sniffer
		// this message is
		// identified by
		// ping :P
		byte[] ping = ControlMessages.userControlMessage(timestamp, Utils
				.intToByteArray(streamID), ControlMessages.STREAM_BEGIN);
		Utils.printStream(ping);
		out.write(ping);
		System.out.println(">>> SENDING STREAM BIGIN - END >>>");

		System.out.println(">>> SENDING _Result >>>");
		byte[] result = Utils.readFile("objects/result_flazer");
		Utils.printStream(result);
		out.write(result);
		System.out.println(">>> END OF SENDING _Result >>>\n\n\n");

		System.out.println(">>> SENDING BW_DONE >>>");
		byte[] bwDone = Utils.readFile("objects/bw_done");
		Utils.printStream(bwDone);
		out.write(bwDone);
		System.out.println(">>> END OF SENDING BW_DONE >>>\n\n\n");
		
		System.out.println("\n\n>>> SENDING RESULT MESSAGE >>>");
		byte[] resultMessage = Utils.readFile("objects/result_flazer2");
		Utils.printStream(resultMessage);
		out.write(resultMessage);
		System.out.println(">>> END OF RESULT MESSAGE >>>");

		
	}

	private void onConnect() throws IOException, ChunkException, UnsupportedFeature {
		System.out.println(">>> Sending Window Ack (SERVER BW) >>>");
		byte[] windowAck = ControlMessages.windowAck(windowSize, timestamp);
		Utils.printStream(windowAck);
		out.write(windowAck);
		System.out.println(">>> Sending Window Ack (SERVER BW) END >>>");
		System.out.println(">>> SENDING SET PEER BW >>>");
		byte[] windowMessage = ControlMessages.peerBW(windowSize, timestamp);
		Utils.printStream(windowMessage);
		out.write(windowMessage);
		System.out.println(">>> SENDING SET PEER BW END >>>");
	}

	private int getHeaderSize(int headerNumber) {
		int headerSize;
		if (headerNumber == 0)
			headerSize = 12;
		else if (headerNumber == 1)
			headerSize = 8;
		else if (headerNumber == 2)
			headerSize = 4;
		else
			headerSize = 1;
		return headerSize;
	}

	private void sendVideo(String string) throws IOException, ChunkException,
			UnsupportedFeature, InterruptedException {
		FlvDemux dem = new FlvDemux("9.flv");
		FLVTag tag = dem.getNextVideoTag();

		do {

			byte[] header = ChunkCreator.createChunk(5, 0, tag.getTimeStamp(),
					tag.getDataSize(), (byte) tag.getTagType(), createStreamID);
			byte[] data = tag.getData();
			try {
				out.write(header);
				out.write(data);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			Thread.sleep(20);
			// Listening for client (for responses).
			byte[] arr = new byte[in.available()];
			if (arr.length > 0) {
				in.read(arr);
				Utils.printStream(arr);
			}

			tag = dem.getNextVideoTag();
		} while (tag != null);

	}

	private void initConnection() throws IOException {
		clientSocket = null;
		try {
			System.out.println("Listening...");
			clientSocket = serverSocket.accept();
			System.out.println("Connection made !");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("Accept failed.");
			System.exit(1);
		}
		out = clientSocket.getOutputStream();
		in = clientSocket.getInputStream();
	}

	private void handshake() throws IOException {
		int c0 = in.read();
		System.out.println("C0 read");
		System.out.println("C0 = " + c0);

		byte[] c1 = new byte[in.available()];
		in.read(c1);
		System.out.println("c1 read: " + c1.length + " bytes.");

		out.write(3);
		byte[] s1 = new byte[1536];
		Random randomizer = new Random();
		randomizer.nextBytes(s1);
		for (int i = 0; i < 8; i++)
			s1[i] = 0;

		out.write(s1);

		byte[] s2 = c1;
		for (int i = 4; i < 8; i++)
			s2[i] = 0;

		out.write(s2);

		Utils.waitForStream(in);
		byte[] c2 = new byte[1536];
		System.out.println("No of bytes in c2:" + in.read(c2));
		System.out.println("C2 read");
	}

	public static void main(String args[]) throws UnknownHostException,
			IOException, InterruptedException, ChunkException,
			UnsupportedFeature {
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
