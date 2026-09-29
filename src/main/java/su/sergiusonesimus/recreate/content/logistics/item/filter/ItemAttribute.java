package su.sergiusonesimus.recreate.content.logistics.item.filter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fluids.IFluidContainerItem;

import org.apache.commons.lang3.StringUtils;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import su.sergiusonesimus.recreate.AllRecipeTypes;
import su.sergiusonesimus.recreate.content.contraptions.processing.InWorldProcessing;
import su.sergiusonesimus.recreate.content.logistics.item.filter.attribute.BookAuthorAttribute;
import su.sergiusonesimus.recreate.content.logistics.item.filter.attribute.ColorAttribute;
import su.sergiusonesimus.recreate.content.logistics.item.filter.attribute.EnchantAttribute;
import su.sergiusonesimus.recreate.content.logistics.item.filter.attribute.FluidContentsAttribute;
import su.sergiusonesimus.recreate.content.logistics.item.filter.attribute.ItemNameAttribute;
import su.sergiusonesimus.recreate.foundation.utility.Lang;
import su.sergiusonesimus.recreate.util.OreDictHelper;

public interface ItemAttribute {

    static List<ItemAttribute> types = new ArrayList<>();

    static ItemAttribute standard = register(StandardTraits.DUMMY);
    static ItemAttribute inTag = register(new InTag("dummy"));
    static ItemAttribute inItemGroup = register(new InItemGroup(CreativeTabs.tabMisc));
    static ItemAttribute addedBy = register(new InItemGroup.AddedBy("dummy"));
    static ItemAttribute hasEnchant = register(EnchantAttribute.EMPTY);
    // TODO
    // static ItemAttribute shulkerFillLevel = register(ShulkerFillLevelAttribute.EMPTY);
    static ItemAttribute hasColor = register(ColorAttribute.EMPTY);
    static ItemAttribute hasFluid = register(FluidContentsAttribute.EMPTY);
    static ItemAttribute hasName = register(new ItemNameAttribute("dummy"));
    static ItemAttribute bookAuthor = register(new BookAuthorAttribute("dummy"));
    // TODO This is most likely not yet implemented on 1.7.10. May add it with mixins
    // static ItemAttribute bookCopy = register(new BookCopyAttribute(-1));
    // TODO Probably not needed on 1.7.10
    // static ItemAttribute astralAmulet = register(new AstralSorceryAmuletAttribute("dummy", -1));
    // static ItemAttribute astralAttunement = register(new AstralSorceryAttunementAttribute("dummy"));
    // static ItemAttribute astralCrystal = register(new AstralSorceryCrystalAttribute("dummy"));
    // static ItemAttribute astralPerkGem = register(new AstralSorceryPerkGemAttribute("dummy"));

    static ItemAttribute register(ItemAttribute attributeType) {
        types.add(attributeType);
        return attributeType;
    }

    default boolean appliesTo(ItemStack stack, World world) {
        return appliesTo(stack);
    }

    boolean appliesTo(ItemStack stack);

    default List<ItemAttribute> listAttributesOf(ItemStack stack, World world) {
        return listAttributesOf(stack);
    }

    public List<ItemAttribute> listAttributesOf(ItemStack stack);

    public String getTranslationKey();

    void writeNBT(NBTTagCompound nbt);

    ItemAttribute readNBT(NBTTagCompound nbt);

    public default void serializeNBT(NBTTagCompound nbt) {
        NBTTagCompound compound = new NBTTagCompound();
        writeNBT(compound);
        nbt.setTag(getNBTKey(), compound);
    }

    public static ItemAttribute fromNBT(NBTTagCompound nbt) {
        for (ItemAttribute itemAttribute : types) {
            if (!itemAttribute.canRead(nbt)) continue;
            return itemAttribute.readNBT(nbt.getCompoundTag(itemAttribute.getNBTKey()));
        }
        return null;
    }

    default Object[] getTranslationParameters() {
        return new String[0];
    }

    default boolean canRead(NBTTagCompound nbt) {
        return nbt.hasKey(getNBTKey());
    }

    default String getNBTKey() {
        return getTranslationKey();
    }

    @SideOnly(value = Side.CLIENT)
    default ChatComponentTranslation format(boolean inverted) {
        return Lang.translate(
            "item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
            getTranslationParameters());
    }

    public static enum StandardTraits implements ItemAttribute {

        DUMMY(s -> false),
        PLACEABLE(s -> s.getItem() instanceof ItemBlock),
        CONSUMABLE(s -> {
            switch (s.getItem()
                .getItemUseAction(s)) {
                default:
                    return false;
                case eat, drink:
                    return true;
            }
        }),
        FLUID_CONTAINER(s -> s.getItem() instanceof IFluidContainerItem),
        ENCHANTED(ItemStack::isItemEnchanted),
        MAX_ENCHANTED(StandardTraits::maxEnchanted),
        RENAMED(s -> s.stackTagCompound != null && s.stackTagCompound.hasKey("display", 10)
            && s.stackTagCompound.getCompoundTag("display")
                .hasKey("Name", 8)),
        DAMAGED(ItemStack::isItemDamaged),
        BADLY_DAMAGED(s -> s.isItemDamaged() && s.getItemDamage() / s.getMaxDamage() > 3 / 4f),
        NOT_STACKABLE(((Predicate<ItemStack>) ItemStack::isStackable).negate()),
        EQUIPABLE(s -> {
            Item item = s.getItem();
            for (int i = 0; i < 4; i++) {
                if (item.isValidArmor(s, i, null)) return true;
            }
            return false;
        }),
        FURNACE_FUEL(TileEntityFurnace::isItemFuel),
        WASHABLE(InWorldProcessing::isWashable),
        // TODO
        // CRUSHABLE((s, w) -> testRecipe(s, w, AllRecipeTypes.CRUSHING.getType())
        // || testRecipe(s, w, AllRecipeTypes.MILLING.getType())),
        SMELTABLE(s -> FurnaceRecipes.smelting()
            .getSmeltingResult(s) != null),
        SMOKABLE(s -> {
            ItemStack result = FurnaceRecipes.smelting()
                .getSmeltingResult(s);
            return result != null && result.getItem()
                .getItemUseAction(result) == EnumAction.eat;
        }),
        BLASTABLE(s -> {
            ItemStack result = FurnaceRecipes.smelting()
                .getSmeltingResult(s);
            return result != null && result.getItem()
                .getItemUseAction(result) != EnumAction.eat;
        });

        private static final IInventory RECIPE_WRAPPER = new InventoryBasic("", false, 1);
        private Predicate<ItemStack> test;
        private BiPredicate<ItemStack, World> testWithWorld;

        private StandardTraits(Predicate<ItemStack> test) {
            this.test = test;
        }

        @SuppressWarnings("unused")
        private static boolean testRecipe(ItemStack s, World w, AllRecipeTypes type) {
            RECIPE_WRAPPER.setInventorySlotContents(0, s.copy());
            return type.find(RECIPE_WRAPPER, w)
                .isPresent();
        }

        @SuppressWarnings("unchecked")
        private static boolean maxEnchanted(ItemStack s) {
            return ((Map<Integer, Integer>) EnchantmentHelper.getEnchantments(s)).entrySet()
                .stream()
                .anyMatch(e -> Enchantment.enchantmentsList[e.getKey()].getMaxLevel() <= e.getValue());
        }

        private StandardTraits(BiPredicate<ItemStack, World> test) {
            this.testWithWorld = test;
        }

        @Override
        public boolean appliesTo(ItemStack stack, World world) {
            if (testWithWorld != null) return testWithWorld.test(stack, world);
            return appliesTo(stack);
        }

        @Override
        public boolean appliesTo(ItemStack stack) {
            return test.test(stack);
        }

        @Override
        public List<ItemAttribute> listAttributesOf(ItemStack stack, World world) {
            List<ItemAttribute> attributes = new ArrayList<>();
            for (StandardTraits trait : values()) if (trait.appliesTo(stack, world)) attributes.add(trait);
            return attributes;
        }

        @Override
        public List<ItemAttribute> listAttributesOf(ItemStack stack) {
            return null;
        }

        @Override
        public String getTranslationKey() {
            return Lang.asId(name());
        }

        @Override
        public String getNBTKey() {
            return "standard_trait";
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            nbt.setBoolean(name(), true);
        }

        @Override
        public ItemAttribute readNBT(NBTTagCompound nbt) {
            for (StandardTraits trait : values()) if (nbt.hasKey(trait.name())) return trait;
            return null;
        }

    }

    /**
     * Since we do not have tags in 1.7.10, replacing them with OreDictionary entries.
     * Keeping the name for consistency though
     */
    public static class InTag implements ItemAttribute {

        public String oreDictName;

        public InTag(String oreDictName) {
            this.oreDictName = oreDictName;
        }

        @Override
        public boolean appliesTo(ItemStack stack) {
            return OreDictHelper.getOreNames(stack)
                .contains(oreDictName);
        }

        @Override
        public List<ItemAttribute> listAttributesOf(ItemStack stack) {
            return OreDictHelper.getOreNames(stack)
                .stream()
                .map(InTag::new)
                .collect(Collectors.toList());
        }

        @Override
        public String getTranslationKey() {
            return "in_tag";
        }

        @Override
        public Object[] getTranslationParameters() {
            return new Object[] { "#" + oreDictName.toString() };
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            nbt.setString("oreDictName", oreDictName);
        }

        @Override
        public ItemAttribute readNBT(NBTTagCompound nbt) {
            return new InTag(nbt.getString("oreDictName"));
        }

    }

    public static class InItemGroup implements ItemAttribute {

        private CreativeTabs group;

        public InItemGroup(CreativeTabs group) {
            this.group = group;
        }

        @Override
        public boolean appliesTo(ItemStack stack) {
            Item item = stack.getItem();
            CreativeTabs[] tabs = item.getCreativeTabs();
            int meta = stack.getItemDamage();
            CreativeTabs tabToCheck = (meta < tabs.length && tabs[meta] != null) ? tabs[meta] : item.getCreativeTab();
            return tabToCheck == group;
        }

        @Override
        public List<ItemAttribute> listAttributesOf(ItemStack stack) {
            CreativeTabs group = stack.getItem()
                .getCreativeTab();
            return group == null ? Collections.emptyList() : Arrays.asList(new InItemGroup(group));
        }

        @Override
        public String getTranslationKey() {
            return "in_item_group";
        }

        @Override
        @SideOnly(value = Side.CLIENT)
        public ChatComponentTranslation format(boolean inverted) {
            return Lang.translate(
                "item_attributes." + getTranslationKey() + (inverted ? ".inverted" : ""),
                I18n.format(group.getTranslatedTabLabel(), new Object[0]));
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            nbt.setString("path", group.tabLabel);
        }

        @Override
        public ItemAttribute readNBT(NBTTagCompound nbt) {
            String readPath = nbt.getString("path");
            for (CreativeTabs group : CreativeTabs.creativeTabArray)
                if (group.tabLabel.equals(readPath)) return new InItemGroup(group);
            return null;
        }

    }

    public static class AddedBy implements ItemAttribute {

        private String modId;

        public AddedBy(String modId) {
            this.modId = modId;
        }

        @Override
        public boolean appliesTo(ItemStack stack) {
            return modId.equals(getCreatorModId(stack));
        }

        @Override
        public List<ItemAttribute> listAttributesOf(ItemStack stack) {
            String id = getCreatorModId(stack);
            return id == null ? Collections.emptyList() : Arrays.asList(new AddedBy(id));
        }

        private String getCreatorModId(ItemStack stack) {
            if (stack != null) {
                String uniqueName = GameData.getItemRegistry()
                    .getNameForObject(stack.getItem());
                if (uniqueName != null && uniqueName.contains(":")) {
                    return uniqueName.split(":")[0];
                }
            }
            return "minecraft";
        }

        @Override
        public String getTranslationKey() {
            return "added_by";
        }

        @Override
        public Object[] getTranslationParameters() {
            ModContainer mod = Loader.instance()
                .getIndexedModList()
                .get(modId);
            String name;

            if (mod != null) {
                name = mod.getName();
            } else {
                name = StringUtils.capitalize(modId);
            }

            return new Object[] { name };
        }

        @Override
        public void writeNBT(NBTTagCompound nbt) {
            nbt.setString("id", modId);
        }

        @Override
        public ItemAttribute readNBT(NBTTagCompound nbt) {
            return new AddedBy(nbt.getString("id"));
        }

    }

}
