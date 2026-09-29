package su.sergiusonesimus.recreate;

import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraftforge.common.util.EnumHelper;

import cpw.mods.fml.common.registry.GameRegistry;
import su.sergiusonesimus.recreate.content.AllSections;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.glue.SuperGlueItem;
import su.sergiusonesimus.recreate.content.contraptions.goggles.GogglesItem;
import su.sergiusonesimus.recreate.content.contraptions.wrench.WrenchItem;
import su.sergiusonesimus.recreate.content.logistics.item.filter.FilterItem;
import su.sergiusonesimus.recreate.foundation.item.CreateCreativeTab;

public class AllItems {

    public static ItemArmor.ArmorMaterial ARMOR_GOGGLES = EnumHelper
        .addArmorMaterial("GOGGLES", 0, new int[] { 0, 0, 0, 0 }, 0);

    public static Item goggles;
    public static Item wrench;
    public static Item super_glue;

    // Logistics

    public static Item filter;
    public static Item attribute_filter;

    public static final CreateCreativeTab BASE_CREATIVE_TAB = new CreateCreativeTab("base");

    public static void registerItems() {
        // Kinetics
        ReCreate.REGISTRATE.startSection(AllSections.KINETICS);

        int gogglesRenderID = ReCreate.proxy.registerArmorRenderID(ReCreate.ID + "/goggles_");

        goggles = new GogglesItem(ARMOR_GOGGLES, gogglesRenderID).setUnlocalizedName("goggles")
            .setMaxStackSize(1)
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        wrench = new WrenchItem().setUnlocalizedName("wrench")
            .setMaxStackSize(1)
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        super_glue = new SuperGlueItem().setUnlocalizedName("super_glue")
            .setTextureName(ReCreate.ID + ":super_glue")
            .setMaxStackSize(1)
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);

        registerItem(goggles);
        registerItem(wrench);
        registerItem(super_glue);

        // Logistics
        ReCreate.REGISTRATE.startSection(AllSections.LOGISTICS);

        filter = FilterItem.regular()
            .setUnlocalizedName("filter")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        attribute_filter = FilterItem.attribute()
            .setUnlocalizedName("attribute_filter")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);

        registerItem(filter);
        registerItem(attribute_filter);
    }

    private static void registerItem(Item item) {
        GameRegistry.registerItem(item, item.getUnlocalizedName(), ReCreate.ID);
        ReCreate.REGISTRATE.addToSection(item, ReCreate.REGISTRATE.currentSection());
    }

}
