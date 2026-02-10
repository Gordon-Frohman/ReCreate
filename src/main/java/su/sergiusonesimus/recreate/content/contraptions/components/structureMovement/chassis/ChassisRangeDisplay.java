package su.sergiusonesimus.recreate.content.contraptions.components.structureMovement.chassis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;

import su.sergiusonesimus.recreate.AllItems;
import su.sergiusonesimus.recreate.AllKeys;
import su.sergiusonesimus.recreate.AllSpecialTextures;
import su.sergiusonesimus.recreate.ClientProxy;
import su.sergiusonesimus.recreate.foundation.utility.Pair;

public class ChassisRangeDisplay {

    private static final int DISPLAY_TIME = 200;
    private static GroupEntry lastHoveredGroup = null;

    private static class Entry {

        ChassisTileEntity te;
        int timer;

        public Entry(ChassisTileEntity te) {
            this.te = te;
            timer = DISPLAY_TIME;
            ClientProxy.OUTLINER.showCluster(getOutlineKey(), createSelection(te))
                .colored(0xFFFFFF)
                .disableNormals()
                .lineWidth(1 / 16f)
                .withFaceTexture(AllSpecialTextures.HIGHLIGHT_CHECKERED);
        }

        protected Object getOutlineKey() {
            return Pair.of(new ChunkCoordinates(te.xCoord, te.yCoord, te.zCoord), 1);
        }

        protected Set<ChunkCoordinates> createSelection(ChassisTileEntity chassis) {
            Set<ChunkCoordinates> positions = new HashSet<>();
            List<ChunkCoordinates> includedBlockPositions = chassis.getIncludedBlockPositions(null, true);
            if (includedBlockPositions == null) return Collections.emptySet();
            positions.addAll(includedBlockPositions);
            return positions;
        }

    }

    private static class GroupEntry extends Entry {

        List<ChassisTileEntity> includedTEs;

        public GroupEntry(ChassisTileEntity te) {
            super(te);
        }

        @Override
        protected Object getOutlineKey() {
            return this;
        }

        @Override
        protected Set<ChunkCoordinates> createSelection(ChassisTileEntity chassis) {
            Set<ChunkCoordinates> list = new HashSet<>();
            includedTEs = te.collectChassisGroup();
            if (includedTEs == null) return list;
            for (ChassisTileEntity chassisTileEntity : includedTEs)
                list.addAll(super.createSelection(chassisTileEntity));
            return list;
        }

    }

    static Map<ChunkCoordinates, Entry> entries = new HashMap<>();
    static List<GroupEntry> groupEntries = new ArrayList<>();

    public static void tick() {
        EntityPlayer player = Minecraft.getMinecraft().thePlayer;
        World world = Minecraft.getMinecraft().theWorld;
        ItemStack heldItem = player.getHeldItem();
        boolean hasWrench = heldItem != null && heldItem.getItem() == AllItems.wrench;

        for (Iterator<ChunkCoordinates> iterator = entries.keySet()
            .iterator(); iterator.hasNext();) {
            ChunkCoordinates pos = iterator.next();
            Entry entry = entries.get(pos);
            if (tickEntry(entry, hasWrench)) iterator.remove();
            ClientProxy.OUTLINER.keep(entry.getOutlineKey());
        }

        for (Iterator<GroupEntry> iterator = groupEntries.iterator(); iterator.hasNext();) {
            GroupEntry group = iterator.next();
            if (tickEntry(group, hasWrench)) {
                iterator.remove();
                if (group == lastHoveredGroup) lastHoveredGroup = null;
            }
            ClientProxy.OUTLINER.keep(group.getOutlineKey());
        }

        if (!hasWrench) return;

        MovingObjectPosition over = Minecraft.getMinecraft().objectMouseOver;
        if (over == null || over.typeOfHit != MovingObjectType.BLOCK) return;
        TileEntity tileEntity = world.getTileEntity(over.blockX, over.blockY, over.blockZ);
        if (tileEntity == null || tileEntity.isInvalid()) return;
        if (!(tileEntity instanceof ChassisTileEntity chassisTileEntity)) return;

        boolean ctrl = AllKeys.ctrlDown();
        ChunkCoordinates pos = new ChunkCoordinates(over.blockX, over.blockY, over.blockZ);

        if (ctrl) {
            GroupEntry existingGroupForPos = getExistingGroupForPos(pos);
            if (existingGroupForPos != null) {
                for (ChassisTileEntity included : existingGroupForPos.includedTEs)
                    entries.remove(new ChunkCoordinates(included.xCoord, included.yCoord, included.zCoord));
                existingGroupForPos.timer = DISPLAY_TIME;
                return;
            }
        }

        if (!entries.containsKey(pos) || ctrl) display(chassisTileEntity);
        else {
            if (!ctrl) entries.get(pos).timer = DISPLAY_TIME;
        }
    }

    private static boolean tickEntry(Entry entry, boolean hasWrench) {
        ChassisTileEntity chassisTileEntity = entry.te;
        World teWorld = chassisTileEntity.getWorld();
        World world = Minecraft.getMinecraft().theWorld;

        if (chassisTileEntity.isInvalid() || teWorld == null
            || teWorld != world
            || !world.blockExists(chassisTileEntity.xCoord, chassisTileEntity.yCoord, chassisTileEntity.zCoord)) {
            return true;
        }

        if (!hasWrench && entry.timer > 20) {
            entry.timer = 20;
            return false;
        }

        entry.timer--;
        if (entry.timer == 0) return true;
        return false;
    }

    public static void display(ChassisTileEntity chassis) {

        // Display a group and kill any selections of its contained chassis blocks
        if (AllKeys.ctrlDown()) {
            GroupEntry hoveredGroup = new GroupEntry(chassis);

            for (ChassisTileEntity included : hoveredGroup.includedTEs)
                ClientProxy.OUTLINER.remove(new ChunkCoordinates(included.xCoord, included.yCoord, included.zCoord));

            groupEntries.forEach(entry -> ClientProxy.OUTLINER.remove(entry.getOutlineKey()));
            groupEntries.clear();
            entries.clear();
            groupEntries.add(hoveredGroup);
            return;
        }

        // Display an individual chassis and kill any group selections that contained it
        ChunkCoordinates pos = new ChunkCoordinates(chassis.xCoord, chassis.yCoord, chassis.zCoord);
        GroupEntry entry = getExistingGroupForPos(pos);
        if (entry != null) ClientProxy.OUTLINER.remove(entry.getOutlineKey());

        groupEntries.clear();
        entries.clear();
        entries.put(pos, new Entry(chassis));

    }

    private static GroupEntry getExistingGroupForPos(ChunkCoordinates pos) {
        for (GroupEntry groupEntry : groupEntries) for (ChassisTileEntity chassis : groupEntry.includedTEs)
            if (pos.posX == chassis.xCoord && pos.posY == chassis.yCoord && pos.posZ == chassis.zCoord)
                return groupEntry;
        return null;
    }

}
