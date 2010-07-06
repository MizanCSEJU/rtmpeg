package utilities;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * 
 * An AMF serializer
 * 
 */

public class AMF {

	private static final byte NUMBER = 0x00;
	private static final byte STRING = 0x02;
	private static final byte OBJECT = 0x03;
	private static final byte NULL = 0x05;
	private static final byte END_OBJECT = 0x09;

	
	/**
	 * Retrieves the filename in the play message which encoded in AMF
	 * 
	 * @param msg - the play message sent from the client
	 * @return the file name
	 */
	public static String getFileName(byte[] msg) {

		int index, nameLength;
		String str = new String(msg);
		index = str.indexOf("play");
		for (int i = index; i < msg.length; i++) {
			if (msg[i] == STRING) {
				nameLength = msg[i + 2];
				if (nameLength == 0)
					return null;
				i += 3;
				byte[] fileName = new byte[nameLength];
				System.arraycopy(msg, i, fileName, 0, nameLength);
				String name = new String(fileName);
				return name + ".flv";
			}
		}
		return null;
	}

	/**
	 * Creates the respond for the NetConnection message
	 * 
	 * @return the respond to be sent in array of bytes
	 */
	public static byte[] netConnectionResult() {

		ByteArrayOutputStream o = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(o);
		addString(out, "_result", true);
		addNumber(out, 1.0);
		addNULL(out);
		newObject(out);
		addString(out, "level", false);
		addString(out, "_status", true);
		addString(out, "code", false);
		addString(out, "NetConnection.Connect.Success", true);
		addString(out, "description", false);
		addString(out, "Connection succeeded.", true);
		addString(out, "fmsVer", false);
		addString(out, "FMS/Ã3,5,1,516", true);
		addString(out, "capabilities", false);
		addNumber(out, 31.0);
		addString(out, "mode", false);
		addNumber(out, 1.0);
		addString(out, "objectEncoding", false);
		addNumber(out, 0);
		endObject(out);

		return o.toByteArray();
	}

	/**
	 * Creates the respond for the onBWDone message
	 * 
	 * @return the respond to be sent in array of bytes
	 */
	public static byte[] onBWDone() {
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(o);
		addString(out, "onBWDone", true);
		addNumber(out, 0);
		addNULL(out);

		return o.toByteArray();
	}

	/**
	 * Creates the result message
	 * 
	 * @return the respond to be sent in array of bytes
	 */
	public static byte[] resultMessage() {

		ByteArrayOutputStream o = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(o);
		addString(out, "_result", true);
		addNumber(out, 2);
		addNULL(out);
		addNumber(out, 1);

		return o.toByteArray();
	}

	/**
	 * Adds an object-marker (in AMF) to the output stream
	 * 
	 * @param out
	 * 
	 */
	private static void newObject(DataOutputStream out) {

		try {
			out.write(OBJECT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Adds a number-marker(in AMF) and a number to the output stream
	 * 
	 * @param out
	 * @param d - the number to be coded in AMF
	 */
	private static void addNumber(DataOutputStream out, double d) {

		try {
			out.write(NUMBER);
			out.writeLong(Double.doubleToLongBits(d));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Adds a string-marker(optionally) and a string to the output stream
	 * 
	 * @param out
	 * @param str - the string to be coded in AMF
	 * @param strTag - true to add the string-marker before the string, false otherwise
	 */
	private static void addString(DataOutputStream out, String str,
			boolean strTag) {

		try {
			if (strTag == true)
				out.write(STRING);
			byte[] strLength = Utils.intToByteArray(str.length());
			out.write(strLength, 2, 2);
			out.write(str.getBytes());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Adds a NULL-marker to the output stream
	 * 
	 * @param out
	 * 
	 */
	private static void addNULL(DataOutputStream out) {
		try {
			out.write(NULL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * Adds an object-end-marker to the output stream
	 * 
	 * @param out
	 * 
	 */
	private static void endObject(DataOutputStream out) {
		try {
			out.write(0);
			out.write(0);
			out.write(END_OBJECT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
