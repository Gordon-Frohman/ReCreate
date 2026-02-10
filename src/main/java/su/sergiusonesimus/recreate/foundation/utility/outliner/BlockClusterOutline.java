package su.sergiusonesimus.recreate.foundation.utility.outliner;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.Vec3;

import su.sergiusonesimus.metaworlds.util.Direction;
import su.sergiusonesimus.metaworlds.util.Direction.Axis;
import su.sergiusonesimus.metaworlds.util.Direction.AxisDirection;
import su.sergiusonesimus.recreate.AllSpecialTextures;
import su.sergiusonesimus.recreate.foundation.render.RenderTypes;
import su.sergiusonesimus.recreate.foundation.utility.Iterate;
import su.sergiusonesimus.recreate.util.VecHelper;

public class BlockClusterOutline extends Outline {

    private Cluster cluster;

    public BlockClusterOutline(Iterable<ChunkCoordinates> selection) {
        cluster = new Cluster();
        selection.forEach(cluster::include);
    }

    @Override
    public void render(float pt) {
        cluster.visibleEdges.forEach(edge -> {
            Vec3 start = Vec3.createVectorHelper(edge.posX, edge.posY, edge.posZ);
            Direction direction = Direction.get(AxisDirection.POSITIVE, edge.axis);
            ChunkCoordinates directionNormal = direction.getNormal();
            Vec3 relativePos = Vec3.createVectorHelper(
                edge.posX + directionNormal.posX,
                edge.posY + directionNormal.posY,
                edge.posZ + directionNormal.posZ);
            renderAACuboidLine(start, relativePos);
        });

        Optional<AllSpecialTextures> faceTexture = params.faceTexture;
        if (!faceTexture.isPresent()) return;

        RenderTypes.setupOutlineTranslucent(
            faceTexture.get()
                .getLocation(),
            true);

        cluster.visibleFaces.forEach((face, axisDirection) -> {
            Direction direction = Direction.get(axisDirection, face.axis);
            int x = face.posX;
            int y = face.posY;
            int z = face.posZ;
            if (axisDirection == AxisDirection.POSITIVE) {
                ChunkCoordinates directionNormal = direction.getOpposite()
                    .getNormal();
                x += directionNormal.posX;
                y += directionNormal.posY;
                z += directionNormal.posZ;
            }
            renderBlockFace(x, y, z, direction);
        });

        RenderTypes.cleanUp();
    }

    protected void renderBlockFace(int x, int y, int z, Direction face) {
        Vec3 center = VecHelper.getCenterOf(x, y, z);
        ChunkCoordinates offsetNormal = face.getNormal();
        Vec3 offset = Vec3.createVectorHelper(offsetNormal.posX, offsetNormal.posY, offsetNormal.posZ);
        Vec3 plane = VecHelper.axisAlingedPlaneOf(offset);
        Axis axis = face.getAxis();

        double modifier = 1 / 2f + 1 / 128d;
        offset = Vec3.createVectorHelper(offset.xCoord * modifier, offset.yCoord * modifier, offset.zCoord * modifier);
        modifier = 1 / 2f;
        plane = Vec3.createVectorHelper(plane.xCoord * modifier, plane.yCoord * modifier, plane.zCoord * modifier)
            .addVector(offset.xCoord, offset.yCoord, offset.zCoord);

        int deg = face.getAxisDirection()
            .getStep() * 90;
        Vec3 a1 = plane.addVector(center.xCoord, center.yCoord, center.zCoord);
        plane = VecHelper.rotate(plane, deg, axis);
        Vec3 a2 = plane.addVector(center.xCoord, center.yCoord, center.zCoord);
        plane = VecHelper.rotate(plane, deg, axis);
        Vec3 a3 = plane.addVector(center.xCoord, center.yCoord, center.zCoord);
        plane = VecHelper.rotate(plane, deg, axis);
        Vec3 a4 = plane.addVector(center.xCoord, center.yCoord, center.zCoord);

        putQuad(a1, a2, a3, a4, face);
    }

    private static class Cluster {

        private Map<MergeEntry, AxisDirection> visibleFaces;
        private Set<MergeEntry> visibleEdges;

        public Cluster() {
            visibleEdges = new HashSet<>();
            visibleFaces = new HashMap<>();
        }

        public void include(ChunkCoordinates pos) {

            // 6 FACES
            for (Axis axis : Iterate.axes) {
                ChunkCoordinates direction = Direction.get(AxisDirection.POSITIVE, axis)
                    .getNormal();
                for (int offset : Iterate.zeroAndOne) {
                    MergeEntry entry = new MergeEntry(
                        axis,
                        pos.posX + direction.posX * offset,
                        pos.posY + direction.posY * offset,
                        pos.posZ + direction.posZ * offset);
                    if (visibleFaces.remove(entry) == null)
                        visibleFaces.put(entry, offset == 0 ? AxisDirection.NEGATIVE : AxisDirection.POSITIVE);
                }
            }

            // 12 EDGES
            for (Axis axis : Iterate.axes) {
                for (Axis axis2 : Iterate.axes) {
                    if (axis == axis2) continue;
                    for (Axis axis3 : Iterate.axes) {
                        if (axis == axis3) continue;
                        if (axis2 == axis3) continue;

                        ChunkCoordinates direction = Direction.get(AxisDirection.POSITIVE, axis2)
                            .getNormal();
                        ChunkCoordinates direction2 = Direction.get(AxisDirection.POSITIVE, axis3)
                            .getNormal();

                        for (int offset : Iterate.zeroAndOne) {
                            Vec3 entryPos = Vec3.createVectorHelper(
                                pos.posX + direction.posX * offset,
                                pos.posY + direction.posY * offset,
                                pos.posZ + direction.posZ * offset);
                            for (int offset2 : Iterate.zeroAndOne) {
                                entryPos = entryPos.addVector(
                                    direction2.posX * offset2,
                                    direction2.posY * offset2,
                                    direction2.posZ * offset2);
                                MergeEntry entry = new MergeEntry(
                                    axis,
                                    (int) entryPos.xCoord,
                                    (int) entryPos.yCoord,
                                    (int) entryPos.zCoord);
                                if (!visibleEdges.remove(entry)) visibleEdges.add(entry);
                            }
                        }
                    }

                    break;
                }
            }

        }

    }

    private static class MergeEntry {

        private Axis axis;
        private int posX;
        private int posY;
        private int posZ;

        public MergeEntry(Axis axis, int x, int y, int z) {
            this.axis = axis;
            this.posX = x;
            this.posY = y;
            this.posZ = z;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof MergeEntry)) return false;

            MergeEntry other = (MergeEntry) o;
            return this.axis == other.axis && this.posX == other.posX
                && this.posY == other.posY
                && this.posZ == other.posZ;
        }

        @Override
        public int hashCode() {
            return (this.posX + this.posZ << 8 + this.posY << 16) * 31 + axis.ordinal();
        }
    }

}
