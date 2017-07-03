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

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.Transform;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.util.Direction;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class Blox {
    private int x;
    private int y;
    private int z;
    private World world;
    private Blox parent = null;

    public Blox (World world, int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
    }
    
    public Blox (BlockSnapshot block) {
        this(block.getLocation().get());
    }
    
    public Blox (Location<World> location) {
        this.x = location.getBlockX();
        this.y = location.getBlockY();
        this.z = location.getBlockZ();
        this.world = location.getExtent();
    }
    
    public Blox (World world, String string) {
        String[] split = string.split(",");
        this.x = Integer.parseInt(split[0]);
        this.y = Integer.parseInt(split[1]);
        this.z = Integer.parseInt(split[2]);
        this.world = world;
    }
    
    public Blox makeRelative(int x, int y, int z) {
        return new Blox(this.world, this.x + x, this.y + y, this.z + z);
    }
    
    public Transform<World> makeRelativeLoc(double x, double y, double z, double rotX, double rotY) {
        //noinspection SuspiciousNameCombination
        return new Transform<>(this.world,
				new Vector3d((double) this.x + x, (double) this.y + y, (double) this.z + z),
				new Vector3d(rotY, rotX, 0));
    }

    public Blox modRelative(int right, int depth, int distance, int modX, int modY, int modZ) {
         return makeRelative(-right * modX + distance * modZ, -depth * modY, -right * modZ + -distance * modX);
    }

    public Transform<World> modRelativeLoc(double right, double depth, double distance, double rotX, double rotY, int modX, int modY, int modZ) {
        return makeRelativeLoc(0.5 + -right * modX + distance * modZ, depth, 0.5 + -right * modZ + -distance * modX, rotX, rotY);
    }

    public void setType(BlockType type) {
        world.getLocation(x, y, z).setBlockType(type, Cause.source(Stargate.stargateContainer).build());
    }

    public BlockType getType() {
        return world.getLocation(x, y, z).getBlockType();
    }

    public void setData(BlockState data) {
        world.getLocation(x, y, z).setBlock(data, Cause.source(Stargate.stargateContainer).build());
    }

    public BlockState getData() {
        return world.getBlock(x, y, z);
    }

    public Location<World> getBlock() {
        return world.getLocation(x, y, z);
    }
    
    public int getX() {
        return x;
    }
    
    public int getY() {
        return y;
    }
    
    public int getZ() {
        return z;
    }
    
    public World getWorld() {
        return world;
    }
    
    public Location<World> getParent() {
        if (parent == null) findParent();
        if (parent == null) return null;
        return parent.getBlock();
    }
    
    private void findParent() {
        int offsetX = 0;
        int offsetY = 0;
        int offsetZ = 0;
        
        if (getBlock().getBlockType().equals(BlockTypes.WALL_SIGN)) {
			Direction d = getBlock().get(Keys.DIRECTION).get();
            if (d.equals(Direction.NORTH)) {
                offsetZ = 1;
            } else if (d.equals(Direction.SOUTH)) {
                offsetZ = -1;
            } else if (d.equals(Direction.WEST)) {
                offsetX = 1;
            } else if (d.equals(Direction.EAST)) {
                offsetX = -1;
            }
        } else if (getBlock().getBlockType().equals(BlockTypes.WALL_SIGN)) {
            offsetY = -1;
        } else {
            return;
        }
        parent = new Blox(world, getX() + offsetX, getY() + offsetY, getZ() + offsetZ);
    }
    
    public String toString() {
        StringBuilder builder = new StringBuilder();
        //builder.append(world.getName());
        //builder.append(',');
        builder.append(x);
        builder.append(',');
        builder.append(y);
        builder.append(',');
        builder.append(z);
        return builder.toString();
    }
    
    @Override
    public int hashCode() {
        int result = 18;
        
        result = result * 27 + x;
        result = result * 27 + y;
        result = result * 27 + z;
        result = result * 27 + world.getName().hashCode();
        
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        Blox blox = (Blox) obj;
        return (x == blox.x) && (y == blox.y) && (z == blox.z) && (world.getName().equals(blox.world.getName())); 
    }
}