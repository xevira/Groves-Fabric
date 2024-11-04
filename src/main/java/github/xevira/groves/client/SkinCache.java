package github.xevira.groves.client;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import github.xevira.groves.Groves;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.SkinTextures;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

@Environment(EnvType.CLIENT)
public class SkinCache {
    public static final Map<UUID, SkinTextures> CACHED_SKINS = new HashMap<>();
    private static final Map<UUID, Supplier<SkinTextures>> TEXTURE_SUPPLIER = new HashMap<>();

    public static SkinTextures fetch(UUID uuid, String name)
    {
        return fetch(new GameProfile(uuid, name));
    }

    public static SkinTextures fetch(GameProfile profile)
    {
        if (CACHED_SKINS.containsKey(profile.getId()))
            return CACHED_SKINS.get(profile.getId());

        if (!TEXTURE_SUPPLIER.containsKey(profile.getId())) {
            Supplier<Supplier<SkinTextures>> supplier = Suppliers.memoize(() -> texturesSupplier(profile));
            TEXTURE_SUPPLIER.put(profile.getId(), () -> (SkinTextures)((Supplier<?>)supplier.get()).get());
        }
        else {
            SkinTextures textures = TEXTURE_SUPPLIER.get(profile.getId()).get();

            if (textures != null)
            {
                CACHED_SKINS.put(profile.getId(), textures);
                return textures;
            }
        }

        return null;
    }

    private static Supplier<SkinTextures> texturesSupplier(GameProfile profile) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        PlayerSkinProvider playerSkinProvider = minecraftClient.getSkinProvider();
        CompletableFuture<SkinTextures> completableFuture = playerSkinProvider.fetchSkinTextures(profile);
        boolean bl = !minecraftClient.uuidEquals(profile.getId());
        SkinTextures skinTextures = DefaultSkinHelper.getSkinTextures(profile);
        return () -> {
            SkinTextures skinTextures2 = completableFuture.getNow(skinTextures);
            return bl && !skinTextures2.secure() ? skinTextures : skinTextures2;
        };
    }
}
