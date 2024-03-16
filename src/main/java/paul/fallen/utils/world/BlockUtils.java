package paul.fallen.utils.world;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.*;

public class BlockUtils {

    public static List<Block> emptyBlocks;
    public static List<Block> rightClickableBlocks;

    static {
        emptyBlocks = Arrays.asList(Blocks.AIR, Blocks.LAVA, Blocks.WATER, Blocks.VINE, Blocks.SNOW, Blocks.GRASS, Blocks.FIRE);
        rightClickableBlocks = Arrays.asList(Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST, Blocks.WHITE_SHULKER_BOX, Blocks.ORANGE_SHULKER_BOX, Blocks.MAGENTA_SHULKER_BOX, Blocks.LIGHT_BLUE_SHULKER_BOX, Blocks.YELLOW_SHULKER_BOX, Blocks.LIME_SHULKER_BOX, Blocks.PINK_SHULKER_BOX, Blocks.GRAY_SHULKER_BOX, Blocks.CYAN_SHULKER_BOX, Blocks.PURPLE_SHULKER_BOX, Blocks.BLUE_SHULKER_BOX, Blocks.BROWN_SHULKER_BOX, Blocks.GREEN_SHULKER_BOX, Blocks.RED_SHULKER_BOX, Blocks.BLACK_SHULKER_BOX, Blocks.ANVIL, Blocks.OAK_BUTTON, Blocks.SPRUCE_BUTTON, Blocks.BIRCH_BUTTON, Blocks.JUNGLE_BUTTON, Blocks.DARK_OAK_BUTTON, Blocks.ACACIA_BUTTON, Blocks.STONE_BUTTON, Blocks.POLISHED_BLACKSTONE_BUTTON, Blocks.REDSTONE_BLOCK, Blocks.OAK_DOOR, Blocks.SPRUCE_DOOR, Blocks.BIRCH_DOOR, Blocks.JUNGLE_DOOR, Blocks.ACACIA_DOOR, Blocks.DARK_OAK_DOOR, Blocks.REPEATER, Blocks.COMPARATOR, Blocks.OAK_FENCE_GATE, Blocks.SPRUCE_FENCE_GATE, Blocks.BIRCH_FENCE_GATE, Blocks.JUNGLE_FENCE_GATE, Blocks.DARK_OAK_FENCE_GATE, Blocks.ACACIA_FENCE_GATE);
    }

    public static Block getBlock(double x, double y, double z) {
        return Minecraft.getInstance().level.getBlockState(new BlockPos(x, y, z)).getBlock();
    }

    public static Block getBlockAbovePlayer(Player inPlayer, double blocks) {
        blocks += inPlayer.getBbHeight();
        return getBlockAtPos(new BlockPos(inPlayer.getX(), inPlayer.getY() + blocks, inPlayer.getZ()));
    }

    public static Block getBlockAtPos(BlockPos inBlockPos) {
        return Minecraft.getInstance().level.getBlockState(inBlockPos).getBlock();
    }

    public static Block getBlockAtPosC(Player inPlayer, double x, double y, double z) {
        return getBlockAtPos(new BlockPos(inPlayer.getX() - x, inPlayer.getY() - y, inPlayer.getZ() - z));
    }

    public static float getBlockDistance(float xDiff, float yDiff, float zDiff) {
        return Mth.sqrt(((xDiff - 0.5F) * (xDiff - 0.5F)) + ((yDiff - 0.5F) * (yDiff - 0.5F))
                + ((zDiff - 0.5F) * (zDiff - 0.5F)));
    }

    public static BlockPos getBlockPos(BlockPos inBlockPos) {
        return inBlockPos;
    }

    public static BlockPos getBlockPos(double x, double y, double z) {
        return getBlockPos(new BlockPos(x, y, z));
    }

    public static BlockPos getBlockPosUnderPlayer(Player inPlayer) {
        return new BlockPos(inPlayer.getX(), (inPlayer.getY() + (inPlayer.getDeltaMovement().y + 0.1D)) - 1D, inPlayer.getZ());
    }

    public static Block getBlockUnderPlayer(Player inPlayer) {
        return getBlockAtPos(
                new BlockPos(inPlayer.getX(), (inPlayer.getY() + (inPlayer.getDeltaMovement().y + 0.1D)) - 1D, inPlayer.getZ()));
    }

    public static float getHorizontalPlayerBlockDistance(BlockPos blockPos) {
        float xDiff = (float) (Minecraft.getInstance().player.getX() - blockPos.getX());
        float zDiff = (float) (Minecraft.getInstance().player.getZ() - blockPos.getZ());
        return Mth.sqrt(((xDiff - 0.5F) * (xDiff - 0.5F)) + ((zDiff - 0.5F) * (zDiff - 0.5F)));
    }

    public static boolean isBlockEmpty(BlockPos pos) {
        try {
            if (emptyBlocks.contains(Minecraft.getInstance().level.getBlockState(pos).getBlock())) {
                AABB box = new AABB(pos);
                for (Entity e : Minecraft.getInstance().level.entitiesForRendering()) {
                    if (e instanceof LivingEntity && box.intersects(e.getBoundingBox())) {
                        return false;
                    }
                }
                return true;
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public static ArrayList<BlockPos> getAllBlocksBetween(BlockPos posA, BlockPos posB) {
        ArrayList<BlockPos> blockPosList = new ArrayList<>();

        int minX = Math.min(posA.getX(), posB.getX());
        int minY = Math.min(posA.getY(), posB.getY());
        int minZ = Math.min(posA.getZ(), posB.getZ());
        int maxX = Math.max(posA.getX(), posB.getX());
        int maxY = Math.max(posA.getY(), posB.getY());
        int maxZ = Math.max(posA.getZ(), posB.getZ());

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blockPosList.add(new BlockPos(x, y, z));
                }
            }
        }

        return blockPosList;
    }
}