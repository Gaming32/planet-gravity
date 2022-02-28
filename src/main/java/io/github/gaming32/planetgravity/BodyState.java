package io.github.gaming32.planetgravity;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.PersistentState;

public final class BodyState extends PersistentState {
    public static final class GravityBody {
        private final Vec3d pos;
        private double range;

        public GravityBody(Vec3d pos, double range) {
            this.pos = pos;
            this.range = range;
        }

        public GravityBody(Vec3d pos) {
            this(pos, 0);
        }

        public GravityBody(NbtCompound nbt) {
            this(
                new Vec3d(
                    nbt.getDouble("x"),
                    nbt.getDouble("y"),
                    nbt.getDouble("z")
                ),
                nbt.contains("range") ? nbt.getDouble("range") : 0
            );
        }

        public NbtCompound writeNbt(NbtCompound nbt) {
            nbt.putDouble("x", pos.x);
            nbt.putDouble("y", pos.y);
            nbt.putDouble("z", pos.z);
            nbt.putDouble("range", range);
            return nbt;
        }

        public Vec3d getPos() {
            return pos;
        }

        public double getRange() {
            return range;
        }

        public void setRange(double range) {
            this.range = range;
        }
    }

    private final Map<Vec3i, GravityBody> bodies;

    public BodyState() {
        this.bodies = new HashMap<>();
    }

    public BodyState(NbtCompound nbt) {
        NbtList bodiesNbt = nbt.getList("bodies", NbtElement.COMPOUND_TYPE);
        this.bodies = new HashMap<>(bodiesNbt.size());
        for (NbtElement bodyNbt : bodiesNbt) {
            GravityBody body = new GravityBody((NbtCompound)bodyNbt);
            bodies.put(floorVec3d(body.pos), body);
        }
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        NbtList bodiesNbt = nbt.getList("bodies", NbtElement.COMPOUND_TYPE);
        bodiesNbt.clear();
        for (GravityBody body : bodies.values()) {
            bodiesNbt.add(body.writeNbt(new NbtCompound()));
        }
        nbt.put("bodies", bodiesNbt);
        return nbt;
    }

    public void addGravityBody(GravityBody body) {
        bodies.put(floorVec3d(body.pos), body);
        setDirty(true);
    }

    public GravityBody removeGravityBody(Vec3d at) {
        final GravityBody body = bodies.remove(floorVec3d(at));
        setDirty(true);
        return body;
    }

    public GravityBody getGravityBody(Vec3d at) {
        return bodies.get(floorVec3d(at));
    }

    public Collection<GravityBody> getAllBodies() {
        return bodies.values();
    }

    public static BodyState getState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(BodyState::new, BodyState::new, "gravityBodies");
    }

    private static Vec3i floorVec3d(Vec3d vec) {
        return new Vec3i(vec.x, vec.y, vec.z);
    }
}
