package thut.core.client.render.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;

import net.minecraft.client.renderer.Quaternion;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.entity.Entity;
import thut.api.maths.Vector3;
import thut.api.maths.Vector4;
import thut.core.client.render.animation.CapabilityAnimation.IAnimationHolder;
import thut.core.client.render.model.IExtendedModelPart;

/** This class applies the tabula style animations to models consisting of
 * IExtendedModelPart parts.
 *
 * @author Thutmose */
public class AnimationHelper
{
    private final static Map<UUID, IAnimationHolder> holderMap = Maps.newHashMap();

    public static boolean animate(final Animation animation, final IAnimationHolder animate, final String partName,
            final IExtendedModelPart part, final float partialTick, final float limbSwing, final int tick)
    {
        final ArrayList<AnimationComponent> components = animation.getComponents(partName);
        if (components == null) { return false; }
        boolean animated = false;
        final Vector3 temp = Vector3.getNewVector();
        float x = 0, y = 0, z = 0;
        float sx = 1, sy = 1, sz = 1;
        int aniTick = tick;
        float time1 = aniTick;
        float time2 = 0;
        final int animationLength = animation.getLength();
        final float limbSpeedFactor = 3f;
        time1 = (time1 + partialTick) % animationLength;
        time2 = limbSwing * limbSpeedFactor % animationLength;
        aniTick = (int) time1;
        for (final AnimationComponent component : components)
        {
            final float time = component.limbBased ? time2 : time1;
            if (component.limbBased) aniTick = (int) time2;
            if (time >= component.startKey)
            {
                animated = true;
                float componentTimer = time - component.startKey;
                if (componentTimer > component.length) componentTimer = component.length;
                temp.addTo(component.posChange[0] / component.length * componentTimer + component.posOffset[0],
                        component.posChange[1] / component.length * componentTimer + component.posOffset[1],
                        component.posChange[2] / component.length * componentTimer + component.posOffset[2]);
                x += (float) (component.rotChange[0] / component.length * componentTimer + component.rotOffset[0]);
                z += (float) (component.rotChange[1] / component.length * componentTimer + component.rotOffset[1]);
                y += (float) (component.rotChange[2] / component.length * componentTimer + component.rotOffset[2]);

                sx += (float) (component.scaleChange[0] / component.length * componentTimer + component.scaleOffset[0]);
                sy += (float) (component.scaleChange[1] / component.length * componentTimer + component.scaleOffset[1]);
                sz += (float) (component.scaleChange[2] / component.length * componentTimer + component.scaleOffset[2]);

                // Apply hidden like this so last hidden state is kept
                part.setHidden(component.hidden);
            }
        }
        if (animated)
        {
            part.setPreTranslations(temp);
            part.setPreScale(temp.set(sx, sy, sz));
            final Quaternion quat = new Quaternion(0, 0, 0, 1);
            if (z != 0) quat.multiply(Vector3f.ZP.rotationDegrees(z));
            if (y != 0) quat.multiply(Vector3f.YN.rotationDegrees(y));
            if (x != 0) quat.multiply(Vector3f.XP.rotationDegrees(x));
            part.setPreRotations(new Vector4(quat));
        }
        return animated;
    }

    public static boolean doAnimation(final List<Animation> list, final Entity entity, final String partName,
            final IExtendedModelPart part, final float partialTick, final float limbSwing)
    {
        boolean animate = false;
        final IAnimationHolder holder = AnimationHelper.getHolder(entity);
        if (holder != null)
        {
            for (final Animation animation : list)
            {
                animate = AnimationHelper.animate(animation, holder, partName, part, partialTick, limbSwing,
                        entity.ticksExisted) || animate;
                holder.setStep(animation, entity.ticksExisted);
            }
        }
        return animate;
    }

    public static IAnimationHolder getHolder(final Entity mob)
    {
        final IAnimationHolder cap = mob.getCapability(CapabilityAnimation.CAPABILITY, null).orElse(null);
        if (cap != null) return cap;
        if (AnimationHelper.holderMap.containsKey(mob.getUniqueID()))
            return AnimationHelper.holderMap.get(AnimationHelper.holderMap.get(mob.getUniqueID()));
        else
        {
            final CapabilityAnimation.DefaultImpl holder = new CapabilityAnimation.DefaultImpl();
            AnimationHelper.holderMap.put(mob.getUniqueID(), holder);
            return holder;
        }
    }
}
