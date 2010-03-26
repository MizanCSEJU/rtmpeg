package demux;

public class Frame {
	private byte[] frame;
	private long offset;
	private long length;
	private long timeStamp;

	public Frame(byte[] frame, long offset, long length, long timeStamp) {
		this.frame = frame;
		this.offset = offset;
		this.length = length;
		this.timeStamp = timeStamp;
	}

	public byte[] getFrame() {
		return frame;
	}

	public long getOffset() {
		return offset;
	}

	public long getLength() {
		return length;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

}
