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

import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.block.InteractBlockEvent;
import org.spongepowered.api.event.entity.MoveEntityEvent;
import org.spongepowered.api.event.filter.Getter;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class pListener {
    @Listener
    public void onPlayerJoin(ClientConnectionEvent.Join event) {
        if (!Stargate.enableBungee) return;

        Player player = event.getTargetEntity();
        String destination = Stargate.bungeeQueue.remove(player.getName().toLowerCase());
        if (destination == null) return;

        Portal portal = Portal.getBungeeGate(destination);
        if (portal == null) {
            Stargate.debug("PlayerJoin", "Error fetching destination portal: " + destination);
            return;
        }
        portal.teleport(player, portal, null);
    }

    @Listener
    public void onPlayerPortal(MoveEntityEvent.Teleport.Portal event) {
        // Do a quick check for a stargate
        Transform<World> from = event.getFromTransform();
        Location<World> fromLoc = from.getLocation();
        World world = from.getExtent();
        int cX = fromLoc.getBlockX();
        int cY = fromLoc.getBlockY();
        int cZ = fromLoc.getBlockZ();
        for (int i = -2; i < 2; i++) {
            for (int j = -2; j < 2; j++) {
                for (int k = -2; k < 2; k++) {
                    Location<World> b = world.getLocation(cX + i, cY + j, cZ + k);
                    // We only need to worry about portal mat
                    // Commented out for now, due to new Minecraft insta-nether
                    //if (b.getType() != Material.PORTAL) continue;
                    Portal portal = Portal.getByEntrance(b);
                    if (portal != null) {
                        event.setCancelled(true);
                        return;
                    }
                }
            }
        }
    }

    @Listener
    @Exclude(MoveEntityEvent.Teleport.Portal.class)
    public void onPlayerMove(MoveEntityEvent event, @Getter("getTargetEntity") Player player) {

        // Check to see if the player actually moved
        if (event.getFromTransform().getLocation().getBlockX() == event.getToTransform().getLocation().getBlockX()
                && event.getFromTransform().getLocation().getBlockY() == event.getToTransform().getLocation().getBlockY()
                && event.getFromTransform().getLocation().getBlockZ() == event.getToTransform().getLocation().getBlockZ()) {
            return;
        }

        Portal portal = Portal.getByEntrance(event.getToTransform().getLocation());
        // No portal or not open
        if (portal == null || !portal.isOpen()) return;

        // Not open for this player
        if (!portal.isOpenFor(player)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            portal.teleport(player, portal, event);
            return;
        }

        Portal destination = portal.getDestination(player);
        if (!portal.isBungee() && destination == null) return;

        boolean deny = false;
        // Check if player has access to this server for Bungee gates
        if (portal.isBungee()) {
            if (!Stargate.canAccessServer(player, portal.getNetwork())) {
                deny = true;
            }
        } else {
            // Check if player has access to this network
            if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                deny = true;
            }

            // Check if player has access to destination world
            if (!Stargate.canAccessWorld(player, destination.getWorld().getName())) {
                deny = true;
            }
        }

        if (!Stargate.canAccessPortal(player, portal, deny)) {
            Stargate.sendMessage(player, Stargate.getString("denyMsg"));
            portal.teleport(player, portal, event);
            portal.close(false);
            return;
        }

        BigDecimal cost = Stargate.getUseCost(player, portal, destination);
        if (cost.compareTo(BigDecimal.ZERO) > 0) {
            UUID target = portal.getGate().getToOwner() ? portal.getOwner() : null;
            if (!Stargate.chargePlayer(player, target, cost)) {
                // Insufficient Funds
                Stargate.sendMessage(player, "Insufficient Funds");
                portal.close(false);
                return;
            }
            String deductMsg = Stargate.getString("ecoDeduct");
            deductMsg = Stargate.replaceVars(deductMsg, new String[] {"%cost%", "%portal%"}, new String[] {TextSerializers.FORMATTING_CODE.serialize(
                    iConomyHandler.format(cost)), portal.getName()});
            Stargate.sendMessage(player, deductMsg, false);
            if (target != null) {
                Optional<Player> p = Sponge.getServer().getPlayer(target);
                if (p.isPresent()) {
                    String obtainedMsg = Stargate.getString("ecoObtain");
                    obtainedMsg = Stargate.replaceVars(obtainedMsg, new String[] {"%cost%", "%portal%"}, new String[] {TextSerializers.FORMATTING_CODE.serialize(iConomyHandler.format(cost)), portal.getName()});
                    Stargate.sendMessage(p.get(), obtainedMsg, false);
                }
            }
        }

        Stargate.sendMessage(player, Stargate.getString("teleportMsg"), false);

        // BungeeCord Support
        if (portal.isBungee()) {
            if (!Stargate.enableBungee) {
                player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(Stargate.getString("bungeeDisabled")));
                portal.close(false);
                return;
            }

            // Teleport the player back to this gate, for sanity's sake
            portal.teleport(player, portal, event);

            // Send the SGBungee packet first, it will be queued by BC if required
            // Build the message, format is <player>#@#<destination>
            String msg = player.getName() + "#@#" + portal.getDestinationName();
            // Build the message data, sent over the SGBungee bungeecord channel
            Stargate.channel.sendTo(player, buf -> buf
                    .writeUTF("Forward")
                    .writeUTF(portal.getNetwork())
                    .writeUTF("SGBungee")
                    .writeShort((short) msg.getBytes().length)
                    .writeBytes(msg.getBytes()));

            // Connect player to new server
            Stargate.channel.sendTo(player, buf -> buf
                    .writeUTF("Connect")
                    .writeUTF(portal.getNetwork()));

            // Close portal if required (Should never be)
            portal.close(false);
            return;
        }

        destination.teleport(player, portal, event);
        portal.close(false);
    }

    @Listener
    @Include({InteractBlockEvent.Secondary.MainHand.class, InteractBlockEvent.Primary.MainHand.class})
    public void onPlayerInteract(InteractBlockEvent event, @First Player player) {
        if (event.getTargetBlock().getState().getType().equals(BlockTypes.AIR)) return;
        if (!event.getTargetBlock().getLocation().isPresent()) return;
        Location<World> block = event.getTargetBlock().getLocation().get();

        // Right click
        if (event instanceof InteractBlockEvent.Secondary) {
            InteractBlockEvent.Secondary rclickEvent = (InteractBlockEvent.Secondary) event;
            if (block.getBlockType().equals(BlockTypes.WALL_SIGN)) {
                Portal portal = Portal.getByBlock(block);
                if (portal == null) return;

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                if ((!portal.isOpen()) && (!portal.isFixed())) {
                    portal.cycleDestination(player);
                }
                return;
            }

            // Implement right-click to toggle a stargate, gets around spawn protection problem.
            if ((block.getBlockType().equals(BlockTypes.STONE_BUTTON))) {
                Portal portal = Portal.getByBlock(block);
                if (portal == null) return;

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                Stargate.blockPopulatorQueue.add(new BloxPopulator(portal.getButton(), portal.getButton().getData()
                        .with(Keys.POWERED, false).get()));
                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                Stargate.openPortal(player, portal);
            }
            return;
        }

        // Left click
        if (event instanceof InteractBlockEvent.Primary) {
            InteractBlockEvent.Primary lclickEvent = (InteractBlockEvent.Primary) event;
            // Check if we're scrolling a sign
            if (block.getBlockType().equals(BlockTypes.WALL_SIGN)) {
                Portal portal = Portal.getByBlock(block);
                if (portal == null) return;

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                    return;
                }

                if ((!portal.isOpen()) && (!portal.isFixed())) {
                    portal.cycleDestination(player, -1);
                }
                return;
            }

            // Check if we're pushing a button.
            if (block.getBlockType().equals(BlockTypes.STONE_BUTTON)) {
                Portal portal = Portal.getByControl(block);
                if (portal == null) return;

                boolean deny = false;
                if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                    deny = true;
                }

                if (!Stargate.canAccessPortal(player, portal, deny)) {
                    Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                } else {
                    Stargate.openPortal(player, portal);
                }
            }
        }
    }
}