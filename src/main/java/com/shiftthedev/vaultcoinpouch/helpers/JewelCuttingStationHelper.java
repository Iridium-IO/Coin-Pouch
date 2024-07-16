package com.shiftthedev.vaultcoinpouch.helpers;

import com.mojang.blaze3d.platform.InputConstants;
import com.shiftthedev.vaultcoinpouch.VCPRegistry;
import com.shiftthedev.vaultcoinpouch.item.CoinPouchItem;
import iskallia.vault.block.entity.VaultJewelCuttingStationTileEntity;
import iskallia.vault.client.ClientExpertiseData;
import iskallia.vault.config.VaultJewelCuttingConfig;
import iskallia.vault.container.VaultJewelCuttingStationContainer;
import iskallia.vault.container.oversized.OverSizedInventory;
import iskallia.vault.gear.VaultGearRarity;
import iskallia.vault.gear.attribute.VaultGearAttributeInstance;
import iskallia.vault.gear.attribute.VaultGearModifier;
import iskallia.vault.gear.data.VaultGearData;
import iskallia.vault.init.ModGearAttributes;
import iskallia.vault.item.tool.JewelItem;
import iskallia.vault.skill.base.LearnableSkill;
import iskallia.vault.skill.base.TieredSkill;
import iskallia.vault.skill.expertise.type.JewelExpertise;
import iskallia.vault.util.MiscUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class JewelCuttingStationHelper
{
    /**
     * Called in mixins/VaultJewelCuttingStationTileEntityMixin
     **/
    public static void withdraw(VaultJewelCuttingStationContainer container, ServerPlayer player, VaultJewelCuttingConfig.JewelCuttingInput recipeInput)
    {
        int bronzeCount = container.getBronzeSlot().getItem().getCount();
        ItemStack secondInput = recipeInput.getSecondInput();
        int recipeCount = secondInput.getCount();
        int remaining = recipeCount - bronzeCount;

        if (remaining <= 0)
        {
            return;
        }

        NonNullList<ItemStack> pouchStacks = NonNullList.create();
        if (CuriosApi.getCuriosHelper().findFirstCurio(player, VCPRegistry.COIN_POUCH).isPresent())
        {
            pouchStacks.add(CuriosApi.getCuriosHelper().findFirstCurio(player, VCPRegistry.COIN_POUCH).get().stack());
        }

        Iterator it = player.getInventory().items.iterator();
        int toRemove = 0;
        while (it.hasNext())
        {
            if (remaining <= 0)
            {
                break;
            }

            ItemStack plStack = (ItemStack) it.next();
            if (VaultJewelCuttingStationTileEntity.canMerge(plStack, secondInput))
            {
                toRemove = Math.min(remaining, plStack.getCount());
                plStack.shrink(toRemove);
                remaining -= toRemove;
            }

            if (plStack.is(VCPRegistry.COIN_POUCH))
            {
                pouchStacks.add(plStack);
            }
        }

        if (remaining <= 0)
        {
            return;
        }

        it = pouchStacks.iterator();
        while (it.hasNext())
        {
            if (remaining <= 0)
            {
                break;
            }

            ItemStack pouchStack = (ItemStack) it.next();
            toRemove = Math.min(remaining, CoinPouchItem.getCoinCount(pouchStack, secondInput));
            CoinPouchItem.extractCoins(pouchStack, secondInput, toRemove);
            remaining -= toRemove;
        }
    }

    /**
     * Called in mixins/VaultJewelCuttingStationTileEntityMixin
     **/
    public static boolean canCraft(VaultJewelCuttingStationTileEntity tileEntity, Player player)
    {
        VaultJewelCuttingConfig.JewelCuttingOutput output = tileEntity.getRecipeOutput();
        VaultJewelCuttingConfig.JewelCuttingInput input = tileEntity.getRecipeInput();
        OverSizedInventory inventory = tileEntity.getInventory();

        if (input == null || output == null)
        {
            return false;
        }

        if (!VaultJewelCuttingStationTileEntity.canMerge(inventory.getItem(0), input.getMainInput()))
        {
            return false;
        }
        if (inventory.getItem(0).getCount() < input.getMainInput().getCount())
        {
            return false;
        }

        if (!hasGold(input.getSecondInput(), inventory.getItem(1), player))
        {
            return false;
        }

        if (!MiscUtils.canFullyMergeIntoSlot(inventory, 2, output.getMainOutputMatching()))
        {
            return false;
        }
        if (!MiscUtils.canFullyMergeIntoSlot(inventory, 3, output.getExtraOutput1Matching()))
        {
            return false;
        }

        return MiscUtils.canFullyMergeIntoSlot(inventory, 4, output.getExtraOutput2Matching());
    }

    /**
     * Called in mixins/VaultJewelCuttingStationTileEntityMixin
     **/
    public static boolean setDisabled_coinpouch(VaultJewelCuttingStationContainer menu, Player player)
    {
        if (menu.getTileEntity() != null && !JewelCuttingStationHelper.canCraft(menu.getTileEntity(), player))
        {
            return true;
        }
        else if (menu.getJewelInputSlot().getItem().getItem() instanceof JewelItem)
        {
            VaultGearData data = VaultGearData.read(menu.getJewelInputSlot().getItem());
            return (Integer) data.getFirstValue(ModGearAttributes.JEWEL_SIZE).orElse(0) <= 10;
        }
        else
        {
            return true;
        }
    }

    /**
     * Called in mixins/VaultJewelCuttingStationTileEntityMixin
     **/
    public static Object setDisabled_vh(VaultJewelCuttingStationContainer menu)
    {
        if (menu.getTileEntity() != null && !menu.getTileEntity().canCraft())
        {
            return true;
        }
        else if (menu.getJewelInputSlot().getItem().getItem() instanceof JewelItem)
        {
            VaultGearData data = VaultGearData.read(menu.getJewelInputSlot().getItem());
            return (Integer) data.getFirstValue(ModGearAttributes.JEWEL_SIZE).orElse(0) <= 10;
        }
        else
        {
            return true;
        }
    }

    /**
     * Called in mixins/JewelCuttingButtonElementMixin
     **/
    public static List<Component> tooltip(VaultJewelCuttingStationContainer container)
    {
        Player player = Minecraft.getInstance().player;
        if (player == null)
        {
            return List.of();
        }
        else
        {
            long window = Minecraft.getInstance().getWindow().getWindow();
            boolean shiftDown = InputConstants.isKeyDown(window, 340) || InputConstants.isKeyDown(window, 344);
            ItemStack inputItem = ItemStack.EMPTY;
            Slot inputSlot = container.getJewelInputSlot();
            if (inputSlot != null && !inputSlot.getItem().isEmpty())
            {
                inputItem = inputSlot.getItem();
            }

            boolean hasInput = !inputItem.isEmpty();
            List<Component> tooltip = new ArrayList();
            VaultJewelCuttingConfig.JewelCuttingInput input = container.getTileEntity().getRecipeInput();
            VaultJewelCuttingConfig.JewelCuttingRange range = container.getTileEntity().getJewelCuttingRange();
            float chance = container.getTileEntity().getJewelCuttingModifierRemovalChance();
            int numberOfFreeCuts = 0;
            Iterator var14 = ClientExpertiseData.getLearnedTalentNodes().iterator();

            while (var14.hasNext())
            {
                TieredSkill learnedTalentNode = (TieredSkill) var14.next();
                LearnableSkill patt3197$temp = learnedTalentNode.getChild();
                if (patt3197$temp instanceof JewelExpertise)
                {
                    JewelExpertise jewelExpertise = (JewelExpertise) patt3197$temp;
                    numberOfFreeCuts = jewelExpertise.getNumberOfFreeCuts();
                }
            }

            ItemStack scrap = container.getScrapSlot().getItem();
            ItemStack bronze = container.getBronzeSlot().getItem();
            if (hasInput)
            {
                VaultGearData data = VaultGearData.read(inputItem);
                List<VaultGearModifier<?>> prefix = new ArrayList(data.getModifiers(VaultGearModifier.AffixType.PREFIX));
                List<VaultGearModifier<?>> suffix = new ArrayList(data.getModifiers(VaultGearModifier.AffixType.SUFFIX));
                int affixSize = prefix.size() + suffix.size();
                VaultGearRarity lowerRarity = VaultJewelCuttingStationTileEntity.getNewRarity(affixSize - 1);
                String var10000 = lowerRarity.name();
                String jewelLowerRarity = "item.the_vault.jewel." + var10000.toLowerCase(Locale.ROOT);
                MutableComponent lowerRarityComponent = (new TranslatableComponent(jewelLowerRarity)).withStyle(ChatFormatting.YELLOW);
                VaultGearRarity rarity = VaultJewelCuttingStationTileEntity.getNewRarity(affixSize);
                var10000 = rarity.name();
                String jewelRarity = "item.the_vault.jewel." + var10000.toLowerCase(Locale.ROOT);
                MutableComponent rarityComponent = (new TranslatableComponent(jewelRarity)).withStyle(ChatFormatting.YELLOW);
                if (affixSize < 2)
                {
                    tooltip.add(new TextComponent("Cut the Jewel into a Gemstone"));
                }
                else
                {
                    int var10003 = range.getMin();
                    tooltip.add((new TextComponent("Cut the jewel down in size (" + var10003 + "-" + range.getMax() + "), making it ")).append(lowerRarityComponent).append(new TextComponent(".")));
                    tooltip.add(new TextComponent("This will make it lose a random affix."));
                }

                if (numberOfFreeCuts > 0)
                {
                    tooltip.add(TextComponent.EMPTY);
                    tooltip.add((new TextComponent("")).append((new TextComponent("* ")).withStyle(ChatFormatting.GOLD)).append(new TextComponent("Your ")).append((new TextComponent("Jeweler Expertise")).withStyle(ChatFormatting.LIGHT_PURPLE)).append(new TextComponent(" gives you ")).append((new TextComponent(String.valueOf(numberOfFreeCuts))).withStyle(ChatFormatting.YELLOW)).append(new TextComponent(" free cut" + (numberOfFreeCuts == 1 ? "" : "s"))));
                    tooltip.add(new TextComponent("retaining its current grade."));
                    int usedFreeCuts = !inputItem.getOrCreateTag().contains("freeCuts") ? 0 : inputItem.getOrCreateTag().getInt("freeCuts");
                    int remaining = numberOfFreeCuts - usedFreeCuts;
                    tooltip.add((new TextComponent("Expertise Cuts: ")).append(addTooltipDots(usedFreeCuts, ChatFormatting.YELLOW)).append(addTooltipDots(remaining, ChatFormatting.GRAY)));
                }

                tooltip.add(TextComponent.EMPTY);
                tooltip.add(new TextComponent("Cost"));
                MutableComponent var10001 = (new TextComponent("- ")).append(input.getMainInput().getHoverName());
                int var10002 = input.getMainInput().getCount();
                tooltip.add(var10001.append(" x" + var10002).append(" [%s]".formatted(scrap.getCount())).withStyle(input.getMainInput().getCount() > scrap.getCount() ? ChatFormatting.RED : ChatFormatting.GREEN));
                var10001 = (new TextComponent("- ")).append(input.getSecondInput().getHoverName());
                var10002 = input.getSecondInput().getCount();

                // Coin Pouch check
                int goldMAmount = bronze.getCount();
                if (CuriosApi.getCuriosHelper().findFirstCurio(player, VCPRegistry.COIN_POUCH).isPresent())
                {
                    goldMAmount += CoinPouchItem.getCoinCount(CuriosApi.getCuriosHelper().findFirstCurio(player, VCPRegistry.COIN_POUCH).get().stack(), input.getSecondInput());
                }

                Iterator it = container.getPlayer().getInventory().items.iterator();
                while (it.hasNext())
                {
                    ItemStack plStack = (ItemStack) it.next();
                    if (VaultJewelCuttingStationTileEntity.canMerge(plStack, input.getSecondInput()))
                    {
                        goldMAmount += plStack.getCount();
                    }
                    else if (plStack.is(VCPRegistry.COIN_POUCH))
                    {
                        goldMAmount += CoinPouchItem.getCoinCount(plStack, input.getSecondInput());
                    }
                }

                tooltip.add(var10001.append(" x" + var10002).append(" [%s]".formatted(goldMAmount)).withStyle(input.getSecondInput().getCount() > goldMAmount ? ChatFormatting.RED : ChatFormatting.GREEN));
                // End of Coin Pouch check

                tooltip.add(new TextComponent(""));
                if (shiftDown)
                {
                    tooltip.addAll(inputItem.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.ADVANCED));
                }
                else
                {
                    tooltip.addAll(inputItem.getTooltipLines(Minecraft.getInstance().player, TooltipFlag.Default.NORMAL));
                }

                Iterator var32 = data.getModifiers(ModGearAttributes.JEWEL_SIZE, VaultGearData.Type.ALL_MODIFIERS).iterator();

                while (var32.hasNext())
                {
                    VaultGearAttributeInstance<Integer> sizeAttribute = (VaultGearAttributeInstance) var32.next();
                    if ((Integer) sizeAttribute.getValue() <= 10)
                    {
                        tooltip.add(new TextComponent(""));
                        tooltip.add((new TextComponent("Cannot cut size to lower than 10")).withStyle(ChatFormatting.RED));
                    }
                }
            }
            else
            {
                tooltip.add((new TextComponent("Requires Jewel")).withStyle(ChatFormatting.RED));
            }

            return tooltip;
        }
    }

    private static boolean hasGold(ItemStack goldInput, ItemStack goldInventory, Player player)
    {
        int goldMissing = goldInput.getCount();
        if (VaultJewelCuttingStationTileEntity.canMerge(goldInventory, goldInput))
        {
            if (goldInventory.getCount() >= goldMissing)
            {
                return true;
            }

            goldMissing -= goldInput.getCount();
        }

        Iterator it = player.getInventory().items.iterator();
        int toRemove = 0;
        while (it.hasNext())
        {
            if (goldMissing <= 0)
            {
                break;
            }

            ItemStack plStack = (ItemStack) it.next();
            if (VaultJewelCuttingStationTileEntity.canMerge(plStack, goldInput))
            {
                toRemove = Math.min(goldMissing, plStack.getCount());
                goldMissing -= toRemove;
            }
            else if (plStack.is(VCPRegistry.COIN_POUCH))
            {
                toRemove = Math.min(goldMissing, CoinPouchItem.getCoinCount(plStack, goldInput));
                goldMissing -= toRemove;
            }
        }

        if (CuriosApi.getCuriosHelper().findFirstCurio(player, VCPRegistry.COIN_POUCH).isPresent())
        {
            goldMissing -= CoinPouchItem.getCoinCount(CuriosApi.getCuriosHelper().findFirstCurio(player, VCPRegistry.COIN_POUCH).get().stack(), goldInput);
        }

        return goldMissing <= 0;
    }

    private static Component addTooltipDots(int amount, ChatFormatting formatting)
    {
        return (new TextComponent("⬢ ".repeat(Math.max(0, amount)))).withStyle(formatting);
    }
}
