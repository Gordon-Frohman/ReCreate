package su.sergiusonesimus.recreate.content.contraptions.base;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.recreate.ReCreate;
import su.sergiusonesimus.recreate.content.contraptions.wrench.IWrenchable;
import team.chisel.ctmlib.ICTMBlock;
import team.chisel.ctmlib.ISubmapManager;

public class CasingBlock extends Block implements IWrenchable, ICTMBlock<ISubmapManager> {

    private ISubmapManager[] managers = new ISubmapManager[CasingType.values().length - 1];

    public CasingBlock(Material material) {
        super(material);
        this.setHardness(2.0F);
        this.setStepSound(soundTypeWood);
    }

    @Override
    public boolean isToolEffective(String type, int metadata) {
        return type.equals("pickaxe") || type.equals("axe");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(Item itemIn, CreativeTabs tab, List list) {
        for (int i = 0; i < CasingType.values().length - 1; i++) {
            if (getTypeFromMeta(i).isVisible()) list.add(new ItemStack(itemIn, 1, i));
        }
    }

    @Override
    public boolean onWrenched(World world, int x, int y, int z, int face, EntityPlayer player) {
        return false;
    }

    @Override
    public Direction getDirection(int meta) {
        return null;
    }

    @Override
    public int getMetaFromDirection(Direction direction) {
        return 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister icon) {
        for (int i = 0; i < managers.length; i++) {
            CasingType type = getTypeFromMeta(i);
            managers[i] = new CasingSubmapManager(type.getCasingName(), type);
            managers[i].registerIcons(ReCreate.ID, this, icon);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {
        return getIcon(side, world.getBlockMetadata(x, y, z));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        return getManager(meta).getIcon(side, meta);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public int getRenderType() {
        return ReCreate.proxy.getCasingBlockRenderID();
    }

    @Override
    public int damageDropped(int meta) {
        return meta;
    }

    @Override
    public ISubmapManager getManager(IBlockAccess world, int x, int y, int z, int meta) {
        return getManager(world.getBlockMetadata(x, y, z));
    }

    @Override
    public ISubmapManager getManager(int meta) {
        return managers[meta % managers.length];
    }

    public static CasingType getTypeFromMeta(int meta) {
        switch (meta) {
            default:
                return CasingType.NONE;
            case 0:
                return CasingType.ANDESITE;
            case 1:
                return CasingType.BRASS;
            case 2:
                return CasingType.COPPER;
            case 3:
                return CasingType.SHADOW_STEEL;
            case 4:
                return CasingType.REFINED_RADIANCE;
            case 5:
                return CasingType.CREATIVE;
        }
    }

    public static enum CasingType {

        NONE(false),
        ANDESITE,
        BRASS,
        COPPER,
        SHADOW_STEEL(false),
        REFINED_RADIANCE(false),
        CREATIVE(false);

        private boolean visible;

        CasingType() {
            this(true);
        }

        CasingType(boolean visible) {
            this.visible = visible;
        }

        public String getCasingName() {
            return this.toString()
                .toLowerCase() + "_casing";
        }

        public boolean isVisible() {
            return visible;
        }
    }

}
