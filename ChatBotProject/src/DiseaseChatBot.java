import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.time.*;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.text.WordUtils;

public class DiseaseChatBot implements ChatBot {
	// Web scraper to get disease info from CDC and WebMD pages
	private DiseaseWebScraper webScraper;
	// Data processor to process disease info from text files generated by the web scraper
	private DiseaseDataProcessor processor;
	// The current disease being discussed by the chat bot
	private String currentDisease;
	// List of supported diseases
	private List<String> supportedDiseases;
	// String of supported diseases as a formatted list response
	private String diseasesResponse;
	
	// SpellingCorrector to try to correct user entry spelling errors
	private SpellingCorrector spellingCorrector;
	// SessionLogger
	private SessionLogger sessionLogger;
	// String path of log folder
	private String logLocation;

	// Random to use for randomly generating responses
	private Random random = new Random();
	// Local time and date for answer related questions
	private LocalTime time = LocalTime.now();
	private LocalDate date = LocalDate.now();
	// Flag to indicate the user has asked for all information in their previous question
	private boolean getAllInfo = false;

	/**
	 * Constructor
	 */
	public DiseaseChatBot() {
		// Get file path on user's system for the log folder
		logLocation = System.getProperty("user.dir").replace("\\","/")+"/log";
		File dir = new File(logLocation);
		// Make the log folders if they don't already exist
		if (!dir.exists()){
			dir.mkdirs();
			(new File(logLocation+"/chat_sessions")).mkdirs();
		}
		// Create an instance of the DiseaseWebScrapper and parse all diseases
		webScraper = new DiseaseWebScraper();
		// Create instance if the disease data processor
		processor = new DiseaseDataProcessor();
		
		// Try to initialize the SessionLogger
		try {
			sessionLogger = new SessionLogger(logLocation);
		} catch (FileNotFoundException e1) {
			System.out.println("Error loading session stats files.");
		}
		
		// Try to initialize the SpellingCorrector
		try {
			InputStream fileSteam = DiseaseChatBot.class.getResourceAsStream("/keywords.txt");
			spellingCorrector = new SpellingCorrector(fileSteam);
		} catch (IOException e) {
			System.out.println("Spelling Corrector failed to load dictionary. Running Disease Chatbot without spell correction...");
		}
		
		// Add the WebMD and CDC diseases to a HashSet to get a list of only the unique supported diseases
		Set<String> set = new HashSet<String>();
        set.addAll(webScraper.getSupportedCDCDiseases());
        set.addAll(webScraper.getSupportedWebMdDiseases());
        supportedDiseases = new ArrayList<String>(set);
        
        // Create a String response with a formatted list of all the supported diseases
        StringBuilder sb = new StringBuilder();
		for (String disease : supportedDiseases) {
			if (disease.equals("hiv"))
				sb.append("HIV, ");
			else
				sb.append(WordUtils.capitalize(disease) + ", ");
		}
		sb.replace(sb.length() - 2, sb.length(), "");
		diseasesResponse = sb.toString();
	}
	
	public String getLogLocation() {
		return logLocation;
	}
	
	@Override
	public String getResponse(String userEntry) {
		userEntry = userEntry.toLowerCase();
		// If the spelling corrector was able to properly initialize, correct user spelling errors
		if (spellingCorrector != null) 
			userEntry = spellingCorrector.getCorrectedText(userEntry);
		// If the user previously asked to get all info, we need to find out what disease they want to print all info for
		if (getAllInfo) {
			getAllInfo = false;
			// Check if they mentioned a disease in their question
			if (checkForDiseaseName(userEntry)) 
				return processor.handleUserInput(currentDisease, DiseaseDataProcessor.ALL_INFO_REQUEST);
			return "I don't have any information about that disease... Try asking me questions about one of the following diseases: "+diseasesResponse;
		}
		getAllInfo = false;
		// Check if they mentioned a disease name in their question
		boolean mentionedDiseaseName = checkForDiseaseName(userEntry);
		// Check if they want all info
		if (userEntry.contains("tell me everything")) {
			if (currentDisease != null) {
				return processor.handleUserInput(currentDisease, DiseaseDataProcessor.ALL_INFO_REQUEST);
			}
			// They want all info but they didn't provide a disease and there is no current disease. Ask them what disease they want but set getAllInfo to true to remember that they asked that question
			getAllInfo = true;
			return "What disease do you want to learn everything about? I can tell you about the following diseases: "+diseasesResponse;
		}
		if (!mentionedDiseaseName) {
			// Check if they asked for past usage
			String stats = getStatsResponse(userEntry);
			if (stats != null) {
				return stats;
			}
			
			// Check if they asked a small talk question 
			String smallTalk = getSmallTalkResponse(userEntry);
			if (smallTalk != null) {
				return smallTalk;
			}
		}
		if (currentDisease == null) {
			// They didn't mention a disease and there is no current disease. Try to prompt them for a question about the supported diseases
			return "I'm not sure how to answer that... Try asking me questions about one of the following diseases: "+diseasesResponse;
		} 
		return processor.handleUserInput(currentDisease, userEntry);
	}
	
	private boolean checkForDiseaseName(String userEntry) {
		// Check if they mentioned a disease name in their question
		for (String disease : supportedDiseases) {
			if (userEntry.contains(disease)) {
				currentDisease = disease;
				return true;
			}
		}
		return false;
	}
	
	private String getStatsResponse(String userInput) {
		if (Pattern.compile("(.*)(\\b)(stat)|(history)(.*)").matcher(userInput).find() || (userInput.contains("usage") && userInput.contains("summary"))){
			if (sessionLogger != null)
				return sessionLogger.getSummary();
			return "Sorry, I wasn't able to find any information for previous sessions.";
		}
		return null;
	}
	
	/**
	 * Generates a response to a set of known small talk prompts. Returns null if the user input doesn't match a known small talk prompt.
	 * @param userInput String possible small talk prompt
	 * @return String response to user input if it matches a small talk prompt, else return null
	 */
	public String getSmallTalkResponse(String userInput) {
		// Check for some common small talk questions and provide a (possibly randomly generated) pre-coded response 
		if (Pattern.compile("(.*)(\\b)(how(re|'re|\\sare))(.*)(\\b)(you)(\\b)(.*)").matcher(userInput).find() || Pattern.compile("(.*)(\\b)(how(s|'s|\\sis))(.*)(\\b)(going)(\\b)(.*)").matcher(userInput).find()) {
			int num = random.nextInt(3);
			if (num == 0) {
				return "Pretty alright.";
			} else if (num == 1) {
				return "Good, thanks for asking!";
			} else {
				return "Great, thanks for asking!";
			}
		} else if (Pattern.compile("(.*)(\\b)(what(s|('s))?)(.*)(\\b)(up)(\\b)(.*)").matcher(userInput).find()) {
			return "Nothing much!";
		} else if (userInput.contains("you") && (userInput.contains("smart") || userInput.contains("good") || userInput.contains("clever") || userInput.contains("funny"))) {
			return "Thank you!";
		} else if (userInput.contains("welcome")) {
			return "You're so polite! How can I help you?";
		} else if (Pattern.compile("(.*)(\\b)(hi)(\\b)(.*)|(.*)(\\b)(hey)(.*)|(.*)(hello)(.*)").matcher(userInput).find()) {
			int num = random.nextInt(3);
			if (num == 0) {
				return "Hi";
			} else if (num == 1) {
				return "Hello";
			} else {
				return "Hey!";
			}
		} else if (userInput.contains("bye")) {
			return "Goodbye!";
		} else if (userInput.contains("thank")) {
			int num = random.nextInt(3);
			if (num == 0) {
				return "You're welcome!";
			} else if (num == 1) {
				return "My pleasure!";
			} else {
				return "Happy to help!";
			}
		} else if (userInput.contains("what") && userInput.contains("your name") || userInput.contains("who") && userInput.contains("you")) {
			return("I'm Bot.");
		} else if (userInput.contains("time")) {
			time = LocalTime.now();
			String stringTime = "";
			if (time.getHour() > 12) {
				int hour = time.getHour() - 12;
				stringTime = stringTime + hour + ":" + String.format("%02d", time.getMinute()) + " PM";
			}
			else {
				stringTime = stringTime + time.getHour() + ":" + String.format("%02d", time.getMinute()) + " AM";
			}
			return stringTime;
		} else if (Pattern.compile("(.*)(\\b)(date)(\\b)(.*)|(.*)(\\b)(day)(.*)|(.*)(month)(.*)|(.*)(year)(.*)").matcher(userInput).find()) {
			date = LocalDate.now();
			return "Today is " + WordUtils.capitalizeFully(date.getDayOfWeek().name()) + ", " + WordUtils.capitalizeFully(date.getMonth().name()) + " " + date.getDayOfMonth() + ", " + date.getYear();
		} else if (userInput.contains("good morning")) {
			return "Good morning!";
		} else if (userInput.contains("good night")) {
			return "Good night!";
		} else if (userInput.contains("good evening")) {
			return "Good Evening!";
		} else if (userInput.contains("good") && userInput.contains("noon")) {
			return "Good Afternoon!";
		} else if(Pattern.compile("(.*)(\\b)(joke)(.*)").matcher(userInput).find()) {
			int num = random.nextInt(3);
			if (num == 0) {
				return "What's a computer's favorite animal? The RAM";
			} else if (num == 1) {
				return "How do robots eat pizza? One byte at a time";
			} else {
				return "I have a joke about recursion, but I have a joke about recursion, but I have a joke about recursion, but I have a joke about recursion, but I have a joke about recursion, but I have a joke about recursion, but I have a joke about recursion, but I �";
			}
		} else if(userInput.contains("what disease") || userInput.contains("what do you know") || userInput.contains("what can i ask") || userInput.contains("can you help me") || userInput.contains("what can you do") || userInput.contains("what can you tell me")) {
			return "Try asking me a question about: "+diseasesResponse;
		}
		
		return null;
	}
	
}
