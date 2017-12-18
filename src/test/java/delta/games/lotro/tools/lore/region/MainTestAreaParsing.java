package delta.games.lotro.tools.lore.region;

import delta.games.lotro.lore.region.Area;

/**
 * Test for area description parsing.
 * @author DAM
 */
public class MainTestAreaParsing
{
  /**
   * Basic main method for test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    AreaPageParser parser=new AreaPageParser();
    Area area=parser.parseAreaPage("Bindbole_Wood");
    if (area!=null)
    {
      System.out.println(area.dump());
    }
  }
}
