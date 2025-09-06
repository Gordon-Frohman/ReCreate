package su.sergiusonesimus.recreate.foundation.block;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public interface ITE<T extends TileEntity> extends ITileEntityProvider {

    Class<? extends T> getTileEntityClass();

    default void withTileEntityDo(IBlockAccess world, int x, int y, int z, Consumer<T> action) {
        getTileEntityOptional(world, x, y, z).ifPresent(action);
    }

    default boolean onTileEntityUse(IBlockAccess world, int x, int y, int z, Function<T, Boolean> action) {
        return getTileEntityOptional(world, x, y, z).map(action)
            .orElse(false);
    }

    default Optional<T> getTileEntityOptional(IBlockAccess world, int x, int y, int z) {
        return Optional.ofNullable(getTileEntity(world, x, y, z));
    }

    @Override
    default TileEntity createNewTileEntity(World worldIn, int meta) {
        try {
            TileEntity te = getTileEntityClass().newInstance();
            return te;
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    @SuppressWarnings("unchecked")
    default T getTileEntity(IBlockAccess worldIn, int x, int y, int z) {
        TileEntity tileEntity = worldIn.getTileEntity(x, y, z);
        Class<? extends T> expectedClass = getTileEntityClass();

        if (tileEntity == null) return null;
        if (!expectedClass.isInstance(tileEntity)) return null;

        return (T) tileEntity;
    }

}
