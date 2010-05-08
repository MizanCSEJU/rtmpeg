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
	
}
