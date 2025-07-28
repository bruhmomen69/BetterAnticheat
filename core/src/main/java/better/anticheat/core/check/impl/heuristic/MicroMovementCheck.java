package better.anticheat.core.check.impl.heuristic;

import better.anticheat.core.BetterAnticheat;
import better.anticheat.core.check.Check;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientInteractEntity;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerFlying;
import wtf.spare.sparej.EvictingDeque;

/**
 * This check looks for very small mouse movement during combat.
 */
public class MicroMovementCheck extends Check {

    private int ticksSinceAttack = 0, buffer = 0;
    private final EvictingDeque<Float> yawSamples = new EvictingDeque<>(128);
    private final EvictingDeque<Float> pitchSamples = new EvictingDeque<>(128);

    public MicroMovementCheck(BetterAnticheat plugin) {
        super(plugin);
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case INTERACT_ENTITY -> {
                WrapperPlayClientInteractEntity wrapper = new WrapperPlayClientInteractEntity(event);
                if (wrapper.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;
                ticksSinceAttack = 0;
            }
            case CLIENT_TICK_END -> ticksSinceAttack++;
        }

        if (!WrapperPlayClientPlayerFlying.isFlying(event.getPacketType())) return;
        if (!player.getRotationTracker().isRotation()) return;

        if (ticksSinceAttack > 2) return;

        final float deltaPitch = Math.abs(player.getRotationTracker().getDeltaPitch());
        final float deltaYaw = Math.abs(player.getRotationTracker().getDeltaYaw());
        final boolean cinematic = player.getRotationTracker().isCinematic() || player.getRotationTracker().isCinematic2();

        if (cinematic) return;
        if (!(deltaPitch > 0 && deltaYaw > 0 && deltaPitch < 20 && deltaYaw < 20)) return;

        pitchSamples.add(deltaPitch);
        yawSamples.add(deltaYaw);

        if (!yawSamples.isFull()) return;

        final double averagePitch = pitchSamples.stream().mapToDouble(d -> d).average().orElse(0);
        final double averageYaw = yawSamples.stream().mapToDouble(d -> d).average().orElse(0);
        long count = 0;
        count += pitchSamples.stream().filter(delta -> delta < 0.001).count();
        count += yawSamples.stream().filter(delta -> delta < 0.001).count();

        if (((averageYaw > 0 && averageYaw < 1.1) || averagePitch <= 0.01) && count >= 80) {
            buffer++;
            if (buffer > 2) fail();
        } else buffer = Math.max(--buffer, 0);

        pitchSamples.clear();
        yawSamples.clear();
    }
}
