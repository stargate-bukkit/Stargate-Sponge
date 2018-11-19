/*
 * Stargate - A portal plugin for Bukkit
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

package flavor.pie.stargate.event;

import flavor.pie.stargate.Portal;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.entity.living.humanoid.player.TargetPlayerEvent;
import org.spongepowered.api.world.World;

public class StargatePortalEvent extends StargateEvent implements TargetPlayerEvent {
    private Player player;
    private Portal destination;
    private Transform<World> exit;
    
    public StargatePortalEvent(Player player, Portal portal, Portal dest, Transform<World> exit) {
        super ("StargatePortalEvent", portal, Sponge.getCauseStackManager().getCurrentCause());

        this.player = player;
        this.destination = dest;
        this.exit = exit;
    }

    /**
     * Return the player that went through the gate.
     * @return player that went through the gate
     */
    @Override
    public Player getTargetEntity() {
        return player;
    }
    
    /**
     * Return the destination gate
     * @return destination gate
     */
    public Portal getDestination() {
        return destination;
    }
    
    /**
     * Return the location of the players exit point
     * @return org.bukkit.Location Location of the exit point
     */
    public Transform<World> getExit() {
        return exit;
    }
    
    /**
     * Set the location of the players exit point
     */
    public void setExit(Transform<World> loc) {
        this.exit = loc;
    }
}
