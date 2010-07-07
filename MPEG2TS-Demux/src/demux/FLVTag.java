package demux;

/**
 * Contains information regarding FLVTag as in the FLV spec.
 * 
 */
public class FLVTag {
	int tagType;

	public FLVTag(int tagType, int dataSize, int timeStamp,
			byte timeStampExtended, int streamID, byte[] data) {
		super();
		this.tagType = tagType;
		this.dataSize = dataSize;
		this.timeStamp = timeStamp;
		this.timeStampExtended = timeStampExtended;
		this.streamID = streamID;
		this.data = data;
	}

	public int getTagType() {
		return tagType;
	}

	public int getDataSize() {
		return dataSize;
	}

	public int getTimeStamp() {
		return timeStamp;
	}

	public byte getTimeStampExtended() {
		return timeStampExtended;
	}

	public int getStreamID() {
		return streamID;
	}

	public byte[] getData() {
		return data;
	}

	int dataSize;
	int timeStamp;
	byte timeStampExtended;
	int streamID;
	byte[] data;
}
