package io.github.gaming32.planetgravity.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.gaming32.planetgravity.BodyState;
import io.github.gaming32.planetgravity.BodyState.GravityBody;
import me.andrew.gravitychanger.api.GravityChangerAPI;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

@Mixin(Entity.class)
public class PlayerEntityMixin {
    private static final double MIN_TRANSITION_DISTANCE = 2.5;

    private ServerWorld lastWorld;
    private BodyState lastState;
    private Vec3d lastTransitionPoint;
    private boolean underControl;

    @Inject(
        method = "move(Lnet/minecraft/entity/MovementType;Lnet/minecraft/util/math/Vec3d;)V",
        at = @At("RETURN")
    )
    private void onMove(final MovementType movementType, final Vec3d movement, final CallbackInfo info) {
        if (!((Object)this instanceof PlayerEntity)) return;
        if (movement.x == 0 && movement.y == 0 && movement.z == 0) return;
        final PlayerEntity player = (PlayerEntity)(Object)this;
        if (!(player.world instanceof ServerWorld)) return;
        final Vec3d pos = player.getPos();
        BodyState state = lastState;
        if (player.world != lastWorld) {
            lastWorld = (ServerWorld)player.world;
            lastState = state = BodyState.getState(lastWorld);
        }
        if (lastTransitionPoint != null && pos.isInRange(lastTransitionPoint, MIN_TRANSITION_DISTANCE)) return;
        final Direction oldDir = GravityChangerAPI.getGravityDirection(player);
        double closestDistance = 0;
        Vec3d closest = null;
        for (GravityBody body : state.getAllBodies()) {
            if (body.getRange() > 0 && !body.getPos().isInRange(pos, body.getRange())) continue;
            double distance;
            if (closest == null) {
                closestDistance = body.getPos().distanceTo(pos);
                closest = body.getPos();
            } else if ((distance = closest.distanceTo(pos)) < closestDistance) {
                closestDistance = distance;
                closest = body.getPos();
            }
        }
        if (closest == null) {
            if (underControl) {
                GravityChangerAPI.setGravityDirection(player, Direction.DOWN);
                underControl = false;
            }
            return;
        }
        underControl = true;
        Direction newDir;
        double maxDist, curDist;
        if (pos.y < closest.y) {
            newDir = Direction.UP;
            maxDist = closest.y - pos.y;
        } else {
            newDir = Direction.DOWN;
            maxDist = pos.y - closest.y;
        }
        if (pos.x < closest.x) {
            if ((curDist = closest.x - pos.x) > maxDist) {
                newDir = Direction.EAST;
                maxDist = curDist;
            }
        } else {
            if ((curDist = pos.x - closest.x) > maxDist) {
                newDir = Direction.WEST;
                maxDist = curDist;
            }
        }
        if (pos.z < closest.z) {
            if ((curDist = closest.z - pos.z) > maxDist) {
                newDir = Direction.SOUTH;
                maxDist = curDist;
            }
        } else {
            if ((curDist = pos.z - closest.z) > maxDist) {
                newDir = Direction.NORTH;
                maxDist = curDist;
            }
        }
        if (newDir != oldDir) {
            lastTransitionPoint = pos;
        }
        GravityChangerAPI.setGravityDirection(player, newDir);
    }
}
