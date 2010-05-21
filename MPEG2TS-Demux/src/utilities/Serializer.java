package utilities;

import java.io.File;
import java.io.IOException;

import demux.Demultiplexer;
import demux.Frame;

public class Serializer {
	final static int headerSize = 8;
	final static int videoData = 0x09;
	final static int rtmpHeaderSize = 11;
	public static byte[] createAMFVideoData(byte[] payload,int timestamp){
		byte [] message = new byte[payload.length+headerSize];
		byte [] payloadLength = Utils.intToByteArray(payload.length); //TODO if payload size is bigger than 3 bytes
		byte [] timeStamp = Utils.intToByteArray(timestamp);
		message[0] = 0x45;
		message[7] = videoData;
		
		for (int i=1 ; i<4 ; i++)
			message[i]=timeStamp[i];
		
		for (int i=1 ; i<4 ; i++)
			message[i+3]=payloadLength[i];
		
		
		for (int i=headerSize ; i<message.length ; i++)
			message[i] = payload[i-headerSize];

		return message;
	}
	
	public static byte [] rtmpVideoMessage(byte[] payload,int timeStamp){
		byte [] message = new byte[payload.length+rtmpHeaderSize];
		byte [] payloadLength = Utils.intToByteArray(payload.length);
		byte [] timestamp = Utils.intToByteArray(timeStamp);
		message[0] = 0x09;
		for (int i=1 ; i<4 ; i++)
			message[i]=payloadLength[i];
		for (int i=4 ; i<8 ; i++)
			message[i]=timestamp[i-4];
		message[11] = 0x05;
		return message;
	}
	
	public static byte [] rtmpVideMessage2(byte[] payload,int timeStamp){
		byte [] message = new byte[payload.length+rtmpHeaderSize];
		byte [] payloadLength = Utils.intToByteArray(payload.length);
		byte [] timestamp = Utils.intToByteArray(timeStamp);
		message[0] = 0x05;
		for (int i=1 ; i<4 ; i++)
			message[i]=timestamp[i];
		
		for (int i=4 ; i<7 ; i++)
			message[i]=payloadLength[i-3];
		message[7] = 0x09;
		return message;
	}
	
	public static void main(String args[]) throws IOException{
		File file = new File("video.mpg");
		Demultiplexer demux = new Demultiplexer(file);
		for (int i=0 ; i<1 ; i++){
			
			Frame f = demux.getNext();
			byte [] data = createAMFVideoData(f.getFrame(),(int)f.getTimeStamp());
			System.out.println("Frame no: "+i+" size: "+(data.length));
			Utils.printStream(data);
		}
		

	}


}
