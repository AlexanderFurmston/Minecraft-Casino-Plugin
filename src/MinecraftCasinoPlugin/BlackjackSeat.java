package MinecraftCasinoPlugin;

import org.bukkit.Location;

public class BlackjackSeat {
	Location location;
	Integer tableID;
	
	public BlackjackSeat(Location location, Integer tableID) {
		this.location = location;
		this.tableID = tableID;
	}
	
	public Location getLocation() {
		return this.location;
	}
	public Integer getTableID() {
		return this.tableID;
	}
}
