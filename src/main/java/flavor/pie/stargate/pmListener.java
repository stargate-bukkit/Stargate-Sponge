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

import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;

import java.util.Optional;

public class pmListener implements RawDataListener {

    @Override
    public void handlePayload(ChannelBuf message, RemoteConnection unused, Platform.Type side) {
        if (!Stargate.enableBungee) return;
        
        // Get data from message
        String inChannel;
        byte[] data;
        try {
            inChannel = message.readUTF();
            short len = message.readShort();
            data = message.readBytes(len);
        } catch (Exception ex) {
            Stargate.log.error("Error receiving BungeeCord message");
            ex.printStackTrace();
            return;
        }
        
        // Verify that it's an SGBungee packet
        if (!inChannel.equals("SGBungee")) {
            return;
        }
        
        // Data should be player name, and destination gate name
        String msg = new String(data);
        String[] parts = msg.split("#@#");
        
        String playerName = parts[0];
        String destination = parts[1];
        
        // Check if the player is online, if so, teleport, otherwise, queue
        Optional<Player> player = Sponge.getServer().getPlayer(playerName);
        if (player.isPresent()) {
            Portal dest = Portal.getBungeeGate(destination);
            // Specified an invalid gate. For now we'll just let them connect at their current location
            if (dest == null) {
                Stargate.log.info("Bungee gate " + destination + " does not exist");
                return;
            }
            dest.teleport(player.get(), dest, null);
        } else {
            Stargate.bungeeQueue.put(playerName.toLowerCase(), destination);
        }
    }
}
