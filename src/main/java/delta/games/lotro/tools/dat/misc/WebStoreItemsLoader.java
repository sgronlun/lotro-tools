package delta.games.lotro.tools.dat.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import delta.games.lotro.common.IdentifiableComparator;
import delta.games.lotro.common.enums.BillingGroup;
import delta.games.lotro.common.enums.LotroEnum;
import delta.games.lotro.common.enums.LotroEnumsRegistry;
import delta.games.lotro.dat.DATConstants;
import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.lore.webStore.WebStoreItem;

/**
 * Loader for web store items.
 * @author DAM
 */
public class WebStoreItemsLoader
{
  private DataFacade _facade;
  private Map<Integer,WebStoreItem> _registry;
  private LotroEnum<BillingGroup> _billingGroups;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public WebStoreItemsLoader(DataFacade facade)
  {
    _facade=facade;
    _registry=new HashMap<Integer,WebStoreItem>();
    _billingGroups=LotroEnumsRegistry.getInstance().get(BillingGroup.class);
  }

  /**
   * Get the managed web store items.
   * @return a list of web store items, sorted by identifier.
   */
  public List<WebStoreItem> getWebStoreItems()
  {
    List<WebStoreItem> ret=new ArrayList<WebStoreItem>();
    ret.addAll(_registry.values());
    Collections.sort(ret,new IdentifiableComparator<WebStoreItem>());
    return ret;
  }

  /**
   * Get a web store item (load it if necessary).
   * @param webStoreItemID Identifier of the item to get.
   * @return A web store item or <code>null</code> if not found.
   */
  public WebStoreItem getWebStoreItem(int webStoreItemID)
  {
    if (webStoreItemID==1879184983)
    {
      // Free!
      return null;
    }
    Integer key=Integer.valueOf(webStoreItemID);
    if (_registry.containsKey(key))
    {
      return _registry.get(key);
    }
    WebStoreItem ret=handleWebStoreItem(webStoreItemID);
    _registry.put(key,ret);
    return ret;
  }

  private WebStoreItem handleWebStoreItem(int webStoreItemID)
  {
    PropertiesSet props=_facade.loadProperties(webStoreItemID+DATConstants.DBPROPERTIES_OFFSET);
    //System.out.println(props.dump());
    WebStoreItem ret=new WebStoreItem(webStoreItemID);
    // Name
    String name=(String)props.getProperty("WebStoreItem_Name");
    ret.setName(name);
    // SKU
    String sku=(String)props.getProperty("WebStoreItem_SKU");
    ret.setSku(sku);
    // Billing Token
    Integer billingTokenCode=(Integer)props.getProperty("WebStoreAccountItem_BillingToken");
    if (billingTokenCode!=null)
    {
      BillingGroup billingGroup=_billingGroups.getEntry(billingTokenCode.intValue());
      ret.setBillingToken(billingGroup);
    }
    // Free for subscribers
    Integer freeForSubscribersInt=(Integer)props.getProperty("WebStoreAccountItem_IsFreeForSubscribers");
    boolean freeForSubscribers=(freeForSubscribersInt!=null)?(freeForSubscribersInt.intValue()>0):false;
    ret.setFreeForSubscribers(freeForSubscribers);
    /*
    WebStoreAccountItem_BillingToken: 629 (WSWILDW)
    WebStoreAccountItem_IsFreeForSubscribers: 1
    WebStoreItem_BuyNowButtonTooltip: Buy the Wildwood
    WebStoreItem_BuyNowButtonTooltip_Disabled: Offer not available at this time
    WebStoreItem_Name: Wildwood Region
    WebStoreItem_SKU: RegionWildwood
    */
    return ret;
  }
}
