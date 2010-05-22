package rtmp;

import java.util.Random;

public class HandshakeProtocol {
	
	public static int s1s2Size = 3072;
	
	public static byte [] handshake(byte [] c1){
		
		Random randomizer = new Random();
		
		byte [] s1s2 = new byte [s1s2Size];
		
		randomizer.nextBytes(s1s2);
		
		for (int i=0 ; i<4 ; i++){
			s1s2[i]=0;
		} // timeStamp
	
		for (int i=4 ; i<8 ; i++){
			s1s2[i]=(byte) (i-3);
		} // version
		
		int serverOffset = getDHOffset(s1s2);
		//int clinetOffset = getDHOffset()
		//#define GETIBPOINTER(x)     ((uint8_t *)((x)._pBuffer + (x)._consumed))
		
		
		return null;
	}
	
	private static byte getDHOffset(byte [] buffer){
		byte offset = 0;
		
		for (int i=1532 ; i<=1535 ; i++)
			offset+=buffer[i];
		
		offset%=632;
		offset+=772;
		
		if (offset + 128 >= 1536)
			System.err.println("Invalid DH offset");
		
		return offset;
	}
	
}
