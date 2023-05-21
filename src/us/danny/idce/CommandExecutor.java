package us.danny.idce;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

//not to be confused with the Bukkit command executor
public class CommandExecutor {
	private final String lore;
	private final String command;
	
	public CommandExecutor(String lore, String command) {
		this.lore = lore;
		this.command = command;
	}
	
	public void executeCommandsForAllPlayersHoldingCorrectItem() {
		ItemStack item;
		for(Player player : Bukkit.getOnlinePlayers()) {
			item = player.getItemInHand();
			if(isValidItem(item)) {
				player.chat(command);
			}
		}
	}
	
	private boolean isValidItem(ItemStack item) {
		if(item.hasItemMeta()) {
    		ItemMeta itemMeta = item.getItemMeta();
    		if(itemMeta.hasLore()) {
    			List<String> loreList = itemMeta.getLore();
    			return loreList.contains(lore);
    		}
    	}
    	return false;
	}
}
