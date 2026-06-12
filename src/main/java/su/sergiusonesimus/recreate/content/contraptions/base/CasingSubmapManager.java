package su.sergiusonesimus.recreate.content.contraptions.base;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.world.IBlockAccess;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import lombok.Getter;
import su.sergiusonesimus.recreate.content.contraptions.base.CasingBlock.CasingType;
import team.chisel.ctmlib.SubmapManagerCTM;
import team.chisel.ctmlib.TextureSubmap;

public class CasingSubmapManager extends SubmapManagerCTM {

    @SideOnly(Side.CLIENT)
    private static CasingRenderBlocks rb;

    @Getter
    private TextureSubmap submapAdditional;

    private CasingType type;

    public CasingSubmapManager(String textureName, CasingType type) {
        super(textureName);
        this.type = type;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(String modName, Block block, IIconRegister register) {
        super.registerIcons(modName, block, register);
        switch (type) {
            case ANDESITE:
            case BRASS:
            case REFINED_RADIANCE:
                submapAdditional = new TextureSubmap(
                    register.registerIcon(
                        modName + ":" + getTextureName() + (type == CasingType.REFINED_RADIANCE ? "_cross" : "_inner")),
                    2,
                    2);
                break;
            default:
                break;

        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public RenderBlocks createRenderContext(RenderBlocks rendererOld, Block block, IBlockAccess world) {
        if (rb == null) {
            rb = new CasingRenderBlocks();
        }
        rb.setRenderBoundsFromBlock(block);
        rb.submap = getSubmap();
        rb.submapSmall = getSubmapSmall();
        rb.submapAdditional = submapAdditional;
        return rb;
    }

}
