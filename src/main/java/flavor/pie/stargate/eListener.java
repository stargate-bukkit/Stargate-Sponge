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

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.entity.damage.source.BlockDamageSource;
import org.spongepowered.api.event.entity.DamageEntityEvent;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public class eListener {
    @Listener
    public void onEntityExplode(ExplosionEvent.Detonate event) {
        for (Location<World> b : event.getAffectedLocations()) {
            Portal portal = Portal.getByBlock(b);
            if (portal == null) continue;
            if (Stargate.config.portal.destroyExplosion) {
                portal.unregister(true, true);
            } else {
                Stargate.blockPopulatorQueue.add(new BloxPopulator(new Blox(b), b.getBlock()));
                event.setCancelled(true);
            }
        }
    }

    @Listener
    public void onEntityDamage(DamageEntityEvent e, @First BlockDamageSource source) {
        Portal portal = Portal.getByEntrance(source.getLocation());
        if (portal == null) return;
        e.setCancelled(true);
    }


        /*
        @Override
        public void onSnowmanTrail(SnowmanTrailEvent event) {
            Portal p = Portal.getByEntrance(event.getBlock());
            if (p != null) event.setCancelled(true);
        }
        */

        /*
        @Override
        public void onEntityDamage(EntityDamageEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            if (!(event instanceof EntityDamageByBlockEvent)) return;
            EntityDamageByBlockEvent bEvent = (EntityDamageByBlockEvent)event;
            Player player = (Player)bEvent.getEntity();
            Block block = bEvent.getDamager();
            // Fucking null blocks, we'll do it live! This happens for lava only, as far as I know.
            // So we're "borrowing" the code from World.java used to determine if we're intersecting a lava block
            if (block == null) {
                CraftEntity ce = (CraftEntity)event.getEntity();
                net.minecraft.server.Entity entity = ce.getHandle();
                AxisAlignedBB axisalignedbb = entity.boundingBox.b(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D);
                int minx = MathHelper.floor(axisalignedbb.a);
                int maxx = MathHelper.floor(axisalignedbb.d + 1.0D);
                int miny = MathHelper.floor(axisalignedbb.b);
                int maxy = MathHelper.floor(axisalignedbb.e + 1.0D);
                int minz = MathHelper.floor(axisalignedbb.c);
                int maxz = MathHelper.floor(axisalignedbb.f + 1.0D);

                for (int x = minx; x < maxx; ++x) {
                    for (int y = miny; y < maxy; ++y) {
                        for (int z = minz; z < maxz; ++z) {
                            int blockType = player.getWorld().getBlockTypeIdAt(x, y, z);
                            if (blockType == Material.LAVA.getId() || blockType == Material.STATIONARY_LAVA.getId()) {
                                block = player.getWorld().getBlockAt(x, y, z);
                                log.info("Found block! " + block);
                                break;
                            }
                        }
                        if (block != null) break;
                    }
                    if (block != null) break;
                }
            }
            if (block == null) return;
            Portal portal = Portal.getByEntrance(block);
            if (portal == null) return;
            log.info("Found portal");
            bEvent.setDamage(0);
            bEvent.setCancelled(true);
        }

        @Override
        public void onEntityCombust(EntityCombustEvent event) {
            if (!(event.getEntity() instanceof Player)) return;
            Player player = (Player)event.getEntity();
            // WHY DOESN'T THIS CANCEL IF YOU CANCEL LAVA DAMAGE?!
            Block block = null;
            CraftEntity ce = (CraftEntity)event.getEntity();
            net.minecraft.server.Entity entity = ce.getHandle();
            AxisAlignedBB axisalignedbb = entity.boundingBox.b(-0.10000000149011612D, -0.4000000059604645D, -0.10000000149011612D);
            int minx = MathHelper.floor(axisalignedbb.a);
            int maxx = MathHelper.floor(axisalignedbb.d + 1.0D);
            int miny = MathHelper.floor(axisalignedbb.b);
            int maxy = MathHelper.floor(axisalignedbb.e + 1.0D);
            int minz = MathHelper.floor(axisalignedbb.c);
            int maxz = MathHelper.floor(axisalignedbb.f + 1.0D);

            for (int x = minx; x < maxx; ++x) {
                for (int y = miny; y < maxy; ++y) {
                    for (int z = minz; z < maxz; ++z) {
                        int blockType = player.getWorld().getBlockTypeIdAt(x, y, z);
                        if (blockType == Material.LAVA.getId() || blockType == Material.STATIONARY_LAVA.getId()) {
                            block = player.getWorld().getBlockAt(x, y, z);
                            log.info("Found block! " + block);
                            break;
                        }
                    }
                    if (block != null) break;
                }
                if (block != null) break;
            }
            if (block == null) return;
            log.info("What? " + block);
            Portal portal = Portal.getByEntrance(block);
            if (portal == null) return;
            log.info("What2?");
            event.setCancelled(true);
        }*/
}