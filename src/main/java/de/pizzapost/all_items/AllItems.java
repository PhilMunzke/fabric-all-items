package de.pizzapost.all_items;

import de.pizzapost.all_items.commands.ModCommands;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.SharedConstants;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class AllItems implements ModInitializer {
    public static final String MOD_ID = "all_items";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean started = false;
    static int sec = 0;
    static int min = 0;
    static int hours = 0;
    static int days = 0;
    static int collected_items = 0;
    public static List<Item> items = new ArrayList<>();
    static List<Item> collectedItemsList = new ArrayList<>();
    private static Thread timerThread;
    static int maxItems;

    static List<String> blockedItems = List.of("minecraft:air", "minecraft:barrier", "minecraft:bedrock", "minecraft:chain_command_block", "minecraft:command_block", "minecraft:command_block_minecart", "minecraft:debug_stick", "minecraft:end_portal_frame", "minecraft:farmland", "minecraft:infested_chiseled_stone_bricks", "minecraft:infested_cobblestone", "minecraft:infested_cracked_stone_bricks", "minecraft:infested_deepslate", "minecraft:infested_mossy_stone_bricks", "minecraft:infested_stone", "minecraft:infested_stone_bricks", "minecraft:jigsaw", "minecraft:knowledge_book", "minecraft:light", "minecraft:player_head", "minecraft:reinforced_deepslate", "minecraft:repeating_command_block", "minecraft:rooted_dirt", "minecraft:snow", "minecraft.spawner", "minecraft:structure_block", "minecraft:structure_void", "minecraft:test_block", "minecraft:test_instance_block", "minecraft:trial_spawner", "minecraft:vault", "minecraft:dirt_path", "minecraft:frogspawn");
    static ServerBossEvent collectedItemsBossbar = new ServerBossEvent(UUID.nameUUIDFromBytes((MOD_ID + ":collected_items").getBytes()), Component.translatable("bossbar.all_items.collected_items", collected_items, items.size()), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
    static ServerBossEvent nextItemBossbar = new ServerBossEvent(UUID.nameUUIDFromBytes((MOD_ID + ":next_item").getBytes()), Component.translatable("bossbar.all_items.next_item"), BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS);

    private static Path getSavePath(MinecraftServer server) {
        return server.getWorldPath(LevelResource.ROOT).resolve("all_items_data.txt");
    }

    public static String getCurrentGameVersion() {
        return SharedConstants.getCurrentVersion().name();
    }

    public static int manuallyUpdate(MinecraftServer server) {
        List<String> newItems = List.of("minecraft:polished_sulfur_wall", "minecraft:sulfur_brick_slab", "minecraft:chiseled_cinnabar", "minecraft:cinnabar_wall", "minecraft:cinnabar_slab", "minecraft:sulfur_brick_wall", "minecraft:cinnabar_brick_wall", "minecraft:potent_sulfur", "minecraft:chiseled_sulfur", "minecraft:golden_dandelion", "minecraft:polished_cinnabar", "minecraft:polished_cinnabar_wall", "minecraft:cinnabar_brick_stairs", "minecraft:cinnabar", "minecraft:sulfur_stairs", "minecraft:cinnabar_brick_slab", "minecraft:sulfur_spike", "minecraft:sulfur_brick_stairs", "minecraft:music_disc_bounce", "minecraft:sulfur_slab", "minecraft:polished_sulfur", "minecraft:sulfur", "minecraft:sulfur_cube_bucket", "minecraft:sulfur_bricks", "minecraft:polished_cinnabar_slab", "minecraft:cinnabar_bricks", "minecraft:polished_sulfur_stairs", "minecraft:polished_cinnabar_stairs", "minecraft:sulfur_wall", "minecraft:cinnabar_stairs", "minecraft:polished_sulfur_slab");
        List<String> finishedItems = List.of("minecraft:mushroom_stem", "minecraft:chorus_flower", "minecraft:bamboo_fence_gate", "minecraft:egg", "minecraft:glow_berries", "minecraft:firefly_bush", "minecraft:fire_coral", "minecraft:pufferfish_bucket", "minecraft:sugar", "minecraft:exposed_chiseled_copper", "minecraft:weathered_copper_golem_statue", "minecraft:polished_tuff_slab", "minecraft:crimson_fence_gate", "minecraft:black_candle", "minecraft:pink_stained_glass", "minecraft:red_wool", "minecraft:pumpkin_pie", "minecraft:cut_copper_slab", "minecraft:grindstone", "minecraft:pink_tulip", "minecraft:music_disc_11", "minecraft:polished_blackstone_pressure_plate", "minecraft:jungle_trapdoor", "minecraft:lime_stained_glass", "minecraft:waxed_oxidized_copper", "minecraft:cherry_sapling", "minecraft:mossy_cobblestone_stairs", "minecraft:tadpole_bucket", "minecraft:cut_red_sandstone_slab", "minecraft:black_bundle", "minecraft:warped_roots", "minecraft:cherry_pressure_plate", "minecraft:bread", "minecraft:cornflower", "minecraft:large_fern", "minecraft:waxed_copper_grate", "minecraft:lapis_block", "minecraft:hanging_roots", "minecraft:brain_coral_fan", "minecraft:magenta_stained_glass", "minecraft:jungle_shelf", "minecraft:host_armor_trim_smithing_template", "minecraft:mossy_stone_bricks", "minecraft:brick_stairs", "minecraft:orange_banner", "minecraft:dried_ghast", "minecraft:andesite_slab", "minecraft:music_disc_5", "minecraft:cobblestone", "minecraft:tuff", "minecraft:sand", "minecraft:weathered_cut_copper_slab", "minecraft:blue_shulker_box", "minecraft:respawn_anchor", "minecraft:blade_pottery_sherd", "minecraft:sculk_shrieker", "minecraft:red_sandstone", "minecraft:oak_fence_gate", "minecraft:jukebox", "minecraft:stone_brick_wall", "minecraft:resin_bricks", "minecraft:resin_clump", "minecraft:dead_bubble_coral", "minecraft:baked_potato", "minecraft:spruce_wood", "minecraft:mossy_stone_brick_stairs", "minecraft:honeycomb_block", "minecraft:crimson_fungus", "minecraft:birch_door", "minecraft:elytra", "minecraft:dropper", "minecraft:lime_concrete", "minecraft:deepslate_tiles", "minecraft:copper_pickaxe", "minecraft:observer", "minecraft:white_shulker_box", "minecraft:piglin_banner_pattern", "minecraft:waxed_oxidized_copper_trapdoor", "minecraft:turtle_scute", "minecraft:pale_oak_stairs", "minecraft:purpur_stairs", "minecraft:orange_bundle", "minecraft:guster_banner_pattern", "minecraft:target", "minecraft:light_gray_shulker_box", "minecraft:iron_sword", "minecraft:leather_chestplate", "minecraft:blue_orchid", "minecraft:pale_oak_button", "minecraft:pearlescent_froglight", "minecraft:waxed_weathered_copper_bulb", "minecraft:copper_boots", "minecraft:light_blue_stained_glass_pane", "minecraft:magenta_concrete_powder", "minecraft:light_blue_glazed_terracotta", "minecraft:stone", "minecraft:beetroot", "minecraft:gray_carpet", "minecraft:jungle_button", "minecraft:birch_sign", "minecraft:waxed_copper_chest", "minecraft:blue_dye", "minecraft:nether_brick_wall", "minecraft:weeping_vines", "minecraft:cobblestone_slab", "minecraft:stripped_jungle_log", "minecraft:mangrove_log", "minecraft:copper_leggings", "minecraft:light_gray_dye", "minecraft:copper_horse_armor", "minecraft:creeper_head", "minecraft:brown_egg", "minecraft:gravel", "minecraft:rabbit", "minecraft:smoker", "minecraft:quartz_slab", "minecraft:waxed_weathered_cut_copper_slab", "minecraft:red_bundle", "minecraft:leather", "minecraft:diamond_helmet", "minecraft:bamboo_chest_raft", "minecraft:polished_deepslate_stairs", "minecraft:green_banner", "minecraft:torchflower", "minecraft:polished_deepslate_slab", "minecraft:honey_bottle", "minecraft:hopper_minecart", "minecraft:ink_sac", "minecraft:acacia_leaves", "minecraft:pale_oak_sapling", "minecraft:blue_bundle", "minecraft:poisonous_potato", "minecraft:lime_harness", "minecraft:oxidized_cut_copper_stairs", "minecraft:basalt", "minecraft:armadillo_scute", "minecraft:bamboo_planks", "minecraft:activator_rail", "minecraft:netherite_leggings", "minecraft:spruce_pressure_plate", "minecraft:pale_oak_slab", "minecraft:oxidized_copper_chest", "minecraft:polished_tuff_wall", "minecraft:waxed_exposed_lightning_rod", "minecraft:polished_andesite_stairs", "minecraft:iron_block", "minecraft:waxed_exposed_copper_golem_statue", "minecraft:light_gray_carpet", "minecraft:dripstone_block", "minecraft:crimson_roots", "minecraft:fern", "minecraft:orange_concrete_powder", "minecraft:bamboo_fence", "minecraft:loom", "minecraft:chainmail_chestplate", "minecraft:lily_pad", "minecraft:allium", "minecraft:white_tulip", "minecraft:black_carpet", "minecraft:fletching_table", "minecraft:sculk_sensor", "minecraft:blaze_rod", "minecraft:stone_button", "minecraft:repeater", "minecraft:weathered_cut_copper_stairs", "minecraft:spruce_button", "minecraft:bowl", "minecraft:oak_leaves", "minecraft:cyan_harness", "minecraft:horn_coral_fan", "minecraft:stripped_birch_wood", "minecraft:golden_nautilus_armor", "minecraft:diamond_horse_armor", "minecraft:light_gray_harness", "minecraft:pale_oak_log", "minecraft:copper_chestplate", "minecraft:brewing_stand", "minecraft:melon_slice", "minecraft:amethyst_cluster", "minecraft:cauldron", "minecraft:pitcher_plant", "minecraft:netherite_horse_armor", "minecraft:chiseled_quartz_block", "minecraft:shield", "minecraft:waxed_exposed_chiseled_copper", "minecraft:mangrove_shelf", "minecraft:red_sandstone_wall", "minecraft:ward_armor_trim_smithing_template", "minecraft:smooth_red_sandstone_slab", "minecraft:music_disc_cat", "minecraft:smooth_quartz_stairs", "minecraft:skull_pottery_sherd", "minecraft:sweet_berries", "minecraft:bell", "minecraft:sandstone_slab", "minecraft:pink_dye", "minecraft:polished_granite_slab", "minecraft:polished_diorite_stairs", "minecraft:oak_sapling", "minecraft:birch_hanging_sign", "minecraft:acacia_button", "minecraft:purple_bundle", "minecraft:crimson_trapdoor", "minecraft:purple_stained_glass", "minecraft:tinted_glass", "minecraft:waxed_exposed_cut_copper_stairs", "minecraft:weathered_copper", "minecraft:iron_pickaxe", "minecraft:cooked_cod", "minecraft:dark_oak_door", "minecraft:chainmail_boots", "minecraft:arrow", "minecraft:vine", "minecraft:iron_helmet", "minecraft:fire_coral_block", "minecraft:jungle_slab", "minecraft:diamond_pickaxe", "minecraft:detector_rail", "minecraft:cyan_terracotta", "minecraft:green_concrete_powder", "minecraft:orange_wool", "minecraft:sentry_armor_trim_smithing_template", "minecraft:melon", "minecraft:purple_banner", "minecraft:diamond_hoe", "minecraft:explorer_pottery_sherd", "minecraft:large_amethyst_bud", "minecraft:cooked_salmon", "minecraft:chorus_plant", "minecraft:mutton", "minecraft:spruce_trapdoor", "minecraft:cherry_log", "minecraft:salmon", "minecraft:water_bucket", "minecraft:lightning_rod", "minecraft:spruce_slab", "minecraft:feather", "minecraft:moss_carpet", "minecraft:exposed_copper_lantern", "minecraft:orange_stained_glass_pane", "minecraft:yellow_bed", "minecraft:spruce_leaves", "minecraft:closed_eyeblossom", "minecraft:pink_glazed_terracotta", "minecraft:gray_candle", "minecraft:green_harness", "minecraft:stone_sword", "minecraft:brown_bundle", "minecraft:lilac", "minecraft:weathered_copper_chest", "minecraft:horn_coral_block", "minecraft:sandstone", "minecraft:warped_door", "minecraft:lapis_lazuli", "minecraft:packed_ice", "minecraft:copper_sword", "minecraft:waxed_copper_block", "minecraft:bone_meal", "minecraft:pumpkin", "minecraft:blast_furnace", "minecraft:stripped_cherry_wood", "minecraft:rabbit_hide", "minecraft:beef", "minecraft:cherry_trapdoor", "minecraft:pale_oak_fence_gate", "minecraft:lime_glazed_terracotta", "minecraft:emerald_ore", "minecraft:lime_banner", "minecraft:heart_of_the_sea", "minecraft:stripped_mangrove_wood", "minecraft:red_nether_bricks", "minecraft:composter", "minecraft:quartz_block", "minecraft:oak_boat", "minecraft:pink_banner", "minecraft:gray_dye", "minecraft:golden_carrot", "minecraft:dead_bubble_coral_block", "minecraft:waxed_oxidized_cut_copper", "minecraft:stone_brick_slab", "minecraft:blue_concrete", "minecraft:blue_candle", "minecraft:crimson_sign", "minecraft:red_banner", "minecraft:cherry_fence", "minecraft:spruce_shelf", "minecraft:stripped_dark_oak_log", "minecraft:chiseled_stone_bricks", "minecraft:name_tag", "minecraft:birch_fence_gate", "minecraft:netherite_spear", "minecraft:diorite_slab", "minecraft:weathered_copper_grate", "minecraft:orange_tulip", "minecraft:cooked_beef", "minecraft:crossbow", "minecraft:mycelium", "minecraft:rabbit_stew", "minecraft:copper_helmet", "minecraft:dark_oak_planks", "minecraft:stone_hoe", "minecraft:wooden_pickaxe", "minecraft:nautilus_shell", "minecraft:purple_wool", "minecraft:black_glazed_terracotta", "minecraft:oak_shelf", "minecraft:stone_pressure_plate", "minecraft:crimson_planks", "minecraft:powder_snow_bucket", "minecraft:nether_star", "minecraft:crimson_fence", "minecraft:tall_dry_grass", "minecraft:lime_stained_glass_pane", "minecraft:quartz_bricks", "minecraft:petrified_oak_slab", "minecraft:yellow_glazed_terracotta", "minecraft:ominous_bottle", "minecraft:diamond_boots", "minecraft:dark_oak_fence", "minecraft:red_sandstone_stairs", "minecraft:verdant_froglight", "minecraft:sponge", "minecraft:firework_rocket", "minecraft:prismarine_brick_slab", "minecraft:pale_moss_carpet", "minecraft:dark_oak_sign", "minecraft:waxed_copper_lantern", "minecraft:breeze_rod", "minecraft:beacon", "minecraft:smooth_sandstone_slab", "minecraft:exposed_copper_trapdoor", "minecraft:music_disc_precipice", "minecraft:iron_spear", "minecraft:ochre_froglight", "minecraft:pitcher_pod", "minecraft:red_sandstone_slab", "minecraft:light_gray_glazed_terracotta", "minecraft:bamboo", "minecraft:pale_oak_wood", "minecraft:peony", "minecraft:chiseled_copper", "minecraft:smooth_sandstone_stairs", "minecraft:diamond_chestplate", "minecraft:armor_stand", "minecraft:gray_glazed_terracotta", "minecraft:blackstone_stairs", "minecraft:waxed_exposed_copper_bulb", "minecraft:recovery_compass", "minecraft:glistering_melon_slice", "minecraft:jungle_leaves", "minecraft:waxed_oxidized_lightning_rod", "minecraft:warped_slab", "minecraft:brown_stained_glass_pane", "minecraft:blue_banner", "minecraft:orange_terracotta", "minecraft:small_amethyst_bud", "minecraft:hay_block", "minecraft:red_harness", "minecraft:pale_oak_pressure_plate", "minecraft:light_gray_bed", "minecraft:gray_concrete", "minecraft:stone_spear", "minecraft:blue_glazed_terracotta", "minecraft:bucket", "minecraft:netherite_helmet", "minecraft:soul_sand", "minecraft:cherry_stairs", "minecraft:wooden_hoe", "minecraft:red_tulip", "minecraft:tide_armor_trim_smithing_template", "minecraft:acacia_wood", "minecraft:cherry_shelf", "minecraft:brown_bed", "minecraft:dark_oak_button", "minecraft:dune_armor_trim_smithing_template", "minecraft:obsidian", "minecraft:snowball", "minecraft:compass", "minecraft:lectern", "minecraft:honey_block", "minecraft:slime_ball", "minecraft:amethyst_block", "minecraft:smooth_red_sandstone", "minecraft:waxed_exposed_copper_bars", "minecraft:music_disc_mellohi", "minecraft:polished_deepslate", "minecraft:suspicious_stew", "minecraft:diamond_spear", "minecraft:friend_pottery_sherd", "minecraft:copper_bulb", "minecraft:lantern", "minecraft:purpur_slab", "minecraft:light_blue_bed", "minecraft:prismarine_stairs", "minecraft:dark_oak_sapling", "minecraft:shaper_armor_trim_smithing_template", "minecraft:quartz", "minecraft:dead_fire_coral_block", "minecraft:bone_block", "minecraft:polished_granite_stairs", "minecraft:torch", "minecraft:polished_blackstone", "minecraft:birch_pressure_plate", "minecraft:flower_pot", "minecraft:red_concrete", "minecraft:red_concrete_powder", "minecraft:dead_bush", "minecraft:jungle_log", "minecraft:wooden_spear", "minecraft:oxidized_cut_copper_slab", "minecraft:iron_nugget", "minecraft:fire_coral_fan", "minecraft:green_bundle", "minecraft:jungle_fence", "minecraft:turtle_egg", "minecraft:spruce_fence", "minecraft:magenta_stained_glass_pane", "minecraft:dark_oak_stairs", "minecraft:waxed_copper_trapdoor", "minecraft:red_shulker_box", "minecraft:resin_brick", "minecraft:goat_horn", "minecraft:mace", "minecraft:ender_pearl", "minecraft:ladder", "minecraft:bamboo_raft", "minecraft:cyan_concrete_powder", "minecraft:acacia_hanging_sign", "minecraft:chiseled_polished_blackstone", "minecraft:purple_dye", "minecraft:ancient_debris", "minecraft:mossy_stone_brick_slab", "minecraft:waxed_weathered_copper_door", "minecraft:waxed_oxidized_copper_lantern", "minecraft:magenta_wool", "minecraft:lead", "minecraft:deepslate_tile_stairs", "minecraft:stripped_acacia_log", "minecraft:deepslate_diamond_ore", "minecraft:tuff_brick_wall", "minecraft:golden_pickaxe", "minecraft:exposed_copper_golem_statue", "minecraft:oxidized_copper_grate", "minecraft:bamboo_mosaic_slab", "minecraft:brewer_pottery_sherd", "minecraft:mourner_pottery_sherd", "minecraft:snout_armor_trim_smithing_template", "minecraft:waxed_oxidized_copper_golem_statue", "minecraft:birch_slab", "minecraft:spruce_sapling", "minecraft:jungle_planks", "minecraft:jungle_sapling", "minecraft:music_disc_13", "minecraft:sticky_piston", "minecraft:waxed_oxidized_copper_bulb", "minecraft:purple_shulker_box", "minecraft:creaking_heart", "minecraft:netherite_hoe", "minecraft:dark_prismarine", "minecraft:enchanted_book", "minecraft:andesite_wall", "minecraft:music_disc_mall", "minecraft:stripped_birch_log", "minecraft:bamboo_button", "minecraft:cooked_chicken", "minecraft:blue_stained_glass", "minecraft:quartz_pillar", "minecraft:andesite", "minecraft:packed_mud", "minecraft:netherite_boots", "minecraft:yellow_wool", "minecraft:waxed_exposed_copper", "minecraft:sniffer_egg", "minecraft:bamboo_pressure_plate", "minecraft:cooked_mutton", "minecraft:mangrove_button", "minecraft:carrot_on_a_stick", "minecraft:netherite_shovel", "minecraft:honeycomb", "minecraft:white_terracotta", "minecraft:soul_lantern", "minecraft:lime_bed", "minecraft:waxed_weathered_copper_lantern", "minecraft:warped_hanging_sign", "minecraft:music_disc_relic", "minecraft:exposed_copper_grate", "minecraft:chainmail_helmet", "minecraft:waxed_oxidized_copper_chain", "minecraft:crimson_pressure_plate", "minecraft:magma_cream", "minecraft:golden_apple", "minecraft:short_dry_grass", "minecraft:lime_candle", "minecraft:dead_tube_coral_block", "minecraft:iron_boots", "minecraft:scaffolding", "minecraft:bamboo_sign", "minecraft:mangrove_stairs", "minecraft:writable_book", "minecraft:shroomlight", "minecraft:lime_concrete_powder", "minecraft:copper_spear", "minecraft:weathered_chiseled_copper", "minecraft:stripped_dark_oak_wood", "minecraft:slime_block", "minecraft:stone_pickaxe", "minecraft:tipped_arrow", "minecraft:flow_pottery_sherd", "minecraft:waxed_copper_bars", "minecraft:music_disc_blocks", "minecraft:blue_bed", "minecraft:jungle_sign", "minecraft:leather_boots", "minecraft:cyan_candle", "minecraft:spruce_fence_gate", "minecraft:oxidized_copper", "minecraft:birch_fence", "minecraft:black_wool", "minecraft:iron_chestplate", "minecraft:milk_bucket", "minecraft:magenta_terracotta", "minecraft:apple", "minecraft:polished_blackstone_wall", "minecraft:dead_fire_coral", "minecraft:white_stained_glass", "minecraft:end_stone_brick_wall", "minecraft:yellow_bundle", "minecraft:copper_ore", "minecraft:raw_gold", "minecraft:diamond", "minecraft:birch_stairs", "minecraft:salmon_bucket", "minecraft:spruce_door", "minecraft:diorite_stairs", "minecraft:dark_oak_hanging_sign", "minecraft:yellow_stained_glass", "minecraft:diamond_leggings", "minecraft:wheat", "minecraft:string", "minecraft:copper_grate", "minecraft:chorus_fruit", "minecraft:acacia_stairs", "minecraft:red_carpet", "minecraft:sculk", "minecraft:iron_door", "minecraft:waxed_copper_bulb", "minecraft:crimson_stairs", "minecraft:mangrove_fence_gate", "minecraft:mangrove_hanging_sign", "minecraft:barrel", "minecraft:purple_glazed_terracotta", "minecraft:acacia_door", "minecraft:hopper", "minecraft:golden_boots", "minecraft:warped_fence_gate", "minecraft:birch_shelf", "minecraft:music_disc_wait", "minecraft:green_bed", "minecraft:warped_fungus_on_a_stick", "minecraft:wind_charge", "minecraft:grass_block", "minecraft:black_stained_glass_pane", "minecraft:redstone_block", "minecraft:golden_helmet", "minecraft:dried_kelp", "minecraft:bamboo_mosaic", "minecraft:trident", "minecraft:shulker_shell", "minecraft:end_stone_brick_slab", "minecraft:birch_wood", "minecraft:mud_brick_wall", "minecraft:oxidized_copper_golem_statue", "minecraft:diamond_block", "minecraft:cyan_concrete", "minecraft:birch_boat", "minecraft:lava_bucket", "minecraft:plenty_pottery_sherd", "minecraft:terracotta", "minecraft:pink_petals", "minecraft:suspicious_gravel", "minecraft:warped_pressure_plate", "minecraft:dragon_head", "minecraft:yellow_concrete_powder", "minecraft:stone_bricks", "minecraft:coarse_dirt", "minecraft:weathered_copper_bulb", "minecraft:paper", "minecraft:crafting_table", "minecraft:brown_glazed_terracotta", "minecraft:magenta_bundle", "minecraft:pink_bundle", "minecraft:light_gray_bundle", "minecraft:cooked_rabbit", "minecraft:waxed_weathered_copper_trapdoor", "minecraft:stonecutter", "minecraft:nether_gold_ore", "minecraft:brown_mushroom", "minecraft:warped_nylium", "minecraft:short_grass", "minecraft:magenta_carpet", "minecraft:chiseled_tuff", "minecraft:polished_andesite", "minecraft:gold_ingot", "minecraft:resin_block", "minecraft:acacia_sign", "minecraft:gray_concrete_powder", "minecraft:enchanting_table", "minecraft:fermented_spider_eye", "minecraft:pink_harness", "minecraft:spawner", "minecraft:diorite", "minecraft:cooked_porkchop", "minecraft:warped_trapdoor", "minecraft:birch_planks", "minecraft:exposed_copper_bulb", "minecraft:exposed_lightning_rod", "minecraft:warped_wart_block", "minecraft:nether_bricks", "minecraft:pink_stained_glass_pane", "minecraft:waxed_oxidized_chiseled_copper", "minecraft:waxed_oxidized_copper_bars", "minecraft:nether_sprouts", "minecraft:dark_oak_chest_boat", "minecraft:stripped_mangrove_log", "minecraft:oxidized_copper_trapdoor", "minecraft:miner_pottery_sherd", "minecraft:waxed_cut_copper", "minecraft:red_mushroom", "minecraft:stone_brick_stairs", "minecraft:small_dripleaf", "minecraft:popped_chorus_fruit", "minecraft:oxidized_copper_lantern", "minecraft:cut_copper", "minecraft:cobbled_deepslate_slab", "minecraft:scrape_pottery_sherd", "minecraft:light_gray_concrete_powder", "minecraft:magma_block", "minecraft:dried_kelp_block", "minecraft:polished_blackstone_button", "minecraft:oak_fence", "minecraft:oxidized_cut_copper", "minecraft:netherite_block", "minecraft:dragon_breath", "minecraft:crying_obsidian", "minecraft:pointed_dripstone", "minecraft:yellow_terracotta", "minecraft:golden_spear", "minecraft:diamond_axe", "minecraft:mangrove_chest_boat", "minecraft:waxed_oxidized_cut_copper_stairs", "minecraft:gray_bundle", "minecraft:guster_pottery_sherd", "minecraft:bow", "minecraft:tuff_wall", "minecraft:turtle_helmet", "minecraft:trapped_chest", "minecraft:polished_tuff", "minecraft:golden_shovel", "minecraft:iron_trapdoor", "minecraft:porkchop", "minecraft:zombie_head", "minecraft:melon_seeds", "minecraft:quartz_stairs", "minecraft:netherite_pickaxe", "minecraft:sandstone_stairs", "minecraft:orange_stained_glass", "minecraft:flow_armor_trim_smithing_template", "minecraft:firework_star", "minecraft:bee_nest", "minecraft:pale_oak_trapdoor", "minecraft:totem_of_undying", "minecraft:dark_prismarine_stairs", "minecraft:spruce_sign", "minecraft:stripped_pale_oak_log", "minecraft:nether_wart", "minecraft:golden_horse_armor", "minecraft:deepslate_iron_ore", "minecraft:medium_amethyst_bud", "minecraft:raw_gold_block", "minecraft:heart_pottery_sherd", "minecraft:light_gray_terracotta", "minecraft:brown_candle", "minecraft:waxed_weathered_copper_grate", "minecraft:blue_wool", "minecraft:torchflower_seeds", "minecraft:bamboo_hanging_sign", "minecraft:tnt", "minecraft:dark_oak_shelf", "minecraft:nether_quartz_ore", "minecraft:pink_terracotta", "minecraft:wolf_armor", "minecraft:waxed_weathered_copper_bars", "minecraft:mangrove_planks", "minecraft:bundle", "minecraft:purple_concrete", "minecraft:cherry_button", "minecraft:blue_harness", "minecraft:white_glazed_terracotta", "minecraft:filled_map", "minecraft:acacia_fence_gate", "minecraft:heartbreak_pottery_sherd", "minecraft:copper_chest", "minecraft:cherry_fence_gate", "minecraft:light_blue_bundle", "minecraft:orange_bed", "minecraft:wooden_axe", "minecraft:light_gray_wool", "minecraft:trial_key", "minecraft:red_mushroom_block", "minecraft:pale_oak_fence", "minecraft:smithing_table", "minecraft:smooth_basalt", "minecraft:brown_concrete_powder", "minecraft:dead_brain_coral", "minecraft:waxed_cut_copper_stairs", "minecraft:prismarine_slab", "minecraft:spectral_arrow", "minecraft:copper_golem_statue", "minecraft:red_bed", "minecraft:copper_shovel", "minecraft:prismarine", "minecraft:chicken", "minecraft:red_stained_glass", "minecraft:waxed_weathered_chiseled_copper", "minecraft:white_concrete", "minecraft:oak_trapdoor", "minecraft:pale_hanging_moss", "minecraft:copper_lantern", "minecraft:lodestone", "minecraft:dark_oak_wood", "minecraft:stone_axe", "minecraft:red_nether_brick_stairs", "minecraft:netherite_scrap", "minecraft:pale_oak_sign", "minecraft:lever", "minecraft:wildflowers", "minecraft:gunpowder", "minecraft:oak_button", "minecraft:snort_pottery_sherd", "minecraft:netherite_sword", "minecraft:spruce_stairs", "minecraft:purple_candle", "minecraft:iron_ore", "minecraft:piston", "minecraft:kelp", "minecraft:bookshelf", "minecraft:creeper_banner_pattern", "minecraft:emerald", "minecraft:blaze_powder", "minecraft:spruce_planks", "minecraft:iron_leggings", "minecraft:brown_stained_glass", "minecraft:clay", "minecraft:green_stained_glass_pane", "minecraft:cherry_wood", "minecraft:cobweb", "minecraft:phantom_membrane", "minecraft:waxed_oxidized_copper_door", "minecraft:iron_axe", "minecraft:soul_soil", "minecraft:bush", "minecraft:waxed_lightning_rod", "minecraft:cobblestone_stairs", "minecraft:furnace_minecart", "minecraft:bamboo_trapdoor", "minecraft:magenta_harness", "minecraft:black_concrete", "minecraft:purpur_block", "minecraft:dark_oak_slab", "minecraft:tropical_fish", "minecraft:waxed_weathered_copper_chain", "minecraft:light_blue_harness", "minecraft:green_terracotta", "minecraft:coal", "minecraft:deepslate_lapis_ore", "minecraft:music_disc_otherside", "minecraft:stripped_jungle_wood", "minecraft:white_banner", "minecraft:light_blue_shulker_box", "minecraft:waxed_weathered_cut_copper_stairs", "minecraft:birch_button", "minecraft:light_weighted_pressure_plate", "minecraft:flower_banner_pattern", "minecraft:crimson_hanging_sign", "minecraft:sculk_vein", "minecraft:waxed_oxidized_copper_chest", "minecraft:coal_block", "minecraft:cake", "minecraft:sheaf_pottery_sherd", "minecraft:leaf_litter", "minecraft:mangrove_door", "minecraft:diamond_sword", "minecraft:prismarine_wall", "minecraft:spruce_hanging_sign", "minecraft:enchanted_golden_apple", "minecraft:heavy_core", "minecraft:lime_dye", "minecraft:cocoa_beans", "minecraft:dead_tube_coral", "minecraft:cobbled_deepslate_stairs", "minecraft:golden_sword", "minecraft:calcite", "minecraft:polished_blackstone_bricks", "minecraft:cartography_table", "minecraft:iron_shovel", "minecraft:cod", "minecraft:dead_fire_coral_fan", "minecraft:music_disc_tears", "minecraft:waxed_exposed_copper_chain", "minecraft:wheat_seeds", "minecraft:copper_door", "minecraft:black_terracotta", "minecraft:cherry_boat", "minecraft:deepslate_brick_stairs", "minecraft:yellow_harness", "minecraft:warped_hyphae", "minecraft:cherry_slab", "minecraft:redstone", "minecraft:shulker_box", "minecraft:flowering_azalea_leaves", "minecraft:gold_ore", "minecraft:oxidized_lightning_rod", "minecraft:pink_candle", "minecraft:polished_granite", "minecraft:eye_armor_trim_smithing_template", "minecraft:green_glazed_terracotta", "minecraft:stripped_warped_stem", "minecraft:gray_bed", "minecraft:mushroom_stew", "minecraft:brush", "minecraft:copper_nautilus_armor", "minecraft:exposed_copper_bars", "minecraft:jack_o_lantern", "minecraft:black_concrete_powder", "minecraft:brown_terracotta", "minecraft:oak_chest_boat", "minecraft:red_stained_glass_pane", "minecraft:golden_axe", "minecraft:tube_coral_fan", "minecraft:music_disc_lava_chicken", "minecraft:deepslate_tile_slab", "minecraft:raw_copper", "minecraft:black_bed", "minecraft:decorated_pot", "minecraft:dark_oak_log", "minecraft:cobblestone_wall", "minecraft:pink_wool", "minecraft:wild_armor_trim_smithing_template", "minecraft:green_carpet", "minecraft:diamond_shovel", "minecraft:glowstone_dust", "minecraft:deepslate_redstone_ore", "minecraft:cactus_flower", "minecraft:dispenser", "minecraft:dirt", "minecraft:blackstone_wall", "minecraft:wayfinder_armor_trim_smithing_template", "minecraft:magenta_concrete", "minecraft:brown_harness", "minecraft:oak_pressure_plate", "minecraft:yellow_concrete", "minecraft:prismarine_bricks", "minecraft:copper_bars", "minecraft:azalea", "minecraft:granite", "minecraft:tuff_brick_slab", "minecraft:dead_bubble_coral_fan", "minecraft:pale_oak_boat", "minecraft:weathered_copper_chain", "minecraft:red_sand", "minecraft:oak_stairs", "minecraft:nether_wart_block", "minecraft:deepslate_gold_ore", "minecraft:bamboo_door", "minecraft:rose_bush", "minecraft:warped_button", "minecraft:iron_hoe", "minecraft:chiseled_resin_bricks", "minecraft:exposed_copper_chain", "minecraft:crimson_slab", "minecraft:purple_carpet", "minecraft:bamboo_stairs", "minecraft:polished_blackstone_brick_wall", "minecraft:warped_fungus", "minecraft:pale_oak_chest_boat", "minecraft:stripped_cherry_log", "minecraft:gray_banner", "minecraft:dark_oak_trapdoor", "minecraft:weathered_copper_door", "minecraft:jungle_wood", "minecraft:fire_charge", "minecraft:weathered_copper_bars", "minecraft:waxed_exposed_copper_grate", "minecraft:dead_brain_coral_fan", "minecraft:fishing_rod", "minecraft:bubble_coral_fan", "minecraft:cherry_sign", "minecraft:copper_chain", "minecraft:glow_lichen", "minecraft:smooth_stone_slab", "minecraft:tropical_fish_bucket", "minecraft:mangrove_pressure_plate", "minecraft:glowstone", "minecraft:danger_pottery_sherd", "minecraft:light_blue_dye", "minecraft:azure_bluet", "minecraft:cyan_glazed_terracotta", "minecraft:bubble_coral_block", "minecraft:raw_iron_block", "minecraft:light_blue_carpet", "minecraft:jungle_boat", "minecraft:piglin_head", "minecraft:blue_carpet", "minecraft:jungle_pressure_plate", "minecraft:resin_brick_wall", "minecraft:book", "minecraft:chest_minecart", "minecraft:cracked_nether_bricks", "minecraft:campfire", "minecraft:beehive", "minecraft:cyan_wool", "minecraft:stripped_oak_wood", "minecraft:pale_oak_shelf", "minecraft:charcoal", "minecraft:tuff_slab", "minecraft:diorite_wall", "minecraft:waxed_exposed_cut_copper", "minecraft:stone_slab", "minecraft:stripped_oak_log", "minecraft:gold_nugget", "minecraft:pink_concrete", "minecraft:andesite_stairs", "minecraft:pale_oak_door", "minecraft:polished_blackstone_brick_slab", "minecraft:mangrove_fence", "minecraft:yellow_candle", "minecraft:purpur_pillar", "minecraft:tnt_minecart", "minecraft:light_blue_concrete_powder", "minecraft:mangrove_wood", "minecraft:crimson_button", "minecraft:red_nether_brick_slab", "minecraft:stripped_warped_hyphae", "minecraft:cyan_stained_glass_pane", "minecraft:nether_brick_fence", "minecraft:stripped_bamboo_block", "minecraft:bamboo_mosaic_stairs", "minecraft:dandelion", "minecraft:cactus", "minecraft:light_gray_candle", "minecraft:cherry_chest_boat", "minecraft:cherry_door", "minecraft:bone", "minecraft:conduit", "minecraft:black_stained_glass", "minecraft:mossy_stone_brick_wall", "minecraft:mangrove_roots", "minecraft:rail", "minecraft:ender_chest", "minecraft:birch_chest_boat", "minecraft:podzol", "minecraft:acacia_pressure_plate", "minecraft:stripped_spruce_log", "minecraft:smooth_quartz", "minecraft:deepslate_tile_wall", "minecraft:nether_brick_slab", "minecraft:white_harness", "minecraft:seagrass", "minecraft:powered_rail", "minecraft:flint_and_steel", "minecraft:jungle_fence_gate", "minecraft:gilded_blackstone", "minecraft:stone_stairs", "minecraft:golden_chestplate", "minecraft:rib_armor_trim_smithing_template", "minecraft:shelter_pottery_sherd", "minecraft:end_stone_bricks", "minecraft:yellow_dye", "minecraft:silence_armor_trim_smithing_template", "minecraft:dark_prismarine_slab", "minecraft:stripped_spruce_wood", "minecraft:echo_shard", "minecraft:tripwire_hook", "minecraft:potato", "minecraft:brown_concrete", "minecraft:prismarine_shard", "minecraft:snow_block", "minecraft:polished_blackstone_stairs", "minecraft:cherry_planks", "minecraft:item_frame", "minecraft:tuff_bricks", "minecraft:exposed_cut_copper_slab", "minecraft:polished_diorite_slab", "minecraft:lime_terracotta", "minecraft:exposed_cut_copper", "minecraft:green_candle", "minecraft:light_gray_stained_glass", "minecraft:green_wool", "minecraft:exposed_cut_copper_stairs", "minecraft:pink_bed", "minecraft:arms_up_pottery_sherd", "minecraft:iron_bars", "minecraft:spruce_log", "minecraft:stripped_pale_oak_wood", "minecraft:warped_sign", "minecraft:cyan_stained_glass", "minecraft:copper_hoe", "minecraft:glow_item_frame", "minecraft:damaged_anvil", "minecraft:stone_shovel", "minecraft:mangrove_slab", "minecraft:red_terracotta", "minecraft:iron_nautilus_armor", "minecraft:magenta_dye", "minecraft:brown_dye", "minecraft:glass", "minecraft:cherry_hanging_sign", "minecraft:dark_oak_boat", "minecraft:music_disc_pigstep", "minecraft:soul_torch", "minecraft:cracked_deepslate_bricks", "minecraft:gray_harness", "minecraft:music_disc_chirp", "minecraft:pale_moss_block", "minecraft:acacia_sapling", "minecraft:pale_oak_planks", "minecraft:exposed_copper", "minecraft:brain_coral_block", "minecraft:raw_iron", "minecraft:budding_amethyst", "minecraft:red_candle", "minecraft:granite_slab", "minecraft:gray_shulker_box", "minecraft:cobbled_deepslate_wall", "minecraft:white_wool", "minecraft:howl_pottery_sherd", "minecraft:flow_banner_pattern", "minecraft:acacia_trapdoor", "minecraft:coal_ore", "minecraft:oak_sign", "minecraft:acacia_shelf", "minecraft:shears", "minecraft:stripped_crimson_hyphae", "minecraft:mossy_cobblestone", "minecraft:oak_door", "minecraft:globe_banner_pattern", "minecraft:blackstone_slab", "minecraft:music_disc_creator", "minecraft:cod_bucket", "minecraft:acacia_chest_boat", "minecraft:black_dye", "minecraft:light_blue_banner", "minecraft:waxed_exposed_copper_lantern", "minecraft:yellow_banner", "minecraft:chipped_anvil", "minecraft:acacia_planks", "minecraft:lingering_potion", "minecraft:crimson_shelf", "minecraft:yellow_carpet", "minecraft:archer_pottery_sherd", "minecraft:map", "minecraft:light_gray_stained_glass_pane", "minecraft:golden_hoe", "minecraft:brick_slab", "minecraft:waxed_weathered_cut_copper", "minecraft:waxed_copper_chain", "minecraft:chiseled_bookshelf", "minecraft:purple_harness", "minecraft:dark_oak_fence_gate", "minecraft:light_blue_wool", "minecraft:cut_red_sandstone", "minecraft:mud_brick_stairs", "minecraft:emerald_block", "minecraft:dragon_egg", "minecraft:cherry_leaves", "minecraft:white_carpet", "minecraft:beetroot_seeds", "minecraft:oxidized_copper_door", "minecraft:bamboo_shelf", "minecraft:mud", "minecraft:horn_coral", "minecraft:spider_eye", "minecraft:yellow_stained_glass_pane", "minecraft:golden_leggings", "minecraft:light_blue_terracotta", "minecraft:waxed_exposed_copper_chest", "minecraft:gray_terracotta", "minecraft:pumpkin_seeds", "minecraft:end_stone", "minecraft:dark_oak_pressure_plate", "minecraft:orange_harness", "minecraft:carrot", "minecraft:chiseled_deepslate", "minecraft:lime_wool", "minecraft:orange_glazed_terracotta", "minecraft:tube_coral", "minecraft:yellow_shulker_box", "minecraft:iron_chain", "minecraft:netherite_nautilus_armor", "minecraft:brown_carpet", "minecraft:polished_blackstone_slab", "minecraft:big_dripleaf", "minecraft:brown_wool", "minecraft:nether_brick", "minecraft:jungle_chest_boat", "minecraft:copper_trapdoor", "minecraft:warped_stem", "minecraft:warped_fence", "minecraft:tuff_stairs", "minecraft:black_shulker_box", "minecraft:muddy_mangrove_roots", "minecraft:raiser_armor_trim_smithing_template", "minecraft:polished_andesite_slab", "minecraft:suspicious_sand", "minecraft:birch_leaves", "minecraft:light_gray_banner", "minecraft:ender_eye", "minecraft:purple_concrete_powder", "minecraft:birch_sapling", "minecraft:dead_horn_coral", "minecraft:vex_armor_trim_smithing_template", "minecraft:purple_terracotta", "minecraft:blue_stained_glass_pane", "minecraft:magenta_glazed_terracotta", "minecraft:cut_sandstone", "minecraft:cut_copper_stairs", "minecraft:stripped_crimson_stem", "minecraft:oak_slab", "minecraft:sea_pickle", "minecraft:oxidized_chiseled_copper", "minecraft:cyan_bed", "minecraft:prize_pottery_sherd", "minecraft:red_nether_brick_wall", "minecraft:waxed_oxidized_copper_grate", "minecraft:warped_shelf", "minecraft:cookie", "minecraft:jungle_door", "minecraft:tube_coral_block", "minecraft:crimson_nylium", "minecraft:granite_wall", "minecraft:calibrated_sculk_sensor", "minecraft:potion", "minecraft:cracked_deepslate_tiles", "minecraft:crafter", "minecraft:beetroot_soup", "minecraft:blue_concrete_powder", "minecraft:cracked_polished_blackstone_bricks", "minecraft:deepslate_bricks", "minecraft:waxed_exposed_copper_door", "minecraft:oak_wood", "minecraft:acacia_fence", "minecraft:leather_leggings", "minecraft:stripped_acacia_wood", "minecraft:bolt_armor_trim_smithing_template", "minecraft:sea_lantern", "minecraft:dead_tube_coral_fan", "minecraft:oxeye_daisy", "minecraft:brown_mushroom_block", "minecraft:lime_shulker_box", "minecraft:carved_pumpkin", "minecraft:smooth_sandstone", "minecraft:crimson_stem", "minecraft:jungle_stairs", "minecraft:exposed_copper_door", "minecraft:skeleton_skull", "minecraft:pink_carpet", "minecraft:raw_copper_block", "minecraft:mangrove_trapdoor", "minecraft:mossy_cobblestone_wall", "minecraft:acacia_log", "minecraft:saddle", "minecraft:gray_stained_glass_pane", "minecraft:open_eyeblossom", "minecraft:magenta_bed", "minecraft:bubble_coral", "minecraft:oxidized_copper_bulb", "minecraft:axolotl_bucket", "minecraft:magenta_shulker_box", "minecraft:light_blue_concrete", "minecraft:flint", "minecraft:flowering_azalea", "minecraft:wooden_shovel", "minecraft:blackstone", "minecraft:waxed_copper_golem_statue", "minecraft:twisting_vines", "minecraft:music_disc_ward", "minecraft:crimson_door", "minecraft:iron_horse_armor", "minecraft:jungle_hanging_sign", "minecraft:redstone_torch", "minecraft:wither_rose", "minecraft:waxed_weathered_copper", "minecraft:disc_fragment_5", "minecraft:bamboo_block", "minecraft:pale_oak_leaves", "minecraft:sculk_catalyst", "minecraft:copper_block", "minecraft:angler_pottery_sherd", "minecraft:chiseled_red_sandstone", "minecraft:end_crystal", "minecraft:polished_diorite", "minecraft:warped_stairs", "minecraft:daylight_detector", "minecraft:green_concrete", "minecraft:dead_horn_coral_fan", "minecraft:purple_bed", "minecraft:chest", "minecraft:minecart", "minecraft:azalea_leaves", "minecraft:mud_brick_slab", "minecraft:birch_log", "minecraft:white_stained_glass_pane", "minecraft:bricks", "minecraft:deepslate_emerald_ore", "minecraft:heavy_weighted_pressure_plate", "minecraft:end_rod", "minecraft:crimson_hyphae", "minecraft:diamond_ore", "minecraft:netherite_upgrade_smithing_template", "minecraft:sunflower", "minecraft:soul_campfire", "minecraft:waxed_oxidized_cut_copper_slab", "minecraft:light_blue_stained_glass", "minecraft:spruce_boat", "minecraft:light_blue_candle", "minecraft:tall_grass", "minecraft:white_concrete_powder", "minecraft:moss_block", "minecraft:acacia_boat", "minecraft:birch_trapdoor", "minecraft:oak_log", "minecraft:weathered_copper_lantern", "minecraft:light_gray_concrete", "minecraft:pink_concrete_powder", "minecraft:white_bundle", "minecraft:green_stained_glass", "minecraft:clay_ball", "minecraft:field_masoned_banner_pattern", "minecraft:brick", "minecraft:deepslate_coal_ore", "minecraft:exposed_copper_chest", "minecraft:black_harness", "minecraft:redstone_ore", "minecraft:comparator", "minecraft:white_candle", "minecraft:orange_shulker_box", "minecraft:smooth_stone", "minecraft:stick", "minecraft:oak_planks", "minecraft:orange_candle", "minecraft:netherite_chestplate", "minecraft:blue_terracotta", "minecraft:mangrove_propagule", "minecraft:note_block", "minecraft:dead_brain_coral_block", "minecraft:cut_sandstone_slab", "minecraft:netherite_axe", "minecraft:prismarine_brick_stairs", "minecraft:netherrack", "minecraft:white_dye", "minecraft:acacia_slab", "minecraft:chiseled_nether_bricks", "minecraft:leather_helmet", "minecraft:end_stone_brick_stairs", "minecraft:glow_ink_sac", "minecraft:waxed_exposed_copper_trapdoor", "minecraft:rabbit_foot", "minecraft:cyan_shulker_box", "minecraft:red_glazed_terracotta", "minecraft:mojang_banner_pattern", "minecraft:resin_brick_slab", "minecraft:oxidized_copper_bars", "minecraft:deepslate_copper_ore", "minecraft:polished_deepslate_wall", "minecraft:wet_sponge", "minecraft:orange_concrete", "minecraft:lime_carpet", "minecraft:spire_armor_trim_smithing_template", "minecraft:copper_axe", "minecraft:netherite_ingot", "minecraft:prismarine_crystals", "minecraft:magenta_candle", "minecraft:cyan_banner", "minecraft:glass_bottle", "minecraft:cracked_stone_bricks", "minecraft:diamond_nautilus_armor", "minecraft:orange_carpet", "minecraft:mossy_cobblestone_slab", "minecraft:waxed_weathered_lightning_rod", "minecraft:orange_dye", "minecraft:waxed_weathered_copper_golem_statue", "minecraft:spore_blossom", "minecraft:gold_block", "minecraft:mud_bricks", "minecraft:burn_pottery_sherd", "minecraft:music_disc_far", "minecraft:sugar_cane", "minecraft:resin_brick_stairs", "minecraft:smooth_red_sandstone_stairs", "minecraft:tuff_brick_stairs", "minecraft:chiseled_tuff_bricks", "minecraft:polished_basalt", "minecraft:polished_tuff_stairs", "minecraft:ominous_trial_key", "minecraft:smooth_quartz_slab", "minecraft:blue_egg", "minecraft:spyglass", "minecraft:polished_blackstone_brick_stairs", "minecraft:white_bed", "minecraft:gray_wool", "minecraft:red_dye", "minecraft:brown_banner", "minecraft:music_disc_creator_music_box", "minecraft:mangrove_sign", "minecraft:skull_banner_pattern", "minecraft:wither_skeleton_skull", "minecraft:leather_horse_armor", "minecraft:lily_of_the_valley", "minecraft:weathered_lightning_rod", "minecraft:pale_oak_hanging_sign", "minecraft:dark_oak_leaves", "minecraft:wooden_sword", "minecraft:dead_horn_coral_block", "minecraft:copper_ingot", "minecraft:cyan_carpet", "minecraft:bordure_indented_banner_pattern", "minecraft:waxed_cut_copper_slab", "minecraft:painting", "minecraft:purple_stained_glass_pane", "minecraft:experience_bottle", "minecraft:pink_shulker_box", "minecraft:deepslate_brick_slab", "minecraft:green_shulker_box", "minecraft:oak_hanging_sign", "minecraft:granite_stairs", "minecraft:music_disc_strad", "minecraft:brick_wall", "minecraft:rotten_flesh", "minecraft:bamboo_slab", "minecraft:brown_shulker_box", "minecraft:amethyst_shard", "minecraft:clock", "minecraft:waxed_copper_door", "minecraft:green_dye", "minecraft:furnace", "minecraft:redstone_lamp", "minecraft:written_book", "minecraft:spruce_chest_boat", "minecraft:iron_ingot", "minecraft:gray_stained_glass", "minecraft:copper_torch", "minecraft:weathered_copper_trapdoor", "minecraft:glass_pane", "minecraft:lapis_ore", "minecraft:ghast_tear", "minecraft:anvil", "minecraft:candle", "minecraft:oxidized_copper_chain", "minecraft:coast_armor_trim_smithing_template", "minecraft:splash_potion", "minecraft:lime_bundle", "minecraft:warped_planks", "minecraft:mangrove_boat", "minecraft:chiseled_sandstone", "minecraft:cyan_dye", "minecraft:cyan_bundle", "minecraft:music_disc_stal", "minecraft:waxed_chiseled_copper", "minecraft:deepslate", "minecraft:copper_nugget", "minecraft:pufferfish", "minecraft:waxed_weathered_copper_chest", "minecraft:black_banner", "minecraft:blue_ice", "minecraft:deepslate_brick_wall", "minecraft:weathered_cut_copper", "minecraft:cobbled_deepslate", "minecraft:nether_brick_stairs", "minecraft:waxed_exposed_cut_copper_slab", "minecraft:ice", "minecraft:sandstone_wall", "minecraft:poppy", "minecraft:brain_coral", "minecraft:mangrove_leaves", "minecraft:chainmail_leggings", "minecraft:magenta_banner");
        Set<Item> knownItems = new HashSet<>(items);
        knownItems.addAll(collectedItemsList);
        List<Item> added = new ArrayList<>();
        for (String id : newItems) {
            Item item = resolveSavedItem(id);
            if (item == null) continue;
            if (blockedItems.contains(item.toString())) continue;
            if (knownItems.contains(item)) continue;
            items.add(item);
            added.add(item);
        }
        maxItems += added.size();
        Item currentItem = !items.isEmpty() && !isFinished() ? items.getFirst() : null;
        Collections.shuffle(items, new Random(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getSeed()));
        if (currentItem != null) {
            items.remove(currentItem);
            items.addFirst(currentItem);
        }
        boolean finishedItemsChanged = false;
        for (String id : finishedItems) {
            Item item = resolveSavedItem(id);
            if (item == null) continue;
            if (items.contains(item)) continue;
            if (collectedItemsList.contains(item)) continue;
            collectedItemsList.add(item);
            finishedItemsChanged = true;
        }
        if (finishedItemsChanged) {
            saveData(server);
        }
        LOGGER.info("Manually updated with {} new items: {}", added.size(), added);
        return added.size();
    }

    private static boolean isFinished() {
        return collected_items >= maxItems;
    }

    static int compareVersions(String a, String b) {
        int[] pa = parseVersion(a);
        int[] pb = parseVersion(b);
        boolean aOld = pa[0] == 1;
        boolean bOld = pb[0] == 1;
        if (aOld && !bOld) return -1;
        if (!aOld && bOld) return 1;
        int len = Math.max(pa.length, pb.length);
        for (int i = 0; i < len; i++) {
            int va = i < pa.length ? pa[i] : 0;
            int vb = i < pb.length ? pb[i] : 0;
            if (va != vb) return Integer.compare(va, vb);
        }
        return 0;
    }

    private static int[] parseVersion(String version) {
        if (version == null) throw new IllegalArgumentException("null version");
        String[] parts = version.trim().split("\\.");
        if (parts.length < 2 || parts.length > 3) throw new IllegalArgumentException("Unparseable version: " + version);
        int[] result = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = Integer.parseInt(parts[i].trim());
        }
        return result;
    }

    private static Item resolveSavedItem(String id) {
        Identifier identifier;
        try {
            identifier = Identifier.parse(id);
        } catch (Exception e) {
            LOGGER.warn("loadData: could not parse item id '{}' from save: {}", id, e.getMessage());
            return null;
        }
        if (!BuiltInRegistries.ITEM.containsKey(identifier)) {
            LOGGER.warn("loadData: item id '{}' from save is not a known item in this registry", id);
            return null;
        }
        return BuiltInRegistries.ITEM.getValue(identifier);
    }

    public static void saveData(MinecraftServer server) {
        try {
            StringBuilder builder = new StringBuilder();
            for (Item item : items) {
                builder.append(BuiltInRegistries.ITEM.getKey(item)).append(",");
            }
            if (!items.isEmpty()) builder.setLength(builder.length() - 1);

            StringBuilder collectedBuilder = new StringBuilder();
            for (Item item : collectedItemsList) {
                collectedBuilder.append(BuiltInRegistries.ITEM.getKey(item)).append(",");
            }
            if (!collectedItemsList.isEmpty()) collectedBuilder.setLength(collectedBuilder.length() - 1);

            Path path = getSavePath(server);
            Files.createDirectories(path.getParent());
            String data = started + ";" + collected_items + ";" + maxItems + ";" + days + ";" + hours + ";" + min + ";" + sec + ";" + builder + ";" + getCurrentGameVersion() + ";" + collectedBuilder;
            Files.write(path, data.getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to save timer state", e);
        }
    }

    public static void loadData(MinecraftServer server) {
        Path path = getSavePath(server);
        if (!Files.exists(path)) return;

        try {
            String[] parts = Files.readString(path).split(";", -1);
            String savedVersion = null;
            if (parts.length == 10) {
                started = Boolean.parseBoolean(parts[0]);
                collected_items = Integer.parseInt(parts[1]);
                maxItems = Integer.parseInt(parts[2]);
                days = Integer.parseInt(parts[3]);
                hours = Integer.parseInt(parts[4]);
                min = Integer.parseInt(parts[5]);
                sec = Integer.parseInt(parts[6]);
                items.clear();
                if (!parts[7].isEmpty()) {
                    String[] itemIds = parts[7].split(",");
                    int unresolved = 0;
                    for (String id : itemIds) {
                        if (id.isBlank()) continue;
                        Item item = resolveSavedItem(id);
                        if (item == null) {
                            unresolved++;
                            continue;
                        }
                        if (!blockedItems.contains(item.toString())) {
                            items.add(item);
                        }
                    }
                    if (unresolved > 0) {
                        LOGGER.warn("loadData: {} of {} remaining-item id(s) in the save could not be resolved and were dropped - the known-item pool is now smaller than the save intended, which can cause spurious 'new item' detection on the next version check", unresolved, itemIds.length);
                    }
                }
                LOGGER.info("loadData: loaded {} remaining items from save", items.size());
                savedVersion = parts[8].isBlank() ? null : parts[8];
                collectedItemsList.clear();
                if (!parts[9].isEmpty()) {
                    String[] collectedIds = parts[9].split(",");
                    int unresolved = 0;
                    for (String id : collectedIds) {
                        if (id.isBlank()) continue;
                        Item item = resolveSavedItem(id);
                        if (item == null) {
                            unresolved++;
                            continue;
                        }
                        collectedItemsList.add(item);
                    }
                    if (unresolved > 0) {
                        LOGGER.warn("loadData: {} of {} collected-item id(s) in the save could not be resolved and were dropped - the known-item pool is now smaller than the save intended, which can cause spurious 'new item' detection on the next version check", unresolved, collectedIds.length);
                    }
                }
                LOGGER.info("loadData: loaded {} collected items from save", collectedItemsList.size());
            } else if (parts.length == 8) {
                started = Boolean.parseBoolean(parts[0]);
                collected_items = Integer.parseInt(parts[1]);
                maxItems = Integer.parseInt(parts[2]);
                days = Integer.parseInt(parts[3]);
                hours = Integer.parseInt(parts[4]);
                min = Integer.parseInt(parts[5]);
                sec = Integer.parseInt(parts[6]);
                items.clear();
                if (!parts[7].isEmpty()) {
                    String[] itemIds = parts[7].split(",");
                    for (String id : itemIds) {
                        if (id.isBlank()) continue;
                        Item item = resolveSavedItem(id);
                        if (item == null) continue;
                        if (!blockedItems.contains(item.toString())) {
                            items.add(item);
                        }
                    }
                }
            } else if (parts.length == 7) {
                started = Boolean.parseBoolean(parts[0]);
                collected_items = Integer.parseInt(parts[1]);
                maxItems = Integer.parseInt(parts[2]);
                days = Integer.parseInt(parts[3]);
                hours = Integer.parseInt(parts[4]);
                min = Integer.parseInt(parts[5]);
                sec = Integer.parseInt(parts[6]);
            }

            LOGGER.info("loadData: parsed {} fields, savedVersion={}, currentVersion={}", parts.length, savedVersion, getCurrentGameVersion());
            if (savedVersion != null) {
                handleVersionCheck(server, savedVersion);
            } else {
                LOGGER.warn("loadData: no saved version found in save file, skipping version check");
            }

            if (started) {
                startGame(server, true);
            }
        } catch (IOException | NumberFormatException e) {
            LOGGER.error("Failed to load timer state", e);
        }
    }

    private static void handleVersionCheck(MinecraftServer server, String savedVersion) {
        String currentVersion = getCurrentGameVersion();
        LOGGER.info("handleVersionCheck: comparing saved='{}' vs current='{}'", savedVersion, currentVersion);

        if (currentVersion.equals(savedVersion)) {
            LOGGER.info("handleVersionCheck: versions are identical, nothing to do");
            return;
        }

        int comparison;
        boolean comparable = true;
        try {
            comparison = compareVersions(currentVersion, savedVersion);
            LOGGER.info("handleVersionCheck: compareVersions result = {}", comparison);
        } catch (IllegalArgumentException e) {
            comparable = false;
            comparison = 1;
            LOGGER.warn("Could not compare versions '{}' (current) and '{}' (saved): {}", currentVersion, savedVersion, e.getMessage());
        }

        if (comparison < 0) {
            LOGGER.info("handleVersionCheck: current version is OLDER than saved version, broadcasting warning only");
            broadcast(server, Component.translatable("warning.all_items.version_older", savedVersion, currentVersion));
            return;
        }

        if (!comparable) {
            broadcast(server, Component.translatable("warning.all_items.version_uncomparable", savedVersion, currentVersion));
        }

        Set<Item> knownItems = new HashSet<>(items);
        knownItems.addAll(collectedItemsList);
        LOGGER.info("handleVersionCheck: known item pool size = {} (remaining={}, collected={})", knownItems.size(), items.size(), collectedItemsList.size());

        List<Item> newItems = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (!blockedItems.contains(item.toString()) && !item.toString().contains("spawn_egg") && !knownItems.contains(item)) {
                newItems.add(item);
            }
        }
        LOGGER.info("handleVersionCheck: found {} new item(s) not in known pool", newItems.size());

        if (newItems.size() > knownItems.size()) {
            LOGGER.warn("handleVersionCheck: about to add {} 'new' item(s), which is MORE than the {} item(s) already known from the save. " + "This usually means the save's item/collected-item lists were smaller than expected (e.g. corrupted or partially unresolved on load) " + "rather than a genuine version upgrade with that many new items. Check the loadData warnings above before trusting this result.", newItems.size(), knownItems.size());
        }

        if (!newItems.isEmpty()) {
            items.addAll(newItems);
            maxItems += newItems.size();
            Collections.shuffle(items, new Random(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getSeed()));
            LOGGER.info("handleVersionCheck: reshuffled {} remaining item(s) with the world seed after adding new items", items.size());
            saveData(server);
            if (comparable) {
                broadcast(server, Component.translatable("info.all_items.version_newer_items_added", savedVersion, currentVersion, newItems.size()));
            }
            LOGGER.info("Added {} new items to the collection list after version change ({} -> {}): {}", newItems.size(), savedVersion, currentVersion, newItems);
        } else {
            LOGGER.info("handleVersionCheck: no new items found, nothing added");
        }
    }

    private static final List<Component> pendingBroadcasts = Collections.synchronizedList(new ArrayList<>());

    private static void broadcast(MinecraftServer server, Component message) {
        LOGGER.info(message.getString());
        List<ServerPlayer> onlinePlayers = server.getPlayerList().getPlayers();
        if (onlinePlayers.isEmpty()) {
            pendingBroadcasts.add(message);
            return;
        }
        for (ServerPlayer player : onlinePlayers) {
            player.sendSystemMessage(message, false);
        }
    }

    @Override
    public void onInitialize() {
        ModCommands.registerModCommands();
        ServerLifecycleEvents.SERVER_STARTED.register(AllItems::loadData);
        ServerTickEvents.END_LEVEL_TICK.register(world -> {
            if (!started || items.isEmpty()) return;
            for (ServerPlayer player : world.players()) {
                Component collectedItemText = null;
                String link = null;
                ItemStack stack = player.getInventory().getSelectedItem();
                Item held = stack.getItem();
                if (!blockedItems.contains(held.toString()) && stack.getItem() == items.getFirst()) {
                    Component collector = player.getDisplayName().copy().withColor(TextColor.GOLD);
                    Component collectedItem = Component.translatable(stack.getItem().getDescriptionId()).copy().withColor(TextColor.GOLD);
                    collectedItemText = Component.translatable("notification.all_items.collected_item", collector, collectedItem);
                    // stat
                    collected_items++;
                    collectedItemsList.add(items.getFirst());
                    items.removeFirst();
                    link = AllItems.getObtainingLink();
                    LOGGER.info("Collected {} by {}", stack.getItem(), player.getGameProfile().name());
                }
                if (collectedItemText != null) {
                    player.sendSystemMessage(collectedItemText, false);
                    String finalLink = link;
                    Component howToObtainNextItem = Component.translatable("command.all_items.obtaining_next").withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(finalLink))).withUnderlined(true));
                    player.sendSystemMessage(howToObtainNextItem, false);
                }
            }
        });
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            started = false;
            if (timerThread != null && timerThread.isAlive()) {
                timerThread.interrupt();
                timerThread = null;
            }
            collected_items = 0;
            sec = min = hours = days = 0;
            items.clear();
            collectedItemsList.clear();
            collectedItemsBossbar.removeAllPlayers();
        });
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (started) {
                collectedItemsBossbar.addPlayer(handler.getPlayer());
            }
            if (!pendingBroadcasts.isEmpty()) {
                ServerPlayer player = handler.getPlayer();
                synchronized (pendingBroadcasts) {
                    for (Component message : pendingBroadcasts) {
                        player.sendSystemMessage(message, false);
                    }
                    pendingBroadcasts.clear();
                }
            }
        });
    }

    public static void startGame(MinecraftServer server, boolean force) {
        if (!force && started) {
            return;
        }
        if (timerThread != null && timerThread.isAlive()) {
            return;
        }

        started = true;
        System.out.println(maxItems);
        System.out.println(collected_items);
        if (items.isEmpty()) {
            shuffleItems(server);
        }

        timerThread = new Thread(() -> {
            while (started) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }

                if (!server.isPaused() && collected_items < maxItems) {
                    sec++;
                    if (sec >= 60) {
                        sec = 0;
                        min++;
                        if (min >= 60) {
                            min = 0;
                            hours++;
                            if (hours >= 24) {
                                hours = 0;
                                days++;
                            }
                        }
                    }
                    saveData(server);
                }

                if (items.isEmpty()) {
                    collectedItemsBossbar.setName(Component.literal(maxItems + "/" + maxItems));
                    nextItemBossbar.setName(Component.translatable("bossbar.all_items.finished"));
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        player.setGameMode(GameType.SPECTATOR);
                    }
                } else {
                    collectedItemsBossbar.setName(Component.translatable("bossbar.all_items.collected_items", collected_items, maxItems));
                }

                Component actionbarMessage = Component.translatable("actionbar.all_items.timer", days, hours, min, sec).withColor(TextColor.GOLD);
                for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                    player.sendSystemMessage(actionbarMessage, true);
                    collectedItemsBossbar.addPlayer(player);
                }
                if (!items.isEmpty()) {
                    nextItemBossbar.setProgress((float) collected_items / maxItems);
                    Item nextItem = items.get(0);
                    if (nextItem.toString().contains("minecraft:music_disc_")) {
                        Item item = nextItem;
                        String raw = item.toString().replace("minecraft:music_disc_", "");
                        String name = raw.substring(0, 1).toUpperCase() + raw.substring(1);
                        Component musicDiscName = Component.translatable(nextItem.getDescriptionId()).append(" ").append(Component.literal(name));
                        nextItemBossbar.setName(musicDiscName);
                    } else {
                        nextItemBossbar.setName(Component.translatable(nextItem.getDescriptionId()));
                    }
                    for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                        player.sendSystemMessage(actionbarMessage, true);
                        nextItemBossbar.addPlayer(player);
                    }
                }
            }
        });

        timerThread.start();
    }

    public static void setGameTimer(MinecraftServer server, int d, int h, int m, int s) {
        days = d;
        hours = h;
        min = m;
        sec = s;
        saveData(server);
    }

    public static void shuffleItems(MinecraftServer server) {
        for (Item item : BuiltInRegistries.ITEM) {
            if (!blockedItems.contains(item.toString()) && !item.toString().contains("spawn_egg")) {
                items.add(item);
            }
        }
        maxItems = items.size();
        Collections.shuffle(items, new Random(Objects.requireNonNull(server.getLevel(Level.OVERWORLD)).getSeed()));
        for (int i = 0; i < collected_items; i++) {
            items.removeFirst();
        }
        System.out.println("Shuffled items: " + items);
    }

    public static Component skipItem() {
        Component skippedItem = Component.translatable(items.getFirst().getDescriptionId()).withColor(TextColor.GOLD);
        if (!items.isEmpty()) {
            collectedItemsList.add(items.getFirst());
            items.removeFirst();
        }
        collected_items++;
        LOGGER.info("HERE");
        return skippedItem;
    }

    public static Boolean isVanillaItem(Item item) {
        return item.toString().startsWith("minecraft:");
    }

    public static String getObtainingLink() {
        String englishName = items.getFirst().toString().replace("minecraft:", "");
        englishName = Arrays.stream(englishName.split("_")).map(s -> s.substring(0, 1).toUpperCase() + s.substring(1)).collect(Collectors.joining("_"));
        return "https://minecraft.wiki/w/" + englishName + "#Obtaining";
    }
}