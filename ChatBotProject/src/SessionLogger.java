import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Reports information and statistics from past sessions.
 * @author Christine Steege
 */
public class SessionLogger {
	/** List of lines from the csv statistics file */
	private ArrayList<String[]> lines = new ArrayList<>();
	
	/**
	 * Constructor
	 * @throws FileNotFoundException 
	 */
	public SessionLogger() throws FileNotFoundException {
		// Read the statistics file and store all the lines
		Scanner scanner = new Scanner(new File("log/chat_statistics.csv"));
        while (scanner.hasNext())
        	lines.add(scanner.next().split(",")); 
	}
	
	/**
	 * Prints a summary of the chat session stats.
	 */
	public void printSummary() {
		int totalUserPrompts = 0;
		int totalSystemResponses = 0;
		int totalTime = 0;
		
		// Add together the total user prompts, system responses, and time from each session
		for (String[] line : lines) {
			totalUserPrompts += Integer.parseInt(line[1]);
			totalSystemResponses += Integer.parseInt(line[2]);
			totalTime += Integer.parseInt(line[3]);
		}
		// Print the results
		System.out.println("There are "+lines.size()+" chats to date with user prompting "+totalUserPrompts+" times and the system responding "+totalSystemResponses+" times. Total duration is "+totalTime+" seconds.");
	}
	
	/**
	 * Prints a summary of the stats for a provided chat number.
	 * @param chatNum chat number
	 */
	public void printChatSummary(int chatNum) {
		// Check that the provided chat number is valid
		int totalChatNum = lines.size();
		if (chatNum > totalChatNum) {
			System.out.println("ERROR: There are only "+totalChatNum+" chat sessions.");
			return;
		}
		if (chatNum < 1) {
			System.out.println("ERROR: Chat number must be positive");
			return;
		} 
		
		// Get the line that corresponds to the provided chat number
		String[] line = lines.get(chatNum-1);
		// Print the stats for that line
		System.out.println("Chat "+chatNum+" had user prompting "+line[1]+" times and system responding "+line[2]+" times. Total duration was "+line[3]+" seconds.");
	}
	
	/**
	 * Prints the session text for the provided chat number.
	 * @param chatNum chat number
	 */
	public void printChat(int chatNum) {
		// Check that the provided chat number is valid
		int totalChatNum = lines.size();
		if (chatNum > totalChatNum) {
			System.out.println("ERROR: There are only "+totalChatNum+" chat sessions.");
			return;
		}
		if (chatNum < 1) {
			System.out.println("ERROR: Chat number must be positive");
			return;
		} 
		
		// Get the line that corresponds to the provided chat number 
		String[] line = lines.get(chatNum-1);
		Scanner scanner;
		try {
			// Read the corresponding session file for the session with the same date
			scanner = new Scanner(new File("log/chat_sessions/"+line[4]+".txt"));
			// Print each line from that text file
	        while (scanner.hasNextLine()) 
	        	System.out.println(scanner.nextLine());
		} 
		catch (FileNotFoundException e) {
			System.out.println("ERROR: Could not find chat session file.");
		}
	}
	
	/**
	 * Main method
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		SessionLogger sessionLogger;
		try {
			sessionLogger = new SessionLogger();
			// Make sure a command was provided.
			if (args.length == 0) {
				System.out.println("ERROR: No command line argumement provided.");
			} 
			else if (args[0].equals("-summary")) {
				sessionLogger.printSummary();
			} 
			else if (args[0].equals("-showchat-summary")) {
				// Make sure a chat number was provided
				if (args.length > 1) {
					// Make sure chat number is an integer
					try {
						int chatNum = Integer.parseInt(args[1]);
						sessionLogger.printChatSummary(chatNum);
					} 
					catch (Exception e) {
						System.out.println("ERROR: Chat number should be an integer.");
					}
				} 
				else {
					System.out.println("ERROR: No chat number provided.");
				}
			} 
			else if (args[0].equals("-showchat")) {
				// Make sure a chat number was provided
				if (args.length > 1) {
					// Make sure chat number is an integer
					try {
						int chatNum = Integer.parseInt(args[1]);
						sessionLogger.printChat(chatNum);
					} 
					catch (Exception e) {
						System.out.println("ERROR: Chat number should be an integer.");
					}
				} 
				else {
					System.out.println("ERROR: No chat number provided.");
				}
			} 
			else {
				System.out.println("ERROR: Command not recognized.");
			}
		} 
		catch (FileNotFoundException e) {
			System.out.println("ERROR: Statistics file not found");
		}
	}
	
}