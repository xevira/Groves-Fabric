package github.xevira.groves.sanctuary;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.item.UnlockScrollItem;
import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.ability.ChunkLoadAbility;
import github.xevira.groves.sanctuary.ability.RegenerationAbility;
import github.xevira.groves.sanctuary.ability.RestorationAbility;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
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

        UnlockScrollItem scroll = Registration.register(
                "unlock_scroll_" + ability.getName(),
                settings -> new UnlockScrollItem(ability, settings),
                new Item.Settings().maxCount(1));

        UNLOCK_SCROLLS.put(ability.getName(), scroll);
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
                    // Turn off
                    ability.setActive(false);
                    ability.onDeactivate(sanctuary.getServer(), sanctuary, player);
                } else if (ability.isEnabled()) {
                    // Check if it can be turned on
                    if (ability.canActivate(sanctuary.getServer(), sanctuary, player)) {
                        ability.setActive(true);
                        ability.onActivate(sanctuary.getServer(), sanctuary, player);
                    } else
                        ability.sendFailure(sanctuary.getServer(), sanctuary, player);
                }
            } else if (ability.isEnabled()) {
                if (ability.canUse(sanctuary.getServer(), sanctuary, player))
                    ability.onUse(sanctuary.getServer(), sanctuary, player);
                else
                    ability.sendFailure(sanctuary.getServer(), sanctuary, player);
            }
        }
    }

    public static void register()
    {
        registerAbility(new ChunkLoadAbility());
        registerAbility(new RegenerationAbility());
        registerAbility(new RestorationAbility());

        // Place all generated UNLOCK scrolls into the item group after the blank unlock scroll.
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(RegistryKeys.ITEM_GROUP, Groves.id("groves_items")))
                .register(GroveAbilities::addScrollsToItemGroup);
    }

    public static void addScrollsToItemGroup(FabricItemGroupEntries entries)
    {
        entries.addAfter(Registration.UNLOCK_SCROLL_ITEM, UNLOCK_SCROLLS.values().toArray(new UnlockScrollItem[0]));
    }
}
