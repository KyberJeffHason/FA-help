package lotr.common.command;

import java.util.List;
import lotr.common.LOTRLevelData;
import net.minecraft.command.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class LOTRCommandFastTravelTimer extends CommandBase {
    @Override
    public String getCommandName() {
        return "fastTravelTimer";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.lotr.fastTravelTimer.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if(args.length >= 1) {
            EntityPlayerMP entityplayer;
            int timer = CommandBase.parseIntBounded(sender, args[0], 0, LOTRCommandFastTravelCooldown.MAX_COOLDOWN);
            if(args.length >= 2) {
                entityplayer = CommandBase.getPlayer(sender, args[1]);
            }
            else {
                entityplayer = CommandBase.getCommandSenderAsPlayer(sender);
                if(entityplayer == null) {
                    throw new PlayerNotFoundException();
                }
            }
            LOTRLevelData.getData(entityplayer).setFTTimer(timer);
            CommandBase.func_152373_a(sender, this, "commands.lotr.fastTravelTimer.set", entityplayer.getCommandSenderName(), timer, LOTRLevelData.getHMSTime(timer));
            return;
        }
        throw new WrongUsageException(this.getCommandUsage(sender), new Object[0]);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if(args.length == 2) {
            return CommandBase.getListOfStringsMatchingLastWord(args, MinecraftServer.getServer().getAllUsernames());
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i) {
        return i == 1;
    }
}
