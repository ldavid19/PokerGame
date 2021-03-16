import java.util.*;

public class Main
{
  public static void main(String[] args)
  {
    Table t = new Table(4);
    HumanStrategy human = new HumanStrategy();
    t.setPlayer(0, new Player(human));
    for (int seat = 1; seat < 3; seat++)
      t.setPlayer(seat, new Player(new RandomStrategy()));
    TextDisplay d = new TextDisplay(t, true);
    t.setDisplay(d);
    human.setDisplay(d);
    t.round();
  }
}