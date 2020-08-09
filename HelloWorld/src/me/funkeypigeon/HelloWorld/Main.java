package me.funkeypigeon.HelloWorld;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import io.netty.util.internal.ThreadLocalRandom;
import net.md_5.bungee.api.ChatColor;

public class Main extends JavaPlugin {
	
	List<Inventory> invs = new ArrayList<Inventory>();
	public static ItemStack[] contents;
	private int itemIndex = 0;
	
	@Override
	public void onEnable() {
		// runs on startup, reload, plugin reload
	     //getServer().getPluginManager().registerEvents(new MyListener(), this);
	}
	
	
	@Override
	public void onDisable() {
		//runs on shutdown, reload, plugin reload
	}
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (label.equalsIgnoreCase("hello")) {
			if (sender instanceof Player) {
				//player
				Player player = (Player) sender;
				if (player.hasPermission("casino.hello")) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Welcome, " + player.getName()));
					return true;
				} else {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&rainbow You don't have the facilities for that bigman"));
					return true;
				}
			} else {
				//console
				sender.sendMessage(ChatColor.GOLD + "You're console, silly!");
				return true;
			}
			
		} else if (label.equalsIgnoreCase("roulette")) {
			// /roulette or /roulette <amount> <colour>
			if (sender instanceof Player) { 
				Player player = (Player) sender;
				if (player.hasPermission("casino.roulette")) {
					Location leaf = new Location(player.getWorld(), (double) 196.5, (double) 70, (double)700.5 );
					double distance = player.getLocation().distance(leaf);
					if (distance < 1.1) {
						if (args.length == 0) {
							//roulette
							player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&a" + Double.toString(distance)) );
							return true;
						} else if (args.length == 2) {
							//roulette <fee>
							if (isInteger(args[0]) && (args[1].equalsIgnoreCase("green") || args[1].equalsIgnoreCase("black") || args[1].equalsIgnoreCase("red"))) {
								ItemStack fee = new ItemStack(Material.DIAMOND);
								fee.setAmount(Integer.parseInt(args[0]));
								if ((player.getInventory().getItemInMainHand().getAmount() >= (Integer.parseInt(args[0]))) && player.getInventory().getItemInMainHand().isSimilar(fee)) {
									player.getInventory().removeItem(fee);
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + "&6 is gambling £" + args[0] + "&6 on roulette!"));
									//spin the GUI
									spin(player, Integer.parseInt(args[0]), args[1]);
									return true;
								} else {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need more diamonds than that to gamble that amount."));
								}
							}
						}
					} else {
						player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&c" + Double.toString(distance)) );
						return true;
					}
				}
			}
			
		} else if (label.equalsIgnoreCase("casino.*")) {
			// /casino <set/locate> <position> <game>
			if (sender instanceof Player) { 
				Player player = (Player) sender;
				if (sender.hasPermission("funkeypigeon."))
				if (args.length == 0) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat's not how to use this command, try /casino <set/locate> <position> <game>"));
					return true;
				} else if (args[0].equals("locate")) {
					player.sendMessage("I don't know where that is at the moment");
					return true;
				} else if (args[0].equals("set")) {
					player.sendMessage("I don't know how to do that yet");
					return true;
				}
			}
		}
		
		return false;
	}
	
	
    public static boolean isInteger(Object object) { 
    	if(object instanceof Integer) { 
    		return true; 
    	} else { 
    		String string = object.toString(); 
    		 
    		try { 
    			Integer.parseInt(string); 
    		} catch(Exception e) { 
    			return false; 
    		}	 
    	} 
       
        return true; 
    } 
	
	
	public void shuffle(Inventory inv) {
		if (contents == null) {
			//make every even block red, every odd black and the final green
			ItemStack[] items = new ItemStack[37];
			for(int i = 0; i < 36; i = i + 2) {
				items[i] = new ItemStack(Material.RED_WOOL, 2);
				items[i+1] = new ItemStack(Material.BLACK_WOOL, 2);
			}
			items[36] = new ItemStack(Material.GREEN_WOOL, 35);
			contents = items;
		}
		
		// randomise starting layout of middle row
		int startingIndex = ThreadLocalRandom.current().nextInt(contents.length);
		for (int i = 0; i < startingIndex; i++) {
			for (int itemstacks = 9; itemstacks < 18; itemstacks++) {
				inv.setItem(itemstacks, contents[(itemstacks + itemIndex) % contents.length]);
			}
			itemIndex++;
		}
		//gold block indicators
		ItemStack indicator = new ItemStack(Material.GOLD_BLOCK);
		ItemMeta meta = indicator.getItemMeta();
		meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', "&8|"));
		indicator.setItemMeta(meta);
		inv.setItem(4, indicator);
		inv.setItem(22, indicator);
	}
	
	
	public void spin(final Player player, Integer fee, String bet) {
		Inventory inv = Bukkit.createInventory(null, 27, ChatColor.translateAlternateColorCodes('&', "&l&4R&0o&4u&0l&2e&4t&0t&4e"));
		shuffle(inv);
		invs.add(inv);
		player.openInventory(inv);
		
		Random r = new Random();
		double seconds = 3 + (6.0 - 3) * r.nextDouble();
		
		//BukkitRunnable is run several times due to .runTaskTimer(this, 0, 2)
		new BukkitRunnable() {
			double delay = 0;
			int ticks = 0;
			boolean done = false;
			
			public void run() {
				if (done)
					return;
				ticks++;
				delay += 1 / (20 * seconds);
				if (ticks > delay * 10) {
					ticks = 0;
					
					for (int itemstacks = 9; itemstacks < 18; itemstacks++) {
						inv.setItem(itemstacks, contents[(itemstacks + itemIndex) % contents.length]);
					}
					itemIndex++;
					
					if (delay >= 0.5) {
						done = true;
						new BukkitRunnable() {
							public void run() {
								//check the bet and outcome, then allocate rewards
								ItemStack item = inv.getItem(13);
								ItemStack betItem;
								ItemStack reward;
								if (bet.equals("red")) {
									betItem = new ItemStack(Material.RED_WOOL, 2);
									reward = new ItemStack(Material.DIAMOND, fee*2);
								} else if (bet.equals("black")) {
									betItem = new ItemStack(Material.BLACK_WOOL, 2);
									reward = new ItemStack(Material.DIAMOND, fee*2);
								} else {
									betItem = new ItemStack(Material.GREEN_WOOL);
									reward = new ItemStack(Material.DIAMOND, fee*35);
								}
								
								if (item.equals(betItem)) {
									player.getInventory().addItem(reward);
									player.updateInventory();
									player.closeInventory();
									cancel();
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + "&6 won £" + Integer.toString(reward.getAmount()) + "&6!" ));
								} else {
									player.closeInventory();
									cancel();
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + "&6 lost :("));
								}
							}
						}.runTaskLater(Main.getPlugin(Main.class), 50);
						cancel();
					}
				}
			}
			
		}.runTaskTimer(this, 0, 2);
	}
}
