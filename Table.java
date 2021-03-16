import java.util.*;

public class Table
{
  public static final int MAX_RAISES = 4;
  
  private Player[] players;
  private int pot = 0;
  private Card[] deck = new Card[52];
  private int dealer;
  private int activeSeat;
  private int call = 0;  //the maximum amount bet by anyone
  private int deckPlace = 0;  //index of next card in deck to be dealt
  private TextDisplay display;
  private TableView tableView;
  private boolean showDown;  //true if we're in the showdown, so that strategies can see players' hands
  
  public Table(int numSeats)
  {
    players = new Player[numSeats];
    
    tableView = new TableView(this);
    
    int index = 0;
    for(int suit = 0; suit < 4; suit++)
    {
      for(int rank = 2; rank < 15; rank++)
      {
        deck[index] = new Card(rank,suit);
        index++;
      }
    }
    
    dealer = (int)(Math.random() * numSeats);
  }
  
  //Card c = new Card(int rank, int suit)
  public void shuffle()
  {
    for(int i = 0; i < 52; i++)
    {
      int index = (int)(Math.random() * (52 - i));
      Card placeHolder = deck[index];
      deck[index] = deck[51 - i];
      deck[51 - i] = placeHolder;
    }
  }
  
  public void setDisplay(TextDisplay display)
  {
    this.display = display;
  }
  
  public void round()
  {
    int numPlayers = 0;
    for (int i = 0; i < players.length; i++)
    {
      if (players[i] != null)
        numPlayers++;
    }
    
    if (numPlayers < 2)
      throw new RuntimeException("not enough players to play:  " + numPlayers);
    
    while (players[dealer] == null)
      dealer = (dealer - 1 + players.length) % players.length;
    
    ante();
    shuffle();
    deal();
    bet();
    if (getNumActivePlayers() > 1)
    {
      exchange();
      if (getNumActivePlayers() > 1)
        bet();
    }
    
    ArrayList<Integer> winningSeats = new ArrayList<Integer>();
    for (int i = 0; i < players.length; i++)
    {
      Player p = players[i];
      if(p != null && !p.hasFolded())
      {
        if (winningSeats.size() == 0)
          winningSeats.add(i);
        else
        {
          int winner = PokerUtil.getWinner(p.getHandCategory(),
                                           p.getHand(),
                                           players[winningSeats.get(0)].getHandCategory(),
                                           players[winningSeats.get(0)].getHand());
          if (winner == 1) //player's hand is better
          {
            winningSeats = new ArrayList<Integer>();
            winningSeats.add(i);
          }
          else if (winner == 0)  //tie
            winningSeats.add(i);
        }
      }
    }
    
    int winnings = pot/winningSeats.size();
    for(int i = 0; i < winningSeats.size(); i++)
      players[winningSeats.get(i)].addChips(winnings);
    pot = 0;
    
    //notify display to show results of this round
    showDown = true;
    if (display != null) display.update();
    
    //notify players that round has ended before resetting all for next round
    for (int i = 0; i < players.length; i++)
    {
      if (players[i] != null)
        players[i].roundEnded();
    }
    
    showDown = false;
    
    deckPlace = 0;
    dealer--;
    dealer = (dealer + players.length) % players.length;
    while(players[dealer] == null)
    {
      dealer--;
      dealer = (dealer + players.length) % players.length;
    }
    
    for (Player p : players)
    {
      if (p != null)
        p.unFold();
    }
  }
  
  public void ante()
  {
    for (Player p : players)
    {
      if(p != null)
      {
        p.removeChips(1);
        pot++;
      }
    }
  }
  
  public int getNumActivePlayers()
  {
    int left = 0;
    for (Player p : players)
    {
      if(p != null && !p.hasFolded())
        left++;
    }
    return left;
  }
  
  public void deal()
  {
    for (int seat = 0; seat < players.length; seat++)
    {
      if(players[seat] != null)
      {
        Card[] hand = new Card[5];
        for(int i = 0; i < 5; i++)
        {
          hand[i] = deck[deckPlace];
          deckPlace++;
        }
        int handCategory = PokerUtil.evaluateHand(hand);
        players[seat].setHand(handCategory, hand);
        players[seat].deal(seat, tableView);
      }
    }
  }
  
  public void bet()
  {
    activeSeat = dealer - 1;
    activeSeat = (activeSeat + players.length) % players.length;
    
    call = 0;  //biggest bet is 0 so far
    int called = 0;  //number of players who have called the biggest bet
    int playersLeft = getNumActivePlayers();
    while(called < playersLeft && playersLeft > 1)
    {
      Player activePlayer = players[activeSeat];
      if (activePlayer != null && !activePlayer.hasFolded())
      {
        if (display != null) display.update();
        int option = activePlayer.act();
        
        if (option == Strategy.FOLD && call == activePlayer.getBet() ||
            option == Strategy.RAISE && call == MAX_RAISES)
          option = Strategy.CALL;
        
        if(option == Strategy.FOLD)
        {
          activePlayer.fold();
          playersLeft--;
        }
        else if(option == Strategy.RAISE)
        {
          call++;
          activePlayer.removeChips(call - activePlayer.getBet());
          activePlayer.setBet(call);
          called = 1;
        }
        else if(option == Strategy.CALL)
        {
          activePlayer.removeChips(call - activePlayer.getBet());
          activePlayer.setBet(call);
          called++;
        }
        else
          throw new RuntimeException("illegal action " + option + " returned by seat " + activeSeat);
        
        for (Player p : players)
        {
          if (p != null)
            p.playerActed(activeSeat, option);
        }
        
      }
      
      activeSeat = activeSeat - 1;
      activeSeat = (activeSeat + players.length) % players.length;
    }
    
    if (display != null) display.update();
    
    for (Player p : players)
    {
      if(p != null)
      {
        pot += p.getBet();
        p.setBet(0);
      }
    }
  }
  
  public void exchange()
  {
    activeSeat = dealer - 1;
    activeSeat = (activeSeat + players.length) % players.length;
    
    for (int i = 0; i < players.length; i++)
    {
      Player activePlayer = players[activeSeat];
      if(activePlayer != null && !activePlayer.hasFolded())
      {
        if (display != null) display.update();
        boolean[] toExchange = activePlayer.exchange();
        Card[] hand = activePlayer.getHand();
        for(int f = 0; f < 5; f++)
        {
          if(toExchange[f] == true)
          {
            hand[f] = deck[deckPlace];
            deckPlace++;
          }
        }
        int handCategory = PokerUtil.evaluateHand(hand);
        activePlayer.setHand(handCategory, hand);
        activePlayer.exchanged();
        
        for (Player p : players)
        {
          if (p != null)
            p.playerExchanged(activeSeat, activePlayer.getNumCardsExchanged());
        }
      }
      activeSeat--;
      activeSeat = (activeSeat + players.length) % players.length;
    }
  }
  
  public void setPlayer(int seat, Player player)
  {
    players[seat] = player;
  }
  
  public int getActiveSeat()
  {
    return activeSeat;
  }
  
  public int getPot()
  {
    return pot;
  }
  
  public int getDealer()
  {
    return dealer;
  }
  
  public int getCall()
  {
    return call;
  }
  
  public int getSize()
  {
    return players.length;
  }
  
  public Player getPlayer(int seat)
  {
    return players[seat];
  }
  
  public String toString()
  {
    String s = "pot:  " + pot + "\n";
    for (int seat = players.length - 1; seat >= 0; seat--)
    {
      if (players[seat] != null)
      {
        if (seat == dealer)
          s += "D";
        else
          s += " ";
        if (seat == activeSeat)
          s += "*";
        else 
          s += " ";
        s += seat + ":  ";
        s += players[seat];
        s += "\n";
      }
    }
    return s;
  }
  
  public TableView getTableView()
  {
    return tableView;
  }
  
  public boolean showDown()
  {
    return showDown;
  }
}
