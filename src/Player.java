import java.util.*;
import java.io.IOException;

/**
 * @author william spaeth
 * @version 1.0.0.0
 * class to define a player and their possible actions
 * Is a runnable class to be connected to a thread so many players can play simultaneously
 *
 */
public class Player implements Runnable {
	
	private ArrayList<Integer> hand;
	private ArrayList<Integer> pile;
	private ArrayList<Integer> prefCards;
	private String playerID;
	public boolean win;
	Player[] players;
	Log log;
	
	
	/**
	 * @param playerID
	 * @param players
	 * @throws IOException
	 * Constructor method for a player class. Initializes all fields necessary to run the player
	 */
	public Player(String playerID, Player[] players) throws IOException
	{
		this.hand = new ArrayList<Integer>();
		this.pile = new ArrayList<Integer>();
		this.prefCards = new ArrayList<Integer>();
		this.playerID = playerID;
		this.players = players;
		this.win = false;
		log = new Log(this.playerID);
	}
	
	/**
	 * @param prefCard
	 * method to add Preferred cards to the players Preferred Cards list
	 */
	public void setPrefCard(int prefCard) {prefCards.add(prefCard);}

	/**
	 * @return playerID
	 * mothod to get a players id (name)
	 */
	public String getPlayerID() {return this.playerID;}
	
	/**
	 * @param card
	 * Method to populate the pile directly from the deck
	 */
	public void pileInsert(int card) {pile.add(card);}
	
	/**
	 * @param card
	 * @param playerID
	 * @param log
	 * Method to populate the pile when inserting to pile from a player's discard
	 */
	public void pileInsert(int card, String playerID, Log log)
	{
		this.pile.add(card);
		System.out.println(playerID + " discards " + card);
		log.writeLine(playerID + " discards " + card);
	}
	
	/**
	 * @return card
	 * method to remove a card from a player's pile.  returns removed card
	 */
	private int pileDraw()
	{
		int card = pile.get(0);
		pile.remove(0);
		return card;
	}
	
	/**
	 * @param card
	 * method to insert card into hand from deck
	 */
	public void handDraw(int card)
	{
		hand.add(card);
	}
	
	/**
	 * @param card
	 * @param playerID
	 * method to insert card into hand by drawing from a player's pile
	 */
	public void handDraw(int card, String playerID)
	{
		hand.add(card);
		System.out.println(playerID + " draws a " + card);
		
		log.writeLine(playerID + " draws a " + card);
	}
	
	/**
	 * @return card to be discarded
	 * determine which card to discard from player's hand
	 * discard non-preferred cards first, then discard preferred card with the least duplicates
	 */
	public int handDiscard()
	{
		int discard = -1;
		int discardIndex = -1;
		ArrayList<Integer> prefCardsAmt = new ArrayList<Integer>();
		
		for(int i = 0; i < prefCards.size(); i++)
		{	//pre-populating PrefCardsAmt with zeros
			prefCardsAmt.add(0);
		}
		
		for(int i = 0; i < hand.size(); i++)
		{	//Loop thru the hand
			for(int j = 0; j < prefCards.size(); j++)
			{	//Loop thru the prefCards and compare ith card in hand to jth prefCard
				if(prefCards.size() == (j + 1) && hand.get(i) != prefCards.get(j) )
				{	//looped thru all prefCards.  ith card doesn't match.  
					//set that card/index to be discarded and keep looking at rest of hand
					discard = hand.get(i);
					discardIndex = i;
				}
				else if(hand.get(i) == prefCards.get(j))
				{	//found a preferred card.  Loop again to next card
					prefCardsAmt.set(j, prefCardsAmt.get(j) + 1);
					j = prefCards.size();
				}
			}
		}
		
		if(discard == -1)
		{	//Only preferred card are in hand		
			//Must now discard preferred card with least number of duplicates
			//Example: two 5's and three 6's.  Discard the 5.
			discardIndex = -1;
			int prefCardAmt = Integer.MAX_VALUE;
			for(int i = 0; i < prefCards.size(); i++)
			{
				if(prefCardsAmt.get(i) < prefCardAmt && prefCardsAmt.get(i) > 0)
				{
					prefCardAmt = prefCardsAmt.get(i);
					discardIndex = i;
				}
			}
			
			discard = prefCards.get(discardIndex);
		}

		//Check win condition
		for(int i = 0; i < prefCards.size(); i++)
		{
			if(prefCardsAmt.get(i) >= Main.getDupsToWin())
			{
				this.win = true;
			}
		}
		
		hand.remove(hand.indexOf(discard));
		return discard;
	}
	
	/**
	 * method to print the player's hand to console and to that player's log file
	 */
	public void displayHand()
	{
		String logMsg = "";
		System.out.print(playerID + " hand is: ");
		logMsg += playerID + " hand is: ";
		for(int i = 0; i < hand.size(); i++)
		{
			System.out.print(hand.get(i) + ((i+1) >= hand.size() ? "" : ", "));
			logMsg += hand.get(i) + ((i+1) >= hand.size() ? "" : ", ");	
		}
		System.out.println();
		log.writeLine(logMsg);
	}

	
	/* (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 * the run function of this runnable class.
	 * outlines how a player plays the game
	 * checks to make sure no one has won (getKeepPlaying)
	 * checks to see whose turn it is (getPlayerTurn)
	 * if no one has won and it is players turn, then perform a player's normal turn actions
	 * if not player's turn, then repeat loop and inquire about players turn again
	 * if someone has won, then end loop normally and finish run method, ending thread.
	 */
	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(Main.getKeepPlaying())
		{
			if(Main.getPlayerTurn(this))
			{
				System.out.println();
				handDraw(pileDraw(), this.playerID);
				Main.nextPlayer(this).pileInsert(handDiscard(), this.playerID, log);
				this.displayHand();
				if(this.win)
				{
					Main.win(this);
				}
				else
				{
					Main.nextPlayerTurn(this);
				}
			}
		}
		System.out.println(this.playerID + " exits");
		log.writeLine(this.playerID + " exits");
	}
}
