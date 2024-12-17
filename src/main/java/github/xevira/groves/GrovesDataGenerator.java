package github.xevira.groves;

import github.xevira.groves.data.generator.DamageTypeGenerator;
import github.xevira.groves.data.generator.EnchantmentGenerator;
import github.xevira.groves.data.generator.ModWorldGenerator;
import github.xevira.groves.data.provider.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.minecraft.registry.RegistryBuilder;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;

public class GrovesDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(EnchantmentGenerator::new);
		pack.addProvider(ModBlockModelProvider::new);
		pack.addProvider(ModBlockLootTableProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModFluidTagProvider::new);
		pack.addProvider(ModItemTagProvider::new);
		pack.addProvider(ModWorldGenerator::new);
		pack.addProvider(DamageTypeGenerator::new);
		//pack.addProvider(ModEnchantmentTagProvider::new);
		pack.addProvider(ModDamageTypeTagProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModEnglishLanguageProvider::new);
	}

	@Override
	public void buildRegistry(RegistryBuilder registryBuilder) {
		registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, Registration::bootstrapConfiguredFeature);
		registryBuilder.addRegistry(RegistryKeys.PLACED_FEATURE, Registration::bootstrapPlacedFeature);
		registryBuilder.addRegistry(RegistryKeys.DAMAGE_TYPE, Registration::bootstrapDamageTypes);
	}
}
