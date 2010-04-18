package rtmp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class Server {
	
	void handShake() throws UnknownHostException, IOException{
		ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(5080);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 5080.");
            System.exit(1);
        }

        Socket clientSocket = null;
        try {
        	System.out.println("Listening...");
            clientSocket = serverSocket.accept();
            System.out.println("Connection made !");
        } catch (IOException e) {
            System.err.println("Accept failed.");
            System.exit(1);
        }

        PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
        BufferedReader in = new BufferedReader(
				new InputStreamReader(
				clientSocket.getInputStream()));
        String inputLine, outputLine;
        KnockKnockProtocol kkp = new KnockKnockProtocol();

        outputLine = kkp.processInput(null);
        out.println(outputLine);

        while ((inputLine = in.readLine()) != null) {
             outputLine = kkp.processInput(inputLine);
             out.println(outputLine);
             if (outputLine.equals("Bye."))
                break;
        }
        out.close();
        in.close();
        clientSocket.close();
        serverSocket.close();
    }

		
	
	public static void main(String args[]) throws UnknownHostException, IOException{
		new Server().handShake();
	}
	
}
