package utilities;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AMF {

	private static final byte NUMBER = 0x00;
	private static final byte STRING = 0x02;
	private static final byte OBJECT = 0x03;
	private static final byte NULL = 0x05;
	private static final byte END_OBJECT = 0x09;

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

	public static byte[] netConnectionResult() {

		ByteArrayOutputStream o = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(o);
		addString(out, "_result",true);
		addNumber(out, 1.0);
		addNULL(out);
		newObject(out);
		addString(out, "level",false);
		addString(out, "_status",true);
		addString(out, "code",false);
		addString(out, "NetConnection.Connect.Success",true);
		addString(out, "description",false);
		addString(out, "Connection succeeded.",true);
		addString(out, "fmsVer", false);
		addString(out, "FMS/Ã3,5,1,516", true);
		addString(out, "capabilities", false);
		addNumber(out, 31.0);
		addString(out,"mode",false);
		addNumber(out, 1.0);
		addString(out,"objectEncoding", false);
		addNumber(out, 0);
		endObject(out);
		
		return o.toByteArray();
	}
	
	public static byte[] onBWDone(){
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(o);
		addString(out, "onBWDone", true);
		addNumber(out, 0);
		addNULL(out);
		
		return o.toByteArray();
	}
	
	public static byte[] resultMessage(){
		
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		DataOutputStream out = new DataOutputStream(o);
		addString(out, "_result",true);
		addNumber(out, 2);
		addNULL(out);
		addNumber(out, 1);
		
		return o.toByteArray();
		}

	private static void newObject(DataOutputStream out) {

		try {
			out.write(OBJECT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void addNumber(DataOutputStream out, double d) {

		try {
			out.write(NUMBER);
			out.writeLong(Double.doubleToLongBits(d));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void addString(DataOutputStream out, String str,boolean strTag) {

		try {
			if (strTag==true)
				out.write(STRING);
			byte[] strLength = Utils.intToByteArray(str.length());
			out.write(strLength, 2, 2);
			out.write(str.getBytes());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void addNULL(DataOutputStream out) {
		try {
			out.write(NULL);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	private static void endObject(DataOutputStream out){
		try {
			out.write(0);
			out.write(0);
			out.write(END_OBJECT);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	public static void main(String args[]) {

		netConnectionResult();
	}

}
