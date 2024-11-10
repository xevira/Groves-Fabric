package github.xevira.groves.sanctuary;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.item.UnlockScrollItem;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.ability.ChunkLoadAbility;
import github.xevira.groves.sanctuary.ability.RegenerationAbility;
import github.xevira.groves.sanctuary.ability.RestorationAbility;
import github.xevira.groves.sanctuary.ability.SummonDruidAbility;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Rarity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GroveAbilities {

    public static final Map<String, GroveAbility> ABILITIES = new HashMap<>();
    public static final Map<String, UnlockScrollItem> UNLOCK_SCROLLS = new HashMap<>();

    private static <T extends GroveAbility> void registerAbility(T ability)
    {
        ABILITIES.put(ability.getName(), ability);

        // Only create scrolls for abilities that are not automatically installed with imprinting
        if (!ability.isAutoInstalled()) {

            String name;
            Item.Settings settings;

            if (ability.isForbidden())
            {
                name = "forbidden_scroll_" + ability.getName();
                settings = new Item.Settings().maxCount(1).rarity(Rarity.EPIC).fireproof();
            }
            else
            {
                name = "unlock_scroll_" + ability.getName();
                settings = new Item.Settings().maxCount(1).rarity(Rarity.RARE);
            }

            UnlockScrollItem scroll = Registration.register(
                    name,
                    s -> new UnlockScrollItem(ability, s),
                    settings);

            UNLOCK_SCROLLS.put(ability.getName(), scroll);
        }
    }

    public static Optional<GroveAbility> getById(int id)
    {
        if(ABILITIES.containsKey(id))
        {
            return Optional.of(ABILITIES.get(id).getConstructor().get());
        }

        return Optional.empty();
    }


    /** Checks if the ability exists for the given name **/
    public static boolean exists(String name)
    {
        return ABILITIES.values().stream().anyMatch(ability -> ability.getName().equalsIgnoreCase(name));
    }

    public static int getIdByName(String name)
    {
        return ABILITIES.values().stream().filter(ability -> ability.getName().equalsIgnoreCase(name)).findFirst().map(ability -> ability.id).orElse(-1);

    }

    public static Optional<GroveAbility> getByName(String name)
    {
        return ABILITIES.values().stream().filter(ability -> ability.getName().equalsIgnoreCase(name)).findFirst();
    }

    public static String getNameById(int id)
    {
        if (ABILITIES.containsKey(id))
            return ABILITIES.get(id).getName();

        return null;
    }

    public static void executeKeybind(@NotNull String name, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        Optional<GroveAbility> ablityOpt = sanctuary.getAbility(name);

        if (ablityOpt.isPresent()) {
            GroveAbility ability = ablityOpt.get();
            if (ability.isAutomatic()) {
                if (ability.isActive()) {
                    ability.deactivate(sanctuary.getServer(), sanctuary, player);
                } else if (ability.isEnabled()) {
                    ability.activate(sanctuary.getServer(), sanctuary, player);
                }
            } else if (ability.isEnabled()) {
                ability.use(sanctuary.getServer(), sanctuary, player);
            }
        }
    }

    // Explicitly *starts* a grove ability
    // If it is already started or is manual, ignore
    public static void startAbility(@NotNull String name, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        Optional<GroveAbility> ablityOpt = sanctuary.getAbility(name);
        if (ablityOpt.isPresent()) {
            GroveAbility ability = ablityOpt.get();

            if (ability.isAutomatic() && ability.isEnabled() && !ability.isActive())
            {
                ability.activate(sanctuary.getServer(), sanctuary, player);
            }
        }
    }

    public static void stopAbility(@NotNull String name, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        Optional<GroveAbility> ablityOpt = sanctuary.getAbility(name);
        if (ablityOpt.isPresent()) {
            GroveAbility ability = ablityOpt.get();

            if (ability.isAutomatic() && ability.isEnabled() && ability.isActive())
            {
                ability.deactivate(sanctuary.getServer(), sanctuary, player);
            }
        }
    }

    public static void useAbility(@NotNull String name, GrovesPOI.GroveSanctuary sanctuary, PlayerEntity player)
    {
        Optional<GroveAbility> ablityOpt = sanctuary.getAbility(name);
        if (ablityOpt.isPresent()) {
            GroveAbility ability = ablityOpt.get();

            if (!ability.isAutomatic() && ability.isEnabled())
            {
                ability.use(sanctuary.getServer(), sanctuary, player);
            }
        }
    }

    public static void register()
    {
        registerAbility(new ChunkLoadAbility());
        registerAbility(new RegenerationAbility());
        registerAbility(new RestorationAbility());
        registerAbility(new SummonDruidAbility());

        // Place all generated UNLOCK scrolls into the item group after the blank unlock scroll.
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Groves.id("groves_items")))
                .register(GroveAbilities::addScrollsToItemGroup);
    }

    public static void addScrollsToItemGroup(FabricItemGroupEntries entries)
    {
        entries.addAfter(Registration.UNLOCK_SCROLL_ITEM, UNLOCK_SCROLLS.values().toArray(new UnlockScrollItem[0]));
    }

    public static void autoInstallAbilities(GrovesPOI.GroveSanctuary sanctuary)
    {
        ABILITIES.values().stream().filter(GroveAbility::isAutoInstalled).forEach(sanctuary::installAbility);
    }
}
