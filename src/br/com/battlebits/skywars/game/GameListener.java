package br.com.battlebits.skywars.game;

import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockCanBuildEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;

import com.google.gson.Gson;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.BattlebitsAPI;
import br.com.battlebits.commons.api.actionbar.ActionBarAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.core.translate.Language;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.Main;
import br.com.battlebits.skywars.data.MongoBackend;
import br.com.battlebits.skywars.data.PlayerData;
import br.com.battlebits.skywars.data.PlayerManager;
import br.com.battlebits.skywars.utils.Combat;
import br.com.battlebits.skywars.utils.Utils;

public class GameListener implements Listener {
	
	private Engine engine;
	
	public GameListener(Engine engine)
	{
		this.engine = engine;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event)
	{
		PlayerData data = null;		
		Gson gson = BattlebitsAPI.getGson();
		
		MongoBackend backend = Main.getInstance().getMongoBackend();
		MongoDatabase database = backend.getClient().getDatabase("skywars");
		MongoCollection<Document> collection = database.getCollection("data");
		
		Document document = collection.find(Filters.eq("uuid", event.getUniqueId().toString())).first();
		
		if (document != null)
		{
			data = gson.fromJson(gson.toJson(document), PlayerData.class);
		}
		else if (document == null)
		{
			data = new PlayerData(event.getUniqueId(), event.getName());
			document = Document.parse(gson.toJson(data));
			collection.insertOne(document);
		}
		
		Main.getInstance().getPlayerManager().add(data);
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) 
	{
		event.setJoinMessage(null);
		
		Player player = event.getPlayer();
	
		PlayerData data = Main.getInstance().getPlayerManager().get(player);
		if (data != null) data.onJoin(player);
		
		switch (engine.getStage())
		{
		    case PREGAME:
		    {
		    	engine.addPlayer(player);
		    	
		    	player.getInventory().clear();
		    	player.getInventory().setArmorContents(new ItemStack[4]);
		    	player.teleport(engine.getMap().getSpawn("lobby"));	
		    	player.updateInventory();
		    	
		    	BukkitMain.broadcastMessage("sw_player_join", new String[] {"%player%", "%size%"}, new String[] {player.getName(), "(" + engine.getPlayers().size() + "/" + Bukkit.getMaxPlayers() + ")"});
		    	break;
		    }
	
		    default:
		    {
		    	player.getActivePotionEffects().forEach(v -> player.removePotionEffect(v.getType()));
		    	player.teleport(engine.getMap().getSpawn("spectators"));
		    	player.getInventory().setArmorContents(new ItemStack[4]);
		    	player.getInventory().clear();
		    	player.updateInventory();
		    	break;
		    }
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		event.setQuitMessage(null);
		
		Player player = event.getPlayer();
		
		if (engine.contains(player))
			BukkitMain.broadcastMessage("sw_player_quit", new String[] {"%player%", player.getName()});
		
		PlayerData data = Main.getInstance().getPlayerManager().remove(player);
		
		switch (engine.getStage())
		{
		    case PREPARING:
		    {
		    	engine.removePlayer(player);
		    	engine.checkCount();
		    	break;
		    }
		    
		    case INGAME:
		    {
		    	if (engine.contains(player))
		    	{
		    		Combat combat = data.getCombat();
			    	
			    	if (combat != null && !combat.isExpired()) 
			    	{
			    		player.setHealth(0D);
			    	}
			    	else
			    	{
			    		data.addTimePlayed();
				    	data.update();
			    	}
		    	}
		    	
		    	engine.removePlayer(player);
		    	engine.checkCount();
		    	
		    	break;
		    }
		    
		    default:
		    {
		    	engine.removePlayer(player);
		    	break;
		    }
		}
	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

	}
	
	@EventHandler
	public void onPlayerInteract(PlayerInteractEntityEvent event)
	{
		
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event)
	{
		Player player = event.getEntity();
		
		if (engine.getStage() == GameStage.INGAME)
		{
			if (engine.contains(player))
			{
				int size = engine.getPlayers().size();
				
				engine.removePlayer(player);
				engine.checkCount();
				
				//event.getDrops().removeIf(v -> Kit.is)
				
				EntityDamageEvent lastDamage = player.getLastDamageCause();
				PlayerManager manager = Main.getInstance().getPlayerManager();
				PlayerData data = manager.get(player);
				
				if (data != null) 
				{
					data.addTimePlayed();
					data.addDeath();
					data.update();
					
					Combat combat = data.getCombat();
					
					if (combat != null && !combat.isExpired())
					{
						Player killer = combat.getDamager();
						
						if (lastDamage.getCause() == DamageCause.VOID)
						{
							BukkitMain.broadcastMessage("sw_death_by_killer_void", new String[] {"%player%", "%killer%"}, new String[] {player.getName(), killer.getName()});
						}
						else if (lastDamage.getCause() == DamageCause.PROJECTILE)
						{
							if (lastDamage instanceof EntityDamageByEntityEvent)
							{
								Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();
								
								if (damager instanceof Arrow)
								{
									double y1 = damager.getLocation().getY();
									double y2 = player.getLocation().getY();
                                    boolean headshot = y1 - y2 > 1.35D;

                                    BukkitMain.broadcastMessage("sw_death_by_arrow" + (headshot ? "_headshot" : ""), new String[] {"%player%", "%killer%"}, new String[] {player.getName(), killer.getName()});
								}
							}
						}
						else
						{
							BukkitMain.broadcastMessage("sw_death_by_killer", new String[] {"%player%", "%killer%"}, new String[] {player.getName(), killer.getName()});
						}
						
						data = manager.get(killer);
						data.addKill();
						data.update();
					}
					else if (lastDamage.getCause() == DamageCause.VOID)
                    {
						BukkitMain.broadcastMessage("sw_death_by_void", new String[] {"%player%", player.getName()});							
                    }
                    else if (lastDamage.getCause() == DamageCause.ENTITY_EXPLOSION)
                    {
						BukkitMain.broadcastMessage("sw_death_by_explosion", new String[] {"%player%", player.getName()});	                    	
                    }
                    else if (lastDamage.getCause() == DamageCause.BLOCK_EXPLOSION)
                    {
						BukkitMain.broadcastMessage("sw_death_by_explosion", new String[] {"%player%", player.getName()});	                    	
                    }
                    else if (lastDamage.getCause() == DamageCause.STARVATION)
                    {
						BukkitMain.broadcastMessage("sw_death_by_hunger", new String[] {"%player%", player.getName()});	                    	
                    }
                    else
                    {
						BukkitMain.broadcastMessage("sw_death", new String[] {"%player%", player.getName()});
                    }
				}

				size--;
				
				if (size > 1)
				{
					for (Player other : Bukkit.getOnlinePlayers())
					{
						Language language = Utils.getLanguage(player);
						
						ActionBarAPI.send(other, T.t(language, "actionbar_players_remaining", new String[] {"%size%", Integer.toString(size)}));					
					}					
				}
			}
		}
		
		event.setDeathMessage(null);
		event.setDroppedExp(0);
	}
	
	@EventHandler(ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event)
    {
        if (engine.getStage() != GameStage.INGAME)
        {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event)
    {
        if (engine.getStage() != GameStage.INGAME)
        {
            event.setCancelled(true);
        }
    }
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamage(EntityDamageEvent event)
	{
		switch (engine.getStage())
		{
		    case PREGAME:
		    {
		    	event.setCancelled(true);
		    	
		    	if (event.getEntity() instanceof Player)
		    	{
		    		Player player = (Player) event.getEntity();
		    		
		    		if (event.getCause() == DamageCause.FALL)
		    		{
		    			player.teleport(engine.getMap().getSpawn("lobby"));
		    		}
		    	}
		    	
		    	break;
		    }
		    
		    case INGAME:
		    {
		    	if (event.getEntity() instanceof Player)
		    	{
		    		Player player = (Player) event.getEntity();
		    		
		    		int time = engine.getSchedule().getTime();
		    		
		    		if (event.getCause() == DamageCause.FALL)
		    		{
		    			event.setCancelled(time <= 3);
		    			
		    			if (!engine.contains(player))
		    			{
		    				event.setCancelled(true);
		    			}
		    		}
		    		else if (!engine.contains(player))
		    		{
		    			event.setCancelled(true);
		    		}
		    	}
		    	
		    	break;
		    }
		    
		    default:
		    {
		    	event.setCancelled(true);
		    	break;
		    }
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDamageByEntity(EntityDamageByEntityEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player damaged = (Player) event.getEntity();
			
			switch (engine.getStage())
			{
			    case INGAME:
			    {
			    	if (!engine.contains(damaged))
			    	{
			    		event.setCancelled(true);
			    		
			    		// Spectator
			    	}
			    	else
			    	{
			    		Player damager = null;
			    		
			    		boolean arrow = false;
			    		
			    		if (event.getDamager() instanceof Player)
			    		{
			    			damager = (Player) event.getDamager();
			    		}
			    		else if (event.getDamager() instanceof Projectile)
			    		{
			    			Projectile proj = (Projectile) event.getDamager();
			    			
			    			if (proj.getShooter() instanceof Player)
			    			{
			    				damager = (Player) proj.getShooter();
			    				
			    				arrow = (proj instanceof Arrow);
			    			}
			    		}
			    		
			    		if (damager != null)
			    		{
			    			if (engine.contains(damager))
			    			{
			    				int t1 = engine.getIsland(damager);
			    				int t2 = engine.getIsland(damaged);
			    				
			    				if (t1 > 0 && t2 > 0 && t1 != t2)
			    				{
			    					if (arrow)
			    					{
			    						double health = 0D;
			    						double damage = 1D;
			    				
			    						if ((health = Math.round((damager.getHealth() - event.getFinalDamage()) * 100.0D)) / 100.0D > 0.0D)
			    						{
			    							damager.sendMessage(T.t(Utils.getLanguage(damager), "sw_arrow_health", new String[] {"%health%", Double.toString(health)}));
			    						}
			    						
			    						ItemStack item = damager.getItemInHand();
			    						
			    						if (item != null)
			    						{
			    							switch (item.getType()) 
			    							{
											    case WOOD_SWORD:
											    case GOLD_SWORD:
											    	damage = 3D;
											    	break;
											    case STONE_SWORD:
											    	damage = 4D;
											    	break;
											    case IRON_SWORD:
											    	damage = 5D;
											    	break;
											    case DIAMOND_SWORD:
											    	damage = 6D;
											    	break;
											    case WOOD_AXE:
											    case WOOD_HOE:
											    case WOOD_SPADE:
											    case WOOD_PICKAXE:
											    case GOLD_AXE:
											    case GOLD_HOE:
											    case GOLD_SPADE:
											    case GOLD_PICKAXE:
											    	damage = 2D;
											    	break;
											    case STONE_AXE:
											    case STONE_HOE:
											    case STONE_SPADE:
											    case STONE_PICKAXE:
											    	damage = 3D;
											    	break;
											    case IRON_AXE:
											    case IRON_HOE:
											    case IRON_SPADE:
											    case IRON_PICKAXE:
											    	damage = 4D;
											    	break;
											    case DIAMOND_AXE:
											    case DIAMOND_HOE:
											    case DIAMOND_SPADE:
											    case DIAMOND_PICKAXE:
											    	damage = 5D;
											    	break;
											    default:
												    break;
											}
			    							
			    							if (item.containsEnchantment(Enchantment.DAMAGE_ALL))
			    							{
			    								damage += item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
			    							}
			    						}
			    						
			    						if (!((Entity) damager).isOnGround() && damager.getVelocity().getY() < 0D)
			    						{
			    							damage += 1D;
			    						}
			    						
			    						event.setDamage(damage);
			    						
			    						PlayerManager manager = Main.getInstance().getPlayerManager();
			    						Combat combat = manager.get(damaged).getCombat();
			    						combat.setDamager(damager);
			    						combat.setExpire(10000L);
			    					}
			    				}
			    				else
			    				{
			    					event.setCancelled(true);
			    				}
			    			}
			    			else
			    			{
			    				event.setCancelled(true);
			    			}
			    		}
			    	}
			    	
			    	break;
			    }
			    
			    default:
			    {
			    	event.setCancelled(true);
			    	break;
			    }
			}
		}
	}
	
	@EventHandler
	public void onFoodLevelChange(FoodLevelChangeEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			Player player = (Player) event.getEntity();
			
			switch (engine.getStage()) 
			{
			    case INGAME:
			    {
			    	if (engine.contains(player))
			    	{
			    		if (event.getFoodLevel() < player.getFoodLevel())
			    		{
			    			if (Utils.RANDOM.nextInt(10000) > 33 * 100)
			    			{
			    				event.setCancelled(true);
			    			}
			    		}
			    	}
			    	else
			    	{
			    		event.setCancelled(true);
			    		player.setFoodLevel(20);
			    	}
			    	
			    	break;
			    }
			    
			    default:
			    {
			    	event.setCancelled(true);
		    		player.setFoodLevel(20);
			    	break;
			    }
			}
		}
	}
	
	@EventHandler
	public void onPotionSplash(PotionSplashEvent event)
	{
		switch (engine.getStage()) 
		{
		    case INGAME:
		    {
		    	for (LivingEntity entity : event.getAffectedEntities())
		    	{
		    		if (entity instanceof Player)
		    		{
		    			Player player = (Player) entity;
		    			
		    			if (!engine.contains(player))
		    			{
		    				event.setIntensity(entity, 0D);
		    			}
		    		}
		    	}
		    	
		    	break;
		    }
		    
		    default:
		    {
		    	event.setCancelled(true);
		    	break;
		    }
		}
	}
	
	@EventHandler
	public void onBlockCanBuild(BlockCanBuildEvent event)
	{
		event.setBuildable(true);
	}
	
	@EventHandler
	public void onBlockPlace(BlockPlaceEvent event)
	{
		switch (engine.getStage())
		{
		    case INGAME:
		    {
		    	Player player = event.getPlayer();
		    	
		    	if (!engine.contains(player))
		    	{
		    		event.setCancelled(true);
		    	}
		    	
		    	break;
		    }
		    
		    default:
		    {
		    	event.setCancelled(true);
		    	break;
		    }
		}
	}
	
	@EventHandler
	public void onBlockBreak(BlockBreakEvent event)
	{
		switch (engine.getStage())
		{
		    case INGAME:
		    {
		    	Player player = event.getPlayer();
		    	
		    	if (!engine.contains(player))
		    	{
		    		event.setCancelled(true);
		    	}
		    	
		    	break;
		    }
		    
		    default:
		    {
		    	event.setCancelled(true);
		    	break;
		    }
		}
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		switch (engine.getStage())
		{
		    case INGAME:
		    {
		    	Player player = event.getPlayer();
		    	
		    	if (!engine.contains(player))
		    	{
		    		event.setCancelled(true);
		    	}
		    	
		    	break;
		    }
		    
		    default:
		    {
		    	event.setCancelled(true);
		    	break;
		    }
		}
	}
	
	@EventHandler
	public void onBlockBurn(BlockBurnEvent event)
	{
		if (engine.getStage() != GameStage.INGAME)
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBlockFade(BlockFadeEvent event)
	{
		if (engine.getStage() != GameStage.INGAME)
		{
			Block block = event.getBlock();
			
			if (block.getType() == Material.ICE)
			{
				event.setCancelled(true);
			}
		}
	}

	@EventHandler
	public void onWeatherChange(WeatherChangeEvent event) 
	{
		if (event.toWeatherState()) 
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) 
	{
		if (event.getSpawnReason() != SpawnReason.CUSTOM)
		{
			event.setCancelled(true);
		}
	}
}
