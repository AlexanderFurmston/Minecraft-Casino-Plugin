package MinecraftCasinoPlugin;

import java.util.UUID;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Card {
	Integer intVal;
	Character value;
	Character suit;
	Inventory inventory;
	String owner;
	ItemStack map;
	
	public Card(Character value, Character suit, Inventory inventory, String owner, ItemStack map) {
		this.value = value;
		this.suit = suit;
		this.inventory = inventory;
		this.owner = owner;
		this.map = map;
		if (isInteger(value)) {
			this.intVal = Character.getNumericValue(value);
		} else {
			switch (value) {
				case 'A':
					this.intVal = 1;
					break;
				case 'J':
					this.intVal = 10;
					break;
				case 'Q':
					this.intVal = 10;
					break;
				case 'K':
					this.intVal = 10;
					break;
			}
		}
	}
	
	public Integer getIntVal() {
		return this.intVal;
	}
	
	public ItemStack getMap() {
		return this.map;
	}
	
	public String getOwner() {
		return this.owner;
	}
	
	public Inventory getInventory() {
		return this.inventory;
	}
	
	//changes the value of an ace between 1 and 11 (and updates its name so the player knows)
	public void changeAce() {
		if (this.intVal == 1) {
			this.intVal = 11;
			this.map.getItemMeta().setDisplayName("11");
		} else if (this.intVal == 11) {
			this.intVal = 1;
			this.map.getItemMeta().setDisplayName("1");
		}
	}
	
	public void changeOwner(String newOwner, Inventory newInv) {
		this.owner = newOwner;
		this.inventory.remove(map);
		this.inventory = newInv;
		this.inventory.addItem(map);
	}
	
	
	public static boolean isInteger(Object object) { 
    	if(object instanceof Integer) { 
    		return true; 
    	} else { 
    		Character theChar = object.toString().charAt(0);
    		 
    		try { 
    			theChar.getNumericValue(theChar);
    		} catch(Exception e) {
    			return false; 
    		}	 
    	} 
       
        return true; 
    }
}
