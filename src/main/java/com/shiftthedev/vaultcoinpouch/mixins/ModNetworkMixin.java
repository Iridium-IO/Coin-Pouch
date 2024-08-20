package com.shiftthedev.vaultcoinpouch.mixins;

import com.shiftthedev.vaultcoinpouch.network.ConfigSyncMessage;
import com.shiftthedev.vaultcoinpouch.network.KeyPressMessage;
import com.shiftthedev.vaultcoinpouch.network.ShiftVaultForgeRequestCraftMessage;
import iskallia.vault.init.ModNetwork;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.simple.SimpleChannel;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(value = ModNetwork.class, remap = false, priority = 9999)
public abstract class ModNetworkMixin
{
    @Shadow
    @Final
    public static SimpleChannel CHANNEL;

    @Inject(method = "initialize", at = @At("RETURN"))
    private static void initialize_coinpouch(CallbackInfo ci)
    {
        CHANNEL.registerMessage(ModNetwork.nextId(), ShiftVaultForgeRequestCraftMessage.class, ShiftVaultForgeRequestCraftMessage::encode, ShiftVaultForgeRequestCraftMessage::decode, ShiftVaultForgeRequestCraftMessage::handle);

        CHANNEL.registerMessage(ModNetwork.nextId(), ConfigSyncMessage.class, ConfigSyncMessage::encode, ConfigSyncMessage::decode, ConfigSyncMessage::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        CHANNEL.registerMessage(ModNetwork.nextId(), KeyPressMessage.class, KeyPressMessage::encode, KeyPressMessage::decode, KeyPressMessage::handle, Optional.of(NetworkDirection.PLAY_TO_SERVER));

    }
}
