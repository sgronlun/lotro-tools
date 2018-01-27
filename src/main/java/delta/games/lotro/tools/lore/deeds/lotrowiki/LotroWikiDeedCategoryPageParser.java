package delta.games.lotro.tools.lore.deeds.lotrowiki;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.Source;

import org.apache.log4j.Logger;

import delta.games.lotro.common.CharacterClass;
import delta.games.lotro.common.Race;
import delta.games.lotro.lore.deeds.DeedDescription;
import delta.games.lotro.lore.deeds.DeedType;
import delta.games.lotro.tools.lore.deeds.DeedsContainer;
import delta.games.lotro.tools.utils.JerichoHtmlUtils;
import delta.games.lotro.utils.Escapes;

/**
 * Parse for lotro-wiki deed category pages.
 * @author DAM
 */
public class LotroWikiDeedCategoryPageParser
{
  private static final Logger _logger=Logger.getLogger(LotroWikiDeedCategoryPageParser.class);

  private static final String INDEX="/index.php/";

  private LotroWikiSiteInterface _lotroWiki;

  private HashSet<String> _deedIds;

  /**
   * Constructor.
   * @param lotroWiki Lotro-wiki interface.
   */
  public LotroWikiDeedCategoryPageParser(LotroWikiSiteInterface lotroWiki)
  {
    _lotroWiki=lotroWiki;
    _deedIds=new HashSet<String>();
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   * @param type Deed type to set, if not <code>null</code>.
   * @param category Category to set, if not <code>null</code>.
   * @return a list of loaded deeds.
   */
  public List<DeedDescription> doCategory(String categoryId, DeedType type, String category)
  {
    List<DeedDescription> deeds=doCategory(categoryId);
    if (category!=null)
    {
      if (category.endsWith(":")) category=category.substring(0,category.length()-1);
    }
    for(DeedDescription deed : deeds)
    {
      if (type!=null) deed.setType(type);
      if (category!=null) deed.setCategory(category);
    }
    writeFile(categoryId,deeds);
    return deeds;
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   * @param type Deed type to set, if not <code>null</code>.
   * @param characterClass Character class to set, if not <code>null</code>.
   * @return a list of loaded deeds.
   */
  public List<DeedDescription> doCategory(String categoryId, DeedType type, CharacterClass characterClass)
  {
    List<DeedDescription> deeds=doCategory(categoryId);
    for(DeedDescription deed : deeds)
    {
      if (type!=null) deed.setType(type);
      if (characterClass!=null) deed.setCategory("Class:"+characterClass.getKey());
    }
    writeFile(categoryId,deeds);
    return deeds;
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   * @param type Deed type to set, if not <code>null</code>.
   * @param race Race to set, if not <code>null</code>.
   * @return a list of loaded deeds.
   */
  public List<DeedDescription> doCategory(String categoryId, DeedType type, Race race)
  {
    List<DeedDescription> deeds=doCategory(categoryId);
    for(DeedDescription deed : deeds)
    {
      if (race!=null) deed.setCategory("Race:"+race.getLabel());
    }
    writeFile(categoryId,deeds);
    return deeds;
  }

  /**
   * Handle a deed category.
   * @param categoryId Category identifier.
   * @return a list of loaded deeds.
   */
  public List<DeedDescription> doCategory(String categoryId)
  {
    String url=LotroWikiConstants.BASE_URL+"/index.php/Category:"+Escapes.escapeUrl(categoryId);
    String file=categoryId+"/main.html";
    File deedsCategoryFile=_lotroWiki.download(url,Escapes.escapeFile(file));
    List<String> deedIds=parseDeedCategoryPage(deedsCategoryFile);
    List<DeedDescription> deeds=loadDeeds(categoryId,deedIds);
    writeFile(categoryId,deeds);
    return deeds;
  }

  private void writeFile(String categoryId,List<DeedDescription> deeds)
  {
    File to=new File("deeds-"+Escapes.escapeFile(categoryId)+".xml").getAbsoluteFile();
    DeedsContainer.writeSortedDeeds(deeds,to);
    //DeedXMLWriter.writeDeedsFile(to,deeds);
  }

  /**
   * Parse a lotro-wiki deed category page.
   * @param from Source page.
   * @return loaded deed IDs.
   */
  public List<String> parseDeedCategoryPage(File from)
  {
    List<String> deedIds=new ArrayList<String>();
    try
    {
      FileInputStream inputStream=new FileInputStream(from);
      Source source=new Source(inputStream);
      parseTables(source,deedIds);
      parseIndex(source,deedIds);
    }
    catch(Exception e)
    {
      _logger.error("Cannot parse deed category page ["+from+"]",e);
    }
    return deedIds;
  }

  private void parseTables(Source source, List<String> deedIds)
  {
    List<Element> tables=JerichoHtmlUtils.findElementsByTagName(source,HTMLElementName.TABLE);
    for(Element table : tables)
    {
      boolean ok=checkTable(table);
      if (!ok)
      {
        continue;
      }
      List<Element> rows=JerichoHtmlUtils.findElementsByTagName(table,HTMLElementName.TR);
      rows.remove(0);
      for(Element row : rows)
      {
        String deedId=handleRow(row);
        if (deedId!=null)
        {
          deedIds.add(deedId);
        }
      }
    }
  }

  private void parseIndex(Source source, List<String> deedIds)
  {
    Element indexSection=JerichoHtmlUtils.findElementByTagNameAndAttributeValue(source,HTMLElementName.DIV,"id","mw-pages");
    if (indexSection!=null)
    {
      List<Element> anchors=JerichoHtmlUtils.findElementsByTagName(indexSection,HTMLElementName.A);
      for(Element anchor : anchors)
      {
        //String title=anchor.getAttributeValue("title");
        String href=anchor.getAttributeValue("href");
        //System.out.println(href + "  ==>  "+title);
        if (href.startsWith(INDEX))
        {
          String deedId=href.substring(INDEX.length());
          deedIds.add(deedId);
        }
      }
    }
  }

  private List<DeedDescription> loadDeeds(String categoryId, List<String> deedIds)
  {
    List<DeedDescription> deeds=new ArrayList<DeedDescription>();
    Set<String> deedKeys=new HashSet<String>();
    LotroWikiDeedPageParser parser=new LotroWikiDeedPageParser();
    int index=0;
    for(String deedId : deedIds)
    {
      String url=LotroWikiConstants.BASE_URL+"/index.php?title="+deedId+"&action=edit";
      String name=Escapes.escapeFile(categoryId)+"/deed"+index+".html";
      File deedFile=_lotroWiki.download(url,name);
      DeedDescription deed=parser.parseDeed(deedFile);
      if (deed!=null)
      {
        boolean alreadyKnown=_deedIds.contains(deedId);
        if (!alreadyKnown)
        {
          deed.setKey(deedId);
          if (!deedKeys.contains(deedId))
          {
            deeds.add(deed);
            deedKeys.add(deedId);
          }
          _deedIds.add(deedId);
          System.out.println(deed);
        }
      }
      index++;
    }
    return deeds;
  }

  private boolean checkTable(Element table)
  {
    List<Element> rows=JerichoHtmlUtils.findElementsByTagName(table,HTMLElementName.TR);
    if (rows.size()>=1)
    {
      Element header=rows.get(0);
      List<Element> cells=JerichoHtmlUtils.findElementsByTagName(header,HTMLElementName.TH);
      if (cells.size()>=1)
      {
        String text=JerichoHtmlUtils.getTextFromTag(cells.get(0));
        return text.contains("Deed");
      }
    }
    return false;
  }

  private String handleRow(Element row)
  {
    String deedId=null;
    List<Element> cells=JerichoHtmlUtils.findElementsByTagName(row,HTMLElementName.TD);
    if (cells.size()>=1)
    {
      Element deedCell=cells.get(0);
      Element anchor=JerichoHtmlUtils.findElementByTagName(deedCell,HTMLElementName.A);
      if (anchor!=null)
      {
        //String title=anchor.getAttributeValue("title");
        String href=anchor.getAttributeValue("href");
        //System.out.println(href + "  ==>  "+title);
        if (href.startsWith(INDEX))
        {
          deedId=href.substring(INDEX.length());
        }
      }
    }
    return deedId;
  }
}
