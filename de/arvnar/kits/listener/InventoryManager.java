package de.arvnar.kits.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import de.arvnar.kits.commands.Kit;
import de.arvnar.kits.main.Main;

public class InventoryManager implements Listener {
	@EventHandler
	public void onInventoryClick(InventoryClickEvent e) {
		Player p = (Player) e.getWhoClicked();
		if(e.isCancelled()) {
			return;
		}
		if(Kit.cfg.getConfigurationSection("Kits") == null) {
			return;
		}
		for(String kit : Kit.cfg.getConfigurationSection("Kits").getKeys(false)) {
			if(e.getInventory().getName().equalsIgnoreCase(Main.prefix + kit + " Kit")) {
				e.setCancelled(true);
				return;
			}
		}
		if(e.getInventory().getName().contains("Kit - ")) {
			e.setCancelled(true);
			return;
		}
		if(e.getInventory().getName().equalsIgnoreCase(Main.prefix + "Kits")) {
			if(e.getClick() == null || e.getRawSlot() == -999) {
				return;
			}
			e.setCancelled(true);
			for(String kit : Kit.cfg.getConfigurationSection("Kits").getKeys(false)) {
				if(Main.getInstance().getConfig().getItemStack("Kit.Item." + kit) == null || Main.getInstance().getConfig().get("Delay.Kit." + kit) == null) {
					continue;
				}
				if(e.getCurrentItem().getType() == Main.getInstance().getConfig().getItemStack("Kit.Item." + kit).getType()) {
					if(p.hasPermission("kit." + kit.toString())) {
						if(!p.hasPermission("kit.delay.bypass")) {
							int i = Main.getInstance().getConfig().getInt("Delay.Kit." + kit);
					        if(Main.delay.containsKey(p.getName() + ":" + kit)) {
					    		if((System.currentTimeMillis() - Main.delay.get(p.getName() + ":" + kit)) / 1000 >= i) {
					    			Main.delay.remove(p.getName() + ":" + kit);
					    		}
					        }
							if(Main.delay.containsKey(p.getName() + ":" + kit)) {
								p.sendMessage(Main.prefix + "You have to wait " + Main.getRemainingTime(i - (System.currentTimeMillis() - Main.delay.get(p.getName() + ":" + kit)) / 1000) + " until you can request the " + kit + " kit.");
								return;
							}
							Main.delay.put(p.getName() + ":" + kit, System.currentTimeMillis());
						}
						Kit.giveKit(p, kit, true);
					} else {
						Main.noPermission(p);
					}
					p.closeInventory();
				}
			}
			p.closeInventory();
		}
	}
}
