package demux;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import utilities.TSutils;

/**
 * 
 * A demultiplexer of MPEG2-TS file, the aim of this class is to provide frames
 * from an MPEG2-TS file that is in a H264 format.
 * 
 * @author Elias Khsheibun
 * @author Rashed Rashed
 * 
 */
public class Demultiplexer {

	private final static int packetSize = 188;
	private final static int pidH264 = 68;
	private static int frameNum = 0;
	private InputStream is;

	public Demultiplexer(File file) throws FileNotFoundException {
		is = new FileInputStream(file);
	}

	private byte[] getNextTSPacket() throws IOException {

		byte b[] = new byte[packetSize];
		if (is.read(b, 0, packetSize) == -1) {
			System.err.println("End of stream has been reached");
		}

		frameNum++;
		return b;
	}

	public Frame getNextFrame() {
		return null;
	}

	public static void main(String[] args) throws IOException {
		File file = new File("video.mpg");
		Demultiplexer demux = new Demultiplexer(file);
		byte b[] = null;
		int counter = 0;
		System.out.println(file.length());

		for (int i = 0; i < file.length() / packetSize; i++) {
			b = demux.getNextTSPacket();
			if (TSutils.getPID(b) == pidH264)
				counter++;
		}
		System.out.println("No of frames read: " + frameNum);
		System.out.println("No of frames with PID = " + pidH264 + " is: "
				+ counter);
	}

}
