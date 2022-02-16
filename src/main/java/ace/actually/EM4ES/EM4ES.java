package ace.actually.EM4ES;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.StructureFeature;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class EM4ES implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LogManager.getLogger("em4es");
    private static final ExecutorService em4esExecutor = Executors.newSingleThreadExecutor();
    private static List<String> VALID_IDS;

    public static Future<ItemStack> makeRandomMap(ServerWorld world, BlockPos from) {
        return em4esExecutor.submit(() -> {
            StructureFeature<?> sf = Registry.STRUCTURE_FEATURE.get(Identifier.tryParse(VALID_IDS.get(world.random.nextInt(VALID_IDS.size()))));
            LOGGER.debug("Searching for structure " + sf.getName() + " around " + from.toShortString() + " in a 1000 block radius...");
            BlockPos pos = world.locateStructure(sf, from, 1000, false);
            while (pos == null) {
                LOGGER.debug("Structure " + sf.getName() + " not found! Generating a new one...");
                sf = Registry.STRUCTURE_FEATURE.get(Identifier.tryParse(VALID_IDS.get(world.random.nextInt(VALID_IDS.size()))));
                LOGGER.debug("Searching for structure " + sf.getName() + " instead...");
                pos = world.locateStructure(sf, from, 1000, false);
            }
            LOGGER.debug("Structure " + sf.getName() + " found!");
            ItemStack itemStack = FilledMapItem.createMap(world, pos.getX(), pos.getZ(), (byte) 2, true, true);
            LOGGER.debug("ItemStack created!");
            new Thread(() -> {  //we have to do this really nasty thing because it can lag very bad
                FilledMapItem.fillExplorationMap(world, itemStack);
                LOGGER.debug("Map filled!");
            });
            Thread.sleep(300);  //wait a bit just because
            addDecorationsNbt(itemStack, pos, "+", sf.hashCode());
            LOGGER.debug("ItemStack Decorated!");

            itemStack.setCustomName(new LiteralText(formatName(sf.getName()) + " Map"));
            LOGGER.debug("ItemStack renamed!");
            return itemStack;
        });
    }

    private static int color(int r, int g, int b) {
        return r << (byte) 16 + g << (byte) 8 + b;
    }

    public static void addDecorationsNbt(ItemStack stack, BlockPos pos, String id, int randomIn) {
        NbtList nbtList;
        if (stack.hasNbt() && stack.getNbt().contains("Decorations", 9)) {
            nbtList = stack.getNbt().getList("Decorations", 10);
        } else {
            nbtList = new NbtList();
            stack.setSubNbt("Decorations", nbtList);
        }
        NbtCompound nbtCompound = new NbtCompound();
        nbtCompound.putByte("type", (byte) 8);
        nbtCompound.putString("id", id);
        nbtCompound.putDouble("x", pos.getX());
        nbtCompound.putDouble("z", pos.getZ());
        nbtCompound.putDouble("rot", 180.0);
        nbtList.add(nbtCompound);
        NbtCompound nbtCompound2 = stack.getOrCreateSubNbt("display");
        Random random = new Random(randomIn);
        nbtCompound2.putInt("MapColor", color(random.nextInt(255), random.nextInt(255), random.nextInt(255)));

    }

    private static String formatName(String name) {
        String[] split = name.split(":");
        if (split.length > 1) name = split[1];
        name = name.replaceAll("_", " ");
        name = WordUtils.capitalizeFully(name);
        return name;
    }

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        try {
            File cfg = new File("./config/EM4ES/StructureWhitelist.cfg");
            if (!cfg.exists()) {
                FileUtils.writeLines(cfg, Registry.STRUCTURE_FEATURE.getIds());
            }
            VALID_IDS = FileUtils.readLines(cfg, "utf-8");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
