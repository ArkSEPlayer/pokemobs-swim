package pokecube.mobs;

import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import pokecube.api.PokecubeAPI;
import pokecube.api.data.PokedexEntry;
import pokecube.api.data.PokedexEntry.EvolutionData;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.events.init.InitDatabase;
import pokecube.api.events.init.RegisterMiscItems;
import pokecube.api.events.init.RegisterPokecubes;
import pokecube.api.events.init.RegisterPokemobsEvent;
import pokecube.api.events.pokemobs.CaptureEvent.Post;
import pokecube.api.events.pokemobs.CaptureEvent.Pre;
import pokecube.api.events.pokemobs.EvolveEvent;
import pokecube.api.items.IPokecube;
import pokecube.api.items.IPokecube.DefaultPokecubeBehaviour;
import pokecube.api.items.IPokecube.NormalPokecubeBehaviour;
import pokecube.api.items.IPokecube.PokecubeBehaviour;
import pokecube.api.stats.CaptureStats;
import pokecube.api.stats.EggStats;
import pokecube.core.PokecubeCore;
import pokecube.core.PokecubeItems;
import pokecube.core.blocks.berries.BerryGenManager;
import pokecube.core.database.Database;
import pokecube.core.database.types.CombatTypeLoader;
import pokecube.core.eventhandlers.EventsHandler;
import pokecube.core.eventhandlers.StatsCollector;
import pokecube.core.init.ItemGenerator;
import pokecube.core.items.berries.BerryManager;
import pokecube.core.items.pokecubes.EntityPokecube;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.Tools;
import pokecube.mobs.abilities.AbilityRegister;
import pokecube.mobs.init.PokemobSounds;
import pokecube.mobs.moves.MoveRegister;
import thut.api.maths.Vector3;
import thut.core.common.ThutCore;
import thut.lib.TComponent;

@Mod(value = PokecubeMobs.MODID)
public class PokecubeMobs
{
    public static final String MODID = "pokecube_mobs";

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS,
            PokecubeMobs.MODID);;

    Map<PokedexEntry, Integer> genMap = Maps.newHashMap();

    public PokecubeMobs()
    {
        MinecraftForge.EVENT_BUS.register(this);
        PokecubeAPI.POKEMOB_BUS.register(this);
        // We override these so that they use ours instead of default ones.
        CombatTypeLoader.TYPES = new ResourceLocation(PokecubeMobs.MODID, "database/types.json");

        final IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        PokecubeMobs.SOUNDS.register(bus);

        new BerryGenManager(PokecubeMobs.MODID);
        MoveRegister.init();
        AbilityRegister.init();
    }

    @SubscribeEvent
    public void evolveTyrogue(final EvolveEvent.Pre evt)
    {
        if (evt.mob.getPokedexEntry() == Database.getEntry("Tyrogue"))
        {
            final int atk = evt.mob.getStat(Stats.ATTACK, false);
            final int def = evt.mob.getStat(Stats.DEFENSE, false);
            if (atk > def) evt.forme = Database.getEntry("Hitmonlee");
            else if (def > atk) evt.forme = Database.getEntry("Hitmonchan");
            else evt.forme = Database.getEntry("Hitmontop");
        }
    }

    private int getGen(PokedexEntry entry)
    {
        int gen;
        if (this.genMap.containsKey(entry)) gen = this.genMap.get(entry);
        else
        {
            gen = entry.getGen();
            final PokedexEntry real = entry;
            if (entry.getBaseForme() != null) entry = entry.getBaseForme();
            for (final EvolutionData d : entry.getEvolutions())
            {
                int gen1 = d.evolution.getGen();
                if (this.genMap.containsKey(d.evolution)) gen1 = this.genMap.get(d.evolution);
                if (gen1 < gen) gen = gen1;
                for (final EvolutionData d1 : d.evolution.getEvolutions())
                {
                    gen1 = d1.evolution.getGen();
                    if (this.genMap.containsKey(d1.evolution)) gen1 = this.genMap.get(d1.evolution);
                    if (d.evolution == entry && gen1 < gen) gen = gen1;
                }
            }
            for (final PokedexEntry e : Database.getSortedFormes())
            {
                int gen1 = e.getGen();
                if (this.genMap.containsKey(e)) gen1 = this.genMap.get(e);
                for (final EvolutionData d : e.getEvolutions()) if (d.evolution == entry && gen1 < gen) gen = gen1;
            }
            this.genMap.put(real, gen);
        }
        return gen;
    }

    public String getModelDirectory(final PokedexEntry entry)
    {
        final int gen = this.getGen(entry);
        switch (gen)
        {
        case 1:
            return "gen_1/entity/models/";
        case 2:
            return "gen_2/entity/models/";
        case 3:
            return "gen_3/entity/models/";
        case 4:
            return "gen_4/entity/models/";
        case 5:
            return "gen_5/entity/models/";
        case 6:
            return "gen_6/entity/models/";
        case 7:
            return "gen_7/entity/models/";
        case 8:
            return "gen_8/entity/models/";
        }
        return "entity/models/";
    }

    public String getTextureDirectory(final PokedexEntry entry)
    {
        final int gen = this.getGen(entry);
        switch (gen)
        {
        case 1:
            return "gen_1/entity/textures/";
        case 2:
            return "gen_2/entity/textures/";
        case 3:
            return "gen_3/entity/textures/";
        case 4:
            return "gen_4/entity/textures/";
        case 5:
            return "gen_5/entity/textures/";
        case 6:
            return "gen_6/entity/textures/";
        case 7:
            return "gen_7/entity/textures/";
        case 8:
            return "gen_8/entity/textures/";
        }
        return "entity/textures/";
    }

    @SubscribeEvent
    public void livingUpdate(final LivingUpdateEvent evt)
    {
        final IPokemob shuckle = PokemobCaps.getPokemobFor(evt.getEntity());
        if (shuckle != null && shuckle.getPokedexNb() == 213)
        {
            if (evt.getEntity().level.isClientSide) return;

            final ItemStack item = shuckle.getEntity().getMainHandItem();
            if (item.isEmpty()) return;
            final Item itemId = item.getItem();
            boolean berry = itemId == BerryManager.getBerryItem("oran");
            final Random r = ThutCore.newRandom();
            if (berry && r.nextGaussian() > EventsHandler.juiceChance)
            {
                if (shuckle.getOwner() instanceof Player player)
                {
                    final String message = "A sweet smell is coming from " + shuckle.getDisplayName().getString();
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal(message));
                }
                shuckle.setHeldItem(new ItemStack(PokecubeItems.BERRYJUICE.get()));
                return;
            }
            berry = itemId == PokecubeItems.BERRYJUICE.get();
            if (berry && r.nextGaussian() > EventsHandler.candyChance)
            {
                final ItemStack candy = PokecubeItems.makeCandyStack();
                if (candy.isEmpty()) return;

                if (shuckle.getOwner() != null && shuckle.getOwner() instanceof Player player)
                {
                    final String message = "The smell coming from " + shuckle.getDisplayName().getString()
                            + " has changed";
                    thut.lib.ChatHelper.sendSystemMessage(player, TComponent.literal(message));
                }
                shuckle.setHeldItem(candy);
                return;
            }
        }
    }

    @SubscribeEvent
    public void makeShedinja(final EvolveEvent.Post evt)
    {
        Entity owner;
        if ((owner = evt.mob.getOwner()) instanceof ServerPlayer) this.makeShedinja(evt.mob, (Player) owner);
    }

    void makeShedinja(final IPokemob evo, final Player player)
    {
        if (evo.getPokedexEntry() == Database.getEntry("ninjask"))
        {
            final Inventory inv = player.getInventory();
            boolean hasCube = false;
            boolean hasSpace = false;
            ItemStack cube = ItemStack.EMPTY;
            int m = -1;
            for (int n = 0; n < inv.getContainerSize(); n++)
            {
                final ItemStack item = inv.getItem(n);
                if (item == ItemStack.EMPTY) hasSpace = true;
                final ResourceLocation key = PokecubeItems.getCubeId(item);
                if (!hasCube && key != null && IPokecube.PokecubeBehaviour.BEHAVIORS.containsKey(key)
                        && !PokecubeManager.isFilled(item))
                {
                    hasCube = true;
                    cube = item;
                    m = n;
                }
                if (hasCube && hasSpace) break;

            }
            if (hasCube && hasSpace)
            {
                final Entity pokemon = PokecubeCore.createPokemob(Database.getEntry("shedinja"), player.getLevel());
                if (pokemon != null)
                {
                    final ItemStack mobCube = cube.copy();
                    mobCube.setCount(1);
                    final IPokemob poke = PokemobCaps.getPokemobFor(pokemon);
                    poke.setPokecube(mobCube);
                    poke.setOwner(player);
                    poke.setExp(Tools.levelToXp(poke.getExperienceMode(), 20), true);
                    poke.getEntity().setHealth(poke.getEntity().getMaxHealth());
                    final ItemStack shedinja = PokecubeManager.pokemobToItem(poke);
                    StatsCollector.addCapture(poke);
                    cube.shrink(1);
                    if (cube.isEmpty()) inv.setItem(m, ItemStack.EMPTY);
                    inv.add(shedinja);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void registerDatabases(final InitDatabase.Pre evt)
    {
        evt.modIDs.add(PokecubeMobs.MODID);
    }

    @SubscribeEvent
    public void registerItems(final RegisterMiscItems event)
    {

        ItemGenerator.berryWoods.put("enigma", MaterialColor.COLOR_BLACK);
        ItemGenerator.berryWoods.put("leppa", MaterialColor.COLOR_RED);
        ItemGenerator.berryWoods.put("nanab", MaterialColor.TERRACOTTA_BROWN);
        ItemGenerator.berryWoods.put("oran", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryWoods.put("pecha", MaterialColor.COLOR_MAGENTA);
        ItemGenerator.berryWoods.put("sitrus", MaterialColor.TERRACOTTA_YELLOW);

        ItemGenerator.berryLeaves.put("enigma", MaterialColor.TERRACOTTA_WHITE);
        ItemGenerator.berryLeaves.put("leppa", MaterialColor.SAND);
        ItemGenerator.berryLeaves.put("nanab", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryLeaves.put("oran", MaterialColor.COLOR_ORANGE);
        ItemGenerator.berryLeaves.put("pecha", MaterialColor.TERRACOTTA_LIGHT_BLUE);
        ItemGenerator.berryLeaves.put("sitrus", MaterialColor.TERRACOTTA_LIGHT_BLUE);

        ItemGenerator.onlyBerryLeaves.put("grepa", MaterialColor.COLOR_RED);
        ItemGenerator.onlyBerryLeaves.put("hondew", MaterialColor.TERRACOTTA_PURPLE);
        ItemGenerator.onlyBerryLeaves.put("kelpsy", MaterialColor.COLOR_PINK);
        ItemGenerator.onlyBerryLeaves.put("pomeg", MaterialColor.COLOR_RED);
        ItemGenerator.onlyBerryLeaves.put("qualot", MaterialColor.PLANT);
        ItemGenerator.onlyBerryLeaves.put("tamato", MaterialColor.COLOR_LIGHT_BLUE);

        ItemGenerator.berryCrops.put("aspear", MaterialColor.COLOR_RED);
        ItemGenerator.berryCrops.put("cheri", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("chesto", MaterialColor.COLOR_PINK);
        ItemGenerator.berryCrops.put("cornn", MaterialColor.COLOR_PINK);
        ItemGenerator.berryCrops.put("enigma", MaterialColor.TERRACOTTA_WHITE);
        ItemGenerator.berryCrops.put("grepa", MaterialColor.COLOR_RED);
        ItemGenerator.berryCrops.put("hondew", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("jaboca", MaterialColor.COLOR_PURPLE);
        ItemGenerator.berryCrops.put("kelpsy", MaterialColor.COLOR_PINK);
        ItemGenerator.berryCrops.put("leppa", MaterialColor.TERRACOTTA_YELLOW);
        ItemGenerator.berryCrops.put("lum", MaterialColor.COLOR_PURPLE);
        ItemGenerator.berryCrops.put("nanab", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryCrops.put("null", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("oran", MaterialColor.COLOR_ORANGE);
        ItemGenerator.berryCrops.put("pecha", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("persim", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryCrops.put("pinap", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("pomeg", MaterialColor.COLOR_RED);
        ItemGenerator.berryCrops.put("qualot", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("rawst", MaterialColor.PLANT);
        ItemGenerator.berryCrops.put("rowap", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryCrops.put("sitrus", MaterialColor.TERRACOTTA_LIGHT_BLUE);
        ItemGenerator.berryCrops.put("tamato", MaterialColor.COLOR_LIGHT_BLUE);

        ItemGenerator.berryFruits.put("aspear", MaterialColor.COLOR_RED);
        ItemGenerator.berryFruits.put("cheri", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("chesto", MaterialColor.COLOR_PINK);
        ItemGenerator.berryFruits.put("cornn", MaterialColor.COLOR_PINK);
        ItemGenerator.berryFruits.put("enigma", MaterialColor.COLOR_BLACK);
        ItemGenerator.berryFruits.put("grepa", MaterialColor.COLOR_YELLOW);
        ItemGenerator.berryFruits.put("hondew", MaterialColor.COLOR_LIGHT_GREEN);
        ItemGenerator.berryFruits.put("jaboca", MaterialColor.COLOR_PURPLE);
        ItemGenerator.berryFruits.put("kelpsy", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("leppa", MaterialColor.COLOR_RED);
        ItemGenerator.berryFruits.put("lum", MaterialColor.COLOR_PURPLE);
        ItemGenerator.berryFruits.put("nanab", MaterialColor.COLOR_PINK);
        ItemGenerator.berryFruits.put("null", MaterialColor.PLANT);
        ItemGenerator.berryFruits.put("oran", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("pecha", MaterialColor.COLOR_PINK);
        ItemGenerator.berryFruits.put("persim", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("pinap", MaterialColor.COLOR_YELLOW);
        ItemGenerator.berryFruits.put("pomeg", MaterialColor.TERRACOTTA_ORANGE);
        ItemGenerator.berryFruits.put("qualot", MaterialColor.COLOR_YELLOW);
        ItemGenerator.berryFruits.put("rawst", MaterialColor.PLANT);
        ItemGenerator.berryFruits.put("rowap", MaterialColor.COLOR_LIGHT_BLUE);
        ItemGenerator.berryFruits.put("sitrus", MaterialColor.COLOR_YELLOW);
        ItemGenerator.berryFruits.put("tamato", MaterialColor.TERRACOTTA_ORANGE);

        ItemGenerator.variants.add("dawnstone");
        ItemGenerator.variants.add("dubiousdisc");
        ItemGenerator.variants.add("duskstone");
        ItemGenerator.variants.add("electirizer");
        ItemGenerator.variants.add("everstone");
        ItemGenerator.variants.add("firestone");
        ItemGenerator.variants.add("kingsrock");
        ItemGenerator.variants.add("leafstone");
        ItemGenerator.variants.add("magmarizer");
        ItemGenerator.variants.add("metalcoat");
        ItemGenerator.variants.add("moonstone");
        ItemGenerator.variants.add("ovalstone");
        ItemGenerator.variants.add("prismscale");
        ItemGenerator.variants.add("protector");
        ItemGenerator.variants.add("reapercloth");
        ItemGenerator.variants.add("shinystone");
        ItemGenerator.variants.add("sunstone");
        ItemGenerator.variants.add("thunderstone");
        ItemGenerator.variants.add("upgrade");
        ItemGenerator.variants.add("waterstone");

        ItemGenerator.variants.add("abomasnowmega");
        ItemGenerator.variants.add("absolmega");
        ItemGenerator.variants.add("aerodactylmega");
        ItemGenerator.variants.add("aggronmega");
        ItemGenerator.variants.add("alakazammega");
        ItemGenerator.variants.add("alphaorb");
        ItemGenerator.variants.add("altariamega");
        ItemGenerator.variants.add("ampharosmega");
        ItemGenerator.variants.add("audinomega");
        ItemGenerator.variants.add("banettemega");
        ItemGenerator.variants.add("beedrillmega");
        ItemGenerator.variants.add("blastoisemega");
        ItemGenerator.variants.add("blazikenmega");
        ItemGenerator.variants.add("cameruptmega");
        ItemGenerator.variants.add("charizardmega-x");
        ItemGenerator.variants.add("charizardmega-y");
        ItemGenerator.variants.add("dianciemega");
        ItemGenerator.variants.add("gallademega");
        ItemGenerator.variants.add("garchompmega");
        ItemGenerator.variants.add("gardevoirmega");
        ItemGenerator.variants.add("gengarmega");
        ItemGenerator.variants.add("glaliemega");
        ItemGenerator.variants.add("gyaradosmega");
        ItemGenerator.variants.add("heracrossmega");
        ItemGenerator.variants.add("houndoommega");
        ItemGenerator.variants.add("kangaskhanmega");
        ItemGenerator.variants.add("latiasmega");
        ItemGenerator.variants.add("latiosmega");
        ItemGenerator.variants.add("lopunnymega");
        ItemGenerator.variants.add("lucariomega");
        ItemGenerator.variants.add("manectricmega");
        ItemGenerator.variants.add("mawilemega");
        ItemGenerator.variants.add("medichammega");
        ItemGenerator.variants.add("megastone");
        ItemGenerator.variants.add("metagrossmega");
        ItemGenerator.variants.add("mewtwomega-x");
        ItemGenerator.variants.add("mewtwomega-y");
        ItemGenerator.variants.add("omegaorb");
        ItemGenerator.variants.add("pidgeotmega");
        ItemGenerator.variants.add("pinsirmega");
        ItemGenerator.variants.add("sableyemega");
        ItemGenerator.variants.add("salamencemega");
        ItemGenerator.variants.add("sceptilemega");
        ItemGenerator.variants.add("scizormega");
        ItemGenerator.variants.add("sharpedomega");
        ItemGenerator.variants.add("shiny_charm");
        ItemGenerator.variants.add("slowbromega");
        ItemGenerator.variants.add("steelixmega");
        ItemGenerator.variants.add("swampertmega");
        ItemGenerator.variants.add("tyranitarmega");
        ItemGenerator.variants.add("venusaurmega");

        ItemGenerator.other.add("mewhair");

        ItemGenerator.fossilVariants.add("aerodactyl");
        ItemGenerator.fossilVariants.add("amaura");
        ItemGenerator.fossilVariants.add("anorith");
        ItemGenerator.fossilVariants.add("archen");
        ItemGenerator.fossilVariants.add("arctovish");
        ItemGenerator.fossilVariants.add("arctozolt");
        ItemGenerator.fossilVariants.add("cranidos");
        ItemGenerator.fossilVariants.add("dracovish");
        ItemGenerator.fossilVariants.add("dracozolt");
        ItemGenerator.fossilVariants.add("kabuto");
        ItemGenerator.fossilVariants.add("lileep");
        ItemGenerator.fossilVariants.add("omanyte");
        ItemGenerator.fossilVariants.add("shieldon");
        ItemGenerator.fossilVariants.add("tirtouga");
        ItemGenerator.fossilVariants.add("tyrunt");
        BerryHelper.initBerries();
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public void registerPokecubes(final RegisterPokecubes event)
    {
        MiscItemHelper.init();
        // Charcoal
        MiscItemHelper.CHARCOALSTACK = new ItemStack(Items.CHARCOAL);

        final PokecubeHelper helper = new PokecubeHelper();
        PokecubeBehaviour.DEFAULTCUBE = new ResourceLocation("pokecube", "pokecube");

        event.register(new NormalPokecubeBehaviour(1).setName("poke"));
        event.register(new NormalPokecubeBehaviour(1.5).setName("great"));
        event.register(new NormalPokecubeBehaviour(2).setName("ultra"));
        event.register(new NormalPokecubeBehaviour(255).setName("master"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.dusk(mob);
            }
        }.setName("dusk"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.quick(mob);
            }
        }.setName("quick"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.timer(mob);
            }
        }.setName("timer"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.net(mob);
            }
        }.setName("net"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.nest(mob);
            }
        }.setName("nest"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.dive(mob);
            }
        }.setName("dive"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.premier(mob);
            }
        }.setName("premier"));
        event.register(new NormalPokecubeBehaviour(1).setName("cherish"));
        event.register(new NormalPokecubeBehaviour(1.5).setName("safari"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.level(mob);
            }
        }.setName("level"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.lure(mob);
            }
        }.setName("lure"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.moon(mob);
            }
        }.setName("moon"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 1;
            }

            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.getCaught();
                mob.addHappiness(200 - mob.getHappiness());
            }
        }.setName("friend"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.love(mob);
            }
        }.setName("love"));
        event.register(new NormalPokecubeBehaviour(1)
        {
            @Override
            public int getAdditionalBonus(final IPokemob mob)
            {
                return helper.heavy(mob);
            }
        }.setName("heavy"));
        event.register(new DefaultPokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return helper.fast(mob);
            }
        }.setName("fast"));
        event.register(new NormalPokecubeBehaviour(1.5).setName("sport"));
        event.register(new NormalPokecubeBehaviour(1)
        {
            @Override
            public void onUpdate(final IPokemob mob)
            {
                helper.luxury(mob);
            }
        }.setName("luxury"));
        event.register(new NormalPokecubeBehaviour(1)
        {
            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.getCaught();
                mob.getEntity().setHealth(mob.getEntity().getMaxHealth());
                mob.healStatus();
            }
        }.setName("heal"));
        event.register(new NormalPokecubeBehaviour(255).setName("park"));
        event.register(new NormalPokecubeBehaviour(255).setName("dream"));

        final PokecubeBehaviour snag = new PokecubeBehaviour()
        {

            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 0;
            }

            @Override
            public void onPostCapture(final Post evt)
            {
                final IPokemob mob = evt.getCaught();
                if (mob != null) evt.pokecube.spawnAtLocation(PokecubeManager.pokemobToItem(mob), 1.0F);
                evt.setCanceled(true);
            }

            @Override
            public void onPreCapture(final Pre evt)
            {
                // The below processing is for pokemobs only
                if (evt.getCaught() == null) return;

                final boolean tameSnag = !evt.getCaught().isPlayerOwned()
                        && evt.getCaught().getGeneralState(GeneralStates.TAMED);

                if (evt.getCaught().isShadow())
                {
                    final EntityPokecube cube = (EntityPokecube) evt.pokecube;
                    final IPokemob mob = PokemobCaps.getPokemobFor(
                            PokecubeCore.createPokemob(evt.getCaught().getPokedexEntry(), cube.getLevel()));
                    cube.setTilt(Tools.computeCatchRate(mob, 1));
                    cube.setTime(cube.getTilt() * 20 + 5);
                    if (!tameSnag) evt.getCaught().setPokecube(evt.getFilledCube());
                    cube.setItem(PokecubeManager.pokemobToItem(evt.getCaught()));
                    PokecubeManager.setTilt(cube.getItem(), cube.getTilt());
                    new Vector3().set(evt.pokecube).moveEntity(cube);
                    evt.getCaught().getEntity().discard();
                    cube.setDeltaMovement(0, 0.1, 0);
                    cube.getLevel().addFreshEntity(cube.copy());
                    evt.pokecube.discard();
                }
                evt.setCanceled(true);
            }
        };

        final PokecubeBehaviour repeat = new PokecubeBehaviour()
        {
            @Override
            public double getCaptureModifier(final IPokemob mob)
            {
                return 0;
            }

            @Override
            public void onPostCapture(final Post evt)
            {

            }

            @Override
            public void onPreCapture(final Pre evt)
            {
                if (evt.getResult() == Result.DENY) return;

                final EntityPokecube cube = (EntityPokecube) evt.pokecube;

                final IPokemob mob = PokemobCaps
                        .getPokemobFor(PokecubeCore.createPokemob(evt.getCaught().getPokedexEntry(), cube.getLevel()));
                final Vector3 v = new Vector3();
                final Entity thrower = cube.shootingEntity;
                int has = CaptureStats.getTotalNumberOfPokemobCaughtBy(thrower.getUUID(), mob.getPokedexEntry());
                has = has + EggStats.getTotalNumberOfPokemobHatchedBy(thrower.getUUID(), mob.getPokedexEntry());
                final double rate = has > 0 ? 3 : 1;
                cube.setTilt(Tools.computeCatchRate(mob, rate));
                cube.setTime(cube.getTilt() * 20 + 5);
                evt.getCaught().setPokecube(evt.getFilledCube());
                cube.setItem(PokecubeManager.pokemobToItem(evt.getCaught()));
                PokecubeManager.setTilt(cube.getItem(), cube.getTilt());
                v.set(evt.pokecube).moveEntity(cube);
                v.moveEntity(mob.getEntity());
                evt.getCaught().getEntity().discard();
                cube.setDeltaMovement(0, 0.1, 0);
                cube.getLevel().addFreshEntity(cube.copy());
                evt.setCanceled(true);
                evt.pokecube.discard();
            }

        };
        event.register(snag.setName("snag"));
        event.register(repeat.setName("repeat"));
    }

    @SubscribeEvent
    public void RegisterPokemobsEvent(final RegisterPokemobsEvent.Register event)
    {
        for (final PokedexEntry entry : Database.getSortedFormes())
        {
            if (entry == Database.missingno) continue;
            if (entry.model != PokedexEntry.MODELNO) continue;
            final String tex = this.getTextureDirectory(entry);
            final String model = this.getModelDirectory(entry);
            entry.setModId(PokecubeMobs.MODID);
            entry.texturePath = PokecubeMobs.MODID + ":" + tex;
            entry.model = new ResourceLocation(PokecubeMobs.MODID, model + entry.getTrimmedName() + entry.modelExt);
            entry.texture = new ResourceLocation(PokecubeMobs.MODID, tex + entry.getTrimmedName() + ".png");
            entry.animation = new ResourceLocation(PokecubeMobs.MODID, model + entry.getTrimmedName() + ".xml");
        }
    }

    @SubscribeEvent
    public void postRegisterPokemobs(final RegisterPokemobsEvent.Post event)
    {
        PokemobSounds.initMobSounds();
    }
}
