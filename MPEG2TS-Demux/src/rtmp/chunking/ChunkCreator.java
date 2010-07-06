package rtmp.chunking;

/**
 * Chuncking creator as in RTMP standard.
 *
 */
public class ChunkCreator {
	/**
	 * Creates a chunk according to the 4 types that are defined in the standard.
	 * @param chunkStreamID
	 * @param fmt
	 * @param timestamp
	 * @param messageLength
	 * @param messageType
	 * @param streamID
	 * @return byte [] representing the created header.
	 * @throws ChunkException
	 * @throws UnsupportedFeature
	 */
	public static byte [] createChunk(int chunkStreamID,int fmt,int timestamp,int messageLength,byte messageType,int streamID) throws ChunkException, UnsupportedFeature{
		
		if (chunkStreamID < 2 || chunkStreamID > 65597 || fmt<0 || fmt>2)
			throw new ChunkException();
		byte [] basicHeader = null;
		if (chunkStreamID < 64) {
			basicHeader = new byte[1];
			basicHeader[0] = (byte) ((byte)chunkStreamID & 0x1F);
		}
		else if (chunkStreamID < 320){
			basicHeader = new byte[2];
			basicHeader[1] = (byte) (chunkStreamID - 64);
		}
		else if (chunkStreamID < 65600){
			int secondByte = (chunkStreamID - 64) % 256;
			int thirdByte = (chunkStreamID - 64)/256;
			basicHeader = new byte[3];
			basicHeader[0] = 1;
			basicHeader[1] = (byte) secondByte;
			basicHeader[2] = (byte) thirdByte;
		}
		byte [] chunkMessageHeader = null;
		
		switch (fmt){
		case 0:
			chunkMessageHeader = new byte[11];
			fillChunkHeader0(chunkMessageHeader,timestamp, messageLength, messageType,streamID);
			break;
		case 1:
			basicHeader[0] =(byte) (basicHeader[0] | 64);
			chunkMessageHeader = new byte[7];
			fillChunkHeader1(chunkMessageHeader);
			break;
		case 2:
			basicHeader[0] =(byte) (basicHeader[0] | 128);
			chunkMessageHeader = new byte[3];
			fillChunkHeader2(chunkMessageHeader);
			break;
		case 3:
			basicHeader[0] =(byte) (basicHeader[0] | (128+64));
			chunkMessageHeader = new byte[0];
			break;
		default:
			break;
		}
		
		byte [] header = new byte[basicHeader.length + chunkMessageHeader.length];
		
		System.arraycopy(basicHeader, 0, header, 0, basicHeader.length);
		System.arraycopy(chunkMessageHeader, 0, header, basicHeader.length, chunkMessageHeader.length);
		
		return header;
	}

	private static void fillChunkHeader2(byte[] chunkMessageHeader) throws UnsupportedFeature {
		// TODO Auto-generated method stub
		throw new UnsupportedFeature();
	}

	private static void fillChunkHeader1(byte[] chunkMessageHeader) throws UnsupportedFeature {
		// TODO Auto-generated method stub
		throw new UnsupportedFeature();
	}

	/**
	 * Fills the chunk header of type 0.
	 * @param chunkMessageHeader
	 * @param timestamp
	 * @param length
	 * @param messageType
	 * @param streamID
	 * @throws UnsupportedFeature
	 */
	private static void fillChunkHeader0(byte[] chunkMessageHeader, int timestamp, int length,byte messageType,int streamID) throws UnsupportedFeature {
		if (timestamp>16777215)
			throw new UnsupportedFeature();
		byte [] timeStamp = utilities.Utils.intToByteArray(timestamp);
		for (int i=1;i<4 ;i++){
			chunkMessageHeader[i-1]=timeStamp[i];
		}
		byte [] lengthArray = utilities.Utils.intToByteArray(length);
		for (int i=1;i<4 ;i++){
			chunkMessageHeader[i+2]=lengthArray[i];
		}
		
		chunkMessageHeader[6] = messageType;
		
		byte [] streamIDArray = new byte[4];
		utilities.Utils.writeInt32Reverse(streamIDArray,streamID);
		
		for (int i=7 ;i<11 ;i++){
			chunkMessageHeader[i] = streamIDArray[i-7];
		}
	}
}
