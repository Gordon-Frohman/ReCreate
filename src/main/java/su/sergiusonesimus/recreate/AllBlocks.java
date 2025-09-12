package su.sergiusonesimus.recreate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;

import cpw.mods.fml.common.registry.GameRegistry;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorItemBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.waterwheel.WaterWheelBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelItemBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.ClutchBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.GearshiftBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxBlock;
import su.sergiusonesimus.recreate.foundation.block.BlockStressDefaults;

public class AllBlocks {

    public static Block shaft;
    public static Block creative_motor;
    public static Block cogwheel;
    public static Block large_cogwheel;
    public static Block mechanical_bearing;
    public static GearshiftBlock unpowered_gearshift;
    public static GearshiftBlock powered_gearshift;
    public static ClutchBlock unpowered_clutch;
    public static ClutchBlock powered_clutch;
    public static Block gearbox;
    public static Block waterwheel;

    public static void registerBlocks() {
        shaft = new ShaftBlock(Material.rock).setBlockName("shaft")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        creative_motor = new CreativeMotorBlock(Material.rock).setBlockName("creative_motor")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        cogwheel = CogWheelBlock.small(Material.wood)
            .setBlockName("cogwheel")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        large_cogwheel = CogWheelBlock.large(Material.wood)
            .setBlockName("large_cogwheel")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        mechanical_bearing = new MechanicalBearingBlock(Material.wood).setBlockName("mechanical_bearing")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        unpowered_gearshift = (GearshiftBlock) new GearshiftBlock(Material.wood, false).setBlockName("gearshift")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        powered_gearshift = (GearshiftBlock) new GearshiftBlock(Material.wood, true).setBlockName("gearshift");
        unpowered_clutch = (ClutchBlock) new ClutchBlock(Material.wood, false).setBlockName("clutch")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        powered_clutch = (ClutchBlock) new ClutchBlock(Material.wood, true).setBlockName("clutch");
        gearbox = new GearboxBlock(Material.wood).setBlockName("gearbox")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        waterwheel = new WaterWheelBlock(Material.wood).setBlockName("water_wheel")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);

        registerMyBlock(shaft);
        registerMyBlock(creative_motor, CreativeMotorItemBlock.class);
        BlockStressDefaults.setCapacity(creative_motor, 16384.0);
        registerMyBlock(cogwheel, CogWheelItemBlock.class);
        registerMyBlock(large_cogwheel, CogWheelItemBlock.class);
        registerMyBlock(mechanical_bearing);
        GameRegistry.registerBlock(unpowered_gearshift, ItemBlock.class, "tile.unpowered_gearshift");
        GameRegistry.registerBlock(powered_gearshift, ItemBlock.class, "tile.powered_gearshift");
        GameRegistry.registerBlock(unpowered_clutch, ItemBlock.class, "tile.unpowered_clutch");
        GameRegistry.registerBlock(powered_clutch, ItemBlock.class, "tile.powered_clutch");
        registerMyBlock(gearbox);
        registerMyBlock(waterwheel);
        BlockStressDefaults.setCapacity(waterwheel, 32.0);
    }

    private static void registerMyBlock(Block block, Class<? extends ItemBlock> pickup, BlockSlab singleSlab,
        BlockSlab doubleSlab, boolean isDouble) {
        GameRegistry.registerBlock(block, pickup, block.getUnlocalizedName(), singleSlab, doubleSlab, isDouble);

    }

    private static void registerMyBlock(Block block, Class<? extends ItemBlock> pickup, Block blockAgain,
        String[] names) {
        GameRegistry.registerBlock(block, pickup, block.getUnlocalizedName(), blockAgain, names);
    }

    private static void registerMyBlock(Block block, Class<? extends ItemBlock> pickup) {
        GameRegistry.registerBlock(block, pickup, block.getUnlocalizedName());
    }

    private static void registerMyBlock(Block block) {
        GameRegistry.registerBlock(block, ItemBlock.class, block.getUnlocalizedName());
    }

}
