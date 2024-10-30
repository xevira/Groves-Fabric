package github.xevira.groves;

import github.xevira.groves.data.generator.EnchantmentGenerator;
import github.xevira.groves.data.provider.*;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

public class GrovesDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();

		pack.addProvider(EnchantmentGenerator::new);
		pack.addProvider(ModBlockModelProvider::new);
		pack.addProvider(ModBlockLootTableProvider::new);
		pack.addProvider(ModBlockTagProvider::new);
		pack.addProvider(ModFluidTagProvider::new);
		//pack.addProvider(ModEnchantmentTagProvider::new);
		pack.addProvider(ModRecipeProvider::new);
		pack.addProvider(ModEnglishLanguageProvider::new);
	}
}
