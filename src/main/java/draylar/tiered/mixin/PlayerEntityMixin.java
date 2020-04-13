package draylar.tiered.mixin;

import draylar.tiered.api.CustomEntityAttributes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {

    private PlayerEntityMixin(EntityType<? extends LivingEntity> type, World world) {
        super(type, world);
    }

    @Inject(method = "initAttributes", at = @At("RETURN"))
    private void initAttributes(CallbackInfo ci) {
        this.getAttributes().register(CustomEntityAttributes.DIG_SPEED);
        this.getAttributes().register(CustomEntityAttributes.CRIT_CHANCE);
    }

    @ModifyVariable(
            method = "getBlockBreakingSpeed",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/effect/StatusEffectUtil;hasHaste(Lnet/minecraft/entity/LivingEntity;)Z"
            ),
            name = "f"
    )
    private float getBlockBreakingSpeed(float f) {
        EntityAttributeInstance instance = this.getAttributeInstance(CustomEntityAttributes.DIG_SPEED);

        for (EntityAttributeModifier modifier : instance.getModifiers()) {
            float amount = (float) modifier.getAmount();

            if (modifier.getOperation() == EntityAttributeModifier.Operation.ADDITION) {
                f += amount;
            } else {
                f *= (amount + 1);
            }
        }

        return f;
    }

    @ModifyVariable(
            method = "attack",
            at = @At(
                    value = "JUMP",
                    ordinal = 2
            ),
            slice = @Slice(
                    from = @At(
                            value = "INVOKE",
                            target = "Lnet/minecraft/entity/player/PlayerEntity;isSprinting()Z",
                            ordinal = 1
                    )
            ),
            name = "bl3"
    )
    private boolean attack(boolean bl3) {
        float customChance = 0;

        EntityAttributeInstance instance = this.getAttributeInstance(CustomEntityAttributes.CRIT_CHANCE);

        for (EntityAttributeModifier modifier : instance.getModifiers()) {
            float amount = (float) modifier.getAmount();
            customChance += amount;
        }

        return bl3 || world.random.nextDouble() < customChance;
    }
}