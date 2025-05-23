package su.sergiusonesimus.recreate.foundation.block;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.block.Block;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.GameRegistry.UniqueIdentifier;

public class BlockStressValues {

    private static final Map<String, IStressValueProvider> PROVIDERS = new HashMap<>();

    public static void registerProvider(String namespace, IStressValueProvider provider) {
        PROVIDERS.put(namespace, provider);
    }

    @Nullable
    public static IStressValueProvider getProvider(String namespace) {
        return PROVIDERS.get(namespace);
    }

    @Nullable
    public static IStressValueProvider getProvider(Block block) {
        return getProvider(GameRegistry.findUniqueIdentifierFor(block).modId);
    }

    public static double getImpact(Block block) {
        UniqueIdentifier blockId = GameRegistry.findUniqueIdentifierFor(block);
        IStressValueProvider provider = getProvider(blockId.modId);
        if (provider != null) {
            return provider.getImpact(block);
        }
        Double defaultImpact = BlockStressDefaults.DEFAULT_IMPACTS.get(blockId);
        if (defaultImpact != null) {
            return defaultImpact;
        }
        return 0;
    }

    public static double getCapacity(Block block) {
        UniqueIdentifier blockId = GameRegistry.findUniqueIdentifierFor(block);
        IStressValueProvider provider = getProvider(blockId.modId);
        if (provider != null) {
            return provider.getCapacity(block);
        }
        Double defaultCapacity = BlockStressDefaults.DEFAULT_CAPACITIES.get(blockId);
        if (defaultCapacity != null) {
            return defaultCapacity;
        }
        return 0;
    }

    public static boolean hasImpact(Block block) {
        UniqueIdentifier blockId = GameRegistry.findUniqueIdentifierFor(block);
        IStressValueProvider provider = getProvider(blockId.modId);
        if (provider != null) {
            return provider.hasImpact(block);
        }
        return BlockStressDefaults.DEFAULT_IMPACTS.containsKey(blockId);
    }

    public static boolean hasCapacity(Block block) {
        UniqueIdentifier blockId = GameRegistry.findUniqueIdentifierFor(block);
        IStressValueProvider provider = getProvider(blockId.modId);
        if (provider != null) {
            return provider.hasCapacity(block);
        }
        return BlockStressDefaults.DEFAULT_CAPACITIES.containsKey(blockId);
    }

    public interface IStressValueProvider {

        /**
         * Gets the stress impact of a block.
         * 
         * @param block The block.
         * @return the stress impact value of the block, or 0 if it does not have one.
         */
        double getImpact(Block block);

        /**
         * Gets the stress capacity of a block.
         * 
         * @param block The block.
         * @return the stress capacity value of the block, or 0 if it does not have one.
         */
        double getCapacity(Block block);

        boolean hasImpact(Block block);

        boolean hasCapacity(Block block);
    }

}
