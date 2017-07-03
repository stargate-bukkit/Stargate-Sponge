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

import flavor.pie.stargate.event.StargateDestroyEvent;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.ChangeBlockEvent;
import org.spongepowered.api.event.block.tileentity.ChangeSignEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.filter.type.Exclude;
import org.spongepowered.api.event.filter.type.Include;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.world.LocatableBlock;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.math.BigDecimal;
import java.util.Optional;

public class bListener {
    @Listener
    public void onSignChange(ChangeSignEvent event, @First Player player) {
        Location<World> loc = event.getTargetTile().getLocation();
        if (!loc.getBlockType().equals(BlockTypes.WALL_SIGN)) return;

        final Portal portal = Portal.createPortal(event, player);
        // Not creating a gate, just placing a sign
        if (portal == null)    return;

        Stargate.sendMessage(player, Stargate.getString("createMsg"), false);
        Stargate.debug("onSignChange", "Initialized stargate: " + portal.getName());
        Task.builder().delayTicks(1).execute(portal::drawSign).submit(Stargate.stargate);
    }

    // Switch to LAST order so as to come after block protection plugins (Hopefully)
    @Listener(order = Order.LAST)
    public void onBlockBreak(ChangeBlockEvent.Break event, @Named(NamedCause.SOURCE) Player player) {
        Location<World> block = event.getTransactions().get(0).getOriginal().getLocation().get();

        Portal portal = Portal.getByBlock(block);
        if (portal == null && Stargate.protectEntrance)
            portal = Portal.getByEntrance(block);
        if (portal == null) return;
        if ((portal.getButton() != null && block.getBlockPosition().equals(portal.getButton().getBlock().getBlockPosition()))
                || block.getBlockPosition().equals(portal.getSign().getBlockPosition())) {
            event.setCancelled(true);
        }
        boolean deny = false;
        String denyMsg = "";

        if (!Stargate.canDestroy(player, portal)) {
            denyMsg = "Permission Denied"; // TODO: Change to Stargate.getString()
            deny = true;
            Stargate.log.info(player.getName() + " tried to destroy gate");
        }

        BigDecimal cost = Stargate.getDestroyCost(player,  portal.getGate());

        StargateDestroyEvent dEvent = new StargateDestroyEvent(portal, player, deny, denyMsg, cost);
        Sponge.getEventManager().post(dEvent);
        if (dEvent.isCancelled()) {
            event.setCancelled(true);
            return;
        }
        if (dEvent.getDeny()) {
            Stargate.sendMessage(player, dEvent.getDenyReason());
            event.setCancelled(true);
            return;
        }

        cost = dEvent.getCost();

        if (cost.compareTo(BigDecimal.ZERO) != 0) {
            if (!Stargate.chargePlayer(player, (String) null, cost)) {
                Stargate.debug("onBlockBreak", "Insufficient Funds");
                Stargate.sendMessage(player, Stargate.getString("ecoInFunds"));
                event.setCancelled(true);
                return;
            }

            if (cost.compareTo(BigDecimal.ZERO) > 0) {
                String deductMsg = Stargate.getString("ecoDeduct");
                deductMsg = Stargate.replaceVars(deductMsg, new String[] {"%cost%", "%portal%"}, new String[] {TextSerializers.FORMATTING_CODE.serialize(iConomyHandler.format(cost)), portal.getName()});
                Stargate.sendMessage(player, deductMsg, false);
            } else if (cost.compareTo(BigDecimal.ZERO) < 0) {
                String refundMsg = Stargate.getString("ecoRefund");
                refundMsg = Stargate.replaceVars(refundMsg, new String[] {"%cost%", "%portal%"}, new String[] {TextSerializers.FORMATTING_CODE.serialize(iConomyHandler.format(cost.negate())), portal.getName()});
                Stargate.sendMessage(player, refundMsg, false);
            }
        }

        portal.unregister(true, true);
        Stargate.sendMessage(player, Stargate.getString("destroyMsg"), false);
    }

    @Listener
    @Include({ChangeBlockEvent.Decay.class, ChangeBlockEvent.Modify.class, ChangeBlockEvent.Break.class})
    public void onBlockPhysics(ChangeBlockEvent event) {
        if (event.getCause().get(NamedCause.SOURCE, Player.class).isPresent()) return;
        if (event.getCause().contains(Stargate.stargateContainer)) return;
        for (Transaction<BlockSnapshot> trans : event.getTransactions()) {
            if (!trans.getOriginal().getLocation().isPresent()) return;
            Location<World> block = trans.getOriginal().getLocation().get();
            Portal portal;
            // Handle keeping portal material and buttons around
            if (block.getBlockType().equals(BlockTypes.STONE_BUTTON)) {
                portal = Portal.getByControl(block);
            } else {
                portal = Portal.getByEntrance(block);
            }
            if (portal == null) {
                portal = Portal.getByBlock(block);
            }
            if (portal != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @Listener
    public void onBlockFromTo(ChangeBlockEvent.Place event, @Named(NamedCause.SOURCE) LocatableBlock block) {
        Portal portal = Portal.getByEntrance(block.getLocation());

        if (portal != null) {
            BlockType type = block.getBlockState().getType();
            if (type.equals(BlockTypes.WATER) || type.equals(BlockTypes.FLOWING_WATER)
                    || type.equals(BlockTypes.LAVA) || type.equals(BlockTypes.FLOWING_LAVA)) {
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onPistonExtend(ChangeBlockEvent.Pre event, @Named(NamedCause.SOURCE) LocatableBlock block) {
        // mod compatibility?
//            if (!block.getBlockState().get(Keys.PISTON_TYPE).isPresent()) return;
        for (Location<World> loc : event.getLocations()) {
            Portal portal = Portal.getByBlock(loc);
            if (portal != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    // The portal interior cannot be broken.
    @Listener(order = Order.EARLY)
    @Exclude(ChangeBlockEvent.Post.class)
    public void onBreakPortal(ChangeBlockEvent event) {
        if (event.getCause().contains(Stargate.stargateContainer)) return;
        for (Transaction<BlockSnapshot> trans : event.getTransactions()) {
            Optional<Location<World>> loc = trans.getOriginal().getLocation();
            if (!loc.isPresent()) continue;
            if (Portal.getByEntrance(loc.get()) != null) {
                event.setCancelled(true);
                break;
            }
        }
    }

}