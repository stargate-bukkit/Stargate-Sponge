/*
 * Stargate - A portal plugin for Bukkit
 * Copyright (C) 2011 Shaun (sturmeh)
 * Copyright (C) 2011 Dinnerbone
 * Copyright (C) 2011, 2012 Steven "Drakia" Scott <Contact@TheDgtl.net>
 * Copyright (C) 2017 Adam Spofford <pieflavor.mc@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package flavor.pie.stargate;

import com.google.common.collect.ImmutableList;
import com.google.common.reflect.TypeToken;
import com.google.inject.Inject;
import flavor.pie.stargate.event.StargateAccessEvent;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader;
//import org.bstats.sponge.MetricsLite2;	//Sorry, I don't want to bother with figuring this out.
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.tileentity.Sign;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.config.DefaultConfig;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.service.permission.SubjectData;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tristate;
import org.spongepowered.api.world.BlockChangeFlags;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
@Plugin(id = "stargate", name = "Stargate", version = "0.1.4", authors = {"pie_flavor", "Dinnerbone", "Drakia"}, description = "The classic portal plugin, ported to Sponge.")
public class Stargate {
    public static Logger log;
    public static Stargate stargate;
    public static PluginContainer stargateContainer;
    private static LangLoader lang;
    
    private static Path portalFolder;
    private static Path gateFolder;
    private static Path langFolder;
    private static int activeTime = 10;
    private static int openTime = 10;
    public static boolean enableBungee = false;
    public static ChannelBinding.RawDataChannel channel;
    
    public static ConcurrentLinkedQueue<Portal> openList = new ConcurrentLinkedQueue<>();
    public static ConcurrentLinkedQueue<Portal> activeList = new ConcurrentLinkedQueue<>();
    
    // Used for populating gate open/closed material.
    public static Queue<BloxPopulator> blockPopulatorQueue = new LinkedList<>();
    
    // HashMap of player names for Bungee support
    public static Map<String, String> bungeeQueue = new HashMap<>();

    @Inject
    Logger logger;
    @Inject @ConfigDir(sharedRoot = false)
    Path dir;
    @Inject @DefaultConfig(sharedRoot = false)
    Path configFile;
    @Inject @DefaultConfig(sharedRoot = false)
    ConfigurationLoader<CommentedConfigurationNode> loader;
    @Inject
    PluginContainer container;
    /*@Inject
    MetricsLite2 metrics;*/

    public static Config config;

    private Path getDataFolder() {
        return dir;
    }

    @Listener
    public void onDisable(GameStoppingServerEvent e) {
        Portal.closeAllGates();
        Portal.clearGates();
    }

    @Listener
    public void onEnable(GameStartedServerEvent e) throws IOException, ObjectMappingException {
        stargateContainer = container;
        
        log = logger;
        Stargate.stargate = this;
        
        //Set the various folders
        portalFolder = dir.resolve(Paths.get("portals"));
        gateFolder = dir.resolve(Paths.get("gates"));
        langFolder = dir.resolve(Paths.get("lang"));

        // Register events before loading gates to stop weird things happening.
        Sponge.getEventManager().registerListeners(this, new pListener());
        Sponge.getEventManager().registerListeners(this, new bListener());
        
        Sponge.getEventManager().registerListeners(this, new vListener());
        Sponge.getEventManager().registerListeners(this, new eListener());
        Sponge.getEventManager().registerListeners(this, new wListener());
        Sponge.getCommandManager().register(this, new SGCommand(), "stargate");
        
        this.loadConfig();
        
        // Enable the required channels for Bungee support
        if (enableBungee) {
            channel = Sponge.getChannelRegistrar().createRawChannel(this, "BungeeCord");
            channel.addListener(new pmListener());
        }
        
        // It is important to load languages here, as they are used during reloadGates()
        lang = new LangLoader(langFolder, Stargate.config.lang);
        
        this.migrate();
        this.reloadGates();
        
        Task.builder().execute(new SGThread()).intervalTicks(100).submit(this);
        Task.builder().execute(new BlockPopulatorThread()).intervalTicks(1).submit(this);
    }

    private void loadConfig() {
        try {
            boolean convert = false;
            if (!Files.exists(configFile)) {
                Path oldConfig = dir.resolve(Paths.get("config.yml"));
                if (Files.exists(oldConfig)) {
                    convert = true;
                    YAMLConfigurationLoader loader = YAMLConfigurationLoader.builder().setPath(oldConfig).build();
                    Config.Old old = loader.load().getValue(Config.Old.type);
                    config = old.convert();
                    this.loader.save(this.loader.createEmptyNode().setValue(Config.type, config));
                }
                Sponge.getAssetManager().getAsset(this, "default.conf").get().copyToFile(configFile);
            }
            if (!convert) {
                config = loader.load().getValue(Config.type);
            }
        } catch (Exception ex) {
            rethrow(ex);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T extends Throwable, V> V rethrow(Throwable t) throws T {
        throw (T) t;
    }

    public void reloadGates() throws IOException {
        // Close all gates prior to reloading
        for (Portal p : openList) {
            p.close(true);
        }
        
        Gate.loadGates(gateFolder);
        log.info("Loaded " + Gate.getGateCount() + " gate layouts");
        for (World world : Sponge.getServer().getWorlds()) {
            Portal.loadAllGates(world);
        }
    }
    
    private void migrate() throws IOException {
        // Only migrate if new file doesn't exist.
        if (!Files.exists(portalFolder)) {
            Files.createDirectories(portalFolder);
        }
        Path newFile = portalFolder.resolve(Paths.get(Sponge.getServer().getDefaultWorld().get().getWorldName() + ".db"));
        if (!Files.exists(newFile)) {
            Files.createDirectories(dir.getParent());
            // Migrate not-so-old stargate db
            Path oldishFile = dir.resolve(Paths.get("stargate.db"));
            if (Files.exists(oldishFile)) {
                Stargate.log.info("Migrating existing stargate.db");
                Files.move(oldishFile, newFile);
            }
        }
        
        // Can't migrate old gates because of blockstates
    }
    
    public static void debug(String rout, String msg) {
        if (Stargate.config.debug) {
            log.info("[SG::" + rout + "] " + msg);
        }
    }
    
    public static void sendMessage(CommandSource player, String message) {
        sendMessage(player, message, true);
    }
    
    public static void sendMessage(CommandSource player, String message, boolean error) {
        if (message.isEmpty()) return;
        Text msg = TextSerializers.FORMATTING_CODE.deserialize(message);
        if (error)
            player.sendMessage(Text.of(TextColors.RED, Stargate.getString("prefix"), TextColors.WHITE, msg));
        else
            player.sendMessage(Text.of(TextColors.GREEN, Stargate.getString("prefix"), TextColors.WHITE, msg));
    }
    
    public static void setLine(Sign sign, int index, String text) {
        sign.offer(sign.lines().set(index, Text.builder(text).color(Stargate.config.portal.signColor).build()));
    }

    public static void setLine(Sign sign, int index, Text text) {
        sign.offer(sign.lines().set(index, Text.builder().append(text).color(Stargate.config.portal.signColor).build()));
    }

    public static Path getSaveLocation() {
        return portalFolder;
    }
    
    public static Path getGateFolder() {
        return gateFolder;
    }

    public static String getDefaultNetwork() {
        return config.portal.defaultGateNetwork;
    }
    
    public static String getString(String name) {
        return lang.getString(name);
    }

    public static void openPortal(Player player, Portal portal) {
        Portal destination = portal.getDestination();

        // Always-open gate -- Do nothing
        if (portal.isAlwaysOn()) {
            return;
        }
        
        // Random gate -- Do nothing
        if (portal.isRandom())
            return;
        
        // Invalid destination
        if ((destination == null) || (destination == portal)) {
            Stargate.sendMessage(player, Stargate.getString("invalidMsg"));
            return;
        }
        
        // Gate is already open
        if (portal.isOpen()) {
            // Close if this player opened the gate
            if (portal.getActivePlayer() == player) {
                portal.close(false);
            }
            return;
        }
        
        // Gate that someone else is using -- Deny access
        if ((!portal.isFixed()) && portal.isActive() &&  (portal.getActivePlayer() != player)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            return;
        }
        
        // Check if the player can use the private gate
        if (portal.isPrivate() && !Stargate.canPrivate(player, portal)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            return;
        }
        
        // Destination blocked
        if ((destination.isOpen()) && (!destination.isAlwaysOn())) {
            Stargate.sendMessage(player, Stargate.getString("blockMsg"));
            return;
        }
        
        // Open gate
        portal.open(player, false);
    }

    /*
     * Check whether the player has the given permissions.
     */
    public static boolean hasPerm(Player player, String perm) {
        Stargate.debug("hasPerm::Permission(" + player.getName() + ")", perm + " => " + player.hasPermission(perm));
        return player.hasPermission(perm);
    }
    
    /*
     * Check a deep permission, this will check to see if the permissions is defined for this use
     * If using Permissions it will return the same as hasPerm
     * If using SuperPerms will return true if the node isn't defined
     * Or the value of the node if it is
     */
    public static boolean hasPermDeep(Player player, String perm) {
        if (player.getPermissionValue(SubjectData.GLOBAL_CONTEXT, perm).equals(Tristate.UNDEFINED)) {
            Stargate.debug("hasPermDeep::Permission(" + player.getName() + ")", perm + " => true");
            return true;
        }
        Stargate.debug("hasPermDeep::Permission(" + player.getName() + ")", perm + " => " + player.hasPermission(perm));
        return player.hasPermission(perm);
    }
    
    /*
     * Check whether player can teleport to dest world
     */
    public static boolean canAccessWorld(Player player, String world) {
        // Can use all Stargate player features or access all worlds
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.world")) {
            // Do a deep check to see if the player lacks this specific world node
            if (!hasPermDeep(player, "stargate.world." + world)) return false;
            return true;
        }
        // Can access dest world
        if (hasPerm(player, "stargate.world." + world)) return true;
        return false;
    }
    
    /*
     * Check whether player can use network
     */
    public static boolean canAccessNetwork(Player player, String network) {
        // Can user all Stargate player features, or access all networks
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.network")) {
            // Do a deep check to see if the player lacks this specific network node
            if (!hasPermDeep(player, "stargate.network." + network)) return false;
            return true;
        }
        // Can access this network
        if (hasPerm(player, "stargate.network." + network)) return true;
        // Is able to create personal gates (Assumption is made they can also access them)
        String playerName = player.getName();
        if (playerName.length() > 11) playerName = playerName.substring(0, 11);
        if (network.equals(playerName) && hasPerm(player, "stargate.create.personal")) return true;
        return false;
    }
    
    /*
     * Check whether the player can access this server
     */
    public static boolean canAccessServer(Player player, String server) {
        // Can user all Stargate player features, or access all servers
        if (hasPerm(player, "stargate.use") || hasPerm(player, "stargate.servers")) {
            // Do a deep check to see if the player lacks this specific server node
            if (!hasPermDeep(player, "stargate.server." + server)) return false;
            return true;
        }
        // Can access this server
        if (hasPerm(player, "stargate.server." + server)) return true;
        return false;
    }
    
    /*
     * Call the StargateAccessPortal event, used for other plugins to bypass Permissions checks
     */
    public static boolean canAccessPortal(Player player, Portal portal, boolean deny) {
        StargateAccessEvent event = new StargateAccessEvent(player, portal, deny);
        Sponge.getEventManager().post(event);
        if (event.getDeny()) return false;
        return true;
    }
    
    /*
     * Return true if the portal is free for the player
     */
    public static boolean isFree(Player player, Portal src, Portal dest) {
    	return true;
    	/*
        // This gate is free
        if (src.isFree()) return true;
        // Player gets free use
        if (hasPerm(player, "stargate.free") || Stargate.hasPerm(player,  "stargate.free.use")) return true;
        // Don't charge for free destination gates
        if (dest != null && !config.economy.freeDestination && dest.isFree()) return true;
        return false;*/
    }
    
    /*
     * Check whether the player can see this gate (Hidden property check)
     */
    public static boolean canSee(Player player, Portal portal) {
        // The gate is not hidden
        if (!portal.isHidden()) return true;
        // The player is an admin with the ability to see hidden gates
        if (hasPerm(player, "stargate.admin") || hasPerm(player, "stargate.admin.hidden")) return true;
        // The player is the owner of the gate
        if (portal.getOwner().equals(player.getUniqueId())) return true;
        return false;
    }
    
    /*
     * Check if the player can use this private gate
     */
    public static boolean canPrivate(Player player, Portal portal) {
        // Check if the player is the owner of the gate
        if (portal.getOwner().equals(player.getUniqueId())) return true;
        // The player is an admin with the ability to use private gates
        if (hasPerm(player, "stargate.admin") || hasPerm(player, "stargate.admin.private")) return true;
        return false;
    }
    
    /*
     * Check if the player has access to {option}
     */
    public static boolean canOption(Player player, String option) {
        // Check if the player can use all options
        if (hasPerm(player, "stargate.option")) return true;
        // Check if they can use this specific option
        if (hasPerm(player, "stargate.option." + option)) return true;
        return false;
    }
    
    /*
     * Check if the player can create gates on {network}
     */
    public static boolean canCreate(Player player, String network) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for all network create permission
        if (hasPerm(player, "stargate.create.network")) {
            // Do a deep check to see if the player lacks this specific network node
            if (!hasPermDeep(player, "stargate.create.network." + network)) return false;
            return true;
        }
        // Check for this specific network
        if (hasPerm(player, "stargate.create.network." + network)) return true;
        
        return false;
    }
    
    /*
     * Check if the player can create a personal gate
     */
    public static boolean canCreatePersonal(Player player) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for personal
        if (hasPerm(player, "stargate.create.personal")) return true;
        return false;
    }
    
    /*
     * Check if the player can create this gate layout
     */
    public static boolean canCreateGate(Player player, String gate) {
        // Check for general create
        if (hasPerm(player, "stargate.create")) return true;
        // Check for all gate create permissions
        if (hasPerm(player, "stargate.create.gate")) {
            // Do a deep check to see if the player lacks this specific gate node
            if (!hasPermDeep(player, "stargate.create.gate." + gate)) return false;
            return true;
        }
        // Check for this specific gate
        if (hasPerm(player, "stargate.create.gate." + gate)) return true;
        
        return false;
    }
    
    /*
     * Check if the player can destroy this gate
     */
    public static boolean canDestroy(Player player, Portal portal) {
        String network = portal.getNetwork();
        // Check for general destroy
        if (hasPerm(player, "stargate.destroy")) return true;
        // Check for all network destroy permission
        if (hasPerm(player, "stargate.destroy.network")) {
            // Do a deep check to see if the player lacks permission for this network node
            if (!hasPermDeep(player, "stargate.destroy.network." + network)) return false;
            return true;
        }
        // Check for this specific network
        if (hasPerm(player, "stargate.destroy.network." + network)) return true;
        // Check for personal gate
        if (player.getUniqueId().equals(portal.getOwner()) && hasPerm(player, "stargate.destroy.personal")) return true;
        return false;
    }
    
    /*
     * Parse a given text string and replace the variables
     */
    public static String replaceVars(String format, String[] search, String[] replace) {
        if (search.length != replace.length) return "";
        for (int i = 0; i < search.length; i++) {
            format = format.replace(search[i], replace[i]);
        }
        return format;
    }
    
    private class BlockPopulatorThread implements Runnable {
        @Override
        public void run() {
            long sTime = System.nanoTime();
            while (System.nanoTime() - sTime < 50000000) {
                BloxPopulator b = Stargate.blockPopulatorQueue.poll();
                if (b == null) return;
                b.getBlox().getBlock().setBlock(b.getMat(), BlockChangeFlags.NONE);
            }
        }
    }
    
    private class SGThread implements Runnable {
        @Override
        public void run() {
            long time = System.currentTimeMillis() / 1000;
            // Close open portals
            for (Iterator<Portal> iter = Stargate.openList.iterator(); iter.hasNext();) {
                Portal p = iter.next();
                // Skip always open gates
                if (p.isAlwaysOn()) continue;
                if (!p.isOpen()) continue;
                if (time > p.getOpenTime() + Stargate.openTime) {
                    p.close(false);
                    iter.remove();
                }
            }
            // Deactivate active portals
            for (Iterator<Portal> iter = Stargate.activeList.iterator(); iter.hasNext();) {
                Portal p = iter.next();
                if (!p.isActive()) continue;
                if (time > p.getOpenTime() + Stargate.activeTime) {
                    p.deactivate();
                    iter.remove();
                }
            }
        }
    }
    // TODO replace messages with i18n
    private class SGCommand implements CommandCallable {
        @Override
        public CommandResult process(CommandSource sender, String arg) throws CommandException {
            String[] args = arg.split(" ");
            if (args.length != 1) throw new CommandException(getUsage(sender));
            if (args[0].equalsIgnoreCase("about")) {
                sender.sendMessage(Text.of("Stargate Plugin originally created by Dinnerbone"));
                sender.sendMessage(Text.of("Ported to Bukkit by Drakia"));
                sender.sendMessage(Text.of("Ported to Sponge by pie_flavor"));
                if (!lang.getString("author").isEmpty())
                    sender.sendMessage(Text.of("Language created by " + lang.getString("author")));
                return CommandResult.success();
            }
            if (sender instanceof Player) {
                Player p = (Player) sender;
                if (!hasPerm(p, "stargate.admin") && !hasPerm(p, "stargate.admin.reload")) {
                    sendMessage(sender, "Permission Denied");
                    return CommandResult.success();
                }
            }
            if (args[0].equalsIgnoreCase("reload")) {
                // Deactivate portals
                for (Portal p : activeList) {
                    p.deactivate();
                }
                // Close portals
                for (Portal p : openList) {
                    p.close(true);
                }
                // Clear all lists
                activeList.clear();
                openList.clear();
                Portal.clearGates();
                Gate.clearGates();

                // Store the old Bungee enabled value
                boolean oldEnableBungee = enableBungee;
                // Reload data
                loadConfig();
                try {
                    reloadGates();
                } catch (IOException ex) {
                    throw new CommandException(Text.of("Could not reload gates!"), ex);
                }
                lang.setLang(config.lang);
                lang.reload();

                // Enable the required channels for Bungee support
                if (oldEnableBungee != enableBungee) {
                    if (enableBungee) {
                        channel = Sponge.getChannelRegistrar().createRawChannel(Stargate.this, "BungeeCord");
                        channel.addListener(new pmListener());
                    } else {
                        Sponge.getChannelRegistrar().unbindChannel(channel);
                        channel = null;
                    }
                }

                sendMessage(sender, "Stargate reloaded");
                return CommandResult.success();
            }
            throw new CommandException(getUsage(sender));
        }

        @Override
        public List<String> getSuggestions(CommandSource source, String arguments, @Nullable Location<World> targetPosition) throws CommandException {
            String[] args = arguments.split(" ");
            // gotta love Java
            if (arguments.endsWith(" ")) {
                if (args.length == 0) {
                    if (source.hasPermission("stargate.admin.reload")) {
                        return ImmutableList.of("about", "reload");
                    } else {
                        return ImmutableList.of("about");
                    }
                }
            }
            if (args.length == 1) {
                if ("about".startsWith(args[0])) {
                    return ImmutableList.of("about");
                } else if ("reload".startsWith(args[0]) && source.hasPermission("stargate.admin.reload")) {
                    return ImmutableList.of("reload");
                }
            }
            return ImmutableList.of();
        }

        @Override
        public boolean testPermission(CommandSource source) {
            return true;
        }

        @Override
        public Optional<Text> getShortDescription(CommandSource source) {
            return Optional.of(Text.of("Stargate base command."));
        }

        @Override
        public Optional<Text> getHelp(CommandSource source) {
            return Optional.empty();
        }

        @Override
        public Text getUsage(CommandSource source) {
            if (!source.hasPermission("stargate.admin.reload")) {
                return Text.of("/stargate about");
            } else {
                return Text.of("/stargate [about|reload]");
            }
        }

    }
}
