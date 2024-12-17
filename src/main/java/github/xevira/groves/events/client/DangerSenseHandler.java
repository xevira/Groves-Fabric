package github.xevira.groves.events.client;

import github.xevira.groves.Groves;
import github.xevira.groves.Registration;
import github.xevira.groves.concoctions.potion.effects.DangerSenseStatusEffect;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.util.math.Box;

import java.util.IdentityHashMap;
import java.util.List;

@Environment(EnvType.CLIENT)
public class DangerSenseHandler {
    public static final DangerSenseHandler INSTANCE = new DangerSenseHandler();

    private int timer;
    private boolean enabled;

    public DangerSenseHandler()
    {
        this.timer = 0;
        this.enabled = false;
    }

    public void enable()
    {
        if (!this.enabled) {
            this.enabled = true;
            this.timer = 0;
        }
    }

    public void tick()
    {
        if (!this.enabled) return;

        ClientPlayerEntity player = MinecraftClient.getInstance().player;

        if (player == null) return;

        if (player.getWorld() == null) return;

        if (this.timer <= 0)
        {
            StatusEffectInstance effect = player.getStatusEffect(Registration.DANGER_SENSE_STATUS_EFFECT);

            if (effect != null)
            {
                Box box = DangerSenseStatusEffect.getBoundingBox(player, effect.getAmplifier());

                List<HostileEntity> hostiles = player.getWorld().getEntitiesByClass(HostileEntity.class, box, entity -> !entity.isGlowing());
                hostiles.forEach(hostile -> MobHandler.addMobHandler(hostile, player));

                this.timer = 5;
            }
            else
                unload();
        }
        else
            this.timer--;
    }

    public void unload()
    {
        this.enabled = false;
        MobHandler.clear();
    }

    public static class MobHandler
    {
        private static final IdentityHashMap<HostileEntity, MobHandler> MONSTERS = new IdentityHashMap<>();

        private final HostileEntity monster;
        private final ClientPlayerEntity player;
        private int lastMonsterAge;

        public MobHandler(HostileEntity monster, ClientPlayerEntity player)
        {
            this.monster = monster;
            this.player = player;
            this.lastMonsterAge = monster.age;

//            MONSTERS.put(monster, this);
        }

        private void remove()
        {
            MONSTERS.remove(this.monster);
        }

        public void startTick()
        {
            if (!this.monster.isAlive())
            {
                this.remove();
                return;
            }

            // Timeouts
            if (this.lastMonsterAge == 0 || (this.monster.age - this.lastMonsterAge) < 10)
            {
                this.lastMonsterAge = this.monster.age;
            }
            else {
                this.remove();
                return;
            }

            StatusEffectInstance effect = this.player.getStatusEffect(Registration.DANGER_SENSE_STATUS_EFFECT);
            if (effect != null)
            {
                Box box = DangerSenseStatusEffect.getBoundingBox(this.player, effect.getAmplifier());

                if (box.intersects(this.monster.getBoundingBox()))
                {
                    this.monster.setGlowing(true);
                    return;
                }
            }

            if (!this.monster.hasStatusEffect(StatusEffects.GLOWING))
            {
                this.monster.setGlowing(false);
            }

            this.remove();
        }

        public static void addMobHandler(HostileEntity monster, ClientPlayerEntity player)
        {
            MobHandler handler = MONSTERS.get(monster);
            if (handler == null) {
                MONSTERS.put(monster, new MobHandler(monster, player));
            }
        }

        public static void onClientStartTick()
        {
            List<MobHandler> handlers = MONSTERS.values().stream().toList();

            handlers.forEach(MobHandler::startTick);
        }

        public static void onEntityUnload(Entity entity)
        {
            if (entity instanceof HostileEntity hostile)
            {
                MONSTERS.remove(hostile);
            }
        }

        public static void clear()
        {
            MONSTERS.clear();
        }
    }
}
