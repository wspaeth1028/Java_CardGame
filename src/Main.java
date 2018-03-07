import java.util.*;
import java.lang.Thread;
import java.io.*;

/**
 * @author wspaeth
 * @version 1.0.0.0
 * class to hold all methods relevant to all players, such as determining player turn,
 * set next player turn, and whether or not to keep playing. 
 * Also holds entry point for program, main()
 * adding another comment
 */
public class Main {

	//fields to dynamically construct the game based off:
	//	1. number of players
	//	2. number of preferred cards each player is assigned
	//	3. number of duplicates needed to win the game
	private static int numPlayers = 4;
	private static int numPreferredCards = 2;
	private static int cardsPerPlayer = numPlayers * numPreferredCards;
	private static int dupsToWin = 4;
	private static int handSize = dupsToWin;
	private static int pileSize = cardsPerPlayer - handSize;
	private static int deckSize = numPlayers * cardsPerPlayer;
	private static ArrayList<Integer> deck = new ArrayList<Integer>();
	private static Player[] players = new Player[numPlayers];
	private static boolean[] playersTurns = new boolean[numPlayers];
	private static Thread[] threads = new Thread[numPlayers];
	
	private static boolean keepPlaying = true;
	private static File dir = new File("./Logs");
	private static File[] logFiles;

	/**
	 * @return keepPlaying
	 * method to return the boolean that tells all players whether or not to keep playing
	 * Synchronized for thread safety
	 */
	public static synchronized boolean getKeepPlaying(){return keepPlaying;}
	/**
	 * @return int
	 * method to return number of duplicates in your hand required to win
	 * syncrhonized for thread safety
	 * 
	 */
	public static synchronized int getDupsToWin(){return dupsToWin;}
	
	/**
	 * @param player
	 * @return boolean
	 * method to return whether or not it is calling players turn
	 * synchronized for thread safety
	 */
	public static synchronized boolean getPlayerTurn(Player player)
	{
		for(int i = 0; i < players.length; i++)
		{
			if(players[i] == player)
			{
				return playersTurns[i];
			}
		}
		return false;
	}
	
	/**
	 * @param player
	 * @return Player
	 * method to return the next player, given a particular player
	 * Synchronized for thread safety
	 */
	public static synchronized Player nextPlayer(Player player)
	{
		for(int i = 0; i < numPlayers; i++)
		{
			if(players[i] == player)
			{
				if(i+1 >= numPlayers)
				{	//if last player's turn, then next player is first player
					return players[0];
				}
				else
				{	//return next player in line.
					return players[i+1];
				}
			}
		}
		return player;
	}
	
	/**
	 * @param player
	 * method to set calling player's turn to false and next player's turn to true
	 * Synchronized for thread safety
	 */
	public static synchronized void nextPlayerTurn(Player player)
	{
		for(int i = 0; i < numPlayers; i++)
		{
			if(players[i] == player)
			{
				playersTurns[i] = false;
				if(i+1 < numPlayers)
				{
					playersTurns[i + 1] = true;
				}
				else if(i+1 == numPlayers)
				{
					playersTurns[0] = true;
				}
			}
		}
	}
	
	/**
	 * @param player
	 * method to satisfy the win condition after a meets the requirements
	 * sets all player turns to false
	 * sets keepPlaying boolean to false
	 * synchronized for thread safety
	 */
	public static synchronized void win(Player player)
	{
		//Stop play
		keepPlaying = false;
		//Set all players turns to false
		for(int i = 0; i < numPlayers; i++)
		{
			playersTurns[i] = false;
		}
		System.out.println(player.getPlayerID() + " wins");
		player.log.writeLine(player.getPlayerID() + " wins");
	}
	
	/**
	 * @param args
	 * main method
	 * ensures Log folder is created & deletes all previous log files
	 * creates the deck, either from input file via command line or from scratch
	 * 	NOTE: deck file must be located in same folder as .project file or have an absolute address included
	 * creates all players
	 * populates players' hands and piles with cards from deck
	 * sets players' preferred cards
	 * starts each player thread, thereby starting game play
	 */
	public static void main(String[] args) 
	{		
		//make directory to hold log files
		dir.mkdir();
		if(!dir.exists())
		{	//make sure Logs directory exists
			System.out.println("There was an error creating the Logs directory in the project directory");
		}
		logFiles = dir.listFiles();
		//Delete all previous Log Files
		for(int i = 0; i < logFiles.length; i++)
		{
			if(logFiles[i].isFile())
			{
				logFiles[i].delete();
			}			
			if(logFiles[i].isDirectory())
			{
				logFiles[i].delete();
			}
		}
		
		//populate the deck
		if(args.length > 0)
		{
			try
			{
				File file = new File(args[0]);
				Scanner fileIn = new Scanner(file);

				while(fileIn.hasNextInt())
				{
					deck.add(fileIn.nextInt());
				}
			}
			catch(FileNotFoundException e)
			{
				System.out.println("File not found. Either file name is incorrect or the file is not in the location of the program");
				return;
			}
			if(deck.size() != deckSize)
			{
				System.out.println("File does not contain enough cards. There should be " + deckSize);
				return;
			}
		}
		else
		{	//populate normal deck, handSize number of each card.  
			//card numbers range from 0 to cardsPerPlayer (numPreferredCards per player)
			for(int i = 0; i < cardsPerPlayer; i++)
			{	//certain range of cards per player
				for(int j = 0; j < dupsToWin; j++)
				{	//have multiples of every card based on their hand size
					deck.add(i);
				}
			}
		}
		
		//Instantiate Players
		for(int i = 0; i < numPlayers; i++)
		{
			try
			{	//instantiating a player also creates log files, so need to catch IOExceptions
				players[i] = new Player("Player " + (i + 1), players);
			}
			catch(IOException e)
			{
				System.out.println(e.getMessage());
				return;
			}
			playersTurns[i] = false;
		}
		
		//Populate Players Hands
		for(int i = 0; i < handSize; i++)
		{
			for(int j = 0; j < numPlayers; j++)
			{
				Random randomDeal = new Random();
				int cardToDeal = randomDeal.nextInt(deck.size());
				players[j].handDraw(deck.get(cardToDeal));
				deck.remove(cardToDeal);
			}
		}
		
		//populate Players Piles
		for(int i = 0; i < deck.size(); i++)
		{
			for(int j = 0; j < numPlayers; j++)
			{
				Random randomDeal = new Random();
				int cardToDeal = randomDeal.nextInt(deck.size());
				players[j].pileInsert(deck.get(cardToDeal));
				deck.remove(cardToDeal);
			}
		}
		
		//Set players preferred cards
		for(int i = 0; i < cardsPerPlayer; i++)
		{
			players[i/numPreferredCards].setPrefCard(i);;
		}
		
		playersTurns[0] = true;
		for(int i = 0; i < numPlayers; i++)
		{
			threads[i] = new Thread(players[i]);
			threads[i].start();
		}		
	}
}

