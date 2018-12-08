package delta.games.lotro.tools.characters.dat;

import java.io.File;

import org.apache.log4j.Logger;

import delta.games.lotro.character.traits.TraitDescription;
import delta.games.lotro.common.stats.StatsProvider;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.tools.utils.dat.DatIconsUtils;
import delta.games.lotro.tools.utils.dat.DatStatUtils;
import delta.games.lotro.tools.utils.dat.DatUtils;

/**
 * Traits loader.
 * @author DAM
 */
public class TraitLoader
{
  private static final Logger LOGGER=Logger.getLogger(TraitLoader.class);

  /**
   * Directory for trait icons.
   */
  public static File TRAIT_ICONS_DIR=new File("data\\traits\\tmp").getAbsoluteFile();

  /**
   * Load a trait.
   * @param facade Data facade.
   * @param traitId Trait identifier.
   * @return the loaded trait description.
   */
  public static TraitDescription loadTrait(DataFacade facade, int traitId)
  {
    TraitDescription ret=null;
    PropertiesSet traitProperties=facade.loadProperties(0x9000000+traitId);
    if (traitProperties!=null)
    {
      //System.out.println("*********** Trait: "+traitId+" ****************");
      ret=new TraitDescription();
      ret.setIdentifier(traitId);
      // Name
      String traitName=DatUtils.getStringProperty(traitProperties,"Trait_Name");
      ret.setName(traitName);
      // Description
      String description=DatUtils.getStringProperty(traitProperties,"Trait_Description");
      ret.setDescription(description);
      // Icon
      int iconId=((Integer)traitProperties.getProperty("Trait_Icon")).intValue();
      ret.setIconId(iconId);
      // Min level
      int minLevel=((Integer)traitProperties.getProperty("Trait_Minimum_Level")).intValue();
      ret.setMinLevel(minLevel);
      // Tier
      //int traitTier=((Integer)traitProperties.getProperty("Trait_Tier")).intValue();
      Integer maxTier=(Integer)traitProperties.getProperty("Trait_Virtue_Maximum_Rank");
      if ((maxTier!=null) && (maxTier.intValue()>1))
      {
        ret.setTiersCount(maxTier.intValue());
      }
      //System.out.println("Trait name: "+traitName+" (min level="+minLevel+")");

      // Stats
      StatsProvider statsProvider=DatStatUtils.buildStatProviders(facade,traitProperties);
      ret.setStatsProvider(statsProvider);
      // Build icon file
      String iconFilename=iconId+".png";
      File to=new File(TRAIT_ICONS_DIR,"traitIcons/"+iconFilename).getAbsoluteFile();
      if (!to.exists())
      {
        boolean ok=DatIconsUtils.buildImageFile(facade,iconId,to);
        if (!ok)
        {
          LOGGER.warn("Could not build trait icon: "+iconFilename);
        }
      }
    }
    return ret;
  }
}
