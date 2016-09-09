package de.arvnar.kits.commands;


import java.io.File;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import de.arvnar.kits.main.Main;

public class Kit implements CommandExecutor {
  	public static File file;
  	public static YamlConfiguration cfg;
	
	@Override
	public boolean onCommand(CommandSender s, Command c, String l, String[] a) {
		if(s instanceof Player) {
			Player p = (Player) s;
			if(a.length == 0) {
				showKits((Player) s);
			} else if(a.length == 2) {
				if(a[0].equalsIgnoreCase("setitem")) {
					if(p.hasPermission("kit.setitem")) {
						if(p.getItemInHand() != null && p.getItemInHand().getType() != Material.AIR) {
							Main.getInstance().getConfig().set("Kit.Item." + a[1], p.getItemInHand());
							Main.getInstance().saveConfig();
							p.sendMessage(Main.prefix + "Successfully edited " + a[1] + " kit.");
						} else {
							p.sendMessage(Main.prefix + "Please hold an item in your hand.");
						}
					} else {
						Main.noPermission(p);
					}
				} else if(a[0].equalsIgnoreCase("worlds") && a[1].equalsIgnoreCase("list")) {
					if(p.hasPermission("kit.worlds.list")) {
						List<String> worlds = Main.getInstance().getConfig().getStringList("Kit.worlds");
						if(worlds.isEmpty()) {
							p.sendMessage(Main.prefix + "There are no worlds, which allow to use kits.");
						} else {
							p.sendMessage(Main.prefix + "You are able to request kits in the following worlds: " + Main.listToString(worlds.toString(), ", "));
						}
					} else {
						Main.noPermission(p);
					}
				} else if(a[0].equalsIgnoreCase("remove")) {
					if(p.hasPermission("kit.remove")) {
						Kit.cfg.set("Kits." + a[1], null);
						Main.save(Kit.file, Kit.cfg);
						Main.load(Kit.file, Kit.cfg);
						p.sendMessage(Main.prefix + a[1] + " kit successfully removed.");
					} else {
						Main.noPermission(p);
					}
				} else {
					showHelp(p);
				}
			} else if(a.length == 3) {
				if(a[0].equalsIgnoreCase("set")) {
					if(p.hasPermission("kit.set")) {
						String kit = a[1];
						int i = 0;
						try {
							i = Integer.parseInt(a[2]);
							if(i < 1) {
								p.sendMessage(Main.prefix + a[2] + " is no valid amount.");
								return true;
							}
							Main.getInstance().getConfig().set("Delay.Kit." + kit, i);
							Main.getInstance().saveConfig();
						} catch (Exception e) {
							p.sendMessage(Main.prefix + a[2] + " is no valid amount.");
							return true;
						}	
						Main.load(Kit.file, Kit.cfg);
						int slot = 1;
						for(ItemStack is : p.getInventory().getContents()) {
							Kit.cfg.set("Kits." + kit + "." + slot, null);
							Main.save(Kit.file, Kit.cfg);
							if(is != null) {
								p.sendMessage(Main.prefix + "You added " + is.getAmount() + Main.getItemName(is.getType()) + " to the " + kit + " kit.");
								Kit.cfg.set("Kits." + kit + "." + slot, is);
								Main.save(Kit.file, Kit.cfg);
							}
							++slot;
						}
						Main.load(Kit.file, Kit.cfg);
						p.sendMessage(Main.prefix + kit + " kit successfully edited.");
					} else {
						Main.noPermission(p);
					}
				} else if(a[0].equalsIgnoreCase("worlds")) {
					List<String> worlds = Main.getInstance().getConfig().getStringList("Kit.worlds");
					if(a[1].equalsIgnoreCase("add")) {
						if(p.hasPermission("kit.worlds.add")) {
							if(worlds.contains(a[2])) {
								p.sendMessage(Main.prefix + "The world " + a[2] + " is already registered.");
							} else {
								worlds.add(a[2]);
								Main.getInstance().getConfig().set("Kit.worlds", worlds);
								Main.getInstance().saveConfig();
								p.sendMessage(Main.prefix + "You successfully added the world " + a[2] + ".");
							}
						} else {
							Main.noPermission(p);
						}
					} else if(a[1].equalsIgnoreCase("remove")) {
						if(p.hasPermission("kit.worlds.remove")) {
							if(worlds.contains(a[2])) {
								worlds.remove(a[2]);
								Main.getInstance().getConfig().set("Kit.worlds", worlds);
								Main.getInstance().saveConfig();
								p.sendMessage(Main.prefix + "You successfully removed the world " + a[2] + ".");
							} else {
								p.sendMessage(Main.prefix + "The world " + a[2] + " is not registered.");
							}
						} else {
							Main.noPermission(p);
						}
					} else {
						showHelp(p);
					}
				} else {
					showHelp(p);
				}
			} else {
				showHelp(p);
			}
		}
		return true;
	}
	
	private void showHelp(Player p) {
		p.sendMessage("§8/Kit - §6Shows all available kits");
		p.sendMessage("§8/Showkit - §6Shows the items of a kit");
		p.sendMessage("§8/Kit remove [Name] - §6Removes a kit");
		p.sendMessage("§8/Kit set [Name] [Delay in seconds] - §6Adds a kit");
		p.sendMessage("§8/Kit setitem [Kit] - §6Set the item of a kit");
		p.sendMessage("§8/Kit worlds [list|add|remove] [world] - §6Manages the worlds");
	}

	public static void showKits(Player p) {
		if(!Main.getInstance().getConfig().getStringList("Kit.worlds").contains(p.getWorld().getName())) {
			p.sendMessage(Main.prefix + "The kits aren't available in this world.");
			return;
		}
		if(Kit.cfg.getConfigurationSection("Kits") == null) {
			p.sendMessage(Main.prefix + "There are no available kits.");
			return;
		}
		int x = Kit.cfg.getConfigurationSection("Kits").getKeys(false).size();
		if(x > 45) {
			x = 54;
		} else if(x > 36) {
			x = 45;
		} else if(x > 27) {
			x = 36;
		} else if(x > 18) {
			x = 27;
		} else if(x > 9) {
			x = 18;
		} else {
			x = 9;
		}
		Inventory i = p.getServer().createInventory(null, x, Main.prefix + "Kits");
		for(String kit : Kit.cfg.getConfigurationSection("Kits").getKeys(false)) {
			if(Main.getInstance().getConfig().getItemStack("Kit.Item." + kit) == null) {
				p.sendMessage(Main.prefix + "There is no item for the " + kit + " kit. You can set it using \"/Kit setitem\".");
				continue;
			}
			if(!p.hasPermission("kit." + kit)) {
				continue;
			}
			ItemStack is = new ItemStack(Main.getInstance().getConfig().getItemStack("Kit.Item." + kit).getType());
			ItemMeta im = is.getItemMeta();
			im.setDisplayName("§8» §c" + kit + " Kit §8«");
			is.setItemMeta(im);
			i.addItem(is);
		}
		p.openInventory(i);
	}
	
	public static void giveKit(Player p, String kit, boolean info) {
		Inventory i = p.getInventory();
		addKit(i, kit);
		p.updateInventory();
		if(info) {
			p.sendMessage(Main.prefix + "You received the " + kit.toString() + " kit.");
		}
	}
	
	public static void addKit(Inventory i, String kit) {
		if(cfg.getConfigurationSection("Kits." + kit) == null) {
			return;
		}
		for(String s : cfg.getConfigurationSection("Kits." + kit).getKeys(false)) {
			i.addItem(cfg.getItemStack("Kits." + kit + "." + s));
		}
	}
}
