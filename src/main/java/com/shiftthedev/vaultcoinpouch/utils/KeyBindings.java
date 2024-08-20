package com.shiftthedev.vaultcoinpouch.utils;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {


    public static final String KEY_CATEGORY = "key.categories.vaultcoinpouch";
    public static final String KEY_SHOWPOUCH = "key.vaultcoinpouch.showpouch";

    public static final KeyMapping SHOW_POUCH = new KeyMapping(KEY_SHOWPOUCH, KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_P, KEY_CATEGORY);

    public static void init() {
        ClientRegistry.registerKeyBinding(SHOW_POUCH);
    }

}
