package ace.actually.EM4ES.mixin;

import ace.actually.EM4ES.EM4ES;
import net.minecraft.client.util.ParticleUtil;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.UniformIntProvider;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;
import java.util.concurrent.Future;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingMixin extends MerchantEntity {

    private final Thread genTradesThread = new Thread(() -> {
        ServerWorld world = (ServerWorld) getWorld();
        Random r = new Random();
        for (int i = 0; i < MathHelper.nextBetween(r, 1, 3) && !isDead(); i++) {
            try {
                EM4ES.LOGGER.debug("Generating Structure Map for Villager " + getName().asString() + " (" + uuidString + ") at position " + getBlockPos().toShortString() + " in world " + world.getRegistryKey().getValue().getPath() + "...");
                Future<ItemStack> map = EM4ES.makeRandomMap(world, getBlockPos());

                if (isDead())
                    return;
                this.playSound(getYesSound(), this.getSoundVolume(), this.getSoundPitch());
                EM4ES.LOGGER.debug("Structure Map for " + getName().asString() + " at position " + getBlockPos() + " in world " + world.getRegistryKey().getValue().getPath() + " generated!");
                offers.add(new TradeOffer(new ItemStack(Items.EMERALD, MathHelper.nextBetween(r, 16, 64)), new ItemStack(Items.COMPASS), map.get(), 1, 1, 1));
            } catch (Exception ignored) {
            }
        }
    });

    public WanderingMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract SoundEvent getYesSound();

    @Inject(at = @At("HEAD"), method = "fillRecipes")
    public void addTrades(CallbackInfo ci) {
        if (!world.isClient) {
            genTradesThread.start();
        }
    }
}
