package pokecube.legends.init;

import net.minecraft.data.worldgen.features.TreeFeatures;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.registries.RegistryObject;
import pokecube.legends.PokecubeLegends;
import pokecube.legends.blocks.FlowerBase;
import pokecube.legends.blocks.MushroomBase;
import pokecube.legends.blocks.plants.AzureColeusBlock;
import pokecube.legends.blocks.plants.BlossomLilyPadBlock;
import pokecube.legends.blocks.plants.DistortedVinesBlock;
import pokecube.legends.blocks.plants.DistortedVinesTopBlock;
import pokecube.legends.blocks.plants.GoldenSweetBerryBushBlock;
import pokecube.legends.blocks.plants.HangingTendrilsBlock;
import pokecube.legends.blocks.plants.HangingTendrilsPlantBlock;
import pokecube.legends.blocks.plants.InvertedOrchidBlock;
import pokecube.legends.blocks.plants.LilyPadBlock;
import pokecube.legends.blocks.plants.PurpleWisteriaVinesBlock;
import pokecube.legends.blocks.plants.PurpleWisteriaVinesPlantBlock;
import pokecube.legends.blocks.plants.TaintedKelpBlock;
import pokecube.legends.blocks.plants.TaintedKelpPlantBlock;
import pokecube.legends.blocks.plants.TaintedRootsBlock;
import pokecube.legends.blocks.plants.TaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TallCorruptedGrassBlock;
import pokecube.legends.blocks.plants.TallDistorticGrassBlock;
import pokecube.legends.blocks.plants.TallGoldenGrassBlock;
import pokecube.legends.blocks.plants.TallTaintedSeagrassBlock;
import pokecube.legends.blocks.plants.TemporalBambooBlock;
import pokecube.legends.blocks.plants.TemporalBambooShootBlock;

public class PlantsInit
{
    // Plants
    public static final RegistryObject<Block> AZURE_COLEUS;
    public static final RegistryObject<Block> COMPRECED_MUSHROOM;
    public static final RegistryObject<Block> CORRUPTED_GRASS;
    public static final RegistryObject<Block> DISTORCED_MUSHROOM;
    public static final RegistryObject<Block> DISTORTIC_GRASS;
    public static final RegistryObject<Block> DISTORTIC_VINES;
    public static final RegistryObject<Block> DISTORTIC_VINES_PLANT;
    public static final RegistryObject<Block> GOLDEN_ALLIUM;
    public static final RegistryObject<Block> GOLDEN_AZURE_BLUET;
    public static final RegistryObject<Block> GOLDEN_CORNFLOWER;
    public static final RegistryObject<Block> GOLDEN_DANDELION;
    public static final RegistryObject<Block> GOLDEN_FERN;
    public static final RegistryObject<Block> GOLDEN_GRASS;
    public static final RegistryObject<Block> GOLDEN_LILY_VALLEY;
    public static final RegistryObject<Block> GOLDEN_ORCHID;
    public static final RegistryObject<Block> GOLDEN_OXEYE_DAISY;
    public static final RegistryObject<Block> GOLDEN_POPPY;
    public static final RegistryObject<Block> GOLDEN_SHROOM_PLANT;
    public static final RegistryObject<Block> GOLDEN_SWEET_BERRY_BUSH;
    public static final RegistryObject<Block> GOLDEN_TULIP;
    public static final RegistryObject<Block> INVERTED_ORCHID;
    public static final RegistryObject<Block> HANGING_TENDRILS;
    public static final RegistryObject<Block> HANGING_TENDRILS_PLANT;
    public static final RegistryObject<Block> LARGE_GOLDEN_FERN;
    public static final RegistryObject<Block> PINK_TAINTED_LILY_PAD;
    public static final RegistryObject<Block> PURPLE_WISTERIA_VINES;
    public static final RegistryObject<Block> PURPLE_WISTERIA_VINES_PLANT;
    public static final RegistryObject<Block> TAINTED_KELP;
    public static final RegistryObject<Block> TAINTED_KELP_PLANT;
    public static final RegistryObject<Block> TAINTED_LILY_PAD;
    public static final RegistryObject<Block> TAINTED_ROOTS;
    public static final RegistryObject<Block> TAINTED_SEAGRASS;
    public static final RegistryObject<Block> TALL_CORRUPTED_GRASS;
    public static final RegistryObject<Block> TALL_GOLDEN_GRASS;
    public static final RegistryObject<Block> TALL_TAINTED_SEAGRASS;
    public static final RegistryObject<Block> TEMPORAL_BAMBOO;
    public static final RegistryObject<Block> TEMPORAL_BAMBOO_SHOOT;

    static
    {
        AZURE_COLEUS = PokecubeLegends.DIMENSIONS_TAB.register("azure_coleus", () -> new AzureColeusBlock(MobEffects.INVISIBILITY, 15,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_BLUE).randomTicks().noCollission().sound(SoundType.AZALEA)));

        COMPRECED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("compreced_mushroom", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission().randomTicks().instabreak().sound(SoundType.GRASS),
                () -> { return TreeFeatures.HUGE_RED_MUSHROOM; }).bonemealTarget(false));

        DISTORCED_MUSHROOM = PokecubeLegends.DIMENSIONS_TAB.register("distorced_mushroom", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission().randomTicks().instabreak().sound(SoundType.GRASS),
                () -> { return TreeFeatures.HUGE_RED_MUSHROOM; }).bonemealTarget(false));

        GOLDEN_FERN = PokecubeLegends.DIMENSIONS_TAB.register("golden_fern", () -> new TallGoldenGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        LARGE_GOLDEN_FERN = PokecubeLegends.DIMENSIONS_TAB.register("large_golden_fern", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        GOLDEN_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("golden_grass", () -> new TallGoldenGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        TALL_GOLDEN_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("tall_golden_grass", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        GOLDEN_ALLIUM = PokecubeLegends.DIMENSIONS_TAB.register("golden_allium", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));
        
        GOLDEN_AZURE_BLUET = PokecubeLegends.DIMENSIONS_TAB.register("golden_azure_bluet", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));
        
        GOLDEN_CORNFLOWER = PokecubeLegends.DIMENSIONS_TAB.register("golden_cornflower", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));
        
        GOLDEN_DANDELION = PokecubeLegends.DIMENSIONS_TAB.register("golden_dandelion", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));
        
        GOLDEN_LILY_VALLEY = PokecubeLegends.DIMENSIONS_TAB.register("golden_lily_of_the_valley", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));
        
        GOLDEN_POPPY = PokecubeLegends.DIMENSIONS_TAB.register("golden_poppy", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));
        
        GOLDEN_ORCHID = PokecubeLegends.DIMENSIONS_TAB.register("golden_orchid", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));
        
        GOLDEN_OXEYE_DAISY = PokecubeLegends.DIMENSIONS_TAB.register("golden_oxeye_daisy", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        GOLDEN_TULIP = PokecubeLegends.DIMENSIONS_TAB.register("golden_tulip", () -> new FlowerBase(MobEffects.ABSORPTION, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).noCollission().instabreak().sound(SoundType.GRASS)));

        GOLDEN_SHROOM_PLANT = PokecubeLegends.DIMENSIONS_TAB.register("golden_shroom_plant", () -> new MushroomBase(BlockBehaviour.Properties
                .of(Material.PLANT, MaterialColor.GOLD).noCollission().randomTicks().instabreak().sound(SoundType.GRASS),
                () -> { return TreeFeatures.HUGE_RED_MUSHROOM; }).bonemealTarget(false));

        GOLDEN_SWEET_BERRY_BUSH = PokecubeLegends.DIMENSIONS_TAB.register("golden_sweet_berry_bush", () -> new GoldenSweetBerryBushBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.GOLD).randomTicks().noCollission().sound(SoundType.SWEET_BERRY_BUSH)));

        INVERTED_ORCHID = PokecubeLegends.DIMENSIONS_TAB.register("inverted_orchid", () -> new InvertedOrchidBlock(MobEffects.HEAL, 10,
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PINK).noCollission().instabreak().sound(SoundType.BAMBOO_SAPLING)));

        CORRUPTED_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("corrupted_grass", () -> new TallCorruptedGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.TERRACOTTA_BLUE).noCollission()
                .instabreak().sound(SoundType.GRASS)));

        TALL_CORRUPTED_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("tall_corrupted_grass", () -> new DoublePlantBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.TERRACOTTA_BLUE).noCollission()
                .instabreak().sound(SoundType.GRASS)));

        TAINTED_ROOTS = PokecubeLegends.DIMENSIONS_TAB.register("tainted_roots", () -> new TaintedRootsBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.TERRACOTTA_PURPLE).noCollission()
                .instabreak().sound(SoundType.ROOTS)));

        TAINTED_KELP = PokecubeLegends.DIMENSIONS_TAB.register("tainted_kelp", () -> new TaintedKelpBlock(
                BlockBehaviour.Properties.of(Material.WATER_PLANT, MaterialColor.TERRACOTTA_PURPLE).noCollission()
                .randomTicks().instabreak().sound(SoundType.WET_GRASS)));

        TAINTED_KELP_PLANT = PokecubeLegends.NO_TAB.register("tainted_kelp_plant", () -> new TaintedKelpPlantBlock(
                BlockBehaviour.Properties.of(Material.WATER_PLANT, MaterialColor.TERRACOTTA_PURPLE).noCollission()
                .instabreak().sound(SoundType.WET_GRASS)));

        PINK_TAINTED_LILY_PAD = PokecubeLegends.DIMENSIONS_TAB.register("pink_blossom_tainted_lily_pad", () -> new BlossomLilyPadBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.TERRACOTTA_PURPLE).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));

        TAINTED_LILY_PAD = PokecubeLegends.DIMENSIONS_TAB.register("tainted_lily_pad", () -> new LilyPadBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.TERRACOTTA_PURPLE).instabreak().sound(SoundType.LILY_PAD).noOcclusion()));

        HANGING_TENDRILS = PokecubeLegends.DIMENSIONS_TAB.register("hanging_tendrils", () -> new HangingTendrilsBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.TERRACOTTA_PURPLE).randomTicks().noCollission()
                .instabreak().sound(SoundType.CAVE_VINES).lightLevel(HangingTendrilsBlock.emission(5))));
        HANGING_TENDRILS_PLANT = PokecubeLegends.DIMENSIONS_TAB.register("hanging_tendrils_plant", () -> new HangingTendrilsPlantBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.TERRACOTTA_PURPLE).noCollission()
                .instabreak().sound(SoundType.CAVE_VINES).lightLevel(HangingTendrilsBlock.emission(5))));

        PURPLE_WISTERIA_VINES = PokecubeLegends.DIMENSIONS_TAB.register("purple_wisteria_vines", () -> new PurpleWisteriaVinesBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE).randomTicks().noCollission()
                .instabreak().sound(SoundType.CAVE_VINES)));
        PURPLE_WISTERIA_VINES_PLANT = PokecubeLegends.DIMENSIONS_TAB.register("purple_wisteria_vines_plant", () -> new PurpleWisteriaVinesPlantBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_PURPLE).noCollission()
                .instabreak().sound(SoundType.CAVE_VINES)));

        TAINTED_SEAGRASS = PokecubeLegends.DIMENSIONS_TAB.register("tainted_seagrass", () -> new TaintedSeagrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_WATER_PLANT, MaterialColor.TERRACOTTA_PURPLE)
                .noCollission().instabreak().sound(SoundType.WET_GRASS)));

        TALL_TAINTED_SEAGRASS = PokecubeLegends.DIMENSIONS_TAB.register("tall_tainted_seagrass", () -> new TallTaintedSeagrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_WATER_PLANT, MaterialColor.TERRACOTTA_PURPLE)
                .noCollission().instabreak().sound(SoundType.WET_GRASS)));

        TEMPORAL_BAMBOO = PokecubeLegends.DIMENSIONS_TAB.register("temporal_bamboo", () -> new TemporalBambooBlock(
                BlockBehaviour.Properties.of(Material.BAMBOO, MaterialColor.WARPED_NYLIUM).randomTicks().instabreak()
                .strength(1.2f).sound(SoundType.BAMBOO).noOcclusion().dynamicShape()));
        TEMPORAL_BAMBOO_SHOOT = PokecubeLegends.DIMENSIONS_TAB.register("temporal_bamboo_shoot", () -> new TemporalBambooShootBlock(
                BlockBehaviour.Properties.of(Material.BAMBOO_SAPLING, MaterialColor.WARPED_NYLIUM).randomTicks().instabreak().noCollission()
                .strength(1.2f).sound(SoundType.BAMBOO_SAPLING)));

        DISTORTIC_GRASS = PokecubeLegends.DIMENSIONS_TAB.register("distortic_grass", () -> new TallDistorticGrassBlock(
                BlockBehaviour.Properties.of(Material.REPLACEABLE_PLANT, MaterialColor.COLOR_MAGENTA).noCollission()
                .instabreak().sound(SoundType.ROOTS)));
        
        DISTORTIC_VINES = PokecubeLegends.DIMENSIONS_TAB.register("distortic_vines", () -> new DistortedVinesTopBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_MAGENTA).randomTicks().noCollission()
                .instabreak().sound(SoundType.WEEPING_VINES)));
        DISTORTIC_VINES_PLANT = PokecubeLegends.DIMENSIONS_TAB.register("distortic_vines_plant", () -> new DistortedVinesBlock(
                BlockBehaviour.Properties.of(Material.PLANT, MaterialColor.COLOR_MAGENTA)
                .noCollission().instabreak().sound(SoundType.WEEPING_VINES)));
    }

    public static void registry()
    {
    }
}
