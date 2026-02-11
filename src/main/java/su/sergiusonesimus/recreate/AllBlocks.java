package su.sergiusonesimus.recreate;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;

import cpw.mods.fml.common.registry.GameRegistry;
import su.sergiusonesimus.recreate.AllTags.AllBlockTags;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.motor.CreativeMotorItemBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.MechanicalBearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.SailBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.bearing.WindmillBearingBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis.LinearChassisBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis.LinearChassisItemBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis.RadialChassisBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.MechanicalPistonHeadBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.piston.PistonExtensionPoleBlock;
import su.sergiusonesimus.recreate.content.contraptions.components.waterwheel.WaterWheelBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.cogwheel.CogWheelItemBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.ClutchBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.encased.GearshiftBlock;
import su.sergiusonesimus.recreate.content.contraptions.relays.gearbox.GearboxBlock;
import su.sergiusonesimus.recreate.foundation.block.BlockStressDefaults;

public class AllBlocks {

    // Kinetics
    public static Block shaft;
    public static Block cogwheel;
    public static Block large_cogwheel;

    public static Block gearbox;
    public static ClutchBlock unpowered_clutch;
    public static ClutchBlock powered_clutch;
    public static GearshiftBlock unpowered_gearshift;
    public static GearshiftBlock powered_gearshift;

    public static Block creative_motor;
    public static Block waterwheel;

    // Fluids

    // Contraptions
    public static Block mechanical_piston;
    public static Block sticky_mechanical_piston;
    public static Block piston_extension_pole;
    public static Block mechanical_piston_head;

    public static Block windmill_bearing;
    public static Block mechanical_bearing;

    public static Block linear_chassis;
    public static Block radial_chassis;

    public static Block sail_frame;
    public static Block[] sails = new Block[16];

    public static void registerBlocks() {

        // Kinetics

        shaft = new ShaftBlock(Material.rock).setBlockName("shaft")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(shaft);
        BlockStressDefaults.setNoImpact(shaft);

        cogwheel = CogWheelBlock.small(Material.wood)
            .setBlockName("cogwheel")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(cogwheel, CogWheelItemBlock.class);
        BlockStressDefaults.setNoImpact(cogwheel);

        large_cogwheel = CogWheelBlock.large(Material.wood)
            .setBlockName("large_cogwheel")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(large_cogwheel, CogWheelItemBlock.class);
        BlockStressDefaults.setNoImpact(large_cogwheel);

        gearbox = new GearboxBlock(Material.wood).setBlockName("gearbox")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(gearbox);
        BlockStressDefaults.setNoImpact(gearbox);

        unpowered_clutch = (ClutchBlock) new ClutchBlock(Material.wood, false).setBlockName("clutch")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        GameRegistry.registerBlock(unpowered_clutch, ItemBlock.class, "tile.unpowered_clutch");
        BlockStressDefaults.setNoImpact(unpowered_clutch);
        powered_clutch = (ClutchBlock) new ClutchBlock(Material.wood, true).setBlockName("clutch");
        GameRegistry.registerBlock(powered_clutch, ItemBlock.class, "tile.powered_clutch");
        BlockStressDefaults.setNoImpact(powered_clutch);

        unpowered_gearshift = (GearshiftBlock) new GearshiftBlock(Material.wood, false).setBlockName("gearshift")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        GameRegistry.registerBlock(unpowered_gearshift, ItemBlock.class, "tile.unpowered_gearshift");
        BlockStressDefaults.setNoImpact(unpowered_gearshift);
        powered_gearshift = (GearshiftBlock) new GearshiftBlock(Material.wood, true).setBlockName("gearshift");
        GameRegistry.registerBlock(powered_gearshift, ItemBlock.class, "tile.powered_gearshift");
        BlockStressDefaults.setNoImpact(powered_gearshift);

        creative_motor = new CreativeMotorBlock(Material.rock).setBlockName("creative_motor")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(creative_motor, CreativeMotorItemBlock.class);
        BlockStressDefaults.setCapacity(creative_motor, 16384.0);

        waterwheel = new WaterWheelBlock(Material.wood).setBlockName("water_wheel")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(waterwheel);
        BlockStressDefaults.setCapacity(waterwheel, 32.0);

        // Fluids

        // Contraptions

        mechanical_piston = MechanicalPistonBlock.normal(Material.wood)
            .setBlockName("mechanical_piston")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(mechanical_piston);
        BlockStressDefaults.setImpact(mechanical_piston, 4.0);
        sticky_mechanical_piston = MechanicalPistonBlock.sticky(Material.wood)
            .setBlockName("sticky_mechanical_piston")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(sticky_mechanical_piston);
        BlockStressDefaults.setImpact(sticky_mechanical_piston, 4.0);

        piston_extension_pole = new PistonExtensionPoleBlock(Material.wood).setBlockName("piston_extension_pole")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(piston_extension_pole);

        mechanical_piston_head = new MechanicalPistonHeadBlock(Material.wood).setBlockName("mechanical_piston_head");
        registerMyBlock(mechanical_piston_head);

        windmill_bearing = new WindmillBearingBlock(Material.wood).setBlockName("windmill_bearing")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(windmill_bearing);
        BlockStressDefaults.setCapacity(windmill_bearing, 512.0);

        mechanical_bearing = new MechanicalBearingBlock(Material.wood).setBlockName("mechanical_bearing")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(mechanical_bearing);
        BlockStressDefaults.setImpact(mechanical_bearing, 4.0);

        linear_chassis = new LinearChassisBlock(Material.wood).setBlockName("linear_chassis")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(linear_chassis, LinearChassisItemBlock.class);

        radial_chassis = new RadialChassisBlock(Material.wood).setBlockName("radial_chassis")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        registerMyBlock(radial_chassis);

        sail_frame = SailBlock.frame()
            .setBlockName("sail_frame")
            .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
        AllBlockTags.SAILS.add(sail_frame);
        AllBlockTags.WINDMILL_SAILS.add(sail_frame);
        AllBlockTags.FAN_TRANSPARENT.add(sail_frame);
        registerMyBlock(sail_frame);
        for (int i = 0; i < ItemDye.field_150921_b.length; i++) {
            sails[i] = SailBlock.withCanvas(i)
                .setBlockName(ItemDye.field_150921_b[i] + "_sail")
                .setCreativeTab(AllItems.BASE_CREATIVE_TAB);
            AllBlockTags.SAILS.add(sails[i]);
            AllBlockTags.WINDMILL_SAILS.add(sails[i]);
            registerMyBlock(sails[i]);
        }
    }

    @SuppressWarnings("unused")
    private static void registerMyBlock(Block block, Class<? extends ItemBlock> pickup, BlockSlab singleSlab,
        BlockSlab doubleSlab, boolean isDouble) {
        GameRegistry.registerBlock(block, pickup, block.getUnlocalizedName(), singleSlab, doubleSlab, isDouble);

    }

    @SuppressWarnings("unused")
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
