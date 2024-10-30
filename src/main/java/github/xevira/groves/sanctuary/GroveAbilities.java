package github.xevira.groves.sanctuary;

import github.xevira.groves.poi.GrovesPOI;
import github.xevira.groves.sanctuary.ability.ChunkLoadAbility;
import github.xevira.groves.sanctuary.ability.RegenerationAbility;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class GroveAbilities {
    // ID LIST
    // 1 - ChunkLoad
    // 2 - Regeneration
    // 3 - MobSpawnProtection
    // 4 - EndermanTeleportProtection
    // 5 - MobGriefingProtection
    // 6 - BlockInteractionProtection
    // 7 - Respawn
    // 8 - Teleport
    // 9 - Defender

    public static final Map<Integer, GroveAbility> ABILITIES = new HashMap<>();

    static {
        registerAbility(new ChunkLoadAbility());
        registerAbility(new RegenerationAbility());
    }

    private static <T extends GroveAbility> void registerAbility(T ability)
    {
        ABILITIES.put(ability.getId(), ability);
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
}
