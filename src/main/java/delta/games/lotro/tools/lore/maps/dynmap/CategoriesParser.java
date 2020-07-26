package delta.games.lotro.tools.lore.maps.dynmap;

import java.util.List;

import delta.common.utils.NumericTools;
import delta.games.lotro.maps.data.CategoriesManager;
import delta.games.lotro.maps.data.Category;

/**
 * Parser for categories definitions.
 * @author DAM
 */
public class CategoriesParser
{
  private static final String TYPES_LINE="types: {";

  /**
   * Parse categories definitions from the localization page.
   * @param lines Lines to read.
   * @return A categories manager.
   */
  public CategoriesManager parse(List<String> lines)
  {
    CategoriesManager manager=new CategoriesManager();
    if (lines==null)
    {
      return manager;
    }
    boolean enabled=false;
    for(String line : lines)
    {
      if (enabled)
      {
        //2: { en: "Point of interest", de: "Sehenswertes", fr: "Point d'interet" },
        int index=line.indexOf(':');
        if (index!=-1)
        {
          String categoryCodeStr=line.substring(0,index).trim();
          Integer categoryCode=NumericTools.parseInteger(categoryCodeStr);
          if (categoryCode!=null)
          {
            Category category=new Category(categoryCode.intValue());
            String labels=line.substring(index+1).trim();
            if (labels.endsWith(",")) labels=labels.substring(0,labels.length()-1);
            String categoryName=ParsingUtils.parseLabel(labels);
            category.setName(categoryName);
            manager.addCategory(category);
          }
        }
      }
      if (line.contains(TYPES_LINE))
      {
        enabled=true;
      }
    }
    return manager;
  }
}
