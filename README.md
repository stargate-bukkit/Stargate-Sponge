# Stargate-Sponge

Dinnerbone and Sturmeh originally created this plugin for hMod. It was ported to Bukkit by Drakia, and now I present to you a port for Sponge!

![](http://i.imgur.com/ovwMQGN.png) ![](http://i.imgur.com/TBgp0v0.png)  

Stargates are portals that can be created for teleportation across long distances. Create multiple gate networks, with a variety of options for functionality.  

## Features

* Stargate has only two commands, and they're for plugin info or reloading. The idea is to aid immersion by making the portal system completely commandless.  
* Stargate is lightweight. Not only do portals take up a miniscule amount of memory, but all portal updates are throttled, and may be split over multiple ticks if necessary.
* Stargate can optionally integrate with the server's economy in order to provide prices for creating, using, and breaking portals.
* Portals can be created in many different styles. You can use the traditional dialer, you can lock it to one location, or you can keep it always on. You can make it backwards, you can hide it from the dialers, you can make it private to only you. You can even hide the network name from the sign, or make the destination random!
* Portals can also be created in many different layouts. The one shipped with the plugin is the traditional 'nether portal' layout, but you can absolutely make your own! Scissors and glue not included.
* Stargate has multi-language support. It ships with some popular languages like French and Spanish, and you can easily add your own.
* Compatibility! Stargate-Sponge will import your old portal db and lang files. Although unfortunately we can't import your old .gate files, since they use numeric IDs and Sponge simply doesn't support them.

## Commands

* `/stargate reload` reloads the plugin.
* `/stargate about` talks about the plugin.

## Pages

* [Portals](https://ore.spongepowered.org/pie_flavor/Stargate/pages/Portals)
* [Gates](https://ore.spongepowered.org/pie_flavor/Stargate/pages/Gates)
* [Configuration](https://ore.spongepowered.org/pie_flavor/Stargate/pages/Configuration)
* [Permissions](https://ore.spongepowered.org/pie_flavor/Stargate/pages/Permissions)

### Disclaimer

This project is nearly a verbatim port of a Bukkit plugin which started as a nearly verbatim port of an hMod plugin, and as such is very much in **BETA**. There will be bugs. I've tried my best to stamp out most of them, but there _have_ to be a couple I don't know about. By using this plugin you acknowledge the previous three sentences. Here's two I _do_ know about. Bungee portals are magnificently borken. They generally work one way buggily, and the other way only once. I haven't removed the Bungee code, but I've removed the setting from the default config. If for some reason you want to turn it on, set `enablebungee` to true in the config. Refer to the Bukkit version's documentation on how to set up Bungee portals. Second is that water/lava portals, well, they _work_, but I can't seem to stop them from flowing everywhere. I would highly recommend you not use them for that and one other reason, which is that SpongeForge does not prevent the player from obtaining the fluid even if the removal is cancelled, and SpongeVanilla simply cannot stop them from removing the fluid in the first place.  
You have been warned.

## Other info

Please submit any and all bugs you find to the [bug tracker](https://github.com/pie-flavor/Stargate-Sponge/issues).  
This plugin uses bStats, which collects data about your server. This data is in no way intrusive, is completely anonymized, and has a negligible impact on server performance, but if you wish to opt out for whatever reason, simply set `enabled` to `false` in `bStats/config.conf`.

## Changelog

0.1 - Ported verbatim.
0.1.1 - Fixed some bugs I missed.