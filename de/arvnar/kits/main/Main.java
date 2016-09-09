package de.arvnar.kits.main;


import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import de.arvnar.kits.commands.Kit;
import de.arvnar.kits.commands.Showkit;
import de.arvnar.kits.listener.InventoryManager;

public class Main extends JavaPlugin {
	public static Plugin instance;
	public static String joinkit;
	public static String respawnkit;
	public static String prefix = "§8[§3Kit§8] §3";
	public static HashMap<String, Long> delay = new HashMap<String, Long>();
	
	public void onEnable() {
		instance = this;
		Kit.file = new File(getDataFolder(), "Kits.yml");
		Kit.cfg = YamlConfiguration.loadConfiguration(Kit.file);
		if(!Kit.file.exists()) {
			Kit.cfg.set("Kit", null);
			Main.save(Kit.file, Kit.cfg);
		}
		FileConfiguration cfg = getConfig();
		cfg.options().copyDefaults(true);
		cfg.addDefault("Kit.firstjoin", "Starter");
		cfg.addDefault("Kit.respawn", "Starter");
		ArrayList<String> l = new ArrayList<String>();
		l.add("world");
		cfg.addDefault("Kit.worlds", l);
		saveConfig();
		joinkit = c(cfg.getString("Kit.firstjoin"));
		respawnkit = c(cfg.getString("Kit.respawn"));
		loadTempFile();
		getCommand("kit").setExecutor(new Kit());
		getCommand("showkit").setExecutor(new Showkit());
		Bukkit.getPluginManager().registerEvents(new InventoryManager(), this);
		
		File messages = new File(getDataFolder(), "file.yml");
		if(!messages.exists()) {
			try {
				Files.copy(Main.getInstance().getResource("file.yml"), messages.toPath(), new CopyOption[0]);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void onDisable() {
		saveTempFile();
	}
	
	public static String c(String s) {
		return ChatColor.translateAlternateColorCodes('&', s);
	}

	public static Plugin getInstance() {
		return instance;
	}
	
	public static String getRemainingTime(long milliseconds) {
		int sekunden = 0, minuten = 0, stunden = 0, tage = 0;
		for(int i = 0; i < milliseconds; ++i) {
			++sekunden;
			if(sekunden >= 60) {
				minuten++;
				sekunden = 0;
			}
			if(minuten >= 60) {
				stunden++;
				minuten = 0;
			}
			if(stunden >= 24) {
				tage++;
				stunden = 0;
			}
		}
		String sekundenString = (sekunden != 0) ? sekunden + " second" + ((sekunden == 1) ? "" : "s") : null, minutenString = (minuten != 0) ? minuten + " minute" + ((minuten == 1) ? "" : "s") : null, stundenString = (stunden != 0) ? stunden + " hour" + ((stunden == 1) ? "" : "s") : null, tageString = (tage != 0) ? tage + " day" + ((tage == 1) ? "" : "s") : null;
		return ((tageString != null) ? tageString + " " : "") + ((stundenString != null) ? stundenString + " " : "") + ((minutenString != null) ? minutenString + " " : "") + ((sekundenString != null) ? sekundenString : "");
	}

	public static void noPermission(Player p) {
		p.sendMessage(Main.prefix + "You don't have enough permissions.");
	}

	public static void load(File file, YamlConfiguration cfg) {
		try {
			cfg.load(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void save(File file, YamlConfiguration cfg) {
		try {
			cfg.save(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getItemName(Material type) {
		String name = "";
		String[] itemname = type.toString().toLowerCase().split("_");
		for(String part : itemname) {
			name = name + " " + part.substring(0, 1).toUpperCase() + part.substring(1, part.length());
		}
		return name;
	}

	public static String listToString(String list, String spacer) {
		return list.replace("[", "").replace("]", "").replace(", ", spacer);
	}
	
	public static void saveTempFile() {
		File file = new File(Main.getInstance().getDataFolder(), "Temp.yml");
		YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
		ArrayList<String> entrys = new ArrayList<String>();
		for(String key : delay.keySet()) {
			if(haveToWait(key, delay.get(key))) {
				entrys.add(key + ";" + delay.get(key));
			}
		}
		cfg.set("Delays", entrys);
		try {
			cfg.save(file);
		} catch (Exception e) {
		}
	}

	public static void loadTempFile() {
		File file = new File(Main.getInstance().getDataFolder(), "Temp.yml");
		if(file.exists()) {
			YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
			for(String entrys : cfg.getStringList("Delays")) {
				String[] entry = entrys.split(";");
				delay.put(entry[0], Long.valueOf(entry[1]));
				checkDelay(entry[0], Long.valueOf(entry[1]));
			}
			file.delete();
		}
	}

	private static void checkDelay(String key, Long seconds) {
        if(delay.containsKey(key)) {
    		if((System.currentTimeMillis() - delay.get(key)) / 1000 >= seconds) {
    			delay.remove(key);
    		}
        }
	}
	
	private static boolean haveToWait(String key, Long seconds) {
		checkDelay(key, seconds);
		return delay.containsKey(key);
	}
}
