package delta.games.lotro.tools.dat.quests;

import delta.games.lotro.dat.data.DataFacade;
import delta.games.lotro.dat.data.PropertiesSet;
import delta.games.lotro.dat.data.enums.EnumMapper;
import delta.games.lotro.lore.quests.QuestDescription;
import delta.games.lotro.tools.dat.utils.DatUtils;

/**
 * Loader for roles data from DAT files.
 * @author DAM
 */
public class DatRolesLoader
{
  private DataFacade _facade;
  private EnumMapper _questRoleAction;

  /**
   * Constructor.
   * @param facade Data facade.
   */
  public DatRolesLoader(DataFacade facade)
  {
    _facade=facade;
    _questRoleAction=facade.getEnumsManager().getEnumMapper(587202589);
  }

  /**
   * Load roles.
   * @param quest Quest.
   * @param properties Input properties.
   */
  public void loadRoles(QuestDescription quest, PropertiesSet properties)
  {
    handleBestowalRoles(quest,properties);
    handleGlobalRoles(quest,properties);
    privateHandleRoles(quest,properties);
  }

  private void handleBestowalRoles(QuestDescription quest, PropertiesSet properties)
  {
    // Quest_BestowalRoles
    Object[] roles=(Object[])properties.getProperty("Quest_BestowalRoles");
    if (roles!=null)
    {
      System.out.println("Roles (bestower):");
      int index=0;
      for(Object roleObj : roles)
      {
        System.out.println("Index: "+index);
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
        if (npcId!=null)
        {
          String npcName=getNpc(npcId.intValue());
          System.out.println("\tNPC: "+npcName);
        }
        String dispenserText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_TextArray",Markers.CHARACTER);
        if (dispenserText!=null)
        {
          System.out.println("\tDispenser text: "+dispenserText);
        }
        String successText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleSuccessText",Markers.CHARACTER);
        System.out.println("\tSuccess text: "+successText);
        index++;
      }
    }
  }

  void handleGlobalRoles(QuestDescription quest, PropertiesSet properties)
  {
    // Quest_GlobalRoles
    Object[] roles=(Object[])properties.getProperty("Quest_GlobalRoles");
    if (roles!=null)
    {
      System.out.println("Roles (global):");
      int index=0;
      for(Object roleObj : roles)
      {
        System.out.println("Index: "+index);
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        Integer dispenserAction=(Integer)roleProps.getProperty("QuestDispenser_Action");
        if (dispenserAction!=null)
        {
          String action=_questRoleAction.getString(dispenserAction.intValue());
          System.out.println("\tdispenserAction: " +action);
        }
        Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
        if (npcId!=null)
        {
          String npcName=getNpc(npcId.intValue());
          System.out.println("\tNPC: "+npcName);
        }
        String dispenserRoleName=(String)roleProps.getProperty("QuestDispenser_RoleName");
        if (dispenserRoleName!=null)
        {
          System.out.println("\tdispenserRolename: " +dispenserRoleName);
        }
        String dispenserRoleConstraint=(String)roleProps.getProperty("QuestDispenser_RoleConstraint");
        if (dispenserRoleConstraint!=null)
        {
          System.out.println("\tdispenserRole Constraint: " +dispenserRoleConstraint);
        }
        String dispenserText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_TextArray",Markers.CHARACTER);
        if (dispenserText!=null)
        {
          System.out.println("\tDispenser text: "+dispenserText);
        }
        String successText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleSuccessText","{***}");
        System.out.println("\tSuccess text: "+successText);
        index++;
      }
    }
  }

  private void privateHandleRoles(QuestDescription quest, PropertiesSet properties)
  {
    // Quest_RoleArray
    Object[] roles=(Object[])properties.getProperty("Quest_RoleArray");
    if (roles!=null)
    {
      System.out.println("Role3:");
      int index=0;
      for(Object roleObj : roles)
      {
        PropertiesSet roleProps=(PropertiesSet)roleObj;
        System.out.println("Index: "+index);
        // [QuestDispenser_Action, QuestDispenser_RoleSuccessText, Quest_ObjectiveIndex, QuestDispenser_NPC, QuestDispenser_RoleName]
        Integer dispenserAction=(Integer)roleProps.getProperty("QuestDispenser_Action");
        if (dispenserAction!=null)
        {
          String action=_questRoleAction.getString(dispenserAction.intValue());
          System.out.println("\tdispenserAction: " +action);
        }
        Integer objectiveIndex=(Integer)roleProps.getProperty("Quest_ObjectiveIndex");
        if (objectiveIndex!=null)
        {
          System.out.println("\tobjectiveIndex: " +objectiveIndex);
        }
        String dispenserRoleName=(String)roleProps.getProperty("QuestDispenser_RoleName");
        if (dispenserRoleName!=null)
        {
          System.out.println("\tdispenserRolename: " +dispenserRoleName);
        }
        Integer npcId=(Integer)roleProps.getProperty("QuestDispenser_NPC");
        if (npcId!=null)
        {
          String npcName=getNpc(npcId.intValue());
          System.out.println("\tNPC: "+npcName);
        }
        String successText=DatUtils.getFullStringProperty(roleProps,"QuestDispenser_RoleSuccessText",Markers.CHARACTER);
        System.out.println("\tSuccess text: "+successText);
        index++;
      }
    }
  }

  private String getNpc(int npcId)
  {
    int dbPropertiesId=npcId+0x09000000;
    PropertiesSet properties=_facade.loadProperties(dbPropertiesId);
    if (properties!=null)
    {
      String npcName=DatUtils.getStringProperty(properties,"Name");
      return npcName;
    }
    return null;
  }
}
