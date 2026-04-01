package su.sergiusonesimus.recreate.content.contraptions.relays.belt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import org.apache.commons.lang3.mutable.MutableBoolean;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.metaworlds.util.Rotation;
import su.sergiusonesimus.recreate.AllBlocks;
import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.base.HorizontalKineticBlock;
import su.sergiusonesimus.recreate.content.contraptions.processing.EmptyingByBasin;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltSlicer.Feedback;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.BeltTileEntity.CasingType;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.TransportedItemStackHandlerBehaviour.TransportedResult;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.item.BeltConnectorItem;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.BeltInventory;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.BeltMovementHandler.TransportedEntityInfo;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftBlock;
import su.sergiusonesimus.recreate.foundation.block.ITE;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;

public class BeltBlock extends HorizontalKineticBlock
    implements ITE<BeltTileEntity>/* TODO , ISpecialBlockItemRequirement */ {

    public static IIcon belt;
    public static IIcon beltScroll;
    public static IIcon beltScrollDiagonal;
    public static IIcon[] coloredScrolls = new IIcon[16];
    public static IIcon[] coloredScrollsDiagonal = new IIcon[16];

    private static final String ASSET_PATH = "textures/blocks/";
    private static final String COLORED_ASSET_PATH = ASSET_PATH + "belt/";
    public static ResourceLocation beltScrollLocation;
    public static ResourceLocation beltScrollDiagonalLocation;
    public static ResourceLocation[] coloredScrollLocations = new ResourceLocation[16];
    public static ResourceLocation[] coloredScrollDiagonalLocations = new ResourceLocation[16];

    public BeltBlock(Material material) {
        super(material);
        this.setStepSound(Block.soundTypeCloth);
        this.setHardness(0.8F);
    }

    /**
     * Returns a bounding box from the pool of bounding boxes (this means this box can change after the pool has been
     * cleared to be reused)
     */
    public AxisAlignedBB getCollisionBoundingBoxFromPool(World worldIn, int x, int y, int z) {
        this.setBlockBoundsBasedOnState(worldIn, x, y, z);
        return super.getCollisionBoundingBoxFromPool(worldIn, x, y, z);
    }

    @Override
    public void setBlockBoundsBasedOnState(IBlockAccess worldIn, int x, int y, int z) {
        if (worldIn == null || !(worldIn.getTileEntity(x, y, z) instanceof BeltTileEntity te)) return;
        int meta = worldIn.getBlockMetadata(x, y, z);
        Axis dirAxis = this.getDirection(meta)
            .getAxis();
        double d = 1d / 16d;
        double lengthMin, lengthMax, widthMin, widthMax;

        switch (te.slopeType) {
            case HORIZONTAL:
                minY = 3 * d;
                maxY = 1d - 3 * d;
                lengthMin = 0;
                lengthMax = 1;
                widthMin = d;
                widthMax = 1d - d;
                if (dirAxis == Axis.X) {
                    minX = lengthMin;
                    maxX = lengthMax;
                    minZ = widthMin;
                    maxZ = widthMax;
                } else {
                    minX = widthMin;
                    maxX = widthMax;
                    minZ = lengthMin;
                    maxZ = lengthMax;
                }
                break;
            case VERTICAL:
                minY = 0;
                maxY = 1;
                lengthMin = d;
                lengthMax = 1d - d;
                widthMin = 3 * d;
                widthMax = 1d - 3 * d;
                if (dirAxis == Axis.X) {
                    minX = widthMin;
                    maxX = widthMax;
                    minZ = lengthMin;
                    maxZ = lengthMax;
                } else {
                    minX = lengthMin;
                    maxX = lengthMax;
                    minZ = widthMin;
                    maxZ = widthMax;
                }
                break;
            case SIDEWAYS:
                minY = d;
                maxY = 1d - d;
                lengthMin = d;
                lengthMax = 1d - d;
                widthMin = 3 * d;
                widthMax = 1d - 3 * d;
                if (dirAxis == Axis.X) {
                    minX = lengthMin;
                    maxX = lengthMax;
                    minZ = widthMin;
                    maxZ = widthMax;
                } else {
                    minX = widthMin;
                    maxX = widthMax;
                    minZ = lengthMin;
                    maxZ = lengthMax;
                }
                break;
            default:
                if (te.partType == BeltPart.MIDDLE || te.partType == BeltPart.PULLEY) {
                    minY = 0;
                    maxY = 1;
                } else {
                    if ((te.slopeType == BeltSlope.UPWARD && te.partType == BeltPart.END)
                        || (te.slopeType == BeltSlope.DOWNWARD && te.partType == BeltPart.START)) {
                        minY = 0;
                        maxY = 1d - 3 * d;
                    } else {
                        minY = 3 * d;
                        maxY = 1;
                    }
                }
                lengthMin = 0;
                lengthMax = 1;
                widthMin = d;
                widthMax = 1d - d;
                if (dirAxis == Axis.X) {
                    minX = lengthMin;
                    maxX = lengthMax;
                    minZ = widthMin;
                    maxZ = widthMax;
                } else {
                    minX = widthMin;
                    maxX = widthMax;
                    minZ = lengthMin;
                    maxZ = lengthMax;
                }
                break;
        }

        if (te.slopeType == BeltSlope.HORIZONTAL) updateTunnelConnections(worldIn, x, y + 1, z);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public void addCollisionBoxesToList(World worldIn, int x, int y, int z, AxisAlignedBB mask, List list,
        Entity collider) {
        List<AxisAlignedBB> result = new ArrayList<AxisAlignedBB>();
        BeltTileEntity te = (BeltTileEntity) worldIn.getTileEntity(x, y, z);
        Direction dir = this.getDirection(worldIn.getBlockMetadata(x, y, z));
        double d = 1D / 16D;
        switch (te.slopeType) {
            case HORIZONTAL:
                if (te.partType == BeltPart.MIDDLE || te.partType == BeltPart.PULLEY) {
                    super.addCollisionBoxesToList(worldIn, x, y, z, mask, result, collider);
                } else if ((te.partType == BeltPart.START && dir.getAxisDirection() == AxisDirection.POSITIVE)
                    || (te.partType == BeltPart.END && dir.getAxisDirection() == AxisDirection.NEGATIVE)) {
                        if (dir.getAxis() == Axis.X) {
                            result
                                .add(AxisAlignedBB.getBoundingBox(x, y + 4 * d, z + d, x + d, y + 12 * d, z + 15 * d));
                            result.add(
                                AxisAlignedBB.getBoundingBox(x + d, y + 3 * d, z + d, x + 1D, y + 13 * d, z + 15 * d));
                        } else {
                            result
                                .add(AxisAlignedBB.getBoundingBox(x + d, y + 4 * d, z, x + 15 * d, y + 12 * d, z + d));
                            result.add(
                                AxisAlignedBB.getBoundingBox(x + d, y + 3 * d, z + d, x + 15 * d, y + 13 * d, z + 1D));
                        }
                    } else {
                        if (dir.getAxis() == Axis.X) {
                            result.add(
                                AxisAlignedBB.getBoundingBox(x, y + 3 * d, z + d, x + 15 * d, y + 13 * d, z + 15 * d));
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + 15 * d, y + 4 * d, z + d, x + 1D, y + 12 * d, z + 15 * d));
                        } else {
                            result.add(
                                AxisAlignedBB.getBoundingBox(x + d, y + 3 * d, z, x + 15 * d, y + 13 * d, z + 15 * d));
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + d, y + 4 * d, z + 15 * d, x + 15 * d, y + 12 * d, z + 1D));
                        }
                    }
                break;
            case VERTICAL:
                if (te.partType == BeltPart.MIDDLE || te.partType == BeltPart.PULLEY) {
                    super.addCollisionBoxesToList(worldIn, x, y, z, mask, result, collider);
                } else {
                    if (dir.getAxis() == Axis.X) {
                        result.add(AxisAlignedBB.getBoundingBox(x + 4 * d, y, z + d, x + 12 * d, y + d, z + 15 * d));
                        result.add(
                            AxisAlignedBB.getBoundingBox(x + 3 * d, y + d, z + d, x + 13 * d, y + 15 * d, z + 15 * d));
                        result.add(
                            AxisAlignedBB.getBoundingBox(x + 4 * d, y + 15 * d, z + d, x + 12 * d, y + 1D, z + 15 * d));
                    } else {
                        result.add(AxisAlignedBB.getBoundingBox(x + d, y, z + 4 * d, x + 15 * d, y + d, z + 12 * d));
                        result.add(
                            AxisAlignedBB.getBoundingBox(x + d, y + d, z + 3 * d, x + 15 * d, y + 15 * d, z + 13 * d));
                        result.add(
                            AxisAlignedBB.getBoundingBox(x + d, y + 15 * d, z + 4 * d, x + 15 * d, y + 1D, z + 12 * d));
                    }
                }
                break;
            case SIDEWAYS:
                if (te.partType == BeltPart.MIDDLE || te.partType == BeltPart.PULLEY) {
                    super.addCollisionBoxesToList(worldIn, x, y, z, mask, result, collider);
                } else if ((te.partType == BeltPart.START && dir.getAxisDirection() == AxisDirection.POSITIVE)
                    || (te.partType == BeltPart.END && dir.getAxisDirection() == AxisDirection.NEGATIVE)) {
                        if (dir.getAxis() == Axis.X) {
                            result
                                .add(AxisAlignedBB.getBoundingBox(x, y + d, z + 4 * d, x + d, y + 15 * d, z + 12 * d));
                            result.add(
                                AxisAlignedBB.getBoundingBox(x + d, y + d, z + 3 * d, x + 1D, y + 15 * d, z + 13 * d));
                        } else {
                            result
                                .add(AxisAlignedBB.getBoundingBox(x + 4 * d, y + d, z, x + 12 * d, y + 15 * d, z + d));
                            result.add(
                                AxisAlignedBB.getBoundingBox(x + 3 * d, y + d, z + d, x + 13 * d, y + 15 * d, z + 1D));
                        }
                    } else {
                        if (dir.getAxis() == Axis.X) {
                            result.add(
                                AxisAlignedBB.getBoundingBox(x, y + d, z + 3 * d, x + 15 * d, y + 15 * d, z + 13 * d));
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + 15 * d, y + d, z + 4 * d, x + 1D, y + 15 * d, z + 12 * d));
                        } else {
                            result.add(
                                AxisAlignedBB.getBoundingBox(x + 3 * d, y + d, z, x + 13 * d, y + 15 * d, z + 15 * d));
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + 3 * d, y + d, z + 15 * d, x + 13 * d, y + 15 * d, z + 1D));
                        }
                    }
                break;
            default:
                double minX, minY, minZ, maxX, maxY, maxZ, dX, dY, dZ;
                int steps;
                if (te.partType == BeltPart.MIDDLE || te.partType == BeltPart.PULLEY) {
                    steps = 16;
                    if (dir.getAxis() == Axis.X) {
                        minX = x;
                        minZ = z + d;
                        maxX = x + d;
                        maxZ = z + 15 * d;
                        dX = d;
                        dZ = 0;
                    } else {
                        minX = x + d;
                        minZ = z;
                        maxX = x + 15 * d;
                        maxZ = z + d;
                        dX = 0;
                        dZ = d;
                    }
                    if ((te.slopeType == BeltSlope.UPWARD && dir.getAxisDirection() == AxisDirection.POSITIVE)
                        || (te.slopeType == BeltSlope.DOWNWARD && dir.getAxisDirection() == AxisDirection.NEGATIVE)) {
                        minY = y + -5 * d;
                        maxY = y + 6 * d;
                        dY = d;
                    } else {
                        minY = y + 10 * d;
                        maxY = y + 21 * d;
                        dY = -d;
                    }
                } else {
                    steps = 8;
                    if ((te.slopeType == BeltSlope.UPWARD && te.partType == BeltPart.END)
                        || (te.slopeType == BeltSlope.DOWNWARD && te.partType == BeltPart.START)) {
                        dY = -d;
                        minY = y + 2 * d;
                        maxY = y + 13 * d;
                    } else {
                        dY = d;
                        minY = y + 3 * d;
                        maxY = y + 14 * d;
                    }
                    if ((te.slopeType == BeltSlope.UPWARD
                        && ((te.partType == BeltPart.START && dir.getAxisDirection() == AxisDirection.POSITIVE)
                            || (te.partType == BeltPart.END && dir.getAxisDirection() == AxisDirection.NEGATIVE)))
                        || (te.slopeType == BeltSlope.DOWNWARD && ((te.partType == BeltPart.END
                            && dir.getAxisDirection() == AxisDirection.NEGATIVE)
                            || (te.partType == BeltPart.START && dir.getAxisDirection() == AxisDirection.POSITIVE)))) {
                        if (dir.getAxis() == Axis.X) {
                            result
                                .add(AxisAlignedBB.getBoundingBox(x, y + 4 * d, z + d, x + d, y + 12 * d, z + 15 * d));
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + d, y + 3 * d, z + d, x + 8 * d, y + 13 * d, z + 15 * d));
                            minX = x + 8 * d;
                            minZ = z + d;
                            maxX = x + 9 * d;
                            maxZ = z + 15 * d;
                            dX = d;
                            dZ = 0;
                        } else {
                            result
                                .add(AxisAlignedBB.getBoundingBox(x + d, y + 4 * d, z, x + 15 * d, y + 12 * d, z + d));
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + d, y + 3 * d, z + d, x + 15 * d, y + 13 * d, z + 8 * d));
                            minX = x + d;
                            minZ = z + 8 * d;
                            maxX = x + 15 * d;
                            maxZ = z + 9 * d;
                            dX = 0;
                            dZ = d;
                        }
                    } else {
                        if (dir.getAxis() == Axis.X) {
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + 8 * d, y + 3 * d, z + d, x + 15 * d, y + 13 * d, z + 15 * d));
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + 15 * d, y + 4 * d, z + d, x + 1D, y + 12 * d, z + 15 * d));
                            minX = x + 7 * d;
                            minZ = z + d;
                            maxX = x + 8 * d;
                            maxZ = z + 15 * d;
                            dX = -d;
                            dZ = 0;
                        } else {
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + d, y + 3 * d, z + 8 * d, x + 15 * d, y + 13 * d, z + 15 * d));
                            result.add(
                                AxisAlignedBB
                                    .getBoundingBox(x + d, y + 4 * d, z + 15 * d, x + 15 * d, y + 12 * d, z + 1D));
                            minX = x + d;
                            minZ = z + 7 * d;
                            maxX = x + 15 * d;
                            maxZ = z + 8 * d;
                            dX = 0;
                            dZ = -d;
                        }
                    }
                }
                for (int i = 0; i < steps; i++) {
                    result.add(AxisAlignedBB.getBoundingBox(minX, minY, minZ, maxX, maxY, maxZ));
                    minX += dX;
                    minY += dY;
                    minZ += dZ;
                    maxX += dX;
                    maxY += dY;
                    maxZ += dZ;
                }
                break;
        }
        for (AxisAlignedBB box : result) {
            if (box.intersectsWith(mask)) list.add(box);
        }
    }

    public List<AxisAlignedBB> getSelectedBoundingBoxesList(World worldIn, int x, int y, int z) {
        List<AxisAlignedBB> list = new ArrayList<AxisAlignedBB>();
        this.addCollisionBoxesToList(
            worldIn,
            x,
            y,
            z,
            AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1),
            list,
            null);
        return list;
    }

    @Override
    public MovingObjectPosition collisionRayTrace(World worldIn, int x, int y, int z, Vec3 startVec, Vec3 endVec) {
        this.setBlockBoundsBasedOnState(worldIn, x, y, z);
        List<AxisAlignedBB> collisionList = new ArrayList<AxisAlignedBB>();
        this.addCollisionBoxesToList(
            worldIn,
            x,
            y,
            z,
            AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 1, z + 1),
            collisionList,
            null);
        for (AxisAlignedBB aabb : collisionList) {
            Vec3 minXVec = startVec.getIntermediateWithXValue(endVec, aabb.minX);
            Vec3 maxXVec = startVec.getIntermediateWithXValue(endVec, aabb.maxX);
            Vec3 minYVec = startVec.getIntermediateWithYValue(endVec, aabb.minY);
            Vec3 maxYVec = startVec.getIntermediateWithYValue(endVec, aabb.maxY);
            Vec3 minZVec = startVec.getIntermediateWithZValue(endVec, aabb.minZ);
            Vec3 maxZVec = startVec.getIntermediateWithZValue(endVec, aabb.maxZ);

            if (minXVec != null && !(minXVec.yCoord >= aabb.minY && minXVec.yCoord <= aabb.maxY
                && minXVec.zCoord >= aabb.minZ
                && minXVec.zCoord <= aabb.maxZ)) {
                minXVec = null;
            }

            if (maxXVec != null && !(maxXVec.yCoord >= aabb.minY && maxXVec.yCoord <= aabb.maxY
                && maxXVec.zCoord >= aabb.minZ
                && maxXVec.zCoord <= aabb.maxZ)) {
                maxXVec = null;
            }

            if (minYVec != null && !(minYVec.xCoord >= aabb.minX && minYVec.xCoord <= aabb.maxX
                && minYVec.zCoord >= aabb.minZ
                && minYVec.zCoord <= aabb.maxZ)) {
                minYVec = null;
            }

            if (maxYVec != null && !(maxYVec.xCoord >= aabb.minX && maxYVec.xCoord <= aabb.maxX
                && maxYVec.zCoord >= aabb.minZ
                && maxYVec.zCoord <= aabb.maxZ)) {
                maxYVec = null;
            }

            if (minZVec != null && !(minZVec.xCoord >= aabb.minX && minZVec.xCoord <= aabb.maxX
                && minZVec.yCoord >= aabb.minY
                && minZVec.yCoord <= aabb.maxY)) {
                minZVec = null;
            }

            if (maxZVec != null && !(maxZVec.xCoord >= aabb.minX && maxZVec.xCoord <= aabb.maxX
                && maxZVec.yCoord >= aabb.minY
                && maxZVec.yCoord <= aabb.maxY)) {
                maxZVec = null;
            }

            Vec3 resultVec = null;

            if (minXVec != null
                && (resultVec == null || startVec.squareDistanceTo(minXVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = minXVec;
            }

            if (maxXVec != null
                && (resultVec == null || startVec.squareDistanceTo(maxXVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = maxXVec;
            }

            if (minYVec != null
                && (resultVec == null || startVec.squareDistanceTo(minYVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = minYVec;
            }

            if (maxYVec != null
                && (resultVec == null || startVec.squareDistanceTo(maxYVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = maxYVec;
            }

            if (minZVec != null
                && (resultVec == null || startVec.squareDistanceTo(minZVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = minZVec;
            }

            if (maxZVec != null
                && (resultVec == null || startVec.squareDistanceTo(maxZVec) < startVec.squareDistanceTo(resultVec))) {
                resultVec = maxZVec;
            }

            if (resultVec != null) {
                byte b0 = -1;

                if (resultVec == minXVec) {
                    b0 = 4;
                }

                if (resultVec == maxXVec) {
                    b0 = 5;
                }

                if (resultVec == minYVec) {
                    b0 = 0;
                }

                if (resultVec == maxYVec) {
                    b0 = 1;
                }

                if (resultVec == minZVec) {
                    b0 = 2;
                }

                if (resultVec == maxZVec) {
                    b0 = 3;
                }

                return new MovingObjectPosition(x, y, z, b0, resultVec);
            }
        }
        return null;
    }

    /**
     * If this block doesn't render as an ordinary block it will return False (examples: signs, buttons, stairs, etc)
     */
    public boolean renderAsNormalBlock() {
        return false;
    }

    /**
     * The type of render function that is called for this block
     */
    public int getRenderType() {
        return ReCreate.proxy.getBeltBlockRenderID();
    }

    /**
     * Gets the block's texture. Args: side, meta
     */
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return belt;
    }

    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister reg) {
        belt = reg.registerIcon(ReCreate.ID + ":belt");
        beltScroll = reg.registerIcon(ReCreate.ID + ":belt_scroll");
        beltScrollLocation = ReCreate.asResource(ASSET_PATH + "belt_scroll.png");
        beltScrollDiagonal = reg.registerIcon(ReCreate.ID + ":belt_diagonal_scroll");
        beltScrollDiagonalLocation = ReCreate.asResource(ASSET_PATH + "belt_diagonal_scroll.png");
        coloredScrolls = new IIcon[ItemDye.field_150921_b.length];
        coloredScrollsDiagonal = new IIcon[coloredScrolls.length];
        for (int i = 0; i < coloredScrolls.length; i++) {
            coloredScrolls[i] = reg.registerIcon(ReCreate.ID + ":belt/" + ItemDye.field_150921_b[i] + "_scroll");
            coloredScrollLocations[i] = ReCreate
                .asResource(COLORED_ASSET_PATH + ItemDye.field_150921_b[i] + "_scroll.png");
            coloredScrollsDiagonal[i] = reg
                .registerIcon(ReCreate.ID + ":belt/" + ItemDye.field_150921_b[i] + "_diagonal_scroll");
            coloredScrollDiagonalLocations[i] = ReCreate
                .asResource(COLORED_ASSET_PATH + ItemDye.field_150921_b[i] + "_diagonal_scroll.png");
        }
    }

    /**
     * Is this block (a) opaque and (b) a full 1m cube? This determines whether or not to render the shared face of two
     * adjacent blocks and also whether the player can attach torches, redstone wire, etc to this block.
     */
    public boolean isOpaqueCube() {
        return false;
    }

    /**
     * Gets the icon name of the ItemBlock corresponding to this block. Used by hoppers.
     */
    @SideOnly(Side.CLIENT)
    public String getItemIconName() {
        return "recreate:textures/items/belt_connector.png";
    }

    /**
     * 
     * @param oldBlock
     * @param oldMeta
     * @param oldExtraData - given in such order: {BeltSlope, BeltPart, CasingType}
     * @param newBlock
     * @param newMeta
     * @param newExtraData - given in such order: {BeltSlope, BeltPart, CasingType}
     */
    @Override
    protected boolean areStatesKineticallyEquivalent(Block oldBlock, int oldMeta, Object[] oldExtraData, Block newBlock,
        int newMeta, Object[] newExtraData) {
        return super.areStatesKineticallyEquivalent(oldBlock, oldMeta, oldExtraData, newBlock, newMeta, newExtraData)
            && oldExtraData != null
            && newExtraData != null
            && oldExtraData[1] == newExtraData[1];
    }

    @Override
    public boolean hasShaftTowards(IBlockAccess world, int x, int y, int z, Direction face) {
        if (face.getAxis() != getAxis(world.getBlockMetadata(x, y, z))) return false;
        return getTileEntityOptional(world, x, y, z).map(BeltTileEntity::hasPulley)
            .orElse(false);
    }

    @Override
    public Axis getAxis(int meta) {
        if (meta < 8) return Axis.Y;
        return this.getDirection(meta)
            .getClockWise()
            .getAxis();
    }

    @Override
    public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
        ArrayList<ItemStack> drops = super.getDrops(world, x, y, z, metadata, fortune);
        TileEntity tileEntity = world.getTileEntity(x, y, z);
        if (tileEntity instanceof BeltTileEntity bte && bte.hasPulley())
            drops.addAll(AllBlocks.shaft.getDrops(world, x, y, z, metadata, fortune));
        return drops;
    }

    @Override
    public void dropBlockAsItem(World worldIn, int x, int y, int z, ItemStack itemIn) {
        BeltTileEntity controllerTE = BeltHelper.getControllerTE(worldIn, x, y, z);
        if (controllerTE != null) {
            BeltInventory inventory = controllerTE.getInventory();
            if (inventory != null) inventory.ejectAll();
        }
    }

    @Override
    public boolean isFlammable(IBlockAccess world, int x, int y, int z, ForgeDirection face) {
        return false;
    }

    @Override
    public void onFallenUpon(World worldIn, int x, int y, int z, Entity entityIn, float fallDistance) {
        super.onFallenUpon(worldIn, x, y, z, entityIn, fallDistance);
        onEntityCollidedWithBlock(worldIn, x, y, z, entityIn);
    }

    @Override
    public void onEntityCollidedWithBlock(World worldIn, int x, int y, int z, Entity entityIn) {
        if (!canTransportObjects(worldIn, x, y, z)) return;
        if (entityIn instanceof EntityPlayer player) {
            if (player.isSneaking()) return;
            if (player.capabilities.isFlying) return;
        }

        // TODO
        // if (AllItems.DIVING_BOOTS.get().isWornBy(entityIn)) return;

        BeltTileEntity belt = BeltHelper.getSegmentTE(worldIn, x, y, z);
        if (belt == null) return;
        if (entityIn instanceof EntityItem entityItem && !entityIn.isDead) {
            if (worldIn.isRemote) return;
            if (entityIn.motionY > 0) return;
            if (entityIn.isDead) return;
            // TODO
            // if (BeltTunnelInteractionHandler.getTunnelOnPosition(worldIn, x, y, z) != null) return;
            withTileEntityDo(worldIn, x, y, z, te -> {
                IInventory handler = te.itemHandler.orElse(null);
                if (handler == null) return;
                ItemStack stack = entityItem.getEntityItem();
                handler.setInventorySlotContents(0, stack);
                if (stack.stackSize == 0) entityItem.setDead();
            });
            return;
        }

        BeltTileEntity controller = BeltHelper.getControllerTE(worldIn, x, y, z);
        if (controller == null || controller.passengers == null) return;
        if (controller.passengers.containsKey(entityIn)) {
            TransportedEntityInfo info = controller.passengers.get(entityIn);
            if (info.getTicksSinceLastCollision() != 0 || (MathHelper.floor_double(entityIn.posX) == x
                && MathHelper.floor_double(entityIn.boundingBox.minY) == y
                && MathHelper.floor_double(entityIn.posZ) == z))
                info.refresh(x, y, z, belt.getBlockType(), belt.getBlockMetadata());
        } else {
            controller.passengers
                .put(entityIn, new TransportedEntityInfo(x, y, z, belt.getBlockType(), belt.getBlockMetadata()));
            entityIn.onGround = true;
        }
    }

    public static boolean canTransportObjects(World world, int x, int y, int z) {
        if (world.getBlock(x, y, z) != AllBlocks.belt) return false;
        BeltTileEntity bte = (BeltTileEntity) world.getTileEntity(x, y, z);
        BeltSlope slope = bte.slopeType;
        return slope != BeltSlope.VERTICAL && slope != BeltSlope.SIDEWAYS;
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float subX,
        float subY, float subZ) {
        if (player.isSneaking() || player.isPlayerSleeping() || player.isRiding()) return false;
        ItemStack heldItem = player.getHeldItem();

        boolean isHand = heldItem == null /* TODO && handIn == InteractionHand.MAIN_HAND */;
        boolean isWrench = !isHand && heldItem.getItem() == AllItems.wrench;
        boolean isConnector = !isHand && heldItem.getItem() instanceof BeltConnectorItem;
        boolean isShaft = !isHand && heldItem.getItem() instanceof ItemBlock itemBlock
            && Block.getBlockFromItem(itemBlock) == AllBlocks.shaft;
        boolean isDye = !isHand && heldItem.getItem() instanceof ItemDye;
        boolean hasWater = false;
        if (!isHand) {
            FluidStack fluidStack = EmptyingByBasin.emptyItem(world, heldItem, true)
                .getFirst();
            if (fluidStack != null) hasWater = fluidStack.getFluid()
                .equals(FluidRegistry.WATER);
        }

        if (isDye || hasWater) {
            if (!world.isRemote) {
                Integer color = hasWater ? null : heldItem.getItemDamage();
                withTileEntityDo(world, x, y, z, te -> te.applyColor(color));
            }
            return true;
        }

        if (isConnector || isWrench) {
            MovingObjectPosition hit = new MovingObjectPosition(
                x,
                y,
                z,
                side,
                Vec3.createVectorHelper(subX, subY, subZ));
            if (isConnector) return BeltSlicer.useConnector(world, x, y, z, player, hit, new Feedback());
            if (isWrench) return BeltSlicer.useWrench(world, x, y, z, player, hit, new Feedback());
        }

        BeltTileEntity belt = BeltHelper.getSegmentTE(world, x, y, z);
        if (belt == null) return false;

        if (isHand) {
            BeltTileEntity controllerBelt = belt.getControllerTE();
            if (controllerBelt == null) return false;
            if (world.isRemote) return true;
            MutableBoolean success = new MutableBoolean(false);
            controllerBelt.getInventory()
                .applyToEachWithin(belt.index + .5f, .55f, (transportedItemStack) -> {
                    player.inventory.addItemStackToInventory(transportedItemStack.stack);
                    success.setTrue();
                    return TransportedResult.removeItem();
                });
            if (success.isTrue()) world.playSoundEffect(x, y, z, "random.pop", .2f, 1f + ReCreate.RANDOM.nextFloat());
        }

        if (isShaft) {
            if (belt.partType != BeltPart.MIDDLE) return false;
            if (world.isRemote) return true;
            if (!player.capabilities.isCreativeMode) heldItem.stackSize--;
            belt.partType = BeltPart.PULLEY;
            belt.updateSpeed = true;
            belt.notifyUpdate();
            return true;
        }

        // TODO
        // if (AllBlocks.BRASS_CASING.isIn(heldItem)) {
        // if (world.isRemote) return true;
        // // TODO AllTriggers.triggerFor(AllTriggers.CASING_BELT, player);
        // belt.casing = CasingType.BRASS;
        // return true;
        // }
        //
        // if (AllBlocks.ANDESITE_CASING.isIn(heldItem)) {
        // if (world.isRemote) return true;
        // // TODO AllTriggers.triggerFor(AllTriggers.CASING_BELT, player);
        // belt.casing = CasingType.ANDESITE;
        // return true;
        // }

        return false;
    }

    @Override
    public boolean onWrenched(World world, int x, int y, int z, int face, EntityPlayer player) {
        BeltTileEntity te = (BeltTileEntity) world.getTileEntity(x, y, z);

        if (te.casing != CasingType.NONE) {
            if (world.isRemote) return true;
            te.casing = CasingType.NONE;
            return true;
        }

        if (te.partType == BeltPart.PULLEY) {
            if (world.isRemote) return true;
            te.partType = BeltPart.MIDDLE;
            if (player != null && !player.capabilities.isCreativeMode)
                player.inventory.addItemStackToInventory(new ItemStack(AllBlocks.shaft));
            te.detachKinetics();
            if (!(world.getTileEntity(te.sourceX, te.sourceY, te.sourceX) instanceof BeltTileEntity)) te.removeSource();
            te.notifyUpdate();
            te.attachKinetics();
            return true;
        }

        return false;
    }

    public static void initBelt(World world, int x, int y, int z) {
        if (world.isRemote) return;

        Block block = world.getBlock(x, y, z);
        if (block != AllBlocks.belt) return;
        // Find controller
        int limit = 1000;
        int currentX = x;
        int currentY = y;
        int currentZ = z;
        while (limit-- > 0) {
            Block currentBlock = world.getBlock(currentX, currentY, currentZ);
            if (currentBlock != AllBlocks.belt) {
                world.func_147480_a(x, y, z, true);
                return;
            }
            ChunkCoordinates nextSegmentPosition = ((BeltTileEntity) world.getTileEntity(currentX, currentY, currentZ))
                .nextSegmentPosition(false);
            if (nextSegmentPosition == null) break;
            if (!world.doChunksNearChunkExist(
                nextSegmentPosition.posX,
                nextSegmentPosition.posY,
                nextSegmentPosition.posZ,
                0)) return;
            currentX = nextSegmentPosition.posX;
            currentY = nextSegmentPosition.posY;
            currentZ = nextSegmentPosition.posZ;
        }

        // Init belts
        int index = 0;
        List<ChunkCoordinates> beltChain = getBeltChain(world, currentX, currentY, currentZ);
        if (beltChain.size() < 2) {
            world.func_147480_a(currentX, currentY, currentZ, true);
            return;
        }

        for (ChunkCoordinates beltPos : beltChain) {
            TileEntity tileEntity = world.getTileEntity(beltPos.posX, beltPos.posY, beltPos.posZ);
            Block currentBlock = world.getBlock(beltPos.posX, beltPos.posY, beltPos.posZ);

            if (tileEntity instanceof BeltTileEntity te && currentBlock == AllBlocks.belt) {
                te.setController(currentX, currentY, currentZ);
                te.beltLength = beltChain.size();
                te.index = index;
                te.attachKinetics();
                te.markDirty();
                te.sendData();

                if (te.isController() && !canTransportObjects(world, beltPos.posX, beltPos.posY, beltPos.posZ))
                    te.getInventory()
                        .ejectAll();
            } else {
                world.func_147480_a(currentX, currentY, currentZ, true);
                return;
            }
            index++;
        }

    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (world.isRemote) return;

        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof BeltTileEntity beltTileEntity)) return;
        if (beltTileEntity.isController()) beltTileEntity.getInventory()
            .ejectAll();
        world.removeTileEntity(x, y, z);

        // Destroy chain
        for (boolean forward : Iterate.trueAndFalse) {
            ChunkCoordinates currentPos = beltTileEntity.nextSegmentPosition(forward);
            if (currentPos == null) continue;
            world.destroyBlockInWorldPartially(
                currentPos.hashCode(),
                currentPos.posX,
                currentPos.posY,
                currentPos.posZ,
                -1);
            Block currentBlock = world.getBlock(currentPos.posX, currentPos.posY, currentPos.posZ);
            if (currentBlock != AllBlocks.belt) continue;

            boolean hasPulley = false;
            TileEntity tileEntity = world.getTileEntity(currentPos.posX, currentPos.posY, currentPos.posZ);
            if (tileEntity instanceof BeltTileEntity belt) {
                if ((belt.partType == BeltPart.END && !forward) || (belt.partType == BeltPart.START && forward))
                    continue;

                if (belt.isController()) belt.getInventory()
                    .ejectAll();

                belt.invalidate();
                hasPulley = belt.hasPulley();
            }

            Block newBlock;
            int newMeta;
            if (hasPulley) {
                newBlock = AllBlocks.shaft;
                newMeta = ((ShaftBlock) AllBlocks.shaft).getMetaFromAxis(
                    getAxis(world.getBlockMetadata(currentPos.posX, currentPos.posY, currentPos.posZ)));
            } else {
                newBlock = Blocks.air;
                newMeta = 0;
            }
            world.setBlock(currentPos.posX, currentPos.posY, currentPos.posZ, newBlock, newMeta, 3);
            world.playAuxSFX(
                2001,
                currentPos.posX,
                currentPos.posY,
                currentPos.posZ,
                Block.getIdFromBlock(currentBlock));
        }
    }

    private void updateTunnelConnections(IBlockAccess world, int x, int y, int z) {
        // TODO
        // Block tunnelBlock = world.getBlock(x, y, z);
        // if (tunnelBlock instanceof BeltTunnelBlock) ((BeltTunnelBlock) tunnelBlock).updateTunnel(world, pos);
    }

    public static List<ChunkCoordinates> getBeltChain(World world, int controllerPosX, int controllerPosY,
        int controllerPosZ) {
        List<ChunkCoordinates> positions = new LinkedList<ChunkCoordinates>();

        Block block = world.getBlock(controllerPosX, controllerPosY, controllerPosZ);
        if (block != AllBlocks.belt) return positions;

        int limit = 1000;
        ChunkCoordinates current = new ChunkCoordinates(controllerPosX, controllerPosY, controllerPosZ);
        while (limit-- > 0 && current != null) {
            Block currentBlock = world.getBlock(current.posX, current.posY, current.posZ);
            if (!(currentBlock instanceof BeltBlock belt)) break;
            positions.add(current);
            current = ((BeltTileEntity) world.getTileEntity(current.posX, current.posY, current.posZ))
                .nextSegmentPosition(true);
        }

        return positions;
    }

    public static boolean canAccessFromSide(Direction facing, int meta) {
        return true;
    }

    @Override
    public Class<BeltTileEntity> getTileEntityClass() {
        return BeltTileEntity.class;
    }

    // TODO
    // @Override
    // public ItemRequirement getRequiredItems(BlockState state, TileEntity te) {
    // List<ItemStack> required = new ArrayList<>();
    // if (state.getValue(PART) != BeltPart.MIDDLE) required.add(AllBlocks.SHAFT.asStack());
    // if (state.getValue(PART) == BeltPart.START) required.add(AllItems.BELT_CONNECTOR.asStack());
    // if (required.isEmpty()) return ItemRequirement.NONE;
    // return new ItemRequirement(ItemUseType.CONSUME, required);
    // }

    @Override
    public int rotate(World world, int x, int y, int z, Rotation rot) {
        int rotatedMeta = super.rotate(world, x, y, z, rot);

        if (getSlopeType(world, x, y, z) != BeltSlope.VERTICAL) return rotatedMeta;
        if (this.getDirection(world.getBlockMetadata(x, y, z))
            .getAxisDirection()
            != this.getDirection(rotatedMeta)
                .getAxisDirection()) {
            BeltPart partType = getPartType(world, x, y, z);
            if (partType == BeltPart.START) this.setPartType(world, x, y, z, BeltPart.END);
            if (partType == BeltPart.END) this.setPartType(world, x, y, z, BeltPart.START);
        }

        return rotatedMeta;
    }

    public BeltSlope getSlopeType(World world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof BeltTileEntity bte)) return null;
        return bte.slopeType;
    }

    public void setSlopeType(World world, int x, int y, int z, BeltSlope slopeType) {
        if (world.getTileEntity(x, y, z) instanceof BeltTileEntity bte) bte.slopeType = slopeType;
    }

    public BeltPart getPartType(World world, int x, int y, int z) {
        if (!(world.getTileEntity(x, y, z) instanceof BeltTileEntity bte)) return null;
        return bte.partType;
    }

    public void setPartType(World world, int x, int y, int z, BeltPart partType) {
        if (world.getTileEntity(x, y, z) instanceof BeltTileEntity bte) bte.partType = partType;
    }

    // TODO
    // public static class RenderProperties extends ReducedDestroyEffects implements DestroyProgressRenderingHandler {
    //
    // @Override
    // public boolean renderDestroyProgress(WorldClient level, LevelRenderer renderer, int breakerId,
    // ChunkCoordinates pos, int progress, BlockState blockState) {
    // TileEntity blockEntity = level.getTileEntity(pos);
    // if (blockEntity instanceof BeltTileEntity belt) {
    // for (ChunkCoordinates beltPos : BeltBlock.getBeltChain(level, belt.getController())) {
    // renderer.destroyBlockProgress(beltPos.hashCode(), beltPos, progress);
    // }
    // }
    // return false;
    // }
    // }

}
