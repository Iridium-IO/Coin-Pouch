package com.shiftthedev.vaultcoinpouch.network;

import com.shiftthedev.vaultcoinpouch.VaultCoinPouch;
import com.shiftthedev.vaultcoinpouch.item.CoinPouchItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class KeyPressMessage {

    private final int slot;

    public KeyPressMessage(int slot) {
        this.slot = slot;
    }

    public int getSlot() {
        return this.slot;
    }

    public static void encode(KeyPressMessage message, FriendlyByteBuf buffer) {
        buffer.writeInt(message.getSlot());
    }

    public static KeyPressMessage decode(FriendlyByteBuf buffer) {
        return new KeyPressMessage(buffer.readInt());
    }

    public static void handle(KeyPressMessage packet, Supplier<NetworkEvent.Context> contextSupplier) {
        ServerPlayer player = contextSupplier.get().getSender(); // This is the server player
        contextSupplier.get().enqueueWork(() -> {
            int slot = packet.getSlot();
            VaultCoinPouch.LOGGER.info("Key press message received");
            VaultCoinPouch.LOGGER.info("Slot: " + slot);

            CoinPouchItem.openGUI(contextSupplier.get().getSender(), slot);

        // Handle the key press here using the server player
        });
        contextSupplier.get().setPacketHandled(true);
    }

}
