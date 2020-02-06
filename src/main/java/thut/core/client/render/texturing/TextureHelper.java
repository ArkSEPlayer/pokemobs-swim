package thut.core.client.render.texturing;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import thut.api.entity.IMobTexturable;

public class TextureHelper implements IPartTexturer
{
    private static class RandomState
    {
        double   chance   = 0.005;
        double[] arr;
        int      duration = 1;

        RandomState(String trigger, double[] arr)
        {
            this.arr = arr;
            final String[] args = trigger.split(":");
            if (args.length > 1) this.chance = Double.parseDouble(args[1]);
            if (args.length > 2) this.duration = Integer.parseInt(args[2]);
        }
    }

    private static class SequenceState
    {
        double[] arr;
        boolean  shift = true;

        SequenceState(double[] arr)
        {
            this.arr = arr;
            for (final double d : arr)
                if (d >= 1) this.shift = false;
        }
    }

    private static class TexState
    {
        Map<String, double[]>     infoStates   = Maps.newHashMap();
        Set<RandomState>          randomStates = Sets.newHashSet();
        SequenceState             sequence     = null;
        // TODO way to handle cheaning this up.
        Map<Integer, RandomState> running      = Maps.newHashMap();
        Map<Integer, Integer>     setTimes     = Maps.newHashMap();

        void addState(String trigger, String[] diffs)
        {
            final double[] arr = new double[diffs.length];
            for (int i = 0; i < arr.length; i++)
                arr[i] = Double.parseDouble(diffs[i].trim());

            if (trigger.contains("random")) this.randomStates.add(new RandomState(trigger, arr));
            else if (trigger.equals("sequence") || trigger.equals("time")) this.sequence = new SequenceState(arr);
            else if (this.parseState(trigger, arr))
            {

            }
            else new NullPointerException("No Template found for " + trigger).printStackTrace();
        }

        boolean applyState(double[] toFill, IMobTexturable mob)
        {
            double dx = 0;
            double dy = 0;
            toFill[0] = dx;
            toFill[1] = dy;
            final Random random = new Random();
            final List<String> states = mob.getTextureStates();
            for (final String state : states)
                if (this.infoStates.containsKey(state))
                {
                    final double[] arr = this.infoStates.get(state);
                    dx = arr[0];
                    dy = arr[1];
                    toFill[0] = dx;
                    toFill[1] = dy;
                    return true;
                }

            if (this.running.containsKey(mob.getEntity().getEntityId()))
            {
                final RandomState run = this.running.get(mob.getEntity().getEntityId());
                final double[] arr = run.arr;
                dx = arr[0];
                dy = arr[1];
                toFill[0] = dx;
                toFill[1] = dy;
                if (mob.getEntity().ticksExisted > this.setTimes.get(mob.getEntity().getEntityId()) + run.duration)
                {
                    this.running.remove(mob.getEntity().getEntityId());
                    this.setTimes.remove(mob.getEntity().getEntityId());
                }
                return true;
            }
            for (final RandomState state : this.randomStates)
            {
                final double[] arr = state.arr;
                if (random.nextFloat() < state.chance)
                {
                    dx = arr[0];
                    dy = arr[1];
                    toFill[0] = dx;
                    toFill[1] = dy;
                    this.running.put(mob.getEntity().getEntityId(), state);
                    this.setTimes.put(mob.getEntity().getEntityId(), mob.getEntity().ticksExisted);
                    return true;
                }
            }
            if (this.sequence != null && this.sequence.shift)
            {
                final int tick = mob.getEntity().ticksExisted % (this.sequence.arr.length / 2);
                dx = this.sequence.arr[tick * 2];
                dy = this.sequence.arr[tick * 2 + 1];
                toFill[0] = dx;
                toFill[1] = dy;
                return true;
            }
            return false;
        }

        String modifyTexture(IMobTexturable mob)
        {
            if (this.sequence != null && !this.sequence.shift)
            {
                final int tick = mob.getEntity().ticksExisted % (this.sequence.arr.length / 2);
                final int dx = (int) this.sequence.arr[tick * 2];
                return "" + dx;
            }
            return null;
        }

        private boolean parseState(String trigger, double[] arr)
        {
            if (trigger != null) trigger = trigger.trim().toLowerCase(Locale.ENGLISH);
            else return false;
            this.infoStates.put(trigger, arr);
            return true;
        }
    }

    @CapabilityInject(IMobTexturable.class)
    public static final Capability<IMobTexturable> CAPABILITY   = null;

    IMobTexturable                                 mob;
    /** Map of part/material name -> texture name */
    Map<String, String>                            texNames     = Maps.newHashMap();
    /** Map of part/material name -> map of custom state -> texture name */
    Map<String, Map<String, String>>               texNames2    = Maps.newHashMap();
    public ResourceLocation                        default_tex;
    String                                         default_path;

    Map<String, Boolean>                           smoothing    = Maps.newHashMap();

    boolean                                        default_flat = true;

    /** Map of part/material name -> resource location */
    Map<String, ResourceLocation>                  texMap       = Maps.newHashMap();

    Map<String, TexState>                          texStates    = Maps.newHashMap();

    Map<String, String>                            formeMap     = Maps.newHashMap();

    public TextureHelper(Node node)
    {
        if (node == null) return;
        if (node.getAttributes().getNamedItem("default") != null)
            this.default_path = node.getAttributes().getNamedItem("default").getNodeValue();
        if (node.getAttributes().getNamedItem("smoothing") != null)
        {
            final boolean flat = !node.getAttributes().getNamedItem("smoothing").getNodeValue()
                    .equalsIgnoreCase("smooth");
            this.default_flat = flat;
        }
        final NodeList parts = node.getChildNodes();
        for (int i = 0; i < parts.getLength(); i++)
        {
            final Node part = parts.item(i);
            if (part.getNodeName().equals("part"))
            {
                final String partName = part.getAttributes().getNamedItem("name").getNodeValue();
                final String partTex = part.getAttributes().getNamedItem("tex").getNodeValue();
                this.addMapping(partName, partTex);
                if (part.getAttributes().getNamedItem("smoothing") != null)
                {
                    final boolean flat = !node.getAttributes().getNamedItem("smoothing").getNodeValue()
                            .equalsIgnoreCase("smooth");
                    this.smoothing.put(partName, flat);
                }
            }
            else if (part.getNodeName().equals("animation"))
            {
                final String partName = part.getAttributes().getNamedItem("part").getNodeValue();
                final String trigger = part.getAttributes().getNamedItem("trigger").getNodeValue();
                final String[] diffs = part.getAttributes().getNamedItem("diffs").getNodeValue().split(",");
                TexState states = this.texStates.get(partName);
                if (states == null) this.texStates.put(partName, states = new TexState());
                states.addState(trigger, diffs);
            }
            else if (part.getNodeName().equals("custom"))
            {
                final String partName = part.getAttributes().getNamedItem("part").getNodeValue();
                final String state = part.getAttributes().getNamedItem("state").getNodeValue();
                final String partTex = part.getAttributes().getNamedItem("tex").getNodeValue();
                this.addCustomMapping(partName, state, partTex);
            }
            else if (part.getNodeName().equals("forme"))
            {
                final String name = part.getAttributes().getNamedItem("name").getNodeValue();
                final String tex = part.getAttributes().getNamedItem("tex").getNodeValue();
                this.formeMap.put(name.toLowerCase(java.util.Locale.ENGLISH).replace(" ", ""), tex);
            }
        }
    }

    @Override
    public void addCustomMapping(String part, String state, String tex)
    {
        Map<String, String> partMap = this.texNames2.get(part);
        if (partMap == null)
        {
            partMap = Maps.newHashMap();
            this.texNames2.put(part, partMap);
        }
        partMap.put(state, tex);
    }

    @Override
    public void addMapping(String part, String tex)
    {
        this.texNames.put(part, tex);
    }

    @Override
    public ResourceLocation getTexture(String part, ResourceLocation default_)
    {
        if (this.mob == null) return default_;
        ResourceLocation tex = this.bindPerState(part);
        if (tex != null) return tex;
        final String texName = this.texNames.containsKey(part) ? this.texNames.get(part) : this.default_path;
        if (texName == null || texName.trim().isEmpty()) this.texNames.put(part, this.default_path);
        tex = this.getResource(texName);
        TexState state;
        String texMod;
        if ((state = this.texStates.get(part)) != null && (texMod = state.modifyTexture(this.mob)) != null)
            tex = this.getResource(tex.getPath() + texMod);
        return tex;
    }

    @Override
    public void bindObject(Object thing)
    {
        this.mob = ((ICapabilityProvider) thing).getCapability(TextureHelper.CAPABILITY).orElse(null);
        if (this.mob != null) this.default_tex = this.getResource(this.default_path);
    }

    private ResourceLocation bindPerState(String part)
    {
        final Map<String, String> partNames = this.texNames2.get(part);
        if (partNames == null) return null;
        final List<String> states = this.mob.getTextureStates();
        for (final String key : partNames.keySet())
            if (states.contains(key))
            {
                final String texKey = part + key;
                String tex;
                if ((tex = this.texNames.get(texKey)) != null)
                {
                }
                else
                {
                    tex = partNames.get(key);
                    this.texNames.put(texKey, tex);
                }
                TexState state;
                String texMod;
                if ((state = this.texStates.get(part)) != null && (texMod = state.modifyTexture(this.mob)) != null)
                    tex = tex + texMod;
                return this.getResource(tex);
            }
        return null;
    }

    private ResourceLocation getResource(String tex)
    {
        if (tex == null) return this.mob.getTexture(null);
        else if (tex.contains(":")) return new ResourceLocation(tex);
        else return new ResourceLocation(this.mob.getModId(), tex);
    }

    @Override
    public boolean hasMapping(String part)
    {
        return this.texNames.containsKey(part);
    }

    @Override
    public boolean isFlat(String part)
    {
        if (this.smoothing.containsKey(part)) return this.smoothing.get(part);
        return this.default_flat;
    }

    @Override
    public boolean shiftUVs(String part, double[] toFill)
    {
        if (this.mob == null) return false;
        TexState state;
        if ((state = this.texStates.get(part)) != null) return state.applyState(toFill, this.mob);
        return false;
    }

}