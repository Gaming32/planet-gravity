package io.github.gaming32.planetgravity.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public class PlayerEntityMixin {
    @Inject(
        method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
        at = @At("RETURN")
    )
    private void onMove(final MovementType movementType, final Vec3d movement, final CallbackInfo info) {
        if (!((Object)this instanceof PlayerEntity)) return;
        if (movement.x == 0 && movement.y == 0 && movement.z == 0) return;
        final PlayerEntity player = (PlayerEntity)(Object)this;
        final Vec3d pos = player.getPos();
        // TODO: use player movement info
    }
}
