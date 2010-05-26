package rtmp.chunking;


public class ControlMessages {
	private static final int messageStreamID = 0; // was 0
	private static final int chunkStreamID = 2;
	private static final byte SET_CHUNK_SIZE = 1;
	private static final byte ABORT_MESSAGE = 2;
	private static final byte ACK = 3;
	private static final byte USER_CONTROL_MESSAGE = 4;
	private static final byte WINDOW_ACK_SIZE = 5;
	private static final byte SET_PEER_BW = 6;
	
	public static final int STREAM_BEGIN = 0;
	public static final int STREAM_EOF = 1;
	public static final int STREAM_IS_RECORDED = 4;
	public static final int PING_REQUEST =6;
	


	
	public static byte [] setChunkSize(int chunkSize,int timestamp) throws ChunkException, UnsupportedFeature{
		if (chunkSize < 0 || chunkSize > 65536)
			throw new ChunkException();
		
		byte [] chunk = ChunkCreator.createChunk(chunkStreamID, 0, timestamp, 4, SET_CHUNK_SIZE, messageStreamID);
		byte [] payload = utilities.Utils.intToByteArray(chunkSize);
		byte [] data = new byte[chunk.length + payload.length];
		System.arraycopy(chunk, 0, data, 0, chunk.length);
		System.arraycopy(payload, 0, data, chunk.length, payload.length);

		return data;
	}
	
	public static byte [] ack(int sequenceNumber,int timestamp) throws ChunkException, UnsupportedFeature{
		
		byte [] chunk = ChunkCreator.createChunk(chunkStreamID, 0, timestamp, 4, ACK, messageStreamID);
		byte [] payload = utilities.Utils.intToByteArray(sequenceNumber);
		byte [] data = new byte[chunk.length + payload.length];
		System.arraycopy(chunk, 0, data, 0, chunk.length);
		System.arraycopy(payload, 0, data, chunk.length, payload.length);

		return data;
	}
	
	public static byte [] windowAck(int ackWindow,int timestamp) throws ChunkException, UnsupportedFeature{
		
		byte [] chunk = ChunkCreator.createChunk(chunkStreamID, 0, timestamp, 4, WINDOW_ACK_SIZE, messageStreamID);
		byte [] payload = utilities.Utils.intToByteArray(ackWindow);
		byte [] data = new byte[chunk.length + payload.length];
		System.arraycopy(chunk, 0, data, 0, chunk.length);
		System.arraycopy(payload, 0, data, chunk.length, payload.length);

		return data;
	}
	
	public static byte [] peerBW(int ackWindow,int timestamp) throws ChunkException, UnsupportedFeature{
		
		byte [] chunk = ChunkCreator.createChunk(chunkStreamID, 0, timestamp, 5, SET_PEER_BW, messageStreamID);
		byte [] payload = utilities.Utils.intToByteArray(ackWindow);
		byte [] data = new byte[chunk.length + payload.length+1];
		System.arraycopy(chunk, 0, data, 0, chunk.length);
		System.arraycopy(payload, 0, data, chunk.length, payload.length);
		data[data.length-1] = 2;
		return data;
	}
	
	public static byte [] userControlMessage(int timestamp,byte [] eventData,int event) throws ChunkException, UnsupportedFeature{
		
		byte [] eventArray = utilities.Utils.intToByteArray(event);
		byte [] chunk = ChunkCreator.createChunk(chunkStreamID, 0, timestamp, 2+eventData.length, USER_CONTROL_MESSAGE, messageStreamID);
		byte [] data = new byte[chunk.length + eventData.length +2];
		System.arraycopy(chunk, 0, data, 0, chunk.length);
		System.arraycopy(eventArray, 2, data, chunk.length, 2);
		System.arraycopy(eventData, 0, data, chunk.length+2, eventData.length);
		
		return data;
	}
	
	
}
