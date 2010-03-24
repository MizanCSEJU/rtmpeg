package demux;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * A demultiplexer of MPEG2-TS file, the aim of this class
 * is to provide frames from an MPEG2-TS file that is in a H264 format.
 * 
 * @author Elias Khsheibun
 * @author Rashed Rashed
 *
 */
public class Demultiplexer {
	
	private final static int packetSize = 188;
	private final static int pidH264 = 301;
	private static int frameNum = 0;
	private InputStream is;
	

	public Demultiplexer(File file) throws FileNotFoundException {
			is = new FileInputStream(file);
	}
	
	private byte[] getNextTSPacket(int offset,int length) throws IOException{
		
		byte b[] = new byte[length];
		is.read(b, offset, length);
		frameNum++;
		return b;
	}
	
	
	public static void main(String[] args) throws IOException{
		File file = new File("a.mpg");
		Demultiplexer d = new Demultiplexer(file);
		byte b[] = d.getNextTSPacket(frameNum*packetSize, packetSize);
		
	}
	
}
