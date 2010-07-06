package utilities;

import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

/**
 * A utilities class for reading files, printing streams, converting
 * bytes/integers.
 * 
 */
public class Utils {

	/**
	 * Reads a file in the current working directory.
	 * 
	 * @param filename
	 * @return byte code of the file.
	 * @throws IOException
	 */
	public static byte[] readFile(String filename) throws IOException {
		File f = new File(filename);
		byte[] content = new byte[(int) f.length()];
		InputStream is = new FileInputStream(f);
		is.read(content);

		return content;
	}

	/**
	 * Given a stream prints to the standard output the stream in a string
	 * format, also prints its Hex code. This method might be used for debugging
	 * purposes.
	 * 
	 * @param stream
	 */
	public static void printStream(byte[] stream) {
		for (int i = 0; i < stream.length; i++) {
			Integer h = ((stream[i] & 0xf0) >> 4);
			Integer l = (stream[i] & 0x0f);
			System.out.print(Integer.toHexString(h) + Integer.toHexString(l));
		}
		System.out.println("\n" + new String(stream));
	}

	/**
	 * Given an in input stream, the method blocks until some input is
	 * available.
	 * 
	 * @param is
	 * @throws IOException
	 */
	public static void waitForStream(InputStream is) throws IOException {
		while (is.available() == 0) {
		}
	}

	/**
	 * Given an integer, it converts it to a byte[] representation, note that
	 * this method is bounded by an integer that is a maximum size of 4 bytes.
	 * 
	 * @param integer
	 * @return
	 */
	public static byte[] intToByteArray(final int integer) {
		int byteNum = (40 - Integer.numberOfLeadingZeros(integer < 0 ? ~integer
				: integer)) / 8;
		byte[] byteArray = new byte[4];

		for (int n = 0; n < byteNum; n++)
			byteArray[3 - n] = (byte) (integer >>> (n * 8));

		return (byteArray);
	}

	/**
	 * Given an arr of allocated 4 bytes, it copies the reversed representation
	 * of the integer x to the array.
	 * 
	 * @param arr
	 * @param x
	 */
	public static void writeInt32Reverse(byte[] arr, int x) {
		arr[0] = ((byte) (0xFF & x));
		arr[1] = ((byte) (0xFF & (x >> 8)));
		arr[2] = ((byte) (0xFF & (x >> 16)));
		arr[3] = ((byte) (0xFF & (x >> 24)));
	}

	/**
	 * Given an array of 4 bytes it converts it to an integer.
	 * 
	 * @param arr
	 * @return integer representation of the array.
	 */
	public static int byteArrayToInt(byte[] arr) {
		int x = 0;
		for (int i = 0; i < 4; i++) {
			int shift = (4 - 1 - i) * 8;
			x += (arr[i] & 0xFF) << shift;
		}
		return x;
	}

	@Test
	public void test1() {
		byte[] b = new byte[4];
		b[0] = (byte) 0x00;
		b[1] = (byte) 0xFF;
		b[2] = (byte) 0x5C;
		b[3] = (byte) 0xDE;
		if (16735454 != byteArrayToInt(b))
			fail();
	}

}
