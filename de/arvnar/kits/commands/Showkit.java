package de.arvnar.kits.commands;


import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import de.arvnar.kits.main.Main;

public class Showkit implements CommandExecutor {
	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
		if(s instanceof Player) {
			Player p = (Player) s;
			if(Kit.cfg.getConfigurationSection("Kits") == null) {
				p.sendMessage(Main.prefix + "There are no available kits.");
				return true;
			}
			if(a.length == 0) {
				usage(p);
			} else {
				for(String kit : Kit.cfg.getConfigurationSection("Kits").getKeys(false)) {
					if(kit.toString().equalsIgnoreCase(a[0]) && p.hasPermission("kit." + kit)) {
						String title = "Kit - " + kit;
						if(title.length() > 32) {
							title = title.substring(0, 32);
						}
						Inventory i = Bukkit.createInventory(null, 36, title);
						Kit.addKit(i, kit);
						p.openInventory(i);
						return true;
					}
				}
				usage(p);
			}
		}
		return true;
	}

	private void usage(Player p) {
		ArrayList<String> s = new ArrayList<String>();
		for(String kit : Kit.cfg.getConfigurationSection("Kits").getKeys(false)) {
			if(p.hasPermission("kit." + kit)) {
				s.add(kit);
			}
		}
		p.sendMessage(Main.prefix + "Use \"/Showkit [" + Main.listToString(s.toString(), "|") + "]\".");
	}
}
