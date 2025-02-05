package pokecube.core.init;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.animal.ShoulderRidingEntity;
import net.minecraftforge.registries.RegistryObject;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.Pokedex;
import pokecube.api.data.PokedexEntry;
import pokecube.api.events.init.InitDatabase;
import pokecube.api.events.init.RegisterPokemobsEvent;
import pokecube.core.PokecubeCore;
import pokecube.core.database.Database;
import pokecube.core.database.pokedex.PokedexEntryLoader;
import pokecube.core.entity.npc.NpcMob;
import pokecube.core.entity.pokemobs.GenericPokemob;
import pokecube.core.entity.pokemobs.PokemobType;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokemobeggs.EntityPokemobEgg;
import pokecube.core.moves.animations.EntityMoveUse;
import thut.api.entity.CopyCaps;

public class EntityTypes
{
    public static final RegistryObject<EntityType<EntityPokecube>> POKECUBE;
    public static final RegistryObject<EntityType<EntityPokemobEgg>> EGG;
    public static final RegistryObject<EntityType<NpcMob>> NPC;
    public static final RegistryObject<EntityType<EntityMoveUse>> MOVE;

    static
    {
        POKECUBE = PokecubeCore.ENTITIES.register("pokecube",
                () -> EntityType.Builder.of(EntityPokecube::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true)
                        .setTrackingRange(32).setUpdateInterval(1).noSummon().fireImmune().sized(0.25f, 0.25f)
                        .build("pokecube"));
        EGG = PokecubeCore.ENTITIES.register("egg",
                () -> EntityType.Builder.of(EntityPokemobEgg::new, MobCategory.CREATURE).noSummon().fireImmune()
                        .sized(0.35f, 0.35f).build("egg"));
        NPC = PokecubeCore.ENTITIES.register("npc", () -> EntityType.Builder.of(NpcMob::new, MobCategory.CREATURE)
                .setCustomClientFactory((s, w) -> getNpc().create(w)).build("pokecube:npc"));
        MOVE = PokecubeCore.ENTITIES.register("move_use",
                () -> EntityType.Builder.of(EntityMoveUse::new, MobCategory.MISC).noSummon().fireImmune()
                        .setTrackingRange(64).setShouldReceiveVelocityUpdates(true).setUpdateInterval(1)
                        .sized(0.5f, 0.5f).setCustomClientFactory((spawnEntity, world) ->
                        {
                            return getMove().create(world);
                        }).build("move_use"));
    }

    public static void init()
    {}

    private static PokemobType<ShoulderRidingEntity> makePokemobEntityType(PokedexEntry entry)
    {
        final PokemobType<ShoulderRidingEntity> type = new PokemobType<>(GenericPokemob::new, entry);
        PokecubeCore.typeMap.put(type, entry);
        CopyCaps.register(type);
        return type;
    }

    public static void registerPokemobs()
    {
        Database.init();
        PokecubeAPI.POKEMOB_BUS.post(new RegisterPokemobsEvent.Pre());
        PokecubeAPI.POKEMOB_BUS.post(new RegisterPokemobsEvent.Register());
        PokedexEntryLoader.postInit();
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (entry.dummy) continue;
            if (!entry.stock) continue;
            try
            {
                Pokedex.getInstance().registerPokemon(entry);
                PokecubeCore.ENTITIES.register(entry.getTrimmedName(), () -> makePokemobEntityType(entry));
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }
        PokecubeAPI.POKEMOB_BUS.post(new RegisterPokemobsEvent.Post());
        Database.postInit();
        PokecubeAPI.POKEMOB_BUS.post(new InitDatabase.Post());
    }

    public static EntityType<NpcMob> getNpc()
    {
        return NPC.get();
    }

    public static EntityType<EntityMoveUse> getMove()
    {
        return MOVE.get();
    }

    public static EntityType<EntityPokemobEgg> getEgg()
    {
        return EGG.get();
    }

    public static EntityType<EntityPokecube> getPokecube()
    {
        return POKECUBE.get();
    }
}
