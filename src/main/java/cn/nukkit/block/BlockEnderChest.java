package cn.nukkit.block;

import cn.nukkit.blockentity.BlockEntity;
import cn.nukkit.blockentity.BlockEntityEnderChest;
import cn.nukkit.item.Item;
import cn.nukkit.item.ItemTool;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.Vector3f;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.StringTag;
import cn.nukkit.nbt.tag.Tag;
import cn.nukkit.player.Player;
import cn.nukkit.utils.BlockColor;
import cn.nukkit.utils.Faceable;
import cn.nukkit.utils.Identifier;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static cn.nukkit.block.BlockIds.OBSIDIAN;

public class BlockEnderChest extends BlockTransparent implements Faceable {

    private Set<Player> viewers = new HashSet<>();

    public BlockEnderChest(Identifier id) {
        super(id);
    }

    @Override
    public boolean canBeActivated() {
        return true;
    }

    @Override
    public int getLightLevel() {
        return 7;
    }

    @Override
    public double getHardness() {
        return 22.5;
    }

    @Override
    public double getResistance() {
        return 3000;
    }

    @Override
    public int getToolType() {
        return ItemTool.TYPE_PICKAXE;
    }

    @Override
    public double getMinX() {
        return this.x + 0.0625;
    }

    @Override
    public double getMinZ() {
        return this.z + 0.0625;
    }

    @Override
    public double getMaxX() {
        return this.x + 0.9375;
    }

    @Override
    public double getMaxY() {
        return this.y + 0.9475;
    }

    @Override
    public double getMaxZ() {
        return this.z + 0.9375;
    }

    @Override
    public boolean place(Item item, Block block, Block target, BlockFace face, Vector3f clickPos, Player player) {
        int[] faces = {2, 5, 3, 4};
        this.setDamage(faces[player != null ? player.getDirection().getHorizontalIndex() : 0]);

        this.getLevel().setBlock(block, this, true, true);
        CompoundTag nbt = new CompoundTag("")
                .putString("id", BlockEntity.ENDER_CHEST)
                .putInt("x", (int) this.x)
                .putInt("y", (int) this.y)
                .putInt("z", (int) this.z);

        if (item.hasCustomName()) {
            nbt.putString("CustomName", item.getCustomName());
        }

        if (item.hasCustomBlockData()) {
            Map<String, Tag> customData = item.getCustomBlockData().getTags();
            for (Map.Entry<String, Tag> tag : customData.entrySet()) {
                nbt.put(tag.getKey(), tag.getValue());
            }
        }

        new BlockEntityEnderChest(this.getLevel().getChunk(this.getChunkX(), this.getChunkZ()), nbt);
        return true;
    }

    @Override
    public boolean onActivate(Item item, Player player) {
        if (player != null) {
            Block top = this.up();
            if (!top.isTransparent()) {
                return true;
            }

            BlockEntity t = this.getLevel().getBlockEntity(this);
            BlockEntityEnderChest chest;
            if (t instanceof BlockEntityEnderChest) {
                chest = (BlockEntityEnderChest) t;
            } else {
                CompoundTag nbt = new CompoundTag("")
                        .putString("id", BlockEntity.ENDER_CHEST)
                        .putInt("x", (int) this.x)
                        .putInt("y", (int) this.y)
                        .putInt("z", (int) this.z);
                chest = new BlockEntityEnderChest(this.getLevel().getChunk(this.getChunkX(), this.getChunkZ()), nbt);
            }

            if (chest.namedTag.contains("Lock") && chest.namedTag.get("Lock") instanceof StringTag) {
                if (!chest.namedTag.getString("Lock").equals(item.getCustomName())) {
                    return true;
                }
            }

            player.setViewingEnderChest(this);
            player.addWindow(player.getEnderChestInventory());
        }

        return true;
    }

    @Override
    public Item[] getDrops(Item item) {
        if (item.isPickaxe() && item.getTier() >= ItemTool.TIER_WOODEN) {
            return new Item[]{
                    Item.get(OBSIDIAN, 0, 8)
            };
        } else {
            return new Item[0];
        }
    }

    @Override
    public BlockColor getColor() {
        return BlockColor.OBSIDIAN_BLOCK_COLOR;
    }

    public Set<Player> getViewers() {
        return viewers;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public boolean canHarvestWithHand() {
        return false;
    }

    @Override
    public boolean canSilkTouch() {
        return true;
    }

    @Override
    public Item toItem() {
        return Item.get(id);
    }

    @Override
    public BlockFace getBlockFace() {
        return BlockFace.fromHorizontalIndex(this.getDamage() & 0x07);
    }
}
