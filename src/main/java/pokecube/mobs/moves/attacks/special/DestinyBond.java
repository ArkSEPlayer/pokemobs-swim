package pokecube.mobs.moves.attacks.special;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import pokecube.api.PokecubeAPI;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.moves.MovePacket;
import pokecube.api.events.pokemobs.RecallEvent;
import pokecube.api.events.pokemobs.combat.KillEvent;
import pokecube.api.moves.IMoveConstants;
import pokecube.api.utils.PokeType;
import pokecube.core.moves.MovesUtils;
import pokecube.core.moves.damage.PokemobDamageSource;
import pokecube.core.moves.templates.Move_Basic;

public class DestinyBond extends Move_Basic
{
    Map<UUID, Set<UUID>> usedOn = Maps.newHashMap();

    public DestinyBond()
    {
        super("destinybond");
        this.setNotInterceptable();
    }

    @SubscribeEvent
    public void onKill(final KillEvent event)
    {
        final UUID killed = event.killed.getEntity().getUUID();
        final Set<UUID> targets = this.usedOn.remove(killed);

        if (targets != null && event.killed.getEntity().getLevel() instanceof ServerLevel)
        {
            final ServerLevel world = (ServerLevel) event.killed.getEntity().getLevel();
            final DamageSource source = new PokemobDamageSource(event.killed.getEntity(), this);
            source.bypassMagic();
            source.bypassArmor();
            for (final UUID id : targets)
            {
                final Entity mob = world.getEntity(id);
                if (mob != null && !mob.isInvulnerable()) mob.hurt(source, Float.MAX_VALUE);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRecall(final RecallEvent event)
    {
        this.usedOn.remove(event.recalled.getEntity().getUUID());
    }

    @Override
    public void init()
    {
        PokecubeAPI.POKEMOB_BUS.register(this);
    }

    @Override
    public void destroy()
    {
        PokecubeAPI.POKEMOB_BUS.unregister(this);
    }

    @Override
    public boolean isSelfMove()
    {
        return false;
    }

    @Override
    public byte getAttackCategory()
    {
        return IMoveConstants.CATEGORY_DISTANCE;
    }

    @Override
    public void onAttack(MovePacket packet)
    {
        this.preAttack(packet);
        if (packet.denied) return;

        final IPokemob attacker = packet.attacker;
        final LivingEntity attackerMob = attacker.getEntity();
        final Entity attacked = packet.attacked;
        final String attack = packet.attack;
        final PokeType type = packet.attackType;
        final int PWR = packet.PWR;
        final int criticalLevel = packet.criticalLevel;
        final byte statusChange = packet.statusChange;
        final byte changeAddition = packet.changeAddition;

        final UUID userId = attackerMob.getUUID();
        final Set<UUID> hits = this.usedOn.getOrDefault(userId, Sets.newHashSet());
        final boolean added = hits.add(attacked.getUUID());
        this.usedOn.put(userId, hits);

        if (!added) if (packet.failed)
        {
            MovesUtils.displayEfficiencyMessages(attacker, attacked, -2, 0);
            packet = new MovePacket(attacker, attacked, attack, type, PWR, criticalLevel, statusChange, changeAddition,
                    false);
            packet.hit = false;
            packet.didCrit = false;
            this.postAttack(packet);
            return;
        }

        this.postAttack(packet);
    }
}
