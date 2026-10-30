package com.gncraft.shadowkingdoms;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.Projectile;
import org.bukkit.block.data.Rotatable;
import org.bukkit.Particle;
import org.bukkit.block.Skull;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.block.BlockFace;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.attribute.Attribute;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.ChatColor;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.block.Action;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class ShadowKingdoms extends JavaPlugin implements Listener {
    private final Map<String, Kingdom> kingdoms = new LinkedHashMap<>();
    private final Map<String, Nation> nations = new LinkedHashMap<>();
    private final Map<UUID, String> openMenuKingdoms = new HashMap<>();
    private final Map<UUID, String> openNexusChests = new HashMap<>();
    private final Map<String, Inventory> activeNexusChestInventories = new HashMap<>();
    private final Map<UUID, String> lastTerritoryByPlayer = new HashMap<>();
    private final Map<UUID, String> upgradeMenuKingdoms = new HashMap<>();
    private final Map<UUID, String> extraUpgradeMenuKingdoms = new HashMap<>();
    private final Map<String, String> mailMenuIds = new HashMap<>();
    private final Map<UUID, String> composingMailRecipient = new HashMap<>();
    private final Map<UUID, String> composingMailStage = new HashMap<>();
    private final Map<UUID, String> composingMailSubject = new HashMap<>();
    private final Map<UUID, List<ItemStack>> composingMailAttachments = new HashMap<>();
    private final Map<UUID, String> bookMailRecipient = new HashMap<>();
    private final Map<UUID, String> bookMailSubject = new HashMap<>();
    private final Set<UUID> confirmedAttachmentMenu = new HashSet<>();
    private final List<MailMessage> mails = new ArrayList<>();
    private long nextMailId = 1L;
    private File dataFile;
    private YamlConfiguration data;
    private static final String MAIN_MENU_TITLE = "§8Nexo del Reino";
    private static final String DETAILS_MENU_TITLE = "§8Información del Reino";
    private static final String MAIL_MENU_TITLE = "§8ShadowMail";
    private static final String MAIL_INBOX_TITLE = "§8Correo recibido";
    private static final String MAIL_SENT_TITLE = "§8Correo enviado";
    private static final String MAIL_DETAILS_PREFIX = "§8Correo #";
    private static final String MAIL_RECIPIENT_TITLE = "§8Elegir reino destinatario";
    private static final String MAIL_ATTACH_TITLE = "§8¿Deseas adjuntar algún ítem?";
    private static final String NEXUS_CHEST_PREFIX = "§8Cofre del Nexo: ";
    private static final String UPGRADES_MENU_TITLE = "§8Mejoras del Reino";
    private static final String EXTRA_UPGRADES_TITLE = "§8Mejoras extra del Reino";
    private final Map<UUID, Integer> flyExitTasks = new HashMap<>();
    private final Map<UUID, GuardSession> guardSessions = new HashMap<>();
    private final Map<String, Integer> nexusBuildTasks = new HashMap<>();
    private static final String MINERALS_MENU_TITLE = "§8Cambiar minerales por PL";
    private static final Map<Material, Double> MINERAL_VALUES = mineralValues();
    private static final String CANNON_MENU_TITLE = "§8Defensas del Reino";
    private static final String CANNON_UPGRADE_TITLE = "§8Administrar Cañón";
    private static final String INFERNO_UPGRADE_TITLE = "§8Administrar Inferno";
    private static final String INFERNO_TEXTURE = "http://textures.minecraft.net/texture/e8e75d95f241ae52207211f0242ada73dc0086c00905ade50a47e17684a7dd3d";
    private static final String CANNON_TEXTURE = "http://textures.minecraft.net/texture/c825f6c2663b9ac45e66aa83150d324558f4533e313ce8eff983e488a83e5ebb";
    private final Map<String, CannonTurret> cannons = new LinkedHashMap<>();
    private final Map<String, List<ArmorStand>> cannonHolograms = new HashMap<>();
    private final Map<UUID, String> cannonUpgradeMenus = new HashMap<>();
    private final Map<String, InfernoTurret> infernos = new LinkedHashMap<>();
    private final Map<String, List<ArmorStand>> infernoHolograms = new HashMap<>();
    private final Map<UUID, String> infernoUpgradeMenus = new HashMap<>();
    private NamespacedKey infernoItemKey;
    // Seguimiento continuo de proyectiles de viento para detectar impactos por trayectoria.
    private final Map<UUID, Location> windChargePreviousLocations = new HashMap<>();
    private final Set<UUID> windChargeHandled = new HashSet<>();
    private NamespacedKey cannonItemKey;

    @Override public void onEnable() {
        dataFile = new File(getDataFolder(), "kingdoms.yml");
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        loadData();
        cleanupInfernoVisualsOnStartup();
        cannonItemKey = new NamespacedKey(this, "cannon_turret");
        infernoItemKey = new NamespacedKey(this, "inferno_turret");
        Bukkit.getScheduler().runTaskTimer(this, this::tickCannons, 5L, 5L);
        Bukkit.getScheduler().runTaskTimer(this, this::tickInfernos, 2L, 2L);
        Bukkit.getScheduler().runTaskTimer(this, this::tickWindChargeCollisions, 1L, 1L);
        Bukkit.getPluginManager().registerEvents(this, this);
        org.bukkit.command.PluginCommand skCommand = getCommand("sk");
        if (skCommand == null) {
            getLogger().severe("El comando /sk no está declarado en plugin.yml. Se deshabilitará ShadowKingdoms.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        skCommand.setExecutor(this);
        skCommand.setTabCompleter((sender, command, alias, args) -> {
            if (args.length == 1) return starts(Arrays.asList("crear", "reclamar", "agregar", "añadir", "invitar", "remover", "promover", "nexo", "cofre", "colocar-nexo", "borrar", "info", "nacion", "pl", "rename", "ayuda"), args[0]);
            if (args.length == 2 && Arrays.asList("agregar","añadir","remover","promover").contains(args[0].toLowerCase(Locale.ROOT))) return Bukkit.getOnlinePlayers().stream().map(Player::getName).filter(n -> n.toLowerCase(Locale.ROOT).startsWith(args[1].toLowerCase(Locale.ROOT))).toList();
            if (args.length == 3 && (args[0].equalsIgnoreCase("añadir") || args[0].equalsIgnoreCase("agregar"))) return starts(Arrays.asList("rey","caballero","peon"), args[2]);
            if (args.length == 2 && args[0].equalsIgnoreCase("nacion")) return starts(Arrays.asList("crear", "invitar", "aceptar", "info"), args[1]);
            if (args.length == 2 && args[0].equalsIgnoreCase("info")) return starts(kingdoms.values().stream().map(k -> ChatColor.stripColor(k.name)).toList(), args[1]);
            if (args.length == 2 && args[0].equalsIgnoreCase("pl")) return starts(Arrays.asList("añadir"), args[1]);
            // /sk pl añadir <reino> <cantidad>: sugiere los reinos y luego cantidades habituales.
            if (args.length == 3 && args[0].equalsIgnoreCase("pl") && args[1].equalsIgnoreCase("añadir")) return starts(kingdoms.values().stream().map(k -> ChatColor.stripColor(k.name)).toList(), args[2]);
            if (args.length == 4 && args[0].equalsIgnoreCase("pl") && args[1].equalsIgnoreCase("añadir")) return starts(Arrays.asList("100", "250", "500", "1000"), args[3]);
            return Collections.emptyList();
        });
        for (Kingdom kingdom : kingdoms.values()) { if(kingdom.nexusBuildingUntil>System.currentTimeMillis()) startNexusConstruction(kingdom,kingdom.nexusBuildingUntil-System.currentTimeMillis()); else if(kingdom.pendingLevel>0){kingdom.level=kingdom.pendingLevel;kingdom.pendingLevel=0;kingdom.nexusBuildingUntil=0;saveData();} else updateNexusHologram(kingdom); }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new ShadowKingdomsExpansion(this).register();
            getLogger().info("Integración PlaceholderAPI activada.");
        }
        getLogger().info("ShadowKingdoms iniciado. Reinos: " + kingdoms.size());
    }

    @Override public void onDisable() { saveData(); for(List<ArmorStand> stands:cannonHolograms.values()) for(ArmorStand stand:stands) if(stand.isValid()) stand.remove(); cannonHolograms.clear(); for(World w:Bukkit.getWorlds()) for(Entity entity:w.getEntities()) if(entity instanceof ArmorStand stand && stand.getScoreboardTags().stream().anyMatch(t->t.startsWith("shadowkingdoms_builder_") || t.startsWith("shadowkingdoms_turret_hitbox_"))) stand.remove(); }

    private List<String> starts(List<String> values, String prefix) {
        List<String> out = new ArrayList<>();
        for (String v : values) if (v.toLowerCase(Locale.ROOT).startsWith(prefix.toLowerCase(Locale.ROOT))) out.add(v);
        return out;
    }

    @Override public boolean onCommand(CommandSender sender, Command command, String label, String[] a) {
        if (a.length == 0 || a[0].equalsIgnoreCase("ayuda")) { help(sender); return true; }
        if (!(sender instanceof Player p)) { sender.sendMessage("§cEste comando debe usarse dentro del juego."); return true; }
        switch (a[0].toLowerCase(Locale.ROOT)) {
            case "crear" -> {
                if (a.length < 2) { p.sendMessage("§eUso: /sk crear <nombre>"); return true; }
                String name = cleanName(a[1]); String key = key(name);
                if (name.isBlank() || kingdoms.containsKey(key)) { p.sendMessage("§cEse nombre no es válido o ya existe."); return true; }
                if (kingdomOf(p.getUniqueId()) != null) { p.sendMessage("§cYa perteneces a un reino."); return true; }
                Chunk chunk = p.getLocation().getChunk();
                if (kingdomAt(chunk) != null) { p.sendMessage("§cEste chunk ya pertenece a un reino."); return true; }
                Location loc = p.getLocation();
                Block base = loc.getBlock();
                base.setType(Material.BEACON, false);
                // Marca visual: piedra negra alrededor y debajo del Nexo.
                for (int x=-2; x<=2; x++) for (int z=-2; z<=2; z++) {
                    if (x == 0 && z == 0) continue;
                    Block ground = base.getRelative(x, -1, z);
                    if (ground.getType().isSolid() && ground.getType() != Material.BEDROCK) ground.setType(Material.BLACKSTONE, false);
                }
                Kingdom k = new Kingdom(name, p.getUniqueId(), p.getName());
                k.createdAt = System.currentTimeMillis();
                k.nexusWorld = chunk.getWorld().getName(); k.nexusX = base.getX(); k.nexusY = base.getY(); k.nexusZ = base.getZ();
                k.claims.add(chunkKey(chunk)); k.members.put(p.getUniqueId().toString(), new Member(p.getName(), "REY"));
                k.moonstones = 0.0; k.level = 1;
                kingdoms.put(key, k); k.nexusBuildingUntil=System.currentTimeMillis()+60000L; saveData();
                startNexusConstruction(k, 60000L);
                p.sendMessage("§a¡Has fundado el reino §e" + name + "§a!");
                p.sendMessage("§7El Nexo está en construcción. Estará listo en 1 minuto; no podrás usarlo mientras tanto.");
            }
            case "reclamar" -> {
                Kingdom k = kingdomOf(p.getUniqueId());
                if (k == null) { p.sendMessage("§cNo perteneces a un reino."); return true; }
                if (!isKing(p, k)) { p.sendMessage("§cSolo el Rey puede reclamar terreno en esta versión."); return true; }
                Chunk c = p.getLocation().getChunk(); Kingdom owner = kingdomAt(c);
                int claimLimit = claimLimit(k);
                if (k.claims.size() >= claimLimit) { p.sendMessage("§cHas alcanzado el límite de reclamaciones: §e" + claimLimit + " chunks§c. Añade miembros o compra Más reclamaciones."); return true; }
                if (owner != null) { p.sendMessage(owner == k ? "§eEste chunk ya es de tu reino." : "§cEste chunk pertenece a otro reino."); return true; }
                k.claims.add(chunkKey(c)); saveData(); p.sendMessage("§aChunk reclamado para §e" + k.name + "§a.");
            }
            case "agregar", "añadir", "invitar" -> {
                boolean legacyPeon=a[0].equalsIgnoreCase("invitar"); boolean legacyKnight=a[0].equalsIgnoreCase("agregar");
                if (a.length < ((legacyPeon||legacyKnight)?2:3)) { p.sendMessage("§eUso: /sk añadir <jugador> <rey|caballero|peon>"); return true; }
                Kingdom k=kingdomOf(p.getUniqueId()); if(k==null || !isKing(p,k)){p.sendMessage("§cSolo el Rey puede añadir miembros.");return true;}
                Player target=Bukkit.getPlayerExact(a[1]); if(target==null){p.sendMessage("§cEse jugador debe estar conectado.");return true;}
                if(kingdomOf(target.getUniqueId())!=null){p.sendMessage("§cEse jugador ya pertenece a un reino.");return true;}
                String rank=legacyPeon?"PEON":legacyKnight && a.length<3?"CABALLERO":a[2].toUpperCase(Locale.ROOT); if(!Arrays.asList("REY","CABALLERO","PEON").contains(rank)){p.sendMessage("§cRango inválido. Usa rey, caballero o peon.");return true;}
                if(rank.equals("REY")){Member current=k.members.get(k.kingId.toString());if(current!=null)k.members.put(k.kingId.toString(),new Member(current.name,"CABALLERO"));k.kingId=target.getUniqueId();k.kingName=target.getName();}
                k.members.put(target.getUniqueId().toString(),new Member(target.getName(),rank)); saveData();
                p.sendMessage("§a"+target.getName()+" añadido como "+rankName(rank)+"."); target.sendMessage("§aTe has unido a §e"+k.name+" §acomo "+rankName(rank)+".");
            }
            case "remover" -> {
                if(a.length<2){p.sendMessage("§eUso: /sk remover <jugador>");return true;} Kingdom k=kingdomOf(p.getUniqueId());
                if(k==null || !isKing(p,k)){p.sendMessage("§cSolo el Rey puede remover miembros.");return true;}
                Player target=Bukkit.getPlayerExact(a[1]); String id=target==null?findMemberId(k,a[1]):target.getUniqueId().toString(); Member member=id==null?null:k.members.get(id);
                if(member==null || member.rank.equals("REY")){p.sendMessage("§cNo se encontró ese miembro o intentaste remover al Rey.");return true;}
                k.members.remove(id); saveData(); p.sendMessage("§e"+member.name+" §cha sido expulsado del reino."); if(target!=null)target.sendMessage("§cHas sido expulsado del reino §e"+k.name+"§c.");
            }
            case "promover" -> {
                if(a.length<2){p.sendMessage("§eUso: /sk promover <jugador>");return true;} Kingdom k=kingdomOf(p.getUniqueId());
                if(k==null || !isKing(p,k)){p.sendMessage("§cSolo el Rey puede promover miembros.");return true;}
                Player target=Bukkit.getPlayerExact(a[1]); String id=target==null?findMemberId(k,a[1]):target.getUniqueId().toString(); Member member=id==null?null:k.members.get(id);
                if(member==null || member.rank.equals("REY")){p.sendMessage("§cNo se encontró ese miembro o ya es Rey.");return true;}
                String next=member.rank.equals("PEON")?"CABALLERO":"REY"; if(next.equals("REY")){Member current=k.members.get(p.getUniqueId().toString()); k.members.put(p.getUniqueId().toString(),new Member(current.name,"CABALLERO")); k.kingId=target!=null?target.getUniqueId():UUID.fromString(id); k.kingName=member.name;}
                k.members.put(id,new Member(member.name,next)); saveData(); p.sendMessage("§a"+member.name+" ahora es "+rankName(next)+"."); if(target!=null)target.sendMessage("§aHas sido promovido a "+rankName(next)+" en §e"+k.name+"§a.");
            }
            case "rename" -> {
                Kingdom own = kingdomOf(p.getUniqueId());
                if (own == null) { p.sendMessage("§cDebes pertenecer a un reino para cambiar su nombre."); return true; }
                if (!isKing(p, own)) { p.sendMessage("§cSolo el Rey puede cambiar el nombre del reino."); return true; }
                if (a.length < 2) { p.sendMessage("§eUso: /sk rename <nombre con códigos de color &>"); return true; }
                String rawName = String.join(" ", Arrays.copyOfRange(a, 1, a.length));
                String newName = ChatColor.translateAlternateColorCodes('&', cleanName(rawName));
                String newKey = key(newName), oldKey = key(own.name);
                if (ChatColor.stripColor(newName).isBlank() || (kingdoms.containsKey(newKey) && !newKey.equals(oldKey))) { p.sendMessage("§cEse nombre no es válido o ya lo utiliza otro reino."); return true; }
                removeNexusHologram(own);
                kingdoms.remove(oldKey);
                own.name = newName;
                kingdoms.put(newKey, own);
                if (activeNexusChestInventories.containsKey(oldKey)) activeNexusChestInventories.put(newKey, activeNexusChestInventories.remove(oldKey));
                for (Nation nation : nations.values()) {
                    if (key(nation.founderKingdom).equals(oldKey)) nation.founderKingdom = newName;
                    if (nation.kingdoms.remove(oldKey)) nation.kingdoms.add(newKey);
                }
                for (MailMessage mail : mails) {
                    if (key(mail.senderKingdom).equals(oldKey)) mail.senderKingdom = newName;
                    if (key(mail.recipientKingdom).equals(oldKey)) mail.recipientKingdom = newName;
                }
                saveData(); updateNexusHologram(own);
                p.sendMessage("§aEl nombre de tu reino ahora es §r" + newName + "§a.");
            }
            case "pl" -> {
                if (!p.hasPermission("shadowkingdoms.admin")) { p.sendMessage("§cNo tienes permiso para modificar PL."); return true; }
                if (a.length < 4 || !a[1].equalsIgnoreCase("añadir")) { p.sendMessage("§eUso: /sk pl añadir <reino> <cantidad>"); return true; }
                Kingdom target = kingdoms.get(key(a[2]));
                if (target == null) { p.sendMessage("§cReino no encontrado."); return true; }
                try { double amount = Double.parseDouble(a[3]); if (!Double.isFinite(amount) || amount <= 0) throw new NumberFormatException(); target.moonstones += amount; saveData(); p.sendMessage("§aAñadidas §e" + formatPL(amount) + " PL §aal reino §e" + target.name + "§a. Saldo: §e" + formatPL(target.moonstones) + " PL"); } catch (NumberFormatException ex) { p.sendMessage("§cCantidad inválida."); }
            }
            case "nexo" -> {
                Kingdom k = kingdomOf(p.getUniqueId()); if (k == null) p.sendMessage("§cNo perteneces a un reino.");
                else if (!k.nexusPlaced) p.sendMessage("§eEl Nexo de §6"+k.name+" §eno está colocado. Usa §f/sk colocar-nexo§e dentro de tu territorio.");
                else p.sendMessage("§eNexo de §6"+k.name+"§e: §f"+k.nexusWorld+" "+k.nexusX+", "+k.nexusY+", "+k.nexusZ+(k.nexusBuildingUntil>System.currentTimeMillis()?" §c(EN CONSTRUCCIÓN)":""));
            }
            case "cofre" -> {
                Kingdom own = kingdomOf(p.getUniqueId());
                if (own == null) { p.sendMessage("§cDebes pertenecer a un reino para abrir su cofre."); return true; }
                if(own.nexusBuildingUntil>System.currentTimeMillis()){p.sendMessage("§cEl Nexo está en construcción. Espera a que termine.");return true;}
                openNexusChest(p, own);
            }
            case "colocar-nexo" -> {
                Kingdom own = kingdomOf(p.getUniqueId());
                if (own == null) { p.sendMessage("§cDebes pertenecer a un reino."); return true; }
                if (!isKing(p, own)) { p.sendMessage("§cSolo el Rey puede volver a colocar el Nexo."); return true; }
                if (own.nexusPlaced) { p.sendMessage("§eTu Nexo ya está colocado."); return true; }
                if (kingdomAt(p.getLocation().getChunk()) != own) { p.sendMessage("§cSolo puedes colocar el Nexo dentro de un chunk reclamado por tu reino."); return true; }
                Block base = p.getLocation().getBlock();
                if (!base.getType().isAir() && !base.isReplaceable()) { p.sendMessage("§cEl bloque donde estás debe estar libre. Colócate en el lugar exacto donde quieres el Nexo."); return true; }
                base.setType(Material.BEACON, false);
                own.nexusWorld = base.getWorld().getName(); own.nexusX = base.getX(); own.nexusY = base.getY(); own.nexusZ = base.getZ(); own.nexusPlaced = true; own.nexusBuildingUntil=System.currentTimeMillis()+60000L;
                saveData(); startNexusConstruction(own,60000L);
                p.sendMessage("§eEl Nexo de §6"+own.name+" §eestá en construcción durante 1 minuto.");
            }
            case "borrar" -> {
                if (a.length < 2 || !a[1].equalsIgnoreCase("reino")) { p.sendMessage("§eUso: /sk borrar reino"); return true; }
                Kingdom own = kingdomOf(p.getUniqueId());
                if (own == null) { p.sendMessage("§cNo perteneces a ningún reino."); return true; }
                if (!isKing(p, own)) { p.sendMessage("§cSolo el Rey puede borrar el reino."); return true; }
                String removedKey = key(own.name);
                if (own.nexusPlaced) {
                    World world = Bukkit.getWorld(own.nexusWorld);
                    if (world != null) {
                        Block nexus = world.getBlockAt(own.nexusX, own.nexusY, own.nexusZ);
                        if (nexus.getType() == Material.BEACON) nexus.setType(Material.AIR, false);
                    }
                }
                removeNexusHologram(own);
                kingdoms.remove(removedKey);
                for (Nation nation : nations.values()) nation.kingdoms.remove(removedKey);
                nations.entrySet().removeIf(entry -> entry.getValue().kingdoms.isEmpty());
                activeNexusChestInventories.remove(removedKey);
                for (UUID memberId : new ArrayList<>(openMenuKingdoms.keySet())) if (removedKey.equals(openMenuKingdoms.get(memberId))) openMenuKingdoms.remove(memberId);
                saveData();
                p.closeInventory();
                p.sendMessage("§cEl reino §e"+own.name+" §cy su territorio han sido eliminados. Los chunks quedan sin reclamar.");
            }
            case "info" -> {
                Kingdom k = a.length >= 2 ? kingdoms.get(key(a[1])) : kingdomOf(p.getUniqueId());
                if (k == null) p.sendMessage("§cNo se encontró ese reino.");
                else p.sendMessage("§6Reino: §e"+k.name+" §7| §6Rey: §e"+k.kingName+" §7| §6Miembros: §e"+k.members.size()+" §7| §6Chunks: §e"+k.claims.size());
            }
            case "nacion" -> nationCommand(p, a);
            case "correo", "mail" -> {
                if (a.length > 1 && a[1].equalsIgnoreCase("adjuntar")) mailCommand(p, a);
                else p.sendMessage("§ePara acceder a ShadowMail, haz clic derecho sobre el Nexo de tu reino.");
            }
            default -> help(sender);
        }
        return true;
    }

    private void nationCommand(Player p, String[] a) {
        if (a.length < 3) { p.sendMessage("§eUso: /sk nacion crear <nombre> | /sk nacion invitar <nacion> <reino>"); return; }
        if (a[1].equalsIgnoreCase("crear")) {
            Kingdom k = kingdomOf(p.getUniqueId()); if (k == null || !isKing(p,k)) { p.sendMessage("§cDebes ser Rey de un reino para crear una nación."); return; }
            String name = cleanName(a[2]); if (nations.containsKey(key(name)) || name.isBlank()) { p.sendMessage("§cNombre inválido o nación existente."); return; }
            Nation n = new Nation(name, k.name); n.kingdoms.add(key(k.name)); nations.put(key(name), n); k.nation = name; saveData(); p.sendMessage("§aNación §e"+name+" §acreada con tu reino.");
        } else if (a[1].equalsIgnoreCase("invitar")) {
            if (a.length < 4) { p.sendMessage("§eUso: /sk nacion invitar <nacion> <reino>"); return; }
            Kingdom k = kingdomOf(p.getUniqueId()); if (k == null || !isKing(p,k)) { p.sendMessage("§cDebes ser Rey."); return; }
            Nation n = nations.get(key(a[2])); Kingdom invited = kingdoms.get(key(a[3]));
            if (n == null || invited == null) { p.sendMessage("§cNo se encontró la nación o el reino."); return; }
            if (!key(n.founderKingdom).equals(key(k.name))) { p.sendMessage("§cSolo el reino fundador puede invitar en esta versión inicial."); return; }
            if (invited.nation != null && !invited.nation.isBlank()) { p.sendMessage("§cEse reino ya pertenece a una nación."); return; }
            n.kingdoms.add(key(invited.name)); invited.nation = n.name; saveData(); p.sendMessage("§aEl reino §e"+invited.name+" §ase unió a la nación §e"+n.name+"§a. Sus miembros pueden abrir contenedores aliados, pero no modificar sus bloques.");
        } else if (a[1].equalsIgnoreCase("info")) {
            Nation n = nations.get(key(a[2])); if (n == null) p.sendMessage("§cNación no encontrada."); else p.sendMessage("§6Nación §e"+n.name+" §7| §6Reinos: §e"+n.kingdoms.size());
        } else p.sendMessage("§eUso: /sk nacion crear <nombre> | /sk nacion invitar <nacion> <reino>");
    }

    private void help(CommandSender s) { s.sendMessage("§8§m----------§r §6§lShadowKingdoms §8§m----------"); s.sendMessage("§e/sk rename <nombre> §7- Cambia el nombre y admite colores con &"); s.sendMessage("§e/sk pl añadir <reino> <cantidad> §7- Añade Piedras Lunares (admin)"); s.sendMessage("§e/sk crear <nombre> §7- Funda un reino y coloca su Nexo"); s.sendMessage("§e/sk reclamar §7- Reclama el chunk actual"); s.sendMessage("§e/sk añadir <jugador> <rey|caballero|peon> §7- Añade un miembro"); s.sendMessage("§e/sk remover <jugador> §7- Expulsa un miembro"); s.sendMessage("§e/sk promover <jugador> §7- Promueve Peón→Caballero→Rey"); s.sendMessage("§e/sk nexo §7- Muestra las coordenadas del Nexo"); s.sendMessage("§e/sk cofre §7- Abre el cofre compartido de tu reino"); s.sendMessage("§e/sk colocar-nexo §7- Vuelve a colocar el Nexo dentro de tu territorio"); s.sendMessage("§e/sk borrar reino §7- Elimina tu reino (solo Rey)"); s.sendMessage("§7Haz clic derecho sobre el Nexo para abrir su menú."); s.sendMessage("§e/sk info [reino] §7- Información del reino"); s.sendMessage("§e/sk nacion crear <nombre> §7- Crea una nación"); s.sendMessage("§e/sk nacion invitar <nacion> <reino> §7- Une un reino a una nación"); }

    @EventHandler public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        Bukkit.getScheduler().runTask(this, () -> {
            Kingdom k = kingdomAt(player.getLocation().getChunk());
            lastTerritoryByPlayer.put(player.getUniqueId(), territoryIdentity(k));
            sendTerritoryTitle(player, k);
            Kingdom own=kingdomOf(player.getUniqueId()); if(own!=null && k==own && extraLevel(own,"territory_fly")>0) player.setAllowFlight(true);
        });
    }

    @EventHandler public void onMove(PlayerMoveEvent e) {
        if (e.getTo() == null) return;
        if (e.getFrom().getWorld().equals(e.getTo().getWorld()) && e.getFrom().getChunk().getX() == e.getTo().getChunk().getX() && e.getFrom().getChunk().getZ() == e.getTo().getChunk().getZ()) return;
        Player player = e.getPlayer();
        Kingdom k = kingdomAt(e.getTo().getChunk());
        Kingdom own = kingdomOf(player.getUniqueId());
        Kingdom previous=kingdomAt(e.getFrom().getChunk());
        if(k!=null && !isMember(player,k) && !isNationAlly(player,k)) enterGuardTerritory(player,k);
        else if(previous!=null && !isMember(player,previous) && !isNationAlly(player,previous)) leaveGuardTerritory(player,previous);
        if (own != null && extraLevel(own,"territory_fly") > 0) {
            if (k == own) { Integer task=flyExitTasks.remove(player.getUniqueId()); if(task!=null) Bukkit.getScheduler().cancelTask(task); if(!player.getAllowFlight()) player.setAllowFlight(true); }
            else if (kingdomAt(e.getFrom().getChunk()) == own) scheduleFlyDisable(player);
        }
        String nextTerritory = territoryIdentity(k);
        String previousTerritory = lastTerritoryByPlayer.put(player.getUniqueId(), nextTerritory);
        if (previousTerritory == null || !previousTerritory.equals(nextTerritory)) sendTerritoryTitle(player, k);
    }

    @EventHandler public void onQuit(PlayerQuitEvent e) {
        Player p=e.getPlayer(); Kingdom territory=kingdomAt(p.getLocation().getChunk()); if(territory!=null && !isMember(p,territory) && !isNationAlly(p,territory))leaveGuardTerritory(p,territory);
        lastTerritoryByPlayer.remove(p.getUniqueId());
    }

    private String territoryIdentity(Kingdom kingdom) {
        return kingdom == null ? "UNCLAIMED" : "KINGDOM:" + key(kingdom.name);
    }

    private void sendTerritoryTitle(Player player, Kingdom kingdom) {
        player.sendTitle(kingdom == null ? "§7Tierra de nadie" : "§6§l" + kingdom.name,
                kingdom == null ? "§8Territorio sin reclamar" : "§eTerritorio del reino", 5, 35, 10);
    }

    @EventHandler public void onNexusClick(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.BEACON) return;
        Block b = e.getClickedBlock();
        Kingdom k = kingdomAt(b.getChunk());
        if (k == null || !k.nexusWorld.equals(b.getWorld().getName()) || k.nexusX != b.getX() || k.nexusY != b.getY() || k.nexusZ != b.getZ()) return;
        e.setCancelled(true);
        if(k.nexusBuildingUntil>System.currentTimeMillis()){e.getPlayer().sendMessage("§eEl Nexo sigue en construcción. Espera §f"+formatDuration(k.nexusBuildingUntil-System.currentTimeMillis())+"§e.");return;}
        openNexusMenu(e.getPlayer(), k);
    }

    private void openNexusMenu(Player player, Kingdom k) {
        openMenuKingdoms.put(player.getUniqueId(), key(k.name));
        Inventory inv = Bukkit.createInventory(null, 27, MAIN_MENU_TITLE);
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta barrierMeta = barrier.getItemMeta();
        barrierMeta.setDisplayName("§cPróximamente");
        barrierMeta.setLore(Arrays.asList("§7Se añadirá en la próxima actualización."));
        barrier.setItemMeta(barrierMeta);
        for (int i=0;i<27;i++) inv.setItem(i, barrier.clone());
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§6§l" + k.name);
        List<String> lore = new ArrayList<>();
        lore.add("§7Rey: §f" + k.kingName);
        lore.add("§7Miembros: §f" + k.members.size());
        lore.add("§7Tiempo existente: §f" + formatDuration(System.currentTimeMillis() - k.createdAt));
        lore.add(""); lore.add("§eHaz clic para ver información detallada.");
        meta.setLore(lore); info.setItemMeta(meta);
        inv.setItem(13, info);
        inv.setItem(11, named(Material.CHEST, "§b§lCasilla de correo", "§7Abrir ShadowMail del reino", "§7Enviar mensajes a otros reinos", "§7Adjuntar y recibir objetos"));
        inv.setItem(15, named(Material.ENDER_CHEST, "§6§lCofre del Nexo", "§7Almacenamiento compartido del reino", "§7Solo miembros de este reino", "§eHaz clic para abrir"));
        inv.setItem(21, named(Material.NETHER_STAR, "§d§lMejoras del reino", "§7Nivel actual: §f" + roman(k.level), "§7Saldo: §d" + formatPL(k.moonstones) + " PL", "§eHaz clic para mejorar el reino"));
        inv.setItem(17,named(Material.DIAMOND_PICKAXE,"§b§lCambiar minerales por PL","§7Intercambia minerales por Piedras Lunares","§eHaz clic para abrir"));
        inv.setItem(19,named(Material.PLAYER_HEAD,"§6§lDefensas del reino","§7Compra y administra torretas","§7Primera defensa disponible: Cañón","§eHaz clic para abrir"));
        inv.setItem(23, named(Material.AMETHYST_SHARD, "§b§lMejoras extra", "§7Mejora aspectos especiales de tu reino", "§7Costes en Piedras Lunares", "§eHaz clic para abrir"));
        player.openInventory(inv);
    }

    private void openNexusChest(Player player, Kingdom kingdom) {
        String kingdomKey = key(kingdom.name);
        Inventory inv = activeNexusChestInventories.get(kingdomKey);
        if (inv == null) {
            inv = Bukkit.createInventory(null, 9, NEXUS_CHEST_PREFIX + kingdom.name);
            for (int i = 0; i < 9; i++) {
                ItemStack item = kingdom.nexusChest[i];
                if (item != null && !item.getType().isAir()) inv.setItem(i, item.clone());
            }
            activeNexusChestInventories.put(kingdomKey, inv);
        }
        openNexusChests.put(player.getUniqueId(), kingdomKey);
        player.openInventory(inv);
    }

    @EventHandler public void onMineralTradeClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player) || !MINERALS_MENU_TITLE.equals(e.getView().getTitle())) return;
        Inventory inv = e.getView().getTopInventory();
        for (int slot = 0; slot < inv.getSize(); slot++) {
            if (slot == 45 || slot == 53) continue;
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            Map<Integer, ItemStack> leftovers = player.getInventory().addItem(item.clone());
            for (ItemStack stack : leftovers.values()) player.getWorld().dropItemNaturally(player.getLocation(), stack);
            inv.setItem(slot, null);
        }
    }

    @EventHandler public void onNexusChestClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        String kingdomKey = openNexusChests.remove(player.getUniqueId());
        if (kingdomKey == null || !e.getView().getTitle().startsWith(NEXUS_CHEST_PREFIX)) return;
        Kingdom kingdom = kingdoms.get(kingdomKey);
        if (kingdom == null) return;
        Inventory shared = activeNexusChestInventories.get(kingdomKey);
        ItemStack[] contents = (shared != null ? shared : e.getInventory()).getContents();
        for (int i = 0; i < 9; i++) {
            ItemStack item = i < contents.length ? contents[i] : null;
            kingdom.nexusChest[i] = item == null || item.getType().isAir() ? null : item.clone();
        }
        saveData();
    }

    private void openKingdomUpgrades(Player player, Kingdom kingdom) {
        upgradeMenuKingdoms.put(player.getUniqueId(), key(kingdom.name));
        Inventory inv = Bukkit.createInventory(null, 27, UPGRADES_MENU_TITLE);
        for (int i = 0; i < 27; i++) inv.setItem(i, named(Material.GRAY_STAINED_GLASS_PANE, "§8 "));
        inv.setItem(4, named(Material.NETHER_STAR, "§d§lMejoras del reino", "§7Nivel actual: §f" + roman(kingdom.level), "§7Piedras lunares: §d" + formatPL(kingdom.moonstones) + " PL", "§7Nivel máximo: §fIII"));
        if (kingdom.level < 3) {
            double cost = kingdom.level == 1 ? 100.0 : 250.0;
            inv.setItem(13, named(Material.EMERALD, "§a§lMejorar a nivel " + roman(kingdom.level + 1), "§7Coste: §d" + formatPL(cost) + " PL", "§7Saldo actual: §d" + formatPL(kingdom.moonstones) + " PL", "§eHaz clic para comprar la mejora"));
        } else inv.setItem(13, named(Material.NETHER_STAR, "§6§lNivel máximo alcanzado", "§7Tu reino ya está en nivel III."));
        inv.setItem(18, named(Material.ARROW, "§eVolver al Nexo"));
        player.openInventory(inv);
    }

    private void handleUpgradeClick(Player player, int slot) {
        String kingdomKey=upgradeMenuKingdoms.get(player.getUniqueId()); Kingdom kingdom=kingdomKey==null?null:kingdoms.get(kingdomKey);
        if(kingdom==null || !isMember(player,kingdom)){player.closeInventory();return;} if(slot==18){openNexusMenu(player,kingdom);return;}
        if(slot!=13 || kingdom.level>=3)return; if(kingdom.nexusBuildingUntil>System.currentTimeMillis()){player.sendMessage("§cEl Nexo está en construcción.");return;}
        double cost=kingdom.level==1?100.0:250.0; if(kingdom.moonstones+1.0E-9<cost){player.sendMessage("§cNo tienes suficientes PL. Necesitas "+formatPL(cost)+".");return;}
        int next=kingdom.level+1; long duration=next==2?60000L:180000L; kingdom.moonstones=roundPL(kingdom.moonstones-cost); kingdom.pendingLevel=next; kingdom.nexusBuildingUntil=System.currentTimeMillis()+duration; saveData();
        player.closeInventory(); player.sendMessage("§eMejora del Nexo iniciada. Nivel "+roman(next)+" en "+(duration/60000)+" minuto(s). El Nexo estará bloqueado mientras se mejora."); startNexusConstruction(kingdom,duration);
    }

    private static final String[] EXTRA_KEYS = {"claims", "killer_edge", "natural_regen", "general_protection", "defenses", "territory_fly", "guards", "guard_madness"};
    private static final String[] EXTRA_NAMES = {"Más reclamaciones", "Filo asesino", "Regeneración natural", "Protección general", "Estructuras defensivas", "Vuelo territorial", "Mejora de guardias", "Locura"};
    private static final int[] EXTRA_MAX = {10,10,10,3,2,1,3,2};
    private static final Material[] EXTRA_MATERIALS = {Material.MAP,Material.IRON_SWORD,Material.GOLDEN_APPLE,Material.SHIELD,Material.DISPENSER,Material.FEATHER,Material.ZOMBIE_HEAD,Material.BLAZE_POWDER};

    private int extraLevel(Kingdom k, String id) { return k.extraLevels.getOrDefault(id, 0); }
    private int claimLimit(Kingdom k) { return 4 + Math.max(0, k.members.size()-1)*2 + extraLevel(k,"claims")*4; }
    private double extraCost(int currentLevel) { return 100.0 + 300.0 * currentLevel; }

    private void openExtraUpgrades(Player player, Kingdom k) {
        extraUpgradeMenuKingdoms.put(player.getUniqueId(), key(k.name));
        Inventory inv = Bukkit.createInventory(null, 54, EXTRA_UPGRADES_TITLE);
        for (int i=0;i<54;i++) inv.setItem(i, named(Material.GRAY_STAINED_GLASS_PANE,"§8 "));
        inv.setItem(4,named(Material.AMETHYST_SHARD,"§b§lMejoras extra", "§7Saldo: §d"+formatPL(k.moonstones)+" PL", "§7Precio: 100 + 300 × nivel actual"));
        int[] slots={10,11,12,13,14,15,16,28};
        for(int i=0;i<EXTRA_KEYS.length;i++) {
            int level=extraLevel(k,EXTRA_KEYS[i]), max=EXTRA_MAX[i];
            double cost=level>=max?0:extraCost(level);
            List<String> lore=new ArrayList<>(); lore.add("§7Nivel: §f"+level+"§7/§f"+max);
            lore.add("§7Siguiente nivel: §f"+(level>=max?"Máximo":level+1));
            if(level<max) { lore.add("§7Coste: §d"+formatPL(cost)+" PL"); lore.add("§eClic para comprar"); } else lore.add("§6Nivel máximo alcanzado");
            lore.addAll(extraDescription(EXTRA_KEYS[i],level));
            inv.setItem(slots[i],named(EXTRA_MATERIALS[i],"§b§l"+EXTRA_NAMES[i],lore.toArray(new String[0])));
        }
        inv.setItem(30,named(Material.GOLDEN_SWORD,"§6§lGuardias: equipo y atributos", "§7Nivel actual: §f"+extraLevel(k,"guards"),"§7Locura: §f"+extraLevel(k,"guard_madness")));
        inv.setItem(49,named(Material.ARROW,"§eVolver al Nexo"));
        player.openInventory(inv);
    }
    private List<String> extraDescription(String id,int level) {
        return switch(id) {
            case "claims" -> List.of("§7Límite actual: §f"+(4+level*4)+" chunks §7(sin miembros extra)");
            case "killer_edge" -> List.of("§7Daño extra territorial: §c+"+String.format(Locale.US,"%.1f",level*0.2)+" puntos");
            case "natural_regen" -> List.of("§7Regeneración natural extra: §a+"+String.format(Locale.US,"%.1f",level*0.2)+" puntos");
            case "general_protection" -> List.of("§7Nivel 1: explosiones", "§7Nivel 2: vandalismo de criaturas", "§7Nivel 3: cultivos protegidos");
            case "defenses" -> List.of("§7Torretas extra por chunk: §f+"+level);
            case "territory_fly" -> List.of("§7Vuelo dentro del territorio", "§7Se desactiva 3 segundos después de salir");
            case "guards" -> List.of("§7I: guardias básicos", "§7II: armadura de cuero", "§7III: armadura de malla");
            case "guard_madness" -> List.of("§7I: Fuerza I", "§7II: +8% velocidad");
            default -> List.of();
        };
    }
    private void handleExtraUpgradeClick(Player player,int slot) {
        String kk=extraUpgradeMenuKingdoms.get(player.getUniqueId()); Kingdom k=kk==null?null:kingdoms.get(kk);
        if(k==null || !isMember(player,k)){player.closeInventory();return;}
        if(slot==49){openNexusMenu(player,k);return;}
        int[] slots={10,11,12,13,14,15,16,28}; int index=-1; for(int i=0;i<slots.length;i++)if(slots[i]==slot)index=i;
        if(index<0)return;
        String id=EXTRA_KEYS[index]; int level=extraLevel(k,id),max=EXTRA_MAX[index];
        if(level>=max){player.sendMessage("§eEsta mejora ya está al máximo.");return;}
        double cost=extraCost(level);
        if(k.moonstones+1.0E-9<cost){player.sendMessage("§cNecesitas §d"+formatPL(cost)+" PL§c.");return;}
        k.moonstones=roundPL(k.moonstones-cost); k.extraLevels.put(id,level+1); saveData();
        player.sendMessage("§aMejora §b"+EXTRA_NAMES[index]+" §acomprada: nivel §e"+(level+1)+"§a.");
        if(id.equals("territory_fly")) refreshTerritoryFlight(player,k);
        openExtraUpgrades(player,k);
    }
    private void enterGuardTerritory(Player player,Kingdom kingdom) {
        UUID id=player.getUniqueId(); String kk=key(kingdom.name); GuardSession session=guardSessions.get(id);
        if(session!=null && session.kingdomKey.equals(kk)) { session.outsideSince=0L; if(session.resetTask!=-1){Bukkit.getScheduler().cancelTask(session.resetTask);session.resetTask=-1;} if(session.aliveCount==0 && System.currentTimeMillis()>=session.nextWaveAt)spawnGuardWave(player,kingdom,session); return; }
        if(session!=null) clearGuardSession(id);
        session=new GuardSession(kk); guardSessions.put(id,session); spawnGuardWave(player,kingdom,session);
    }
    private void leaveGuardTerritory(Player player,Kingdom kingdom) {
        GuardSession session=guardSessions.get(player.getUniqueId()); if(session==null || !session.kingdomKey.equals(key(kingdom.name)))return;
        if(session.outsideSince==0L) session.outsideSince=System.currentTimeMillis();
        if(session.resetTask!=-1) Bukkit.getScheduler().cancelTask(session.resetTask);
        UUID id=player.getUniqueId(); session.resetTask=Bukkit.getScheduler().runTaskLater(this,()->{GuardSession current=guardSessions.get(id); if(current==session && current.outsideSince>0 && System.currentTimeMillis()-current.outsideSince>=180000L)guardSessions.remove(id);},3600L).getTaskId();
    }
    private void spawnGuardWave(Player intruder,Kingdom kingdom,GuardSession session) {
        if(!intruder.isOnline() || intruder.isDead() || extraLevel(kingdom,"guards")<=0)return;
        session.nextWaveAt=0L; session.aliveCount=0; int amount=session.nextAmount;
        String tag="shadowkingdoms_guard_"+key(kingdom.name); Location base=intruder.getLocation().clone();
        for(int i=0;i<amount;i++) {
            double angle=(Math.PI*2*i)/amount; Location at=base.clone().add(Math.cos(angle)*2,0,Math.sin(angle)*2);
            LivingEntity guard=i%2==0?base.getWorld().spawn(at,Zombie.class):base.getWorld().spawn(at,Skeleton.class);
            guard.setCustomName("§6Guardia de "+kingdom.name); guard.setCustomNameVisible(true); guard.setCanPickupItems(false); guard.setRemoveWhenFarAway(false); guard.addScoreboardTag(tag); guard.addScoreboardTag("shadowkingdoms_guard_for_"+intruder.getUniqueId()); session.aliveCount++;
            EntityEquipment eq=guard.getEquipment(); if(eq!=null){eq.setItemInMainHand(new ItemStack(Material.AIR)); if(extraLevel(kingdom,"guards")>=2){boolean chain=extraLevel(kingdom,"guards")>=3; Material[] armor=chain?new Material[]{Material.CHAINMAIL_HELMET,Material.CHAINMAIL_CHESTPLATE,Material.CHAINMAIL_LEGGINGS,Material.CHAINMAIL_BOOTS}:new Material[]{Material.LEATHER_HELMET,Material.LEATHER_CHESTPLATE,Material.LEATHER_LEGGINGS,Material.LEATHER_BOOTS}; eq.setHelmet(new ItemStack(armor[0]));eq.setChestplate(new ItemStack(armor[1]));eq.setLeggings(new ItemStack(armor[2]));eq.setBoots(new ItemStack(armor[3]));eq.setHelmetDropChance(0);eq.setChestplateDropChance(0);eq.setLeggingsDropChance(0);eq.setBootsDropChance(0);}}
            int madness=extraLevel(kingdom,"guard_madness"); if(madness>=1)guard.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH,Integer.MAX_VALUE,0,false,false)); if(madness>=2 && guard.getAttribute(Attribute.MOVEMENT_SPEED)!=null)guard.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(guard.getAttribute(Attribute.MOVEMENT_SPEED).getBaseValue()*1.08); if(guard instanceof Monster m)m.setTarget(intruder);
        }
        intruder.sendMessage("§c¡Los guardias de §e"+kingdom.name+" §chan aparecido! §7Oleada: "+amount);
    }
    /** Los guardias nunca deben adquirir como objetivo ni dañar a miembros o aliados de su reino. */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGuardTarget(EntityTargetLivingEntityEvent e) {
        if (!(e.getTarget() instanceof Player target)) return;
        Kingdom guardKingdom = kingdomForGuard(e.getEntity());
        if (guardKingdom == null) return;
        if (isMember(target, guardKingdom) || isNationAlly(target, guardKingdom)) {
            e.setCancelled(true);
            if (e.getEntity() instanceof Monster monster) monster.setTarget(null);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGuardDamage(EntityDamageByEntityEvent e) {
        if (!(e.getEntity() instanceof Player victim)) return;
        Kingdom guardKingdom = kingdomForGuard(e.getDamager());
        if (guardKingdom == null) return;
        if (isMember(victim, guardKingdom) || isNationAlly(victim, guardKingdom)) e.setCancelled(true);
    }

    private Kingdom kingdomForGuard(Entity entity) {
        for (String tag : entity.getScoreboardTags()) {
            if (!tag.startsWith("shadowkingdoms_guard_") || tag.startsWith("shadowkingdoms_guard_for_")) continue;
            String kingdomKey = tag.substring("shadowkingdoms_guard_".length());
            Kingdom kingdom = kingdoms.get(kingdomKey);
            if (kingdom != null) return kingdom;
        }
        return null;
    }

    @EventHandler public void onGuardDeath(EntityDeathEvent e) {
        Entity dead=e.getEntity(); String ownerTag=dead.getScoreboardTags().stream().filter(t->t.startsWith("shadowkingdoms_guard_for_")).findFirst().orElse(null); if(ownerTag==null)return;
        UUID id; try{id=UUID.fromString(ownerTag.substring("shadowkingdoms_guard_for_".length()));}catch(IllegalArgumentException ex){return;}
        GuardSession session=guardSessions.get(id); if(session==null)return; session.aliveCount=Math.max(0,session.aliveCount-1);
        if(session.aliveCount==0){session.nextAmount=Math.min(6,session.nextAmount+2);session.nextWaveAt=System.currentTimeMillis()+30000L; Bukkit.getScheduler().runTaskLater(this,()->{Player p=Bukkit.getPlayer(id);GuardSession current=guardSessions.get(id);if(p!=null && current==session && current.outsideSince==0L && System.currentTimeMillis()>=current.nextWaveAt){Kingdom k=kingdoms.get(current.kingdomKey);if(k!=null && kingdomAt(p.getLocation().getChunk())==k)spawnGuardWave(p,k,current);}},600L);}
    }
    private void clearGuardSession(UUID id){GuardSession s=guardSessions.remove(id);if(s!=null && s.resetTask!=-1)Bukkit.getScheduler().cancelTask(s.resetTask);}

    private void refreshTerritoryFlight(Player p,Kingdom k) {
        if(extraLevel(k,"territory_fly")>0 && kingdomAt(p.getLocation().getChunk())==k) { p.setAllowFlight(true); p.sendMessage("§bVuelo territorial activado."); }
    }
    private void scheduleFlyDisable(Player p) {
        Integer old=flyExitTasks.remove(p.getUniqueId()); if(old!=null) Bukkit.getScheduler().cancelTask(old);
        int task=Bukkit.getScheduler().runTaskLater(this,()->{
            Kingdom here=kingdomAt(p.getLocation().getChunk()); Kingdom own=kingdomOf(p.getUniqueId());
            if(own==null || extraLevel(own,"territory_fly")==0 || here!=own) { if(p.isFlying())p.setFlying(false); p.setAllowFlight(false); }
            flyExitTasks.remove(p.getUniqueId());
        },60L).getTaskId(); flyExitTasks.put(p.getUniqueId(),task);
    }

    private double roundPL(double amount) { return Math.floor(Math.max(0, amount) * 100.0) / 100.0; }
    private String formatPL(double amount) { return String.format(Locale.US, "%.2f", roundPL(amount)); }
    private String roman(int level) { return switch (level) { case 1 -> "I"; case 2 -> "II"; default -> "III"; }; }

    private void updateNexusHologram(Kingdom kingdom) {
        removeNexusHologram(kingdom);
        if (!kingdom.nexusPlaced) return;
        World world = Bukkit.getWorld(kingdom.nexusWorld); if (world == null) return;
        Location loc = new Location(world, kingdom.nexusX + 0.5, kingdom.nexusY + 1.65, kingdom.nexusZ + 0.5);
        ArmorStand stand = world.spawn(loc, ArmorStand.class, entity -> {
            entity.setInvisible(true); entity.setMarker(true); entity.setGravity(false); entity.setSilent(true); entity.setInvulnerable(true); entity.setPersistent(true);
            entity.setCustomName("§6§lNexo " + roman(kingdom.level)); entity.setCustomNameVisible(true);
        });
        // Tag the hologram so it can be found and removed safely on updates/restarts.
        stand.addScoreboardTag("shadowkingdoms_nexus_hologram");
        stand.addScoreboardTag("shadowkingdoms_kingdom_" + key(kingdom.name));
    }

    private void removeNexusHologram(Kingdom kingdom) {
        World world = Bukkit.getWorld(kingdom.nexusWorld); if (world == null) return;
        Location loc = new Location(world, kingdom.nexusX + 0.5, kingdom.nexusY + 1.65, kingdom.nexusZ + 0.5);
        for (Entity entity : world.getNearbyEntities(loc, 2.0, 3.0, 2.0)) {
            if (entity instanceof ArmorStand stand && stand.getScoreboardTags().contains("shadowkingdoms_nexus_hologram") && stand.getScoreboardTags().contains("shadowkingdoms_kingdom_" + key(kingdom.name))) stand.remove();
        }
    }

    public String placeholder(Player player, String identifier) {
        Kingdom kingdom = kingdomOf(player.getUniqueId());
        if (kingdom == null) return identifier.equalsIgnoreCase("kingdom") ? "Sin reino" : "0";
        return switch (identifier.toLowerCase(Locale.ROOT)) {
            case "pl" -> formatPL(kingdom.moonstones);
            case "level" -> roman(kingdom.level);
            case "level_number" -> Integer.toString(kingdom.level);
            case "kingdom" -> kingdom.name;
            default -> null;
        };
    }

    private void openKingdomDetails(Player player, Kingdom k) {
        openMenuKingdoms.put(player.getUniqueId(), key(k.name));
        Inventory inv = Bukkit.createInventory(null, 27, DETAILS_MENU_TITLE);
        ItemStack info = new ItemStack(Material.BEACON);
        ItemMeta meta = info.getItemMeta();
        meta.setDisplayName("§6§l" + k.name);
        List<String> lore = new ArrayList<>();
        lore.add("§7Rey: §f" + k.kingName);
        lore.add("§7Miembros: §f" + k.members.size());
        lore.add("§7Chunks reclamados: §f" + k.claims.size());
        lore.add("§7Tiempo existente: §f" + formatDuration(System.currentTimeMillis() - k.createdAt));
        lore.add("§7Nexo: §f" + k.nexusWorld + " " + k.nexusX + ", " + k.nexusY + ", " + k.nexusZ);
        lore.add(""); lore.add("§eMiembros del reino:");
        for (Member m : k.members.values()) lore.add("§8• §f" + m.name + " §7- " + rankName(m.rank));
        meta.setLore(lore); info.setItemMeta(meta); inv.setItem(13, info);
        ItemStack back = new ItemStack(Material.ARROW); ItemMeta bm = back.getItemMeta(); bm.setDisplayName("§eVolver"); back.setItemMeta(bm); inv.setItem(18, back);
        player.openInventory(inv);
    }

    @EventHandler public void onMineralTradeDrag(InventoryDragEvent e) {
        if (!e.getView().getTitle().equals(MINERALS_MENU_TITLE)) return;
        for (int raw : e.getRawSlots()) {
            if (raw < e.getView().getTopInventory().getSize()) {
                if (raw == 45 || raw == 53 || !MINERAL_VALUES.containsKey(e.getOldCursor().getType())) {
                    e.setCancelled(true);
                    return;
                }
            }
        }
    }

    @EventHandler public void onMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        String title = e.getView().getTitle();
        if (title.equals(MINERALS_MENU_TITLE)) {
            if (e.getClickedInventory() == null) return;
            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                int slot = e.getSlot();
                if (slot == 45 || slot == 53) {
                    e.setCancelled(true);
                    handleMineralTrade(player, slot);
                    return;
                }
                ItemStack cursor = e.getCursor();
                if (cursor != null && !cursor.getType().isAir() && !MINERAL_VALUES.containsKey(cursor.getType())) e.setCancelled(true);
                if (e.isShiftClick()) e.setCancelled(true);
                return;
            }
            if (e.isShiftClick()) {
                e.setCancelled(true);
                ItemStack moving = e.getCurrentItem();
                if (moving != null && MINERAL_VALUES.containsKey(moving.getType())) moveMineralsIntoTrade(player, moving);
            }
            return;
        }
        if (title.startsWith(NEXUS_CHEST_PREFIX)) {
            if (e.getClickedInventory() == null) return;
            if (e.isShiftClick() && e.getClickedInventory() == e.getView().getBottomInventory()) {
                // Permitir shift-click estándar; la GUI solo contiene 9 espacios reales.
            }
            return;
        }
        boolean mailTitle = title.equals(MAIL_MENU_TITLE) || title.startsWith(MAIL_INBOX_TITLE) || title.startsWith(MAIL_SENT_TITLE) || title.equals(MAIL_RECIPIENT_TITLE) || title.equals(MAIL_ATTACH_TITLE) || title.startsWith(MAIL_DETAILS_PREFIX);
        if (title.equals(INFERNO_UPGRADE_TITLE)) { e.setCancelled(true); if(e.getClickedInventory()==e.getView().getTopInventory()) handleInfernoUpgradeClick(player,e.getSlot()); return; }
        if (title.equals(CANNON_MENU_TITLE) || title.equals(CANNON_UPGRADE_TITLE)) {
            e.setCancelled(true);
            if(e.getClickedInventory()!=e.getView().getTopInventory()) return;
            if(title.equals(CANNON_UPGRADE_TITLE)){
                String cannonId=cannonUpgradeMenus.get(player.getUniqueId()); CannonTurret cannon=cannonId==null?null:cannons.get(cannonId);
                long now=System.currentTimeMillis();
                if(cannon==null || cannon.buildUntil>now || cannon.upgradeUntil>now){
                    if(cannon!=null){long remaining=cannon.buildUntil>now?cannon.buildUntil-now:cannon.upgradeUntil-now;player.sendMessage("§cEl cañón está "+(cannon.buildUntil>now?"en construcción":"mejorándose")+". No puedes acceder hasta que termine. Tiempo restante: §f"+formatDuration(remaining));}
                    player.closeInventory();return;
                }
            }
            if(title.equals(CANNON_MENU_TITLE)) handleCannonShopClick(player,e.getSlot()); else handleCannonUpgradeClick(player,e.getSlot());
            return;
        }
        if (!title.equals(MAIN_MENU_TITLE) && !title.equals(DETAILS_MENU_TITLE) && !title.equals(UPGRADES_MENU_TITLE) && !title.equals(EXTRA_UPGRADES_TITLE) && !mailTitle) return;
        if (title.equals(MAIL_ATTACH_TITLE)) {
            if (e.getClickedInventory() == e.getView().getTopInventory()) {
                int slot=e.getSlot();
                if(slot==0 || slot==8) { e.setCancelled(true); handleMailMenuClick(player,title,slot); }
                else if(slot<1 || slot>7) e.setCancelled(true);
            }
            return;
        }
        e.setCancelled(true);
        if (e.getClickedInventory() == null || e.getClickedInventory() != e.getView().getTopInventory()) return;
        if (mailTitle) { handleMailMenuClick(player, title, e.getSlot()); return; }
        if (title.equals(UPGRADES_MENU_TITLE)) { handleUpgradeClick(player, e.getSlot()); return; }
        if (title.equals(EXTRA_UPGRADES_TITLE)) { handleExtraUpgradeClick(player,e.getSlot()); return; }
        Kingdom k = kingdoms.get(openMenuKingdoms.get(player.getUniqueId()));
        if (k == null) return;
        if(k.nexusBuildingUntil>System.currentTimeMillis()){player.sendMessage("§eEl Nexo está en construcción. No se puede utilizar todavía.");player.closeInventory();return;}
        if (title.equals(MAIN_MENU_TITLE) && e.getSlot() == 13) openKingdomDetails(player, k);
        else if (title.equals(MAIN_MENU_TITLE) && e.getSlot() == 15) {
            Kingdom own = kingdomOf(player.getUniqueId());
            if (own == null || !key(own.name).equals(key(k.name))) {
                player.sendMessage("§cSolo los miembros de este reino pueden abrir su cofre del Nexo.");
                return;
            }
            openNexusChest(player, own);
        }
        else if (title.equals(MAIN_MENU_TITLE) && e.getSlot() == 21) {
            Kingdom own = kingdomOf(player.getUniqueId());
            if (own == null || !key(own.name).equals(key(k.name))) { player.sendMessage("§cSolo los miembros del reino pueden ver sus mejoras."); return; }
            openKingdomUpgrades(player, own);
        }
        else if (title.equals(MAIN_MENU_TITLE) && e.getSlot() == 17) { Kingdom own=kingdomOf(player.getUniqueId()); if(own==null || !key(own.name).equals(key(k.name)))return; openMineralMenu(player); }
        else if (title.equals(MAIN_MENU_TITLE) && e.getSlot() == 19) {
            Kingdom own=kingdomOf(player.getUniqueId());
            if(own==null || !key(own.name).equals(key(k.name))){player.sendMessage("§cSolo los miembros del reino pueden administrar defensas.");return;}
            openCannonShop(player,own);
        }
        else if (title.equals(MAIN_MENU_TITLE) && e.getSlot() == 23) {
            Kingdom own=kingdomOf(player.getUniqueId());
            if(own==null || !key(own.name).equals(key(k.name))){player.sendMessage("§cSolo los miembros del reino pueden comprar mejoras extra.");return;}
            openExtraUpgrades(player,own);
        }
        else if (title.equals(MAIN_MENU_TITLE) && e.getSlot() == 11) {
            Kingdom own = kingdomOf(player.getUniqueId());
            if (own == null || !key(own.name).equals(key(k.name))) {
                player.sendMessage("§cSolo los miembros de este reino pueden acceder a su casilla de correo.");
                return;
            }
            openMailMenu(player);
        }
        else if (title.equals(DETAILS_MENU_TITLE) && e.getSlot() == 18) openNexusMenu(player, k);
    }

    private void openCannonShop(Player p, Kingdom k) {
        Inventory inv=Bukkit.createInventory(null,27,CANNON_MENU_TITLE);
        inv.setItem(4,named(Material.TARGET,"§6§lDefensas del reino","§7Saldo: §d"+formatPL(k.moonstones)+" PL"));
        ItemStack cannonIcon=createCannonItem(); ItemMeta cannonMeta=cannonIcon.getItemMeta(); cannonMeta.setLore(Arrays.asList("§7Torreta defensiva del reino","§7Daño: §c2½ corazones","§7Disparo: §f1 segundo","§7Alcance: §f16 bloques","§7Precio: §d250 PL","§eClic para comprar")); cannonIcon.setItemMeta(cannonMeta); inv.setItem(13,cannonIcon);
        ItemStack infernoIcon=createInfernoItem(); ItemMeta infernoMeta=infernoIcon.getItemMeta(); infernoMeta.setLore(Arrays.asList("§7Torreta de rayo progresivo","§7Daño creciente al mantener fijación","§7Munición: §e300","§7Durabilidad: §f250 / 500 / 1000","§7Precio: §d1000 PL","§eClic para comprar")); infernoIcon.setItemMeta(infernoMeta); inv.setItem(15,infernoIcon);
        inv.setItem(22,named(Material.ARROW,"§eVolver al Nexo")); p.openInventory(inv);
    }
    private void handleCannonShopClick(Player p,int slot) {
        Kingdom k=kingdomOf(p.getUniqueId()); if(k==null){p.closeInventory();return;}
        if(slot==22){openNexusMenu(p,k);return;}
        if(slot==15){double cost=1000.0;if(k.moonstones+1.0E-9<cost){p.sendMessage("§cNecesitas 1000 PL para comprar una Inferno.");return;}k.moonstones=roundPL(k.moonstones-cost);ItemStack infernoItem=createInfernoItem();Map<Integer,ItemStack> overflow=p.getInventory().addItem(infernoItem);for(ItemStack extra:overflow.values())p.getWorld().dropItemNaturally(p.getLocation(),extra);saveData();p.sendMessage("§aHas comprado una Torreta Inferno por 1000 PL. Colócala sobre una valla dentro de tu reino.");openCannonShop(p,k);return;}
        if(slot!=13)return;
        double price=250.0; if(k.moonstones+1.0E-9<price){p.sendMessage("§cNecesitas 250 PL para comprar un Cañón.");return;}
        k.moonstones=roundPL(k.moonstones-price); ItemStack item=createCannonItem();
        Map<Integer,ItemStack> rest=p.getInventory().addItem(item); for(ItemStack extra:rest.values())p.getWorld().dropItemNaturally(p.getLocation(),extra);
        saveData(); p.sendMessage("§aHas comprado un Cañón de nivel I. Colócalo encima de una valla dentro de tu reino."); p.playSound(p.getLocation(),Sound.BLOCK_ANVIL_USE,0.8f,1.2f); openCannonShop(p,k);
    }
    private ItemStack createCannonItem() {
        ItemStack item=new ItemStack(Material.PLAYER_HEAD); ItemMeta raw=item.getItemMeta();
        if(raw instanceof org.bukkit.inventory.meta.SkullMeta meta){
            try { PlayerProfile profile=Bukkit.createPlayerProfile(UUID.fromString("c825f6c2-663b-9ac4-5e66-aa83150d3245")); PlayerTextures textures=profile.getTextures(); textures.setSkin(new java.net.URL(CANNON_TEXTURE)); profile.setTextures(textures); meta.setOwnerProfile(profile); } catch(Exception ex){getLogger().warning("No se pudo aplicar la textura del cañón: "+ex.getMessage());}
            meta.getPersistentDataContainer().set(cannonItemKey,PersistentDataType.BYTE,(byte)1);
            meta.setDisplayName("§6§lCañón §7(Nivel I)"); meta.setLore(Arrays.asList("§7Torreta defensiva del reino","§7Colócala encima de una valla","§7Daño: §c2½ corazones","§7Cadencia: §f1 segundo")); item.setItemMeta(meta);
        } return item;
    }
    private ItemStack createInfernoItem() {
        ItemStack item=new ItemStack(Material.PLAYER_HEAD); ItemMeta raw=item.getItemMeta();
        if(raw instanceof org.bukkit.inventory.meta.SkullMeta meta){
            try { PlayerProfile profile=Bukkit.createPlayerProfile(UUID.fromString("e8e75d95-f241-ae52-2072-11f0242ada73")); PlayerTextures textures=profile.getTextures(); textures.setSkin(new java.net.URL(INFERNO_TEXTURE)); profile.setTextures(textures); meta.setOwnerProfile(profile); } catch(Exception ex){getLogger().warning("No se pudo aplicar la textura Inferno: "+ex.getMessage());}
            meta.getPersistentDataContainer().set(infernoItemKey,PersistentDataType.BYTE,(byte)1);
            meta.setDisplayName("§c§lTorreta Inferno §7(Nivel I)");meta.setLore(Arrays.asList("§7Torreta defensiva de rayo progresivo","§7Colócala encima de una valla","§7Munición: 300","§7Durabilidad: 250"));item.setItemMeta(meta);
        } return item;
    }
    private int infernoMaxDurability(int level){return level==1?250:level==2?500:1000;}
    private void openInfernoUpgrade(Player p,InfernoTurret t){
        infernoUpgradeMenus.put(p.getUniqueId(),t.id);Inventory inv=Bukkit.createInventory(null,27,INFERNO_UPGRADE_TITLE);int max=infernoMaxDurability(t.level);
        inv.setItem(4,named(Material.PLAYER_HEAD,"§c§lInferno — Nivel "+roman(t.level),"§7Daño progresivo: §c"+(t.level==1?1:t.level==2?1.5:2)+" corazones iniciales","§7Munición: §e"+t.ammo+"/300","§7Durabilidad: §f"+t.durability+"/"+max));
        int needed=300-t.ammo;double reloadCost=roundPL(needed*0.10);inv.setItem(15,named(Material.FIRE_CHARGE,"§6§lRecargar munición","§7Munición: §e"+t.ammo+"/300","§7Añade: §f"+needed,"§7Coste: §d"+formatPL(reloadCost)+" PL","§eClic para recargar"));
        if(t.durability<=0){if(t.repairUntil>System.currentTimeMillis())inv.setItem(11,named(Material.ANVIL,"§e§lReparando Inferno","§7Tiempo: §f"+formatDuration(t.repairUntil-System.currentTimeMillis())));else inv.setItem(11,named(Material.IRON_INGOT,"§a§lReparar Inferno","§7Duración: §f2 minutos","§7Coste: §d150 PL","§eClic para iniciar"));}
        if(t.level<3){double cost=t.level==1?400:700;inv.setItem(13,named(Material.ANVIL,"§a§lMejorar a nivel "+roman(t.level+1),"§7Durabilidad máxima: §f"+infernoMaxDurability(t.level+1),"§7Coste: §d"+formatPL(cost)+" PL","§eClic para mejorar"));}else inv.setItem(13,named(Material.NETHER_STAR,"§6Nivel máximo"));
        inv.setItem(22,named(Material.ARROW,"§eCerrar"));p.openInventory(inv);
    }
    private void handleInfernoUpgradeClick(Player p,int slot){
        String id=infernoUpgradeMenus.get(p.getUniqueId());InfernoTurret t=id==null?null:infernos.get(id);if(t==null){p.closeInventory();return;}if(slot==22){p.closeInventory();return;}
        Kingdom k=kingdoms.get(t.kingdomKey);if(k==null||!isMember(p,k)){p.closeInventory();return;}long now=System.currentTimeMillis();
        if(t.upgradeUntil>now){
            p.sendMessage("§eLa Inferno ya está mejorándose. Tiempo restante: §f"+formatDuration(t.upgradeUntil-now)+"§e.");
            p.closeInventory();
            return;
        }
        if(slot==11&&t.durability<=0){if(t.repairUntil>now){p.sendMessage("§eLa Inferno ya está reparándose.");return;}if(k.moonstones<150){p.sendMessage("§cNecesitas 150 PL para reparar la Inferno.");return;}k.moonstones=roundPL(k.moonstones-150);t.repairUntil=now+120000L;saveData();p.sendMessage("§aReparación iniciada: 150 PL y 2 minutos.");openInfernoUpgrade(p,t);return;}
        if(t.durability<=0||t.repairUntil>now){p.sendMessage("§cLa Inferno está averiada o reparándose.");openInfernoUpgrade(p,t);return;}
        if(slot==15){int amount=300-t.ammo;if(amount<=0){p.sendMessage("§eLa munición ya está al máximo.");return;}double cost=roundPL(amount*0.10);if(k.moonstones<cost){p.sendMessage("§cNo tienes suficientes PL.");return;}k.moonstones=roundPL(k.moonstones-cost);t.ammo=300;saveData();openInfernoUpgrade(p,t);return;}
        if(slot==13&&t.level<3){double cost=t.level==1?400:700;if(k.moonstones<cost){p.sendMessage("§cNo tienes suficientes PL.");return;}k.moonstones=roundPL(k.moonstones-cost);t.pendingLevel=t.level+1;t.upgradeStarted=now;t.upgradeUntil=now+30000L;saveData();p.sendMessage("§eMejora de Inferno iniciada: 30 segundos.");p.closeInventory();}
    }

    private void openCannonUpgrade(Player p,CannonTurret turret) {
        cannonUpgradeMenus.put(p.getUniqueId(),turret.id); Inventory inv=Bukkit.createInventory(null,27,CANNON_UPGRADE_TITLE);
        int lvl=turret.level; double damage=lvl==1?2.5:lvl==2?3.5:4.5; int seconds=lvl==1?1:lvl==2?3:5;
        inv.setItem(4,named(Material.PLAYER_HEAD,"§6§lCañón — Nivel "+roman(lvl),"§7Daño: §c"+damage+" corazones","§7Tiempo por disparo: §f"+seconds+" segundo(s)","§7Munición: §e"+turret.ammo+" / 100"));
        double reloadCost=roundPL((100-turret.ammo)*0.30);
        inv.setItem(15,named(Material.FIRE_CHARGE,"§6§lRecargar munición","§7Munición actual: §e"+turret.ammo+" / 100","§7Añade: §f"+(100-turret.ammo),"§7Coste: §d"+formatPL(reloadCost)+" PL","§eClic para recargar"));
        if (turret.durability <= 0) {
            if (turret.repairUntil > System.currentTimeMillis()) {
                inv.setItem(11,named(Material.ANVIL,"§e§lReparación en curso","§7Tiempo restante: §f"+formatDuration(turret.repairUntil-System.currentTimeMillis()),"§8No necesita otra acción"));
            } else {
                inv.setItem(11,named(Material.IRON_INGOT,"§a§lReparar cañón","§7Durabilidad: §c0/300","§7Duración: §f2 minutos","§7Coste: §d150 PL","§eClic para iniciar"));
            }
        }
        if(lvl<3){double price=lvl==1?400.0:700.0;double nextDamage=lvl==1?3.5:4.5;inv.setItem(13,named(Material.ANVIL,"§a§lMejorar a nivel "+roman(lvl+1),"§7Daño: §c"+nextDamage+" corazones","§7Tiempo por disparo: §f"+(lvl+1==2?3:5)+" segundos","§7Coste: §d"+formatPL(price)+" PL","§eClic para mejorar"));}
        else inv.setItem(13,named(Material.NETHER_STAR,"§6Nivel máximo alcanzado"));
        inv.setItem(22,named(Material.ARROW,"§eCerrar"));p.openInventory(inv);
    }
    private void handleCannonUpgradeClick(Player p,int slot) {
        String id=cannonUpgradeMenus.get(p.getUniqueId()); CannonTurret t=id==null?null:cannons.get(id); if(t==null){p.closeInventory();return;}
        if(slot==22){p.closeInventory();return;}
        long now=System.currentTimeMillis();
        if(t.buildUntil>now || t.upgradeUntil>now){
            long remaining=t.buildUntil>now?t.buildUntil-now:t.upgradeUntil-now;
            p.sendMessage("§cNo puedes acceder al cañón mientras está "+(t.buildUntil>now?"en construcción":"mejorándose")+". Tiempo restante: §f"+formatDuration(remaining)+"§c.");
            p.closeInventory();return;
        }
        Kingdom k=kingdoms.get(t.kingdomKey); if(k==null||!isMember(p,k)){p.closeInventory();return;}
        if (slot == 11 && t.durability <= 0) {
            if (t.repairUntil > now) {
                p.sendMessage("§eEl cañón ya está siendo reparado. Tiempo restante: §f"+formatDuration(t.repairUntil-now));
                openCannonUpgrade(p,t);
                return;
            }
            final double repairCost = 150.0;
            if (k.moonstones + 1.0E-9 < repairCost) {
                p.sendMessage("§cNecesitas §d150 PL §cpara reparar este cañón. Saldo del reino: §d" + formatPL(k.moonstones) + " PL§c.");
                openCannonUpgrade(p,t);
                return;
            }
            k.moonstones = roundPL(k.moonstones - repairCost);
            t.repairUntil = now + 120000L;
            saveData();
            p.playSound(p.getLocation(),Sound.BLOCK_ANVIL_USE,1f,0.8f);
            p.sendMessage("§aHas iniciado la reparación del cañón. Coste: §d150 PL§a. Estará listo en 2 minutos.");
            openCannonUpgrade(p,t);
            return;
        }
        if (t.durability <= 0) {
            p.sendMessage("§cEl cañón está averiado. Debes repararlo antes de administrarlo.");
            openCannonUpgrade(p,t);
            return;
        }
        if(slot==15){
            int needed=100-t.ammo;
            if(needed<=0){p.sendMessage("§eEl cañón ya tiene la munición al máximo.");return;}
            double cost=roundPL(needed*0.30);
            if(k.moonstones+1.0E-9<cost){p.sendMessage("§cNecesitas "+formatPL(cost)+" PL para recargar el cañón.");return;}
            k.moonstones=roundPL(k.moonstones-cost);t.ammo=100;saveData();
            p.playSound(p.getLocation(),Sound.ITEM_FIRECHARGE_USE,0.8f,1.2f);
            p.sendMessage("§aCañón recargado: 100/100 municiones. Coste: "+formatPL(cost)+" PL.");openCannonUpgrade(p,t);return;
        }
        if(slot!=13||t.level>=3)return;
        double price=t.level==1?400.0:700.0; if(k.moonstones+1.0E-9<price){p.sendMessage("§cNo tienes suficientes PL.");return;}
        if(t.upgradeUntil>System.currentTimeMillis()){p.sendMessage("§eEl cañón ya está en construcción de mejora.");return;}
        k.moonstones=roundPL(k.moonstones-price);t.pendingLevel=t.level+1;t.upgradeUntil=System.currentTimeMillis()+30000L;t.nextShotAt=t.upgradeUntil; saveData();
        p.playSound(p.getLocation(),Sound.BLOCK_ANVIL_USE,1f,0.8f);p.sendMessage("§eMejora iniciada. No podrás acceder al cañón durante 30 segundos. Seguirá recibiendo daño.");p.closeInventory();
    }
    private boolean isFence(Material m){return m.name().endsWith("_FENCE");}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onCannonPlace(BlockPlaceEvent e) {
        ItemStack hand=e.getItemInHand();
        if(hand.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta infernoMeta && infernoMeta.getPersistentDataContainer().has(infernoItemKey,PersistentDataType.BYTE)){
            Block placed=e.getBlockPlaced();Block below=placed.getRelative(BlockFace.DOWN);Kingdom kingdom=kingdomAt(placed.getChunk());
            if(!isFence(below.getType())||kingdom==null||!isMember(e.getPlayer(),kingdom)){e.setCancelled(true);e.getPlayer().sendMessage("§cLa Inferno debe colocarse sobre una valla dentro de tu reino.");return;}
            long count=infernos.values().stream().filter(t->t.kingdomKey.equals(key(kingdom.name))&&t.world.equals(placed.getWorld().getName())&&(t.x>>4)==placed.getChunk().getX()&&(t.z>>4)==placed.getChunk().getZ()).count()+cannons.values().stream().filter(t->t.kingdomKey.equals(key(kingdom.name))&&t.world.equals(placed.getWorld().getName())&&(t.x>>4)==placed.getChunk().getX()&&(t.z>>4)==placed.getChunk().getZ()).count();
            int max=1+extraLevel(kingdom,"defenses");if(count>=max){e.setCancelled(true);e.getPlayer().sendMessage("§cEste chunk ya alcanzó su límite de torretas: "+max+".");return;}
            String id=turretId(placed.getLocation());applyInfernoTexture(placed);InfernoTurret turret=new InfernoTurret(id,key(kingdom.name),placed.getWorld().getName(),placed.getX(),placed.getY(),placed.getZ(),1,System.currentTimeMillis()+30000L);infernos.put(id,turret);saveData();e.getPlayer().sendMessage("§cTorreta Inferno colocada. Construcción: 30 segundos.");return;
        }
        if(!(hand.getItemMeta() instanceof org.bukkit.inventory.meta.SkullMeta meta)||!meta.getPersistentDataContainer().has(cannonItemKey,PersistentDataType.BYTE))return;
        Block placed=e.getBlockPlaced(); Block below=placed.getRelative(BlockFace.DOWN); Kingdom k=kingdomAt(placed.getChunk());
        if(!isFence(below.getType())){e.setCancelled(true);e.getPlayer().sendMessage("§cEl Cañón debe colocarse encima de una valla de madera o del Nether.");return;}
        if(k==null||!isMember(e.getPlayer(),k)){e.setCancelled(true);e.getPlayer().sendMessage("§cSolo puedes construir cañones dentro de tu propio reino.");return;}
        long inChunk=cannons.values().stream().filter(t->t.kingdomKey.equals(key(k.name))&&t.world.equals(placed.getWorld().getName())&&(t.x>>4)==placed.getChunk().getX()&&(t.z>>4)==placed.getChunk().getZ()).count()+infernos.values().stream().filter(t->t.kingdomKey.equals(key(k.name))&&t.world.equals(placed.getWorld().getName())&&(t.x>>4)==placed.getChunk().getX()&&(t.z>>4)==placed.getChunk().getZ()).count();
        int maxPerChunk=1+extraLevel(k,"defenses");
        if(inChunk>=maxPerChunk){e.setCancelled(true);e.getPlayer().sendMessage("§cEste chunk ya alcanzó su límite de torretas: "+maxPerChunk+".");return;}
        String id=turretId(placed.getLocation()); Skull skull=(Skull)placed.getState();
        try { PlayerProfile profile=Bukkit.createPlayerProfile(UUID.fromString("c825f6c2-663b-9ac4-5e66-aa83150d3245"));PlayerTextures textures=profile.getTextures();textures.setSkin(new java.net.URL(CANNON_TEXTURE));profile.setTextures(textures);skull.setOwnerProfile(profile);skull.update(); }catch(Exception ex){getLogger().warning("Textura de cañón no aplicada: "+ex.getMessage());}
        CannonTurret turret=new CannonTurret(id,key(k.name),placed.getWorld().getName(),placed.getX(),placed.getY(),placed.getZ(),1,System.currentTimeMillis()+30000L);
        cannons.put(id,turret);saveData();e.getPlayer().sendMessage("§eCañón colocado. Construcción: §f30 segundos§e.");e.getPlayer().playSound(placed.getLocation(),Sound.BLOCK_ANVIL_PLACE,1f,0.9f);
    }
    private void applyInfernoTexture(Block placed){if(!(placed.getState() instanceof Skull skull))return;try{PlayerProfile profile=Bukkit.createPlayerProfile(UUID.fromString("e8e75d95-f241-ae52-2072-11f0242ada73"));PlayerTextures textures=profile.getTextures();textures.setSkin(new java.net.URL(INFERNO_TEXTURE));profile.setTextures(textures);skull.setOwnerProfile(profile);skull.update();}catch(Exception ex){getLogger().warning("Textura Inferno no aplicada: "+ex.getMessage());}}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onInfernoBreak(BlockBreakEvent e){String id=turretId(e.getBlock().getLocation());InfernoTurret t=infernos.get(id);if(t==null)return;Kingdom k=kingdoms.get(t.kingdomKey);if(k==null||!isMember(e.getPlayer(),k)){e.setCancelled(true);return;}e.setDropItems(false);e.setExpToDrop(0);removeInfernoEntities(t);infernos.remove(id);e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(),createInfernoItem());saveData();e.getPlayer().sendMessage("§eHas desmontado la Torreta Inferno.");}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onInfernoInteract(PlayerInteractEvent e){if(e.getAction()!=Action.RIGHT_CLICK_BLOCK||e.getClickedBlock()==null)return;InfernoTurret t=infernos.get(turretId(e.getClickedBlock().getLocation()));if(t==null)return;e.setCancelled(true);Kingdom k=kingdoms.get(t.kingdomKey);if(k==null||!isMember(e.getPlayer(),k)){e.getPlayer().sendMessage("§cSolo los miembros pueden administrar esta Inferno.");return;}long now=System.currentTimeMillis();if(t.upgradeUntil>now){e.getPlayer().sendMessage("§bLa Inferno está mejorándose a nivel "+roman(t.pendingLevel)+". Tiempo restante: §f"+formatDuration(t.upgradeUntil-now));return;}openInfernoUpgrade(e.getPlayer(),t);}
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onCannonBreak(BlockBreakEvent e) {
        String id=turretId(e.getBlock().getLocation());CannonTurret t=cannons.get(id);if(t==null)return;Kingdom k=kingdoms.get(t.kingdomKey);
        if(k==null||!isMember(e.getPlayer(),k)){e.setCancelled(true);return;}
        e.setDropItems(false);e.setExpToDrop(0);removeCannonHologram(id);cannons.remove(id);e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(),createCannonItem());saveData();e.getPlayer().sendMessage("§eHas desmontado el Cañón.");
    }
    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onCannonInteract(PlayerInteractEvent e) {
        if(e.getAction()!=Action.RIGHT_CLICK_BLOCK||e.getClickedBlock()==null)return;String id=turretId(e.getClickedBlock().getLocation());CannonTurret t=cannons.get(id);if(t==null)return;
        e.setCancelled(true);Kingdom k=kingdoms.get(t.kingdomKey);if(k==null||!isMember(e.getPlayer(),k)){e.getPlayer().sendMessage("§cSolo los miembros del reino pueden administrar este Cañón.");return;}
        long now=System.currentTimeMillis();
        if(t.buildUntil>now || t.upgradeUntil>now){long remaining=t.buildUntil>now?t.buildUntil-now:t.upgradeUntil-now;e.getPlayer().sendMessage("§cNo puedes acceder al cañón mientras está "+(t.buildUntil>now?"en construcción":"mejorándose")+". Tiempo restante: §f"+formatDuration(remaining)+"§c.");return;}
        openCannonUpgrade(e.getPlayer(),t);
    }
    private String turretId(Location l){return l.getWorld().getName()+":"+l.getBlockX()+":"+l.getBlockY()+":"+l.getBlockZ();}
    private void tickInfernos(){long now=System.currentTimeMillis();List<String> invalid=new ArrayList<>();for(InfernoTurret t:infernos.values()){
        World w=Bukkit.getWorld(t.world);if(w==null||!w.isChunkLoaded(t.x>>4,t.z>>4))continue;Block block=w.getBlockAt(t.x,t.y,t.z);if(!isFence(block.getRelative(BlockFace.DOWN).getType())||(block.getType()!=Material.PLAYER_HEAD&&block.getType()!=Material.PLAYER_WALL_HEAD)){removeInfernoEntities(t);if(block.getType()==Material.PLAYER_HEAD||block.getType()==Material.PLAYER_WALL_HEAD)block.setType(Material.AIR,false);invalid.add(t.id);continue;}
        if(t.upgradeUntil>0&&t.upgradeUntil<=now){t.level=t.pendingLevel;t.pendingLevel=0;t.upgradeStarted=0;t.upgradeUntil=0;t.durability=infernoMaxDurability(t.level);t.nextDamageAt=now;saveData();}
        if(t.durability<=0&&t.repairUntil>0&&t.repairUntil<=now){t.durability=infernoMaxDurability(t.level);t.repairUntil=0;t.nextDamageAt=now;saveData();}
        updateInfernoHologram(t,w,now);if(t.buildUntil>now||t.upgradeUntil>now||t.durability<=0||t.repairUntil>now||t.ammo<=0){t.target=null;t.lockStarted=0;continue;}
        Location origin=new Location(w,t.x+0.5,t.y+0.85,t.z+0.5);Kingdom kingdom=kingdoms.get(t.kingdomKey);if(kingdom==null)continue;
        LivingEntity target=w.getNearbyLivingEntities(origin,16,8,16).stream().filter(x->x.isValid()&&!x.isDead()).filter(x->{if(x instanceof Player pl)return !isMember(pl,kingdom)&&!isNationAlly(pl,kingdom)&&kingdomAt(pl.getLocation().getChunk())==kingdom;if(x instanceof Monster)return kingdomAt(x.getLocation().getChunk())==kingdom&&!isGuard(x,kingdom);return false;}).filter(x->origin.distanceSquared(x.getLocation())<=256).filter(x->w.rayTraceBlocks(origin,x.getEyeLocation().toVector().subtract(origin.toVector()).normalize(),origin.distance(x.getEyeLocation()),org.bukkit.FluidCollisionMode.NEVER,true)==null).min(Comparator.comparingDouble(x->x.getLocation().distanceSquared(origin))).orElse(null);
        if(target==null){t.target=null;t.lockStarted=0;t.nextDamageAt=now;continue;}
        if(t.target==null||!t.target.equals(target.getUniqueId())){t.target=target.getUniqueId();t.lockStarted=now;t.nextDamageAt=now+1000L;}
        long lockedSeconds=Math.max(0L,(now-t.lockStarted)/1000L);
        // Apuntar al pecho (55% de la altura), no a los ojos. El tick de Inferno
        // corre cada 2 ticks para que el seguimiento sea más fluido.
        Location chestTarget = target.getLocation().clone().add(0.0, Math.max(0.35, target.getHeight() * 0.55), 0.0);
        drawInfernoFireBeam(w, origin, chestTarget, lockedSeconds);
        if(now>=t.nextDamageAt){long elapsed=(now-t.lockStarted)/1000L;double damage=t.level==1?(elapsed<=1?2.0:elapsed==2?4.0:6.0):t.level==2?(elapsed<=1?3.0:elapsed==2?6.0:8.0):(elapsed<=1?4.0:elapsed==2?8.0:10.0);target.damage(damage);t.ammo--;t.nextDamageAt=now+1000L;w.playSound(target.getLocation(),Sound.BLOCK_BEACON_AMBIENT,0.25f,1.8f);saveData();}
    }if(!invalid.isEmpty()){for(String id:invalid)infernos.remove(id);saveData();}}
    /** Rayo de fuego compacto: sin entidades ni proyectiles y con baja densidad de partículas. */
    private void drawInfernoFireBeam(World world, Location from, Location to, long lockedSeconds) {
        Vector delta = to.toVector().subtract(from.toVector());
        double distance = delta.length();
        if (distance < 0.1) return;

        Vector direction = delta.clone().normalize();
        int phase = lockedSeconds <= 0 ? 1 : lockedSeconds == 1 ? 2 : 3;
        // Menos puntos por metro: evita la nube de partículas que producía el rayo anterior.
        double step = 0.36;
        double width = phase == 1 ? 0.012 : phase == 2 ? 0.028 : 0.050;
        int particlesPerPoint = phase == 1 ? 1 : phase == 2 ? 2 : 3;

        Vector perpendicular = direction.clone().crossProduct(new Vector(0, 1, 0));
        if (perpendicular.lengthSquared() < 0.001) {
            perpendicular = direction.clone().crossProduct(new Vector(1, 0, 0));
        }
        perpendicular.normalize();
        Vector second = direction.clone().crossProduct(perpendicular).normalize();

        for (double d = 0.0; d <= distance; d += step) {
            Location point = from.clone().add(direction.clone().multiply(d));
            world.spawnParticle(Particle.FLAME, point, 1, 0, 0, 0, 0.0);
            if (particlesPerPoint >= 2) {
                Location side = point.clone().add(perpendicular.clone().multiply(width));
                world.spawnParticle(Particle.FLAME, side, 1, 0, 0, 0, 0.0);
            }
            if (particlesPerPoint >= 3) {
                Location otherSide = point.clone().subtract(perpendicular.clone().multiply(width))
                        .add(second.clone().multiply(width * 0.35));
                world.spawnParticle(Particle.FLAME, otherSide, 1, 0, 0, 0, 0.0);
            }
        }
    }
    private void removeInfernoGuardian(InfernoTurret t){/* La Inferno ya no utiliza entidades auxiliares. */}
    private void cleanupInfernoVisualsOnStartup(){
        for(World world:Bukkit.getWorlds())for(Entity entity:world.getEntities()){
            if(entity instanceof ArmorStand stand&&stand.getScoreboardTags().contains("shadowkingdoms_inferno_hologram"))stand.remove();
            else if(entity.getScoreboardTags().stream().anyMatch(tag->tag.startsWith("shadowkingdoms_inferno_guardian_"))&&(entity instanceof org.bukkit.entity.Guardian||entity instanceof org.bukkit.entity.ElderGuardian))entity.remove();
        }
        infernoHolograms.clear();
    }
    private void removeInfernoEntities(InfernoTurret t){removeInfernoGuardian(t);List<ArmorStand> stands=infernoHolograms.remove(t.id);if(stands!=null)for(ArmorStand a:stands)if(a.isValid())a.remove();}
    private void updateInfernoHologram(InfernoTurret t, World w, long now) {
        // La Inferno utiliza SIEMPRE cuatro ArmorStands: título, durabilidad,
        // estado y munición. No cambiamos el número de líneas al pasar de ROTO
        // a REPARANDO, evitando que queden líneas antiguas superpuestas.
        List<ArmorStand> stands = infernoHolograms.get(t.id);
        if (stands == null) stands = new ArrayList<>();

        // Recupera o reconstruye las cuatro líneas si alguna entidad desapareció.
        if (stands.size() != 4 || stands.stream().anyMatch(a -> a == null || !a.isValid())) {
            for (ArmorStand old : stands) if (old != null && old.isValid()) old.remove();
            stands = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                final int line = i;
                ArmorStand stand = w.spawn(new Location(w, t.x + 0.5, t.y + 1.5 - line * 0.25, t.z + 0.5), ArmorStand.class, x -> {
                    x.setInvisible(true);
                    x.setMarker(true);
                    x.setGravity(false);
                    x.setInvulnerable(true);
                    x.setCustomNameVisible(true);
                    x.addScoreboardTag("shadowkingdoms_inferno_hologram");
                });
                stands.add(stand);
            }
            infernoHolograms.put(t.id, stands);
        }

        // Elimina restos de hologramas de versiones anteriores en ESTA torreta.
        // La comprobación horizontal evita tocar hologramas de otras estructuras cercanas.
        for (Entity entity : w.getNearbyEntities(new Location(w, t.x + 0.5, t.y + 1.5, t.z + 0.5), 0.20, 1.1, 0.20)) {
            if (!(entity instanceof ArmorStand old) || stands.contains(old)) continue;
            if (old.getScoreboardTags().contains("shadowkingdoms_inferno_hologram")) old.remove();
        }

        setInfernoHologramLine(stands.get(0), "§c§lINFERNO §7— §eNivel " + roman(t.level));
        setInfernoHologramLine(stands.get(1), "§cDurabilidad: §f" + t.durability + "/" + infernoMaxDurability(t.level));

        String status;
        if (t.repairUntil > now) {
            status = "§e§lREPARANDO §7" + formatDuration(t.repairUntil - now);
        } else if (t.durability <= 0) {
            status = "§c§lROTO";
        } else if (t.upgradeUntil > now) {
            long duration = Math.max(1L, t.upgradeUntil - t.upgradeStarted);
            int progress = (int) Math.max(0, Math.min(100, ((now - t.upgradeStarted) * 100L) / duration));
            status = "§b⚒ Mejorando a nivel " + roman(t.pendingLevel) + " §7" + progress + "%";
        } else {
            status = "";
        }

        // Una sola línea de estado: nunca se muestran ROTO y REPARANDO a la vez.
        setInfernoHologramLine(stands.get(2), status);
        setInfernoHologramLine(stands.get(3), "§eMunición: §f" + t.ammo + "/300");
    }
    private void setInfernoHologramLine(ArmorStand stand,String text){if(!Objects.equals(stand.getCustomName(),text))stand.setCustomName(text);}

    private void tickCannons(){
        long now=System.currentTimeMillis();
        List<String> brokenTurrets = new ArrayList<>();
        for(CannonTurret t:cannons.values()){
            World w=Bukkit.getWorld(t.world);
            if(w==null||!w.isChunkLoaded(t.x>>4,t.z>>4))continue;
            Block b=w.getBlockAt(t.x,t.y,t.z);
            // Toda torreta debe permanecer montada sobre una valla. Si el soporte
            // desaparece o deja de ser una valla, la torreta se destruye por completo.
            Block support = b.getRelative(BlockFace.DOWN);
            if(!isFence(support.getType())){
                if(b.getType()==Material.PLAYER_HEAD||b.getType()==Material.PLAYER_WALL_HEAD){
                    b.setType(Material.AIR,false);
                }
                removeCannonHologram(t.id);
                removeTurretHitbox(t.id,w);
                brokenTurrets.add(t.id);
                continue;
            }
            if(b.getType()!=Material.PLAYER_HEAD&&b.getType()!=Material.PLAYER_WALL_HEAD){removeCannonHologram(t.id);removeTurretHitbox(t.id,w);brokenTurrets.add(t.id);continue;}
            ensureTurretHitbox(t,w);
            if(t.upgradeUntil>0L&&t.upgradeUntil<=now){t.level=t.pendingLevel;t.pendingLevel=0;t.upgradeUntil=0L;t.nextShotAt=now;saveData();}
            if (t.durability <= 0 && t.repairUntil > 0L && t.repairUntil <= now) {
                t.durability = 300;
                t.repairUntil = 0L;
                t.nextShotAt = now;
                saveData();
                w.playSound(new Location(w,t.x+0.5,t.y+1.0,t.z+0.5),Sound.BLOCK_ANVIL_USE,1f,1.2f);
                w.spawnParticle(Particle.HAPPY_VILLAGER,new Location(w,t.x+0.5,t.y+1.0,t.z+0.5),12,0.3,0.4,0.3,0.02);
            }
            updateCannonHologram(t,w,now);
            if(t.durability <= 0 || t.repairUntil > now || t.buildUntil>now||t.upgradeUntil>now||t.nextShotAt>now)continue;
            Kingdom k=kingdoms.get(t.kingdomKey);if(k==null)continue;
            Location origin=b.getLocation().add(0.5,0.65,0.5);
            LivingEntity target=w.getNearbyLivingEntities(origin,16,8,16).stream()
                .filter(x->x.isValid()&&!x.isDead())
                .filter(x->{if(x instanceof Player pl)return !isMember(pl,k)&&!isNationAlly(pl,k)&&kingdomAt(pl.getLocation().getChunk())==k;if(x instanceof Monster)return kingdomAt(x.getLocation().getChunk())==k&&!isGuard(x,k);return false;})
                .filter(x->origin.distanceSquared(x.getLocation())<=256.0)
                .filter(x->w.rayTraceBlocks(origin,x.getEyeLocation().toVector().subtract(origin.toVector()).normalize(),origin.distance(x.getEyeLocation()),org.bukkit.FluidCollisionMode.NEVER,true)==null)
                .min(Comparator.comparingDouble(x->x.getLocation().distanceSquared(origin))).orElse(null);
            if(target==null||t.ammo<=0)continue;
            Vector delta=target.getEyeLocation().toVector().subtract(origin.toVector());
            // La cara frontal de la textura personalizada del cañón está orientada
            // al sentido contrario al frente que Bukkit asigna al PLAYER_HEAD.
            // Compensamos 180 grados para que el cañón mire hacia el objetivo.
            BlockFace desired=faceForYaw((float)Math.toDegrees(Math.atan2(-delta.getX(),delta.getZ()))+180.0f);
            if(b.getBlockData() instanceof Rotatable rotatable && rotatable.getRotation()!=desired){
                rotatable.setRotation(desired);b.setBlockData(rotatable,true);t.aimReadyAt=now+350L;continue;
            }
            if(t.aimReadyAt>now)continue;
            Vector direction=delta.normalize();
            Snowball shot=w.spawn(origin,Snowball.class);shot.setShooter(null);shot.setVelocity(direction.multiply(2.0));
            shot.addScoreboardTag("shadowkingdoms_cannon_projectile");shot.addScoreboardTag("shadowkingdoms_cannon_owner_"+t.kingdomKey);shot.addScoreboardTag("shadowkingdoms_cannon_level_"+t.level);
            t.ammo--;t.nextShotAt=now+cannonCooldownMillis(t.level);
            if(t.level>=2)w.spawnParticle(Particle.SMOKE,origin.clone().add(0,0.35,0),t.level==2?8:16,0.12,0.35,0.12,0.015);
            saveData();
        }
        if(!brokenTurrets.isEmpty()){
            for(String id:brokenTurrets)cannons.remove(id);
            saveData();
        }
    }
    // Hitbox invisible individual: hace que los proyectiles colisionen con el cuerpo de la torreta.
    private String turretHitboxTag(String id) { return "shadowkingdoms_turret_hitbox_" + id.replace(':', '_'); }

    private void ensureTurretHitbox(CannonTurret turret, World world) {
        String tag = turretHitboxTag(turret.id);
        for (Entity entity : world.getEntities()) {
            if (entity instanceof ArmorStand stand && stand.getScoreboardTags().contains(tag)) return;
        }
        Location location = new Location(world, turret.x + 0.5, turret.y, turret.z + 0.5);
        world.spawn(location, ArmorStand.class, stand -> {
            stand.setVisible(false); stand.setInvisible(true); stand.setMarker(false); stand.setSmall(false);
            stand.setGravity(false); stand.setBasePlate(false); stand.setArms(false); stand.setInvulnerable(true);
            stand.setSilent(true); stand.setPersistent(true); stand.setRemoveWhenFarAway(false); stand.setCollidable(true);
            stand.addScoreboardTag("shadowkingdoms_turret_hitbox"); stand.addScoreboardTag(tag);
        });
    }

    private void removeTurretHitbox(String id, World world) {
        String tag = turretHitboxTag(id);
        for (Entity entity : world.getEntities()) if (entity instanceof ArmorStand stand && stand.getScoreboardTags().contains(tag)) stand.remove();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWindChargeHitTurret(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (!isWindCharge(projectile)) return;

        // Respaldo para servidores que eliminan el proyectil en el mismo tick del impacto.
        Entity hit = event.getHitEntity();
        if (hit instanceof ArmorStand stand && stand.getScoreboardTags().contains("shadowkingdoms_turret_hitbox")) {
            CannonTurret turret = findTurretForHitbox(stand);
            if (turret != null) applyWindChargeDamage(projectile, turret);
            return;
        }
        Block hitBlock = event.getHitBlock();
        if (hitBlock != null) {
            Location impact = hitBlock.getLocation().add(0.5, 0.5, 0.5);
            CannonTurret nearby = cannons.values().stream()
                .filter(t -> t.world.equals(impact.getWorld().getName()))
                .filter(t -> Math.abs((t.x + 0.5) - impact.getX()) <= 1.15
                    && Math.abs((t.y + 0.5) - impact.getY()) <= 1.65
                    && Math.abs((t.z + 0.5) - impact.getZ()) <= 1.15)
                .findFirst().orElse(null);
            if (nearby != null) applyWindChargeDamage(projectile, nearby); else { InfernoTurret it=infernos.values().stream().filter(t->t.world.equals(impact.getWorld().getName())&&Math.abs(t.x+0.5-impact.getX())<=1.15&&Math.abs(t.y+0.5-impact.getY())<=1.65&&Math.abs(t.z+0.5-impact.getZ())<=1.15).findFirst().orElse(null);if(it!=null)applyInfernoWindDamage(projectile,it); }
        }
    }

    private boolean isWindCharge(Entity entity) {
        if (!(entity instanceof Projectile)) return false;
        String type = entity.getType().name();
        return type.equals("WIND_CHARGE") || type.equals("BREEZE_WIND_CHARGE")
            || type.endsWith("_WIND_CHARGE");
    }

    private CannonTurret findTurretForHitbox(ArmorStand stand) {
        return cannons.values().stream()
            .filter(candidate -> stand.getScoreboardTags().contains(turretHitboxTag(candidate.id)))
            .findFirst().orElse(null);
    }

    // La colisión no depende de que Minecraft genere un EntityDamageByEntityEvent:
    // se barre el segmento recorrido por el proyectil entre dos ticks consecutivos.
    private void tickWindChargeCollisions() {
        Set<UUID> seen = new HashSet<>();
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!isWindCharge(entity)) continue;
                Projectile projectile = (Projectile) entity;
                UUID id = projectile.getUniqueId();
                seen.add(id);
                if (windChargeHandled.contains(id)) continue;

                Location current = projectile.getLocation().clone();
                Location previous = windChargePreviousLocations.put(id, current);
                if (previous == null || !previous.getWorld().equals(current.getWorld())) {
                    if (isPointInsideAnyTurret(current)) {
                        CannonTurret turret = turretAtPoint(current);
                        if (turret != null) registerWindChargeHit(projectile, turret); else { InfernoTurret it=infernoAtPoint(current); if(it!=null) applyInfernoWindDamage(projectile,it); }
                    }
                    continue;
                }

                Vector from = previous.toVector();
                Vector delta = current.toVector().subtract(from);
                double length = delta.length();
                int steps = Math.max(1, (int) Math.ceil(length / 0.08));
                for (int step = 0; step <= steps; step++) {
                    Location sample = previous.clone().add(delta.clone().multiply(step / (double) steps));
                    CannonTurret turret = turretAtPoint(sample);
                    if (turret != null) { registerWindChargeHit(projectile, turret); break; }
                    InfernoTurret inferno=infernoAtPoint(sample);if(inferno!=null){applyInfernoWindDamage(projectile,inferno);break;}
                }
            }
        }
        windChargePreviousLocations.keySet().removeIf(id -> !seen.contains(id));
        windChargeHandled.removeIf(id -> !seen.contains(id));
    }

    private boolean isPointInsideAnyTurret(Location point) {
        return turretAtPoint(point) != null;
    }

    private CannonTurret turretAtPoint(Location point) {
        if (point.getWorld() == null) return null;
        for (CannonTurret turret : cannons.values()) {
            if (!turret.world.equals(point.getWorld().getName())) continue;
            // Hitbox generosa: cubre el cuerpo del cañón y su cabeza, no solo la valla.
            if (point.getX() >= turret.x - 0.20 && point.getX() <= turret.x + 1.20
                && point.getY() >= turret.y - 0.05 && point.getY() <= turret.y + 1.65
                && point.getZ() >= turret.z - 0.20 && point.getZ() <= turret.z + 1.20) return turret;
        }
        return null;
    }

    private InfernoTurret infernoAtPoint(Location point){if(point.getWorld()==null)return null;for(InfernoTurret t:infernos.values())if(t.world.equals(point.getWorld().getName())&&point.getX()>=t.x-0.2&&point.getX()<=t.x+1.2&&point.getY()>=t.y-0.05&&point.getY()<=t.y+1.65&&point.getZ()>=t.z-0.2&&point.getZ()<=t.z+1.2)return t;return null;}
    private void applyInfernoWindDamage(Projectile projectile,InfernoTurret t){if(windChargeHandled.contains(projectile.getUniqueId())||t.durability<=0)return;if(projectile.getShooter() instanceof Player attacker){Kingdom owner=kingdoms.get(t.kingdomKey);if(owner!=null&&(isMember(attacker,owner)||isNationAlly(attacker,owner)))return;}if(!windChargeHandled.add(projectile.getUniqueId()))return;t.durability=Math.max(0,t.durability-20);World w=Bukkit.getWorld(t.world);if(w!=null){Location at=new Location(w,t.x+0.5,t.y+1,t.z+0.5);w.playSound(at,Sound.BLOCK_ANVIL_LAND,0.8f,1.5f);w.spawnParticle(Particle.CRIT,at,10,0.25,0.3,0.25,0.04);}if(t.durability==0){t.target=null;t.repairUntil=0;removeInfernoGuardian(t);}saveData();}
    private void registerWindChargeHit(Projectile projectile, CannonTurret turret) {
        applyWindChargeDamage(projectile, turret);
    }

    private void applyWindChargeDamage(Projectile projectile, CannonTurret turret) {
        if (windChargeHandled.contains(projectile.getUniqueId())) return;
        if (projectile.getShooter() instanceof Player attacker) {
            Kingdom owner = kingdoms.get(turret.kingdomKey);
            if (owner != null && (isMember(attacker, owner) || isNationAlly(attacker, owner))) return;
        }
        if (!windChargeHandled.add(projectile.getUniqueId())) return;
        if (turret.durability <= 0) return;
        turret.durability = Math.max(0, turret.durability - 50);
        World world = Bukkit.getWorld(turret.world);
        if (world == null) { saveData(); return; }
        Location impact = new Location(world, turret.x + 0.5, turret.y + 1.0, turret.z + 0.5);
        world.playSound(impact, Sound.BLOCK_ANVIL_LAND, 0.8f, 1.5f);
        world.spawnParticle(Particle.CRIT, impact, 12, 0.25, 0.35, 0.25, 0.05);
        if (turret.durability <= 0) {
            turret.durability = 0;
            turret.repairUntil = 0L;
            world.playSound(impact, Sound.BLOCK_ANVIL_BREAK, 1f, 0.8f);
        }
        updateCannonHologram(turret, world, System.currentTimeMillis());
        saveData();
    }

    private BlockFace faceForYaw(float yaw){
        BlockFace[] faces={BlockFace.SOUTH,BlockFace.SOUTH_SOUTH_WEST,BlockFace.SOUTH_WEST,BlockFace.WEST_SOUTH_WEST,BlockFace.WEST,BlockFace.WEST_NORTH_WEST,BlockFace.NORTH_WEST,BlockFace.NORTH_NORTH_WEST,BlockFace.NORTH,BlockFace.NORTH_NORTH_EAST,BlockFace.NORTH_EAST,BlockFace.EAST_NORTH_EAST,BlockFace.EAST,BlockFace.EAST_SOUTH_EAST,BlockFace.SOUTH_EAST,BlockFace.SOUTH_SOUTH_EAST};
        int index=Math.floorMod(Math.round(yaw/22.5f),16);return faces[index];
    }

    private void updateCannonHologram(CannonTurret turret, World world, long now) {
        List<ArmorStand> stands = cannonHolograms.get(turret.id);
        boolean building = turret.buildUntil > now;
        boolean upgrading = !building && turret.upgradeUntil > now;
        boolean underConstruction = building || upgrading;
        boolean damaged = !underConstruction && turret.durability <= 0;
        int desiredStandCount = underConstruction ? 2 : (damaged ? 4 : 3);
        if (stands == null || stands.stream().anyMatch(stand -> !stand.isValid()) || stands.size() != desiredStandCount) {
            removeCannonHologram(turret.id);
            stands = new ArrayList<>();
            Location base = new Location(world, turret.x + 0.5, turret.y + 0.95, turret.z + 0.5);
            int count = desiredStandCount;
            for (int i=0;i<count;i++) {
                ArmorStand stand = world.spawn(base.clone().add(0, underConstruction ? (i==0?0.28:0) : (0.60-i*0.25), 0), ArmorStand.class, as -> {
                    as.setVisible(false); as.setMarker(true); as.setGravity(false);
                    as.setInvulnerable(true); as.setPersistent(false); as.setCustomNameVisible(true);
                    as.addScoreboardTag("shadowkingdoms_cannon_hologram");
                    as.addScoreboardTag("shadowkingdoms_cannon_hologram_" + turret.id.replace(':','_'));
                });
                stands.add(stand);
            }
            cannonHolograms.put(turret.id, stands);
        }
        if (underConstruction) {
            long remaining = building ? turret.buildUntil - now : turret.upgradeUntil - now;
            int percent = (int)Math.max(0, Math.min(100, 100 - Math.ceil(remaining / 300.0)));
            int filled = Math.max(0, Math.min(10, (int)Math.round(percent / 10.0)));
            String bar = "§a" + "▰".repeat(filled) + "§7" + "▱".repeat(10-filled);
            stands.get(0).setCustomName(building ? "§6⚒ Construyendo Cañón" : "§b⚒ Mejorando Cañón a nivel " + roman(turret.pendingLevel));
            stands.get(1).setCustomName("§e[" + bar + "§e] §f" + percent + "%");
        } else {
            stands.get(0).setCustomName("§6§lCañón §7— §eNivel " + roman(turret.level));
            stands.get(1).setCustomName("§cDurabilidad: §f" + turret.durability + "/300");
            if (damaged) {
                String status;
                if (turret.repairUntil > now) {
                    status = "§e§lREPARANDO §7— §f" + formatDuration(turret.repairUntil-now);
                } else {
                    status = "§c§lROTO";
                }
                stands.get(2).setCustomName(status);
                stands.get(3).setCustomName("§eMunición: §f" + turret.ammo + "/100");
            } else {
                stands.get(2).setCustomName("§eMunición: §f" + turret.ammo + "/100");
            }
        }
    }

    private void removeCannonHologram(String id) {
        List<ArmorStand> stands = cannonHolograms.remove(id);
        if (stands != null) for (ArmorStand stand : stands) if (stand.isValid()) stand.remove();
    }

    private long cannonCooldownMillis(int level){return level==1?1000L:level==2?3000L:5000L;}
    private boolean isGuard(Entity entity,Kingdom k){return entity.getScoreboardTags().contains("shadowkingdoms_guard_"+key(k.name));}

    @EventHandler(priority=EventPriority.HIGHEST,ignoreCancelled=true)
    public void onCannonProjectileDamage(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Snowball fireball)||!fireball.getScoreboardTags().contains("shadowkingdoms_cannon_projectile"))return;
        for(String tag:fireball.getScoreboardTags())if(tag.startsWith("shadowkingdoms_cannon_owner_")){
            Kingdom owner=kingdoms.get(tag.substring("shadowkingdoms_cannon_owner_".length()));if(owner==null)return;
            if(e.getEntity() instanceof Player player&&(isMember(player,owner)||isNationAlly(player,owner))){e.setCancelled(true);return;}
            if(e.getEntity() instanceof Monster monster&&isGuard(monster,owner)){e.setCancelled(true);return;}
            int level=fireball.getScoreboardTags().stream().filter(x->x.startsWith("shadowkingdoms_cannon_level_")).mapToInt(x->{try{return Integer.parseInt(x.substring("shadowkingdoms_cannon_level_".length()));}catch(Exception ex){return 1;}}).findFirst().orElse(1);
            e.setDamage(level==1?5.0:level==2?7.0:9.0);
            return;
        }
    }

    private void mailCommand(Player p, String[] args) {
        Kingdom own = kingdomOf(p.getUniqueId());
        if (own == null) { p.sendMessage("§cDebes pertenecer a un reino para usar ShadowMail."); return; }
        if (args.length > 1 && args[1].equalsIgnoreCase("adjuntar")) {
            if (!composingMailStage.containsKey(p.getUniqueId())) { p.sendMessage("§cPrimero inicia un correo desde /sk correo."); return; }
            ItemStack hand = p.getInventory().getItemInMainHand();
            if (hand == null || hand.getType().isAir()) { p.sendMessage("§cSostén el objeto que deseas adjuntar en tu mano principal."); return; }
            List<ItemStack> attachments = composingMailAttachments.computeIfAbsent(p.getUniqueId(), id -> new ArrayList<>());
            if (attachments.size() >= 9) { p.sendMessage("§cMáximo 9 pilas de objetos por correo."); return; }
            attachments.add(hand.clone()); p.getInventory().setItemInMainHand(null);
            p.sendMessage("§aAdjunto añadido. Total: " + attachments.size() + "/9. Puedes repetir /sk correo adjuntar o escribir el asunto en el chat.");
            return;
        }
        p.sendMessage("§ePara acceder a ShadowMail, haz clic derecho sobre el Nexo de tu reino.");
    }

    private void openMailMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, MAIL_MENU_TITLE);
        inv.setItem(10, named(Material.WRITABLE_BOOK, "§aBandeja de entrada", "§7Ver correos recibidos"));
        inv.setItem(13, named(Material.BOOK, "§eCorreos enviados", "§7Consultar mensajes enviados"));
        inv.setItem(16, named(Material.FEATHER, "§bRedactar correo", "§7Enviar mensaje a otro reino", "§7Puedes adjuntar objetos reales"));
        p.openInventory(inv);
    }

    private void openMailList(Player p, boolean sent) { openMailList(p, sent, 0); }

    private void openMailList(Player p, boolean sent, int page) {
        Kingdom own = kingdomOf(p.getUniqueId());
        if (own == null) return;
        page = Math.max(0, Math.min(1, page));
        String baseTitle = sent ? MAIL_SENT_TITLE : MAIL_INBOX_TITLE;
        String title = baseTitle + " §7(" + (page + 1) + "/2)";
        Inventory inv = Bukkit.createInventory(null, 54, title);
        mailMenuIds.keySet().removeIf(k -> k.startsWith(p.getUniqueId() + ":" + baseTitle));
        List<MailMessage> matching = new ArrayList<>();
        for (MailMessage m : mails) {
            boolean match = sent ? key(m.senderKingdom).equals(key(own.name)) : key(m.recipientKingdom).equals(key(own.name));
            if (match) matching.add(m);
        }
        Collections.reverse(matching);
        int start = page * 53;
        for (int i = start; i < Math.min(matching.size(), start + 53); i++) {
            MailMessage m = matching.get(i); int slot = i - start;
            long remaining = m.attachments.stream().filter(a -> a != null && !a.getType().isAir()).count();
            ItemStack icon = named(remaining > 0 ? Material.CHEST : Material.PAPER,
                (m.read ? "§7" : "§a") + m.subject,
                "§7De: §f" + m.senderKingdom, "§7Para: §f" + m.recipientKingdom,
                "§7Fecha: §f" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date(m.createdAt)),
                "§7Adjuntos restantes: §f" + remaining, "§eClic para abrir");
            inv.setItem(slot, icon); mailMenuIds.put(p.getUniqueId() + ":" + title + ":" + slot, Long.toString(m.id));
        }
        inv.setItem(53, named(Material.ARROW, page == 0 ? "§eSiguiente página »" : "§e« Página anterior", "§7Página " + (page + 1) + " de 2"));
        p.openInventory(inv);
    }

    private void openMailDetails(Player p, MailMessage m) {
        Kingdom own = kingdomOf(p.getUniqueId());
        if (own == null || (!key(m.recipientKingdom).equals(key(own.name)) && !key(m.senderKingdom).equals(key(own.name)))) return;
        if (key(m.recipientKingdom).equals(key(own.name))) m.read = true;
        saveData();
        Inventory inv = Bukkit.createInventory(null, 54, MAIL_DETAILS_PREFIX + m.id);
        inv.setItem(4, named(Material.PAPER, "§6" + m.subject, "§7De: §f" + m.senderKingdom, "§7Para: §f" + m.recipientKingdom));
        if (key(m.recipientKingdom).equals(key(own.name))) {
            inv.setItem(18, named(Material.WRITABLE_BOOK, "§eLeer mensaje", "§7De: §f" + m.senderKingdom,
                    "§7Asunto: §f" + m.subject, "§eClic para abrir el mensaje"));
        }
        for (int i=0; i<m.attachments.size() && i<9; i++) {
            ItemStack stack = m.attachments.get(i);
            if (stack == null || stack.getType().isAir()) continue;
            ItemStack display = stack.clone(); ItemMeta meta = display.getItemMeta();
            List<String> lore = meta != null && meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
            lore.add("§eClic para reclamar este adjunto"); if (meta != null) { meta.setLore(lore); display.setItemMeta(meta); }
            inv.setItem(19+i, display);
        }
        inv.setItem(49, named(Material.HOPPER, "§aRecoger todos los adjuntos", "§7Los objetos necesitan espacio en tu inventario"));
        inv.setItem(45, named(Material.ARROW, "§eVolver"));
        mailMenuIds.put(p.getUniqueId()+":detail", Long.toString(m.id));
        p.openInventory(inv);
    }

    private void handleMailMenuClick(Player p, String title, int slot) {
        if (title.equals(MAIL_MENU_TITLE)) {
            if (slot == 10) openMailList(p, false);
            else if (slot == 13) openMailList(p, true);
            else if (slot == 16) openRecipientMenu(p);
            return;
        }
        if (title.equals(MAIL_RECIPIENT_TITLE)) {
            if (slot == 49) { openMailMenu(p); return; }
            List<Kingdom> options = new ArrayList<>(); Kingdom currentOwn = kingdomOf(p.getUniqueId());
            for (Kingdom candidate : kingdoms.values()) if (currentOwn == null || !key(candidate.name).equals(key(currentOwn.name))) options.add(candidate);
            if (slot >= 0 && slot < options.size()) {
                Kingdom own = kingdomOf(p.getUniqueId()), target = options.get(slot);
                if (own == null || key(own.name).equals(key(target.name))) { p.sendMessage("§cDebes elegir otro reino."); return; }
                composingMailRecipient.put(p.getUniqueId(), target.name); composingMailStage.remove(p.getUniqueId()); composingMailAttachments.put(p.getUniqueId(), new ArrayList<>());
                openMailAttachmentMenu(p, target); return;
            }
            return;
        }
        if (title.equals(MAIL_ATTACH_TITLE)) {
            if (slot == 0 || slot == 8) {
                List<ItemStack> attached = new ArrayList<>();
                UUID playerId = p.getUniqueId();
                Inventory top = p.getOpenInventory().getTopInventory();
                if (slot == 8) {
                    for (int i=1;i<=7;i++) { ItemStack item=top.getItem(i); if(item!=null && !item.getType().isAir()) attached.add(item.clone()); }
                } else {
                    // NO descarta los adjuntos del correo, no los objetos del jugador: se devuelven.
                    for (int i=1;i<=7;i++) {
                        ItemStack item = top.getItem(i);
                        if (item == null || item.getType().isAir()) continue;
                        Map<Integer, ItemStack> leftover = p.getInventory().addItem(item.clone());
                        for (ItemStack stack : leftover.values()) p.getWorld().dropItemNaturally(p.getLocation(), stack);
                        top.setItem(i, null);
                    }
                }
                String recipient=composingMailRecipient.get(p.getUniqueId());
                if(recipient==null || !kingdoms.containsKey(key(recipient))) { p.sendMessage("§cEl reino destinatario ya no existe."); openRecipientMenu(p); return; }
                composingMailAttachments.put(playerId, attached);
                confirmedAttachmentMenu.add(playerId);
                bookMailRecipient.put(playerId, recipient);
                bookMailSubject.put(p.getUniqueId(), "Correo de " + Optional.ofNullable(kingdomOf(p.getUniqueId())).map(k -> k.name).orElse("reino"));
                p.closeInventory();
                ItemStack book=new ItemStack(Material.WRITABLE_BOOK); BookMeta bm=(BookMeta)book.getItemMeta();
                bm.setTitle("Correo a " + recipient); bm.setAuthor(p.getName()); bm.setPages("Escribe tu mensaje aquí y FIRMA el libro para enviarlo. Máximo 500 caracteres."); book.setItemMeta(bm);
                Map<Integer,ItemStack> left=p.getInventory().addItem(book);
                for(ItemStack overflow:left.values()) p.getWorld().dropItemNaturally(p.getLocation(), overflow);
                p.sendMessage(slot == 0 ? "§eSin adjuntos. Escribe tu mensaje en el libro y fírmalo para enviarlo." : "§eAdjuntos preparados. Escribe tu mensaje en el libro y fírmalo para enviarlo."); return;
            }
            return;
        }
        if (title.startsWith(MAIL_INBOX_TITLE) || title.startsWith(MAIL_SENT_TITLE)) {
            boolean sent = title.startsWith(MAIL_SENT_TITLE);
            int page = title.contains("(2/2)") ? 1 : 0;
            if (slot == 53) { openMailList(p, sent, page == 0 ? 1 : 0); return; }
            String id = mailMenuIds.get(p.getUniqueId() + ":" + title + ":" + slot);
            if (id != null) { MailMessage m = findMail(Long.parseLong(id)); if (m != null) openMailDetails(p,m); }
            return;
        }
        if (title.startsWith(MAIL_DETAILS_PREFIX)) {
            long id; try { id=Long.parseLong(title.substring(MAIL_DETAILS_PREFIX.length())); } catch (NumberFormatException ex) { return; }
            MailMessage m=findMail(id); if (m==null) return;
            if (slot==45) { Kingdom own = kingdomOf(p.getUniqueId()); if (own != null) openMailList(p, key(m.senderKingdom).equals(key(own.name))); return; }
            if (slot==18 && key(m.recipientKingdom).equals(key(Optional.ofNullable(kingdomOf(p.getUniqueId())).map(k -> k.name).orElse("")))) {
                openMailBook(p, m); return;
            }
            if (slot==49) { claimMailAttachments(p,m,-1); openMailDetails(p,m); return; }
            if (slot>=19 && slot<28) { claimMailAttachments(p,m,slot-19); openMailDetails(p,m); }
        }
    }


    private void openMailBook(Player player, MailMessage message) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) return;
        meta.setTitle(message.subject == null || message.subject.isBlank() ? "Correo de ShadowMail" : message.subject);
        meta.setAuthor(message.senderKingdom);
        String body = message.body == null ? "" : message.body;
        List<String> pages = new ArrayList<>();
        // Divide el contenido en páginas de tamaño moderado para que los mensajes largos sean legibles.
        final int pageLength = 240;
        if (body.isEmpty()) pages.add("(Mensaje vacío)");
        else for (int start = 0; start < body.length(); start += pageLength) pages.add(body.substring(start, Math.min(body.length(), start + pageLength)));
        meta.setPages(pages);
        book.setItemMeta(meta);
        player.openBook(book);
    }

    private void openRecipientMenu(Player p) {
        Inventory inv=Bukkit.createInventory(null,54,MAIL_RECIPIENT_TITLE); int slot=0; Kingdom own=kingdomOf(p.getUniqueId());
        for (Kingdom k:kingdoms.values()) { if (own!=null && key(k.name).equals(key(own.name))) continue; if(slot>=45) break; inv.setItem(slot++,named(Material.BEACON,"§6"+k.name,"§7Rey: §f"+k.kingName,"§eClic para redactar")); }
        inv.setItem(49,named(Material.ARROW,"§eVolver")); p.openInventory(inv);
    }

    private void openMailAttachmentMenu(Player p, Kingdom target) {
        Inventory inv=Bukkit.createInventory(null,9,MAIL_ATTACH_TITLE);
        inv.setItem(0,named(Material.RED_DYE,"§cNO","§7Enviar sin adjuntos / cancelar adjuntos"));
        inv.setItem(8,named(Material.LIME_DYE,"§aAdjuntar","§7Confirma los objetos y abre el libro"));
        p.openInventory(inv);
        p.sendMessage("§7Destinatario: §e"+target.name+"§7. Coloca hasta 7 pilas de objetos en las casillas centrales.");
    }

    @EventHandler public void onMailBookSign(PlayerEditBookEvent e) {
        if (!e.isSigning()) return;
        UUID id=e.getPlayer().getUniqueId();
        String recipient=bookMailRecipient.get(id);
        if(recipient==null) return;
        BookMeta signedMeta = e.getNewBookMeta();
        List<String> pages = new ArrayList<>(signedMeta.getPages());
        String body=String.join("\n", pages).trim();
        Player p=e.getPlayer();
        // El libro firmado es solo un editor temporal de ShadowMail: no debe quedarse en el inventario.
        removeSignedMailBookNextTick(p, signedMeta.getTitle(), signedMeta.getAuthor(), pages);
        if(body.isBlank() || body.length()>500) { p.sendMessage("§cEl mensaje debe tener entre 1 y 500 caracteres. Redacta otro correo desde el Nexo."); return; }
        Bukkit.getScheduler().runTask(this, () -> {
            Kingdom own=kingdomOf(id);
            if(own==null || !kingdoms.containsKey(key(recipient))) { p.sendMessage("§cNo se pudo validar el remitente o destinatario."); returnAttachments(p,id); clearComposition(id); bookMailRecipient.remove(id); bookMailSubject.remove(id); return; }
            long pending=mails.stream().filter(m -> key(m.recipientKingdom).equals(key(recipient))).count();
            if(pending>=108) { p.sendMessage("§cLa bandeja de entrada de ese reino está llena. No se envió el correo; los adjuntos se devolvieron."); returnAttachments(p,id); clearComposition(id); bookMailRecipient.remove(id); bookMailSubject.remove(id); return; }
            MailMessage m=new MailMessage(nextMailId++,own.name,recipient,bookMailSubject.getOrDefault(id,"Correo de "+own.name),body,System.currentTimeMillis());
            for(ItemStack item:composingMailAttachments.getOrDefault(id,Collections.emptyList())) if(item!=null && !item.getType().isAir()) m.attachments.add(item.clone());
            mails.add(m); saveData(); composingMailRecipient.remove(id); bookMailRecipient.remove(id); bookMailSubject.remove(id); composingMailAttachments.remove(id);
            p.sendMessage("§aCorreo enviado a §e"+recipient+"§a con "+m.attachments.size()+" adjuntos.");
        });
    }

    private void removeSignedMailBookNextTick(Player player, String title, String author, List<String> pages) {
        Bukkit.getScheduler().runTask(this, () -> {
            if (!player.isOnline()) return;
            for (int slot = 0; slot < player.getInventory().getSize(); slot++) {
                ItemStack item = player.getInventory().getItem(slot);
                if (item == null || item.getType() != Material.WRITTEN_BOOK || !(item.getItemMeta() instanceof BookMeta meta)) continue;
                if (!Objects.equals(title, meta.getTitle()) || !Objects.equals(author, meta.getAuthor())) continue;
                if (!meta.getPages().equals(pages)) continue;
                // Solo elimina una coincidencia: el ejemplar firmado para enviar este mensaje.
                if (item.getAmount() > 1) item.setAmount(item.getAmount() - 1);
                else player.getInventory().setItem(slot, null);
                return;
            }
        });
    }

    private void claimMailAttachments(Player p, MailMessage m, int index) {
        Kingdom own=kingdomOf(p.getUniqueId()); if(own==null || !key(m.recipientKingdom).equals(key(own.name))) { p.sendMessage("§cSolo el reino destinatario puede reclamar adjuntos."); return; }
        List<Integer> targets=new ArrayList<>(); if(index<0) { for(int i=0;i<m.attachments.size();i++) if(m.attachments.get(i)!=null) targets.add(i); } else if(index<m.attachments.size()) targets.add(index);
        for(int i:targets) {
            ItemStack item=m.attachments.get(i); if(item==null || item.getType().isAir()) continue;
            if(!canFit(p, item)) { p.sendMessage("§cTu inventario no tiene espacio suficiente. Los adjuntos restantes se conservaron."); break; }
            p.getInventory().addItem(item.clone());
            m.attachments.set(i,null); saveData();
        }
        p.updateInventory();
    }


    @EventHandler public void onMailInventoryClose(org.bukkit.event.inventory.InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player p) || !MAIL_ATTACH_TITLE.equals(e.getView().getTitle())) return;
        UUID id = p.getUniqueId();
        if (confirmedAttachmentMenu.remove(id)) return;
        Inventory inv = e.getInventory();
        for (int slot = 1; slot <= 7; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null || item.getType().isAir()) continue;
            Map<Integer, ItemStack> leftover = p.getInventory().addItem(item.clone());
            for (ItemStack stack : leftover.values()) p.getWorld().dropItemNaturally(p.getLocation(), stack);
            inv.setItem(slot, null);
        }
        composingMailAttachments.remove(id); composingMailRecipient.remove(id);
        p.sendMessage("§eRedacción cancelada; los objetos se devolvieron a tu inventario o al suelo si estaba lleno.");
    }

    @EventHandler public void onMailDisconnect(org.bukkit.event.player.PlayerQuitEvent e) {
        UUID id = e.getPlayer().getUniqueId();
        // Si el jugador se desconecta durante la redacción, devuelve los objetos reservados antes de limpiar el estado.
        if (composingMailAttachments.containsKey(id) && !bookMailRecipient.containsKey(id)) returnAttachments(e.getPlayer(), id);
        clearComposition(id); bookMailRecipient.remove(id); bookMailSubject.remove(id);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onKingdomChatFormat(AsyncChatEvent e) {
        // Paper moderno: usa el renderer Adventure para que todos los destinatarios
        // reciban el prefijo, incluso si otros plugins no respetan el formato antiguo.
        Kingdom kingdom = kingdomOf(e.getPlayer().getUniqueId());
        if (kingdom == null) return;

        String prefix = "§8[§r" + kingdom.name + "§8] §r" + e.getPlayer().getName() + "§7: §f";
        Component prefixComponent = LegacyComponentSerializer.legacySection().deserialize(prefix);
        e.renderer((source, sourceDisplayName, message, viewer) -> prefixComponent.append(message));
    }

    @EventHandler public void onMailChat(AsyncPlayerChatEvent e) {
        UUID id=e.getPlayer().getUniqueId(); String stage=composingMailStage.get(id); if(stage==null) return;
        e.setCancelled(true); String input=e.getMessage().trim();
        Bukkit.getScheduler().runTask(this, () -> {
            Player p=Bukkit.getPlayer(id); if(p==null) return;
            if(input.equalsIgnoreCase("cancelar")) { p.sendMessage("§cRedacción cancelada. Los adjuntos reservados se devolvieron a tu inventario."); returnAttachments(p,id); clearComposition(id); return; }
            if(stage.equals("SUBJECT")) {
                if(input.isBlank() || input.length()>60) { p.sendMessage("§cEl asunto debe tener entre 1 y 60 caracteres. Inténtalo de nuevo o escribe cancelar."); return; }
                composingMailSubject.put(id,input); composingMailStage.put(id,"BODY"); p.sendMessage("§aAsunto guardado. Escribe ahora el mensaje (máximo 500 caracteres). Escribe cancelar para abortar.");
            } else {
                if(input.isBlank() || input.length()>500) { p.sendMessage("§cEl mensaje debe tener entre 1 y 500 caracteres."); return; }
                Kingdom own=kingdomOf(id); String recipient=composingMailRecipient.get(id); if(own==null || recipient==null || !kingdoms.containsKey(key(recipient))) { p.sendMessage("§cNo se pudo validar el remitente o destinatario."); returnAttachments(p,id); clearComposition(id); return; }
                MailMessage m=new MailMessage(nextMailId++,own.name,recipient,composingMailSubject.getOrDefault(id,"Sin asunto"),input,System.currentTimeMillis());
                List<ItemStack> attached=composingMailAttachments.getOrDefault(id,new ArrayList<>()); for(ItemStack item:attached) m.attachments.add(item.clone()); mails.add(m); saveData(); clearComposition(id);
                p.sendMessage("§aCorreo enviado al reino §e"+recipient+"§a con "+m.attachments.size()+" adjuntos.");
            }
        });
    }

    private boolean canFit(Player p, ItemStack item) { Inventory temp=Bukkit.createInventory(null,36); ItemStack[] contents=p.getInventory().getStorageContents(); for(int i=0;i<Math.min(36,contents.length);i++) if(contents[i]!=null) temp.setItem(i,contents[i].clone()); return temp.addItem(item.clone()).isEmpty(); }
    private void returnAttachments(Player p, UUID id) { List<ItemStack> list=composingMailAttachments.get(id); if(list!=null) for(ItemStack item:list) if(item!=null) { Map<Integer,ItemStack> left=p.getInventory().addItem(item.clone()); for(ItemStack overflow:left.values()) p.getWorld().dropItemNaturally(p.getLocation(),overflow); } }
    private void clearComposition(UUID id) { composingMailStage.remove(id); composingMailRecipient.remove(id); composingMailSubject.remove(id); composingMailAttachments.remove(id); }
    private MailMessage findMail(long id) { for(MailMessage m:mails) if(m.id==id) return m; return null; }
    private ItemStack named(Material material, String name, String... lore) { ItemStack item=new ItemStack(material); ItemMeta meta=item.getItemMeta(); meta.setDisplayName(name); if(lore.length>0) meta.setLore(Arrays.asList(lore)); item.setItemMeta(meta); return item; }

    private String rankName(String rank) { return switch (rank) { case "REY" -> "Rey"; case "CABALLERO" -> "Caballero"; case "PEON" -> "Peón"; default -> rank; }; }
    private String formatDuration(long millis) {
        long seconds = Math.max(0, millis / 1000); long days=seconds/86400; seconds%=86400; long hours=seconds/3600; seconds%=3600; long minutes=seconds/60;
        if (days > 0) return days + "d " + hours + "h";
        if (hours > 0) return hours + "h " + minutes + "m";
        if (minutes > 0) return minutes + "m";
        return seconds + "s";
    }

    @EventHandler public void onBreak(BlockBreakEvent e) {
        Block block = e.getBlock();
        Kingdom territory = kingdomAt(block.getChunk());
        if (protectedFrom(e.getPlayer(), block.getChunk(), false)) { e.setCancelled(true); return; }
        if (territory != null && territory.nexusPlaced
                && territory.nexusWorld.equals(block.getWorld().getName())
                && territory.nexusX == block.getX() && territory.nexusY == block.getY()
                && territory.nexusZ == block.getZ() && block.getType() == Material.BEACON) {
            // El Nexo es una estructura del reino: al destruirlo no debe soltar un beacon.
            e.setDropItems(false);
            removeNexusHologram(territory);
            territory.nexusPlaced = false;
            saveData();
            e.getPlayer().sendMessage("§eEl Nexo de §6" + territory.name + " §eha sido destruido. El reino sigue existiendo; el Rey puede usar §f/sk colocar-nexo§e dentro de su territorio para reponerlo.");
        }
    }
    @EventHandler public void onPlace(BlockPlaceEvent e) { if (protectedFrom(e.getPlayer(), e.getBlock().getChunk(), false)) e.setCancelled(true); }
    @EventHandler public void onInteract(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        Block b = e.getClickedBlock(); Kingdom k = kingdomAt(b.getChunk()); if (k == null) return;
        if (isMember(e.getPlayer(), k)) return;
        if (isNationAlly(e.getPlayer(), k) && isContainer(b.getType())) return;
        if (isInteractive(b.getType())) e.setCancelled(true);
    }
    @EventHandler public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player p)) return;
        InventoryHolder holder = e.getInventory().getHolder(); if (!(holder instanceof BlockState state)) return;
        Kingdom k = kingdomAt(state.getBlock().getChunk()); if (k == null || isMember(p,k)) return;
        if (isNationAlly(p,k) && isContainer(state.getType())) return;
        e.setCancelled(true);
    }
    @EventHandler public void onCropTrample(org.bukkit.event.entity.EntityInteractEvent e) { Kingdom k=kingdomAt(e.getBlock().getChunk()); if(k==null || extraLevel(k,"general_protection")<3)return; if(e.getEntity() instanceof Player p && isMember(p,k))return; if(e.getBlock().getType()==Material.FARMLAND)e.setCancelled(true); }
    @EventHandler public void onEntityChange(EntityChangeBlockEvent e) { Kingdom k=kingdomAt(e.getBlock().getChunk()); if(k!=null && extraLevel(k,"general_protection")>=2) e.setCancelled(true); }
    @EventHandler public void onEntityExplode(EntityExplodeEvent e) { e.blockList().removeIf(b -> { Kingdom k=kingdomAt(b.getChunk()); return k!=null && extraLevel(k,"general_protection")>=1; }); }
    @EventHandler public void onBlockExplode(BlockExplodeEvent e) { e.blockList().removeIf(b -> { Kingdom k=kingdomAt(b.getChunk()); return k!=null && extraLevel(k,"general_protection")>=1; }); }
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
    public void onExtraTerritoryDamage(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player attacker)) return;
        Kingdom own=kingdomOf(attacker.getUniqueId()); if(own==null) return;
        Kingdom territory=kingdomAt(e.getEntity().getLocation().getChunk());
        if(territory!=own || (e.getEntity() instanceof Player victim && isMember(victim,own))) return;
        e.setDamage(e.getDamage()+extraLevel(own,"killer_edge")*0.2);
    }
    @EventHandler(priority=EventPriority.HIGH,ignoreCancelled=true)
    public void onNaturalRegen(EntityRegainHealthEvent e) {
        if(!(e.getEntity() instanceof Player p) || e.getRegainReason()!=EntityRegainHealthEvent.RegainReason.SATIATED) return;
        Kingdom k=kingdomOf(p.getUniqueId()); if(k==null || kingdomAt(p.getLocation().getChunk())!=k)return;
        e.setAmount(e.getAmount()+extraLevel(k,"natural_regen")*0.2);
    }

    @EventHandler public void onHangingBreak(HangingBreakByEntityEvent e) {
        if (e.getRemover() instanceof Player p) { if (protectedFrom(p, e.getEntity().getLocation().getChunk(), false)) e.setCancelled(true); }
        else if (kingdomAt(e.getEntity().getLocation().getChunk()) != null) e.setCancelled(true);
    }

    private boolean protectedFrom(Player p, Chunk chunk, boolean container) {
        Kingdom k = kingdomAt(chunk); if (k == null || isMember(p,k)) return false;
        return !(container && isNationAlly(p,k));
    }
    private boolean isMember(Player p, Kingdom k) { return k.members.containsKey(p.getUniqueId().toString()); }
    private boolean isKing(Player p, Kingdom k) { Member m = k.members.get(p.getUniqueId().toString()); return m != null && m.rank.equals("REY"); }
    private boolean isNationAlly(Player p, Kingdom target) {
        Kingdom own = kingdomOf(p.getUniqueId()); if (own == null || target.nation == null || own.nation == null) return false;
        return key(own.nation).equals(key(target.nation)) && !key(own.name).equals(key(target.name));
    }
    private boolean isInteractive(Material m) { String n=m.name(); return n.contains("CHEST") || n.contains("BARREL") || n.contains("DOOR") || n.contains("TRAPDOOR") || n.contains("FENCE_GATE") || n.contains("BUTTON") || n.contains("LEVER") || n.contains("FURNACE") || n.contains("ANVIL") || n.contains("HOPPER") || n.contains("DISPENSER") || n.contains("DROPPER") || n.contains("SHULKER_BOX") || n.contains("BREWING_STAND") || n.contains("BEACON") || n.contains("LECTERN") || n.contains("SMITHING_TABLE") || n.contains("CRAFTING_TABLE") || n.contains("ENCHANTING_TABLE") || n.contains("CAMPFIRE") || n.contains("JUKEBOX"); }
    private boolean isContainer(Material m) { String n=m.name(); return n.contains("CHEST") || n.contains("BARREL") || n.contains("SHULKER_BOX") || n.contains("FURNACE") || n.contains("HOPPER") || n.contains("DISPENSER") || n.contains("DROPPER") || n.contains("BREWING_STAND"); }
    private Kingdom kingdomAt(Chunk c) { String ck=chunkKey(c); for (Kingdom k:kingdoms.values()) if (k.claims.contains(ck)) return k; return null; }
    private Kingdom kingdomOf(UUID id) { String s=id.toString(); for (Kingdom k:kingdoms.values()) if (k.members.containsKey(s)) return k; return null; }
    private String chunkKey(Chunk c) { return c.getWorld().getName()+":"+c.getX()+":"+c.getZ(); }
    private String cleanName(String n) { return n.replaceAll("[^a-zA-Z0-9_&-]", ""); }
    private String key(String s) { return ChatColor.stripColor(ChatColor.translateAlternateColorCodes('&', s)).toLowerCase(Locale.ROOT); }

    private void loadData() {
        data = YamlConfiguration.loadConfiguration(dataFile);
        ConfigurationSection ks=data.getConfigurationSection("kingdoms");
        if (ks != null) for (String id:ks.getKeys(false)) {
            String path="kingdoms."+id; Kingdom k=new Kingdom(data.getString(path+".name",id), UUID.fromString(data.getString(path+".king-uuid")), data.getString(path+".king-name","Unknown"));
            k.nexusWorld=data.getString(path+".nexus.world", "world"); k.nexusX=data.getInt(path+".nexus.x"); k.nexusY=data.getInt(path+".nexus.y"); k.nexusZ=data.getInt(path+".nexus.z"); k.nexusPlaced=data.getBoolean(path+".nexus.placed", true); k.nation=data.getString(path+".nation", "");
            k.createdAt=data.getLong(path+".created-at", System.currentTimeMillis()); k.nexusBuildingUntil=data.getLong(path+".nexus-building-until",0L); k.pendingLevel=data.getInt(path+".pending-level",0); ConfigurationSection extras=data.getConfigurationSection(path+".extra-upgrades"); if(extras!=null) for(String ek:extras.getKeys(false)) k.extraLevels.put(ek,Math.max(0,Math.min(10,extras.getInt(ek)))); k.level=Math.max(1,Math.min(3,data.getInt(path+".level",1))); k.moonstones=roundPL(data.getDouble(path+".pl",0.0)); k.claims.addAll(data.getStringList(path+".claims")); List<?> chestItems=data.getList(path+".nexus-chest", new ArrayList<>()); for(int i=0;i<Math.min(9,chestItems.size());i++){ Object item=chestItems.get(i); k.nexusChest[i]=item instanceof ItemStack stack ? stack.clone() : null; } ConfigurationSection ms=data.getConfigurationSection(path+".members"); if(ms!=null) for(String uuid:ms.getKeys(false)) k.members.put(uuid,new Member(data.getString(path+".members."+uuid+".name","Unknown"),data.getString(path+".members."+uuid+".rank","CABALLERO")));
            kingdoms.put(key(k.name),k);
        }
        ConfigurationSection ts=data.getConfigurationSection("turrets"); if(ts!=null)for(String id:ts.getKeys(false)){String q="turrets."+id;CannonTurret t=new CannonTurret(id,data.getString(q+".kingdom",""),data.getString(q+".world","world"),data.getInt(q+".x"),data.getInt(q+".y"),data.getInt(q+".z"),Math.max(1,Math.min(3,data.getInt(q+".level",1))),data.getLong(q+".build-until",0L));t.nextShotAt=data.getLong(q+".next-shot",0L);t.pendingLevel=data.getInt(q+".pending-level",0);t.upgradeUntil=data.getLong(q+".upgrade-until",0L);t.ammo=Math.max(0,Math.min(100,data.getInt(q+".ammo",100)));t.durability=Math.max(0,Math.min(300,data.getInt(q+".durability",300)));t.repairUntil=data.getLong(q+".repair-until",0L);cannons.put(id,t);}
        ConfigurationSection is=data.getConfigurationSection("infernos");if(is!=null)for(String id:is.getKeys(false)){String q="infernos."+id;InfernoTurret t=new InfernoTurret(id,data.getString(q+".kingdom",""),data.getString(q+".world","world"),data.getInt(q+".x"),data.getInt(q+".y"),data.getInt(q+".z"),Math.max(1,Math.min(3,data.getInt(q+".level",1))),data.getLong(q+".build-until",0));t.ammo=Math.max(0,Math.min(300,data.getInt(q+".ammo",300)));t.durability=Math.max(0,Math.min(infernoMaxDurability(t.level),data.getInt(q+".durability",infernoMaxDurability(t.level))));t.repairUntil=data.getLong(q+".repair-until",0);t.upgradeUntil=data.getLong(q+".upgrade-until",0);t.upgradeStarted=data.getLong(q+".upgrade-started",t.upgradeUntil>0?t.upgradeUntil-30000L:0L);t.pendingLevel=data.getInt(q+".pending-level",0);infernos.put(id,t);}
        ConfigurationSection ns=data.getConfigurationSection("nations"); if(ns!=null) for(String id:ns.getKeys(false)){ Nation n=new Nation(data.getString("nations."+id+".name",id),data.getString("nations."+id+".founder","")); n.kingdoms.addAll(data.getStringList("nations."+id+".kingdoms")); nations.put(key(n.name),n); }
        ConfigurationSection ms=data.getConfigurationSection("mail"); if(ms!=null) for(String id:ms.getKeys(false)) { String path="mail."+id; MailMessage m=new MailMessage(Long.parseLong(id),data.getString(path+".sender",""),data.getString(path+".recipient",""),data.getString(path+".subject","Sin asunto"),data.getString(path+".body",""),data.getLong(path+".created-at",System.currentTimeMillis())); m.read=data.getBoolean(path+".read",false); List<?> items=data.getList(path+".attachments",new ArrayList<>()); for(Object obj:items) m.attachments.add(obj instanceof ItemStack item ? item.clone() : null); mails.add(m); nextMailId=Math.max(nextMailId,m.id+1); }
    }
    private void saveData() {
        if(data==null) data=new YamlConfiguration(); data.set("kingdoms",null); data.set("nations",null); data.set("mail",null); data.set("turrets",null); data.set("infernos",null);
        for (Map.Entry<String, Inventory> entry : activeNexusChestInventories.entrySet()) {
            Kingdom kingdom = kingdoms.get(entry.getKey());
            if (kingdom == null) continue;
            ItemStack[] contents = entry.getValue().getContents();
            for (int i = 0; i < 9; i++) {
                ItemStack item = i < contents.length ? contents[i] : null;
                kingdom.nexusChest[i] = item == null || item.getType().isAir() ? null : item.clone();
            }
        }
        for(Kingdom k:kingdoms.values()) { String p="kingdoms."+key(k.name); data.set(p+".name",k.name); data.set(p+".king-uuid",k.kingId.toString()); data.set(p+".king-name",k.kingName); data.set(p+".created-at",k.createdAt); data.set(p+".nexus-building-until",k.nexusBuildingUntil); data.set(p+".pending-level",k.pendingLevel); data.set(p+".extra-upgrades",k.extraLevels); data.set(p+".level",k.level); data.set(p+".pl",roundPL(k.moonstones)); data.set(p+".nexus.world",k.nexusWorld); data.set(p+".nexus.x",k.nexusX); data.set(p+".nexus.y",k.nexusY); data.set(p+".nexus.z",k.nexusZ); data.set(p+".nexus.placed",k.nexusPlaced); data.set(p+".nation",k.nation); data.set(p+".claims",new ArrayList<>(k.claims)); data.set(p+".nexus-chest", Arrays.asList(k.nexusChest)); for(Map.Entry<String,Member> e:k.members.entrySet()){ data.set(p+".members."+e.getKey()+".name",e.getValue().name); data.set(p+".members."+e.getKey()+".rank",e.getValue().rank); } }
        for(CannonTurret t:cannons.values()){String q="turrets."+t.id;data.set(q+".kingdom",t.kingdomKey);data.set(q+".world",t.world);data.set(q+".x",t.x);data.set(q+".y",t.y);data.set(q+".z",t.z);data.set(q+".level",t.level);data.set(q+".build-until",t.buildUntil);data.set(q+".next-shot",t.nextShotAt);data.set(q+".pending-level",t.pendingLevel);data.set(q+".upgrade-until",t.upgradeUntil);data.set(q+".ammo",t.ammo);data.set(q+".durability",t.durability);data.set(q+".repair-until",t.repairUntil);}
        for(InfernoTurret t:infernos.values()){String q="infernos."+t.id;data.set(q+".kingdom",t.kingdomKey);data.set(q+".world",t.world);data.set(q+".x",t.x);data.set(q+".y",t.y);data.set(q+".z",t.z);data.set(q+".level",t.level);data.set(q+".build-until",t.buildUntil);data.set(q+".ammo",t.ammo);data.set(q+".durability",t.durability);data.set(q+".repair-until",t.repairUntil);data.set(q+".upgrade-until",t.upgradeUntil);data.set(q+".upgrade-started",t.upgradeStarted);data.set(q+".pending-level",t.pendingLevel);}
        for(Nation n:nations.values()){ String p="nations."+key(n.name); data.set(p+".name",n.name); data.set(p+".founder",n.founderKingdom); data.set(p+".kingdoms",new ArrayList<>(n.kingdoms)); }
        for(MailMessage m:mails) { String p="mail."+m.id; data.set(p+".sender",m.senderKingdom); data.set(p+".recipient",m.recipientKingdom); data.set(p+".subject",m.subject); data.set(p+".body",m.body); data.set(p+".created-at",m.createdAt); data.set(p+".read",m.read); data.set(p+".attachments",m.attachments); }
        try { data.save(dataFile); } catch(IOException ex){ getLogger().severe("No se pudieron guardar los datos: "+ex.getMessage()); }
    }

    private static final class InfernoTurret {final String id,kingdomKey,world;final int x,y,z;int level,ammo=300,durability=250,pendingLevel;long buildUntil,repairUntil,upgradeUntil,upgradeStarted,nextDamageAt,lockStarted;UUID target;InfernoTurret(String id,String kingdomKey,String world,int x,int y,int z,int level,long buildUntil){this.id=id;this.kingdomKey=kingdomKey;this.world=world;this.x=x;this.y=y;this.z=z;this.level=level;this.buildUntil=buildUntil;this.durability=level==1?250:level==2?500:1000;}}
    private static final class CannonTurret {final String id,kingdomKey,world;final int x,y,z;int level,ammo=100,durability=300,pendingLevel=0;long buildUntil,nextShotAt,upgradeUntil,aimReadyAt,repairUntil;CannonTurret(String id,String kingdomKey,String world,int x,int y,int z,int level,long buildUntil){this.id=id;this.kingdomKey=kingdomKey;this.world=world;this.x=x;this.y=y;this.z=z;this.level=level;this.buildUntil=buildUntil;this.nextShotAt=buildUntil;}}
    private static final class GuardSession { final String kingdomKey; int nextAmount=2,aliveCount=0,resetTask=-1; long nextWaveAt=0L,outsideSince=0L; GuardSession(String key){kingdomKey=key;} }
    private String findMemberId(Kingdom k,String name){for(Map.Entry<String,Member> e:k.members.entrySet())if(e.getValue().name.equalsIgnoreCase(name))return e.getKey();return null;}
    private static Map<Material,Double> mineralValues(){Map<Material,Double> m=new EnumMap<>(Material.class);m.put(Material.COAL,0.10);m.put(Material.CHARCOAL,0.10);m.put(Material.COPPER_INGOT,0.15);m.put(Material.RAW_COPPER,0.12);m.put(Material.IRON_INGOT,0.50);m.put(Material.RAW_IRON,0.40);m.put(Material.GOLD_INGOT,0.75);m.put(Material.RAW_GOLD,0.60);m.put(Material.REDSTONE,0.20);m.put(Material.LAPIS_LAZULI,0.25);m.put(Material.AMETHYST_SHARD,0.30);m.put(Material.DIAMOND,3.00);m.put(Material.EMERALD,2.50);m.put(Material.QUARTZ,0.20);m.put(Material.ANCIENT_DEBRIS,4.00);m.put(Material.NETHERITE_SCRAP,1.50);m.put(Material.NETHERITE_INGOT,8.00);return Collections.unmodifiableMap(m);}
    private void openMineralMenu(Player p) {
        Inventory inv = Bukkit.createInventory(null, 54, MINERALS_MENU_TITLE);
        inv.setItem(45, named(Material.ARROW, "§e§lVolver al Nexo", "§7Regresar al menú principal"));
        inv.setItem(53, named(Material.GREEN_DYE, "§a§lConvertir minerales", "§7Deposita minerales en esta interfaz", "§7y pulsa para convertirlos en PL", "§eLos minerales depositados serán consumidos"));
        p.openInventory(inv);
    }

    private String prettyMaterial(Material m) {
        String s=m.name().toLowerCase(Locale.ROOT).replace('_',' ');
        return s.substring(0,1).toUpperCase(Locale.ROOT)+s.substring(1);
    }

    private void moveMineralsIntoTrade(Player player, ItemStack source) {
        Material type = source.getType();
        Inventory top = player.getOpenInventory().getTopInventory();
        int remaining = source.getAmount();
        for (int slot = 0; slot < top.getSize() && remaining > 0; slot++) {
            if (slot == 45 || slot == 53) continue;
            ItemStack current = top.getItem(slot);
            if (current != null && current.getType() == type && current.getAmount() < current.getMaxStackSize()) {
                int add = Math.min(remaining, current.getMaxStackSize() - current.getAmount());
                current.setAmount(current.getAmount() + add);
                remaining -= add;
            }
        }
        for (int slot = 0; slot < top.getSize() && remaining > 0; slot++) {
            if (slot == 45 || slot == 53) continue;
            ItemStack current = top.getItem(slot);
            if (current == null || current.getType().isAir()) {
                int add = Math.min(remaining, type.getMaxStackSize());
                top.setItem(slot, new ItemStack(type, add));
                remaining -= add;
            }
        }
        if (remaining == 0) source.setAmount(0);
        else source.setAmount(remaining);
        player.updateInventory();
    }

    private void handleMineralTrade(Player p,int slot) {
        Kingdom k = kingdomOf(p.getUniqueId());
        if (k == null) { p.closeInventory(); return; }
        if (slot == 45) { openNexusMenu(p, k); return; }
        if (slot != 53) return;
        Inventory top = p.getOpenInventory().getTopInventory();
        double earned = 0.0;
        int totalItems = 0;
        Map<Material,Integer> totals = new EnumMap<>(Material.class);
        for (int i = 0; i < top.getSize(); i++) {
            if (i == 45 || i == 53) continue;
            ItemStack item = top.getItem(i);
            if (item == null || item.getType().isAir()) continue;
            Double value = MINERAL_VALUES.get(item.getType());
            if (value == null) continue;
            totals.merge(item.getType(), item.getAmount(), Integer::sum);
            totalItems += item.getAmount();
            earned += item.getAmount() * value;
            top.setItem(i, null);
        }
        if (totalItems == 0) { p.sendMessage("§cPrimero deposita minerales en los espacios vacíos."); return; }
        earned = roundPL(earned);
        k.moonstones = roundPL(k.moonstones + earned);
        saveData();
        p.sendMessage("§aConvertiste §e" + totalItems + " minerales §apor §d" + formatPL(earned) + " PL§a para el reino §e" + k.name + "§a.");
        p.sendMessage("§7Saldo del reino: §d" + formatPL(k.moonstones) + " PL");
        openMineralMenu(p);
    }

    private void startNexusConstruction(Kingdom k,long duration) {
        String kk = key(k.name);
        Integer old = nexusBuildTasks.remove(kk);
        if (old != null) Bukkit.getScheduler().cancelTask(old);
        World world = Bukkit.getWorld(k.nexusWorld);
        if (world != null) {
            Location soundLocation = new Location(world, k.nexusX + 0.5, k.nexusY + 0.5, k.nexusZ + 0.5);
            world.playSound(soundLocation, Sound.BLOCK_ANVIL_PLACE, 1.0f, 0.9f);
        }
        final List<ArmorStand> holograms = new ArrayList<>();
        if (world != null) {
            Location base = new Location(world, k.nexusX + 0.5, k.nexusY + 2.15, k.nexusZ + 0.5);
            ArmorStand title = world.spawn(base.clone().add(0, 0.28, 0), ArmorStand.class, stand -> {
                stand.setVisible(false); stand.setMarker(true); stand.setGravity(false); stand.setInvulnerable(true); stand.setPersistent(false);
                stand.setCustomName("§6⚒ Construyendo Nexo"); stand.setCustomNameVisible(true); stand.addScoreboardTag("shadowkingdoms_builder_" + kk);
            });
            ArmorStand progress = world.spawn(base, ArmorStand.class, stand -> {
                stand.setVisible(false); stand.setMarker(true); stand.setGravity(false); stand.setInvulnerable(true); stand.setPersistent(false);
                stand.setCustomName("§e[§7░░░░░░░░░░§e] §f0%"); stand.setCustomNameVisible(true); stand.addScoreboardTag("shadowkingdoms_builder_" + kk);
            });
            holograms.add(title); holograms.add(progress);
            long startedAt = System.currentTimeMillis();
            int[] progressTask = new int[1];
            progressTask[0] = Bukkit.getScheduler().runTaskTimer(this, () -> {
                if (!progress.isValid() || !title.isValid()) { Bukkit.getScheduler().cancelTask(progressTask[0]); return; }
                double ratio = Math.min(1.0, (System.currentTimeMillis() - startedAt) / (double) Math.max(1L, duration));
                int percent = (int)Math.floor(ratio * 100.0);
                int filled = (int)Math.round(ratio * 10.0);
                String bar = "§a" + "▰".repeat(filled) + "§7" + "▱".repeat(10 - filled);
                progress.setCustomName("§e[" + bar + "§e] §f" + percent + "%");
            }, 0L, 5L).getTaskId();
            Bukkit.getScheduler().runTaskLater(this, () -> {
                Bukkit.getScheduler().cancelTask(progressTask[0]);
                for (ArmorStand stand : holograms) if (stand.isValid()) stand.remove();
            }, Math.max(1L, duration / 50L));
        }
        int task = Bukkit.getScheduler().runTaskLater(this, () -> {
            k.nexusBuildingUntil = 0L;
            if (k.pendingLevel > 0) { k.level = k.pendingLevel; k.pendingLevel = 0; }
            updateNexusHologram(k); saveData(); nexusBuildTasks.remove(kk);
            World completedWorld = Bukkit.getWorld(k.nexusWorld);
            if (completedWorld != null) {
                Location soundLocation = new Location(completedWorld, k.nexusX + 0.5, k.nexusY + 0.5, k.nexusZ + 0.5);
                completedWorld.playSound(soundLocation, Sound.BLOCK_ANVIL_LAND, 1.0f, 1.15f);
            }
            for (Player online : Bukkit.getOnlinePlayers()) if (isMember(online, k)) online.sendMessage("§a¡La construcción/mejora del Nexo de §e" + k.name + " §aha finalizado!");
        }, Math.max(1L, duration / 50L)).getTaskId();
        nexusBuildTasks.put(kk, task);
    }

    private static final class Kingdom {
        String name; UUID kingId; String kingName; String nexusWorld="world", nation=""; int nexusX,nexusY,nexusZ; boolean nexusPlaced=true;
        long createdAt=System.currentTimeMillis(); long nexusBuildingUntil=0L; int pendingLevel=0; int level=1; double moonstones=0.0; final Set<String> claims=new LinkedHashSet<>(); final Map<String,Member> members=new LinkedHashMap<>(); final ItemStack[] nexusChest = new ItemStack[9]; final Map<String,Integer> extraLevels=new HashMap<>();
        Kingdom(String name,UUID id,String kingName){this.name=name;this.kingId=id;this.kingName=kingName;}
    }
    private static final class MailMessage {
        final long id; String senderKingdom, recipientKingdom; final String subject, body; final long createdAt; boolean read=false; final List<ItemStack> attachments=new ArrayList<>();
        MailMessage(long id,String sender,String recipient,String subject,String body,long createdAt){this.id=id;this.senderKingdom=sender;this.recipientKingdom=recipient;this.subject=subject;this.body=body;this.createdAt=createdAt;}
    }
    private record Member(String name,String rank) {}
    private static final class Nation { final String name; String founderKingdom; final Set<String> kingdoms=new LinkedHashSet<>(); Nation(String n,String f){name=n;founderKingdom=f;} }
}
