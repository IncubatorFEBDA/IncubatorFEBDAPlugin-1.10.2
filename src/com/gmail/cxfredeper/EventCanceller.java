package com.gmail.cxfredeper;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;

public class EventCanceller extends Event implements Listener {
	private static HandlerList handlerList = new HandlerList();
	public List<String> freezedPlayers = new ArrayList<String>();

	@EventHandler
	private void asyncPlayerChatEvent(AsyncPlayerChatEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void inventoryOpenEvent(InventoryOpenEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerAchievementAwardedEvent(PlayerAchievementAwardedEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerAnimationEvent(PlayerAnimationEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerBedEnterEvent(PlayerBedEnterEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerDropItemEvent(PlayerDropItemEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerEditBookEvent(PlayerEditBookEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerFishEvent(PlayerFishEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
/*	
	@EventHandler
	private void playerGameModeChangeEvent(PlayerGameModeChangeEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	*/
	@EventHandler
	private void playerInteractEntityEvent(PlayerInteractEntityEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerInteractEvent(PlayerInteractEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }

	@EventHandler
	private void playerItemConsumeEvent(PlayerItemConsumeEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerItemHeldEvent(PlayerItemHeldEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerKickEvent(PlayerKickEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }

	@EventHandler
	private void playerMoveEvent(PlayerMoveEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true);}
	
	@EventHandler
	private void playerPickupItemEvent(PlayerPickupItemEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }

	@EventHandler
	private void playerShearEntityEvent(PlayerShearEntityEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerStatisticIncrementEvent(PlayerStatisticIncrementEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerToggleFlightEvent(PlayerToggleFlightEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerToggleSneakEvent(PlayerToggleSneakEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerToggleSprintEvent(PlayerToggleSprintEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }
	
	@EventHandler
	private void playerVelocityEvent(PlayerVelocityEvent e) { if (freezedPlayers.contains(e.getPlayer().getName())) e.setCancelled(true); }

	@Override
	public HandlerList getHandlers() {
		return handlerList;
	}
	
	public static HandlerList getHandlerList() {
		return handlerList ;
	}
}
