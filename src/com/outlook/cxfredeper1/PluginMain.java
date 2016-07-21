package com.outlook.cxfredeper1;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import com.gmail.gengchenliu0.Enigma;

/* TO DO LIST
 * combine info to one file
 * game mode save
 * way points
 */

public class PluginMain extends JavaPlugin implements Listener {
	
	private String verString = "CxfredeperPlugin 1.3.162";
	
	private Map<String, Location> tpLocs = new HashMap<String, Location>();
	private Map<String, PlayerInfoContainer> playerInfos = new HashMap<String, PlayerInfoContainer>();
	private List<String> logedinPlayers = new ArrayList<String>();
	private List<String> registeredPlayers = new ArrayList<String>();
	private long interval; //20 ticks = 1 sec (in general)
	private List<String> messages = new ArrayList<String>();
	private BukkitScheduler scheduler = getServer().getScheduler();
	private int broadcastID;
	private static String message = "";
	private Path filePath = Paths.get("plugins/cxfredeperPluginData/cxfredeperPluginData");
	private BufferedReader fileReader;
	private BufferedWriter fileWriter;
	private Server server = this.getServer();
	private EventCanceller ec = new EventCanceller();
	private Logger logger = getLogger();
	private Logger serverLogger = Bukkit.getServer().getLogger();
	private Enigma enigma = new Enigma();

	@Override
    public void onEnable() {
        server.getPluginManager().registerEvents(this, this);
        server.getPluginManager().registerEvents(ec, this);
        //create directories
        Path pluginHomeDir = Paths.get("plugins/cxfredeperPluginData");
        if (!Files.exists(pluginHomeDir)) {
			try {
				logger.info("Directory '\\plugins\\cxfredeperPluginData' not found. Creating.");
				Files.createDirectories(pluginHomeDir);
			} catch (IOException e) {
				logger.info("Could not create 'cxfredeperPluginData' directory. Here's the stack trace:");
				e.printStackTrace();
				logger.info("You may want to create the directory '\\plugins\\cxfredeperPluginData' on your own.");
			}
        }
        
        //load player info
		try {
			fileReader = Files.newBufferedReader(filePath, Charset.forName("UTF-8"));
			readPlayerInfo();
		} catch (NoSuchFileException ex0) {
			logger.info("File 'cxfredeperPluginData' not found. Creating.");
			try {
				fileWriter = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"));
				fileReader = Files.newBufferedReader(filePath, Charset.forName("UTF-8"));
				readPlayerInfo();
			} catch (IOException ex00) {
				logger.info("Failed to create 'cxfredeperPluginData'. Here's the stack trace:");
				ex00.printStackTrace();
			}
		} catch (IOException ex1) {
			logger.info("Failed to read 'cxfredeperPluginData'. Here's the stack trace:");
			ex1.printStackTrace();
		}
		
		//trying to filter out login and register commands from logger
		//filters not working
		Filter filter = new Filter() {
			@Override
			public boolean isLoggable(LogRecord log) {
				return !(log.getMessage().contains("issued server command: //login") || log.getMessage().contains("issued server command: //register"));
			}
		};
		
		serverLogger.setFilter(filter);
		serverLogger.info("issued server command: //login");
		
		for (Handler handler : serverLogger.getHandlers()) {
			handler.setFilter(filter);
		}
		
		logger.info("CxfredeperPlugin Enabled.");
    }
    
    @Override
    public void onDisable() {
    	try {
    		//write the passwords and homes
    		//homesFileWriter = Files.newBufferedWriter(homesFilePath, Charset.forName("UTF-8"));
    		if (fileWriter == null)
    			fileWriter = Files.newBufferedWriter(filePath, Charset.forName("UTF-8"));
    		//passwords
    		logger.info("Saving player info.");
    		for (String playerID : playerInfos.keySet()) {
    			PlayerInfoContainer playerInfo = playerInfos.get(playerID);
	    		try {
					writePlayerInfo(playerInfo);
				} catch (IOException e) {
					logger.info("Failed to save player information to 'cxfredeperPluginData'. Here's the stack trace:");
					e.printStackTrace();
				}
    		}
    		logger.info("Player info saved.");
    		
    		//close the readers and writers
			fileReader.close();
			fileWriter.close();
		} catch (IOException e) {
			logger.info("IOException Caught. Here's the stack trace:");
			e.printStackTrace();
		}
    	logger.info("CxfredeperPlugin Disabled.");
    }

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player player = null;
		List<String> argsL = null;
		String playerID = null;
		PlayerInfoContainer playerInfo = new PlayerInfoContainer();
		Boolean senderIsPlayer = sender instanceof Player;
		String commandString = cmd.getName();
		
		if (senderIsPlayer) {
			player = (Player) sender;
			argsL = new ArrayList<String>(Arrays.asList(args));
			playerID = player.getName();
			playerInfo = playerInfos.get(playerID);
			//if the player haven't register, playerInfo will be null
			//thus create a PlayerInfoContainer with what we know at least
			if (playerInfo == null) {
				playerInfo = new PlayerInfoContainer();
				playerInfo.setUserName(playerID);
				playerInfo.setGamemode(GameMode.SURVIVAL);
			}
		}

		if (commandString.equals("/tp") && senderIsPlayer) {
			
			for (String i : args) {
				if (!logedinPlayers.contains(sender.getServer().getPlayer(i))) {
					sender.sendMessage(ChatColor.RED + "Player offline or does not exist.");
					return true;
				}
			}
			
			if (args.length == 2) {
				Player playerI = server.getPlayer(args[0]);
				Player playerT = server.getPlayer(args[1]);
				tpLocs.put(playerI.getName(), playerI.getLocation());
				playerI.teleport(playerT);
				return true;
			}
			
			else if (args.length == 1) {
				Player playerT = server.getPlayer(args[0]);
				tpLocs.put(playerID, player.getLocation());
				player.teleport(playerT);
				return true;
			}
			return false;
		}
		
		else if (commandString.equals("/sethome") && senderIsPlayer) {
			Location loc = player.getLocation();
			playerInfo.setHomeLoc(loc);
			playerInfos.put(playerID, playerInfo);
			player.setBedSpawnLocation(loc);
			player.sendMessage(ChatColor.GREEN + "//sethome command executed: home set at "
					+ Double.toString(loc.getX()) + ", "
					+ Double.toString(loc.getY()) + ", "
					+ Double.toString(loc.getZ()));
			return true;
		}
		
		else if (commandString.equals("/home") && senderIsPlayer) {		
			try {
				tpLocs.put(playerID, player.getLocation());
				Location homeLoc = playerInfo.getHomeLoc();
				player.teleport(homeLoc);
				player.sendMessage(ChatColor.GREEN + "//home command executed: teleported " + playerID + " to " 
						+ Double.toString(homeLoc.getX()) + ", "
						+ Double.toString(homeLoc.getY()) + ", "
						+ Double.toString(homeLoc.getZ()));
			}
			
			catch (NullPointerException e) {
				player.sendMessage(ChatColor.RED + "NullPointerException: " + e.getMessage());
				player.sendMessage(ChatColor.YELLOW + "Please use //sethome before using //home");
			}
			return true;
		}
		
		else if (commandString.equals("/back") && senderIsPlayer) {
			try {
				Location loc = player.getLocation();
				player.teleport(tpLocs.get(playerID));
				tpLocs.put(playerID, loc);
				player.sendMessage(ChatColor.GREEN + "//back command executed: teleported " + playerID + " to " 
							+ Double.toString(loc.getX()) + ", "
							+ Double.toString(loc.getY()) + ", "
							+ Double.toString(loc.getZ()));
			}
			catch (NullPointerException e) {
				player.sendMessage(ChatColor.RED + "NullPointerException: " + e.getMessage());
				player.sendMessage(ChatColor.YELLOW + "Please use //home or //tp before using //back.");
			}
			finally{}
			return true;
		}
		
		else if (commandString.equals("/broadcast")) {
			if (args.length == 0) return false;
			
			else if (argsL.get(0).equalsIgnoreCase("add")) {
				argsL.remove(0);
				interval = Long.parseLong(argsL.remove(argsL.size() - 1));
				message = "";
				for (String arg : argsL) {
					message = message.concat(arg + " ");
				}
				broadcastID = scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
					String message = PluginMain.message;
					
					@Override
					public void run() {
						Bukkit.getServer().getLogger().info("Timer Ran");
						Bukkit.getServer().broadcastMessage(ChatColor.BLUE + "<Broadcast> " + message);
					}
		        }, 0l, interval * 20);
				sender.sendMessage("Message set: " + message + "\nRepeating every " + interval + " second(s)");
				sender.sendMessage("Broadcast ID: " + broadcastID);
				
				messages.add(message + "|>|<|" + broadcastID);
				return true;
			}
			else if (argsL.get(0).equalsIgnoreCase("stop")) {
				String id = argsL.get(1);
				scheduler.cancelTask(Integer.parseInt(id));
				sender.sendMessage("Broadcast " + id + " stoped.");
				for (int i=0; i<messages.size(); i++) {
					int index = scan(messages.get(i), "|>|<|");
					if (messages.get(i).substring(index).equals(id)) messages.remove(i);
				}
				return true;
			}
			else if (argsL.get(0).equalsIgnoreCase("list")) {
				for (String s : messages) {
					sender.sendMessage("Message: " + s.replace("|>|<|", " ID: "));
				}
				sender.sendMessage("There are " + messages.size() + " broadcasts ongoing.");
				return true;
			}
		}
		
		else if (commandString.equals("/register") && senderIsPlayer) {
			if (args.length != 2)
				return false;
			if (registeredPlayers.contains(playerID)) {
				sender.sendMessage(ChatColor.RED + "You have already registered. Please use //login to login.");
				return true;
			}
			if (args[0].equals(args[1])) {
				//now, the player must had issued a valid register command and is a unregistered player
				String password = args[0];
				if (password.length() >= 5) {
					registeredPlayers.add(playerID);
					//construct the playerInfo
					playerInfo.setPassword(args[0]);
					playerInfos.put(playerID, playerInfo);
					ec.freezedPlayers.remove(playerID);
					player.setGameMode(GameMode.SURVIVAL);
					sender.sendMessage(ChatColor.ITALIC + "You have registered.");
				}
				else
					sender.sendMessage(ChatColor.RED + "Your password is too short, it must have at least 5 characters.");
			}
			else 
				sender.sendMessage(ChatColor.RED + "The passwords you entered are different. Please enter again.");
			return true;
		}
		
		else if (commandString.equals("/login") && senderIsPlayer) {
			if (args.length != 1)
				return false;
			else if (logedinPlayers.contains(playerID))
				sender.sendMessage(ChatColor.RED + "You have already logged in.");
			else if (!registeredPlayers.contains(playerID))
				sender.sendMessage(ChatColor.RED + "You must register first. Please use //register to register your account.");
			else if (playerInfo.getPassword().equals(args[0])) {
				logedinPlayers.add(playerID);
				ec.freezedPlayers.remove(playerID);
				player.setGameMode(playerInfo.getGamemode());
				sender.sendMessage(ChatColor.ITALIC + "You have logged in.");
			}
			else
				sender.sendMessage(ChatColor.RED + "Incorrect password, please try again.");
			return true;
		}
		
		//waypoint related
		else if (commandString.equals("/waypoint") && senderIsPlayer) {
			if (args.length == 1) {
				Location loc = playerInfo.getWayPoint(args[0]);
				if (loc == null) {
					sender.sendMessage(ChatColor.RED + "You must set the waypoint first. Please use //setwaypoint to set a waypoint.");
					return true;
				}
				tpLocs.put(playerID, player.getLocation());
				player.teleport(loc);
				player.sendMessage(ChatColor.GREEN + "//waypoint command executed: teleported " + playerID + " to " 
						+ Double.toString(loc.getX()) + ", "
						+ Double.toString(loc.getY()) + ", "
						+ Double.toString(loc.getZ()));
				return true;
			}
			return false;
		}
		
		else if (commandString.equals("/waypoints") && senderIsPlayer) {
			Map<String, Location> wayPoints = playerInfo.getWayPoints();
			if (wayPoints.size() == 0) {
				player.sendMessage(ChatColor.YELLOW + "You haven't set any waypoint.\nUse the //setwaypoint command to set a waypoint.");
				return true;
			}
			player.sendMessage(ChatColor.YELLOW + "---Here's a list of your waypoint(s)---\n");
			for (String id : wayPoints.keySet()) {
				Location wayPoint = wayPoints.get(id);
				player.sendMessage(ChatColor.YELLOW + id + " : "
						+ wayPoint.getBlockX() + ", " + wayPoint.getBlockY() + ", " + wayPoint.getBlockZ());
			}
			return true;
		}
		
		else if (commandString.equals("/setwaypoint") && senderIsPlayer) {
			if (args.length == 1) {
				String id = args[0];
				Location loc = player.getLocation();
				playerInfo.putWayPoint(id, loc);
				player.sendMessage(ChatColor.GREEN + "Waypoint set.\n" + id + " : "
						+ loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
				return true;
			}
			return false;
		}

		else if (commandString.equals("/delwaypoint") && senderIsPlayer) {
			if (args.length == 1) {
				String id = args[0];
				Location wayPoint = playerInfo.delWayPoint(id);
				//if there is no such waypoint
				if (wayPoint == null) {
					player.sendMessage(ChatColor.RED + "No such waypoint: " + id
							+ ". Please confirm your waypoint id is correct.\n"
							+ "You can see a list of all your waypoints with //waypoints command.");
					return true;
				}
				player.sendMessage(ChatColor.YELLOW + "Way point \"" + id + "\" deleted.");
				return true;
			}
			return false;
		}
		
		else if (commandString.equals("/version")) {
			player.sendMessage(verString);
			return true;
		}
		
		else if (commandString.equals("stop")) {
			if ((senderIsPlayer)) {
				sender.sendMessage(ChatColor.RED + "You do not have the permission to stop the server. Stop command is only allowed in the console.");
				return true;
			}
			else
				server.shutdown();
		}
		
		//in the end, update playerInfo to playerInfos
		if (senderIsPlayer)
			playerInfos.put(playerID, playerInfo);
		
		return false;
	}

	//the scan method, a tool used in the "broadcast" command
	private int scan(String input, String pattern) {
		for (int i=0; i<=input.length()-pattern.length(); i++) {
			if (input.substring(i, i + pattern.length()).equals(pattern))
				return i+pattern.length();
		}
		return -1;
	}
	
	/* PLAYER INFO FORMAT
	 * 01 username
	 * 02 password
	 * 03 gamemode
	 * 04 home location (world name, X, Y, Z)
	 * 05+ way points
	 */
	private void readPlayerInfo() throws IOException {
		PlayerInfoContainer playerInfo = new PlayerInfoContainer();
		//first line, the username
		String name = fileReader.readLine();
		
		//BUT! before we move on, check if the file is empty
		//if empty, finish the method and move on.
		if (name == null)
			return;
		
		name = enigma.encode(name);
		//second line, password
		String password = enigma.encode(fileReader.readLine());
		//third line, game mode
		GameMode gamemode = GameMode.getByValue(Integer.valueOf(enigma.encode(fileReader.readLine())));
		//fourth+ lines, locations
		//home location on the fourth
		String homeLocString = enigma.encode(fileReader.readLine());
		Location homeLoc;
		if (homeLocString.equals("null"))
			homeLoc = null;
		else {
			homeLoc = parseLocation(homeLocString);
		}
		//fifth+ lines, way points
		String wayPointString;
		while ((wayPointString = fileReader.readLine()) != null) {
			wayPointString = enigma.encode(wayPointString);
			String[] wayPointArray = wayPointString.split(" ");
			Location wayPoint = parseLocation(wayPointString);
			playerInfo.putWayPoint(wayPointArray[wayPointArray.length-1], wayPoint);
		}
		if (password.equals("null")) {
		}
		else {
			playerInfo.setUserName(name);
			playerInfo.setPassword(password);
			playerInfo.setHomeLoc(homeLoc);
			playerInfo.setGamemode(gamemode);
			playerInfos.put(name, playerInfo);
			registeredPlayers.add(name);
		}
	}
	
	private void writePlayerInfo(PlayerInfoContainer playerInfo) throws IOException {
		//username
		fileWriter.write(enigma.encode(playerInfo.getUserName()) + "\n");
		//password
		String password = playerInfo.getPassword();
		if (password == null)
			fileWriter.write(enigma.encode("null") + "\n");
		else
			fileWriter.write(enigma.encode(playerInfo.getPassword()) + "\n");
		//game mode
		fileWriter.write(enigma.encode(Integer.toString(playerInfo.getGamemode().getValue())) + "\n");
		//home location
		Location homeLoc = playerInfo.getHomeLoc();
		if (homeLoc == null)
			fileWriter.write(enigma.encode("null") + "\n");
		else
			fileWriter.write(enigma.encode(
				homeLoc.getWorld().getName() + " " 
				+ homeLoc.getX() + " " 
				+ homeLoc.getY() + " " 
				+ homeLoc.getZ()) + "\n");
		//waypoints
		for (String locID : playerInfo.getWayPoints().keySet()) {
			Location loc = playerInfo.getWayPoint(locID);
			fileWriter.write(enigma.encode(
				loc.getWorld().getName() + " " 
				+ loc.getX() + " " 
				+ loc.getY() + " " 
				+ loc.getZ() + " " 
				+ locID) + "\n");
		}
	}
	
	//intended location string format:
	//*world name* *x coordinate* *y coordinate* *z coordinate*
	
	//TODO deal with the waypoint format
	private Location parseLocation(String locString) throws IndexOutOfBoundsException {
		String[] locStringArray = locString.split(" ");
		//parse the home location string array to Location object
		Location loc = new Location(server.getWorld(locStringArray[0]), 0d, 0d, 0d);
		loc.setX(Double.valueOf(locStringArray[1]));
		loc.setY(Double.valueOf(locStringArray[2]));
		loc.setZ(Double.valueOf(locStringArray[3]));
		return loc;
	}

	//Event handlers
	@EventHandler
	public void onJoin(PlayerJoinEvent event) 
    {
		event.setJoinMessage("");
		Player player = event.getPlayer();
		String playerName = player.getName();
		ec.freezedPlayers.add(playerName);
		player.setGameMode(GameMode.CREATIVE);
		if (!registeredPlayers.contains(playerName)) {
			//TODO tell the player to register itself
			for (int i = 0; i < 5; i++) {
				player.sendMessage(ChatColor.DARK_GREEN + "Please use //register to login.");
			}
//			scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
//				@Override
//				public void run() {
//					event.getPlayer().sendMessage(ChatColor.DARK_GREEN 
//						+ "Please use //register to register.");
//				}
//	        }, 0l, 50);
		}
		else {
			//TODO tell the player to login
			for (int i = 0; i < 5; i++) {
				player.sendMessage(ChatColor.DARK_GREEN + "Please use //login to login.");
			}
		}
		
		if (playerName.equals("cxfredeper"))
			player.sendMessage(ChatColor.YELLOW + "Hi cxfredeper, this server is running your plugin!");
		else 
			player.sendMessage(ChatColor.YELLOW + "Welcome " + playerName + "!"
				+ " This server is running cxfredeper's plugin!");
    }
	
	@EventHandler
	public void onLogout(PlayerQuitEvent event)
	{
		String playerID = event.getPlayer().getName();
		event.setQuitMessage("");
		logedinPlayers.remove(playerID);
		ec.freezedPlayers.remove(playerID);
		PlayerInfoContainer playerInfo = playerInfos.get(playerID);
		if (playerInfo != null) {
			playerInfo.setGamemode(event.getPlayer().getGameMode());
			playerInfos.put(playerID, playerInfo);
		}
	}
	
	@EventHandler
	public void onPlayerDeathEvent(EntityDeathEvent e) {
		Player placeHolderPlayer;
		if (e.getEntity() instanceof Player) {
			placeHolderPlayer = (Player) e.getEntity();
			tpLocs.put(placeHolderPlayer.getName(), placeHolderPlayer.getLocation());
		}
	}
}
