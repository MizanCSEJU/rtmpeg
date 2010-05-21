package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Utils {
	
	public static byte[] readFile(String filename) throws IOException{
		File f = new File(filename);
		byte[] content = new byte[(int) f.length()];
		InputStream is = new FileInputStream(f);
		is.read(content);
		
		return content;
	}
	
	public static void printStream(byte [] stream){
		for (int i = 0; i < stream.length; i++) {
			Integer h = ((stream[i] & 0xf0) >> 4);
			Integer l = (stream[i] & 0x0f);
			System.out.print(Integer.toHexString(h) + Integer.toHexString(l));
		}
		System.out.println("\n"+new String(stream));
	}
	
	public static void waitForStream(InputStream is) throws IOException{
		while (is.available() == 0) {}
	}
	
	/**
	 * Returns a byte array containing the two's-complement representation of the integer.<br>
	 * The byte array will be in big-endian byte-order with a fixes length of 4
	 * (the least significant byte is in the 4th element).<br>
	 * <br>
	 * <b>Example:</b><br>
	 * <code>intToByteArray(258)</code> will return { 0, 0, 1, 2 },<br>
	 * <code>BigInteger.valueOf(258).toByteArray()</code> returns { 1, 2 }. 
	 * @param integer The integer to be converted.
	 * @return The byte array of length 4.
	 */
	public static byte[] intToByteArray (final int integer) {
		int byteNum = (40 - Integer.numberOfLeadingZeros (integer < 0 ? ~integer : integer)) / 8;
		byte[] byteArray = new byte[4];
		
		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));
		
		return (byteArray);
	}
	
}
