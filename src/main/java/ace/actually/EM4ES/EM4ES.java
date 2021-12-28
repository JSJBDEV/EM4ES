package ace.actually.EM4ES;

import net.fabricmc.api.ModInitializer;
import net.minecraft.block.ChestBlock;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.item.map.MapIcon;
import net.minecraft.item.map.MapState;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.entry.ItemEntry;
import net.minecraft.loot.entry.LeafEntry;
import net.minecraft.loot.function.ExplorationMapLootFunction;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
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
import java.util.Locale;
import java.util.Random;

public class EM4ES implements ModInitializer {
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LogManager.getLogger("modid");
	private static List<String> VALID_IDS;
	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		try {
			File cfg = new File("./config/EM4ES/StructureWhitelist.cfg");
			if(!cfg.exists())
			{
				FileUtils.writeLines(cfg,Registry.STRUCTURE_FEATURE.getIds());
			}
			VALID_IDS = FileUtils.readLines(cfg,"utf-8");

		} catch (IOException e) {
			e.printStackTrace();
		}
		LOGGER.info("Hello Fabric world!");
	}


	public static ItemStack makeRandomMap(ServerWorld world,BlockPos from)
	{

		StructureFeature sf = Registry.STRUCTURE_FEATURE.get(Identifier.tryParse(VALID_IDS.get(world.random.nextInt(VALID_IDS.size()))));

		BlockPos pos = world.locateStructure(sf,from,1000,false);
		while(pos==null)
		{
			sf = Registry.STRUCTURE_FEATURE.get(Identifier.tryParse(VALID_IDS.get(world.random.nextInt(VALID_IDS.size()))));
			pos = world.locateStructure(sf,from,1000,false);
		}
		ItemStack itemStack = FilledMapItem.createMap(world, pos.getX(), pos.getZ(), (byte) 2, true, true);
		FilledMapItem.fillExplorationMap(world, itemStack);
		addDecorationsNbt(itemStack, pos, "+", sf.hashCode());

		itemStack.setCustomName(new LiteralText(WordUtils.capitalizeFully(sf.getName()).replace("_"," ") +" Map"));
		return itemStack;
	}

	private static int color(int r, int g, int b)
	{
		return r<<(byte)16 + g<<(byte)8 + b;
	}

	public static void addDecorationsNbt(ItemStack stack, BlockPos pos, String id,int randomIn) {
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
		nbtCompound2.putInt("MapColor", color(random.nextInt(255),random.nextInt(255),random.nextInt(255)));

	}
}
