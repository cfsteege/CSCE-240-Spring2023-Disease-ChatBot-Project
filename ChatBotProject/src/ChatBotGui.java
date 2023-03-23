import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import java.io.FileWriter;

import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.plaf.metal.MetalButtonUI;
import javax.swing.text.*;

import com.formdev.flatlaf.FlatLightLaf;


/**
 * This class takes an implementation of the ChatBot interface and creates a simple, 
 * customizable GUI for the user to enter input and see the ChatBot responses.
 * @author Christine Steege
 *
 */

//TODO: handle "quit"/"exit" queries?

public class ChatBotGui extends JFrame {
	/** Chat bot that is used to generate responses to user input */
	private ChatBot chatBot;
	/** Color of bot text */
	private Color botTextColor;
	/** Color of send button */
	private Color sendButtonColor;
	/** File path of text file to print session to */
	private String outputFilePath;
	/** Flag to tell whether to print the session to a file or not */
	private boolean printSessionToFile = false;
	
	/** Chat pane to display the bot and user chat */
	private JTextPane chatPane = new JTextPane();
	/** Button to send user entry */
	private JButton sendButton = new JButton();
	/** Text field for user to entry input */
	private JTextField textField = new JTextField();
	
	/**
	 * Constructor that only takes in the ChatBot and creates a generic GUI.
	 * @param chatBot ChatBot used to generate responses to user input
	 */
	public ChatBotGui(ChatBot chatBot) {
		this(chatBot, Color.GRAY, Color.BLUE, "", (new JFrame()).getIconImage(), null);
	}
	
	/**
	 * Fully parameterized constructor to fully customize GUI upon instantiation.
	 * @param chatBot ChatBot used to generate responses to user input
	 * @param sendButtonColor Send button color
	 * @param botTextColor Bot text color
	 * @param title GUI frame title 
	 * @param frameIcon GUI frame icon
	 */
	public ChatBotGui(ChatBot chatBot, Color sendButtonColor, Color botTextColor, String title, Image frameIcon, Image sendIcon) {
		// Set all the class attributes
		this.chatBot = chatBot;
		setSendButtonColor(sendButtonColor);
		setBotTextColor(botTextColor);
		setTitle(title);
		
		// Try to use the FlatLightLaf look and feel
		try {
			UIManager.setLookAndFeel(new FlatLightLaf());
		} catch (UnsupportedLookAndFeelException e1) {
			System.out.println("Unable to load FlatLightLaf look and feel, using default look and feel.");
		}
		
		// Main panel to contain all the components
		JPanel mainPanel = new JPanel();
		// Set the layout for the main panel
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
		
		// Customize the chat pane
		chatPane.setPreferredSize(new Dimension(350, 450));
		chatPane.setEditable(false);
		chatPane.setBackground(Color.WHITE);
		chatPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		// Scoll pane to store the chat pane
		JScrollPane chatScrollPane = new JScrollPane(chatPane);
		
		// Add an action listener to the text field (this will be called when the enter key is pressed)
		textField.addActionListener(e->handleSendAction());
		
		// Add an action listener to the send button
		sendButton.addActionListener(e->handleSendAction());
		// Set the size of the send button
		sendButton.setPreferredSize(new Dimension(35, 20));
		// Set the icon to a scaled down image of a white arrow
		sendButton.setIcon(new ImageIcon(sendIcon.getScaledInstance(15, 15,  java.awt.Image.SCALE_AREA_AVERAGING)));
		//sendButton.setBorderPainted(false);
		
		// Panel to contain the text field and send button
		JPanel textFieldPanel = new JPanel(new BorderLayout(5,0));
		// Add the text field and send button to the text field panel
		textFieldPanel.add(textField, BorderLayout.CENTER);
		textFieldPanel.add(sendButton, BorderLayout.EAST);
		// Add an upper border to the panel to create a bit of separation between this and the chat area
		textFieldPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

		// Add the scroll pane and text field panel to the main panel
		mainPanel.add(chatScrollPane);
		mainPanel.add(textFieldPanel);
		// Add an empty border to the main panel
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
		// Customize the frame and add the main panel
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setContentPane(mainPanel);
		this.pack();
		this.setResizable(false);
		
		if (System.getProperty("os.name").toLowerCase().contains("mac")) {
			JRootPane rootPane = SwingUtilities.getRootPane(sendButton);
			rootPane.setDefaultButton(sendButton);
		}
		
		// Set the icon for the frame for Windows OS (and other systems which do support this method)
		this.setIconImage(frameIcon);
        // Set icon for Mac OS (and other systems which do support this method)
		try {
	        Taskbar.getTaskbar().setIconImage(frameIcon);
        } catch (Exception e) {
            System.out.println("The os does not support: 'taskbar.setIconImage'");
        }
		
		// Add a window listener to print to a text file upon closing if that option is selected 
		this.addWindowListener(new WindowAdapter() {
	        @Override
	        public void windowClosing(WindowEvent e) {
	        	if (printSessionToFile)
	        		printChatSessionToFile();
	        }
	    });
	}
	
	/**
	 * This gets the generated response from the ChatBot for the current user entry from the 
	 * text field and appends the user entry and response to the chat pane. This is called 
	 * when either the enter key is pressed from the text field or the send button is pressed.
	 */
	private void handleSendAction() {
		// Get the current entry from the text field
		String userEntry = textField.getText();
		// Check that the entry is not blank
		if (!userEntry.isBlank()) {
			// Append the user entry
			appendToPane("You", userEntry, Color.BLACK);
			// Clear the text field
			textField.setText("");
			
			// Get the generated response from the ChatBot
			String response = chatBot.getResponse(userEntry);
			// Append the response to the chat pane
			appendToPane("Bot", response, botTextColor);
		}
	}
	
	/**
	 * Appends the String user and String message to the text pane in the provided color as "<user>: <message>".
	 * @param user String name to use as the sender of the message.
	 * @param message String message to append to the text pane.
	 * @param color Color to use for text appended to the text pane.
	 */
	private void appendToPane(String user, String message, Color color)
    {
		// Create an instance of a SimpleAttributeSet
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();  
		// Make the attribute set bold for the user name and set it to the provided color
        StyleConstants.setBold(attributeSet, true); 
        StyleConstants.setForeground(attributeSet, color);  
        // Set the character attributes for the provided pane to the custom attribute set
        chatPane.setCharacterAttributes(attributeSet, true); 
        // Get a Document to append text to for the pane
        Document doc = chatPane.getStyledDocument();  
        try {
        	// Add the user name
			doc.insertString(doc.getLength(), user+": ", attributeSet);
			// Remove the bold from the text
			StyleConstants.setBold(attributeSet, false); 
			// Add the user message
	        doc.insertString(doc.getLength(), message+"\n", attributeSet);
		} catch (BadLocationException e) {
			System.out.println("Something went wrong within the document model for the chat text pane.");		
		}  

    }
	
	/**
	 * Sets the bot text color to the provided color.
	 * @param color Color to set bot text color to.
	 */
	public void setBotTextColor(Color color) {
		this.botTextColor = color;
	}
	
	/**
	 * Sets the send button color to the provided color.
	 * @param color Color to set the send button color to.
	 */
	public void setSendButtonColor(Color color) {
		this.sendButtonColor = color;
		sendButton.setBackground(this.sendButtonColor);
	}
	
	/**
	 * Sets the file path to print a session to. If this path is not set, the session will not be printed to an output file. 
	 * This should be called before the ChatBotGui is set to visible.
	 * @param filePath String file path of text file	 
	 */
	public void printSessionToFile(String filePath) {
		this.printSessionToFile = true;
		this.outputFilePath = filePath;
	}
	
	/**
	 * Prints the chat session to the output text file.
	 */
	private void printChatSessionToFile() {
		try {
			// Open a new FileWriter with the file name
			FileWriter writer = new FileWriter(outputFilePath);
			// Write the text from the chat pane to the file
			writer.write(chatPane.getText());
			writer.close();
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Main method.
	 * @param args
	 */
	public static void main(String[] args) {		
		Image sendImage = new ImageIcon(ChatBotGui.class.getResource("/SendImage.png")).getImage(); 
		Image frameImage = new ImageIcon(ChatBotGui.class.getResource("/VirusIcon.png")).getImage();
		ChatBotGui diseaseChatGui = new ChatBotGui(new DiseaseChatBot(), Color.decode("#02ACC9"), Color.decode("#00859B"), "Disease Chatbot", frameImage, sendImage);
		//diseaseChatGui.printSessionToFile("test/chat-session");
		diseaseChatGui.setVisible(true);
	}

}
