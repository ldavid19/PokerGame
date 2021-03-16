public class HumanStrategy implements Strategy
{
  private Card[] hHand;
  private int hSeat;
  private int handCat;
  private TableView table;
  private TextDisplay disp;
  public HumanStrategy()
  {
  }
  public void setDisplay(TextDisplay d)
  {
    disp=d;
  }
  public void deal(int seat, int handCategory, Card[] hand, TableView tableView)
  {
    hHand=hand;
    hSeat=seat;
    handCat=handCategory;
    table=tableView;
  }
  public int act()
  {
    return disp.act(hHand);
  }
  public boolean[] exchange()
  {
    return disp.exchange(hHand);
  }
  public void exchanged(int handCategory, Card[] hand)
  {
    handCat=handCategory;
    hHand=hand;
  }
  public void roundEnded()
  {
  }

  public void playerActed(int seat, int action)
  {
  }
  
  public void playerExchanged(int seat, int numCardsExchanged)
  {
  }
}