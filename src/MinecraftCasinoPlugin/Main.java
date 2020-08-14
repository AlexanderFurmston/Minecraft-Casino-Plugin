package MinecraftCasinoPlugin;

import java.util.*;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import io.netty.util.internal.ThreadLocalRandom;
import net.md_5.bungee.api.ChatColor;

import MinecraftCasinoPlugin.BlackjackSeat;
import MinecraftCasinoPlugin.Card;

public class Main extends JavaPlugin implements Listener{
	
	List<Inventory> invs = new ArrayList<Inventory>();
	public static ItemStack[] contents;
	private int itemIndex = 0;
	
	List<Location> rouletteLocations = new ArrayList<Location>();
	List<BlackjackSeat> blackjackLocations = new ArrayList<BlackjackSeat>();
	
	@Override
	public void onEnable() {
		// runs on startup, reload, plugin reload
	     //getServer().getPluginManager().registerEvents(new MyListener(), this);
		this.getServer().getPluginManager().registerEvents(this, this);
		
		//writes config.yml to data folder
		this.saveDefaultConfig();
		//gets the roulette seat locations and puts them in a list
		rouletteLocations = (List<Location>) this.getConfig().getList("roulette.seats");
		blackjackLocations = (List<BlackjackSeat>) this.getConfig().getList("blackjack.seats");
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
					if (checkRouletteLocation(player.getLocation())) {
						
						if (args.length == 0) {
							//roulette
							player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&aYou are seated. Now try /roulette <bet amount> <colour>") );
							return true;
							
						} else if (args.length == 2) {
							//roulette <fee>
							if (isInteger(args[0]) && (args[1].equalsIgnoreCase("green") || args[1].equalsIgnoreCase("black") || args[1].equalsIgnoreCase("red"))) {
								ItemStack fee = new ItemStack(Material.DIAMOND);
								fee.setAmount(Integer.parseInt(args[0]));
								if ((player.getInventory().getItemInMainHand().getAmount() >= (Integer.parseInt(args[0]))) && player.getInventory().getItemInMainHand().isSimilar(fee)) {
									player.getInventory().removeItem(fee);
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + "&6 is gambling £" + args[0] + "&6 on " + args[1] + "&6 !"));
									//spin the GUI
									spin(player, Integer.parseInt(args[0]), args[1]);
									return true;
								} else {
									player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou need more diamonds than that to gamble that amount."));
									return true;
								}
							}
						}
						
						player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cWrong command, try /roulette <bet amount> <bet colour>."));
						return true;
					} else {
						player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&cYou need to sit at the roulette table for that.") );
						return true;
					}
				}
			}
		} else if (label.equalsIgnoreCase("blackjack")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.hasPermission("casino.blackjack")) {
					if (args.length == 1) {
						
					}
				}
				player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&cYou need to sit at the roulette table for that.") );
				return true;
			}
			
		} else if (label.equalsIgnoreCase("casino")) {
			// /casino <set/locate> <position> <game>
			// for blackjack: /casino <set/locate> <position> <tableID>
			if (sender instanceof Player) { 
				Player player = (Player) sender;
				if (player.hasPermission("casino.admin")) {
					if (args.length == 3) {
						if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("seat") && args[2].equalsIgnoreCase("roulette")) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Seat set for blackjack."));
							saveRouletteLocation(player.getLocation());
							return true;
						} else if (args[0].equalsIgnoreCase("locate") && args[1].equalsIgnoreCase("seat") && args[2].equalsIgnoreCase("roulette")) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6There are roulette seats at the following locations:"));
							for(Location location: rouletteLocations) {
								player.sendMessage( ChatColor.translateAlternateColorCodes('&', "X: &r" + Double.toString(location.getX()) + " &6Y: &r" + Double.toString(location.getY()) + " &6Z: &r" + Double.toString(location.getZ())) );
							}
							return true;
						} else if (args[0].equalsIgnoreCase("locate") && args[1].equalsIgnoreCase("seat") && args[2].equalsIgnoreCase("blackjack")) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6There are blackjack seats at the following locations:"));
							for(BlackjackSeat seat: blackjackLocations) {
								player.sendMessage( ChatColor.translateAlternateColorCodes('&', "X: &r" + Double.toString(seat.getLocation().getX()) + " &6Y: &r" + Double.toString(seat.getLocation().getY()) + " &6Z: &r" + Double.toString(seat.getLocation().getZ())) );
							}
							return true;
						}
					} else if (args.length == 4) {
						if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("seat") && args[2].equalsIgnoreCase("blackjack") && isInteger(args[3])) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Seat set for blackjack."));
							saveBlackjackLocation(player.getLocation(), Integer.parseInt(args[3]));
						}
					}
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat's not how to use this command, try /casino <set/locate> <position> <game>. For blackjack include fourth argument tableID"));
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
								Integer reward;
								if (bet.equals("red")) {
									betItem = new ItemStack(Material.RED_WOOL, 2);
									reward = fee * 2;
								} else if (bet.equals("black")) {
									betItem = new ItemStack(Material.BLACK_WOOL, 2);
									reward = fee * 2;
								} else {
									betItem = new ItemStack(Material.GREEN_WOOL, 35);
									reward = fee * 35;
								}
								
								if (item.equals(betItem)) {
									giveItems(Material.DIAMOND, reward, player);
									cancel();
									Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + "&6 won £" + Integer.toString(reward) + "&6!" ));
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
	
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		if (!invs.contains(event.getInventory())) {
			return;
		} else {
			event.setCancelled(true);
			return; 
		}
	}
	
	
	public void saveRouletteLocation(Location location) {
		rouletteLocations.add(location);
		this.getConfig().set("roulette.seats", rouletteLocations);
		this.saveConfig();
	}
	
	public boolean checkRouletteLocation(Location location) {
		for (Location seat : rouletteLocations) {
			if (seat.distance(location) < 1.1) {
				return true;
			}
		}
		return false;
	}
	
	public void saveBlackjackLocation(Location location, Integer tableID) {
		BlackjackSeat seat = new BlackjackSeat(location, tableID);
		blackjackLocations.add(seat);
		this.getConfig().set("blackjack.seats", blackjackLocations);
		this.saveDefaultConfig();
	}
	
	public boolean checkBlackjackLocation(Location location) {
		for (BlackjackSeat seat: blackjackLocations) {
			if (seat.getLocation().distance(location) < 1.1) {
				return true;
			}
		}
		return false;
	}
	
	
	public void giveItems(Material material, Integer amount, Player player) {
		if (amount <= 64) {
			ItemStack items = new ItemStack(material, amount);
			player.getInventory().addItem(items);
			player.updateInventory();
		} else {
			Integer i = 0;
			while (amount - i >= 64) {
				ItemStack items = new ItemStack(material, 64);
				player.getInventory().addItem(items);
				player.updateInventory();
				i = i + 64;
			}
			ItemStack items = new ItemStack(material, amount - i);
			player.getInventory().addItem(items);
			player.updateInventory();
		}
	}
}
