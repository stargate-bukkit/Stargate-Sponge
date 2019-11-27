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
import org.spongepowered.api.entity.living.player.Player;
import java.util.List;

public class StargateCreateEvent extends StargateEvent {
    private boolean deny;
    private String denyReason;
    private List<String> lines;

    
    public StargateCreateEvent(Player player, Portal portal, List<String> lines, boolean deny, String denyReason) {
        super("StargateCreateEvent", portal, Sponge.getCauseStackManager().getCurrentCause());
        this.lines = lines;
        this.deny = deny;
        this.denyReason = denyReason;
    }
    
    public String getLine(int index) throws IndexOutOfBoundsException {
        return lines.get(index);
    }
    
    public boolean getDeny() {
        return deny;
    }
    
    public void setDeny(boolean deny) {
        this.deny = deny;
    }
    
    public String getDenyReason() {
        return denyReason;
    }
    
    public void setDenyReason(String denyReason) {
        this.denyReason = denyReason;
    }

}
