package com.gmail.cxfredeper;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.GameMode;
import org.bukkit.Location;

public class PlayerInfoContainer {
	private String userName = "";
	private String password;
	private Location homeLoc;
	private Map<String, Location> wayPoints = new HashMap<String, Location>();
	private GameMode gamemode;
	
	public String getUserName() {
		return userName;
	}
	
	public String getPassword() {
		return password;
	}
	
	public Location getHomeLoc() {
		return homeLoc;
	}
	
	public GameMode getGamemode() {
		return gamemode;
	}
	
	public Location getWayPoint(String id) {
		return wayPoints.get(id);
	}
	
	public Map<String, Location> getWayPoints() {
		return wayPoints;
	}
	
	public void setUserName(String userName) {
		this.userName = userName;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setHomeLoc(Location loc) {
		homeLoc = loc;
	}
	
	public void setGamemode(GameMode gamemode) {
		this.gamemode = gamemode;
	}
	
	public void putWayPoint(String id, Location wayPoint) {
		wayPoints.put(id, wayPoint);
	}
	
	public Location delWayPoint(String id) {
		return wayPoints.remove(id);
	}
	
	public PlayerInfoContainer() {
	}
}
