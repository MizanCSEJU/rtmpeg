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
	private InputStream is;
	

	public Demultiplexer(File file) throws FileNotFoundException {
			is = new FileInputStream(file);
	}
	
	private byte[] readBytes(int offset,int length) throws IOException{
		byte b[] = new byte[length];
		is.read(b, offset, length);
		return b;
	}
	
	public byte[] getNextFrame() {
		return null;
	}
	
	public static void main(String[] args) throws IOException{
		File file = new File("c:\\video.mpg");
		Demultiplexer d = new Demultiplexer(file);
		byte b[] = d.readBytes(0, packetSize);
	}
	
}
