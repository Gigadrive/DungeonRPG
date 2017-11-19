package net.wrathofdungeons.dungeonrpg.cmd;

import com.sun.org.apache.regexp.internal.RE;
import net.wrathofdungeons.dungeonapi.DungeonAPI;
import net.wrathofdungeons.dungeonapi.MySQLManager;
import net.wrathofdungeons.dungeonapi.cmd.manager.Command;
import net.wrathofdungeons.dungeonapi.user.Rank;
import net.wrathofdungeons.dungeonapi.util.Util;
import net.wrathofdungeons.dungeonrpg.DungeonRPG;
import net.wrathofdungeons.dungeonrpg.inv.CraftingMenu;
import net.wrathofdungeons.dungeonrpg.items.CustomItem;
import net.wrathofdungeons.dungeonrpg.items.StoredCustomItem;
import net.wrathofdungeons.dungeonrpg.professions.CraftingRecipe;
import net.wrathofdungeons.dungeonrpg.user.GameUser;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

public class ModifyRecipeCommand extends Command {
    public ModifyRecipeCommand(){
        super("recipe", Rank.ADMIN);
    }

    void sendUsage(Player p, String label){
        p.sendMessage(ChatColor.RED + "/" + label + " create");
        p.sendMessage(ChatColor.RED + "/" + label + " list");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> changeresult");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> info");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> removeneededitem <Index>");
        p.sendMessage(ChatColor.RED + "/" + label + " <ID> addneededitem");
    }

    @Override
    public void execute(Player p, String label, String[] args) {
        GameUser u = GameUser.getUser(p);

        if(!DungeonRPG.isTestServer()){
            p.sendMessage(ChatColor.RED + "That command is not available on this server.");
            return;
        }

        if(args.length == 1){
            if(args[0].equalsIgnoreCase("create")){
                DungeonAPI.async(() -> {
                    try {
                        int id = -1;

                        PreparedStatement ps = MySQLManager.getInstance().getConnection().prepareStatement("INSERT INTO `craftingRecipes` (`addedBy`) VALUES(?);", Statement.RETURN_GENERATED_KEYS);
                        ps.setString(1,p.getUniqueId().toString());
                        ps.executeUpdate();

                        ResultSet rs = ps.getGeneratedKeys();
                        if(rs.first()) id = rs.getInt(1);

                        MySQLManager.getInstance().closeResources(rs,ps);

                        if(id > 0){
                            CraftingRecipe recipe = new CraftingRecipe(id);

                            p.sendMessage(ChatColor.GREEN + "Recipe created. ID: #" + recipe.getId());
                        } else {
                            p.sendMessage(ChatColor.RED + "An error occurred.");
                        }
                    } catch(Exception e){
                        e.printStackTrace();
                        p.sendMessage(ChatColor.RED + "An error occurred.");
                    }
                });
            } else if(args[0].equalsIgnoreCase("list")){
                if(CraftingRecipe.STORAGE.size() == 0){
                    p.sendMessage(ChatColor.RED + "No recipes found.");
                } else {
                    for(CraftingRecipe recipe : CraftingRecipe.STORAGE.values()){
                        if(recipe.getResult() != null){
                            p.sendMessage(ChatColor.YELLOW + String.valueOf(recipe.getId()) + ": " + ChatColor.GREEN + ChatColor.stripColor(recipe.getResult().getData().getName()));
                        } else {
                            p.sendMessage(ChatColor.YELLOW + String.valueOf(recipe.getId()) + ": " + ChatColor.GREEN + "no result defined");
                        }
                    }
                }
            } else {
                sendUsage(p,label);
            }
        } else if(args.length == 2){
            if(Util.isValidInteger(args[0])){
                CraftingRecipe recipe = CraftingRecipe.getRecipe(Integer.parseInt(args[0]));

                if(recipe != null){
                    if(args[1].equalsIgnoreCase("changeresult")){
                        if(p.getItemInHand() != null){
                            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

                            if(item != null){
                                StoredCustomItem s = new StoredCustomItem(item,p.getItemInHand().getAmount());
                                s.update();
                                recipe.setResult(s);
                                recipe.setHasUnsavedData(true);

                                p.sendMessage(ChatColor.GREEN + "Result set to: " + s.amount + "x " + ChatColor.stripColor(s.getData().getName()));
                            } else {
                                p.sendMessage(ChatColor.RED + "Please hold the item you want as a result in your hand.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Please hold the item you want as a result in your hand.");
                        }
                    } else if(args[1].equalsIgnoreCase("info")){
                        p.sendMessage(ChatColor.GREEN + "Recipe ID: " + ChatColor.YELLOW + "#" + recipe.getId());
                        if(recipe.getResult() != null) p.sendMessage(ChatColor.GREEN + "Result: " + ChatColor.YELLOW + "#" + ChatColor.stripColor(recipe.getResult().getData().getName()));
                        if(recipe.getNeededItems() != null && recipe.getNeededItems().length > 0){
                            p.sendMessage(ChatColor.GREEN + "Needed Items:");

                            for(StoredCustomItem item : recipe.getNeededItems()){
                                p.sendMessage(ChatColor.YELLOW + "        " + item.amount + "x " + ChatColor.stripColor(item.getData().getName()));
                            }
                        }
                    } else if(args[1].equalsIgnoreCase("addneededitem")){
                        if(p.getItemInHand() != null){
                            CustomItem item = CustomItem.fromItemStack(p.getItemInHand());

                            if(item != null){
                                if(recipe.getNeededItems().length < CraftingMenu.CRAFTING_SLOTS.length){
                                    StoredCustomItem s = new StoredCustomItem(item,p.getItemInHand().getAmount());
                                    s.update();

                                    ArrayList<StoredCustomItem> a = new ArrayList<StoredCustomItem>();
                                    if(recipe.getNeededItems() != null) for(StoredCustomItem sc : recipe.getNeededItems()) a.add(sc);
                                    a.add(s);
                                    recipe.setNeededItems(a.toArray(new StoredCustomItem[]{}));
                                    recipe.setHasUnsavedData(true);

                                    p.sendMessage(ChatColor.GREEN + "Item added.");
                                } else {
                                    p.sendMessage(ChatColor.RED + "That recipe has already reached the maximum of " + CraftingMenu.CRAFTING_SLOTS.length +  " needed items.");
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "Please hold the item you want as a result in your hand.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Please hold the item you want as a result in your hand.");
                        }
                    } else {
                        sendUsage(p,label);
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Unknown Recipe ID.");
                }
            } else {
                sendUsage(p,label);
            }
        } else if(args.length == 3){
            if(Util.isValidInteger(args[0])){
                CraftingRecipe recipe = CraftingRecipe.getRecipe(Integer.parseInt(args[0]));

                if(recipe != null){
                    if(args[1].equalsIgnoreCase("removeneededitem")){
                        if(Util.isValidInteger(args[2])){
                            Integer index = Integer.parseInt(args[2]);

                            if(index < recipe.getNeededItems().length && index >= 0){
                                ArrayList<StoredCustomItem> a = new ArrayList<StoredCustomItem>();
                                if(recipe.getNeededItems() != null){
                                    int f = 0;

                                    for(StoredCustomItem sc : recipe.getNeededItems()){
                                        if(index != f) a.add(sc);

                                        f++;
                                    }
                                }
                                recipe.setNeededItems(a.toArray(new StoredCustomItem[]{}));
                                recipe.setHasUnsavedData(true);
                            } else {
                                p.sendMessage(ChatColor.RED + "Please enter a valid index number.");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "Please enter a valid number.");
                        }
                    } else {
                        sendUsage(p,label);
                    }
                } else {
                    p.sendMessage(ChatColor.RED + "Unknown Recipe ID.");
                }
            } else {
                sendUsage(p,label);
            }
        }
    }
}
