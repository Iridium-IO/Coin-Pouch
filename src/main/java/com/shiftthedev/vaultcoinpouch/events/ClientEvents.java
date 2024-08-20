package com.shiftthedev.vaultcoinpouch.events;

import com.shiftthedev.vaultcoinpouch.VCPRegistry;
import com.shiftthedev.vaultcoinpouch.VaultCoinPouch;
import com.shiftthedev.vaultcoinpouch.config.VCPConfigScreen;
import com.shiftthedev.vaultcoinpouch.container.CoinPouchScreen;
import com.shiftthedev.vaultcoinpouch.network.KeyPressMessage;
import com.shiftthedev.vaultcoinpouch.utils.KeyBindings;
import iskallia.vault.init.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.ConfigGuiHandler;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;

import static com.shiftthedev.vaultcoinpouch.VCPRegistry.COIN_POUCH_CONTAINER;

@EventBusSubscriber(
        bus = EventBusSubscriber.Bus.MOD,
        value = {Dist.CLIENT}
)
public class ClientEvents
{
    @OnlyIn(Dist.CLIENT)
    public static VCPConfigScreen CONFIG_SCREEN = new VCPConfigScreen();

    @SubscribeEvent(
            priority = EventPriority.LOW
    )
    public static void setupClient(FMLClientSetupEvent event)
    {
        registerScreen();
        registerConfigScreen();
    }


    @OnlyIn(Dist.CLIENT)
    private static void registerConfigScreen()
    {
        ModLoadingContext.get().registerExtensionPoint(ConfigGuiHandler.ConfigGuiFactory.class,
                () -> new ConfigGuiHandler.ConfigGuiFactory(new BiFunction<Minecraft, Screen, Screen>()
                {
                    @Override
                    public Screen apply(Minecraft minecraft, Screen screen)
                    {
                        CONFIG_SCREEN.setup(minecraft, screen);
                        return CONFIG_SCREEN;
                    }
                }));
    }

    @OnlyIn(Dist.CLIENT)
    public static void registerScreen()
    {
        MenuScreens.register(COIN_POUCH_CONTAINER, CoinPouchScreen::new);
    }



    @Mod.EventBusSubscriber(modid = VaultCoinPouch.MOD_ID, value = Dist.CLIENT)
    public static class ClientForge
    {
        @SubscribeEvent
        public static void onKeyInput(InputEvent.KeyInputEvent event)
        {
            if (KeyBindings.SHOW_POUCH.consumeClick())
            {
                Minecraft minecraft = Minecraft.getInstance();
                Player player = minecraft.player;
                if (player == null) return;

                int slot = findSlotMatchingItemIgnoreNBT(player, VCPRegistry.COIN_POUCH);
                if (slot == -1) return;

                ModNetwork.CHANNEL.sendToServer(new KeyPressMessage(slot));

            }

        }
        public static int findSlotMatchingItemIgnoreNBT(Player player, Item targetItem) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stackInSlot = player.getInventory().getItem(i);
                if (stackInSlot.getItem() == targetItem) return i;
            }

            AtomicInteger curiosSlot = new AtomicInteger(-1);

            // Check the player's Curios inventory
            //THIS DOES NOT WORK IN GAME AND I'M NOT SKILLED ENOUGH TO FIGURE OUT WHY. THE CODE RETURNS THE CURIO SLOT, BUT THE GUI ONLY FLICKERS AND CLOSES
            CuriosApi.getCuriosHelper().getCuriosHandler(player).ifPresent(handler -> {
                for (Map.Entry<String, ICurioStacksHandler> entry : handler.getCurios().entrySet()) {
                    String id = entry.getKey();
                    ICurioStacksHandler stacksHandler = entry.getValue();
                    for (int i = 0; i < stacksHandler.getSlots(); i++) {
                        ItemStack stackInSlot = stacksHandler.getStacks().getStackInSlot(i);
                        if (stackInSlot.getItem() == targetItem) {
                            // Set the slot index with an offset to differentiate it from the main inventory slots
                            curiosSlot.set(player.getInventory().getContainerSize() + i);
                            break;
                        }
                    }
                }
            });

            return curiosSlot.get();
        }

    }

}

