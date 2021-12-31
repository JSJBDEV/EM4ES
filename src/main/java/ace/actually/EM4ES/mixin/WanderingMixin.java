package ace.actually.EM4ES.mixin;

import ace.actually.EM4ES.EM4ES;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.village.TradeOffer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingMixin extends MerchantEntity {

    public WanderingMixin(EntityType<? extends MerchantEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(at = @At("TAIL"), method = "fillRecipes")
    public void addTrades(CallbackInfo ci) {
        if(!world.isClient) {
            ServerWorld world = (ServerWorld) getWorld();
            Thread thread = new Thread(() -> {
                for (int i = 0; i < random.nextInt(1,8); i++) {
                    offers.add(new TradeOffer(new ItemStack(Items.EMERALD, random.nextInt(1,64)), EM4ES.makeRandomMap(world, getBlockPos()), 1, 1, 1));
                }
            });
            thread.start();
        }
    }
}
