package su.sergiusonesimus.recreate.foundation.item;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.Language;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.StatCollector;

import com.google.common.base.Strings;

import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.content.AllSections;
import su.sergiusonesimus.recreate.content.contraptions.goggles.IHaveGoggleInformation;
import su.sergiusonesimus.recreate.foundation.item.ItemDescription.Palette;
import su.sergiusonesimus.recreate.foundation.utility.Couple;
import su.sergiusonesimus.recreate.foundation.utility.FontHelper;
import su.sergiusonesimus.recreate.foundation.utility.Lang;
import su.sergiusonesimus.recreate.util.TextHelper;

public class TooltipHelper {

    public static final int maxWidthPerLine = 200;
    public static final Map<String, ItemDescription> cachedTooltips = new HashMap<>();
    public static Language cachedLanguage;
    private static boolean gogglesMode;
    private static final Map<ItemStack, Supplier<String>> tooltipReferrals = new HashMap<>();

    public static IChatComponent holdShift(Palette color, boolean highlighted) {
        IChatComponent keyShift = Lang.translate("tooltip.keyShift");
        keyShift.getChatStyle()
            .setColor(EnumChatFormatting.GRAY);
        IChatComponent holdForDescription = Lang.translate("tooltip.holdForDescription", keyShift);
        holdForDescription.getChatStyle()
            .setColor(EnumChatFormatting.DARK_GRAY);
        return holdForDescription;
    }

    public static void addHint(List<IChatComponent> tooltip, String hintKey, Object... messageParams) {
        IChatComponent spacing = IHaveGoggleInformation.componentSpacing;
        IChatComponent spacingCopy = spacing.createCopy()
            .appendSibling(Lang.translate(hintKey + ".title"));
        spacingCopy.getChatStyle()
            .setColor(EnumChatFormatting.GOLD);
        tooltip.add(spacingCopy);
        IChatComponent hint = Lang.translate(hintKey);
        List<IChatComponent> cutComponent = TooltipHelper
            .cutTextComponent(hint, EnumChatFormatting.GRAY, EnumChatFormatting.WHITE);
        for (IChatComponent component : cutComponent) {
            tooltip.add(
                spacing.createCopy()
                    .appendSibling(component));
        }
    }

    public static void referTo(Item item, int meta, Supplier<? extends Item> itemWithTooltip) {
        referTo(
            new ItemStack(item, 1, meta),
            itemWithTooltip.get()
                .getUnlocalizedName());
    }

    public static void referTo(Block block, int meta, Supplier<? extends Item> itemWithTooltip) {
        referTo(
            new ItemStack(block, 1, meta),
            itemWithTooltip.get()
                .getUnlocalizedName());
    }

    public static void referTo(Item item, String string) {
        referTo(new ItemStack(item), string);
    }

    public static void referTo(Block block, String string) {
        referTo(new ItemStack(block), string);
    }

    public static void referTo(ItemStack item, String string) {
        tooltipReferrals.put(item, () -> string);
    }

    @Deprecated
    public static List<String> cutString(IChatComponent s, EnumChatFormatting defaultColor,
        EnumChatFormatting highlightColor) {
        return cutString(s.getUnformattedText(), defaultColor, highlightColor, 0);
    }

    @Deprecated
    public static List<String> cutString(String s, EnumChatFormatting defaultColor, EnumChatFormatting highlightColor,
        int indent) {
        // Apply markup
        String markedUp = s.replaceAll("_([^_]+)_", highlightColor + "$1" + defaultColor);

        // Split words
        List<String> words = new LinkedList<>();
        BreakIterator iterator = BreakIterator.getLineInstance(TextHelper.getCurrentLocale());
        iterator.setText(markedUp);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String word = markedUp.substring(start, end);
            words.add(word);
        }

        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        List<String> lines = FontHelper.cutString(fontRenderer, markedUp, maxWidthPerLine);

        // Format
        String lineStart = Strings.repeat(" ", indent);
        List<String> formattedLines = new ArrayList<>(lines.size());
        String format = defaultColor.toString();
        for (String line : lines) {
            String formattedLine = format + lineStart + line;
            formattedLines.add(formattedLine);
        }
        return formattedLines;
    }

    public static List<IChatComponent> cutStringTextComponent(String c, EnumChatFormatting defaultColor,
        EnumChatFormatting highlightColor) {
        return cutTextComponent(new ChatComponentText(c), defaultColor, highlightColor, 0);
    }

    public static List<IChatComponent> cutTextComponent(IChatComponent c, EnumChatFormatting defaultColor,
        EnumChatFormatting highlightColor) {
        return cutTextComponent(c, defaultColor, highlightColor, 0);
    }

    public static List<IChatComponent> cutStringTextComponent(String c, EnumChatFormatting defaultColor,
        EnumChatFormatting highlightColor, int indent) {
        return cutTextComponent(new ChatComponentText(c), defaultColor, highlightColor, indent);
    }

    public static List<IChatComponent> cutTextComponent(IChatComponent c, EnumChatFormatting defaultColor,
        EnumChatFormatting highlightColor, int indent) {
        String s = c.getUnformattedText();

        // Apply markup
        String markedUp = s;// .replaceAll("_([^_]+)_", highlightColor + "$1" + defaultColor);

        // Split words
        List<String> words = new LinkedList<>();
        BreakIterator iterator = BreakIterator.getLineInstance(TextHelper.getCurrentLocale());
        iterator.setText(markedUp);
        int start = iterator.first();
        for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator.next()) {
            String word = markedUp.substring(start, end);
            words.add(word);
        }

        // Apply hard wrap
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        List<String> lines = new LinkedList<>();
        StringBuilder currentLine = new StringBuilder();
        int width = 0;
        for (String word : words) {
            int newWidth = fontRenderer.getStringWidth(word.replaceAll("_", ""));
            if (width + newWidth > maxWidthPerLine) {
                if (width > 0) {
                    String line = currentLine.toString();
                    lines.add(line);
                    currentLine = new StringBuilder();
                    width = 0;
                } else {
                    lines.add(word);
                    continue;
                }
            }
            currentLine.append(word);
            width += newWidth;
        }
        if (width > 0) {
            lines.add(currentLine.toString());
        }

        // Format
        IChatComponent lineStart = new ChatComponentText(Strings.repeat(" ", indent));
        lineStart.getChatStyle()
            .setColor(defaultColor);
        List<IChatComponent> formattedLines = new ArrayList<>(lines.size());
        Couple<EnumChatFormatting> f = Couple.create(highlightColor, defaultColor);

        boolean currentlyHighlighted = false;
        for (String string : lines) {
            IChatComponent currentComponent = lineStart.createCopy();
            String[] split = string.split("_");
            for (String part : split) {
                IChatComponent partComponent = new ChatComponentText(part);
                partComponent.getChatStyle()
                    .setColor(f.get(currentlyHighlighted));
                currentComponent.appendSibling(partComponent);
                currentlyHighlighted = !currentlyHighlighted;
            }

            formattedLines.add(currentComponent);
            currentlyHighlighted = !currentlyHighlighted;
        }

        return formattedLines;
    }

    private static void checkLocale() {
        Language currentLanguage = Minecraft.getMinecraft()
            .getLanguageManager()
            .getCurrentLanguage();
        if (cachedLanguage != currentLanguage) {
            cachedTooltips.clear();
            cachedLanguage = currentLanguage;
        }
    }

    public static boolean hasTooltip(ItemStack stack, EntityPlayer player) {
        checkLocale();

        ItemStack helmet = player.getEquipmentInSlot(4);
        boolean hasGlasses = helmet != null && helmet.getItem() == AllItems.goggles;

        if (hasGlasses != gogglesMode) {
            gogglesMode = hasGlasses;
            cachedTooltips.clear();
        }

        String key = getTooltipTranslationKey(stack);
        if (cachedTooltips.containsKey(key)) {
            ItemDescription description = cachedTooltips.get(key);
            return description != ItemDescription.MISSING;
        }
        return findTooltip(stack);
    }

    public static ItemDescription getTooltip(ItemStack stack) {
        checkLocale();
        String key = getTooltipTranslationKey(stack);
        if (cachedTooltips.containsKey(key)) {
            ItemDescription itemDescription = cachedTooltips.get(key);
            if (itemDescription != ItemDescription.MISSING) return itemDescription;
        }
        return null;
    }

    private static boolean findTooltip(ItemStack stack) {
        String key = getTooltipTranslationKey(stack);
        if (StatCollector.canTranslate(key)) {
            cachedTooltips.put(key, buildToolTip(key, stack));
            return true;
        }
        cachedTooltips.put(key, ItemDescription.MISSING);
        return false;
    }

    private static ItemDescription buildToolTip(String translationKey, ItemStack stack) {
        AllSections module = AllSections.of(stack);
        ItemDescription tooltip = new ItemDescription(module.getTooltipPalette());
        String summaryKey = translationKey + ".summary";

        // Summary
        if (StatCollector.canTranslate(summaryKey))
            tooltip = tooltip.withSummary(new ChatComponentText(StatCollector.translateToLocal(summaryKey)));

        // Behaviours
        for (int i = 1; i < 100; i++) {
            String conditionKey = translationKey + ".condition" + i;
            String behaviourKey = translationKey + ".behaviour" + i;
            if (!StatCollector.canTranslate(conditionKey)) break;
            if (i == 1) tooltip.getLinesOnShift()
                .add(new ChatComponentText(""));
            tooltip.withBehaviour(
                StatCollector.translateToLocal(conditionKey),
                StatCollector.translateToLocal(behaviourKey));
        }

        // Controls
        for (int i = 1; i < 100; i++) {
            String controlKey = translationKey + ".control" + i;
            String actionKey = translationKey + ".action" + i;
            if (!StatCollector.canTranslate(controlKey)) break;
            tooltip.withControl(StatCollector.translateToLocal(controlKey), StatCollector.translateToLocal(actionKey));
        }

        return tooltip.createTabs();
    }

    @SuppressWarnings("unlikely-arg-type")
    public static String getTooltipTranslationKey(ItemStack stack) {
        Item item = stack.getItem();
        if (tooltipReferrals.containsKey(item)) return tooltipReferrals.get(item)
            .get() + ".tooltip";
        return item.getUnlocalizedName(stack) + ".tooltip";
    }

}
