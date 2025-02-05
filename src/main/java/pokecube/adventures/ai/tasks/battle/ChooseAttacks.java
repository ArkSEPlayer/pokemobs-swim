package pokecube.adventures.ai.tasks.battle;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.PokemobCaps;
import pokecube.api.moves.Move_Base;
import pokecube.api.utils.PokeType;
import pokecube.core.ai.brain.BrainUtils;
import pokecube.core.moves.MovesUtils;

public class ChooseAttacks extends BaseBattleTask
{
    public ChooseAttacks(final LivingEntity trainer)
    {
        super(trainer);
    }

    /**
     * @param move
     *            - the attack to check
     * @param user
     *            - the user of the sttack
     * @param target
     *            - the target of the attack
     * @return - the damage that will be dealt by the attack (before reduction
     *         due to armour)
     */
    private int getPower(final String move, final IPokemob user, final Entity target)
    {
        final Move_Base attack = MovesUtils.getMoveFromName(move);
        if (attack == null) return 0;
        int pwr = attack.getPWR(user, target);
        final IPokemob mob = PokemobCaps.getPokemobFor(target);
        if (mob != null) pwr *= PokeType.getAttackEfficiency(attack.getType(user), mob.getType1(), mob.getType2());
        return pwr;
    }

    /**
     * Searches for pokemobs most damaging move against the target, and sets it
     * as current attack
     */
    private void setMostDamagingMove()
    {
        final IPokemob outMob = this.trainer.getOutMob();
        int index = outMob.getMoveIndex();
        int max = 0;
        final Entity target = BrainUtils.getAttackTarget(outMob.getEntity());
        final String[] moves = outMob.getMoves();
        for (int i = 0; i < 4; i++)
        {
            final String s = moves[i];
            if (s != null)
            {
                final int temp = this.getPower(s, outMob, target);
                if (temp > max)
                {
                    index = i;
                    max = temp;
                }
            }
        }
        outMob.setMoveIndex(index);
    }

    private void considerSwapMove()
    {
        // TODO choose between damaging/stats/status moves
        this.setMostDamagingMove();
    }

    @Override
    protected void tick(final ServerLevel worldIn, final LivingEntity owner, final long gameTime)
    {
        // If trainer has a living, real mob out, tell it to do stuff.
        // Check if pokemob has a valid Pokemob as a target.
        if (PokemobCaps.getPokemobFor(this.target) != null)
            // using best move for target.
            this.considerSwapMove();
        // Otherwise just pick whatever does most damage
        else this.setMostDamagingMove();
    }

    @Override
    protected boolean canStillUse(final ServerLevel worldIn, final LivingEntity entityIn,
            final long gameTimeIn)
    {
        return this.trainer.getOutMob() != null;
    }

    @Override
    protected boolean checkExtraStartConditions(final ServerLevel worldIn, final LivingEntity owner)
    {
        if (!super.checkExtraStartConditions(worldIn, owner)) return false;
        return this.trainer.getOutMob() != null;
    }
}
