/**
 * This represents a ChatBot interface. The bot must provide a method to able to take a String user entry and provide a String response.
 * @author Christine Steege
 *
 */
public interface ChatBot {
	/**
	 * This method takes a string user entry and returns a String response.
	 * @param userEntry String user entry
	 * @return Generated String response
	 */
	public String getResponse(String userEntry); 
}
