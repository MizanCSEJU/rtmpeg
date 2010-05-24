package demux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import utilities.Utils;

public class FlvDemux {
	private FileInputStream is;
	File file = null;
	
	public FlvDemux(String filename) throws IOException {
		file = new File(filename);
		is = new FileInputStream(file);
		readHeader();
	}

	public void readHeader() throws IOException {
		byte signatureA = (byte) is.read();
		if (signatureA != 0x46)
			System.err.println("error in signature A");

		byte signatureB = (byte) is.read();
		if (signatureB != 0x4c)
			System.err.println("error in signature B");

		byte signatureC = (byte) is.read();
		if (signatureC != 0x56)
			System.err.println("error in signature C");

		byte version = (byte) is.read();
		System.out.println("Version = " + version);

		byte flags = (byte) is.read();
		System.out.println("Flags = " + flags);

		char[] headerLength = new char[4];
		for (int i = 0; i < headerLength.length; i++)
			headerLength[i] = (char) is.read();
		System.out
				.println("Header Length = " + byteArrayToInt(headerLength, 0));

		// End of header

		// Start of FLV Body:

		byte[] previousTagSize0 = new byte[4];
		is.read(previousTagSize0);

		for (int i = 0; i < previousTagSize0.length; i++)
			if (previousTagSize0[i] != 0)
				System.err.println("Error in tag 0");

	}

	private FLVTag processFLVTag() throws IOException {
		int tagType = is.read();
		switch (tagType) {
		case 8:
			System.out.println("audio");
			break;
		case 9:
			System.out.println("video");
			break;
		case 18:
			System.out.println("script data");
			break;
		default:
			System.err.println("reserved " + tagType);
			break;
		}

		char[] dataSize = new char[3];
		for (int i = 0; i < dataSize.length; i++)
			dataSize[i] = (char) is.read();
		char[] tmp3 = new char[4];
		System.arraycopy(dataSize, 0, tmp3, 1, 3);
		int dataSizeInt = byteArrayToInt(tmp3, 0);
		System.out.println("Data size = " + dataSizeInt + " bytes");

		char[] timestamp = new char[3];
		for (int i = 0; i < timestamp.length; i++)
			timestamp[i] = (char) is.read();

		char[] tmp = new char[4];
		System.arraycopy(timestamp, 0, tmp, 1, 3);

		int timeStamp = byteArrayToInt(tmp, 0);
		System.out.println("timestamp = " + timeStamp + " milliseconds");

		byte extendedTimeStamp = (byte) is.read();
		System.out.println("Extended timestamp = " + extendedTimeStamp
				+ " milliseconds");

		char[] streamID = new char[3];
		char[] tmp2 = new char[4];

		System.arraycopy(streamID, 0, tmp2, 1, 3);
		for (int i = 0; i < streamID.length; i++)
			streamID[i] = (char) is.read();
		int id = byteArrayToInt(tmp2, 0);
		System.out.println("Stream ID = " + id);

		byte[] data = new byte[dataSizeInt];
		is.read(data);

		return new FLVTag(tagType, dataSizeInt, timeStamp, extendedTimeStamp,
				id, data);
	}

	/**
	 * Convert the byte array to an int starting from the given offset.
	 * 
	 * @param b
	 *            The byte array
	 * @param offset
	 *            The array offset
	 * @return The integer
	 */

	public static int byteArrayToInt(char[] b, int offset) {
		int value = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			value += (b[i + offset] & 0x000000FF) << shift;
		}
		return value;
	}

	public FLVTag getNextTag() throws IOException {
		FLVTag tag = processFLVTag();
		if (!readPrevTag())
			return null;
		return tag;
	}

	private boolean readPrevTag() throws IOException {
		if (is.available() < 1)
			return false;
		char[] prevTagSize = new char[4];
		for (int i = 0; i < prevTagSize.length; i++)
			prevTagSize[i] = (char) is.read();
		System.out.println("prevTagSize = " + byteArrayToInt(prevTagSize, 0));
		return true;
	}

	public FLVTag getNextVideoTag() throws IOException {
		FLVTag tag = processFLVTag();
		if (tag.tagType == 9) {
			if (!readPrevTag())
				return null;
			return tag;
		}

		else {
			if (!readPrevTag())
				return null;
			return getNextVideoTag();
		}

	}

	public static void main(String[] args) throws IOException,
			InterruptedException {
		char[] tmp = new char[4];
		tmp[2] = 0x01;
		tmp[3] = 0x2C;
		System.out.println(byteArrayToInt(tmp, 0));
		FlvDemux demux = new FlvDemux("sample.flv");
		
		int i = 0;
		FLVTag tag = demux.getNextVideoTag();
		Utils.printStream(tag.getData());
		//while (tag!=null){
		// System.out.println("\n\n\n\nTag number "+i)
		//tag = demux.getNextVideoTag();
		//}
		// Thread.sleep(1000);
		// i++;
		// }

		// System.out.println(tag.data[i]);

	}
}
