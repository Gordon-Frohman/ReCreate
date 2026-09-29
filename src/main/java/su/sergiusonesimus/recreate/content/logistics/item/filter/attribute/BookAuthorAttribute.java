package su.sergiusonesimus.recreate.content.logistics.item.filter.attribute;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import su.sergiusonesimus.recreate.content.logistics.item.filter.ItemAttribute;

public class BookAuthorAttribute implements ItemAttribute {

    String author;

    public BookAuthorAttribute(String author) {
        this.author = author;
    }

    @Override
    public boolean appliesTo(ItemStack itemStack) {
        return extractAuthor(itemStack).equals(author);
    }

    @Override
    public List<ItemAttribute> listAttributesOf(ItemStack itemStack) {
        String name = extractAuthor(itemStack);

        List<ItemAttribute> atts = new ArrayList<>();
        if (name.length() > 0) {
            atts.add(new BookAuthorAttribute(name));
        }
        return atts;
    }

    @Override
    public String getTranslationKey() {
        return "book_author";
    }

    @Override
    public Object[] getTranslationParameters() {
        return new Object[] { author };
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        nbt.setString("author", this.author);
    }

    @Override
    public ItemAttribute readNBT(NBTTagCompound nbt) {
        return new BookAuthorAttribute(nbt.getString("author"));
    }

    private String extractAuthor(ItemStack stack) {
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null && nbt.hasKey("author")) {
            return nbt.getString("author");
        }
        return "";
    }
}
