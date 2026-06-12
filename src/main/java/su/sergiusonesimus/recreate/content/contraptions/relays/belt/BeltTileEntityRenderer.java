package su.sergiusonesimus.recreate.content.contraptions.relays.belt;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DestroyBlockProgress;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MathHelper;

import org.lwjgl.opengl.GL11;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.compat.tebreaker.TileEntityBreakerIntegration;
import su.sergiusonesimus.recreate.content.contraptions.base.CasingBlock.CasingType;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntity;
import su.sergiusonesimus.recreate.content.contraptions.base.KineticTileEntityRenderer;
import su.sergiusonesimus.recreate.content.contraptions.relays.belt.transport.TransportedItemStack;
import su.sergiusonesimus.recreate.content.contraptions.relays.elementary.shaft.ShaftModel;
import su.sergiusonesimus.recreate.foundation.render.ShadowRenderHelper;
import su.sergiusonesimus.recreate.foundation.utility.AngleHelper;
import su.sergiusonesimus.recreate.foundation.utility.Color;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.AnimationTickHolder;

public class BeltTileEntityRenderer extends KineticTileEntityRenderer {

    private static BeltModel horizontalMiddle = new BeltModel(false, false);
    private static BeltModel horizontalEnd = new BeltModel(true, false);
    private static BeltModel diagonalMiddle = new BeltModel(false, true);
    private static BeltModel diagonalEnd = new BeltModel(true, true);

    private static BeltCasingModel horizontalMiddleCasing = new BeltCasingModel(BeltSlope.HORIZONTAL, BeltPart.MIDDLE);
    private static BeltCasingModel horizontalPulleyCasing = new BeltCasingModel(BeltSlope.HORIZONTAL, BeltPart.PULLEY);
    private static BeltCasingModel horizontalEndCasing = new BeltCasingModel(BeltSlope.HORIZONTAL, BeltPart.END);
    private static BeltCasingModel diagonalMiddleCasing = new BeltCasingModel(BeltSlope.UPWARD, BeltPart.MIDDLE);
    private static BeltCasingModel diagonalPulleyCasing = new BeltCasingModel(BeltSlope.UPWARD, BeltPart.PULLEY);
    private static BeltCasingModel diagonalStartCasing = new BeltCasingModel(BeltSlope.UPWARD, BeltPart.START);
    private static BeltCasingModel diagonalEndCasing = new BeltCasingModel(BeltSlope.UPWARD, BeltPart.END);
    private static BeltCasingModel sidewaysMiddleCasing = new BeltCasingModel(BeltSlope.SIDEWAYS, BeltPart.MIDDLE);
    private static BeltCasingModel sidewaysPulleyCasing = new BeltCasingModel(BeltSlope.SIDEWAYS, BeltPart.PULLEY);

    private static ShaftModel shaft = new ShaftModel();
    private static BeltPulleyModel pulley = new BeltPulleyModel();

    @Override
    protected void renderSafe(KineticTileEntity te, double x, double y, double z, float partialTicks) {
        Block block = te.getBlockType();
        if (!(block instanceof BeltBlock belt)) return;
        BeltTileEntity beltTE = (BeltTileEntity) te;
        int meta = te.getBlockMetadata();

        BeltSlope beltSlope = beltTE.slopeType;
        BeltPart part = beltTE.partType;

        Direction facing = belt.getDirection(meta);
        AxisDirection axisDirection = facing.getAxisDirection();

        boolean downward = beltSlope == BeltSlope.DOWNWARD;
        boolean upward = beltSlope == BeltSlope.UPWARD;
        boolean diagonal = downward || upward;
        boolean start = part == BeltPart.START;
        boolean end = part == BeltPart.END;
        boolean sideways = beltSlope == BeltSlope.SIDEWAYS;
        boolean alongX = facing.getAxis() == Axis.X;

        float renderTick = AnimationTickHolder.getRenderTime(te.getWorld());
        Color color = getColor(beltTE);

        GL11.glPushMatrix();
        GL11.glTranslated(x + .5d, y + .5d, z + .5d);
        GL11.glColor4f(color.getRedAsFloat(), color.getGreenAsFloat(), color.getBlueAsFloat(), color.getAlphaAsFloat());

        boolean damageTexture = ReCreate.isTileEntityBreakerLoaded
            && TileEntityBreakerIntegration.shouldRenderDamageTexture(this);

        if (beltTE.hasPulley()) {
            Axis axis = belt.getAxis(meta);
            float rotation = getAngleForTe(beltTE, beltTE.xCoord, beltTE.yCoord, beltTE.zCoord, axis);
            shaft.setAxis(axis);
            shaft.setRotation(rotation);
            shaft.render(this);

            if (damageTexture) {
                DestroyBlockProgress dbp = TileEntityBreakerIntegration.getTileEntityDestroyProgress(te);
                if (dbp != null) {
                    TileEntityBreakerIntegration.setBreakTexture(this, TileEntityBreakerIntegration.BELT_PULLEY, dbp);
                }
            }

            pulley.setAxis(axis);
            pulley.setRotation(rotation);
            pulley.render(this);
        }

        float yRot = AngleHelper.horizontalAngle(facing) + (upward ? 180 : 0) + (sideways ? 270 : 0);
        GL11.glRotatef(yRot, 0, 1, 0);

        if (sideways) GL11.glRotatef(90, 0, 0, 1);

        if (!diagonal && beltSlope != BeltSlope.HORIZONTAL) GL11.glRotatef(90, 1, 0, 0);

        if (downward || (beltSlope == BeltSlope.VERTICAL && axisDirection == AxisDirection.POSITIVE)) {
            boolean b = start;
            start = end;
            end = b;
            part = start ? BeltPart.START : (end ? BeltPart.END : part);
        }

        DestroyBlockProgress damageState = null;
        if (ReCreate.isTileEntityBreakerLoaded && !TileEntityBreakerIntegration.shouldRenderDamageTexture(this)) {
            for (ChunkCoordinates pos : BeltBlock
                .getBeltChain(te.getWorld(), beltTE.controllerX, beltTE.controllerY, beltTE.controllerZ)) {
                DestroyBlockProgress dbp = TileEntityBreakerIntegration.getTileEntityDestroyProgress(
                    te.getWorld()
                        .getTileEntity(pos.posX, pos.posY, pos.posZ));
                if (dbp != null
                    && (damageState == null || dbp.getPartialBlockDamage() > damageState.getPartialBlockDamage())) {
                    damageState = dbp;
                }
            }
        }

        if (!damageTexture) {
            for (boolean bottom : Iterate.trueAndFalse) {
                BeltModel beltPartial = getBeltPartial(diagonal, part, bottom);

                // UV shift
                float speed = beltTE.getSpeed();
                float time = renderTick * -axisDirection.getStep();
                if (diagonal && (downward == alongX) || !sideways && !diagonal && alongX
                    || sideways && axisDirection == AxisDirection.NEGATIVE) speed = -speed;

                float scrollMult = diagonal ? 3f / 8f : 0.5f;

                float spriteSize = 1F;

                double scroll = speed * time / (31.5 * 16) + (bottom ? 0.5 : 0);
                scroll = scroll - Math.floor(scroll);
                scroll = scroll * spriteSize * scrollMult;

                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glTranslatef(0.0F, (float) scroll, 0.0F);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);

                beltPartial.render(beltTE.color, this);

                if (damageState != null) {
                    GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                    GL11.glPushMatrix();

                    GL11.glEnable(GL11.GL_BLEND);
                    OpenGlHelper.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);

                    GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                    GL11.glPolygonOffset(-3.0F, -3.0F);

                    GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);

                    TileEntityBreakerIntegration.setBreakTexture(
                        this,
                        diagonal ? TileEntityBreakerIntegration.DIAGONAL_BELT : TileEntityBreakerIntegration.BELT,
                        damageState);
                    beltPartial.render(beltTE.color, this);

                    GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                    GL11.glPopMatrix();
                    GL11.glPopAttrib();

                    OpenGlHelper.glBlendFunc(770, 771, 1, 0);
                }

                GL11.glMatrixMode(GL11.GL_TEXTURE);
                GL11.glLoadIdentity();
                GL11.glMatrixMode(GL11.GL_MODELVIEW);

                if (diagonal) break;
            }
        } else {
            TileEntityBreakerIntegration.setBreakTexture(this, null);
        }

        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        if (!damageTexture && beltTE.casing != CasingType.NONE) {
            BeltCasingModel casing = getCasing(beltSlope, part);
            casing.render(beltTE.casing, this);

            if (damageState != null) {
                GL11.glPushAttrib(GL11.GL_ENABLE_BIT | GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                GL11.glPushMatrix();

                GL11.glEnable(GL11.GL_BLEND);
                OpenGlHelper.glBlendFunc(GL11.GL_DST_COLOR, GL11.GL_SRC_COLOR, GL11.GL_ONE, GL11.GL_ZERO);

                GL11.glEnable(GL11.GL_POLYGON_OFFSET_FILL);
                GL11.glPolygonOffset(-3.0F, -3.0F);

                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.7F);

                TileEntityBreakerIntegration
                    .setBreakTexture(this, TileEntityBreakerIntegration.BELT_CASING, damageState);
                casing.render(beltTE.casing, this);

                GL11.glDisable(GL11.GL_POLYGON_OFFSET_FILL);
                GL11.glPopMatrix();
                GL11.glPopAttrib();

                OpenGlHelper.glBlendFunc(770, 771, 1, 0);
            }
        }

        GL11.glPopMatrix();

        if (!damageTexture) renderItems(beltTE, partialTicks, x, y, z);
    }

    public static BeltModel getBeltPartial(boolean diagonal, BeltPart part, boolean bottom) {
        BeltModel result;
        if (diagonal) {
            switch (part) {
                default:
                case MIDDLE:
                    result = diagonalMiddle;
                    break;
                case START:
                    result = diagonalEnd;
                    result.belt.rotateAngleX = (float) Math.PI;
                    break;
                case END:
                    result = diagonalEnd;
                    result.belt.rotateAngleX = 0;
                    break;
            }
        } else {
            switch (part) {
                default:
                case MIDDLE:
                    result = horizontalMiddle;
                    result.top.showModel = !bottom;
                    result.bottom.showModel = bottom;
                    break;
                case START:
                    result = horizontalEnd;
                    result.belt.rotateAngleX = 0;
                    result.top.showModel = !bottom;
                    result.side.showModel = !bottom;
                    result.bottom.showModel = bottom;
                    break;
                case END:
                    result = horizontalEnd;
                    result.belt.rotateAngleX = (float) Math.PI;
                    result.top.showModel = bottom;
                    result.side.showModel = bottom;
                    result.bottom.showModel = !bottom;
                    break;
            }
        }
        return result;
    }

    public static BeltCasingModel getCasing(BeltSlope slopeType, BeltPart partType) {
        BeltCasingModel result = null;
        switch (slopeType) {
            case HORIZONTAL:
                switch (partType) {
                    case MIDDLE:
                        result = horizontalMiddleCasing;
                        break;
                    case PULLEY:
                        result = horizontalPulleyCasing;
                        break;
                    default:
                        result = horizontalEndCasing;
                        result.casing.rotateAngleY = partType == BeltPart.START ? (float) Math.PI : 0;
                        break;
                }
                break;
            case VERTICAL, SIDEWAYS:
                switch (partType) {
                    case MIDDLE:
                        result = sidewaysMiddleCasing;
                        break;
                    default:
                        result = sidewaysPulleyCasing;
                        break;
                }
                break;
            default:
                switch (partType) {
                    case MIDDLE:
                        result = diagonalMiddleCasing;
                        break;
                    case END:
                        result = diagonalEndCasing;
                        break;
                    case PULLEY:
                        result = diagonalPulleyCasing;
                        break;
                    case START:
                        result = diagonalStartCasing;
                        break;
                }
                break;
        }
        return result;
    }

    protected void renderItems(BeltTileEntity te, float partialTicks, double x, double y, double z) {
        if (!te.isController() || te.beltLength == 0 || !(te.blockType instanceof BeltBlock belt)) return;

        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);

        Direction beltFacing = belt.getDirection(te.getBlockMetadata());
        ChunkCoordinates normal = beltFacing.getNormal();

        GL11.glTranslated(normal.posX * -0.5 + 0.5, 15 / 16f, normal.posZ * -0.5 + 0.5);

        BeltSlope slope = te.slopeType;
        int verticality = (slope == BeltSlope.DOWNWARD) ? -1 : (slope == BeltSlope.UPWARD ? 1 : 0);
        boolean slopeAlongX = normal.posX != 0;

        for (TransportedItemStack transported : te.getInventory()
            .getTransportedItems()) {
            GL11.glPushMatrix();

            Random r = new Random(transported.angle);

            float offset = (te.getSpeed() == 0) ? transported.beltPosition
                : transported.prevBeltPosition
                    + (transported.beltPosition - transported.prevBeltPosition) * partialTicks;

            float sideOffset = (te.getSpeed() == 0) ? transported.sideOffset
                : transported.prevSideOffset + (transported.sideOffset - transported.prevSideOffset) * partialTicks;

            float verticalMovement = (offset < 0.5f) ? 0
                : verticality * (Math.min(offset, te.beltLength - 0.5f) - 0.5f);

            double offX = normal.posX * offset;
            double offY = verticalMovement;
            double offZ = normal.posZ * offset;
            GL11.glTranslated(offX, offY, offZ);

            boolean alongX = beltFacing.getClockWise()
                .getAxis() == Axis.X;
            if (!alongX) sideOffset *= -1;
            GL11.glTranslated(alongX ? sideOffset : 0, 0, alongX ? 0 : sideOffset);

            boolean renderUpright = BeltHelper.isItemUpright(transported.stack);
            boolean onSlope = slope != BeltSlope.HORIZONTAL
                && MathHelper.clamp_float(offset, 0.5f, te.beltLength - 0.5f) == offset;

            if (onSlope) {
                boolean tiltForward = (slope == BeltSlope.DOWNWARD ^ (normal.posX > 0 || normal.posZ > 0))
                    == (normal.posZ != 0);
                float slopeAngle = tiltForward ? -45 : 45;
                GL11.glRotatef(slopeAngle, slopeAlongX ? 0 : 1, 0, slopeAlongX ? 1 : 0);
                GL11.glTranslated(0, 1 / 8f, 0);
            }

            GL11.glPushMatrix();
            GL11.glTranslated(0, -17D / 128D, 0);
            ShadowRenderHelper.renderShadow(0, 0, 0, 0.75f, 0.2f);
            GL11.glPopMatrix();

            if (renderUpright) {
                if (onSlope) {
                    boolean tiltForward = (slope == BeltSlope.DOWNWARD ^ (normal.posX > 0 || normal.posZ > 0))
                        == (normal.posZ != 0);
                    float slopeAngle = tiltForward ? -45 : 45;
                    GL11.glRotatef(-slopeAngle, slopeAlongX ? 0 : 1, 0, slopeAlongX ? 1 : 0);
                }
                Entity renderView = Minecraft.getMinecraft().renderViewEntity;
                if (renderView != null) {
                    double diffX = (te.xCoord + 0.5 + (normal.posX * offset)) - renderView.posX;
                    double diffZ = (te.zCoord + 0.5 + (normal.posZ * offset)) - renderView.posZ;
                    float yRot = (float) (Math.atan2(diffX, diffZ) * 180 / Math.PI + 180);
                    GL11.glRotatef(yRot, 0, 1, 0);
                }
            }

            renderStack(transported, te, r, renderUpright);

            GL11.glPopMatrix();
        }
        GL11.glPopMatrix();
    }

    private void renderStack(TransportedItemStack transported, BeltTileEntity te, Random r, boolean renderUpright) {
        ItemStack stack = transported.stack;
        int count = (int) (Math.log(stack.stackSize) / Math.log(2)) / 2;

        boolean isBlock = stack.getItem() instanceof ItemBlock && RenderBlocks.renderItemIn3d(
            Block.getBlockFromItem(stack.getItem())
                .getRenderType());

        EntityItem entityItem = new EntityItem(te.getWorldObj(), 0, 0, 0, stack.copy());
        if (!isBlock) entityItem.getEntityItem().stackSize = 1;
        entityItem.hoverStart = 0.0f;

        if (count > 1) GL11.glTranslated(0, 0, count / 32f);

        for (int i = 0; i <= count; i++) {
            GL11.glPushMatrix();

            GL11.glTranslated(0, -3D / 32D, 0);

            if (!renderUpright) {
                GL11.glRotatef(transported.angle, 0, 1, 0);
                if (!isBlock) {
                    GL11.glRotatef(90f, 1, 0, 0);
                    GL11.glTranslated(0, -7D / 32D, 0);
                }
            } else {
                GL11.glTranslated(0, -1D / 32D, 0);
            }

            RenderManager.instance.renderEntityWithPosYaw(entityItem, 0, 0, 0, 0, 0);
            GL11.glPopMatrix();

            if (isBlock) break;

            if (!renderUpright) {
                GL11.glRotated(10D, 0, 1, 0);
                GL11.glTranslated(0, isBlock ? 1 / 64d : 1 / 32d, 0);
            } else {
                GL11.glTranslated(0, 0, -1 / 16f);
            }
        }
    }

}
