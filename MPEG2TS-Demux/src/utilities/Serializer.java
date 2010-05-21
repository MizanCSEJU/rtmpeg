package utilities;

import java.io.File;
import java.io.IOException;

import demux.Demultiplexer;
import demux.Frame;

public class Serializer {
	final static int headerSize = 8;
	final static int videoData = 0x09;
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
