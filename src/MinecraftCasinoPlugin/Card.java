package MinecraftCasinoPlugin;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class Card {
	Integer intVal;
	public Card(Character value, String suit, Player owner, ItemStack map) {
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
