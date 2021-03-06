package rtmp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import rtmp.amf.AMF;
import rtmp.chunking.ChunkCreator;
import rtmp.chunking.ChunkException;
import rtmp.chunking.ControlMessages;
import rtmp.chunking.UnsupportedFeature;

import demux.FLVTag;
import demux.FlvDemux;

import utilities.Logger;
import utilities.Utils;

import java.util.Random;

/**
 * RTMP Server side implementation.
 * 
 */
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
	private boolean SENDING_ON = true;
	private String fileName = null;

	public Server() throws InterruptedException, UnknownHostException,
			IOException {
		init();
	}

	/**
	 * Initializes the server (used by the constructor).
	 * 
	 * @throws InterruptedException
	 */
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

	/**
	 * Runs the server.
	 * 
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 * @throws ChunkException
	 * @throws UnsupportedFeature
	 */
	void run() throws UnknownHostException, IOException, InterruptedException,
			ChunkException, UnsupportedFeature {

		initConnection();
		handshake();
		parseUserMessages();

		Logger.log("\n\n>>> SENDING SET CHUNK SIZE >>>");
		byte[] setChunkSize = ControlMessages.setChunkSize(65536, timestamp);
		out.write(setChunkSize);
		Logger.log(">>> END OF SET CHUNK SIZE >>>\n\n\n");

		if (fileName != null)
			sendVideo(fileName);

		out.close();
		in.close();
		clientSocket.close();
		serverSocket.close();
	}

	private void parseUserMessages() throws IOException, ChunkException,
			UnsupportedFeature {

		while (SENDING_ON) {
			Utils.waitForStream(in);
			byte[] arr = new byte[in.available()];
			in.read(arr);
			parse(arr);
		}
	}

	private void parse(byte[] arr) throws IOException, ChunkException,
			UnsupportedFeature {
		int headerNumber = (arr[0] & 0xC0) >> 6;
		int headerSize = getHeaderSize(headerNumber);

		byte[] messageLengthArr = new byte[4];
		for (int i = 1; i < 4; i++)
			messageLengthArr[i] = arr[i + 3];
		int size = utilities.Utils.byteArrayToInt(messageLengthArr);
		byte[] message = new byte[size];

		if (headerSize == 12 && ((arr[0] & 0xF) == 3 || arr[0] == 8)
				&& arr[7] == FUNCTION_CALL) { // AMF message Function Call

			System.arraycopy(arr, headerSize, message, 0, size);
			String messageContent = new String(message);
			if (messageContent.indexOf("connect") != -1)
				onConnect();
			if (messageContent.indexOf("play") != -1)
				onPlay(arr);
			parseRemaineder(arr, message, headerSize);
		}

		else if (headerSize == 12 && arr[7] == ControlMessages.WINDOW_ACK_SIZE) {
			sendStreamBegin();
			parseRemaineder(arr, message, headerSize);
		} else {
			String s = new String(arr);
			if (s.indexOf("play") != -1)
				onPlay(arr);
			SENDING_ON = false;
		}

	}

	private void onPlay(byte[] arr) {

		fileName = AMF.getFileName(arr);
	}

	/**
	 * Parses the remainder of a client message.
	 * 
	 * @param arr
	 * @param message
	 * @param headerSize
	 * @throws IOException
	 * @throws ChunkException
	 * @throws UnsupportedFeature
	 */
	private void parseRemaineder(byte[] arr, byte[] message, int headerSize)
			throws IOException, ChunkException, UnsupportedFeature {
		if (arr.length > message.length + headerSize + 1) {
			byte[] left = new byte[arr.length - message.length - headerSize];
			System.arraycopy(arr, message.length + headerSize, left, 0,
					left.length);
			parse(left);
			return;
		}
	}

	/**
	 * Sends the stream begin command to the client.
	 * 
	 * @throws ChunkException
	 * @throws UnsupportedFeature
	 * @throws IOException
	 */
	private void sendStreamBegin() throws ChunkException, UnsupportedFeature,
			IOException {
		Logger.log(">>> SENDING STREAM BIGIN >>>"); // in the sniffer
		// this message is
		// identified by
		// ping :P
		byte[] ping = ControlMessages.userControlMessage(timestamp, Utils
				.intToByteArray(streamID), ControlMessages.STREAM_BEGIN);
		out.write(ping);
		Logger.log(">>> SENDING STREAM BIGIN - END >>>");

		Logger.log(">>> SENDING _result >>>");
		byte[] result = AMF.netConnectionResult();
		byte[] resultHeader = ChunkCreator.createChunk(3, 0, 0,
				result.length - 1, (byte) 0x14, 0);
		out.write(resultHeader);
		out.write(result);
		Logger.log(">>> END OF SENDING _result >>>\n\n\n");

		Logger.log(">>> SENDING BW_DONE >>>");
		byte[] BWDoneResult = AMF.onBWDone();
		byte[] BWDoneHeader = ChunkCreator.createChunk(3, 0, 0,
				BWDoneResult.length, (byte) 0x14, 0);
		out.write(BWDoneHeader);
		out.write(BWDoneResult);
		Logger.log(">>> END OF SENDING BW_DONE >>>\n\n\n");

		Logger.log("\n\n>>> SENDING RESULT MESSAGE >>>");
		byte[] resultMessage = AMF.resultMessage();
		byte[] resultMessageHeader = ChunkCreator.createChunk(3, 0, 0,
				resultMessage.length, (byte) 0x14, 0);
		out.write(resultMessageHeader);
		out.write(resultMessage);
		Logger.log(">>> END OF RESULT MESSAGE >>>");

	}

	/**
	 * On connect sendings.
	 * 
	 * @throws IOException
	 * @throws ChunkException
	 * @throws UnsupportedFeature
	 */
	private void onConnect() throws IOException, ChunkException,
			UnsupportedFeature {
		Logger.log(">>> Sending Window Ack (SERVER BW) >>>");
		byte[] windowAck = ControlMessages.windowAck(windowSize, timestamp);
		out.write(windowAck);
		Logger.log(">>> Sending Window Ack (SERVER BW) END >>>");
		Logger.log(">>> SENDING SET PEER BW >>>");
		byte[] windowMessage = ControlMessages.peerBW(windowSize, timestamp);
		out.write(windowMessage);
		Logger.log(">>> SENDING SET PEER BW END >>>");
	}

	/**
	 * 
	 * @param headerNumber
	 * @return headerSize
	 */
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

	/**
	 * Sends the video.
	 * 
	 * @param filename
	 * @throws IOException
	 * @throws ChunkException
	 * @throws UnsupportedFeature
	 * @throws InterruptedException
	 */
	private void sendVideo(String filename) throws IOException, ChunkException,
			UnsupportedFeature, InterruptedException {
		FlvDemux dem = new FlvDemux(filename);
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

			Thread.sleep(30);
			// Listening for client (for responses).
			byte[] arr = new byte[in.available()];
			if (arr.length > 0) {
				in.read(arr);
			}

			tag = dem.getNextVideoTag();
		} while (tag != null);

	}

	/**
	 * Initializes the TCP connection.
	 * 
	 * @throws IOException
	 */
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

	/**
	 * Makes the initial handshake. Receives C0,C1,C2 and sends S0,S1,S2
	 * according to the RTMP protocol.
	 * 
	 * @throws IOException
	 */
	private void handshake() throws IOException {
		int c0 = in.read();
		Logger.log("C0 read");
		Logger.log("C0 = " + c0);

		byte[] c1 = new byte[in.available()];
		in.read(c1);
		Logger.log("c1 read: " + c1.length + " bytes.");

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
		Logger.log("No of bytes in c2:" + in.read(c2));
		Logger.log("C2 read");
		Logger.log("Handshake Done!");
	}

	public static void main(String args[]) throws UnknownHostException,
			IOException, InterruptedException, ChunkException,
			UnsupportedFeature {

		if (args.length > 1 || (args.length == 1 && !args[0].matches("-LOG"))) {
				System.out.println("Usage:-");
				System.out
						.println("To run in debug mode: java -jar server.jar -LOG");
				System.out.println("otherwise: java -jar server.jar");
				return;
			}
		if (args.length == 1)
			Logger.turnLogModeOn();

		Server server = new Server();
		server.run();
	}
}
