import java.util.*;

public class Player
{
  private static Set<Integer> usedIDs = new HashSet<Integer>();
  
  private Card[] hand;  //always stored in sorted order
  private int stack;  //all the money the player has that is not currently in play
  private int bet;  //money pushed in by player during current betting round
  private Strategy strategy;
  private boolean folded;
  private int numCardsExchanged;
  private int handCategory;
  private int id;  //has a unique ID, randomly assigned
  
  public Player(Strategy strat)
  {
    hand = null;
    bet = 0;
    stack = 0;
    strategy = strat;
    folded = false;
    numCardsExchanged = -1;
    handCategory = -1;
    
    if (usedIDs.size() >= 900)
      throw new RuntimeException("all ID values used");
    
    do
      id = (int)(Math.random() * 900 + 100);
    while (usedIDs.contains(id));
    
    usedIDs.add(id);
  }
  
  public int getBet()
  {
    return bet;
  }
  
  public void fold()
  {
    folded = true;
  }
  
  public void unFold()
  {
    folded = false;
    numCardsExchanged = -1;
  }
  
  public void setBet(int newBet)
  {
    bet = newBet;
  }
  
  public Card[] getHand()
  {
    return hand;
  }
  
  public int getStack()
  {
    return stack;
  }
  
  public boolean hasFolded()
  {
    return folded;
  }
  
  public int getNumCardsExchanged()
  {
    return numCardsExchanged;
  }
  
  public int getHandCategory()
  {
    return handCategory;
  }
  
  public boolean[] exchange()
  {
    boolean[] cardsToExchange = strategy.exchange();
    if (cardsToExchange == null || cardsToExchange.length != 5)
      throw new RuntimeException("bad exchange from " + strategy);
    numCardsExchanged = 0;
    for(int i = 0; i < 5; i++)
    {
      if(cardsToExchange[i] == true)
        numCardsExchanged++;
    }
    return cardsToExchange;
  }
  
  //pre:  hand and handCategory already up to date
  public void deal(int seat, TableView tableView)
  {
    strategy.deal(seat, handCategory, hand, tableView);
  }
  
  public int act()
  {
    return strategy.act();
  }
  
  public void addChips(int chips) //adds to stack
  {
    stack = stack + chips;
  }
  
  public void removeChips(int chips) //removes from stack
  {
    stack = stack - chips;
  }
  
  public void setStack(int chips)
  {
    stack = chips;
  }
  
  public void setHand(int newHandCategory, Card[] newHand)
  {
    hand=newHand;
    handCategory = newHandCategory;
  }
  
  public int getID()
  {
    return id;
  }
  
  public String toString() //All information, including hand
  {
    return PokerUtil.handToString(hand) + " " + publicToString();
  }
  
  public String publicToString() //Just information availible to public, does not include hand
  {
    String s = id + String.format("(%3d)", stack);
    if (folded)
      s += " F";
    else
    {
      if (numCardsExchanged != -1)
        s += " exchanged=" + numCardsExchanged;
      if (bet > 0)
        s += " bet=" + bet;
    }
    return s;
  }
  
  //pre:  hand category and hand already updated after exchange
  public void exchanged()
  {
    strategy.exchanged(handCategory, hand);
  }
  
  //should never use this player object again
  public void releaseID()
  {
    usedIDs.remove(id);
  }
  
  public void roundEnded()
  {
    strategy.roundEnded();
  }
  
  public Strategy getStrategy()
  {
    return strategy;
  }
  
  public void playerActed(int seat, int action)
  {
    strategy.playerActed(seat, action);
  }
  
  public void playerExchanged(int seat, int numCardsExchanged)
  {
    strategy.playerExchanged(seat, numCardsExchanged);
  }
}
