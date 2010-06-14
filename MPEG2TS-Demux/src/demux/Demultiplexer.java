package demux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import utilities.TSutils;

/**
 * 
 * A de-multiplexer of MPEG2-TS file, the aim of this class is to provide frames
 * from an MPEG2-TS file that is in a H264 format.
 * 
 * @author Elias Khsheibun
 * @author Rashed Rashed
 * 
 */
public class Demultiplexer {
	
	private File file;
	private final static int packetSize = 188;
	private final static int H264PID = 68;
	private final static int ptsTimeResolution = 90000;
	private final static int bufferSize = 100;
	private final static int frameCacheSize = 100;
	private static int packetNum = 0;
	private int packetBufferNum = 0;
	private byte[][] buffer = new byte[bufferSize][packetSize];
	private int bufferPointer;
	private InputStream is;
	private long noOfPacketsInFile;
	private long frameNo;
	private long offset;
	private List<Frame> frameCache;

	/**
	 * Fills a window of TS packets in buffer. each packet is a 188 bytes TS
	 * packet the size of the window that is filled is ptsTimeResolution. The
	 * packets are taken from the input file.
	 * 
	 * @throws IOException
	 */
	private void fillBuffer() throws IOException {
		for (int i = 0; i < buffer.length && packetNum + 1 < noOfPacketsInFile; i++) {
			buffer[i] = getNextTSPacket();
			packetBufferNum++;
		}

		bufferPointer = 0;
	}

	/**
	 * Inits the demux, calculates no. of TS packets in file, also fills the
	 * buffer with TS packets - using fillBuffer. And calculates several frame
	 * packets - a kind of caching, using fillFramesBuffer
	 * 
	 * @see fillFramesCache
	 * @see fillBuffer
	 * @param file
	 * @throws IOException
	 */
	public Demultiplexer(String filename) throws IOException {
		file = new File(filename);
		is = new FileInputStream(file);
		noOfPacketsInFile = (file.length() / packetSize);
		fillBuffer();
		bufferPointer = 0;
		frameNo = 0;
		offset = 0;
		frameCache = new ArrayList<Frame>();
		fillFramesCache();

	}

	/**
	 * A cache of the final frames needed. After the method is called, up to
	 * frameCacheSize frames will be ready in the cache.
	 * 
	 * @see frameCacheSize
	 * @throws IOException
	 */
	private void fillFramesCache() throws IOException {

		for (int i = frameCache.size(); i < frameCacheSize; i++) {
			Frame f = getNextFrame();
			if (f == null)
				break;
			frameCache.add(f);
		}
	}

	/**
	 * Reads a TS packet from the stream, and returns it.
	 * 
	 * @return TS packet (a 188 byte array)
	 * @throws IOException
	 */
	private byte[] getNextTSPacket() throws IOException {

		byte b[] = new byte[packetSize];
		if (is.read(b, 0, packetSize) == -1) {
			System.err.println("End of stream has been reached");
		}

		return b;
	}

	/**
	 * Reads the next frame from the cache, and in case the cache is empty, it
	 * fills the cache and brings the next frame.
	 * 
	 * @return next frame which contains payloads of TS packets until
	 *         (excluding) a packet that contains a PES, also other fields are
	 *         included.
	 * @see Frame
	 * @throws IOException
	 */
	public Frame getNext() throws IOException {
		if (frameCache.size() == 0)
			fillFramesCache();

		if (frameCache.size() == 0)
			return null;

		Frame f = frameCache.get(0);
		frameCache.remove(0);

		return f;
	}

	/**
	 * From a series of TS packets we produce a Frame, linking all bytes of
	 * payloads of the TS frames needed.
	 * 
	 * @return next frame which contains payloads of TS packets until
	 *         (excluding) a packet that contains a PES, also other fields are
	 *         included.
	 * @see Frame
	 * @throws IOException
	 */
	private Frame getNextFrame() throws IOException {

		List<byte[]> arrayList = new ArrayList<byte[]>();

		if (packetNum >= noOfPacketsInFile)
			return null;
		long timestamp = 0;
		// The first loop for finding the start of a PES
		for (; packetNum < noOfPacketsInFile; packetNum++, bufferPointer++) {

			if (bufferPointer == buffer.length)
				fillBuffer();

			if (TSutils.getPID(buffer[bufferPointer]) != H264PID) {
				continue;
			}

			if (TSutils.isStartOfPES(buffer[bufferPointer])) {
				timestamp = TSutils.getDTS(buffer[bufferPointer]);
				byte[] payload = getPayload(buffer[bufferPointer]);
				arrayList.add(payload);
				packetNum++;
				bufferPointer++;
				break;
			}
		}

		// Adding packet payloads until next PES is found
		for (; packetNum < noOfPacketsInFile; packetNum++, bufferPointer++) {
			if (bufferPointer == buffer.length)
				fillBuffer();

			if (TSutils.getPID(buffer[bufferPointer]) != H264PID) {
				continue;
			}

			if (!TSutils.isStartOfPES(buffer[bufferPointer])) {
				byte[] payload = getPayload(buffer[bufferPointer]);
				if (payload != null)
					arrayList.add(payload);
			} else {
				break;
			}
		}

		if (packetNum > noOfPacketsInFile)
			return null;

		int totalSize = 0;
		for (byte[] b : arrayList) {
			totalSize += b.length;
		}

		byte[] frame = new byte[totalSize];
		int j = 0;
		for (byte[] b : arrayList) {
			for (int i = 0; i < b.length; i++)
				frame[j++] = b[i];
		}

		Frame f = new Frame(frame, offset, frame.length,
				(timestamp * (long) (1000))
						/ ((long) (ptsTimeResolution)));
		offset += frame.length;
		frameNo++;
		return f;
	}

	/**
	 * Given a TS packet it returns the payload of that packet.
	 * 
	 * @param tsPacket
	 * @return Payload of the TS packet.
	 */
	private byte[] getPayload(byte[] tsPacket) {

		if (!TSutils.payloadExists(tsPacket)) {
			return null;
		}

		int payloadOffset = TSutils.getPayloadOffset(tsPacket);
		int payloadLength = packetSize - payloadOffset;
		byte[] payload = new byte[payloadLength];
		for (int i = 0; i < payloadLength; i++)
			payload[i] = tsPacket[payloadOffset + i];

		return payload;
	}

	public static void main(String[] args) throws IOException {
		Demultiplexer demux = new Demultiplexer("video.mpg");
		Frame f = demux.getNext();
		int i=0;
		do {
			System.out.println("Frame :"+(i++)+" timestamp: "+f.getTimeStamp());
		}while ((f=demux.getNext())!=null);
		
	}

}
