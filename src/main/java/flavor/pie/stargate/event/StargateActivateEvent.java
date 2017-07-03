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

import java.util.ArrayList;

import flavor.pie.stargate.Portal;
import flavor.pie.stargate.Stargate;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;

public class StargateActivateEvent extends StargateEvent {
    private ArrayList<String> destinations;
    private String destination;

    public StargateActivateEvent(Portal portal, Player player, ArrayList<String> destinations, String destination) {
        super("StargateActivateEvent", portal, Cause.source(player).named("Stargate", Stargate.stargateContainer).build());

        this.destinations = destinations;
        this.destination = destination;
    }

    public ArrayList<String> getDestinations() {
        return destinations;
    }
    
    public void setDestinations(ArrayList<String> destinations) {
        this.destinations = destinations;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
}
