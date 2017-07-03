package delta.games.lotro.tools.lore.maps;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import delta.common.utils.misc.IntegerHolder;
import delta.games.lotro.maps.data.CategoriesManager;
import delta.games.lotro.maps.data.Category;
import delta.games.lotro.maps.data.MapBundle;
import delta.games.lotro.maps.data.MapsManager;
import delta.games.lotro.maps.data.Marker;
import delta.games.lotro.maps.data.MarkersManager;

/**
 * Maps cleaner.
 * @author DAM
 */
public class MainMapsCleaner
{
  private void doIt()
  {
    File rootDir=new File("../lotro-maps-db");
    MapsManager mapsManager=new MapsManager(rootDir);
    mapsManager.load();
    cleanEmptyCategories(mapsManager);
  }

  private void cleanEmptyCategories(MapsManager mapsManager)
  {
    HashMap<Integer,IntegerHolder> markersByCategory=new HashMap<Integer,IntegerHolder>();
    List<MapBundle> mapBundles=mapsManager.getMaps();
    for(MapBundle mapBundle : mapBundles)
    {
      MarkersManager markersManager=mapBundle.getData();
      List<Marker> markers=markersManager.getAllMarkers();
      for(Marker marker : markers)
      {
        Category category=marker.getCategory();
        if (category!=null)
        {
          Integer code=Integer.valueOf(category.getCode());
          IntegerHolder counter=markersByCategory.get(code);
          if (counter==null)
          {
            counter=new IntegerHolder();
            markersByCategory.put(code,counter);
          }
          counter.increment();
        }
      }
    }
    CategoriesManager categoriesManager=mapsManager.getCategories();
    List<Integer> sortedCodes=new ArrayList<Integer>(markersByCategory.keySet());
    Collections.sort(sortedCodes);
    for(Integer code : sortedCodes)
    {
      IntegerHolder counter=markersByCategory.get(code);
      Category category=categoriesManager.getByCode(code.intValue());
      System.out.println(category.getLabel()+": "+counter);
    }
    // Prepare cleanup
    HashSet<Integer> codes2Remove=new HashSet<Integer>();
    List<Category> categories=categoriesManager.getAllSortedByCode();
    for(Category category : categories)
    {
      Integer key=Integer.valueOf(category.getCode());
      if (!sortedCodes.contains(key))
      {
        codes2Remove.add(key);
      }
    }
    // Perform cleanup
    for(Integer code : codes2Remove)
    {
      System.out.println("Removing category: "+categoriesManager.getByCode(code.intValue()).getLabel());
      categoriesManager.removeCategory(code.intValue());
    }
    mapsManager.saveCategories();
  }

  /**
   * Main method for this test.
   * @param args Not used.
   */
  public static void main(String[] args)
  {
    new MainMapsCleaner().doIt();
  }
}
