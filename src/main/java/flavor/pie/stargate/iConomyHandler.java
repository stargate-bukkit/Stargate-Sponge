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
/*
package flavor.pie.stargate;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.UniqueAccount;
//import org.spongepowered.api.service.economy.transaction.ResultType;
//import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;

import java.math.BigDecimal;
import java.util.UUID;

public class iConomyHandler {
    
    public static EconomyService economy() {
        return Sponge.getServiceManager().provide(EconomyService.class).orElse(null);
    }

    public static UniqueAccount getAccount(String player) {
        return economy().getOrCreateAccount(Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(player).get().getUniqueId()).orElse(null);
    }
    
    public static BigDecimal getBalance(String player) {
    	return BigDecimal.ZERO;	//This seems to be happening when economy is disabled
        /*EconomyService economy = economy();
        if (!Stargate.config.economy.enable) return BigDecimal.ZERO;
        if (economy != null) {
            return economy.getOrCreateAccount(Sponge.getServer().getPlayer(player).get().getUniqueId()).get().getBalance(economy.getDefaultCurrency());
        }
        return BigDecimal.ZERO;*//*
    }
    
    public static boolean chargePlayer(String player, String target, BigDecimal amount) {
    	return true;	//This seems to be happening when economy is disabled
        /*EconomyService economy = economy();
        if (!Stargate.config.economy.enable) return true;
        if (economy != null) {
            UniqueAccount acct = getAccount(player);
            if (player.equals(target)) return true;
            TransactionResult res;
            if (target == null) {
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    res = acct.withdraw(economy.getDefaultCurrency(), amount, Sponge.getCauseStackManager().getCurrentCause());
                } else {
                    res = acct.deposit(economy.getDefaultCurrency(), amount.negate(), Sponge.getCauseStackManager().getCurrentCause());
                }
            } else {
                if (amount.compareTo(BigDecimal.ZERO) > 0) {
                    res = acct.transfer(getAccount(target), economy.getDefaultCurrency(), amount,
                            Sponge.getCauseStackManager().getCurrentCause());
                } else {
                    res = getAccount(target).transfer(acct, economy.getDefaultCurrency(), amount.negate(),
                            Sponge.getCauseStackManager().getCurrentCause());
                }
            }
            return res.getResult().equals(ResultType.SUCCESS);
        }
        return true;*//*
    }

    public static boolean chargePlayer(String player, UUID target, BigDecimal amount) {
        if (target == null) return chargePlayer(player, (String) null, amount);
        return chargePlayer(player, Sponge.getServiceManager().provideUnchecked(UserStorageService.class).get(target).get().getName(), amount);
    }
    
    public static boolean useiConomy() {
    	return false;	//This seems to be happening when economy is disabled
        /*if (!Stargate.config.economy.enable) return false;
        if (economy() != null) return true;
        return false;*//*
    }
    
    public static Text format(BigDecimal amt) {
        EconomyService economy = economy();
        if (economy != null) {
            return economy.getDefaultCurrency().format(amt);
        }
        return Text.EMPTY;
    }
    
    public static boolean setupeConomy() {
        return Sponge.getServiceManager().isRegistered(EconomyService.class);
    }
}
*/