package github.xevira.groves.data.provider;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.block.entity.MoonwellMultiblockMasterBlockEntity;
import github.xevira.groves.concoctions.potion.effects.*;
import github.xevira.groves.item.UnlockScrollItem;
import github.xevira.groves.sanctuary.*;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.block.Block;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.fluid.Fluid;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.potion.Potion;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModEnglishLanguageProvider extends FabricLanguageProvider {
    private static final List<String> RANK_SUFFIX = List.of(
            " I",
            " II",
            " III",
            " IV",
            " V",
            " VI",
            " VII",
            " VIII",
            " IX",
            " X"
    );

    public ModEnglishLanguageProvider(FabricDataOutput dataOutput, CompletableFuture<RegistryWrapper.WrapperLookup> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    private static void addText(@NotNull TranslationBuilder builder, @NotNull Text text, String value) {
        if (value == null) return;

        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            builder.add(translatableTextContent.getKey(), value);
        } else {
            Groves.LOGGER.warn("Failed to add translation for text: {}", text.getString());
        }
    }

    private static void addText(@NotNull TranslationBuilder builder, @NotNull String path, String value) {
        if (value == null) return;

        Text text = Text.translatable(path);
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            builder.add(translatableTextContent.getKey(), value);
        } else {
            Groves.LOGGER.warn("Failed to add translation for text: {}", text.getString());
        }
    }

    private static void addText(@NotNull TranslationBuilder builder, @NotNull Item item, @NotNull String path, String value) {
        if (value == null) return;

        Text text = Text.translatable(item.getTranslationKey() + path);
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            builder.add(translatableTextContent.getKey(), value);
        } else {
            Groves.LOGGER.warn("Failed to add translation for text: {}", text.getString());
        }
    }

    private static void addText(@NotNull TranslationBuilder builder, @NotNull String prefix, @NotNull String path, String value) {
        if (value == null) return;

        Text text = Text.translatable(prefix + "." + Groves.MOD_ID + "." + path);
        if (text.getContent() instanceof TranslatableTextContent translatableTextContent) {
            builder.add(translatableTextContent.getKey(), value);
        } else {
            Groves.LOGGER.warn("Failed to add translation for text: {}", text.getString());
        }
    }

    private static void addStatusEffect(@NotNull TranslationBuilder builder, @NotNull RegistryEntry<StatusEffect> effect, @NotNull String value) {
        addText(builder, effect.value().getTranslationKey(), value);
    }

    private static void addPotion(@NotNull TranslationBuilder builder, @NotNull RegistryEntry<Potion> potion, @NotNull String value) {
        addText(builder, Items.POTION, ".effect." + potion.value().getBaseName(), "Potion of " + value);
        addText(builder, Items.SPLASH_POTION, ".effect." + potion.value().getBaseName(), "Splash Potion of " + value);
        addText(builder, Items.LINGERING_POTION, ".effect." + potion.value().getBaseName(), "Lingering Potion of " + value);
        addText(builder, Items.TIPPED_ARROW, ".effect." + potion.value().getBaseName(), "Arrow of " + value);
    }

    private static void addPotion(@NotNull TranslationBuilder builder, @NotNull RegistryEntry<Potion> potion, @NotNull String base, @NotNull String splash, @NotNull String lingering, @NotNull String arrow) {

        addText(builder, Items.POTION, ".effect." + potion.value().getBaseName(), base);
        addText(builder, Items.SPLASH_POTION, ".effect." + potion.value().getBaseName(), splash);
        addText(builder, Items.LINGERING_POTION, ".effect." + potion.value().getBaseName(), lingering);
        addText(builder, Items.TIPPED_ARROW, ".effect." + potion.value().getBaseName(), arrow);
    }

    private static void addDeathMessages(@NotNull TranslationBuilder builder, @NotNull String type, @NotNull String value, @NotNull String playerValue)
    {
        addText(builder, "death.attack."  + type, value);
        addText(builder, "death.attack."  + type + ".player", playerValue);
    }

    @Override
    public void generateTranslations(RegistryWrapper.WrapperLookup wrapperLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add(Registration.AQUAMARINE_ORE_BLOCK, "Aquamarine Ore");
        translationBuilder.add(Registration.DEEPSLATE_AQUAMARINE_ORE_BLOCK, "Deepslate Aquamarine Ore");
        translationBuilder.add(Registration.AQUAMARINE_BLOCK_BLOCK, "Block of Aquamarine");

        translationBuilder.add(Registration.CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, "Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK, "Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK, "Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK, "Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK, "Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK, "Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK, "Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK, "Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.CRACKED_MOONSTONE_BRICKS_BLOCK, "Cracked Moonstone Bricks");
        translationBuilder.add(Registration.MOONSTONE_BRICKS_BLOCK, "Moonstone Bricks");
        translationBuilder.add(Registration.MOONSTONE_BRICK_SLAB_BLOCK, "Moonstone Brick Slab");
        translationBuilder.add(Registration.MOONSTONE_BRICK_STAIRS_BLOCK, "Moonstone Brick Stairs");
        translationBuilder.add(Registration.MOONSTONE_BRICK_WALL_BLOCK, "Moonstone Brick Wall");

        translationBuilder.add(Registration.CHISELED_MOONWELL_BRICKS_FULL_MOON_BLOCK, "Chiseled Moonwell Bricks");
        translationBuilder.add(Registration.CHISELED_MOONWELL_BRICKS_WANING_GIBBOUS_BLOCK, "Chiseled Moonwell Bricks");
        translationBuilder.add(Registration.CHISELED_MOONWELL_BRICKS_THIRD_QUARTER_BLOCK, "Chiseled Moonwell Bricks");
        translationBuilder.add(Registration.CHISELED_MOONWELL_BRICKS_WANING_CRESCENT_BLOCK, "Chiseled Moonwell Bricks");
        translationBuilder.add(Registration.CHISELED_MOONWELL_BRICKS_NEW_MOON_BLOCK, "Chiseled Moonwell Bricks");
        translationBuilder.add(Registration.CHISELED_MOONWELL_BRICKS_WAXING_CRESCENT_BLOCK, "Chiseled Moonwell Bricks");
        translationBuilder.add(Registration.CHISELED_MOONWELL_BRICKS_FIRST_QUARTER_BLOCK, "Chiseled Moonwell Bricks");
        translationBuilder.add(Registration.CHISELED_MOONWELL_BRICKS_WAXING_GIBBOUS_BLOCK, "Chiseled Moonwell Bricks");
        translationBuilder.add(Registration.CRACKED_MOONWELL_BRICKS_BLOCK, "Cracked Moonwell Bricks");
        translationBuilder.add(Registration.MOONWELL_BASIN_BLOCK, "Moonwell Basin");
        translationBuilder.add(Registration.MOONWELL_BRICKS_BLOCK, "Moonwell Bricks");
        translationBuilder.add(Registration.MOONWELL_BRICK_SLAB_BLOCK, "Moonwell Brick Slab");
        translationBuilder.add(Registration.MOONWELL_BRICK_STAIRS_BLOCK, "Moonwell Brick Stairs");
        translationBuilder.add(Registration.MOONWELL_BRICK_WALL_BLOCK, "Moonwell Brick Wall");

        translationBuilder.add(Registration.SANCTUM_LOG_BLOCK, "Sanctum Log");
        translationBuilder.add(Registration.SANCTUM_CORE_LOG_BLOCK, "Sanctum Core Log");
        translationBuilder.add(Registration.SANCTUM_WOOD_BLOCK, "Sanctum Wood");
        translationBuilder.add(Registration.STRIPPED_SANCTUM_LOG_BLOCK, "Stripped Sanctum Log");
        translationBuilder.add(Registration.STRIPPED_SANCTUM_WOOD_BLOCK, "Stripped Sanctum Wood");
        translationBuilder.add(Registration.SANCTUM_LEAVES_BLOCK, "Sanctum Leaves");
        translationBuilder.add(Registration.SANCTUM_SAPLING_BLOCK, "Sanctum Sapling");

        translationBuilder.add(Registration.SANCTUM_PLANKS_BLOCK, "Sanctum Planks");
        translationBuilder.add(Registration.SANCTUM_SLAB_BLOCK, "Sanctum Slab");
        translationBuilder.add(Registration.SANCTUM_STAIRS_BLOCK, "Sanctum Stairs");
        translationBuilder.add(Registration.SANCTUM_BUTTON_BLOCK, "Sanctum Button");
        translationBuilder.add(Registration.SANCTUM_DOOR_BLOCK, "Sanctum Door");
        translationBuilder.add(Registration.SANCTUM_TRAPDOOR_BLOCK, "Sanctum Trapdoor");
        translationBuilder.add(Registration.SANCTUM_PRESSURE_PLATE_BLOCK, "Sanctum Pressure Plate");
        translationBuilder.add(Registration.SANCTUM_FENCE_BLOCK, "Sanctum Fence");
        translationBuilder.add(Registration.SANCTUM_FENCE_GATE_BLOCK, "Sanctum Fence Gate");
        translationBuilder.add(Registration.SANCTUM_SIGN_BLOCK, "Sanctum Sign");
        translationBuilder.add(Registration.SANCTUM_HANGING_SIGN_BLOCK, "Sanctum Hanging Sign");

        translationBuilder.add(Registration.SANCTUM_BOAT_ITEM, "Sanctum Boat");
        translationBuilder.add(Registration.SANCTUM_CHEST_BOAT_ITEM, "Sanctum Boat with Chest");

        translationBuilder.add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FULL_MOON_BLOCK, "Waxed Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_GIBBOUS_BLOCK, "Waxed Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_THIRD_QUARTER_BLOCK, "Waxed Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WANING_CRESCENT_BLOCK, "Waxed Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_NEW_MOON_BLOCK, "Waxed Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_CRESCENT_BLOCK, "Waxed Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_FIRST_QUARTER_BLOCK, "Waxed Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_CHISELED_MOONSTONE_BRICKS_WAXING_GIBBOUS_BLOCK, "Waxed Chiseled Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_CRACKED_MOONSTONE_BRICKS_BLOCK, "Waxed Cracked Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_MOONSTONE_BRICKS_BLOCK, "Waxed Moonstone Bricks");
        translationBuilder.add(Registration.WAXED_MOONSTONE_BRICK_SLAB_BLOCK, "Waxed Moonstone Brick Slab");
        translationBuilder.add(Registration.WAXED_MOONSTONE_BRICK_STAIRS_BLOCK, "Waxed Moonstone Brick Stairs");
        translationBuilder.add(Registration.WAXED_MOONSTONE_BRICK_WALL_BLOCK, "Waxed Moonstone Brick Wall");

        translationBuilder.add(Registration.BLESSED_MOON_WATER_BLOCK, "Blessed Moon Water");
        translationBuilder.add(Registration.MOONLIGHT_BLOCK, "Moonlight");

        translationBuilder.add(Registration.WIND_CHIME_BLOCK, "Wind Chime");
        translationBuilder.add(Registration.WORN_WIND_CHIME_BLOCK, "Worn Wind Chime");
        translationBuilder.add(Registration.DAMAGED_WIND_CHIME_BLOCK, "Damaged Wind Chime");

        translationBuilder.add(Registration.AQUAMARINE_ITEM, "Aquamarine");
        translationBuilder.add(Registration.AQUAMARINE_DUST_ITEM, "Aquamarine Dust");
        translationBuilder.add(Registration.BLESSED_MOON_WATER_BUCKET_ITEM, "Bucket of Blessed Moon Water");
        translationBuilder.add(Registration.IMPRINTING_SIGIL_ITEM, "Imprinting Sigil");
        translationBuilder.add(Registration.ENCHANTED_IMPRINTING_SIGIL_ITEM, "Enchanted Imprinting Sigil");
        translationBuilder.add(Registration.IRONWOOD_SHARD_ITEM, "Ironwood Shard");
        translationBuilder.add(Registration.MOONLIGHT_BUCKET_ITEM, "Bucket of Moonlight");
        translationBuilder.add(Registration.MOON_PHIAL_ITEM, "Phial of the Moon");
        translationBuilder.add(Registration.INTO_THE_HEART_OF_THE_UNIVERSE_MUSIC_DISC_ITEM, "Music Disc");
        addText(translationBuilder, Registration.INTO_THE_HEART_OF_THE_UNIVERSE_MUSIC_DISC_ITEM, ".desc", "Druid Music - Into the Heart of the Universe");

        translationBuilder.add(Registration.ENDER_HEART_ITEM, "Ender Heart");
        translationBuilder.add(Registration.GHAST_HEART_ITEM, "Ghast Heart");
        translationBuilder.add(Registration.SHULKER_BULLET_ITEM, "Shulker Bullet");
        translationBuilder.add(Registration.SPIDER_LEG_ITEM, "Spider Leg");
        translationBuilder.add(Registration.EAGLE_FEATHER_ITEM, "Eagle Feather");
        translationBuilder.add(Registration.DOLPHIN_FIN_ITEM, "Dolphin Fin");
        addText(translationBuilder, Registration.DOLPHIN_FIN_ITEM, ".lore", "You monster...");
        translationBuilder.add(Registration.BEE_STINGER_ITEM, "Bee Stinger");

        translationBuilder.add(Registration.SANCTUM_SWORD_ITEM, "Sanctum Sword");
        translationBuilder.add(Registration.SANCTUM_PICKAXE_ITEM, "Sanctum Pickaxe");
        translationBuilder.add(Registration.SANCTUM_AXE_ITEM, "Sanctum Axe");
        translationBuilder.add(Registration.SANCTUM_SHOVEL_ITEM, "Sanctum Shovel");
        translationBuilder.add(Registration.SANCTUM_HOE_ITEM, "Sanctum Hoe");

        translationBuilder.add(Registration.FORBIDDEN_SCROLL_ITEM, "Forbidden Scroll");
        translationBuilder.add(Registration.UNLOCK_SCROLL_ITEM, "Blank Unlock Scroll");
        addText(translationBuilder, Registration.UNLOCK_SCROLL_ITEM, ".lore", "Craft with the required ingredients to make Grove Sanctuary unlock scrolls.");

        addText(translationBuilder, "entity", "druid", "Druid");
        addText(translationBuilder, "entity", "sanctum_chest_boat", "Sanctum Boat with Chest");

        addText(translationBuilder, "tooltip", "hold.shift", "Hold Shift for more information.");

        addText(translationBuilder, "tooltip", "ability.cost.start", "Start Cost:");
        addText(translationBuilder, "tooltip", "ability.cost.tick", "Maintenance Cost:");
        addText(translationBuilder, "tooltip", "ability.cost.use", "Use Cost:");

        for(GroveAbility ability : GroveAbilities.ABILITIES.values())
        {
            addText(translationBuilder, "name", "ability." + ability.getName(), ability.getEnglishTranslation());
            for(int i = 1; i <= ability.getMaxRank(); i++)
                addText(translationBuilder, "lore", "ability." + ability.getName() + "." + i, ability.getEnglishLoreTranslation(i));

            addText(translationBuilder, "tooltip", "ability." + ability.getName() + ".cost.start", ability.getEnglishStartCostTranslation());
            addText(translationBuilder, "tooltip", "ability." + ability.getName() + ".cost.tick", ability.getEnglishTickCostTranslation());
            addText(translationBuilder, "tooltip", "ability." + ability.getName() + ".cost.use", ability.getEnglishUseCostTranslation());

            List<UnlockScrollItem> scrolls = GroveAbilities.UNLOCK_SCROLLS.get(ability.getName());
            if (scrolls != null && !scrolls.isEmpty())
            {
                for(int i = 0; i < scrolls.size(); i++)
                {
                    int rank = i + 1;
                    UnlockScrollItem scroll = scrolls.get(i);
                    String suffix = RANK_SUFFIX.get(i);

                    if (ability.getMaxRank() > 1)
                    {
                        if (ability.isForbidden())
                            translationBuilder.add(scroll, "Forbidden Scroll (" + ability.getEnglishTranslation() + suffix + ")");
                        else
                            translationBuilder.add(scroll, "Unlock Scroll (" + ability.getEnglishTranslation() + suffix + ")");
                    }
                    else {
                        if (ability.isForbidden())
                            translationBuilder.add(scroll, "Forbidden Scroll (" + ability.getEnglishTranslation() + ")");
                        else
                            translationBuilder.add(scroll, "Unlock Scroll (" + ability.getEnglishTranslation() + ")");
                    }

                    if (ability.hasUnlockRequirement(rank) && ability.getEnglishUnlockTranslation(rank) != null)
                        addText(translationBuilder, "tooltip", "ability." + ability.getName() + ".unlock." + rank, ability.getEnglishUnlockTranslation(rank));

                }
            }
        }

        for(GroveUnlock unlock : GroveUnlocks.UNLOCK_MAP.values())
        {
            addText(translationBuilder, "toast", unlock.getName() + ".title", unlock.getEnglishToastTitle());
            addText(translationBuilder, "toast", unlock.getName() + ".text", unlock.getEnglishToastText());
        }

        translationBuilder.addEnchantment(Registration.LIGHT_FOOTED_ENCHANTMENT_KEY, "Light Footed");
        translationBuilder.addEnchantment(Registration.SOLAR_REPAIR_ENCHANTMENT_KEY, "Solar Repair");
        translationBuilder.addEnchantment(Registration.THUNDERING_ENCHANTMENT_KEY, "Thundering");

        translationBuilder.add(Registration.MOONSTONE_BLOCKS, "Moonstone Blocks");
        translationBuilder.add(Registration.MOONWELL_BLOCKS, "Moonwell Blocks");
        translationBuilder.add(Registration.MOONWELL_CONSTRUCTION_BLOCKS, "Moonwell Construction Blocks");
        translationBuilder.add(Registration.MOONWELL_INTERACTION_BLOCKS, "Moonwell Interaction Blocks");
        translationBuilder.add(Registration.SANCTUM_LOG_BLOCKS, "Sanctum Logs");
        translationBuilder.add(Registration.SANCTUM_PLANKS_ITEMS, "Sanctum Planks");
        translationBuilder.add(Registration.SANCTUM_LOG_ITEMS, "Sanctum Logs");
        translationBuilder.add(Registration.BLESSED_MOON_WATERS_TAG, "Blessed Moon Waters");
        translationBuilder.add(Registration.MOONLIGHT_TAG, "Moonlight");

        addText(translationBuilder, MoonwellMultiblockMasterBlockEntity.TITLE, "Moonwell");
        addText(translationBuilder, GroveSanctuary.TITLE, "Grove Sanctuary");

        addText(translationBuilder, "itemgroup", "groves_items", "Groves");

        addText(translationBuilder, "lore", "lunar_phase.full_moon", "Full Moon");
        addText(translationBuilder, "lore", "lunar_phase.full_moon_day", "Full Moon to Waning Gibbous");
        addText(translationBuilder, "lore", "lunar_phase.waning_gibbous", "Waning Gibbous");
        addText(translationBuilder, "lore", "lunar_phase.waning_gibbous_day", "Waning Gibbous to Third Quarter");
        addText(translationBuilder, "lore", "lunar_phase.third_quarter", "Third Quarter");
        addText(translationBuilder, "lore", "lunar_phase.third_quarter_day", "Third Quarter to Waning Crescent");
        addText(translationBuilder, "lore", "lunar_phase.waning_crescent", "Waning Crescent");
        addText(translationBuilder, "lore", "lunar_phase.waning_crescent_day", "Waning Crescent to New Moon");
        addText(translationBuilder, "lore", "lunar_phase.new_moon", "New Moon");
        addText(translationBuilder, "lore", "lunar_phase.new_moon_day", "New Moon to Waxing Crescent");
        addText(translationBuilder, "lore", "lunar_phase.waxing_crescent", "Waxing Crescent");
        addText(translationBuilder, "lore", "lunar_phase.waxing_crescent_day", "Waxing Crescent to First Quarter");
        addText(translationBuilder, "lore", "lunar_phase.first_quarter", "First Quarter");
        addText(translationBuilder, "lore", "lunar_phase.first_quarter_day", "First Quarter to Waxing Gibbous");
        addText(translationBuilder, "lore", "lunar_phase.waxing_gibbous", "Waxing Gibbous");
        addText(translationBuilder, "lore", "lunar_phase.waxing_gibbous_day", "Waxing Gibbous to Full Moon");

        addText(translationBuilder, "sound", "druid.appeared", "Druid appears");
        addText(translationBuilder, "sound", "druid.ambient", "Druid mumbles");
        addText(translationBuilder, "sound", "druid.death", "Druid dies");
        addText(translationBuilder, "sound", "druid.disappeared", "Druid disappears");
        addText(translationBuilder, "sound", "druid.drink_milk", "Druid drinks milk");
        addText(translationBuilder, "sound", "druid.drink_potion", "Druid drinks potion");
        addText(translationBuilder, "sound", "druid.hurt", "Druid hurts");
        addText(translationBuilder, "sound", "druid.no", "Druid disagrees");
        addText(translationBuilder, "sound", "druid.reappeared", "Druid appears");
        addText(translationBuilder, "sound", "druid.trade", "Druid trades");
        addText(translationBuilder, "sound", "druid.yes", "Druid agrees");

        addText(translationBuilder, "sound", "mob_effect.bouncy.bounce", "Someone bounces.");

        addText(translationBuilder, "sound", "moonwell.activate", "Moonwell activates");
        addText(translationBuilder, "sound", "moonwell.deactivate", "Moonwell deactivates");
        addText(translationBuilder, "sound", "mace.thundering", "Mace thunders");

        addText(translationBuilder, "sound", "wind_chime.collide", "Someone collides with a wind chime");
        addText(translationBuilder, "sound", "wind_chime.protect", "Wind chime chimes");
        addText(translationBuilder, "sound", "wind_chime.step", "Wind chime chimes");
        addText(translationBuilder, "sound", "wind_chime.place", "Wind chime placed");
        addText(translationBuilder, "sound", "wind_chime.break", "Wind chime broken");
        addText(translationBuilder, "sound", "wind_chime.hit", "Wind chime hit");
        addText(translationBuilder, "sound", "wind_chime.fall", "Wind chime fell");

        addText(translationBuilder, "text", "imprinting.already_exists", "§cThere is already a Grove Sanctuary here.§r");
        addText(translationBuilder, "text", "imprinting.has_grove", "§cYou already possess a Grove Sanctuary.§r");
        addText(translationBuilder, "text", "moon_phial.world_no_time", "§cWorld has no time.§r");
        addText(translationBuilder, "text", "moon_phial.world_daytime", "§cMust be at night.§c");
        addText(translationBuilder, "text", "moon_phial.wrong_phase", "§cThe moon is in the wrong phase.  Try a brighter phase.§r");
        addText(translationBuilder, "text", "moon_phial.grove.not_owner", "§cThis Grove Sanctuary belongs to someone else.§r");
        addText(translationBuilder, "text", "moon_phial.not_in_grove", "§cMust be in a Grove Sanctuary to form a Moonwell.§r");
        addText(translationBuilder, "text", "moon_phial.moonwell_exists", "§cA Moonwell already exists for this Grove Sanctuary.§r");
        addText(translationBuilder, "text", "moonwell.invalid_block", "§cInvalid block at (%s, %s, %s).§r");

        addText(translationBuilder, "label", "groves.name", "Name:");
        addText(translationBuilder, "button", "groves.name.set", "Set");
        addText(translationBuilder, "label", "groves.foliage", "Foliage:");
        addText(translationBuilder, "text", "groves.foliage", "%s leaf block%s");
        addText(translationBuilder, "label", "groves.sunlight", "Sunlight:");
        addText(translationBuilder, "label", "groves.darkness", "Darkness:");
        addText(translationBuilder, "tooltip", "groves.sunlight", "Total Sunlight: %s (%s%%)");
        addText(translationBuilder, "tooltip", "groves.sunlight.collected", "Total Sunlight Collected: %s");
        addText(translationBuilder, "tooltip", "groves.darkness", "Total Darkness: %s (%s%%)");
        addText(translationBuilder, "tooltip", "groves.darkness.collected", "Total Darkness Collected: %s");
        addText(translationBuilder, "label", "groves.moonwell", "Moonwell:");
        addText(translationBuilder, "text", "groves.moonwell", "(%s, %s, %s)");
        addText(translationBuilder, "text", "groves.no_moonwell", "None");
        addText(translationBuilder, "label", "groves.spawn", "Spawn Point:");
        addText(translationBuilder, "text", "groves.spawn", "(%s, %s, %s)");
        addText(translationBuilder, "button", "groves.spawn.set", "Set to Current Location");

        addText(translationBuilder, "tooltip", "groves.tab.general", "General");
        addText(translationBuilder, "tooltip", "groves.tab.chunks", "Chunks");
        addText(translationBuilder, "tooltip", "groves.tab.friends", "Friends");
        addText(translationBuilder, "tooltip", "groves.tab.abilities", "Abilities");
        addText(translationBuilder, "tooltip", "groves.tab.keybinds", "Keybinds");

        addText(translationBuilder, "label", "groves.tab.general", "General");
        addText(translationBuilder, "label", "groves.tab.chunks", "Chunks");
        addText(translationBuilder, "label", "groves.tab.friends", "Friends");
        addText(translationBuilder, "label", "groves.tab.abilities", "Abilities");
        addText(translationBuilder, "label", "groves.tab.keybinds", "Keybinds");

        addText(translationBuilder,"tooltip", "groves.chunks.keep_loaded.on", "Click to disable chunk loading for chunk X = %s, Z = %s.");
        addText(translationBuilder,"tooltip", "groves.chunks.keep_loaded.off", "Click to enable chunk loading for chunk X = %s, Z = %s.");

        addText(translationBuilder, "key.category", "groves", "Groves");
        addText(translationBuilder, "key", "open_groves_ui", "Open Groves UI");
        addText(translationBuilder, "key", "groves_ability_chunk_load", "Toggle Grove Chunk Loading");
        addText(translationBuilder, "key", "groves_ability_regeneration", "Toggle Grove Regeneration");

        addText(translationBuilder, "text", "ability.on_cooldown.suffix", " is on cooldown.");
        addText(translationBuilder, "text", "ability.not_enough_sunlight.activate", "Not enough sunlight.  Need %s to activate.");
        addText(translationBuilder, "text", "ability.not_enough_sunlight.use", "Not enough sunlight.  Need %s to use.");
        addText(translationBuilder, "text", "ability.empty_hand", "Nothing in your main hand to restore.");
        addText(translationBuilder, "text", "ability.no_durability", "Item in main hand has no durability.");
        addText(translationBuilder, "text", "ability.no_damage", "Item in main hand is not damaged.");
        addText(translationBuilder, "text", "ability.item_restored", "Item restored.");
        addText(translationBuilder, "text", "ability.item_partially_restored", "Item partially restored.");

        addText(translationBuilder, "text", "ability.suffix.1", " I");
        addText(translationBuilder, "text", "ability.suffix.2", " II");
        addText(translationBuilder, "text", "ability.suffix.3", " III");
        addText(translationBuilder, "text", "ability.suffix.4", " IV");
        addText(translationBuilder, "text", "ability.suffix.5", " V");
        addText(translationBuilder, "text", "ability.suffix.6", " VI");
        addText(translationBuilder, "text", "ability.suffix.7", " VII");
        addText(translationBuilder, "text", "ability.suffix.8", " VIII");
        addText(translationBuilder, "text", "ability.suffix.9", " IX");
        addText(translationBuilder, "text", "ability.suffix.10", " X");

        addText(translationBuilder, "tooltip", "groves.chunk.location", "X = %s, Z = %s");
        addText(translationBuilder, "tooltip", "groves.chunk.available", "Available");
        addText(translationBuilder, "tooltip", "groves.chunk.claimed", "Claimed");
        addText(translationBuilder, "tooltip", "groves.chunk.origin", "Origin");
        addText(translationBuilder, "tooltip", "groves.chunk.keep_loaded", "Keep Loaded");
        addText(translationBuilder, "tooltip", "groves.chunk.toggle", "Shift + Left-Click to toggle loading.");
        addText(translationBuilder, "tooltip", "groves.chunk.claim", "Ctrl + Left-Click to claim chunk.");

        addText(translationBuilder, "error", "groves.claim_chunk.cost", "Insufficient sunlight.  Need %s to claim another chunk.");
        addText(translationBuilder, "error", "groves.claim_chunk.invalid", "Chunk is incapable of supporting a Sanctuary.");
        addText(translationBuilder, "error", "groves.claim_chunk.own", "You already oan it.");
        addText(translationBuilder, "error", "groves.claim_chunk.claimed", "Chunk has already been claimed by someone else.");
        addText(translationBuilder, "error", "location.not_grove", "Spawn point must be inside the Sanctuary.");
        addText(translationBuilder, "error", "location.air", "Spawn point not on a valid block.");

        addText(translationBuilder, "text", "imprint.successful.prefix", "Imprint successful.  You can use ");
        addText(translationBuilder, "text", "imprint.successful.suffix", " to open the Grove Sanctuary UI.");
        addText(translationBuilder, "error", "imprint.exists", "There is already a Sanctuary here.");
        addText(translationBuilder, "error", "imprint.has", "You already own a Sanctuary.");
        addText(translationBuilder, "error", "imprint.own", "You already own this Sanctuary.");
        addText(translationBuilder, "error", "imprint.abandoned", "The abandoned Sanctuary did not accept your imprint.");

        addText(translationBuilder, "gui", "sanctuary.abandoned", "Abandoned Sanctuary");
        addText(translationBuilder, "gui", "sanctuary.abandoned.named", "Abandoned %s");
        addText(translationBuilder, "gui", "sanctuary.claimed", "%s's Sanctuary");
        addText(translationBuilder, "gui", "sanctuary.claimed.named", "%s's %s");
        addText(translationBuilder, "gui", "sanctuary.yours", "Your Sanctuary");
        // Yours.named is just the literal of your grove name in the code

        addText(translationBuilder, "gui", "sanctuary.entered.abandoned", "Entering Abandoned Sanctuary");
        addText(translationBuilder, "gui", "sanctuary.entered.abandoned.named", "Entering Abandoned %s");
        addText(translationBuilder, "gui", "sanctuary.entered.claimed", "Entering %s's Sanctuary");
        addText(translationBuilder, "gui", "sanctuary.entered.claimed.named", "Entering %s's %s");
        addText(translationBuilder, "gui", "sanctuary.entered.yours", "Entering Your Sanctuary");
        addText(translationBuilder, "gui", "sanctuary.entered.yours.named", "Entering %s");

        addText(translationBuilder, "error", "already.friend", "Player is already a friend.");
        addText(translationBuilder, "error", "player.not.found", "Player does not exist.");
        addText(translationBuilder, "error", "player.not.friend", "Player is not a friend.");

        addText(translationBuilder, "error", "ability.on_cooldown.suffix", " is on cooldown.");

        addText(translationBuilder, "button", "start", "On");
        addText(translationBuilder, "button", "stop", "Off");
        addText(translationBuilder, "button", "use", "Use");

        addDeathMessages(translationBuilder, "suffocation", "%1$s suffocated", "%1$s suffocated while fighting %2$s");
        addDeathMessages(translationBuilder, "sunlight", "%1$s burned to death from the sun", "%1$s  burned to death from the sun while fighting %2$s");
        addDeathMessages(translationBuilder, "void", "%1$s was devoured by the void", "%1$s was devoured bythe void while fighting %2$s");
        addDeathMessages(translationBuilder, "water", "%1$s liquified", "%1$s liquified while fighting %2$s");

        addStatusEffect(translationBuilder, Registration.AMOROUS_STATUS_EFFECT, "Amorous");
        addStatusEffect(translationBuilder, Registration.ANCHOR_STATUS_EFFECT, "Anchor");
        addStatusEffect(translationBuilder, Registration.ANTIDOTE_STATUS_EFFECT, "Antidote");
        addStatusEffect(translationBuilder, Registration.AQUAPHOBIA_STATUS_EFFECT, "Aquaphobia");
        addStatusEffect(translationBuilder, Registration.BOUNCY_STATUS_EFFECT, "Bouncy");
        addStatusEffect(translationBuilder, Registration.CHANNELING_STATUS_EFFECT, "Channeling");
        addStatusEffect(translationBuilder, Registration.CORROSION_STATUS_EFFECT, "Corrosion");
        addStatusEffect(translationBuilder, Registration.DANGER_SENSE_STATUS_EFFECT, "Danger Sense");
        addStatusEffect(translationBuilder, Registration.DROWNING_STATUS_EFFECT, "Drowning");
        addStatusEffect(translationBuilder, Registration.ECHO_STATUS_EFFECT, "Echo");
        addStatusEffect(translationBuilder, Registration.EXPLOSIVE_STATUS_EFFECT, "Explosive");
        addStatusEffect(translationBuilder, Registration.FLOURISHING_STATUS_EFFECT, "Flourishing");
        addStatusEffect(translationBuilder, Registration.FREEZING_STATUS_EFFECT, "Freezing");
        addStatusEffect(translationBuilder, Registration.GILLS_STATUS_EFFECT, "Gills");
        addStatusEffect(translationBuilder, Registration.GRAVITY_STATUS_EFFECT, "Gravity");
        addStatusEffect(translationBuilder, Registration.INFERNO_STATUS_EFFECT, "Inferno");
        addStatusEffect(translationBuilder, Registration.INTANGIBLE_STATUS_EFFECT, "Intangible");
        addStatusEffect(translationBuilder, Registration.PHOTOSYNTHESIS_STATUS_EFFECT, "Photosynthesis");
        addStatusEffect(translationBuilder, Registration.PORPHYRIA_STATUS_EFFECT, "Porphyria");
        addStatusEffect(translationBuilder, Registration.RADIANCE_STATUS_EFFECT, "Radiance");
        addStatusEffect(translationBuilder, Registration.RECALL_STATUS_EFFECT, "Recall");
        addStatusEffect(translationBuilder, Registration.REVEAL_STATUS_EFFECT, "Reveal");
        addStatusEffect(translationBuilder, Registration.SILENCE_STATUS_EFFECT, "Silence");
        addStatusEffect(translationBuilder, Registration.SPIDER_WALKING_STATUS_EFFECT, "Spider Walking");
        addStatusEffect(translationBuilder, Registration.STICKY_STATUS_EFFECT, "Sticky");
        addStatusEffect(translationBuilder, Registration.SWARMING_STATUS_EFFECT, "Swarming");
        addStatusEffect(translationBuilder, Registration.TAMING_STATUS_EFFECT, "Taming");
        addStatusEffect(translationBuilder, Registration.VOID_STATUS_EFFECT, "Void");
        addStatusEffect(translationBuilder, Registration.WARMING_STATUS_EFFECT, "Warming");

        // Base potions
        addPotion(translationBuilder, Registration.ACRID_BASE_POTION, "Acrid Potion", "Splash Acrid Potion", "Lingering Acrid Potion", "Tipped Arrow");
        addPotion(translationBuilder, Registration.FOUL_BASE_POTION, "Foul Potion", "Splash Foul Potion", "Lingering Foul Potion", "Tipped Arrow");

        // Vanilla potions
        addPotion(translationBuilder, Registration.HASTE_POTION, "Haste");
        addPotion(translationBuilder, Registration.DULLNESS_POTION, "Dullness");
        addPotion(translationBuilder, Registration.BLINDNESS_POTION, "Blindness");
        addPotion(translationBuilder, Registration.HUNGER_POTION, "Hunger");
        addPotion(translationBuilder, Registration.DECAY_POTION, "Decay");
        addPotion(translationBuilder, Registration.RESISTANCE_POTION, "Resistance");
        addPotion(translationBuilder, Registration.NOTCH_POTION, "Notch");
        addPotion(translationBuilder, Registration.LEVITATION_POTION, "Levitation");
        addPotion(translationBuilder, Registration.NAUSEA_POTION, "Nausea");
        addPotion(translationBuilder, Registration.GLOWING_POTION, "Glowing");
        addPotion(translationBuilder, Registration.LUCK_POTION, "Luck");
        addPotion(translationBuilder, Registration.UNLUCK_POTION, "Bad Luck");
        addPotion(translationBuilder, Registration.NEPTUNE_POTION, "Neptune");
        addPotion(translationBuilder, Registration.GRACE_POTION, "Grace");

        // Groves potions
        addPotion(translationBuilder, Registration.ANCHOR_POTION, "Anchoring");
        addPotion(translationBuilder, Registration.ANTIDOTE_POTION, "Antidote", "Splash Antidote", "Lingering Antidote", "Arrow of Antidote");
        addPotion(translationBuilder, Registration.AQUAPHOBIA_POTION, "Aquaphobia");
        addPotion(translationBuilder, Registration.BOUNCY_POTION, "Bouncing");
        addPotion(translationBuilder, Registration.CHANNELING_POTION, "Channeling");
        addPotion(translationBuilder, Registration.CORROSIVE_POTION, "Corrosive");
        addPotion(translationBuilder, Registration.DANGER_SENSE_POTION, "Danger Sense");
        addPotion(translationBuilder, Registration.DROWNING_POTION, "Drowning");
        addPotion(translationBuilder, Registration.ECHO_POTION, "Echoing");
        addPotion(translationBuilder, Registration.EXPLOSIVE_POTION, "Nitroglycerin", "Splash Nitroglycerin", "Lingering Nitroglycerin", "Arrow of Nitroglycerin");
        addPotion(translationBuilder, Registration.FLOURISHING_POTION, "Flourishing");
        addPotion(translationBuilder, Registration.FREEZING_POTION, "Freezing");
        addPotion(translationBuilder, Registration.GILLS_POTION, "Gills");
        addPotion(translationBuilder, Registration.GRAVITY_POTION, "Gravity");
        addPotion(translationBuilder, Registration.INFERNO_POTION, "the Inferno");
        addPotion(translationBuilder, Registration.INTANGIBLE_POTION, "Intangibility");
        addPotion(translationBuilder, Registration.LOVE_POTION, "Love");
        addPotion(translationBuilder, Registration.PHOTOSYNTHESIS_POTION, "Photosynthesis");
        addPotion(translationBuilder, Registration.PORPHYRIA_POTION, "Porphyria");
        addPotion(translationBuilder, Registration.RADIANCE_POTION, "Radiance");
        addPotion(translationBuilder, Registration.RECALL_POTION, "Recall");
        addPotion(translationBuilder, Registration.REVEAL_POTION, "Revealing");
        addPotion(translationBuilder, Registration.SILENCE_POTION, "Silencing");
        addPotion(translationBuilder, Registration.SPIDER_WALKING_POTION, "Spider Walking");
        addPotion(translationBuilder, Registration.STICKY_POTION, "Sticking");
        addPotion(translationBuilder, Registration.SWARMING_POTION, "Swarming");
        addPotion(translationBuilder, Registration.TAMING_POTION, "Taming");
        addPotion(translationBuilder, Registration.VOID_POTION, "the Void");
        addPotion(translationBuilder, Registration.WARMING_POTION, "Warming");
    }
}
