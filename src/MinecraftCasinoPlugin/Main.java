package MinecraftCasinoPlugin;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.libs.org.apache.commons.lang3.ArrayUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import io.netty.util.internal.ThreadLocalRandom;
import net.md_5.bungee.api.ChatColor;
import net.minecraft.server.v1_16_R1.Block;
import MinecraftCasinoPlugin.BlackjackSeat;
import MinecraftCasinoPlugin.Card;

public class Main extends JavaPlugin implements Listener {
	
	ArrayList<Inventory> invs = new ArrayList<Inventory>();
	public static ItemStack[] contents;
	private int itemIndex = 0;
	
	ArrayList<Location> rouletteLocations = new ArrayList<Location>();
	ArrayList<BlackjackSeat> blackjackLocations = new ArrayList<BlackjackSeat>();
	ArrayList<ItemStack> cardsList = new ArrayList<ItemStack>();
	ArrayList<Object> l = new ArrayList<Object>();
	static LinkedHashMap<Integer, BlackjackGame> blackjackGames = new LinkedHashMap<Integer, BlackjackGame>();
	static LinkedHashMap<UUID, Integer> blackjackPlayerLocations = new LinkedHashMap<UUID, Integer>();
	ItemStack[] cards;
	ItemStack[] chest;
	
	@Override
	public void onEnable() {
		// runs on startup, reload, plugin reload
	     //getServer().getPluginManager().registerEvents(new MyListener(), this);
		this.getServer().getPluginManager().registerEvents(this, this);
		
		//writes config.yml to data folder
		this.saveDefaultConfig();
		
		//gets the roulette/blackjack seat locations and puts them in a list
		rouletteLocations = (ArrayList<Location>) this.getConfig().getList("roulette.seats");
		ArrayList<Object> l = (ArrayList<Object>) this.getConfig().getList("blackjack.seats");
		for (Integer i = 0; i+1 < l.size(); i += 2) {
			Location location = getLocationFromString((String) l.get(i));
			Integer tableID = (Integer) l.get(i+1);
			BlackjackSeat seat = new BlackjackSeat(location, tableID);
			blackjackLocations.add(seat);
		}
		
		//loads the cards from the config.yml into a list, then converts to ItemStack[]
		cardsList = (ArrayList<ItemStack>) this.getConfig().get("cards");
		cards = cardsList.toArray(new ItemStack[cardsList.size()]); //remove null?
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
						
						player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&cWrong command, try /roulette <bet amount> <bet colour>.") );
						return true;
					} else {
						player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&cYou need to sit at the roulette table for that.") );
						return true;
					}
				}
			}
		} else if (label.equalsIgnoreCase("blackjack")) {
			// /blackjack <start/end/join>
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (player.hasPermission("casino.blackjack")) {
					Location playerLocation = player.getLocation();
					if (args.length == 1 && checkBlackjackLocation(playerLocation)) {
						if (args[0].equalsIgnoreCase("start")) {
							//on /blackjack start, register a new blackjack game and add it to the list of blackjack games
							BlackjackGame game = new BlackjackGame(player, cards);
							Integer tableID = getBlackjackTableID(playerLocation);
							Bukkit.broadcastMessage("tableID" + tableID);
							this.getServer().getPluginManager().registerEvents(game, this);
							blackjackGames.put(tableID, game);
							blackjackPlayerLocations.put(player.getUniqueId(), tableID);
							game.deal(player, 2);
							return true;
						} else if (args[0].equalsIgnoreCase("join")) {
							if (blackjackPlayerLocations.get(player.getUniqueId()) != null) {
								player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&cYou're already in a Blackjack game. Type /blackjack <leave/end>, then try again.") );
								return true;
							} else {
								//find out which table the player is sitting at, add them to it
								BlackjackGame game = blackjackGames.get(getBlackjackTableID(playerLocation));
								Bukkit.broadcastMessage("game:" + game);
								Bukkit.broadcastMessage("blackjackGames:" + blackjackGames.get(0) + blackjackGames.get(1));
								Bukkit.broadcastMessage("tableID:" + getBlackjackTableID(playerLocation));
								game.add(player);
								blackjackPlayerLocations.put(player.getUniqueId(), getBlackjackTableID(playerLocation));
								return true;
							}
						} else if (args[0].equalsIgnoreCase("end")) {
							Integer tableID = blackjackPlayerLocations.getOrDefault(player.getUniqueId(), 0);
							if (tableID != 0 && blackjackGames.get(tableID).getDealer().getUniqueId().equals(player.getUniqueId())) {
								endBlackjack(player);
								return true;
							} else {
								player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&cYou're not the dealer of a Blackjack game.") );
								return true;
							}
						} else if (args[0].equalsIgnoreCase("leave")) {
							Integer tableID = blackjackPlayerLocations.getOrDefault(player.getUniqueId(), 0);
							Bukkit.broadcastMessage("bjloc" + blackjackLocations.get(0).getTableID());
							if (tableID != 0) {
								blackjackGames.get(tableID).undeal(player);
								blackjackGames.get(tableID).remove(player, this);
								return true;
							} else {
								player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&cYou're not in a Blackjack game.") );
								return true;
							}
						}
					}
				}
				player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&cYou need to sit at the Blackjack table for that.") );
				return true;
			}
			
		} else if (label.equalsIgnoreCase("casino")) {
			// /casino <set/locate> <position> <game>
			if (sender instanceof Player) { 
				Player player = (Player) sender;
				if (player.hasPermission("casino.admin")) {
					if (args.length == 3) {
						if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("seat") && args[2].equalsIgnoreCase("roulette")) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Seat set for roulette."));
							saveRouletteLocation(player.getLocation());
							return true;
						} else if (args[0].equalsIgnoreCase("locate") && args[1].equalsIgnoreCase("seat") && args[2].equalsIgnoreCase("roulette")) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6There are roulette seats at the following locations:"));
							for(Location location: rouletteLocations) {
								player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&6X: &r" + Double.toString(location.getX()) + " &6Y: &r" + Double.toString(location.getY()) + " &6Z: &r" + Double.toString(location.getZ())) );
							}
							return true;
						} else if (args[0].equalsIgnoreCase("locate") && args[1].equalsIgnoreCase("seat") && args[2].equalsIgnoreCase("blackjack")) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6There are blackjack seats at the following locations:"));
							for(BlackjackSeat seat : blackjackLocations) {
								String ID = Integer.toString(seat.getTableID());
								//String ID = seat.getValue();
								String x = Double.toString(seat.getLocation().getX());
								String y = Double.toString(seat.getLocation().getY());
								String z = Double.toString(seat.getLocation().getZ());
								player.sendMessage( ChatColor.translateAlternateColorCodes('&', "&6ID: " + ID + " &6X: &r" + x + " &6Y: &r" + y + " &6Z: &r" + z) );
							}
							return true;
						}
					} else if (args.length == 4) {
						if (args[0].equalsIgnoreCase("set") && args[1].equalsIgnoreCase("seat") && args[2].equalsIgnoreCase("blackjack") && isInteger(args[3])) {
							player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Seat set for blackjack."));
							saveBlackjackLocation(player.getLocation(), Integer.parseInt(args[3]));
						}
					} else if (args.length == 1) {
						if (args[0].equalsIgnoreCase("savecards")) {
							this.getConfig().set("cards", chest);
							this.saveConfig();
							cardsList = (ArrayList<ItemStack>) this.getConfig().get("cards");
							cards = cardsList.toArray(new ItemStack[cardsList.size()]);
						} else if (args[0].equalsIgnoreCase("loadcards")) {
							//Integer i = 0;
							for (Integer i = 0; i < cards.length; i++) {
								ItemStack card;
								if (cards[i] != null) {
									card = cards[i];
									if (card.getType() == Material.FILLED_MAP) {
										player.getInventory().addItem(card);
										player.updateInventory();
									}
								}
							}
						}
					}
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cThat's not how to use this command, try /casino <set/locate> <position> <game>. For setting blackjack seat include fourth argument tableID"));
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
		meta.setDisplayName( ChatColor.translateAlternateColorCodes('&', "&8|") );
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
									Bukkit.broadcastMessage( ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + "&6 won £" + Integer.toString(reward) + "&6!" ) );
								} else {
									player.closeInventory();
									cancel();
									Bukkit.broadcastMessage( ChatColor.translateAlternateColorCodes('&', player.getDisplayName() + "&6 lost :(") );
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
	
    @EventHandler
    public void ChestSaves(PlayerInteractEvent event){
    	//if player has relevant permissions, diamond in hand,
    	//opens chest then save their inventory to a global variable so it can be saved to a config file using /casino savecards
    	Chest block = (Chest) event.getClickedBlock().getState();
        Player player = event.getPlayer();
        //DoubleChest doubleChest;
        if (block.getType() == Material.CHEST && player.getInventory().getItemInMainHand().getType() == Material.DIAMOND && player.hasPermission("casino.admin")){
            chest = block.getInventory().getContents();
            //chest = ArrayUtils.addAll((ItemStack[]) doubleChest.getLeftSide().getInventory().getContents(), (ItemStack[]) doubleChest.getRightSide().getInventory().getContents());
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
		String loc = getStringFromLocation(location);
		l.add(loc);
		l.add(tableID);
		this.getConfig().set("blackjack.seats", l);
		this.saveConfig();
	}
	
	public boolean checkBlackjackLocation(Location location) {
		for (BlackjackSeat seat : blackjackLocations) {
			if (seat.getLocation().distance(location) < 1.1) {
				return true;
			}
		}
		return false;
	}
	
	public Integer getBlackjackTableID(Location location) {
		for (BlackjackSeat seat : blackjackLocations) {
			if (seat.getLocation().distance(location) < 1.1) {
				return seat.getTableID().intValue();
			}
		}
		return 0;
	}
	
	public void endBlackjack(Player dealer) {
		//get tableID using player name (player must be dealer from checks in /blackjack <leave/end> command
		Integer tableID = getBlackjackTableID(dealer.getLocation());
		//go thru list of players, remove them from blackjackPlayerLocations and remove cards from their inventories
		for (Player player : blackjackGames.get(tableID).getPlayers()) {
			blackjackPlayerLocations.remove(player.getUniqueId());
			for (ItemStack item : player.getInventory().getContents() ) {
				try {
					if (item == null) return;
				} finally {
					if (item.getType().equals(Material.FILLED_MAP)) {
						player.getInventory().remove(item);
					}
				}
			}
		}
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
	
	public static String getStringFromLocation(Location loc) {
		if (loc == null) {
			return "";
		}
		return loc.getWorld().getName() + ":" + loc.getX() + ":" + loc.getY() + ":" + loc.getZ() + ":" + loc.getYaw() + ":" + loc.getPitch() ;
	}

	public static Location getLocationFromString(String s) {
		if (s == null || s.trim() == "") {
			return null;
		}
		final String[] parts = s.split(":");
		if (parts.length == 6) {
			World w = Bukkit.getServer().getWorld(parts[0]);
			double x = Double.parseDouble(parts[1]);
			double y = Double.parseDouble(parts[2]);
			double z = Double.parseDouble(parts[3]);
			float yaw = Float.parseFloat(parts[4]);
			float pitch = Float.parseFloat(parts[5]);
			return new Location(w, x, y, z, yaw, pitch);
		}
		return null;
	}
}
