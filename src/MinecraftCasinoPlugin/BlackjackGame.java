package MinecraftCasinoPlugin;

import java.util.*;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import MinecraftCasinoPlugin.Card;

public class BlackjackGame implements Listener {
	Player dealer;
	Inventory inventory;
	LinkedHashMap<String, Card> deck = new LinkedHashMap<String, Card>();
	Character[] suits = {'S', 'C', 'D', 'H'};
	Character[] values = {'A', '2', '3', '4', '5', '6', '7', '8', '9', 'J', 'Q', 'K'};
	ArrayList<Player> players = new ArrayList<Player>();
	ItemStack[] cards;
	
	public BlackjackGame(Player dealer, ItemStack[] cards) {
		this.dealer = dealer;
		this.inventory = Bukkit.createInventory(null, 27, "table");;
		this.cards = cards;
		Integer i = 0;
		for(Character suit : suits) {
			for (Character value : values ) {
				Card card = new Card(value, suit, inventory, "table", cards[i]);
				String suitValue = Character.toString(suit) + Character.toString(value);
				deck.put(suitValue, card);
				i++;
			}
		}
	}
	
	public void add(Player player) {
		players.add(player);
		deal(player, 2);
	}
	
	public void remove(Player player, Main context) {
		if (player.getName().equalsIgnoreCase(dealer.getName())) {
			context.endBlackjack(player);
		} else {
			players.remove(player);
		}
	}
	
	public void deal(Player player, Integer amount) {
		//Go through the LinkedHashMap of cards, if they're taken then don't assign them to the new player. If they're not, do (for the amount)
		Integer dealt = 0;
		for (Integer index = 0; dealt < amount; index++) {
			String key = (String) deck.keySet().toArray()[index];
			Card value = deck.get(key);
			Bukkit.broadcastMessage("Value:" + value);
			Bukkit.broadcastMessage("getOwner:" + value.getOwner());
			if (value.getOwner().equalsIgnoreCase("table")) {
				value.changeOwner(player.getName(), player.getInventory());
				dealt++;
			}
		}
	}
	
	public void undeal(Player player) {
		for (ItemStack item : player.getInventory().getContents() ) {
			if (item == null) {
			} else if (item.getType().equals(Material.FILLED_MAP)) {
				player.getInventory().remove(item);
			}
		}
	}
	
	public void shuffle() {
		LinkedHashMap<String, Card> newPack = new LinkedHashMap<String, Card>();
		while (deck.size() > 0) {
			//shuffle stuff
			Random rand = new Random();
			Integer index = rand.nextInt(deck.size());
			String key = (String) deck.keySet().toArray()[index];
			Card value = (Card) deck.values().toArray()[index];
			newPack.put(key, value);
			deck.remove(key);
		}
	}
	
	public Inventory getInventory() {
		return this.inventory;
	}
	
	public Player getDealer() {
		return this.dealer;
	}
	
	public ArrayList<Player> getPlayers() {
		return this.players;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent event) {
		//on the click of a map, check if it's a card, then check if an ace. If it's ace, change it's value between 1 and 11	
		if (event.getCurrentItem().getType() == Material.FILLED_MAP) {
			if ( event.getCurrentItem().isSimilar(deck.get("AS").getMap()) ) {
				deck.get("AS").changeAce();
			} else if ( event.getCurrentItem().isSimilar(deck.get("AC").getMap()) ) {
				deck.get("AC").changeAce();
			} else if ( event.getCurrentItem().isSimilar(deck.get("AD").getMap()) ) {
				deck.get("AD").changeAce();
			} else if ( event.getCurrentItem().isSimilar(deck.get("AH").getMap()) ) {
				deck.get("AH").changeAce();
			}
		}
	}
}
