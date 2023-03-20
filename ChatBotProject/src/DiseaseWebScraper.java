import com.gargoylesoftware.htmlunit.*;
import com.gargoylesoftware.htmlunit.html.*;
import java.io.*;
import java.util.*;

/**
 * This class is able to parse disease information from WebMD and CDC websites for certain diseases.
 * It is able to separate this info into sections based on the headers of the webpages, but 
 * currently this class doesn't do anything other than print these sections to a text file in the 
 * order they appear on the webpage. I've only tested a small subset of the diseases for both CDC and 
 * WebMD. But in theory, this should work for any of the diseases listed on the CDC website or any 
 * disease on WebMD I can find an article with the right format.
 * @author Christine Steege
 *
 */
public class DiseaseWebScraper {
	/** WebClient to use to access disease web pages */
	private WebClient client;
	/** List of supported CDC diseases */
	private List<String> supportedCDCDiseases;
	/** List of supported WebMD diseases */
	private List<String> supportedWebMdDiseases;
	/** Maps disease name to the CDC link for that disease */
	private HashMap<String, String> diseaseToCDCLink;
	/** Maps disease name to the WebMD link for that disease */
	private HashMap<String, String> diseaseToWebMdLink;
	
	private String delimeter = "<SECT>";
	
	/**
	 * Default Constructor
	 * Sets up the web client and the map of the disease name to WebMd link.
	 */
	public DiseaseWebScraper() {
		client = new WebClient();

		// Disable the CSS/Javascript stuff
		client.getOptions().setCssEnabled(false);
		client.getOptions().setJavaScriptEnabled(false);
		client.getOptions().setThrowExceptionOnFailingStatusCode(false);
		client.getOptions().setThrowExceptionOnScriptError(false);
		// Prevent the entire html from being printed to the console
		client.getOptions().setPrintContentOnFailingStatusCode(false);
		
		// Create the hash map and fill it with the CDC diseases that this program currently supports
		diseaseToCDCLink = new HashMap<>();
		diseaseToCDCLink.put("measles", "https://wwwnc.cdc.gov/travel/diseases/measles");
		diseaseToCDCLink.put("mumps", "https://wwwnc.cdc.gov/travel/diseases/mumps");
		diseaseToCDCLink.put("malaria", "https://wwwnc.cdc.gov/travel/diseases/malaria");
		diseaseToCDCLink.put("polio", "https://wwwnc.cdc.gov/travel/diseases/poliomyelitis");
		diseaseToCDCLink.put("hiv", "https://wwwnc.cdc.gov/travel/diseases/hiv");
		diseaseToCDCLink.put("rubella", "https://wwwnc.cdc.gov/travel/diseases/rubella");
		diseaseToCDCLink.put("rabies", "https://wwwnc.cdc.gov/travel/diseases/rabies");
		diseaseToCDCLink.put("avian flu", "https://wwwnc.cdc.gov/travel/diseases/avian-bird-flu");
		diseaseToCDCLink.put("scabies", "https://wwwnc.cdc.gov/travel/diseases/scabies");

		// Create the hash map and fill it with the WebMd diseases that this program currently supports
		diseaseToWebMdLink = new HashMap<>();
		diseaseToWebMdLink.put("measles", "https://www.webmd.com/children/vaccines/what-is-measles");
		diseaseToWebMdLink.put("mumps", "https://www.webmd.com/children/vaccines/what-are-the-mumps");
		diseaseToWebMdLink.put("malaria", "https://www.webmd.com/a-to-z-guides/malaria-symptoms");
		diseaseToWebMdLink.put("polio", "https://www.webmd.com/children/what-is-polio");
		diseaseToWebMdLink.put("hiv", "https://www.webmd.com/skin-problems-and-treatments/aids-related-skin-conditions");
		diseaseToWebMdLink.put("rubella", "https://www.webmd.com/a-to-z-guides/what-is-rubella");
		// TODO: Frustratingly, some WebMD pages HTML is formatted slightly different than the other pages, so I need to come up 
		// with a way to account for this or simply tweak the HTML myself and always use a local copy
		// Also some pages are split into separate webpages on the WebMD site, so I either need to find a way to scrape multiple 
		// WebMD pages for one disease or rely on a local copy of the HTML where I combine the pages into one
		
		// Get the lists of currently supported diseases for each site
		supportedCDCDiseases = new ArrayList<String>(diseaseToCDCLink.keySet());
		supportedWebMdDiseases = new ArrayList<String>(diseaseToWebMdLink.keySet());
	}
	
	/**
	 * Method to get a supported disease list for CDC
	 * @return List of supported disease Strings
	 */
	public List<String> getSupportedCDCDiseases() {
		return supportedCDCDiseases;
	}
	
	/**
	 * Method to get a supported disease list for WebMd
	 * @return List of supported disease Strings
	 */
	public List<String> getSupportedWebMdDiseases() {
		return supportedWebMdDiseases;
	}
	
	/**
	 *  Method to close any resources opened when an instance of this class is created.
	 */
	public void close() {
		client.close();
	}
	
	/**
	 * Parses the CDC webpage for the provided disease and prints the formatted results to a text file
	 * @param disease String name of disease
	 * @return true if successful, false if unsuccessful
	 */
	public boolean parseCDCPage(String disease) {
		// Get the search URL from the map of disease names to URL
		String searchUrl;
		if (diseaseToCDCLink.containsKey(disease.toLowerCase())) {
			searchUrl = diseaseToCDCLink.get(disease.toLowerCase());
		}
		else {
			System.out.println(disease+" is not a suppported disease.");
			return false;
		}
		disease = disease.toLowerCase();
		HtmlPage page;
		DomNode descriptionSectionNode = null;

		List<DomNode> headingNodes = new ArrayList<>();
		
		try {
			// Get the page based on the URL 
			page = client.getPage(searchUrl);
			
			// Get a list of the headings for each section
			List<HtmlHeading3> headings = page.getByXPath("//h3");

			// Loop through all of the web page headings
			for (HtmlHeading3 heading : headings) {
				// We want to keep track of the node that corresponds to the "What is <disease>?" section as we 
				// will need to do a bit of special parsing for this section
				if (heading.getId().contains("whatis")) 
					descriptionSectionNode = heading.getParentNode();
				// Add all other nodes to the heading node list until we've reached the "After Travel" section
				else if (!heading.getId().equals("aftertravel"))
					headingNodes.add(heading.getParentNode());
				// We've reached the "After Travel" section, break;
				else if (heading.getId().equals("aftertravel")) {
					headingNodes.add(heading.getParentNode());
					break;
				}
			}
			
			if (descriptionSectionNode == null) {
				// Something went wrong parsing the headings. Maybe the web page formatting changed.
				throw new Exception();
			}
		}
		catch (Exception e) {
			System.out.println("Something went wrong trying to parse the CDC webpage for "+disease+". Attempting to load local copy...");
			try {
				File f = new File("html/"+disease.replaceAll(" ", "-")+"-cdc.html");
				page = client.getPage("file:\\\\"+f.getAbsolutePath());
				
				// Get a list of the headings for each section
				List<HtmlHeading3> headings = page.getByXPath("//h3");

				// Loop through all of the web page headings
				for (HtmlHeading3 heading : headings) {
					// We want to keep track of the node that corresponds to the "What is <disease>?" section as we 
					// will need to do a bit of special parsing for this section
					if (heading.getId().contains("whatis")) 
						descriptionSectionNode = heading.getParentNode();
					// Add all other nodes to the heading node list until we've reached the "After Travel" section
					else if (!heading.getId().equals("aftertravel"))
						headingNodes.add(heading.getParentNode());
					// We've reached the "After Travel" section, break;
					else if (heading.getId().equals("aftertravel")) {
						headingNodes.add(heading.getParentNode());
						break;
					}
				}
				
				if (descriptionSectionNode == null) {
					// Something went wrong parsing the headings. Maybe the web page formatting changed.
					throw new Exception();
				}
			} 
			catch (Exception ex) {
				System.out.println("Unable to load local copy.");
				ex.printStackTrace();
				return false;
			}
		} 
		
		// Creating a list to store the parsed disease info for the first section
		List<String> diseaseInfo = new ArrayList<>();
		diseaseInfo.add(delimeter+descriptionSectionNode.getVisibleText());
		
		// The CDC website includes a general disease definition and the symptoms in one section, so I wanted to 
		// try to detect these individual portions of information and separate them as they may be useful in the future
		DomNode next = descriptionSectionNode.getNextSibling();
	    while (next != null) {
	    	if (next instanceof HtmlParagraph) {
    			// If the paragraph contains the word symptoms, we will assume we have reached the symptoms section
	    		if (next.getTextContent().toLowerCase().contains("symptoms")) break;
	    		// Add the text from the paragraph to the disease info list
	    		String info = next.getTextContent().trim();
	    		if (!info.isBlank()) diseaseInfo.add(info);
	    	}
	    	else if (next instanceof HtmlUnorderedList) 
	    		// Add the formatted list string to the disease info list
    			diseaseInfo.add(getListAsFormattedString((HtmlUnorderedList)next));
	    	// Go to the next node
	    	next = next.getNextSibling();
	    } // At the end we should have reached the symptoms section
	    
		// Create a list for the section String lists and add the first section
		List<List<String>> sections = new ArrayList<>();
		diseaseInfo.add("(Source: "+diseaseToCDCLink.get(disease)+")");
	    sections.add(diseaseInfo);
		// Create a String list for the Strings of each section
		ArrayList<String> sectionInfo = new ArrayList<>();
		// Parse and add the symptom section information
		sectionInfo.add(delimeter+"Symptoms");
		getInfoFromCDCSection(next, headingNodes.get(1), sectionInfo);
		sectionInfo.add("(Source: "+diseaseToCDCLink.get(disease)+")");
		sections.add(sectionInfo);
		
		// Starting after the title section, loop through every other section except the last one, which will be the "After Travel" section
		// Currently, the "After Travel" and "More Information" sections are ignored
		for (int i = 1; i < headingNodes.size()-1; i++) {
			// Reset the String List for the next section
			sectionInfo = new ArrayList<>();
			// Adding in the header text simply for the sake of printing the complete webpage to a text file
			sectionInfo.add(delimeter+headingNodes.get(i).getTextContent());
		    // Extract the disease info for each section
			getInfoFromCDCSection(headingNodes.get(i), headingNodes.get(i+1), sectionInfo);
			// Add the section String list to the sections list
			sectionInfo.add("(Source: "+diseaseToCDCLink.get(disease)+")");
			sections.add(sectionInfo);
		}
	    
	    // Print to a text file
	    printDiseaseInfoToTextfile(disease.replace(" ", "-")+"-cdc.txt", sections);
	    
	    // Print the stats
//		System.out.println("Source: CDC");
//	    printDiseaseInfoStats(disease, sections);
//	    System.out.println();
	    
	    return true;
	}
	
	/**
	 * Parses the WebMD webpage for the provided disease and prints the formatted results to a text file
	 * @param disease String name of disease
	 * @return true if successful, false if unsuccessful
	 */
	public boolean parseWebMdPage(String disease) {
		// Get the search URL from the map of disease names to URL
		String searchUrl;
		if (diseaseToWebMdLink.containsKey(disease.toLowerCase())) {
			searchUrl = diseaseToWebMdLink.get(disease.toLowerCase());
		}
		else {
			System.out.println(disease+" is not a suppported disease.");
			return false;
		}
		disease = disease.toLowerCase();

		HtmlPage page;
		List<DomNode> headings;
		try {
			// Get the page based on the URL
			page = client.getPage(searchUrl);

			// Get a list of the headings for each section
			headings = page.getByXPath("//h2");
			int numSections = headings.size();
			
			if (numSections == 0) {
				// Something went wrong parsing the headings. Maybe the webpage formatting changed.
				throw new Exception();
			}
		} 
		catch (Exception e) {
			System.out.println("Something went wrong trying to parse the WebMD webpage for "+disease+". Attempting to load local copy...");
			try {
				File f = new File("html/"+disease.replaceAll(" ", "-")+"-webmd.html");
				page = client.getPage("file:\\\\"+f.getAbsolutePath());
				
				// Get a list of the headings for each section
				headings = page.getByXPath("//h2");
			} 
			catch (Exception ex) {
				System.out.println("Unable to load local copy");
				ex.printStackTrace();
				return false;
			}
		}
		
		int numSections = headings.size();
		// Create a list for the section String lists
		List<List<String>> sections = new ArrayList<>();
		for (int i = 0; i < numSections; i++) {
			// Create a String list for each section
			ArrayList<String> sectionInfo = new ArrayList<>();
			// Adding in the header text simply for the sake of printing the complete webpage to a text file
			sectionInfo.add(delimeter+headings.get(i).getTextContent());
		    // Extract the disease info for each section
			getInfoFromWebMdSection(headings.get(i), sectionInfo);
			// Add the section String list to the sections list
			sectionInfo.add("(Source: "+diseaseToWebMdLink.get(disease)+")");
			sections.add(sectionInfo);
		}
		
	    // Print to a text_file
		printDiseaseInfoToTextfile(disease.replace(" ", "-")+"-webmd.txt", sections);
		
	    // Print the stats
//		System.out.println("Source: WebMD");
//	    printDiseaseInfoStats(disease, sections);
//	    System.out.println();
	    
	    return true;
	}
	
	/**
	 * Parses and formats text from a CDC disease webpage section into the provided String list 
	 * starting from the provided current DomNode until the provided next DomNode.
	 * @param currentNode The current DomNode you want to parse information starting from
	 * @param nextNode The DomNode you want to parse until
	 * @param infoList List to store formatted String info extracted from HTML elements. 
	 * A new String is added for each are separate paragraph or list
	 */
	private void getInfoFromCDCSection(DomNode currentNode, DomNode nextNode, List<String> infoList) {
		while (currentNode != nextNode && currentNode != null) {
	    	if (currentNode instanceof HtmlParagraph || currentNode instanceof HtmlHeading4) {
	    		// Add the text from the paragraph to the disease info list
	    		String info = currentNode.getTextContent().trim();
    			if (!info.isBlank()) infoList.add(info);
	    	}
	    	else if (currentNode instanceof HtmlUnorderedList) {
	    		// Add the formatted list String to the info list
	    		infoList.add(getListAsFormattedString((HtmlUnorderedList)currentNode));
	    	}
	    	else if (currentNode instanceof HtmlDivision) {
	    		// There may be content within a division, check the children
	    		List<DomNode> children = currentNode.getChildNodes();
	    		for (DomNode child : children) {
	    			if (child instanceof HtmlParagraph) {
	    	    		// Add the text from the paragraph to the disease info list
	    	    		String info = child.getTextContent().trim();
	        			if (!info.isBlank()) infoList.add(info);
	    	    	}
	    	    	else if (child instanceof HtmlUnorderedList) {
	    	    		// Add the formatted list String to the info list
	    	    		infoList.add(getListAsFormattedString((HtmlUnorderedList)child));
	    	    	}
	    		}
	    	}
	    	// Go to the next node
	    	currentNode = currentNode.getNextSibling(); 
	    }
	}
	
	/**
	 * Parses and formats text from a CDC disease webpage section starting from the current 
	 * node until the method detects we have reached a new section. 
	 * @param currentNode The current DomNode you want to parse information starting from
	 * @param infoList List to store formatted String info extracted from HTML elements. 
	 * A new String is added for each are separate paragraph or list
	 */
	private void getInfoFromWebMdSection(DomNode currentNode, List<String> infoList) {
		while (currentNode != null) {
	    	if (currentNode instanceof HtmlParagraph) {
	    		// Add the text from the paragraph to the disease info list
	    		String info = currentNode.getTextContent().trim();
    			if (!info.isBlank()) infoList.add(info);
	    	}
	    	else if (currentNode instanceof HtmlUnorderedList) {
	    		// Add the formatted list String to the info list
	    		infoList.add(getListAsFormattedString((HtmlUnorderedList)currentNode));
	    	}
	    	// For some reason, paragraphs are sometimes put in separate sections within the same 
	    	// section, so we have check for that
	    	else if (currentNode instanceof HtmlSection) {
	    		List<DomNode> nodes = currentNode.getChildNodes();
	    		boolean reachedSeperateHeading = false;
	    		for (DomNode node : nodes) {
	    			if (node instanceof HtmlParagraph) {
	    	    		// Add the text from the paragraph to the disease info list
	    	    		String info = node.getTextContent().trim();
	        			if (!info.isBlank()) infoList.add(info);
	    	    	}
	    	    	else if (node instanceof HtmlUnorderedList) {
	    	    		// Add the formatted list String to the info list
	    	    		infoList.add(getListAsFormattedString((HtmlUnorderedList)node));
	    	    	}
	    			// There is a chance this section contains a new heading, in which case we should 
	    			// break out of the for loop and while loop
	    	    	else if (node instanceof HtmlHeading2) {
	    	    		reachedSeperateHeading = true;
	    	    		break;
	    	    	}
	    		}
	    		// Break out of while loop
	    		if (reachedSeperateHeading) break;
	    	} 
	    	
	    	if (currentNode.getNextSibling() != null) {
	    		// There's a next node, go to the next node
		    	currentNode = currentNode.getNextSibling();
	    	}
	    	else {
	    		// Strangely, each header might have multiple separate sections in the HTML so we need to 
		    	// check if this is the case or not before assuming we've truly reached the end of the section 
	    		boolean newSection = false;
	    		List<DomNode> nodes = new ArrayList<>();
				if (currentNode.getParentNode().getNextSibling() != null) {
					nodes = currentNode.getParentNode().getNextSibling().getChildNodes();
					for (DomNode node : nodes) {
						if (node instanceof HtmlHeading2) {
							// We've reached a different section
							newSection = true;
							currentNode = null;
							break;
						}
					}
		    		// If we haven't reached a true new section, set the current node to the first node 
					// of this neighboring section, as it must contain more relevant info 
		    		if (!newSection) currentNode = nodes.get(0);	
		    		else break;
				} 
	    		else {
	    			// There's a chance this section may be at the split of one of the "article-page" HTML divisions 
	    			// so we also need to check for that
	    			boolean divisionBreak = false;
	    			if (currentNode.getParentNode().getParentNode().getNextSibling() != null) {
	    				DomNode node = currentNode.getParentNode().getParentNode().getNextSibling();
	    				// Check if the next node two nodes up in the hierarchy is another one of the "article-page"
	    				// divisions, in which case it might contain more information for this section
	    				if (node instanceof HtmlDivision) {
	    					if (node.getAttributes().getNamedItem("class").getNodeValue().toString().contains("article-page")) {
	    						divisionBreak = true;
	    						// Move the node over to the first node in this new division
	    						currentNode = node.getFirstChild();
	    					}
	    				} 
	    			}
	    			// There's no additional "article-page" divisions, we've truly reached the end so break out of the while
	    			if (!divisionBreak) break;
	    		}
	    	}	
	    }
	}
	
	/**
	 * Creates a formated list as a single String from a given HtmlUnorderedList
	 * @param list the HtmlUnorderedList to read list elements from
	 * @return single formatted string to represent the list
	 */
	private String getListAsFormattedString(HtmlUnorderedList list) {
		// Use a StringBuilder to create the String from the list
		StringBuilder listSB = new StringBuilder();
		// Get the children nodes
		Iterator<DomNode> children = list.getChildren().iterator();
		while (children.hasNext()) {
			DomNode child = children.next();
			if (child instanceof HtmlListItem) {
				Iterator<DomNode> grandChildren = child.getChildren().iterator();
				StringBuilder listItemSB = new StringBuilder("   - ");
				while (grandChildren.hasNext()) {
					DomNode grandChild = grandChildren.next();
					// Make sure it's not a sublists (annoyingly, these really mess up the formatting for lists)
					if (!(grandChild instanceof HtmlUnorderedList)) {
						// Append to the main list item if it's not blank
						if (!grandChild.getVisibleText().isBlank()) {
							listItemSB.append(grandChild.getTextContent());
						}
					} 
					else {
						handleSublist(listItemSB, grandChild, 2);
					}
				}
				// Because of differences between the layout of CDC list text vs WebMD, sometimes we may need to add a newline to 
				// the list item if there isn't already one
				if (!listItemSB.toString().endsWith("\n")){
					listItemSB.append("\n");
				}
				// Finally, append the list item to the list StringBuilder
				listSB.append(listItemSB.toString());
			}
		}
		String listString = listSB.toString();
		// Chop off the last "\n"
		if (!listString.isBlank()) listString = listString.substring(0, listString.length()-1);
		return listString;
	}
	
	/**
	 * Recursive method to handle sublists within lists. 
	 * @param listItemSB StringBuilder to add sublist String to
	 * @param listNode DomNode of the HtmlUnorderedList
	 * @param depth How deep into the list we are (normal list point: depth = 1, sublist point: depth = 2, etc.)
	 */
	private void handleSublist(StringBuilder listItemSB, DomNode listNode, int depth) {
		String strippedListItemString = listItemSB.toString().stripTrailing();
		listItemSB.replace(0, listItemSB.length(), strippedListItemString);

		// This Node is a sublist, so we have to iterate over it's items
		DomNodeList<DomNode> sublistChildern = listNode.getChildNodes();
		for (DomNode sublistchild : sublistChildern) {
			DomNodeList<DomNode> sublistGrandChildren = sublistchild.getChildNodes();
			for (DomNode grandChild : sublistGrandChildren) {
				if (!(grandChild instanceof HtmlUnorderedList)) {
					// Append to the main list item if it's not blank
					if (!grandChild.getVisibleText().isBlank()) {
						listItemSB.append("\n");
						for (int i = 0; i < depth; i++)
							listItemSB.append("   ");
						listItemSB.append("- "+grandChild.getTextContent());
					}
				} 
				else {
					handleSublist(listItemSB, grandChild, depth+1);
				}
			}
		} 
	}
	
	/**
	 * Prints the parsed disease info for the webpage to a text file with the provided name based on the provided 
	 * list of section String lists
	 * @param outputFileName name of the output file
	 * @param sectionInfoStrings list that contains each section of the web page parsed into a list 
	 * of string by header/paragraphs/lists
	 */
	private void printDiseaseInfoToTextfile(String outputFileName, List<List<String>> sectionInfoStrings) {
		try {
			// Open a new FileWriter with the file name
			FileWriter writer = new FileWriter("data/" + outputFileName);
			// Loop through each information section
			for (List<String> sectionInfo : sectionInfoStrings) {
				// Loop through each parsed String of the section (these strings represent the header or 
				// individual paragraphs or lists
				for (String infoString : sectionInfo) {
					writer.write(infoString+"\n");
				}
				writer.write("\n");
			}
			writer.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * Prints the stats for the provided disease info parsed from webpage of that disease to the console
	 * @param disease String name of the disease
	 * @param sectionInfoStrings list that contains each section of the web page parsed into a list 
	 * of string by header/paragraphs/lists
	 */
	private void printDiseaseInfoStats(String disease, List<List<String>> sectionInfoStrings) {
		System.out.println("Disease: "+disease);
		System.out.println("Number of sections: "+ sectionInfoStrings.size());
		int sectionNum = 1;
		int totalCharCount = 0;
		int totalWordCount = 0;
		int totalLineCount = 0;
		for (List<String> sectionInfo : sectionInfoStrings) {
			int sectionWordCount = 0;
			int sectionCharCount = 0;
			int sectionLineCount = 0;
			for (String infoString : sectionInfo) {
				sectionCharCount+= infoString.length();
				sectionWordCount += infoString.split("\\s+").length;
				sectionLineCount++;
			}
			totalCharCount += sectionCharCount;
			totalWordCount += sectionWordCount;
			totalLineCount += sectionLineCount;
			System.out.println("Section "+sectionNum+":");
			System.out.println("\tCharacter count: "+sectionCharCount);
			System.out.println("\tWord count: "+sectionWordCount);
			System.out.println("\tLine count: "+sectionLineCount);
			sectionNum++;
		}
		System.out.println("Total character count: "+totalCharCount);
		System.out.println("Total word count: "+totalWordCount);
		System.out.println("Total line count: "+totalLineCount);
	}
	
	/**
	 * Method to parse all supported diseases.
	 */
	public void parseAllDiseases(){
		for (String disease : supportedCDCDiseases)
			parseCDCPage(disease);
		for (String disease : supportedWebMdDiseases)
			parseWebMdPage(disease);
	}

	
	/**
	 * This is just a testing method to print out all of the WebMD section headings for 
	 * the supported diseases to get a feel for what possible sections there are
	 * @param disease String disease name
	 */
	private void printWebMdHeadings(String disease) {
		String searchUrl = diseaseToWebMdLink.get(disease);		
		HtmlPage page;
		List<HtmlHeading2> headings;
		try {
			page = client.getPage(searchUrl);
			headings = page.getByXPath("//h2");
			for (HtmlHeading2 heading : headings)
				System.out.println(heading.getTextContent());
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
