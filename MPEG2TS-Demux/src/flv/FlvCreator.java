package flv;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import demux.FLVTag;
import demux.FlvDemux;

/**
 * Given Flv tags, creates an Flv file.
 * 
 */
public class FlvCreator {
	File file = null;
	FileOutputStream os = null;
	int previousTagSize = 0;

	/**
	 * Constructor
	 * 
	 * @param filename
	 *            - in main directory.
	 * @throws IOException
	 */
	public FlvCreator(String filename) throws IOException {
		file = new File(filename);
		if (file.exists())
			file.delete();
		file.createNewFile();
		os = new FileOutputStream(file);
	}

	/**
	 * Writes the FLV header as in the FLV Spec.
	 * 
	 * @param audio
	 *            - should be set true if audio is available.
	 * @param video
	 *            - should be set true if video is available
	 * @throws IOException
	 */
	private void writeHeader(boolean audio, boolean video) throws IOException {
		os.write(0x46); // F
		os.write(0x4c); // L
		os.write(0x56); // V
		os.write(1); // Version
		byte flags = 0x0;
		if (video)
			flags = (byte) (flags | (byte) 0x01);
		if (audio)
			flags = (byte) (flags | (byte) 0x04);
		os.write(flags); // Flags

		byte[] offset = new byte[4];
		offset[3] = 0x09;
		os.write(offset);
	}

	/**
	 * Writes the PrevTagSize as in the FLV standard.
	 * 
	 * @throws IOException
	 */
	private void writePrevTagSize() throws IOException {
		byte[] prevTagSize = utilities.Utils.intToByteArray(previousTagSize);
		os.write(prevTagSize);
	}

	/**
	 * Writes the FLVTag Data.
	 * 
	 * @param tagType
	 *            - 9 for video, 8 for audio.
	 * @param dataSize
	 * @param timeStamp
	 * @param timeStampExtended
	 * @param data
	 * @param metadata
	 * @see FLV Standard.
	 * @throws IOException
	 */
	private void writeFlvTag(byte tagType, int dataSize, int timeStamp,
			byte timeStampExtended, byte[] data, boolean metadata)
			throws IOException {
		os.write(tagType);
		byte[] datasize = utilities.Utils.intToByteArray(dataSize);

		for (int i = 1; i < 4; i++)
			os.write(datasize[i]);

		byte[] timestamp = utilities.Utils.intToByteArray(timeStamp);
		for (int i = 1; i < 4; i++)
			os.write(timestamp[i]);

		os.write(timeStampExtended);
		byte[] streamID = new byte[3];
		os.write(streamID);

		if (metadata)
			writeMetaData();

		os.write(data);

		previousTagSize = dataSize + 1 + 11;
	}

	/**
	 * Writes FrameType + Codec for video tags.
	 * 
	 * @throws IOException
	 */
	private void writeMetaData() throws IOException {
		os.write(20); // FrameType + Codec
		os.write(0); // AVCPacketType
		byte[] compositionTime = new byte[3];
		os.write(compositionTime); // ?!?!?

	}

	public static void main(String args[]) throws IOException {
		FlvCreator creator = new FlvCreator("example.flv");
		creator.writeHeader(false, true);
		creator.writePrevTagSize();

		FlvDemux demux = new FlvDemux("sample.flv");
		FLVTag tag = demux.getNextTag();

		while (tag != null) {

			creator.writeFlvTag((byte) tag.getTagType(), tag.getDataSize(), tag
					.getTimeStamp(), tag.getTimeStampExtended(), tag.getData(),
					false);
			creator.writePrevTagSize();

			tag = demux.getNextVideoTag();

		}

	}
}
