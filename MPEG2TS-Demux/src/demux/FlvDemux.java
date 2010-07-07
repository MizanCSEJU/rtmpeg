package demux;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import utilities.Logger;

/**
 * FLV Demultiplexer. Receives as an input an FLV file and returns FLV packets.
 * 
 * 
 */
public class FlvDemux {
	private FileInputStream is;
	File file = null;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            - the file name of the FLV (in the main directory).
	 * @throws IOException
	 */
	public FlvDemux(String filename) throws IOException {
		file = new File(filename);
		is = new FileInputStream(file);
		readHeader();
	}

	/**
	 * Reads the FLV header.
	 * 
	 * @throws IOException
	 */
	public void readHeader() throws IOException {
		byte signatureA = (byte) is.read();
		if (signatureA != 0x46)
			Logger.log("error in signature A");

		byte signatureB = (byte) is.read();
		if (signatureB != 0x4c)
			Logger.log("error in signature B");

		byte signatureC = (byte) is.read();
		if (signatureC != 0x56)
			Logger.log("error in signature C");

		byte version = (byte) is.read();
		Logger.log("Version = " + version);

		byte flags = (byte) is.read();
		Logger.log("Flags = " + flags);

		char[] headerLength = new char[4];
		for (int i = 0; i < headerLength.length; i++)
			headerLength[i] = (char) is.read();
		Logger.log("Header Length = " + byteArrayToInt(headerLength, 0));

		// End of header

		// Start of FLV Body:

		byte[] previousTagSize0 = new byte[4];
		is.read(previousTagSize0);

		for (int i = 0; i < previousTagSize0.length; i++)
			if (previousTagSize0[i] != 0)
				Logger.log("Error in tag 0");

	}

	/**
	 * Reads a tag and determines its type.
	 * 
	 * @return FLVTag that was read.
	 * @see FLVTag
	 * @throws IOException
	 */
	private FLVTag processFLVTag() throws IOException {
		int tagType = is.read();
		switch (tagType) {
		case 8:
			Logger.log("audio");
			break;
		case 9:
			Logger.log("video");
			break;
		case 18:
			Logger.log("script data");
			break;
		default:
			Logger.log("reserved " + tagType);
			break;
		}

		char[] dataSize = new char[3];
		for (int i = 0; i < dataSize.length; i++)
			dataSize[i] = (char) is.read();
		char[] tmp3 = new char[4];
		System.arraycopy(dataSize, 0, tmp3, 1, 3);
		int dataSizeInt = byteArrayToInt(tmp3, 0);
		Logger.log("Data size = " + dataSizeInt + " bytes");

		char[] timestamp = new char[3];
		for (int i = 0; i < timestamp.length; i++)
			timestamp[i] = (char) is.read();

		char[] tmp = new char[4];
		System.arraycopy(timestamp, 0, tmp, 1, 3);

		int timeStamp = byteArrayToInt(tmp, 0);
		Logger.log("timestamp = " + timeStamp + " milliseconds");

		byte extendedTimeStamp = (byte) is.read();
		Logger.log("Extended timestamp = " + extendedTimeStamp
				+ " milliseconds");

		char[] streamID = new char[3];
		char[] tmp2 = new char[4];

		System.arraycopy(streamID, 0, tmp2, 1, 3);
		for (int i = 0; i < streamID.length; i++)
			streamID[i] = (char) is.read();
		int id = byteArrayToInt(tmp2, 0);
		Logger.log("Stream ID = " + id);

		byte[] data = new byte[dataSizeInt];
		is.read(data);

		return new FLVTag(tagType, dataSizeInt, timeStamp, extendedTimeStamp,
				id, data);
	}

	/**
	 * Converts byteArray to int.
	 * 
	 * @param arr
	 *            - the array
	 * @param offset
	 *            - the start offset of the conversion
	 * @return
	 */
	public static int byteArrayToInt(char[] arr, int offset) {
		int x = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (3 - i) * 8;
			x += (arr[i + offset] & 0xFF) << shift;
		}
		return x;
	}

	/**
	 * 
	 * @return next flv tag available - when reaching EOF returns null.
	 * @see FLVTag
	 * @throws IOException
	 */
	public FLVTag getNextTag() throws IOException {
		FLVTag tag = processFLVTag();
		if (!readPrevTag())
			return null;
		return tag;
	}

	/**
	 * Reads the previous tag header.
	 * 
	 * @return true if prev tag was available, false otherwise.
	 * @throws IOException
	 */
	private boolean readPrevTag() throws IOException {
		if (is.available() < 1)
			return false;
		char[] prevTagSize = new char[4];
		for (int i = 0; i < prevTagSize.length; i++)
			prevTagSize[i] = (char) is.read();
		Logger.log("prevTagSize = " + byteArrayToInt(prevTagSize, 0));
		return true;
	}

	/**
	 * 
	 * @return next available video tag. (Identified by type 9).
	 * @throws IOException
	 */
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
}
