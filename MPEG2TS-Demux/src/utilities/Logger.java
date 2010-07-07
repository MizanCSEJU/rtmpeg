package utilities;

/**
 *  
 * 
 *
 */
public class Logger {

	private static String mode = null;

	public static void turnLogModeOn() {
		mode = new String("LOG");
	}

	public static void log(String string) {
		if (mode == null)
			return;
		if (mode.matches("LOG")) {
			System.out.println(string);
		}

	}

}
