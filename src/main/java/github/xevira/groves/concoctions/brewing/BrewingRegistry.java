package github.xevira.groves.concoctions.brewing;

import github.xevira.groves.Groves;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BrewingRegistry {
    private static final Map<StatusEffect, StatusEffectEntry> EFFECT_REGISTRY = new HashMap<>();

    public static boolean INJECTIONS_ENABLED = true;

    public static void onServerStarted(MinecraftServer server)
    {
        Groves.LOGGER.info("BrewingRegistry.onServerStarted");
        INJECTIONS_ENABLED = false;

        // Process all of the information
        EFFECT_REGISTRY.values().forEach(StatusEffectEntry::process);
    }

    public static boolean isEmpty(RegistryEntry<Potion> entry)
    {
        Potion potion = entry.value();

        return potion.getEffects().isEmpty();
    }

    public static boolean isSimple(RegistryEntry<Potion> entry)
    {
        Potion potion = entry.value();

        return potion.getEffects().size() == 1;
    }

    public static void registerItemRecipe(Item input, Item ingredient, Item output)
    {

    }

    private static void processStatusEffect(StatusEffectInstance effect)
    {
        Groves.LOGGER.info("status effect: [{}], {}, {}", effect.getEffectType().value().getTranslationKey(), effect.getAmplifier(), effect.getDuration());

        StatusEffect type = effect.getEffectType().value();

        StatusEffectEntry entry;
        if (EFFECT_REGISTRY.containsKey(type)) {
            entry = EFFECT_REGISTRY.get(type);
        }
        else {
            entry = new StatusEffectEntry(type);
            EFFECT_REGISTRY.put(type, entry);
        }

        if (effect.getAmplifier() > 0) { // Amplified
            entry.addAmplifier(effect.getAmplifier());
        } else      // Base
            entry.addDuration(effect.getDuration());

    }

    /** Captures the status effects for potions to determine their processing values.  For example, Slowness does not just do Slowness I and II.  It skips right to IV.  Need to account for that. **/
    public static void registerPotionRecipe(RegistryEntry<Potion> input, Item ingredient, RegistryEntry<Potion> output)
    {
        output.value().getEffects().forEach(BrewingRegistry::processStatusEffect);
    }

    private static class StatusEffectEntry
    {
        private final StatusEffect effect;
        public List<Integer> durations;
        public List<Integer> amplifiers;     // Configured set of allowed amplifiers.
        public int afterAmplify;            // How much should it amplify by after going beyond the provided amplifications

        StatusEffectEntry(StatusEffect effect)
        {
            this.effect = effect;
            this.durations = new ArrayList<>();
            this.amplifiers = new ArrayList<>();
            this.afterAmplify = 1;
        }

        public boolean canExtend() { return this.durations.size() > 1; }
        
        public void addDuration(int duration)
        {
            // Already in the list
            if (this.durations.contains(duration))
                return;

            // Insert into the list in numerical order
            for(int i = 0; i < this.durations.size(); i++)
            {
                if (duration < this.durations.get(i))
                {
                    this.durations.add(i, duration);
                    return;
                }
            }

            this.durations.add(duration);
        }

        public boolean canAmplify() { return !this.amplifiers.isEmpty(); }

        public void addAmplifier(int amplifier)
        {
            // Already in the list
            if (this.amplifiers.contains(amplifier))
                return;

            // Insert into the list in numerical order
            for(int i = 0; i < this.amplifiers.size(); i++)
            {
                if (amplifier < this.amplifiers.get(i))
                {
                    this.amplifiers.add(i, amplifier);
                    return;
                }
            }

            this.amplifiers.add(amplifier);
        }

        public void process()
        {
            // Check the last two amplifiers in the set to determine the difference.
            // If there is only one amplifier in the mix, use that amplifier's value
            if(!this.amplifiers.isEmpty())
            {
                this.afterAmplify = this.amplifiers.getLast();
                if (this.amplifiers.size() > 1)
                    this.afterAmplify -= this.amplifiers.get(this.amplifiers.size() - 2);
            }

            if (this.canAmplify())
                Groves.LOGGER.info("[{}]: {}, {}, {}, {}, {}", this.effect.getTranslationKey(), this.canExtend(), this.canAmplify(), this.durations, this.amplifiers, this.afterAmplify);
            else
                Groves.LOGGER.info("[{}]: {}, {}, {}", this.effect.getTranslationKey(), this.canExtend(), this.canAmplify(), this.durations);
        }

        public int getNewAmplifier(int amplifier)
        {
            if (!canAmplify()) return 0;

            if (amplifier > 0) {
                // Already at the end of the list of amplifiers, use the after amplification
                if (amplifier < this.amplifiers.getLast())
                {
                    // Find the largest amplifier that is less than or equal to the amplifier
                    for (int i = this.amplifiers.size() - 1; i-- > 0; ) {
                        if (this.amplifiers.get(i) <= amplifier) {
                            return this.amplifiers.get(i + 1);
                        }
                    }
                }

                return amplifier + this.afterAmplify;
            }
            else
            {
                return this.amplifiers.getFirst();
            }
        }
    }
}
