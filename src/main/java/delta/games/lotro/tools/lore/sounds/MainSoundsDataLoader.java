package delta.games.lotro.tools.lore.sounds;

import org.apache.log4j.Logger;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.dat.data.script.ScriptsTable;
import delta.games.lotro.dat.loaders.script.ScriptTableLoader;

/**
 * Loader for sounds data.
 * @author DAM
 */
public class MainSoundsDataLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainSoundsDataLoader.class);

  private DataFacade _facade;
  private EnumMapper _channel;

  private MainSoundsDataLoader()
  {
    _facade=new DataFacade();
    _channel=_facade.getEnumsManager().getEnumMapper(587203405);
  }

  private ScriptsTable loadScript(int id)
  {
    byte[] scriptTableData=_facade.loadData(id);
    ScriptsTable ret=null;
    ScriptTableLoader loader=new ScriptTableLoader(_facade);
    try
    {
      ret=loader.decode(scriptTableData);
    }
    catch(Exception e)
    {
      LOGGER.warn("Decoding error for script ID="+id,e);
    }
    return ret;
  }

  private void doIt()
  {
    int[] ids= { 0x7000009, 0x700042E, 0x070017f8,
        0x700042F, // Notes
        0x700000D, 0x700001C, 0x7000008, 0x7000000
    };
    ScriptsInspectorForSounds inspector=new ScriptsInspectorForSounds(_channel);
    for(int id : ids)
    {
      ScriptsTable table=loadScript(id);
      inspector.inspect(table);
    }
    SoundsDataAggregator aggregator=inspector.getAggregator();
    aggregator.dump();
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainSoundsDataLoader().doIt();
  }
}
