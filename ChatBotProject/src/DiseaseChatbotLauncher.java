import java.awt.Color;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import javax.swing.ImageIcon;

/**
 * This class launches the disease chatbot and is able to log the sessions and their statistics if that option is selected.
 * @author Christine Steege
 *
 */
public class DiseaseChatbotLauncher {
	/** ChatBotGui to use to open the Disease Chatbot GUI */
	private ChatBotGui diseaseChatGui;
	/** Disease Chatbot */
	private DiseaseChatBot diseaseChatBot;
	
	/**
	 * Constructor
	 */
	public DiseaseChatbotLauncher(boolean logSession) {
		// Create DiseaseChatGui instance to open the GUI
		Image sendImage = new ImageIcon(ChatBotGui.class.getResource("/SendImage.png")).getImage(); 
		Image frameImage = new ImageIcon(ChatBotGui.class.getResource("/VirusIcon.png")).getImage();
		diseaseChatBot = new DiseaseChatBot();
		diseaseChatGui = new ChatBotGui(diseaseChatBot, Color.decode("#02ACC9"), Color.decode("#00859B"), "Disease Chatbot", frameImage, sendImage);
		
		if (logSession) {
			// Get the number of chat session files and check if it has reached 50
			int numOutputFiles = new File("log/chat_sessions/").listFiles().length;
			if (numOutputFiles > 49) {
				System.out.println("Chat session log is full. There are currently 50 stored chat sessions.");
			}
			else {
				// Add a window listener to log the session when the window is closed
				diseaseChatGui.addWindowListener(new WindowAdapter() {
					@Override
					public void windowClosed(WindowEvent e) {
						printChatSession();
					}
				});
			}
		}
		// Open the GUI
		diseaseChatGui.setVisible(true);
	}
	
	/** 
	 * 	Prints the session text and the session statistics to the log folder
	 */ 
	private void printChatSession() {
		// Get a formatted date string to use for the title of the text file
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		String dateString = dateFormat.format(date);
		// Print the session text
		printChatSessionToFile(diseaseChatGui.getChatSessionText(), "log/chat_sessions/"+dateString+".txt");
		// Print the session stats
		printChatSessionStatsToFile(diseaseChatGui.getTotalChatSessionTime(), diseaseChatGui.getTotalUserUtternaces(), diseaseChatGui.getTotalSystemUtternaces(), dateString, "log/chat_statistics.csv");
	}
	
	/**
	 * Prints the chat session text to a the provided text file.
	 * @param sessionText String with session text
	 * @param outputFilePath File path of file to print session text to
	 */
	private void printChatSessionToFile(String sessionText, String outputFilePath) {
		try {
			// Open a new FileWriter with the file name
			FileWriter writer = new FileWriter(outputFilePath);
			// Write the text to the file
			writer.write(sessionText);
			writer.close();
		} 
		catch (Exception e) {
			System.out.println("Error writing to chat session file.");
		}
	}
	
	/**
	 * Prints the chat session text to a the provided text file.
	 * @param totalSessionTime Total session time in milliseconds
	 * @param userUtterances Total user utterances
	 * @param systemUtterances Total system utterances
	 * @param outputFilePath File path of file to print session stats to
	 */
	private void printChatSessionStatsToFile(long totalSessionTime, int userUtterances, int systemUtterances, String date, String outputFilePath) {
		 File file = new File(outputFilePath);
		 int chatNum = 0;
		 // If the file exists, we need to find out the last chat number
		 if (file.exists()) {
			Scanner scanner;
			try {
				// Read the stats file and get the last line
				scanner = new Scanner(file);
				String lastLine = "";
		        while (scanner.hasNextLine()) 
		        	lastLine = scanner.nextLine();
		        String[] line = lastLine.split(",");
				// Get the last chat number and add one
		        chatNum = Integer.parseInt(line[0])+1;
			} catch (FileNotFoundException e) {
				System.out.println("Error reading statistics file.");
			}	 
		 }
		    
		try {
			// Open a new FileWriter with the file name
			FileWriter writer = new FileWriter(outputFilePath, true);
			// Write the text to the file
			long timeInSeconds = Math.round(totalSessionTime / 1000.0);
			writer.write(chatNum+","+userUtterances+","+systemUtterances+","+timeInSeconds+","+date+"\n");
			writer.close();
		} 
		catch (Exception e) {
			System.out.println("Error writing to statistics file.");
		}
	}
	
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(String[] args) {
		new DiseaseChatbotLauncher(true);
	}

}
