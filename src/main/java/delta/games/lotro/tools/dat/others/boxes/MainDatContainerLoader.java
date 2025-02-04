package delta.games.lotro.tools.dat.others.boxes;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import delta.games.lotro.common.treasure.FilteredTrophyTable;
import delta.games.lotro.common.treasure.LootsManager;
import delta.games.lotro.common.treasure.RelicsList;
import delta.games.lotro.common.treasure.TreasureList;
import delta.games.lotro.common.treasure.TrophyList;
import delta.games.lotro.common.treasure.WeightedTreasureTable;
import delta.games.lotro.common.treasure.io.xml.TreasureXMLWriter;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Container;
import delta.games.lotro.lore.items.Item;
import delta.games.lotro.lore.items.ItemsManager;
import delta.games.lotro.lore.items.containers.ItemsContainer;
import delta.games.lotro.lore.items.containers.LootTables;
import delta.games.lotro.lore.items.containers.LootType;
import delta.games.lotro.lore.items.io.xml.ContainerXMLWriter;
import delta.games.lotro.lore.items.legendary.relics.Relic;
import delta.games.lotro.lore.items.legendary.relics.RelicsContainer;
import delta.games.lotro.lore.items.legendary.relics.RelicsManager;
import delta.games.lotro.tools.dat.GeneratedFiles;
import delta.games.lotro.tools.dat.others.LootLoader;

/**
 * Get the contents of a container (box,chest,scrolls...) from DAT files.
 * @author DAM
 */
public class MainDatContainerLoader
{
  private static final Logger LOGGER=Logger.getLogger(MainDatContainerLoader.class);

  private DataFacade _facade;
  private LootLoader _lootLoader;
  private LootsManager _loots;
  private CustomInstancesLootLoader _instancesLootLoader;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public MainDatContainerLoader(DataFacade facade)
  {
    _facade=facade;
    _loots=new LootsManager();
    _lootLoader=new LootLoader(facade,_loots);
    _instancesLootLoader=new CustomInstancesLootLoader(_facade,_lootLoader);
  }

  /**
   * Load a container.
   * @param item Item to use.
   * @return the loaded container.
   */
  public Container load(Item item)
  {
    int itemID=item.getIdentifier();
    PropertiesSet properties=_facade.loadProperties(itemID+DATConstants.DBPROPERTIES_OFFSET);
    if (properties==null)
    {
      LOGGER.warn("Could not handle container item ID="+itemID);
      return null;
    }

    //System.out.println(properties.dump());
    ItemsContainer itemsContainer=new ItemsContainer(item);
    LootTables lootTables=itemsContainer.getLootTables();
    // Filtered trophy table
    Integer filteredLootTableId=(Integer)properties.getProperty("PackageItem_FilteredTrophyTableTemplate");
    if (filteredLootTableId!=null)
    {
      FilteredTrophyTable filteredTable=_lootLoader.getFilteredTrophyTable(filteredLootTableId.intValue());
      lootTables.set(LootType.FILTERED_TROPHY_TABLE,filteredTable);
    }
    // Free-people weighted treasure table ID
    Integer freepWeightedTreasureTableId=(Integer)properties.getProperty("PackageItem_Freep_WeightedTreasureTableID");
    if ((freepWeightedTreasureTableId!=null) && (freepWeightedTreasureTableId.intValue()!=0))
    {
      WeightedTreasureTable weightedTable=_lootLoader.getWeightedTreasureTable(freepWeightedTreasureTableId.intValue());
      lootTables.set(LootType.WEIGHTED_TREASURE_TABLE,weightedTable);
    }
    // Trophy template
    Integer trophyTemplateId=(Integer)properties.getProperty("PackageItem_TrophyListTemplate");
    if ((trophyTemplateId!=null) && (trophyTemplateId.intValue()!=0))
    {
      TrophyList trophyList=_lootLoader.getTrophyList(trophyTemplateId.intValue());
      lootTables.set(LootType.TROPHY_LIST,trophyList);
    }
 
    //handleLootTables(properties,"SoftLock_",itemsContainer.getLootTables());
    handleLootTables(properties,"",itemsContainer.getLootTables());
    // Preview
    /*
    Integer preview=(Integer)properties.getProperty("PackageItem_IsPreviewable");
    if ((preview!=null) && (preview.intValue()!=0))
    {
      System.out.println("Preview:");
      Object[] previewList=(Object[])properties.getProperty("PackageItem_PreviewList");
      for(Object previewIdObj : previewList)
      {
        Integer previewId=(Integer)previewIdObj;
        Item item=ItemsManager.getInstance().getItem(previewId.intValue());
        System.out.println("\t"+item);
      }
    }
    */
    // Effects (for scrolls)
    TreasureList treasureList=handleEffects(properties);
    if (treasureList!=null)
    {
      lootTables.set(LootType.TREASURE_LIST,treasureList);
    }

    Container ret=null;
    if (lootTables.hasTables())
    {
      ret=itemsContainer;
      Integer previewable=(Integer)properties.getProperty("PackageItem_IsPreviewable");
      if ((previewable!=null) && (previewable.intValue()==1))
      {
        Object[] previewList=(Object[])properties.getProperty("PackageItem_PreviewList");
        if ((previewList==null) || (previewList.length==0))
        {
          LOGGER.warn("No or empty preview list for "+item);
        }
      }
      /*
      Integer action=(Integer)properties.getProperty("Usage_Action");
      Integer weenieType=(Integer)properties.getProperty("WeenieType");
      Integer bind2Acc=(Integer)properties.getProperty("PackageItem_BindAllItemsToAccount");
      Integer bind2Char=(Integer)properties.getProperty("PackageItem_BindAllItemsToCharacter");
      Integer playerForMunging=(Integer)properties.getProperty("PackageItem_UsePlayerAsContainerForMunging");
      //System.out.println(item+"\t"+action+"\t"+weenieType+"\t"+bind2Acc+"\t"+bind2Char+"\t"+previewable+"\t"+playerForMunging);
      */
    }

    // Relics?
    RelicsContainer relicsContainer=handleRelics(item,properties);
    if (relicsContainer!=null)
    {
      if (ret!=null)
      {
        LOGGER.warn("Both containers (items+relics) for: "+itemID);
      }
      ret=relicsContainer;
    }
     /*
PackageItem_IsPreviewable: 0
PackageItem_BindAllItemsToAccount: 1
PackageItem_UsePlayerAsContainerForMunging: 1

If PackageItem_IsPreviewable: 1
=> PackageItem_PreviewList: 
  #1: 1879259202
  #2: 1879259205
  #3: 1879259200
  #4: 1879188748
     */
    return ret;
  }

  private void handleLootTables(PropertiesSet properties, String prefix, LootTables lootTables)
  {
    // Other filtered trophy table
    Integer filteredTrophyTableId=(Integer)properties.getProperty(prefix+"LootGen_FilteredTrophyTable");
    if ((filteredTrophyTableId!=null) && (filteredTrophyTableId.intValue()!=0))
    {
      FilteredTrophyTable filteredTable=_lootLoader.getFilteredTrophyTable(filteredTrophyTableId.intValue());
      lootTables.set(LootType.FILTERED_TROPHY_TABLE,filteredTable);
    }
    // Other filtered trophy table
    Integer filteredTrophyTable2Id=(Integer)properties.getProperty(prefix+"LootGen_FilteredTrophyTable2");
    if ((filteredTrophyTable2Id!=null) && (filteredTrophyTable2Id.intValue()!=0))
    {
      FilteredTrophyTable filteredTable=_lootLoader.getFilteredTrophyTable(filteredTrophyTable2Id.intValue());
      lootTables.set(LootType.FILTERED_TROPHY_TABLE2,filteredTable);
    }
    // Other filtered trophy table
    Integer filteredTrophyTable3Id=(Integer)properties.getProperty(prefix+"LootGen_FilteredTrophyTable3");
    if ((filteredTrophyTable3Id!=null) && (filteredTrophyTable3Id.intValue()!=0))
    {
      FilteredTrophyTable filteredTable=_lootLoader.getFilteredTrophyTable(filteredTrophyTable3Id.intValue());
      lootTables.set(LootType.FILTERED_TROPHY_TABLE3,filteredTable);
    }
    // Trophy list override
    Integer trophyListOverrideId=(Integer)properties.getProperty(prefix+"LootGen_TrophyList_Override");
    if ((trophyListOverrideId!=null) && (trophyListOverrideId.intValue()!=0))
    {
      TrophyList trophyList=_lootLoader.getTrophyList(trophyListOverrideId.intValue());
      lootTables.set(LootType.TROPHY_LIST,trophyList);
    }
    // Barter trophy list
    Integer barterTrophyListId=(Integer)properties.getProperty(prefix+"LootGen_BarterTrophyList");
    if ((barterTrophyListId!=null) && (barterTrophyListId.intValue()!=0))
    {
      TrophyList barterTrophyList=_lootLoader.getTrophyList(barterTrophyListId.intValue());
      lootTables.set(LootType.BARTER_TROPHY_LIST,barterTrophyList);
    }
    // Treasure list override
    Integer treasureListOverrideId=(Integer)properties.getProperty("LootGen_TreasureList_Override");
    if ((treasureListOverrideId!=null) && (treasureListOverrideId.intValue()!=0))
    {
      TreasureList treasureList=_lootLoader.getTreasureList(treasureListOverrideId.intValue());
      lootTables.set(LootType.TREASURE_LIST,treasureList);
    }
    // Reputation trophy
    Integer reputationTrophyList=(Integer)properties.getProperty("LootGen_ReputationTrophyList");
    if ((reputationTrophyList!=null) && (reputationTrophyList.intValue()!=0))
    {
      // Never happens
      LOGGER.warn("Reputation trophy - should never happen");
      _lootLoader.getTrophyList(reputationTrophyList.intValue());
    }
    // Custom skirmish loot lookup table
    Integer customSkirmishLootLookupTableId=(Integer)properties.getProperty("LootGen_CustomSkirmishLootLookupTable");
    if ((customSkirmishLootLookupTableId!=null) && (customSkirmishLootLookupTableId.intValue()!=0))
    {
      _instancesLootLoader.handleCustomSkirmishLootLookupTable(customSkirmishLootLookupTableId.intValue());
      lootTables.setCustomSkirmishLootTableId(customSkirmishLootLookupTableId);
    }
  }

  private TreasureList handleEffects(PropertiesSet properties)
  {
    TreasureList ret=null;
    Object[] effects=(Object[])properties.getProperty("EffectGenerator_UsageEffectList");
    if (effects!=null)
    {
      for(Object effectObj : effects)
      {
        PropertiesSet effectItemProps=(PropertiesSet)effectObj;
        Integer effectId=(Integer)effectItemProps.getProperty("EffectGenerator_EffectID");
        if (effectId!=null)
        {
          ret=handleEffect(effectId.intValue());
        }
      }
    }
    return ret;
  }

  private TreasureList handleEffect(int effectId)
  {
    TreasureList ret=null;
    PropertiesSet effectProps=_facade.loadProperties(effectId+DATConstants.DBPROPERTIES_OFFSET);
    Integer treasureListTemplateId=(Integer)effectProps.getProperty("Effect_Lootgen_DiscoveryTreasureListTemplate");
    if (treasureListTemplateId!=null)
    {
      ret=_lootLoader.getTreasureList(treasureListTemplateId.intValue());
    }
    return ret;
  }

  private RelicsContainer handleRelics(Item item, PropertiesSet properties)
  {
    RelicsContainer ret=null;
    Integer relicId=(Integer)properties.getProperty("ItemAdvancement_RunicToCreate");
    if (relicId!=null)
    {
      RelicsManager relicsMgr=RelicsManager.getInstance();
      Relic relic=relicsMgr.getById(relicId.intValue());
      if (relic!=null)
      {
        ret=new RelicsContainer(item);
        ret.setRelic(relic);
      }
      else
      {
        LOGGER.warn("Relic not found: "+relicId);
      }
    }

    Integer runicLootListId=(Integer)properties.getProperty("ItemAdvancement_RunicLootListOverride");
    if (runicLootListId!=null)
    {
      RelicsList list=_lootLoader.getRelicsList(runicLootListId.intValue());
      //System.out.println(list);
      ret=new RelicsContainer(item);
      ret.setRelicsList(list);
    }
    return ret;
  }

  /**
   * Load containers.
   */
  public void doIt()
  {
    List<Container> containers=new ArrayList<Container>();
    ItemsManager itemsMgr=ItemsManager.getInstance();
    for(Item item : itemsMgr.getAllItems())
    {
      Container container=load(item);
      if (container!=null)
      {
        containers.add(container);
      }
    }
    // Dump some stats
    _loots.dump();
    // Write loot data
    TreasureXMLWriter.writeLootsFile(GeneratedFiles.LOOTS,_loots);
    // Write container data
    ContainerXMLWriter.writeContainersFile(GeneratedFiles.CONTAINERS,containers);
    // Write custom instance loots
    _instancesLootLoader.writeData();
    // Test samples:
    /*
    // Battle Gift Box
    load(1879303552);
    // Cosmetic Gift Box
    load(1879303553);
    // Ancient Riddermark Scroll Case III
    load(1879265139);
    // Coffer of Adventurer's Armour - Heavy (Incomparable: 1879378494)
    load(1879378494);
    // Coffer of Adventurer's Jewellery - Might (Incomparable: 1879378473)
    load(1879378473);
    */
  }

  /**
   * Main method for this tool.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    DataFacade facade=new DataFacade();
    new MainDatContainerLoader(facade).doIt();
    facade.dispose();
  }
}
