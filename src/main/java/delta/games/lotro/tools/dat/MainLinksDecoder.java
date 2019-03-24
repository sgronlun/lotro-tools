package delta.games.lotro.tools.dat;

import java.io.File;
import java.util.Map;

import javax.xml.transform.sax.TransformerHandler;

import org.apache.log4j.Logger;

import delta.common.utils.io.FileIO;
import delta.common.utils.io.xml.XmlFileWriterHelper;
import delta.common.utils.io.xml.XmlWriter;
import delta.common.utils.text.EncodingNames;
import delta.games.lotro.dat.utils.Dump;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemInstance;
import delta.games.lotro.lore.items.io.xml.ItemXMLWriter;
import delta.games.lotro.plugins.LuaParser;
import delta.games.lotro.plugins.lotrocompanion.links.ChatItemLinksDecoder;

/**
 * Parser for the main data as found in LotroCompanion plugin data.
 * @author DAM
 */
public class MainLinksDecoder
{
  private static final Logger LOGGER=Logger.getLogger(MainLinksDecoder.class);

  private ChatItemLinksDecoder _decoder;

  /**
   * Constructor.
   */
  public MainLinksDecoder()
  {
    _decoder=new ChatItemLinksDecoder();
  }

  /**
   * Parse/use data from the given file.
   * @param dataFile Input file.
   * @throws Exception If an error occurs.
   */
  public void doIt(File dataFile) throws Exception
  {
    LuaParser parser=new LuaParser();
    Map<String,Object> data=parser.read(dataFile);
    byte[] buffer=loadBuffer(data);
    File bufferFile=new File(dataFile.getParentFile(),dataFile.getName()+".bin");
    FileIO.writeFile(bufferFile,buffer);
    ItemInstance<? extends Item> instance=_decoder.decodeBuffer(buffer);
    System.out.println(instance.dump());
    File to=new File(dataFile.getParentFile(),dataFile.getName()+".xml");
    writeItemInstance(to,instance);
  }

  private void writeItemInstance(File to, final ItemInstance<? extends Item> instance)
  {
    XmlFileWriterHelper helper=new XmlFileWriterHelper();
    XmlWriter writer=new XmlWriter()
    {
      public void writeXml(TransformerHandler hd) throws Exception
      {
        ItemXMLWriter itemWriter=new ItemXMLWriter();
        itemWriter.writeItemInstance(hd,instance);
      }
    };
    helper.write(to,EncodingNames.UTF_8,writer);
  }

  @SuppressWarnings("unchecked")
  private byte[] loadBuffer(Map<String,Object> data)
  {
    byte[] buffer=null;
    Map<String,Double> rawData=(Map<String,Double>)data.get("rawData");
    if (rawData!=null)
    {
      int nb=rawData.size();
      buffer=new byte[nb];
      for(int i=0;i<nb;i++)
      {
        String key=(i+1)+".0";
        Double value=rawData.get(key);
        if (value!=null)
        {
          buffer[i]=value.byteValue();
        }
      }
      Dump.dump(buffer);
    }
    return buffer;
  }

  private static void doFile(File dataFile)
  {
    if (!dataFile.exists())
    {
      return;
    }
    try
    {
      System.out.println("File: "+dataFile);
      MainLinksDecoder parser=new MainLinksDecoder();
      parser.doIt(dataFile);
    }
    catch(Exception e)
    {
      LOGGER.error("Error when loading link from file "+dataFile, e);
    }
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    File linksDir=new File("D:\\shared\\damien\\dev\\lotrocompanion\\lua\\links");
    // Ethell
    File ethell=new File(linksDir,"ethell");
    doFile(new File(ethell,"weapon.txt")); // Imbued
    doFile(new File(ethell,"emblem.txt")); // Non imbued
    doFile(new File(ethell,"supdoomfoldtstandardofwar.txt"));
    // Lorewyne
    File lorewyne=new File(linksDir,"lorewyne");
    doFile(new File(lorewyne,"legendary book.plugindata")); // Imbued
    // Tilmo
    File tilmo=new File(linksDir,"tilmo");
    doFile(new File(tilmo,"club.txt")); // Imbued
    // Glumlug
    File glumlug=new File(linksDir,"glumlug");
    doFile(new File(glumlug,"katioucha5.txt")); // Imbued
    doFile(new File(glumlug,"axe.txt")); // Non imbued
    doFile(new File(glumlug,"oldbow.txt")); // Non imbued
    doFile(new File(glumlug,"3rdage axe.txt")); // Non imbued
    // Kargarth
    File kargarth=new File(linksDir,"kargarth");
    doFile(new File(kargarth,"belt.txt")); // Non imbued
    // Utharr
    File utharr=new File(linksDir,"utharr");
    doFile(new File(utharr,"satchel.txt")); // Non imbued
    // Beleganth
    File beleganth=new File(linksDir,"beleganth");
    doFile(new File(beleganth,"weapon.txt")); // Non imbued
    // Meva
    File meva=new File(linksDir,"meva");
    doFile(new File(meva,"EarringOfTheWillfulDefender.txt")); // Earring, with essences
    doFile(new File(meva,"EmbossedMantleOfBardsWill.txt")); // Mantle, with essences
    doFile(new File(meva,"RunedGlovesOfBardWill.txt")); // Gloves, with essences
    doFile(new File(meva,"old book.txt")); // old book, non imbued
    // Giswald
    File giswald=new File(linksDir,"giswald");
    doFile(new File(giswald,"2h weapon.txt")); // LI weapon, non imbued
    doFile(new File(giswald,"dyed helm.txt")); // umber dyed helm
    doFile(new File(giswald,"indigo dyed gauntlets.txt")); // indigo dyed gauntlets
    doFile(new File(giswald,"helm-thorin.txt")); // scaled helm
    doFile(new File(giswald,"mathomhunter box.txt")); // Box
    doFile(new File(giswald,"orange dye.txt")); // orange dye
  }
}
