package pokecube.core.handlers;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.IForgeRegistry;
import pokecube.api.PokecubeAPI;
import pokecube.api.events.init.RegisterPokecubes;
import pokecube.api.items.IPokecube.PokecubeBehavior;
import pokecube.core.PokecubeItems;
import pokecube.core.database.Database;
import pokecube.core.impl.PokecubeMod;
import pokecube.core.items.pokecubes.Pokecube;
import pokecube.core.items.vitamins.ItemVitamin;
import thut.api.OwnableCaps;

public class ItemHandler
{
    private static void addMiscItems(final IForgeRegistry<Item> registry)
    {
        ItemGenerator.registerItems(registry);
    }

    private static void addPokecubes(final IForgeRegistry<Item> registry)
    {
        final RegisterPokecubes event = new RegisterPokecubes();
        PokecubeAPI.POKEMOB_BUS.post(event);

        // Register any cube behaviours and cubes from event.
        for (final PokecubeBehavior i : event.behaviors)
        {
            PokecubeBehavior.addCubeBehavior(i);
            final String name = i.getRegistryName().getPath();
            final Item.Properties props = new Item.Properties();
            props.tab(PokecubeItems.TAB_POKECUBES);
            props.durability(255).defaultDurability(255);
            final Pokecube cube = new Pokecube(props);
            if (PokecubeItems.POKECUBE_CUBES.isEmpty()) PokecubeItems.POKECUBE_CUBES = new ItemStack(cube);
            registry.register(cube.setRegistryName(PokecubeMod.ID, name + "cube"));

            PokecubeItems.addCube(i.getRegistryName(), new Item[]
            { cube });
        }

        final Item.Properties props = new Item.Properties();
        props.tab(PokecubeItems.TAB_POKECUBES);
        final Pokecube pokeseal = new Pokecube(props);
        PokecubeBehavior.POKESEAL = new ResourceLocation("pokecube:seal");
        registry.register(pokeseal.setRegistryName(PokecubeMod.ID, "pokeseal"));

        PokecubeItems.addCube(PokecubeBehavior.POKESEAL, new Item[]
        { pokeseal });

    }

    private static void addVitamins(final IForgeRegistry<Item> registry)
    {
        final Item.Properties props = new Item.Properties().tab(PokecubeItems.TAB_ITEMS);
        for (final String type : ItemVitamin.vitamins)
        {
            final ItemVitamin item = new ItemVitamin(props, Database.trim(type));
            registry.register(item);
        }
    }

    public static void registerBlocks(final IForgeRegistry<Block> iForgeRegistry)
    {
        ItemGenerator.registerBlocks(iForgeRegistry);
    }

    private static void registerItemBlocks(final IForgeRegistry<Item> registry)
    {
        PokecubeItems.POKECUBE_BLOCKS = new ItemStack(PokecubeItems.HEALER.get());
    }

    public static void registerItems(final IForgeRegistry<Item> iForgeRegistry)
    {
        ItemHandler.addPokecubes(iForgeRegistry);
        ItemHandler.addVitamins(iForgeRegistry);
        ItemHandler.addMiscItems(iForgeRegistry);
        ItemHandler.registerItemBlocks(iForgeRegistry);
    }

    public static void postInit()
    {
        // Register classes for ownable caps
        OwnableCaps.TILES.add(PokecubeItems.TRADE_TYPE.get());
        OwnableCaps.TILES.add(PokecubeItems.TM_TYPE.get());
        OwnableCaps.TILES.add(PokecubeItems.PC_TYPE.get());
        OwnableCaps.TILES.add(PokecubeItems.HEALER_TYPE.get());
        OwnableCaps.TILES.add(PokecubeItems.BASE_TYPE.get());
    }
}
