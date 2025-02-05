package pokecube.api.entity.pokemob.commandhandlers;

import io.netty.buffer.ByteBuf;
import pokecube.api.entity.pokemob.IPokemob;
import pokecube.api.entity.pokemob.ai.CombatStates;
import pokecube.api.entity.pokemob.ai.GeneralStates;
import pokecube.api.entity.pokemob.ai.LogicStates;
import pokecube.core.PokecubeCore;
import pokecube.core.ai.routes.IGuardAICapability;
import pokecube.core.network.pokemobs.PacketCommand.DefaultHandler;
import pokecube.core.utils.CapHolders;
import pokecube.core.utils.TimePeriod;
import thut.api.maths.Vector3;
import thut.lib.TComponent;

public class StanceHandler extends DefaultHandler
{
    public static final byte STAY   = 0;
    public static final byte GUARD  = 1;
    public static final byte SIT    = 2;
    public static final byte GZMOVE = 3;

    boolean state;
    byte    key;

    public StanceHandler()
    {
    }

    public StanceHandler(final Boolean state, final Byte key)
    {
        this.state = state;
        this.key = key;
    }

    @Override
    public void handleCommand(final IPokemob pokemob) throws Exception
    {
        boolean stay = pokemob.getGeneralState(GeneralStates.STAYING);
        final IGuardAICapability guard = pokemob.getEntity().getCapability(CapHolders.GUARDAI_CAP, null).orElse(null);
        switch (this.key)
        {
        case STAY:
            pokemob.setGeneralState(GeneralStates.STAYING, stay = !pokemob.getGeneralState(GeneralStates.STAYING));
            break;
        case GUARD:
            if (PokecubeCore.getConfig().guardModeEnabled) pokemob.setCombatState(CombatStates.GUARDING, !pokemob
                    .getCombatState(CombatStates.GUARDING));
            else pokemob.displayMessageToOwner(TComponent.translatable("pokecube.config.guarddisabled"));
            break;
        case SIT:
            pokemob.setLogicState(LogicStates.SITTING, !pokemob.getLogicState(LogicStates.SITTING));
            break;
        case GZMOVE:
            pokemob.setCombatState(CombatStates.USINGGZMOVE, !pokemob.getCombatState(CombatStates.USINGGZMOVE));
            pokemob.displayMessageToOwner(TComponent.translatable("pokecube.gzmode." + (pokemob.getCombatState(
                    CombatStates.USINGGZMOVE) ? "set" : "unset")));
            break;
        }
        if (stay)
        {
            final Vector3 mid = new Vector3().set(pokemob.getEntity());
            if (guard != null)
            {
                guard.getPrimaryTask().setActiveTime(TimePeriod.fullDay);
                guard.getPrimaryTask().setPos(mid.getPos());
            }
        }
        else if (guard != null) guard.getPrimaryTask().setActiveTime(TimePeriod.never);
    }

    @Override
    public void readFromBuf(final ByteBuf buf)
    {
        super.readFromBuf(buf);
        this.state = buf.readBoolean();
        this.key = buf.readByte();
    }

    @Override
    public void writeToBuf(final ByteBuf buf)
    {
        super.writeToBuf(buf);
        buf.writeBoolean(this.state);
        buf.writeByte(this.key);
    }

}
