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
	/** String path of log folder */
	private String logLocation;
	
	/**
	 * Constructor
	 * @param logLocation String file location for log folder
	 * @throws FileNotFoundException 
	 */
	public SessionLogger(String logLocation) throws FileNotFoundException {
		this.logLocation = logLocation;
		// Read the statistics file and store all the lines
		Scanner scanner = new Scanner(new File(logLocation+"/chat_statistics.csv"));
        while (scanner.hasNext())
        	lines.add(scanner.next().split(",")); 
	}
	
	/**
	 * Returns a summary of the chat session stats.
	 * @return Stting summary of stats
	 */
	public String getSummary() {
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
		return "There are "+lines.size()+" chats to date with user prompting "+totalUserPrompts+" times and the system responding "+totalSystemResponses+" times. Total duration is "+totalTime+" seconds.";
	}
	
	/**
	 * Returns a summary of the stats for a provided chat number.
	 * @param chatNum chat number
	 * @return String summary of the stats for a provided chat number
	 */
	public String getChatSummary(int chatNum) {
		// Check that the provided chat number is valid
		int totalChatNum = lines.size();
		if (chatNum > totalChatNum) {
			return "ERROR: There are only "+totalChatNum+" chat sessions.";
		}
		if (chatNum < 1) {
			return "ERROR: Chat number must be positive";
		} 
		
		// Get the line that corresponds to the provided chat number
		String[] line = lines.get(chatNum-1);
		// Print the stats for that line
		return "Chat "+chatNum+" had user prompting "+line[1]+" times and system responding "+line[2]+" times. Total duration was "+line[3]+" seconds.";
	}
	
	/**
	 * Returns the session text for the provided chat number.
	 * @param chatNum chat number
	 * @return String session text for provided chat number
	 */
	public String getChat(int chatNum) {
		// Check that the provided chat number is valid
		int totalChatNum = lines.size();
		if (chatNum > totalChatNum) {
			return "ERROR: There are only "+totalChatNum+" chat sessions.";
		}
		if (chatNum < 1) {
			return "ERROR: Chat number must be positive";
		} 
		
		// Get the line that corresponds to the provided chat number 
		String[] line = lines.get(chatNum-1);
		Scanner scanner;
		try {
			StringBuilder sb = new StringBuilder();
			// Read the corresponding session file for the session with the same date
			scanner = new Scanner(new File(logLocation+"/chat_sessions/"+line[4]+".txt"));
			// Print each line from that text file
	        while (scanner.hasNextLine()) 
	        	sb.append(scanner.nextLine());
	        return sb.toString();
		} 
		catch (FileNotFoundException e) {
			return "ERROR: Could not find chat session file.";
		}
	}
	
	/**
	 * Main method
	 * @param args program arguments
	 */
	public static void main(String[] args) {
		SessionLogger sessionLogger;
		try {
			sessionLogger = new SessionLogger("log");
			// Make sure a command was provided.
			if (args.length == 0) {
				System.out.println("ERROR: No command line argumement provided.");
			} 
			else if (args[0].equals("-summary")) {
				System.out.println(sessionLogger.getSummary());
			} 
			else if (args[0].equals("-showchat-summary")) {
				// Make sure a chat number was provided
				if (args.length > 1) {
					// Make sure chat number is an integer
					try {
						int chatNum = Integer.parseInt(args[1]);
						System.out.println(sessionLogger.getChatSummary(chatNum));
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
						System.out.println(sessionLogger.getChat(chatNum));
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