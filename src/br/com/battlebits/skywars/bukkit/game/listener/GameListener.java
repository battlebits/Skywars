package br.com.battlebits.skywars.bukkit.game.listener;

import br.com.battlebits.skywars.bukkit.game.Engine;
import br.com.battlebits.skywars.bukkit.game.GameStage;
import org.bson.Document;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
import org.bukkit.event.entity.*;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.bukkit.inventory.ItemStack;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;

import br.com.battlebits.commons.api.actionbar.ActionBarAPI;
import br.com.battlebits.commons.bukkit.BukkitMain;
import br.com.battlebits.commons.core.account.BattlePlayer;
import br.com.battlebits.commons.core.translate.T;
import br.com.battlebits.skywars.bukkit.Main;
import br.com.battlebits.skywars.bukkit.data.mongodb.MongoBackend;
import br.com.battlebits.skywars.bukkit.data.PlayerData;
import br.com.battlebits.skywars.bukkit.data.PlayerManager;
import br.com.battlebits.skywars.bukkit.game.kits.Kit;
import br.com.battlebits.skywars.bukkit.game.task.DeathTask;
import br.com.battlebits.skywars.bukkit.utils.Combat;
import br.com.battlebits.skywars.bukkit.utils.Utils;

import static br.com.battlebits.commons.BattlebitsAPI.getGson;

public class GameListener implements Listener {
    private Engine engine;

    public GameListener(Engine engine) {
        this.engine = engine;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        PlayerData data = null;

        MongoBackend backend = Main.getInstance().getMongoBackend();
        MongoDatabase database = backend.getClient().getDatabase("skywars");
        MongoCollection<Document> collection = database.getCollection("data");
        Document document = collection.find(Filters.eq("uuid", event.getUniqueId().toString())).first();

        if (document != null) {
            String json = getGson().toJson(document);
            data = getGson().fromJson(json, PlayerData.class);
        } else if (document == null) {
            data = new PlayerData(event.getUniqueId(), event.getName());
            document = Document.parse(getGson().toJson(data));
            collection.insertOne(document);
        }

        Main.getInstance().getPlayerManager().add(data);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Player player = event.getPlayer();

        PlayerData pd = Main.getInstance().getPlayerManager().get(player);
        if (pd != null)
            pd.onJoin(player);

        if (!pd.hasItem("Kit", "Fisherman")) {
            pd.addItem("Kit", "Fisherman");
        }

        if (!pd.hasItem("Kit", "Teleporter")) {
            pd.addItem("Kit", "Teleporter");
        }

        if (!pd.hasItem("Kit", "Archer")) {
            pd.addItem("Kit", "Archer");
        }

        switch (engine.getStage()) {
            case PREGAME: {
                engine.addPlayer(player);
                Utils.clearInventory(player);
                Utils.addPlayerItems(player);
                player.teleport(engine.getMap().getSpawn("lobby"));
                BukkitMain.broadcastMessage("skywars-player-join", new String[]{"%playerName%", "%playerCount%"}, new String[]{player.getDisplayName(), "(" + engine.getPlayers().size() + "/" + Bukkit.getMaxPlayers() + ")"});
                break;
            }

            default: {
                Utils.clearInventory(player);
                Utils.addSpectatorItems(player);
                player.setGameMode(GameMode.ADVENTURE);
                player.teleport(engine.getMap().getSpawn("spectators"));
                player.getActivePotionEffects().forEach(v -> player.removePotionEffect(v.getType()));
                player.setAllowFlight(true);
                player.setFlying(true);
                player.setFireTicks(0);
                player.setFoodLevel(20);
                player.setLevel(0);
                player.setExp(0F);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Player player = event.getPlayer();

        if (engine.contains(player)) {
            BukkitMain.broadcastMessage("skywars-player-leave", new String[]{"%playerName%", player.getName()});
        }

        PlayerData pd = Main.getInstance().getPlayerManager().remove(player);

        switch (engine.getStage()) {
            case PREPARING: {
                engine.removePlayer(player);
                engine.checkCount();
                break;
            }

            case INGAME: {
                if (pd != null && engine.contains(player)) {
                    Combat combat = pd.getCombat();
                    if (combat != null && combat.isValid()) {
                        player.setHealth(0D);
                    } else {
                        pd.addTimePlayed();
                        pd.executeUpdate();
                    }
                }

                engine.removePlayer(player);
                engine.checkCount();
                break;
            }

            default: {
                engine.removePlayer(player);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (cancelEvent(player)) {
            event.setCancelled(true);
            player.updateInventory();
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEntityEvent event) {
        switch (engine.getStage()) {
            case INGAME: {
                Player player = event.getPlayer();
                if (!engine.contains(player)) {
                    event.setCancelled(true);
                    if (event.getRightClicked() instanceof Player) {
                        player.setGameMode(GameMode.SPECTATOR);
                        player.setSpectatorTarget(event.getRightClicked());
                        player.setSneaking(false);
                    }
                }
                break;
            }

            default: {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHangingBreakByEntity(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            Player remover = (Player) event.getRemover();
            event.setCancelled(cancelEvent(remover));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onHangingPlace(HangingPlaceEvent event) {
        event.setCancelled(cancelEvent(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        switch (engine.getStage()) {
            case INGAME: {
                Player player = event.getPlayer();
                if (engine.contains(player)) {
                    ItemStack item = event.getItemDrop().getItemStack();
                    if (Kit.isUndroppable(item)) {
                        event.setCancelled(true);
                        player.updateInventory();
                    }
                } else {
                    event.setCancelled(true);
                }
                break;
            }

            default: {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        event.setCancelled(cancelEvent(event.getPlayer()));
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (engine.getStage() == GameStage.INGAME) {
            if (engine.contains(player)) {
                int size = engine.getPlayers().size();

                new DeathTask(engine, event);
                engine.removePlayer(player);
                engine.checkCount();

                event.getDrops().removeIf(v -> Kit.isUndroppable(v));
                EntityDamageEvent lastDamage = player.getLastDamageCause();

                PlayerData victimData = Main.getInstance().getPlayerManager().get(player);
                victimData.addDeath();
                victimData.addTimePlayed();
                victimData.executeUpdate();

                Combat combat = victimData.getCombat();

                if (combat != null && combat.isValid()) {
                    Player killer = combat.getDamager();

                    if (lastDamage.getCause() == DamageCause.VOID) {
                        BukkitMain.broadcastMessage("skywars-death-by-killer-void", new String[]{"%playerName%", "%killerName%"}, new String[]{player.getName(), killer.getName()});
                    } else if (lastDamage.getCause() == DamageCause.PROJECTILE) {
                        if (lastDamage instanceof EntityDamageByEntityEvent) {
                            Entity damager = ((EntityDamageByEntityEvent) lastDamage).getDamager();
                            if (damager instanceof Arrow) {
                                double y1 = damager.getLocation().getY();
                                double y2 = player.getLocation().getY();
                                boolean headshot = y1 - y2 > 1.35D;
                                BukkitMain.broadcastMessage("skywars-death-by-arrow" + (headshot ? "-headshot" : ""), new String[]{"%playerName%", "%killerName%"}, new String[]{player.getName(), killer.getName()});
                            }
                        }
                    } else {
                        BukkitMain.broadcastMessage("skywars-death-by-player", new String[]{"%playerName%", "%killerName%"}, new String[]{player.getName(), killer.getName()});
                    }

                    PlayerData killerData = Main.getInstance().getPlayerManager().get(killer);
                    killerData.addKill();
                    killerData.executeUpdate();
                } else if (lastDamage.getCause() == DamageCause.VOID) {
                    BukkitMain.broadcastMessage("skywars-death-by-void", new String[]{"%playerName%", player.getName()});
                } else if (lastDamage.getCause() == DamageCause.ENTITY_EXPLOSION) {
                    BukkitMain.broadcastMessage("sskywars-death-by-explosion", new String[]{"%playerName%", player.getName()});
                } else if (lastDamage.getCause() == DamageCause.BLOCK_EXPLOSION) {
                    BukkitMain.broadcastMessage("skywars-death-by-explosion", new String[]{"%playerName%", player.getName()});
                } else if (lastDamage.getCause() == DamageCause.STARVATION) {
                    BukkitMain.broadcastMessage("skywars-death-by-hunger", new String[]{"%playerName%", player.getName()});
                } else {
                    BukkitMain.broadcastMessage("skywars-death-unknown", new String[]{"%playerName%", player.getName()});
                }

                size--;

                if (size > 1) {
                    for (Player other : Bukkit.getOnlinePlayers()) {
                        ActionBarAPI.send(other, T.t(BattlePlayer.getLanguage(player.getUniqueId()), "skywars-players-remaining-actionbar", new String[]{"%playerCount%", Integer.toString(size)}));
                    }
                }
            }
        }

        event.setDeathMessage(null);
        event.setDroppedExp(0);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (engine.getStage() != GameStage.INGAME) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (engine.getStage() != GameStage.INGAME) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamage(EntityDamageEvent event) {
        switch (engine.getStage()) {
            case PREGAME: {
                event.setCancelled(true);
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (event.getCause() == DamageCause.VOID) {
                        player.teleport(engine.getMap().getSpawn("lobby"));
                    }
                }
                break;
            }

            case INGAME: {
                if (event.getEntity() instanceof Player) {
                    Player player = (Player) event.getEntity();
                    if (!engine.contains(player)) {
                        event.setCancelled(true);
                    } else if (event.getCause() == DamageCause.FALL) {
                        if (engine.getSchedule().getTime() < 3) {
                            event.setCancelled(true);
                        }
                    } else if (event.getCause() == DamageCause.VOID) {
                        event.setDamage(100D);
                    }
                }
                break;
            }

            default: {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();
        if (event.isSneaking() && !engine.contains(player) && engine.getStage() != GameStage.PREGAME) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                player.setSpectatorTarget(player);
                player.setGameMode(GameMode.ADVENTURE);
                player.setAllowFlight(true);
                player.setFlying(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player) {
            Player damaged = (Player) event.getEntity();
            switch (engine.getStage()) {
                case INGAME: {
                    if (!engine.contains(damaged)) {
                        event.setCancelled(true);

                        // TODO: Fix arrow bug
                    } else {
                        Player damager = null;
                        boolean arrow = false;

                        if (event.getDamager() instanceof Player) {
                            damager = (Player) event.getDamager();
                        } else if (event.getDamager() instanceof Projectile) {
                            Projectile proj = (Projectile) event.getDamager();
                            if (proj.getShooter() instanceof Player) {
                                damager = (Player) proj.getShooter();
                                arrow = (proj instanceof Arrow);
                            }
                        }

                        if (damager != null) {
                            if (engine.contains(damager)) {
                                int pid = engine.getIsland(damager);
                                int tid = engine.getIsland(damaged);

                                if (pid > 0 && tid > 0 && pid != tid) {
                                    if (arrow) {
                                        double health;

                                        if ((health = Math.round((damager.getHealth() - event.getFinalDamage()) * 100.0D)) / 100.0D > 0D) {
                                            damager.sendMessage(T.t(BattlePlayer.getLanguage(damager.getUniqueId()), "skywars-player-health-arrow-hit", new String[]{"%health%", Double.toString(health)}));
                                        }
                                    }

                                    double damage = 1D;
                                    ItemStack item = damager.getItemInHand();

                                    if (item != null) {
                                        switch (item.getType()) {
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

                                        if (item.containsEnchantment(Enchantment.DAMAGE_ALL)) {
                                            damage += item.getEnchantmentLevel(Enchantment.DAMAGE_ALL);
                                        }
                                    }

                                    if (!((Entity) damager).isOnGround() && damager.getVelocity().getY() < 0D) {
                                        damage += 1D;
                                    }

                                    event.setDamage(damage);

                                    PlayerManager manager = Main.getInstance().getPlayerManager();
                                    Combat combat = manager.get(damaged).getCombat();
                                    combat.setDamager(damager);
                                    combat.setExpire(10000L);
                                } else {
                                    event.setCancelled(true);
                                }
                            } else {
                                event.setCancelled(true);
                                damager.setGameMode(GameMode.SPECTATOR);
                                damager.setSpectatorTarget(damaged);
                                damager.setSneaking(false);
                            }
                        }
                    }
                    break;
                }

                default: {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            switch (engine.getStage()) {
                case INGAME: {
                    if (engine.contains(player)) {
                        if (event.getFoodLevel() < player.getFoodLevel()) {
                            if (Utils.RANDOM.nextInt(10000) > 33 * 100) {
                                event.setCancelled(true);
                            }
                        }
                    } else {
                        event.setCancelled(true);
                        player.setFoodLevel(20);
                    }
                    break;
                }

                default: {
                    event.setCancelled(true);
                    player.setFoodLevel(20);
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPotionSplash(PotionSplashEvent event) {
        switch (engine.getStage()) {
            case INGAME: {
                for (LivingEntity entity : event.getAffectedEntities()) {
                    if (entity instanceof Player) {
                        Player player = (Player) entity;
                        if (!engine.contains(player)) {
                            event.setIntensity(entity, 0D);
                        }
                    }
                }
                break;
            }

            default: {
                event.setCancelled(true);
                break;
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onBlockCanBuild(BlockCanBuildEvent event) {
        event.setBuildable(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        event.setCancelled(cancelEvent(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        event.setCancelled(cancelEvent(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockIgnite(BlockIgniteEvent event) {
        event.setCancelled(cancelEvent(event.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBurn(BlockBurnEvent event) {
        if (engine.getStage() != GameStage.INGAME) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockFade(BlockFadeEvent event) {
        if (engine.getStage() != GameStage.INGAME) {
            Block block = event.getBlock();

            if (block.getType() == Material.ICE) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onWeatherChange(WeatherChangeEvent event) {
        if (event.toWeatherState()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            event.setCancelled(cancelEvent(target));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityTargetLivingEntity(EntityTargetLivingEntityEvent event) {
        if (event.getTarget() instanceof Player) {
            Player target = (Player) event.getTarget();
            event.setCancelled(cancelEvent(target));
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    private boolean cancelEvent(Player player) {
        return engine.getStage() != GameStage.INGAME || !engine.contains(player);
    }
}
