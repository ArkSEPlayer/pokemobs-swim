package pokecube.core.impl.entity.impl;

import net.minecraft.resources.ResourceLocation;
import pokecube.api.entity.IOngoingAffected.IOngoingEffect;

public abstract class BaseEffect implements IOngoingEffect
{
    private final ResourceLocation id;

    private int duration;

    public BaseEffect(ResourceLocation id)
    {
        this.id = id;
    }

    @Override
    public int getDuration()
    {
        return this.duration;
    }

    @Override
    public ResourceLocation getID()
    {
        return this.id;
    }

    @Override
    public void setDuration(int duration)
    {
        this.duration = duration;
    }

}
