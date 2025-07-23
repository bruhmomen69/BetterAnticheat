package better.anticheat.core.player.tracker.impl;

import better.anticheat.core.player.Player;
import better.anticheat.core.player.tracker.Tracker;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;
import com.github.retrooper.packetevents.event.simple.PacketPlaySendEvent;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerBlockPlacement;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPlayerDigging;
import org.jetbrains.annotations.Nullable;
import wtf.spare.sparej.incrementer.IntIncrementer;

import com.github.retrooper.packetevents.protocol.item.ItemStack;
import com.github.retrooper.packetevents.protocol.player.InteractionHand;
import com.github.retrooper.packetevents.protocol.world.BlockFace;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;

import java.util.Optional;

public class ActionTracker extends Tracker {
    public ActionTracker(Player player) {
        super(player);
    }

    private final IntIncrementer digState = new IntIncrementer(0);
    // Digging
    private boolean digging = false;
    private BlockFace diggingBlockFace = BlockFace.OTHER;
    private @Nullable Vector3i diggingPosition = null;

    // Placing
    private BlockFace placingBlockFace = BlockFace.OTHER;
    private BlockFace lastPlacingBlockFace = BlockFace.OTHER;
    private @Nullable Vector3i lastPlacePosition = null;
    private @Nullable Vector3i placePosition = null;
    private @Nullable Vector3f lastCursorPosition = null;
    private @Nullable Vector3f cursorPosition = null;
    private @Nullable InteractionHand lastPlaceHand = null;
    private @Nullable InteractionHand placeHand = null;
    private Optional<ItemStack> lastPlaceItem = Optional.empty();
    private Optional<ItemStack> placeItem = Optional.empty();
    private boolean isPlacingInsideBlock = false;

    // Sprint/Sneak
    private boolean sneaking = false, wasSneaking = false;
    private boolean sprinting = false, wasSprinting = false;

    // Smart collision values
    private boolean crouching = false, wasCrouching = false;

    // Ticks Since
    private final IntIncrementer ticksSinceSwing = new IntIncrementer();
    private final IntIncrementer ticksSinceCombatSwing = new IntIncrementer();
    private final IntIncrementer ticksSinceAttack = new IntIncrementer();
    private final IntIncrementer ticksSinceDigging = new IntIncrementer();
    private final IntIncrementer ticksSinceSuccessfulDig = new IntIncrementer();
    private final IntIncrementer ticksSincePlace = new IntIncrementer();
    private final IntIncrementer ticksSinceRealPlace = new IntIncrementer();
    private final IntIncrementer ticksSinceRealPlaceUnder = new IntIncrementer();
    private final IntIncrementer ticksSinceTeleport = new IntIncrementer();

    @Override
    public void handlePacketPlayReceive(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_DIGGING -> {
                final var wrapper = new WrapperPlayClientPlayerDigging(event);
                // State
                switch (wrapper.getAction()) {
                    case FINISHED_DIGGING: {
                        if (container.getAction().getDigState().get() > 1) {
                            container.getTicks().getTicksSinceSuccessfulDig().set(0);
                        }
                    }
                    case RELEASE_USE_ITEM:
                    case CANCELLED_DIGGING: {
                        container.getAction().getDigState().set(0);
                        break;
                    }

                    case START_DIGGING: {
                        container.getAction().getDigState().increment(0);
                        break;
                    }
                }

                // Block face + pos
                container.getAction().setDiggingBlockFace(wrapper.getBlockFace());
                container.getAction().setDiggingPosition(wrapper.getBlockPosition());

                container.getAction().setDigging(container.getAction().getDigState().get() > 1);
            }
            case PLAYER_BLOCK_PLACEMENT -> {
                final var wrapper = new WrapperPlayClientPlayerBlockPlacement(event);
                // Block face
                container.getAction().setLastPlacingBlockFace(container.getAction().getPlacingBlockFace());
                container.getAction().setPlacingBlockFace(wrapper.getFace());

                // Place position
                container.getAction().setLastPlacePosition(container.getAction().getPlacePosition());
                container.getAction().setPlacePosition(wrapper.getBlockPosition());

                // Cursor Position
                container.getAction().setCursorPosition(container.getAction().getCursorPosition());
                container.getAction().setCursorPosition(wrapper.getCursorPosition());

                // IHand
                container.getAction().setLastPlaceHand(container.getAction().getLastPlaceHand());
                container.getAction().setPlaceHand(wrapper.getHand());

                if (wrapper.getInsideBlock().isPresent()) {
                    container.getAction().setPlacingInsideBlock(wrapper.getInsideBlock().get());
                } else {
                    container.getAction().setPlacingInsideBlock(false);
                }

                // ItemStack
                container.getAction().setLastPlaceItem(container.getAction().getPlaceItem());
                container.getAction().setPlaceItem(wrapper.getItemStack());

                // Ticks
                container.getTicks().getTicksSincePlace().set(0);

                if (wrapper.getCursorPosition().getX() != 0.0 || wrapper.getCursorPosition().getY() != 0.0 || wrapper.getCursorPosition().getZ() != 0.0) {
                    if (wrapper.getBlockPosition().getY() < 2000) {
                        if (wrapper.getItemStack().isPresent()) {
                            final var itemStack = wrapper.getItemStack().get();
                            if (!itemStack.isEmpty()) {
                                container.getTicks().getTicksSinceRealPlace().set(0);

                                if (wrapper.getBlockPosition().getY() <= container.getPosition().getY()) {
                                    container.getTicks().getTicksSinceRealPlaceUnder().set(0);
                                }

                                this.data.getEventBus().publish(new RealPlaceEvent(data, wrapper));
                            }
                        }
                    }
                }
            }

            case CLIENT_TICK_END -> {
                if (container.getAction().isDigging()) {
                    container.getTicks().getTicksSinceDigging().increment();
                } else {
                    container.getTicks().getTicksSinceDigging().set(0);
                }
                container.getTicks().getTicksSinceSuccessfulDig().increment();

                container.getTicks().getTicksSincePlace().increment();
                container.getTicks().getTicksSinceRealPlace().increment();
                container.getTicks().getTicksSinceRealPlaceUnder().increment();

                // Was
                this.container.getAction().setWasSneaking(this.container.getAction().isSneaking());
                this.container.getAction().setWasSprinting(this.container.getAction().isSprinting());
            }

            case ENTITY_ACTION -> {
                switch (wrapper.getAction()) {
                    // Sprinting
                    case START_SPRINTING: {
                        this.container.getAction().setSprinting(true);
                        break;
                    }
                    case STOP_SPRINTING: {
                        this.container.getAction().setSprinting(false);
                        break;
                    }
                    // Sneaking
                    case START_SNEAKING: {
                        this.container.getAction().setSneaking(true);
                        break;
                    }
                    case STOP_SNEAKING: {
                        this.container.getAction().setSneaking(false);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void handlePacketPlaySend(PacketPlaySendEvent event) {
    }
}
