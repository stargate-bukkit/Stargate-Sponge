package flavor.pie.stargate;

import java.math.BigDecimal;

import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;

@ConfigSerializable
public class Config {
    public static final TypeToken<Config> type = TypeToken.of(Config.class);
    @Setting
    public int version = 1;
    @Setting
    public PortalSection portal;
    //@Setting
    //public EconomySection economy;
    @Setting
    public boolean debug = false;
    @Setting
    public String lang = "en";

    @ConfigSerializable
    public static class PortalSection {
        @Setting("default-gate-network")
        public String defaultGateNetwork = "central";
        @Setting("max-gates")
        public int maxGates = 0;
        @Setting("dest-memory")
        public boolean destMemory = false;
        @Setting("ignore-entrance")
        public boolean ignoreEntrance = false;
        @Setting("sign-color")
        public TextColor signColor = TextColors.BLACK;
        @Setting("destroy-explosion")
        public boolean destroyExplosion = false;
        @Setting("sort-lists")
        public boolean sortLists = false;
    }

    /*
    @ConfigSerializable
    public static class EconomySection {
        @Setting
        public boolean enable = false;
        @Setting("create-cost")
        public BigDecimal createCost = BigDecimal.ZERO;
        @Setting("destroy-cost")
        public BigDecimal destroyCost = BigDecimal.ZERO;
        @Setting("use-cost")
        public BigDecimal useCost = BigDecimal.ZERO;
        @Setting("to-owner")
        public boolean toOwner = false;
        @Setting("free-destination")
        public boolean freeDestination = true;
        @Setting("free-gates-green")
        public boolean freeGatesGreen = true;
    }*/

    @ConfigSerializable
    public static class Old {
        public static final TypeToken<Config.Old> type = TypeToken.of(Config.Old.class);
        @Setting("portal-folder")
        public String portalFolder;
        @Setting("gate-folder")
        public String gateFolder;
        @Setting("default-gate-network")
        public String defaultGateNetwork;
        @Setting("destroyexplosion")
        public boolean destroyExplosion;
        @Setting("maxgates")
        public int maxGates;
        @Setting
        public String lang;
        @Setting
        public boolean destMemory;
        @Setting
        public boolean ignoreEntrance;
        @Setting
        public boolean handleVehicles;
        @Setting
        public boolean sortLists;
        @Setting
        public boolean protectEntrance;
        @Setting
        public TextColor signColor;
        @Setting("useiconomy")
        public boolean useIconomy;
        @Setting("createcost")
        public int createCost;
        @Setting("destroycost")
        public int destroyCost;
        @Setting("usecost")
        public int useCost;
        @Setting("toowner")
        public boolean toOwner;
        @Setting("chargefreedestination")
        public boolean chargeFreeDestination;
        @Setting("freegatesgreen")
        public boolean freeGatesGreen;
        @Setting
        public boolean debug;
        @Setting("permdebug")
        public boolean permDebug;

        public Config convert() {
            Config config = new Config();
            config.lang = lang;
            config.debug = debug;
            PortalSection pSection = new PortalSection();
            pSection.defaultGateNetwork = defaultGateNetwork;
            pSection.destMemory = destMemory;
            pSection.destroyExplosion = destroyExplosion;
            pSection.ignoreEntrance = ignoreEntrance;
            pSection.maxGates = maxGates;
            pSection.signColor = signColor;
            pSection.sortLists = sortLists;
            config.portal = pSection;
            /*EconomySection eSection = new EconomySection();
            eSection.createCost = new BigDecimal(createCost);
            eSection.destroyCost = new BigDecimal(destroyCost);
            eSection.enable = useIconomy;
            eSection.freeDestination = chargeFreeDestination;
            eSection.freeGatesGreen = freeGatesGreen;
            eSection.toOwner = toOwner;
            eSection.useCost = new BigDecimal(useCost);
            config.economy = eSection;*/
            return config;
        }
    }

    public static class BigDecimalSerializer implements TypeSerializer<BigDecimal> {
        @Override
        public void serialize(TypeToken<?> type, BigDecimal obj, ConfigurationNode value) throws ObjectMappingException {
            value.setValue(obj.toPlainString());
        }

        @Override
        public BigDecimal deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
            try {
                return new BigDecimal(value.getString());
            } catch (NumberFormatException e) {
                throw new ObjectMappingException(e);
            }
        }
    }
}