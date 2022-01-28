package delta.games.lotro.tools.dat.items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.items.Item;

/**
 * Loader for cosmetic details for items.
 * @author DAM
 */
public class CosmeticLoader
{
  private Map<String,List<Item>> _map=new HashMap<String,List<Item>>();

  /**
   * Handle an item.
   * @param item Item to use.
   * @param props Properties to use.
   */
  public void handleItem(Item item, PropertiesSet props)
  {
    Integer physObj=(Integer)props.getProperty("PhysObj");
    if (physObj==null)
    {
      return;
    }
    Object[] entryArray=(Object[])props.getProperty("Item_WornAppearanceMapList");
    if (entryArray==null)
    {
      return;
    }
    StringBuilder sb=new StringBuilder();
    for(Object entryObj : entryArray)
    {
      if (sb.length()>0)
      {
        sb.append(',');
      }
      PropertiesSet entryProps=(PropertiesSet)entryObj;
      int key=((Integer)entryProps.getProperty("Item_AppearanceKey")).intValue();
      sb.append(key).append('/');
      int sex=((Integer)entryProps.getProperty("Item_SexOfWearer")).intValue();
      sb.append(sex).append('/');
      int species=((Integer)entryProps.getProperty("Item_SpeciesOfWearer")).intValue();
      sb.append(species).append('/');
      int wornAppearance=((Integer)entryProps.getProperty("Item_WornAppearance")).intValue();
      sb.append(wornAppearance);
    }
    String appearanceHash=sb.toString();
    //System.out.println(physObj+" => "+appearanceHash+" => "+item);
    List<Item> list=_map.get(appearanceHash);
    if (list==null)
    {
      list=new ArrayList<Item>();
      _map.put(appearanceHash,list);
    }
    list.add(item);
  }

  /**
   * Dump the loaded data.
   */
  public void dump()
  {
    List<String> keys=new ArrayList<String>(_map.keySet());
    System.out.println(_map.size());
    Collections.sort(keys);
    for(String key : keys)
    {
      System.out.println("Key: "+key);
      for(Item item : _map.get(key))
      {
        System.out.println("\t"+item);
      }
    }
  }
}
