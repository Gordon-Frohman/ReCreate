package su.sergiusonesimus.recreate.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.ClientProxy;
import su.sergiusonesimus.recreate.content.contraptions.base.CasingBlock.CasingType;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.item.BeltConnectorItem;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.BeltInventory;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.TransportedItemStack;
import su.sergiusonesimus.recreate.foundation.utility.Lang;
import su.sergiusonesimus.recreate.util.VecHelper;

public class BeltSlicer {

    public static class Feedback {

        int color = 0xffffff;
        AxisAlignedBB bb;
        String langKey;
        EnumChatFormatting formatting = EnumChatFormatting.WHITE;
    }

    public static boolean useWrench(World world, int x, int y, int z, EntityPlayer player, MovingObjectPosition hit,
        Feedback feedBack) {
        BeltTileEntity controllerTE = BeltHelper.getControllerTE(world, x, y, z);
        if (controllerTE == null || hit.typeOfHit != MovingObjectType.BLOCK) return false;
        BeltTileEntity segmentTE = BeltHelper.getSegmentTE(world, x, y, z);
        if (segmentTE == null) return false;
        Direction sideDir = Direction.from3DDataValue(hit.sideHit);
        if (segmentTE.casing != CasingType.NONE && sideDir != Direction.UP) return false;
        BeltPart part = segmentTE.partType;
        if (part == BeltPart.PULLEY && sideDir.getAxis() != Axis.Y) return false;

        int beltLength = controllerTE.beltLength;
        if (beltLength == 2) return false;

        Vec3 beltVec = BeltHelper.getBeltVector(world, x, y, z);
        ChunkCoordinates beltVector = new ChunkCoordinates(
            (int) beltVec.xCoord,
            (int) beltVec.yCoord,
            (int) beltVec.zCoord);
        List<ChunkCoordinates> beltChain = BeltBlock
            .getBeltChain(world, controllerTE.xCoord, controllerTE.yCoord, controllerTE.zCoord);
        boolean creative = player.capabilities.isCreativeMode;

        // Shorten from End
        if (hoveringEnd(world, x, y, z, hit)) {
            if (world.isRemote) return true;

            for (ChunkCoordinates blockPos : beltChain) {
                BeltTileEntity belt = BeltHelper.getSegmentTE(world, blockPos);
                if (belt == null) continue;
                belt.detachKinetics();
                belt.invalidateItemHandler();
                belt.beltLength = 0;
            }

            BeltInventory inventory = controllerTE.inventory;
            ChunkCoordinates next = new ChunkCoordinates(x, y, z);
            if (part == BeltPart.END) {
                next.posX -= beltVector.posX;
                next.posY -= beltVector.posY;
                next.posZ -= beltVector.posZ;
            } else {
                next.posX += beltVector.posX;
                next.posY += beltVector.posY;
                next.posZ += beltVector.posZ;
            }
            Block replacedBlock = world.getBlock(next.posX, next.posY, next.posZ);
            BeltTileEntity replacedTE = BeltHelper.getSegmentTE(world, next);
            Block replacementBlock = world.getBlock(x, y, z);
            int replacementMeta = world.getBlockMetadata(x, y, z);
            BeltPart replacedPart = replacedTE.partType;
            BeltTileEntity replacementTE = (BeltTileEntity) KineticTileEntity
                .switchToBlockState(world, next.posX, next.posY, next.posZ, replacementBlock, replacementMeta, true);
            replacementTE.partType = segmentTE.partType;
            replacementTE.slopeType = segmentTE.slopeType;
            replacementTE.casing = replacedTE.casing;
            world.setBlockToAir(x, y, z);
            world.removeTileEntity(x, y, z);
            world.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(replacementBlock));

            if (!creative && replacedBlock == AllBlocks.belt && replacedPart == BeltPart.PULLEY)
                player.inventory.addItemStackToInventory(new ItemStack(AllBlocks.shaft));

            // Eject overshooting items
            if (part == BeltPart.END && inventory != null) {
                List<TransportedItemStack> toEject = new ArrayList<>();
                for (TransportedItemStack transportedItemStack : inventory.getTransportedItems())
                    if (transportedItemStack.beltPosition > beltLength - 1) toEject.add(transportedItemStack);
                toEject.forEach(inventory::eject);
                toEject.forEach(inventory.getTransportedItems()::remove);
            }

            // Transfer items to new controller
            if (part == BeltPart.START && replacedTE != null && inventory != null) {
                controllerTE.inventory = null;
                replacedTE.inventory = null;
                replacedTE.setController(next);
                for (TransportedItemStack transportedItemStack : inventory.getTransportedItems()) {
                    transportedItemStack.beltPosition -= 1;
                    if (transportedItemStack.beltPosition <= 0) {
                        EntityItem entity = new EntityItem(
                            world,
                            x + .5f,
                            y + 11 / 16f,
                            z + .5f,
                            transportedItemStack.stack);
                        entity.motionX = entity.motionY = entity.motionZ = 0;
                        entity.delayBeforeCanPickup = 10;
                        entity.isAirBorne = true;
                        world.spawnEntityInWorld(entity);
                    } else replacedTE.getInventory()
                        .addItem(transportedItemStack);
                }
            }

            return true;
        }

        // Split in half
        int hitSegment = segmentTE.index;
        Vec3 centerOf = VecHelper.getCenterOf(hit.blockX, hit.blockY, hit.blockZ);
        Vec3 subtract = centerOf.subtract(hit.hitVec);
        boolean towardPositive = subtract
            .dotProduct(Vec3.createVectorHelper(beltVector.posX, beltVector.posY, beltVector.posZ)) > 0;
        ChunkCoordinates next = new ChunkCoordinates(x, y, z);
        if (!towardPositive) {
            next.posX -= beltVector.posX;
            next.posY -= beltVector.posY;
            next.posZ -= beltVector.posZ;
        } else {
            next.posX += beltVector.posX;
            next.posY += beltVector.posY;
            next.posZ += beltVector.posZ;
        }

        if (hitSegment == 0 || hitSegment == 1 && !towardPositive) return false;
        if (hitSegment == controllerTE.beltLength - 1 || hitSegment == controllerTE.beltLength - 2 && towardPositive)
            return false;

        // Look for shafts
        BeltTileEntity otherTE = (BeltTileEntity) world.getTileEntity(next.posX, next.posY, next.posZ);
        if (!creative) {
            int requiredShafts = 0;
            if (!segmentTE.hasPulley()) requiredShafts++;
            Block otherBlock = world.getBlock(next.posX, next.posY, next.posZ);
            if (otherBlock == AllBlocks.belt && otherTE.partType == BeltPart.MIDDLE) requiredShafts++;

            int amountRetrieved = 0;
            boolean beltFound = false;
            Search: while (true) {
                for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
                    if (amountRetrieved == requiredShafts && beltFound) break Search;

                    ItemStack itemstack = player.inventory.getStackInSlot(i);
                    if (itemstack == null) continue;
                    int count = itemstack.stackSize;

                    if (itemstack.getItem() instanceof BeltConnectorItem) {
                        if (!world.isRemote) itemstack.stackSize--;
                        beltFound = true;
                        continue;
                    }

                    if (itemstack.getItem() instanceof ItemBlock itemBlock
                        && Block.getBlockFromItem(itemBlock) == AllBlocks.shaft) {
                        int taken = Math.min(count, requiredShafts - amountRetrieved);
                        if (!world.isRemote) if (taken == count) player.inventory.setInventorySlotContents(i, null);
                        else itemstack.stackSize -= taken;
                        amountRetrieved += taken;
                    }
                }

                if (!world.isRemote)
                    player.inventory.addItemStackToInventory(new ItemStack(AllBlocks.shaft, amountRetrieved));
                return false;
            }
        }

        if (!world.isRemote) {
            for (ChunkCoordinates blockPos : beltChain) {
                BeltTileEntity belt = BeltHelper.getSegmentTE(world, blockPos);
                if (belt == null) continue;
                belt.detachKinetics();
                belt.invalidateItemHandler();
                belt.beltLength = 0;
            }

            BeltInventory inventory = controllerTE.inventory;
            segmentTE.partType = towardPositive ? BeltPart.END : BeltPart.START;
            otherTE.partType = towardPositive ? BeltPart.START : BeltPart.END;
            world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, Block.soundTypeCloth.getBreakSound(), 0.5F, 2.3F);

            // Transfer items to new controller
            BeltTileEntity newController = towardPositive ? BeltHelper.getSegmentTE(world, next) : segmentTE;
            if (newController != null && inventory != null) {
                newController.inventory = null;
                newController.setController(newController.xCoord, newController.yCoord, newController.zCoord);
                for (Iterator<TransportedItemStack> iterator = inventory.getTransportedItems()
                    .iterator(); iterator.hasNext();) {
                    TransportedItemStack transportedItemStack = iterator.next();
                    float newPosition = transportedItemStack.beltPosition - hitSegment - (towardPositive ? 1 : 0);
                    if (newPosition <= 0) continue;
                    transportedItemStack.beltPosition = newPosition;
                    iterator.remove();
                    newController.getInventory()
                        .addItem(transportedItemStack);
                }
            }
        }

        return true;
    }

    public static boolean useConnector(World world, int x, int y, int z, EntityPlayer player, MovingObjectPosition hit,
        Feedback feedBack) {
        BeltTileEntity controllerTE = BeltHelper.getControllerTE(world, x, y, z);
        if (controllerTE == null) return false;
        BeltTileEntity segmentTE = BeltHelper.getSegmentTE(world, x, y, z);
        BeltBlock belt = (BeltBlock) world.getBlock(x, y, z);
        int beltMeta = world.getBlockMetadata(x, y, z);

        int beltLength = controllerTE.beltLength;
        if (beltLength == BeltConnectorItem.maxLength()) return false;

        Vec3 beltVec3 = BeltHelper.getBeltVector(world, x, y, z);
        ChunkCoordinates beltVector = new ChunkCoordinates(
            (int) beltVec3.xCoord,
            (int) beltVec3.yCoord,
            (int) beltVec3.zCoord);
        BeltPart part = segmentTE.partType;
        BeltSlope slope = segmentTE.slopeType;
        Direction facing = belt.getDirection(beltMeta);
        List<ChunkCoordinates> beltChain = BeltBlock
            .getBeltChain(world, controllerTE.xCoord, controllerTE.yCoord, controllerTE.zCoord);
        boolean creative = player.capabilities.isCreativeMode;

        if (!hoveringEnd(world, x, y, z, hit)) return false;

        ChunkCoordinates next = new ChunkCoordinates(x, y, z);
        if (part == BeltPart.START) {
            next.posX -= beltVector.posX;
            next.posY -= beltVector.posY;
            next.posZ -= beltVector.posZ;
        } else {
            next.posX += beltVector.posX;
            next.posY += beltVector.posY;
            next.posZ += beltVector.posZ;
        }
        BeltTileEntity mergedController = null;
        int mergedBeltLength = 0;

        // Merge Belts / Extend at End
        Block nextBlock = world.getBlock(next.posX, next.posY, next.posZ);
        BeltTileEntity nextSegmentTE = BeltHelper.getSegmentTE(world, next);
        if (!nextBlock.getMaterial()
            .isReplaceable()) {
            if (!(nextBlock instanceof BeltBlock nextBelt)) return false;
            if (!beltStatesCompatible(segmentTE, nextSegmentTE)) return false;
            int nextMeta = world.getBlockMetadata(next.posX, next.posY, next.posZ);

            mergedController = BeltHelper.getControllerTE(world, next);
            if (mergedController == null) return false;
            if (mergedController.beltLength + beltLength > BeltConnectorItem.maxLength()) return false;

            mergedBeltLength = mergedController.beltLength;

            if (!world.isRemote) {
                boolean flipBelt = facing != nextBelt.getDirection(nextMeta);
                Integer color = controllerTE.color;
                for (ChunkCoordinates blockPos : BeltBlock
                    .getBeltChain(world, mergedController.xCoord, mergedController.yCoord, mergedController.zCoord)) {
                    BeltTileEntity currentTE = BeltHelper.getSegmentTE(world, blockPos);
                    if (currentTE == null) continue;
                    currentTE.detachKinetics();
                    currentTE.invalidateItemHandler();
                    currentTE.beltLength = 0;
                    currentTE.color = color;
                    if (flipBelt) world.setBlockMetadataWithNotify(
                        blockPos.posX,
                        blockPos.posY,
                        blockPos.posZ,
                        flipBelt(world, blockPos.posX, blockPos.posY, blockPos.posZ),
                        1 | 2);
                }

                // Reverse items
                if (flipBelt && mergedController.inventory != null) {
                    List<TransportedItemStack> transportedItems = mergedController.inventory.getTransportedItems();
                    for (TransportedItemStack transportedItemStack : transportedItems) {
                        transportedItemStack.beltPosition = mergedBeltLength - transportedItemStack.beltPosition;
                        transportedItemStack.prevBeltPosition = mergedBeltLength
                            - transportedItemStack.prevBeltPosition;
                    }
                }
            }
        }

        if (!world.isRemote) {
            for (ChunkCoordinates blockPos : beltChain) {
                BeltTileEntity currentTE = BeltHelper.getSegmentTE(world, blockPos);
                if (currentTE == null) continue;
                currentTE.detachKinetics();
                currentTE.invalidateItemHandler();
                currentTE.beltLength = 0;
            }

            BeltInventory inventory = controllerTE.inventory;
            segmentTE = (BeltTileEntity) KineticTileEntity
                .switchToBlockState(world, x, y, z, AllBlocks.belt, segmentTE.getBlockMetadata(), true);
            segmentTE.partType = BeltPart.MIDDLE;

            if (mergedController == null) {
                // Attach at end
                nextSegmentTE = (BeltTileEntity) KineticTileEntity.switchToBlockState(
                    world,
                    next.posX,
                    next.posY,
                    next.posZ,
                    AllBlocks.belt,
                    segmentTE.getBlockMetadata(),
                    true);
                nextSegmentTE.partType = part;
                nextSegmentTE.slopeType = slope;
                nextSegmentTE.casing = CasingType.NONE;
                nextSegmentTE.notifyUpdate();
                if (nextSegmentTE != null) nextSegmentTE.color = controllerTE.color;
                world.playSoundEffect(x + 0.5D, y + 0.5D, z + 0.5D, Block.soundTypeCloth.getBreakSound(), 0.5F, 1F);

                // Transfer items to new controller
                if (part == BeltPart.START && nextSegmentTE != null && inventory != null) {
                    nextSegmentTE.setController(next);
                    for (TransportedItemStack transportedItemStack : inventory.getTransportedItems()) {
                        transportedItemStack.beltPosition += 1;
                        nextSegmentTE.getInventory()
                            .addItem(transportedItemStack);
                    }
                }

            } else {
                // Merge with other
                BeltInventory mergedInventory = mergedController.inventory;
                world.playSoundEffect(x, y, z, Block.soundTypeCloth.getBreakSound(), 0.5F, 1.3F);
                // TODO May also have to add a line here to change casing type
                KineticTileEntity.switchToBlockState(world, next.posX, next.posY, next.posZ, belt, beltMeta, true);
                nextSegmentTE.partType = BeltPart.MIDDLE;

                if (!creative) {
                    player.inventory.addItemStackToInventory(new ItemStack(AllBlocks.shaft, 2));
                    player.inventory.addItemStackToInventory(new ItemStack(AllBlocks.belt));
                }

                // Transfer items to other controller
                int searchX = controllerTE.xCoord;
                int searchY = controllerTE.yCoord;
                int searchZ = controllerTE.zCoord;
                for (int i = 0; i < 10000; i++) {
                    Block currentBlock = world.getBlock(searchX, searchY, searchZ);
                    if (currentBlock != AllBlocks.belt) break;
                    BeltTileEntity currentTE = (BeltTileEntity) world.getTileEntity(searchX, searchY, searchZ);
                    if (currentTE.partType != BeltPart.START) {
                        searchX -= beltVector.posX;
                        searchY -= beltVector.posY;
                        searchZ -= beltVector.posZ;
                        continue;
                    }

                    BeltTileEntity newController = BeltHelper.getSegmentTE(world, searchX, searchY, searchZ);

                    if (newController != controllerTE && inventory != null) {
                        newController.setController(searchX, searchY, searchZ);
                        controllerTE.inventory = null;
                        for (TransportedItemStack transportedItemStack : inventory.getTransportedItems()) {
                            transportedItemStack.beltPosition += mergedBeltLength;
                            newController.getInventory()
                                .addItem(transportedItemStack);
                        }
                    }

                    if (newController != mergedController && mergedInventory != null) {
                        newController.setController(searchX, searchY, searchZ);
                        mergedController.inventory = null;
                        for (TransportedItemStack transportedItemStack : mergedInventory.getTransportedItems()) {
                            if (newController == controllerTE) transportedItemStack.beltPosition += beltLength;
                            newController.getInventory()
                                .addItem(transportedItemStack);
                        }
                    }

                    break;
                }
            }
        }
        return true;
    }

    static boolean beltStatesCompatible(BeltTileEntity belt, BeltTileEntity nextBelt) {
        BeltBlock block = (BeltBlock) belt.getBlockType();
        BeltBlock nextBlock = (BeltBlock) nextBelt.getBlockType();
        int meta = belt.getBlockMetadata();
        int nextMeta = nextBelt.getBlockMetadata();
        Direction facing1 = block.getDirection(meta);
        Direction facing2 = nextBlock.getDirection(nextMeta);
        BeltSlope slope = belt.slopeType;
        BeltSlope nextSlope = nextBelt.slopeType;

        switch (slope) {
            case UPWARD:
                if (nextSlope == BeltSlope.DOWNWARD) return facing1 == facing2.getOpposite();
                return nextSlope == slope && facing1 == facing2;
            case DOWNWARD:
                if (nextSlope == BeltSlope.UPWARD) return facing1 == facing2.getOpposite();
                return nextSlope == slope && facing1 == facing2;
            default:
                return nextSlope == slope && facing2.getAxis() == facing1.getAxis();
        }
    }

    static int flipBelt(World world, int x, int y, int z) {
        if (!(world.getBlock(x, y, z) instanceof BeltBlock belt)) return -1;
        int meta = world.getBlockMetadata(x, y, z);
        BeltTileEntity te = (BeltTileEntity) world.getTileEntity(x, y, z);
        Direction facing = belt.getDirection(meta);
        BeltSlope slope = te.slopeType;
        BeltPart part = te.partType;

        if (slope == BeltSlope.UPWARD) te.slopeType = BeltSlope.DOWNWARD;
        else if (slope == BeltSlope.DOWNWARD) te.slopeType = BeltSlope.UPWARD;

        if (part == BeltPart.END) te.partType = BeltPart.START;
        else if (part == BeltPart.START) te.partType = BeltPart.END;

        meta = belt.getMetaFromDirection(facing.getOpposite());
        world.setBlockMetadataWithNotify(x, y, z, meta, 1 | 2);
        return meta;
    }

    static boolean hoveringEnd(World world, int x, int y, int z, MovingObjectPosition hit) {
        BeltTileEntity te = (BeltTileEntity) world.getTileEntity(x, y, z);
        BeltPart part = te.partType;
        if (part == BeltPart.MIDDLE || part == BeltPart.PULLEY) return false;

        Vec3 beltVector = BeltHelper.getBeltVector(world, x, y, z);
        Vec3 centerOf = VecHelper.getCenterOf(0, 0, 0);
        Vec3 subtract = centerOf.subtract(hit.hitVec);

        return subtract.dotProduct(beltVector) > 0 == (part == BeltPart.END);
    }

    @SideOnly(Side.CLIENT)
    public static void tickHoveringInformation() {
        Minecraft mc = Minecraft.getMinecraft();
        MovingObjectPosition target = mc.objectMouseOver;
        if (target == null || target.typeOfHit != MovingObjectType.BLOCK) return;

        WorldClient world = mc.theWorld;
        int posX = target.blockX;
        int posY = target.blockY;
        int posZ = target.blockZ;
        Block block = world.getBlock(posX, posY, posZ);
        ItemStack held = mc.thePlayer.getHeldItem();
        // TODO ItemStack heldOffHand = mc.thePlayer.getItemInHand(InteractionHand.OFF_HAND);

        if (mc.thePlayer.isSneaking()) return;
        if (block != AllBlocks.belt) return;

        Feedback feedback = new Feedback();

        // TODO: Populate feedback in the methods for clientside
        if ((held != null && held.getItem() == AllItems.wrench) /* TODO || AllItems.WRENCH.isIn(heldOffHand) */)
            useWrench(world, posX, posY, posZ, mc.thePlayer, target, feedback);
        else if (held != null
            && held.getItem() instanceof BeltConnectorItem /* TODO || AllItems.BELT_CONNECTOR.isIn(heldOffHand) */)
            useConnector(world, posX, posY, posZ, mc.thePlayer, target, feedback);
        else return;

        if (feedback.langKey != null) {
            ChatComponentTranslation message = Lang.translate(feedback.langKey);
            message.getChatStyle()
                .setColor(feedback.formatting);
            mc.thePlayer.addChatMessage(message);
        } else mc.thePlayer.addChatMessage(new ChatComponentText(""));

        if (feedback.bb != null) ClientProxy.OUTLINER.chaseAABB("BeltSlicer", feedback.bb)
            .lineWidth(1 / 16f)
            .colored(feedback.color);
    }

}
