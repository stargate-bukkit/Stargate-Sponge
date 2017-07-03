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

package flavor.pie.stargate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.world.LoadWorldEvent;
import org.spongepowered.api.event.world.UnloadWorldEvent;
import org.spongepowered.api.world.World;

public class wListener {
    @Listener
    public void onWorldLoad(LoadWorldEvent event) {
        World w = event.getTargetWorld();
        Portal.loadAllGates(w);
    }

    // We need to reload all gates on world unload, boo
    @Listener
    public void onWorldUnload(UnloadWorldEvent event) {
        Stargate.debug("onWorldUnload", "Reloading all Stargates");
        World w = event.getTargetWorld();
        Portal.clearGates();
        for (World world : Sponge.getServer().getWorlds()) {
            if (world.getUniqueId().equals(w.getUniqueId())) continue;
            Portal.loadAllGates(world);
        }
    }
}