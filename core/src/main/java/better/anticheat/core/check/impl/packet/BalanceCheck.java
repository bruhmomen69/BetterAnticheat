package better.anticheat.core.check.impl.packet;

import better.anticheat.core.check.Check;
import better.anticheat.core.configuration.ConfigSection;
import com.github.retrooper.packetevents.event.simple.PacketPlayReceiveEvent;

public class BalanceCheck extends Check {

    private long lastTick = -1, balance = 0;
    private long maxBalance, minBalance;

    public BalanceCheck() {
        super("Balance");
    }

    @Override
    public void handleReceivePlayPacket(PacketPlayReceiveEvent event) {
        switch (event.getPacketType()) {
            case PLAYER_LOADED:
                lastTick = 0;
                break;
            case CLIENT_TICK_END:
                if (lastTick == -1) return;

                long tick = System.currentTimeMillis();
                if (lastTick == 0) {
                    lastTick = tick;
                    return;
                }

                balance += 50;
                balance -= (tick - lastTick);
                balance = Math.max(balance, minBalance);

                if (balance > maxBalance) {
                    fail(balance);
                    balance = 0;
                }

                lastTick = tick;

                break;
        }
    }

    @Override
    public void load(ConfigSection section) {
        super.load(section);
        maxBalance = section.getObject(Integer.class, "max-balance", 100);
        minBalance = section.getObject(Integer.class, "min-balance", -3000);
    }

    @Override
    public void load() {
        super.load();
        BalanceCheck balanceCheck = (BalanceCheck) reference;
        maxBalance = balanceCheck.maxBalance;
        minBalance = balanceCheck.minBalance;
    }
}
