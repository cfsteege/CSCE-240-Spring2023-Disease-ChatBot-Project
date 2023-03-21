import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import com.swabunga.spell.event.TeXWordFinder;

/**
 * This class provides a simple API for using Jazzy to correct text based on the provided dictionary file.
 * The Jazzy library relies on Lawrence Philips' Metaphone Algorithm and a form of Near-Miss Algorithm to 
 * give the nearest suggestions within a certain tolerance of error for misspelled words based on a 
 * provided dictionary of words.
 * 
 * @author Christine Steege
 * 
 */
public class SpellingCorrector implements SpellCheckListener {
	// Spell checker to spell checker 
	private SpellChecker spellChecker;
	// Keep a list of the misspelled words
	private List<String> misspelledWords;
	// SpellDictionaryHashMap to store dictionary words
	private SpellDictionaryHashMap dictionaryHashMap;

	/**
	 * Constructor
	 * @param dictionary text file to use as the dictionary for the spell checker
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public SpellingCorrector(File dictionary) throws FileNotFoundException, IOException {
		misspelledWords = new ArrayList<String>();
		// Create the SpellDictionaryHashMap from the provided dictionary file
		dictionaryHashMap = new SpellDictionaryHashMap(dictionary);
		spellChecker = new SpellChecker(dictionaryHashMap);
		// Add this class as a spell check listener to the spell checker
		spellChecker.addSpellCheckListener(this);
	}

	/**
	 * Returns the corrected input text based on the dictionary words.
	 * @param text input to correct
	 * @return the corrected input
	 */
	public String getCorrectedText(String text) {
		// Check the spelling for the words in the provided text (a SpellCheckEvent will occur if the word is found to be misspelled)
		spellChecker.checkSpelling(new StringWordTokenizer(text, new TeXWordFinder()));		
		
		// Loop through all the misspelled words 
		for (String misspelledWord : misspelledWords) {
			// Don't try to correct single character words
			if (misspelledWord.length() < 2)
				continue;
			// Get the suggested word based on the provided dictionary
			List<Word> suggestions = spellChecker.getSuggestions(misspelledWord, 0);
			// If there are suggestions, that means the word was within tolerance of one or more of the dictionary words
			if (suggestions.size() > 0) {
				// Get the best suggestion
				String bestSuggestion = (suggestions.get(0).toString());
				// Replace the misspelled word with the best suggestion
				text = text.replace(misspelledWord, bestSuggestion);
			}
			
		}
		return text;
	}

	/**
	 * This is the implementation of the spellingError method for the SpellCheckListener. This 
	 * method is called whenever a SpellCheckEvent occurs from a class that this class is added 
	 * to as a listener.
	 */
	@Override
	public void spellingError(SpellCheckEvent event) {
		// Every time a spell check event occurs, we will update our list of misspelled words
		misspelledWords.add(event.getInvalidWord());
	}

}