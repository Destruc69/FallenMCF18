package paul.fallen.pathfinder;

import com.mojang.math.Vector3d;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import paul.fallen.FALLENClient;
import paul.fallen.utils.client.MathUtils;
import paul.fallen.utils.render.RenderUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AStarCustomPathFinder {
    private static final Vector3d[] flatCardinalDirections = {
            new Vector3d(1, 0, 0),
            new Vector3d(-1, 0, 0),
            new Vector3d(0, 0, 1),
            new Vector3d(0, 0, -1),

            new Vector3d(1, 1, 0),
            new Vector3d(-1, 1, 0),
            new Vector3d(0, 1, 1),
            new Vector3d(0, 1, -1),

            new Vector3d(1, -1, 0),
            new Vector3d(-1, -1, 0),
            new Vector3d(0, -1, 1),
            new Vector3d(0, -1, -1)
    };
    private final Vector3d startVector3d;
    private ArrayList<Vector3d> path = new ArrayList<Vector3d>();
    private final Vector3d endVector3d;
    private final ArrayList<Hub> hubs = new ArrayList<Hub>();
    private final ArrayList<Hub> hubsToWork = new ArrayList<Hub>();
    private final double minDistanceSquared = 9;
    private final boolean nearest = true;

    public AStarCustomPathFinder(Vector3d startVector3d, Vector3d endVector3d) {
        this.startVector3d = new Vector3d((int) startVector3d.x, (int) startVector3d.y, (int) startVector3d.z);
        this.endVector3d = new Vector3d((int) endVector3d.x, (int) endVector3d.y, (int) endVector3d.z);
    }

    public ArrayList<Vector3d> getPath() {
        return path;
    }

    public void compute() {
        compute(500, 2);
    }

    public void compute(int loops, int depth) {
        path.clear();
        hubsToWork.clear();
        ArrayList<Vector3d> initPath = new ArrayList<Vector3d>();
        initPath.add(startVector3d);
        hubsToWork.add(new Hub(startVector3d, null, initPath, MathUtils.getDistance(startVector3d, endVector3d), 0, 0));
        search:
        for (int i = 0; i < loops; i++) {
            Collections.sort(hubsToWork, new CompareHub());
            int j = 0;
            if (hubsToWork.isEmpty()) {
                break;
            }
            for (Hub hub : new ArrayList<Hub>(hubsToWork)) {
                j++;
                if (j > depth) {
                    break;
                } else {
                    hubsToWork.remove(hub);
                    hubs.add(hub);

                    for (Vector3d direction : flatCardinalDirections) {
                        Vector3d loc = new Vector3d(hub.getLoc().x + direction.x, hub.getLoc().y + direction.y, hub.getLoc().z + direction.z);
                        if (checkPositionValidity(loc, false)) {
                            if (addHub(hub, loc, 0)) {
                                break search;
                            }
                        }
                    }

                    Vector3d loc1 = new Vector3d(hub.getLoc().x, hub.getLoc().y + 1, hub.getLoc().z);
                    if (checkPositionValidity(loc1, false)) {
                        if (addHub(hub, loc1, 0)) {
                            break search;
                        }
                    }

                    Vector3d loc2 = new Vector3d(hub.getLoc().x, hub.getLoc().y - 1, hub.getLoc().z);
                    if (checkPositionValidity(loc2, false)) {
                        if (addHub(hub, loc2, 0)) {
                            break search;
                        }
                    }
                }
            }
        }
        if (nearest) {
            Collections.sort(hubs, new CompareHub());
            path = hubs.get(0).getPath();
        }
    }

    public static boolean checkPositionValidity(Vector3d loc, boolean checkGround) {
        return checkPositionValidity((int) loc.x, (int) loc.y, (int) loc.z, checkGround);
    }

    public static boolean checkPositionValidity(int x, int y, int z, boolean checkGround) {
        BlockPos block1 = new BlockPos(x, y, z);
        BlockPos block2 = new BlockPos(x, y + 1, z);
        BlockPos block3 = new BlockPos(x, y - 1, z);
        if (FALLENClient.INSTANCE.getModuleManager().pathfinder.type.sval == "ground") {
            return !isBlockSolid(block1) && !isBlockSolid(block2) && (isBlockSolid(block3) || !checkGround) && isSafeToWalkOn(block3);
        } else {
            return !isBlockSolid(block1) && !isBlockSolid(block2) && !isBlockSolid(block3);
        }
    }

    private static boolean isBlockSolid(BlockPos block) {
        return !Minecraft.getInstance().level.getBlockState(block).getBlock().equals(Blocks.AIR);
    }

    private static boolean isSafeToWalkOn(BlockPos block) {
        Minecraft mc = Minecraft.getInstance();
        return mc.level.getBlockState(block).getBlock().equals(Blocks.AIR) && !mc.level.getBlockState(block.offset(0, -1, 0)).getBlock().equals(Blocks.AIR) && mc.level.getBlockState(block.offset(0, 1, 0)).getBlock().equals(Blocks.AIR);
    }

    public Hub isHubExisting(Vector3d loc) {
        for (Hub hub : hubs) {
            if (hub.getLoc().equals(loc)) {
                return hub;
            }
        }
        for (Hub hub : hubsToWork) {
            if (hub.getLoc().equals(loc)) {
                return hub;
            }
        }
        return null;
    }

    public boolean addHub(Hub parent, Vector3d loc, double cost) {
        Hub existingHub = isHubExisting(loc);
        double totalCost = cost;
        if (parent != null) {
            totalCost += parent.getTotalCost();
        }
        if (existingHub == null) {
            if (loc.equals(endVector3d) || (minDistanceSquared != 0 && MathUtils.getDistance(loc, endVector3d) <= minDistanceSquared)) {
                path.clear();
                path = new ArrayList<Vector3d>(parent.getPath());
                path.add(loc);
                return true;
            } else {
                ArrayList<Vector3d> path = new ArrayList<Vector3d>(parent.getPath());
                path.add(loc);
                hubsToWork.add(new Hub(loc, parent, path, MathUtils.getDistance(loc, endVector3d), cost, totalCost));
            }
        } else if (existingHub.getCost() > cost) {
            ArrayList<Vector3d> path = new ArrayList<Vector3d>(parent.getPath());
            path.add(loc);
            existingHub.setLoc(loc);
            existingHub.setParent(parent);
            existingHub.setPath(path);
            existingHub.setSquareDistanceToFromTarget(MathUtils.getDistance(loc, endVector3d));
            existingHub.setCost(cost);
            existingHub.setTotalCost(totalCost);
        }
        return false;
    }

    private class Hub {
        private Vector3d loc = null;
        private Hub parent = null;
        private ArrayList<Vector3d> path;
        private double squareDistanceToFromTarget;
        private double cost;
        private double totalCost;

        public Hub(Vector3d loc, Hub parent, ArrayList<Vector3d> path, double squareDistanceToFromTarget, double cost, double totalCost) {
            this.loc = loc;
            this.parent = parent;
            this.path = path;
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
            this.cost = cost;
            this.totalCost = totalCost;
        }

        public Vector3d getLoc() {
            return loc;
        }

        public Hub getParent() {
            return parent;
        }

        public ArrayList<Vector3d> getPath() {
            return path;
        }

        public double getSquareDistanceToFromTarget() {
            return squareDistanceToFromTarget;
        }

        public double getCost() {
            return cost;
        }

        public void setLoc(Vector3d loc) {
            this.loc = loc;
        }

        public void setParent(Hub parent) {
            this.parent = parent;
        }

        public void setPath(ArrayList<Vector3d> path) {
            this.path = path;
        }

        public void setSquareDistanceToFromTarget(double squareDistanceToFromTarget) {
            this.squareDistanceToFromTarget = squareDistanceToFromTarget;
        }

        public void setCost(double cost) {
            this.cost = cost;
        }

        public double getTotalCost() {
            return totalCost;
        }

        public void setTotalCost(double totalCost) {
            this.totalCost = totalCost;
        }
    }

    public class CompareHub implements Comparator<Hub> {
        @Override
        public int compare(Hub o1, Hub o2) {
            return (int) (
                    (o1.getSquareDistanceToFromTarget() + o1.getTotalCost()) - (o2.getSquareDistanceToFromTarget() + o2.getTotalCost())
            );
        }
    }

    public double[] calculateMotion(ArrayList<Vector3d> path, double rotationYaw, double speed) {
        Minecraft mc = Minecraft.getInstance();
        double playerX = mc.player.getX();
        double playerZ = mc.player.getZ();
        double velocityX = mc.player.getDeltaMovement().x;
        double velocityZ = mc.player.getDeltaMovement().z;

        rotationYaw = Math.toRadians(rotationYaw);

        int closestBlockIndex = 0;
        double closestBlockDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < path.size(); i++) {
            double distance = MathUtils.getDistance(new Vector3d(mc.player.position().x, mc.player.position().y, mc.player.position().z), new Vector3d(path.get(i).x, path.get(i).y, path.get(i).z));
            if (distance < closestBlockDistance) {
                closestBlockDistance = distance;
                closestBlockIndex = i;
            }
        }

        BlockPos closestBlock = new BlockPos(new BlockPos(path.get(closestBlockIndex).x, path.get(closestBlockIndex).y, path.get(closestBlockIndex).z));

        // Ensure we don't exceed the array size when accessing nextBlock
        BlockPos nextBlock = (closestBlockIndex == path.size() - 1) ? closestBlock : new BlockPos(path.get(closestBlockIndex + 1).x, path.get(closestBlockIndex + 1).y, path.get(closestBlockIndex + 1).z);

        // Adjust delta values based on player's velocity
        double deltaX = nextBlock.getX() + 0.5 - playerX + velocityX;
        double deltaZ = nextBlock.getZ() + 0.5 - playerZ + velocityZ;

        double targetAngle = Math.atan2(deltaZ, deltaX);
        double playerAngle = rotationYaw; // Use radians directly

        // Smoothly adjust the player's rotationYaw
        double angleDifference = targetAngle - playerAngle;

        // Ensure the angle difference is within the range [-π, π]
        angleDifference = (angleDifference + Math.PI) % (2 * Math.PI) - Math.PI;

        // Calculate the distance to the target position
        double distance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // Calculate the motion components (motionX and motionZ)
        double motionX = Math.cos(angleDifference) * distance;
        double motionZ = Math.sin(angleDifference) * distance;

        // Apply motion limits based on speed
        double motionMagnitude = Math.sqrt(motionX * motionX + motionZ * motionZ);
        if (motionMagnitude > speed) {
            motionX *= speed / motionMagnitude;
            motionZ *= speed / motionMagnitude;
        }

        return new double[]{motionX, motionZ};
    }

    public Vector3d getTargetPosition(ArrayList<Vector3d> path) {
        Minecraft mc = Minecraft.getInstance();
        int closestBlockIndex = 0;
        double closestBlockDistance = Double.POSITIVE_INFINITY;

        for (int i = 0; i < path.size(); i++) {
            double distance = MathUtils.getDistance(new Vector3d(mc.player.position().x, mc.player.position().y, mc.player.position().z), new Vector3d(path.get(i).x, path.get(i).y, path.get(i).z));
            if (distance < closestBlockDistance) {
                closestBlockDistance = distance;
                closestBlockIndex = i;
            }
        }

        BlockPos closestBlock = new BlockPos(new BlockPos(path.get(closestBlockIndex).x, path.get(closestBlockIndex).y, path.get(closestBlockIndex).z));

        // Ensure we don't exceed the array size when accessing nextBlock
        BlockPos nextBlock = (closestBlockIndex == path.size() - 1) ? closestBlock : new BlockPos(path.get(closestBlockIndex + 1).x, path.get(closestBlockIndex + 1).y, path.get(closestBlockIndex + 1).z);

        return new Vector3d(nextBlock.getX(), nextBlock.getY(), nextBlock.getZ());
    }

    public void renderPath(RenderLevelStageEvent event) {
    }

    public boolean hasReachedEndOfPath() {
        return MathUtils.getDistance(new Vector3d(Minecraft.getInstance().player.position().x, Minecraft.getInstance().player.position().y, Minecraft.getInstance().player.position().z), this.getPath().get(this.getPath().size() - 1)) < 1;
    }

    public void move() {
        Minecraft mc = Minecraft.getInstance();
        double[] m = calculateMotion(this.getPath(), Math.toRadians(mc.player.rotA), mc.player.isSprinting() ? 0.26 : 0.2);
        if (FALLENClient.INSTANCE.getModuleManager().pathfinder.type.sval == "ground") {
            mc.player.setDeltaMovement(m[0], mc.player.getDeltaMovement().y, m[1]);

            if (getTargetPosition(this.getPath()).y > mc.player.getY()) {
                if (mc.player.isOnGround()) {
                    mc.player.jumpFromGround();
                }
            }
        } else {
            if (getTargetPosition(this.getPath()).y > mc.player.getY()) {
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, 1, mc.player.getDeltaMovement().z);
            } else if (getTargetPosition(this.getPath()).y < mc.player.getY()) {
                mc.player.setDeltaMovement(mc.player.getDeltaMovement().x, -1, mc.player.getDeltaMovement().z);
            } else {
                mc.player.setDeltaMovement(m[0], 0, m[1]);
            }
        }
    }
}