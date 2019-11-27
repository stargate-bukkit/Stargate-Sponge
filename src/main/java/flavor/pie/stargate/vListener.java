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

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntityTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.entity.MoveEntityEvent;


public class vListener {
    @Listener
    public void onVehicleMove(MoveEntityEvent event) {
        Entity passenger = event.getTargetEntity().getPassengers().stream().findFirst().orElse(null);
        Entity vehicle = event.getTargetEntity();
        if (vehicle.getType().equals(EntityTypes.PLAYER)) return;

        Portal portal = Portal.getByEntrance(event.getToTransform().getLocation());
        if (portal == null || !portal.isOpen()) return;

        // We don't support vehicles in Bungee portals
        if (portal.isBungee()) return;

        if (passenger instanceof Player) {
            Player player = (Player)passenger;
            if (!portal.isOpenFor(player)) {
                Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                return;
            }

            Portal dest = portal.getDestination(player);
            if (dest == null) return;
            boolean deny = false;
            // Check if player has access to this network
            if (!Stargate.canAccessNetwork(player, portal.getNetwork())) {
                deny = true;
            }

            // Check if player has access to destination world
            if (!Stargate.canAccessWorld(player, dest.getWorld().getName())) {
                deny = true;
            }

            if (!Stargate.canAccessPortal(player, portal, deny)) {
                Stargate.sendMessage(player, Stargate.getString("denyMsg"));
                portal.close(false);
                return;
            }

            Stargate.sendMessage(player, Stargate.getString("teleportMsg"), false);
            dest.teleport(vehicle);
            portal.close(false);
        } else {
            Portal dest = portal.getDestination();
            if (dest == null) return;
            dest.teleport(vehicle);
        }
    }
}