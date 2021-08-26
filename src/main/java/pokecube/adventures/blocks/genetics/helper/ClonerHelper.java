package pokecube.adventures.blocks.genetics.helper;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.MinecraftForge;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector;
import pokecube.adventures.blocks.genetics.helper.recipe.RecipeSelector.SelectorValue;
import pokecube.adventures.events.GeneEditEvent;
import pokecube.adventures.events.GeneEditEvent.EditType;
import pokecube.core.PokecubeCore;
import pokecube.core.database.PokedexEntry;
import pokecube.core.entity.pokemobs.genetics.GeneticsManager;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene;
import pokecube.core.entity.pokemobs.genetics.genes.SpeciesGene.SpeciesInfo;
import pokecube.core.items.pokecubes.PokecubeManager;
import pokecube.core.utils.TagNames;
import thut.api.entity.genetics.Alleles;
import thut.api.entity.genetics.Gene;
import thut.api.entity.genetics.GeneRegistry;
import thut.api.entity.genetics.IMobGenetics;
import thut.core.common.ThutCore;

public class ClonerHelper
{
    public static class DNAPack
    {
        public final String        id;
        public final Alleles<?, ?> alleles;
        public final float         chance;

        public DNAPack(final String id, final Alleles<?, ?> alleles, final float chance)
        {
            this.alleles = alleles;
            this.chance = chance;
            this.id = id;
        }

        @Override
        public String toString()
        {
            return this.id;
        }

        @Override
        public int hashCode()
        {
            return this.id.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (!(obj instanceof DNAPack)) return false;
            return this.toString().equals(obj.toString());
        }
    }

    public static final String SELECTORTAG = "DNASelector";

    public static Map<Ingredient, DNAPack> DNAITEMS = Maps.newHashMap();

    public static PokedexEntry getFromGenes(final ItemStack stack)
    {
        final IMobGenetics genes = ClonerHelper.getGenes(stack);
        if (genes == null) return null;
        final Alleles<SpeciesInfo, SpeciesGene> gene = genes.getAlleles(GeneticsManager.SPECIESGENE);
        if (gene != null)
        {
            final SpeciesGene sgene = gene.getExpressed();
            final SpeciesInfo info = sgene.getValue();
            return info.entry;
        }
        return null;
    }

    public static Class<? extends Gene<?>> getGene(final String line)
    {
        final String[] args = line.split(":");
        String domain = "pokecube";
        String path = "";
        if (args.length == 2)
        {
            domain = args[0];
            path = args[1];
        }
        else path = args[0];
        path = path.split("#")[0];
        path = ThutCore.trim(path);
        final ResourceLocation location = new ResourceLocation(domain, path);
        final Class<? extends Gene<?>> geneClass = GeneRegistry.getClass(location);
        return geneClass;
    }

    public static IMobGenetics getGenes(final ItemStack stack)
    {
        if (stack.isEmpty() || !stack.hasTag()) return null;
        final CompoundNBT nbt = stack.getTag();
        if (!nbt.contains(GeneticsManager.GENES))
        {
            if (PokecubeManager.isFilled(stack))
            {
                final CompoundNBT poketag = nbt.getCompound(TagNames.POKEMOB);
                if (!poketag.getCompound("ForgeCaps").contains(GeneticsManager.POKECUBEGENETICS.toString()))
                    return null;
                if (!poketag.getCompound("ForgeCaps").getCompound(GeneticsManager.POKECUBEGENETICS.toString()).contains(
                        "V")) return null;
                final INBT genes = poketag.getCompound("ForgeCaps").getCompound(GeneticsManager.POKECUBEGENETICS
                        .toString()).get("V");
                final IMobGenetics eggs = GeneRegistry.GENETICS_CAP.getDefaultInstance();
                GeneRegistry.GENETICS_CAP.getStorage().readNBT(GeneRegistry.GENETICS_CAP, eggs, null, genes);
                return eggs;
            }
            return null;
        }
        final INBT genes = nbt.get(GeneticsManager.GENES);
        final IMobGenetics eggs = GeneRegistry.GENETICS_CAP.getDefaultInstance();
        GeneRegistry.GENETICS_CAP.getStorage().readNBT(GeneRegistry.GENETICS_CAP, eggs, null, genes);
        if (eggs.getAlleles().isEmpty()) return null;
        return eggs;
    }

    public static Set<Class<? extends Gene<?>>> getGeneSelectors(final ItemStack stack)
    {
        final Set<Class<? extends Gene<?>>> ret = Sets.newHashSet();
        if (stack.isEmpty() || !stack.hasTag()) return ret;
        if (stack.getTag().contains("pages") && stack.getTag().get("pages") instanceof ListNBT)
        {
            final ListNBT pages = (ListNBT) stack.getTag().get("pages");
            try
            {
                final ITextComponent comp = ITextComponent.Serializer.fromJson(pages.getString(0));
                for (final String line : comp.getString().split("\n"))
                {
                    if (line.equalsIgnoreCase("ALL"))
                    {
                        ret.addAll(GeneRegistry.getGenes());
                        break;
                    }
                    final Class<? extends Gene<?>> geneClass = ClonerHelper.getGene(line);
                    if (geneClass != null) ret.add(geneClass);
                }
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.warn("Error locating selectors for " + stack + " " + stack.getTag(), e);
            }
        }
        return ret;
    }

    public static int getIndex(final ItemStack stack)
    {
        if (stack.isEmpty() || !stack.hasTag()) return -1;
        if (stack.getTag().contains("pages") && stack.getTag().get("pages") instanceof ListNBT)
        {
            final ListNBT pages = (ListNBT) stack.getTag().get("pages");
            try
            {
                final ITextComponent comp = ITextComponent.Serializer.fromJson(pages.getString(0));
                for (final String line : comp.getString().split("\n"))
                {
                    if (line.equalsIgnoreCase("ALL")) return -1;
                    final String[] args = line.split("#");
                    if (args.length == 2) return Integer.parseInt(args[1]);
                }
            }
            catch (final Exception e)
            {
                PokecubeCore.LOGGER.warn("Error checking index for " + stack + " " + stack.getTag(), e);
            }
        }
        return -1;
    }

    public static SelectorValue getSelectorValue(final ItemStack selector)
    {
        final SelectorValue def = RecipeSelector.getSelectorValue(selector);
        if (selector.isEmpty() || !selector.hasTag()) return def;
        final CompoundNBT selectorTag = selector.getTag().getCompound(ClonerHelper.SELECTORTAG);
        return SelectorValue.load(selectorTag);
    }

    public static boolean isDNAContainer(final ItemStack stack)
    {
        if (stack.isEmpty() || !stack.hasTag()) return false;
        final String potion = stack.getTag().getString("Potion");
        return potion.equals("minecraft:water") || potion.equals("minecraft:mundane");
    }

    private static <T, GENE extends Gene<T>> void merge(final IMobGenetics source, final IMobGenetics destination,
            final IGeneSelector selector, final ResourceLocation loc)
    {
        final Alleles<T, GENE> alleles = source.getAlleles(loc);
        Alleles<T, GENE> eggsAllele = destination.getAlleles(loc);
        eggsAllele = selector.merge(alleles, eggsAllele);
        if (eggsAllele != null) destination.getAlleles().put(loc, eggsAllele);
    }

    public static void mergeGenes(final IMobGenetics genesIn, final ItemStack destination, final IGeneSelector selector,
            final boolean force)
    {
        IMobGenetics eggs = ClonerHelper.getGenes(destination);
        if (eggs == null) eggs = GeneRegistry.GENETICS_CAP.getDefaultInstance();
        for (final ResourceLocation loc : genesIn.getKeys())
            ClonerHelper.merge(genesIn, eggs, selector, loc);
        ClonerHelper.setGenes(destination, eggs, force ? EditType.OTHER : EditType.EXTRACT);
    }

    public static void registerDNA(final DNAPack entry, final Ingredient stack)
    {
        ClonerHelper.DNAITEMS.put(stack, entry);
    }

    public static void setGenes(final ItemStack stack, final IMobGenetics genes, final EditType reason)
    {
        if (stack.isEmpty() || !stack.hasTag()) return;
        MinecraftForge.EVENT_BUS.post(new GeneEditEvent(genes, reason));
        final CompoundNBT nbt = stack.getTag();
        final INBT geneTag = GeneRegistry.GENETICS_CAP.getStorage().writeNBT(GeneRegistry.GENETICS_CAP, genes, null);
        if (PokecubeManager.isFilled(stack))
        {
            final CompoundNBT poketag = nbt.getCompound(TagNames.POKEMOB);
            poketag.getCompound("ForgeCaps").getCompound(GeneticsManager.POKECUBEGENETICS.toString()).put("V", geneTag);
        }
        else nbt.put(GeneticsManager.GENES, geneTag);
    }

    private static <T, GENE extends Gene<T>> void splice(final IMobGenetics source, final IMobGenetics destination,
            final IGeneSelector selector, final ResourceLocation loc)
    {
        Alleles<T, GENE> alleles = source.getAlleles(loc);
        final Alleles<T, GENE> eggsAllele = destination.getAlleles(loc);
        alleles = selector.merge(alleles, eggsAllele);
        if (alleles != null)
        {
            final Random rand = ThutCore.newRandom();
            if (alleles.getExpressed().getEpigeneticRate() > rand.nextFloat())
            {
                final GENE gene = alleles.getAllele(rand.nextBoolean() ? 0 : 1);
                alleles.setExpressed(gene);
            }
            destination.getAlleles().put(loc, alleles);
        }
    }

    public static void spliceGenes(final IMobGenetics genesIn, final ItemStack destination,
            final IGeneSelector selector)
    {
        IMobGenetics eggs = ClonerHelper.getGenes(destination);
        if (eggs == null) eggs = GeneRegistry.GENETICS_CAP.getDefaultInstance();
        ClonerHelper.setGenes(destination, genesIn, EditType.EXTRACT);
        for (final ResourceLocation loc : genesIn.getKeys())
            ClonerHelper.splice(genesIn, eggs, selector, loc);
        ClonerHelper.setGenes(destination, eggs, EditType.SPLICE);
    }
}
