package pokecube.mobs.moves.attacks.fixedorcustom;

import net.minecraft.world.entity.Entity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.IPokemob.Stats;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.core.moves.templates.Move_Basic;

public class Electroball extends Move_Basic
{

    public Electroball()
    {
        super("electroball");
    }

    @Override
    public int getPWR(IPokemob user, Entity target)
    {
        final IPokemob targetMob = PokemobCaps.getPokemobFor(target);
        if (targetMob == null) return 50;
        final int targetSpeed = targetMob.getStat(Stats.VIT, true);
        final int userSpeed = user.getStat(Stats.VIT, true);
        int pwr = 60;
        final double var = (double) targetSpeed / (double) userSpeed;
        if (var < 0.25) pwr = 150;
        else if (var < 0.33) pwr = 120;
        else if (var < 0.5) pwr = 80;
        else pwr = 60;
        return pwr;
    }
}
