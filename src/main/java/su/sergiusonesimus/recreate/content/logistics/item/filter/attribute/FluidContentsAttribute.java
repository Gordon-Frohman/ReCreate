package su.sergiusonesimus.recreate.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.IFluidContainerItem;

import su.sergiusonesimus.recreate.content.logistics.item.filter.ItemAttribute;

public class FluidContentsAttribute implements ItemAttribute {

    public static final FluidContentsAttribute EMPTY = new FluidContentsAttribute(null);

    private final Fluid fluid;

    public FluidContentsAttribute(@Nullable Fluid fluid) {
        this.fluid = fluid;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return extractFluids(itemStack).contains(fluid);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        return extractFluids(itemStack).stream()
            .map(FluidContentsAttribute::new)
            .collect(Collectors.toList());
    }

    @Override
    public String getTranslationKey() {
        return "has_fluid";
    }

    @Override
    public Object[] getTranslationParameters() {
        String parameter = "";
        if (fluid != null) parameter = new ChatComponentTranslation(fluid.getUnlocalizedName()).getUnformattedText();
        return new Object[] { parameter };
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        if (fluid == null) return;
        String id = FluidRegistry.getFluidName(fluid);
        if (id == null) return;
        nbt.setString("id", id);
    }

    @Override
    public ItemAttribute readNBT(NBTTagCompound nbt) {
        return nbt.hasKey("id") ? new FluidContentsAttribute(FluidRegistry.getFluid(nbt.getString("id"))) : EMPTY;
    }

    private List<Fluid> extractFluids(ItemStack stack) {
        List<Fluid> fluids = new ArrayList<>();

        if (stack != null && stack.getItem() instanceof IFluidContainerItem container) fluids.add(
            container.getFluid(stack)
                .getFluid());

        return fluids;
    }
}
