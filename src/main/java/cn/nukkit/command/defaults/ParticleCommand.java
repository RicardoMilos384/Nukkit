package cn.nukkit.command.defaults;

import cn.nukkit.block.Block;
import cn.nukkit.command.CommandSender;
import cn.nukkit.command.data.CommandParamType;
import cn.nukkit.command.data.CommandParameter;
import cn.nukkit.item.Item;
import cn.nukkit.lang.TranslationContainer;
import cn.nukkit.level.Position;
import cn.nukkit.level.particle.*;
import cn.nukkit.math.Vector3f;
import cn.nukkit.player.Player;
import cn.nukkit.utils.TextFormat;

import java.util.Random;

import static cn.nukkit.item.ItemIds.SLIME_BALL;
import static cn.nukkit.item.ItemIds.SNOWBALL;

/**
 * Created on 2015/11/12 by xtypr.
 * Package cn.nukkit.command.defaults in project Nukkit .
 */
public class ParticleCommand extends VanillaCommand {
    private static final String[] ENUM_VALUES = new String[]{"explode", "hugeexplosion", "hugeexplosionseed", "bubble"
            , "splash", "wake", "water", "crit", "smoke", "spell", "instantspell", "dripwater", "driplava", "townaura"
            , "spore", "portal", "flame", "lava", "reddust", "snowballpoof", "slime", "itembreak", "terrain", "heart"
            , "ink", "droplet", "enchantmenttable", "happyvillager", "angryvillager", "forcefield"};
    public ParticleCommand(String name) {
        super(name, "%nukkit.command.particle.description", "%nukkit.command.particle.usage");
        this.setPermission("nukkit.command.particle");
        this.commandParameters.clear();
        this.commandParameters.put("default", new CommandParameter[]{
                new CommandParameter("name", false, ENUM_VALUES),
                new CommandParameter("position", CommandParamType.POSITION, false),
                new CommandParameter("count", CommandParamType.INT, true),
                new CommandParameter("data", true)
        });
    }

    @Override
    public boolean execute(CommandSender sender, String commandLabel, String[] args) {
        if (!this.testPermission(sender)) {
            return true;
        }

        if (args.length < 4) {
            sender.sendMessage(new TranslationContainer("commands.generic.usage", this.usageMessage));

            return true;
        }

        Position defaultPosition;
        if (sender instanceof Player) {
            defaultPosition = ((Player) sender).getPosition();
        } else {
            defaultPosition = new Position(0, 0, 0, sender.getServer().getDefaultLevel());
        }

        String name = args[0].toLowerCase();

        double x;
        double y;
        double z;

        try {
            x = getDouble(args[1], defaultPosition.getX());
            y = getDouble(args[2], defaultPosition.getY());
            z = getDouble(args[3], defaultPosition.getZ());
        } catch (Exception e) {
            return false;
        }
        Position position = new Position(x, y, z, defaultPosition.getLevel());

        int count = 1;
        if (args.length > 4) {
            try {
                double c = Double.valueOf(args[4]);
                count = (int) c;
            } catch (Exception e) {
                //ignore
            }
        }
        count = Math.max(1, count);

        int data = -1;
        if (args.length > 5) {
            try {
                double d = Double.valueOf(args[5]);
                data = (int) d;
            } catch (Exception e) {
                //ignore
            }
        }

        Particle particle = this.getParticle(name, position, data);

        if (particle == null) {
            sender.sendMessage(new TranslationContainer(TextFormat.RED + "%commands.particle.notFound", name));
            return true;
        }

        sender.sendMessage(new TranslationContainer("commands.particle.success", name, String.valueOf(count)));

        Random random = new Random(System.currentTimeMillis());

        for (int i = 0; i < count; i++) {
            particle.setComponents(
                    position.x + (random.nextFloat() * 2 - 1),
                    position.y + (random.nextFloat() * 2 - 1),
                    position.z + (random.nextFloat() * 2 - 1)
            );
            position.getLevel().addParticle(particle);
        }

        return true;
    }

    private Particle getParticle(String name, Vector3f pos, int data) {
        switch (name) {
            case "explode":
                return new ExplodeParticle(pos);
            case "hugeexplosion":
                return new HugeExplodeParticle(pos);
            case "hugeexplosionseed":
                return new HugeExplodeSeedParticle(pos);
            case "bubble":
                return new BubbleParticle(pos);
            case "splash":
                return new SplashParticle(pos);
            case "wake":
            case "water":
                return new WaterParticle(pos);
            case "crit":
                return new CriticalParticle(pos);
            case "smoke":
                return new SmokeParticle(pos, data != -1 ? data : 0);
            case "spell":
                return new EnchantParticle(pos);
            case "instantspell":
                return new InstantEnchantParticle(pos);
            case "dripwater":
                return new WaterDripParticle(pos);
            case "driplava":
                return new LavaDripParticle(pos);
            case "townaura":
            case "spore":
                return new SporeParticle(pos);
            case "portal":
                return new PortalParticle(pos);
            case "flame":
                return new FlameParticle(pos);
            case "lava":
                return new LavaParticle(pos);
            case "reddust":
                return new RedstoneParticle(pos, data != -1 ? data : 1);
            case "snowballpoof":
                return new ItemBreakParticle(pos, Item.get(SNOWBALL));
            case "slime":
                return new ItemBreakParticle(pos, Item.get(SLIME_BALL));
            case "itembreak":
                if (data != -1 && data != 0) {
                    return new ItemBreakParticle(pos, Item.get(data));
                }
                break;
            case "terrain":
                if (data != -1 && data != 0) {
                    return new TerrainParticle(pos, Block.get(data));
                }
                break;
            case "heart":
                return new HeartParticle(pos, data != -1 ? data : 0);
            case "ink":
                return new InkParticle(pos, data != -1 ? data : 0);
            case "droplet":
                return new RainSplashParticle(pos);
            case "enchantmenttable":
                return new EnchantmentTableParticle(pos);
            case "happyvillager":
                return new HappyVillagerParticle(pos);
            case "angryvillager":
                return new AngryVillagerParticle(pos);
            case "forcefield":
                return new BlockForceFieldParticle(pos);
        }

        if (name.startsWith("iconcrack_")) {
            String[] d = name.split("_");
            if (d.length == 3) {
                return new ItemBreakParticle(pos, Item.get(Integer.valueOf(d[1]), Integer.valueOf(d[2])));
            }
        } else if (name.startsWith("blockcrack_")) {
            String[] d = name.split("_");
            if (d.length == 2) {
                return new TerrainParticle(pos, Block.get(Integer.valueOf(d[1]) & 0xff, Integer.valueOf(d[1]) >> 12));
            }
        } else if (name.startsWith("blockdust_")) {
            String[] d = name.split("_");
            if (d.length >= 4) {
                return new DustParticle(pos, Integer.valueOf(d[1]) & 0xff, Integer.valueOf(d[2]) & 0xff, Integer.valueOf(d[3]) & 0xff, d.length >= 5 ? Integer.valueOf(d[4]) & 0xff : 255);
            }
        }

        return null;
    }

    private static double getDouble(String arg, double defaultValue) throws Exception {
        if (arg.startsWith("~")) {
            String relativePos = arg.substring(1);
            if (relativePos.isEmpty()) {
                return defaultValue;
            }
            return defaultValue + Double.parseDouble(relativePos);
        }
        return Double.parseDouble(arg);
    }
}
