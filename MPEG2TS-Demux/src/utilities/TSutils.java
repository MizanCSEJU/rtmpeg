package utilities;

public class TSutils {

	// added from Mpeg2TSParser.cpp -> changed PByte to byte[]
	public static int getPID(byte[] buffer) {
		int pid = ((buffer[1] & 0x1f) << 8) + buffer[2];
		return pid;
	}

	public static boolean isAdaptationFieldExist(byte[] tsPacket) {
		return ((tsPacket[3] & 0x20) != 0);
	}

	public static boolean isPCRExist(byte[] tsPacket) {
		int offset = 5;

		if (isAdaptationFieldExist(tsPacket) && (tsPacket[offset] & 0x10) != 0)
			return true;

		return false;
	}

	/**
	 * 
	 * @param tsPacket
	 * @return adaptation field length, assumes that adaptation field exists
	 */
	public static int adaptationFieldLength(byte[] tsPacket) {
		return (tsPacket[4] & 0xff);
	}

	public static int tsDataOffset(byte[] tsPacket) {
		int offset = 4;

		if (isAdaptationFieldExist(tsPacket))
			offset += 1 + adaptationFieldLength(tsPacket);

		return offset;
	}

	public static boolean isStartOfPES(byte[] tsPacket) {
		if ((tsPacket[1] & 0x40) == 0)
			return false;

		int offset = tsDataOffset(tsPacket);

		return (tsPacket[offset] == 0 && tsPacket[offset + 1] == 0 && tsPacket[offset + 2] == 0x01);
	}

	public static long getPTS(byte[] tsPacket) {

		int offset = tsDataOffset(tsPacket);

		offset += 9; // offset now points to the MSB(byte) of the PTS.

		long pts = (tsPacket[offset] & 0x0e) >> 1;
		pts = (pts << 15) + ((tsPacket[offset + 1] & 0xff) << 7)
				+ ((tsPacket[offset + 2] & 0xfe) >> 1);
		pts = (pts << 15) + ((tsPacket[offset + 3] & 0xff) << 7)
				+ ((tsPacket[offset + 4] & 0xfe) >> 1);

		return pts;
	}

	public static long getDTS(byte[] tsPacket) {

		int offset = tsDataOffset(tsPacket);

		offset += 14; // offset now points to the MSB(byte) of the DTS.

		long dts = (tsPacket[offset] & 0x0e) >> 1;
		dts = (dts << 15) + ((tsPacket[offset + 1] & 0xff) << 7)
				+ ((tsPacket[offset + 2] & 0xfe) >> 1);
		dts = (dts << 15) + ((tsPacket[offset + 3] & 0xff) << 7)
				+ ((tsPacket[offset + 4] & 0xfe) >> 1);

		return dts;
	}

	public static long getPCRbase(byte[] tsPacket) {
		int offset = 6;

		return ((tsPacket[offset] & 0xff) << 25)
				+ ((tsPacket[offset + 1] & 0xff) << 17)
				+ ((tsPacket[offset + 2] & 0xff) << 9)
				+ ((tsPacket[offset + 3] & 0xff) << 1)
				+ ((tsPacket[offset + 4] & 0x80) >> 7);
	}

	public static int getPayloadOffset(byte[] tsPacket) {
		int internalOffset = tsDataOffset(tsPacket);

		if (isStartOfPES(tsPacket)) {
			int PES_header_data_length = tsPacket[internalOffset + 8];
			internalOffset = (internalOffset + 9 + PES_header_data_length);
		}

		return internalOffset;
	}

	public static boolean payloadExists(byte[] tsPacket) {
		return (tsPacket[3] & 0x10) != 0;
	}

}