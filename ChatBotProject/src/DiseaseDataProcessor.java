import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import org.apache.commons.text.WordUtils;

/**
 * This class will processes information for a provided disease and attempts to identify and answer prompts based on the stored information for that disease.
 * @author Christine Steege
 */
public class DiseaseDataProcessor {	
	// Delimeter written before section titles in the disease text files
	private static final String SECTION_DELIMETER = "<SECT>";
	// String to represent the user wants to know all info
	public static final String ALL_INFO_REQUEST = "tell me everything";

	// Regex patterns to match the common possible information sections
	public static final Pattern WHAT_IS = Pattern.compile("(.*)(what is)|(what(s|'s))|(tell me about)(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern RISKS_CAUSES = Pattern.compile("(.*)(\\b)(risk)(.*)|(.*)(\\b)(cause)(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern SYMPTOMS = Pattern.compile("(.*)(symptom)(.*)|(.*)(\\b)(sign(s?))(\\b)(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern TREATMENT = Pattern.compile("(.*)(treat(ment|ed)?)(.*)|(.*)(remed(y|(ies)))(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern OUTLOOK = Pattern.compile("(.*)(outlook)(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern COMPLICATIONS = Pattern.compile("(.*)(complication)(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern PREVENTION = Pattern.compile("(.*)(prevent)(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern VACCINE = Pattern.compile("(.*)(vaccin(e|ation))(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern DIAGNOSIS = Pattern.compile("(.*)(diagnos)(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern TRANSMISSION = Pattern.compile("(.*)(transmi)(.*)|(.*)(spread)(.*)", Pattern.CASE_INSENSITIVE);
	public static final Pattern TYPES = Pattern.compile("(.*)(\\b)(type)(.*)|(.*)(\\b)(strain)(.*)", Pattern.CASE_INSENSITIVE);

	// List to store regex patterns
	private List<Pattern> regexPatterns = new ArrayList<>();
	// Map of regex patterns to a String prompt
	private HashMap<Pattern, String> patternToPrompt = new HashMap<>();
	// List of all the unique info sections that have been found for a disease
	private List<String> foundInfoSections = new ArrayList<>();
	
	// Maps of regex patterns to corresponding section information for CDC and WebMD
	private HashMap<Pattern, String> CDCSectionInfo = new HashMap<>();
	private HashMap<Pattern, String> WebMDSectionInfo = new HashMap<>();
	
	/**
	 * Constructor
	 */
	public DiseaseDataProcessor() {
		// Add all of the regex patterns to the list
		regexPatterns.add(WHAT_IS);
		regexPatterns.add(RISKS_CAUSES);
		regexPatterns.add(SYMPTOMS);
		regexPatterns.add(TREATMENT);
		regexPatterns.add(OUTLOOK);
		regexPatterns.add(COMPLICATIONS);
		regexPatterns.add(PREVENTION);
		regexPatterns.add(VACCINE);
		regexPatterns.add(DIAGNOSIS);
		regexPatterns.add(TRANSMISSION);
		regexPatterns.add(TYPES);
		
		// Add the corresponding String prompt to all the regex patterns
		patternToPrompt.put(WHAT_IS, "what it is");
		patternToPrompt.put(RISKS_CAUSES, "risk factors and causes");
		patternToPrompt.put(SYMPTOMS, "symptoms");
		patternToPrompt.put(TREATMENT, "treatment");
		patternToPrompt.put(OUTLOOK, "outlook");
		patternToPrompt.put(COMPLICATIONS, "complications");
		patternToPrompt.put(PREVENTION, "prevention");
		patternToPrompt.put(VACCINE, "vaccine");
		patternToPrompt.put(DIAGNOSIS, "diagnosis");
		patternToPrompt.put(TRANSMISSION, "transmission");
		patternToPrompt.put(TYPES, "types");
	}
	
	/**
	 * Processes the information for the provided disease from CDC and WebMD and stores it in CDCSectionInfo and/or WebMDSectionInfo
	 * @param disease String name of disease
	 * @return true if information was found for the disease, else false
	 */
	private boolean processDiseaseInfo(String disease) {
		// Clear any previous disease info
		CDCSectionInfo = new HashMap<>();
		WebMDSectionInfo = new HashMap<>();
		foundInfoSections = new ArrayList<>();
		// Handle CDC page for disease
		try {
			// Try to process and store the disease info from the corresponding file
			CDCSectionInfo = getProcessedFileInfo("data/"+disease.toLowerCase().replace(" ", "-")+"-cdc.txt");
		} catch(Exception e) { 
			System.out.println("No supported CDC page found for \""+disease+"\"");
		}
		
		// Handle WebMD page for disease
		try {
			// Try to process and store the disease info from the corresponding file
			WebMDSectionInfo = getProcessedFileInfo("data/"+disease.toLowerCase().replace(" ", "-")+"-webmd.txt");
		} catch (Exception e) {
			System.out.println("No supported WebMD page found for \""+disease+"\"");
		}
				
		// Returns true only if either the CDCSectionInfo and/or the WebMDSectionInfo is not still empty
		return !CDCSectionInfo.isEmpty() || !WebMDSectionInfo.isEmpty();
	}
	
	/**
	 * Processes disease info from the file with the provided name and returns a map matched regex patterns to the corresponding information sections
	 * @param fileName name of file with disease info
	 * @return HashMap of any matched regex pattern to the corresponding information section
	 * @throws FileNotFoundException
	 */
	private HashMap<Pattern, String> getProcessedFileInfo(String fileName) throws FileNotFoundException {
	    Scanner scanner = new Scanner(new File(fileName));
	    HashMap<Pattern, String> sectionInfo = new HashMap<>();
	    
        String line = scanner.nextLine();
	    // Scan every line of the input file
	    while (scanner.hasNextLine()) {
	        // StringBuilder to format section information
	        StringBuilder sectionInfoSB = new StringBuilder();
	        // Check if a section header has been found
	        if (line.startsWith(SECTION_DELIMETER)) {
	        	boolean matchFound = false;
	        	// Check all the regex patterns
	        	for (Pattern pattern : regexPatterns) {
	        		// Check if the pattern matches the header
	        		if (pattern.matcher(line).find()) {
	        			matchFound = true;
	        			// Store the prompt if it hasn't already been found
	        			String prompt = patternToPrompt.get(pattern);
	        			if (!foundInfoSections.contains(prompt)) {
	        				foundInfoSections.add(prompt);
	        			}
	        			// Append the section header
	        			sectionInfoSB.append(line.replace(SECTION_DELIMETER, "")+"\n");
	        			// Loop through all the lines following the header to store the section information
	        			while (scanner.hasNextLine()) {
	        				line = scanner.nextLine();
	        				// We've reached a separate section, break
	        				if (line.startsWith(SECTION_DELIMETER)) {
	        					sectionInfo.put(pattern, sectionInfoSB.toString().trim());
	        					break;
	        				}
	        				// Append the line to the stored section info
	        				if (!line.isBlank())
	        					sectionInfoSB.append(line+"\n");
	        				
	        				if(!scanner.hasNextLine())
	        					sectionInfo.put(pattern, sectionInfoSB.toString().trim());
	        			}
	        			// Break out of the for loop since we've already found the matching pattern
	        			break;
	        		}
	        	}
	        	
	        	if (!matchFound) {
        			line = scanner.nextLine();
        			// TODO: Handle sections that don't match a common section?
        		}
	        } else {
	        	line = scanner.nextLine();
	        }
	     }
	    
	    return sectionInfo;
	}
	
	/**
	 * Takes a prompt and attempts to answer it based on the stored information for the found regex patterns
	 * @param prompt String prompt
	 */
	private String answerPrompt(String prompt, String disease) {
		if (prompt.equals(ALL_INFO_REQUEST)) {
			StringBuilder answerSB = new StringBuilder();
			for (Pattern pattern : regexPatterns) {
				// Try to get the CDC and WebMD info from the matched pattern
				String CDCInfo = CDCSectionInfo.get(pattern);
				String WebMDInfo = WebMDSectionInfo.get(pattern);
				if (CDCInfo != null && WebMDInfo != null) {
					// Both pages have the info, find the section with the most information and print it
					if (WebMDInfo.length() > CDCInfo.length()) {
						answerSB.append(WebMDInfo+"\n\n");
					} else {
						answerSB.append(CDCInfo+"\n\n");
					}
				} else if (CDCInfo != null) {
					answerSB.append(CDCInfo+"\n\n");
				} else if (WebMDInfo != null) {
					answerSB.append(WebMDInfo+"\n\n");
				}
			}
			return answerSB.toString().trim();
		} else {
	    	// Check all the regex patterns
			for (Pattern pattern : regexPatterns) {
	    		// Check if the pattern matches the prompt
				if (pattern.matcher(prompt).find()) {
					// Double check that the pattern is asking about the disease itself
					if (pattern == WHAT_IS && !(prompt.toLowerCase().contains("what is "+disease.toLowerCase()) || !prompt.toLowerCase().contains("what's "+disease.toLowerCase()) || !prompt.toLowerCase().contains("tell me about "+disease.toLowerCase()))) {
						continue;
					}
					
					// Try to get the CDC and WebMD info from the matched pattern
					String CDCInfo = CDCSectionInfo.get(pattern);
					String WebMDInfo = WebMDSectionInfo.get(pattern);
					if (CDCInfo != null && WebMDInfo != null) {
						// Both pages have the info, find the section with the most information and print it after removing the header
						if (WebMDInfo.length() > CDCInfo.length()) {
							return WebMDInfo.substring(WebMDInfo.indexOf('\n')+1);
						} else {
							return CDCInfo.substring(CDCInfo.indexOf('\n')+1);
						}
					} else if (CDCInfo != null) {
						return CDCInfo.substring(CDCInfo.indexOf('\n')+1);
					} else if (WebMDInfo != null) {
						return WebMDInfo.substring(WebMDInfo.indexOf('\n')+1);
					}
				}
			}
			
			// Format a String a list of all the info we found for this disease
			StringBuilder sb = new StringBuilder();
			for (String section : foundInfoSections)
				sb.append(section + ", ");
			sb.replace(sb.length() - 2, sb.length(), "");
			return "Sorry, I wasn't able to find any information to answer your question. I did find information about "+WordUtils.capitalize(disease)+", try asking me about one of the following: " + sb.toString()+". (Or try asking me about another disease!)";
		}
	}
	
	/**
	 * Takes a String disease and user input and gives a response based on processed disease info.
	 * @param disease String disease
	 * @param prompt String user prompt
	 * @return String response
	 */
	public String handleUserInput(String disease, String prompt) {
		// Check if we were successfully able to process info for this disease
		if (processDiseaseInfo(disease)) {
			// Try to answer the prompt
			return answerPrompt(prompt, disease);
		}
		else {
			return "Unable to answer prompt.\n";
		}
	}
}
