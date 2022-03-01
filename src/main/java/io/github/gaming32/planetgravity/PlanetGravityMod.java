package io.github.gaming32.planetgravity;

import java.util.Collection;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.gaming32.planetgravity.BodyState.GravityBody;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.PosArgument;
import net.minecraft.command.argument.Vec3ArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

public class PlanetGravityMod implements ModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("planetgravity");

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(CommandManager.literal("gbody")
                .requires(context -> context.hasPermissionLevel(2))
                .then(LiteralArgumentBuilder.<ServerCommandSource>literal("add")
                    .then(RequiredArgumentBuilder.<ServerCommandSource, PosArgument>argument("pos", Vec3ArgumentType.vec3(true))
                        .executes(context -> {
                            final Vec3d pos = context.getArgument("pos", PosArgument.class).toAbsolutePos(context.getSource());
                            BodyState.getState(context.getSource().getWorld()).addGravityBody(new GravityBody(pos));
                            context.getSource().sendFeedback(
                                Text.of("Successfully added " + pos + " as a gravity body"), true
                            );
                            return 1;
                        })
                    )
                )
                .then(LiteralArgumentBuilder.<ServerCommandSource>literal("remove")
                    .then(RequiredArgumentBuilder.<ServerCommandSource, PosArgument>argument("pos", Vec3ArgumentType.vec3(true))
                        .executes(context -> {
                            final Vec3d pos = context.getArgument("pos", PosArgument.class).toAbsolutePos(context.getSource());
                            BodyState.getState(context.getSource().getWorld()).removeGravityBody(pos);
                            context.getSource().sendFeedback(
                                Text.of("Successfully removed " + pos + " from the list of gravity bodies"), true
                            );
                            return 1;
                        })
                    )
                )
                .then(LiteralArgumentBuilder.<ServerCommandSource>literal("modify")
                    .then(RequiredArgumentBuilder.<ServerCommandSource, PosArgument>argument("pos", Vec3ArgumentType.vec3(true))
                        .then(LiteralArgumentBuilder.<ServerCommandSource>literal("range")
                            .then(RequiredArgumentBuilder.<ServerCommandSource, Double>argument("range", DoubleArgumentType.doubleArg(0))
                                .executes(context -> {
                                    final Vec3d pos = context.getArgument("pos", PosArgument.class).toAbsolutePos(context.getSource());
                                    final double range = context.getArgument("range", Double.class);
                                    final BodyState state = BodyState.getState(context.getSource().getWorld());
                                    final GravityBody body = state.getGravityBody(pos);
                                    if (pos == null) {
                                        context.getSource().sendError(Text.of("No body found at " + pos));
                                        return 0;
                                    }
                                    body.setRange(range);
                                    state.setDirty(true);
                                    context.getSource().sendFeedback(
                                        Text.of("Successfully updated the range of " + pos + " to " + range), true
                                    );
                                    return 1;
                                })
                            )
                        )
                    )
                )
                .then(LiteralArgumentBuilder.<ServerCommandSource>literal("list")
                    .executes(context -> {
                        final Collection<GravityBody> bodies = BodyState.getState(context.getSource().getWorld()).getAllBodies();
                        if (bodies.size() == 0) {
                            context.getSource().sendFeedback(Text.of("No bodies"), false);
                            return 1;
                        }
                        StringBuilder result = new StringBuilder("The following gravity bodies are configured:");
                        for (GravityBody body : bodies) {
                            result.append('\n').append("+ Position = ").append(body.getPos());
                            if (body.getRange() > 0) {
                                result.append(", Range = ").append(body.getRange());
                            }
                        }
                        context.getSource().sendFeedback(Text.of(result.toString()), false);
                        return 1;
                    })
                )
                .then(LiteralArgumentBuilder.<ServerCommandSource>literal("clear")
                    .executes(context -> {
                        final BodyState state = BodyState.getState(context.getSource().getWorld());
                        int numberCleared = state.getAllBodies().size();
                        state.getAllBodies().clear();
                        state.setDirty(true);
                        context.getSource().sendFeedback(
                            Text.of("Cleared " + numberCleared + " bodies"), true
                        );
                        return 1;
                    })
                )
            );
		});
    }
}
