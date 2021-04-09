package lotr.common.command;

import java.util.List;
import lotr.common.LOTRLevelData;
import net.minecraft.command.*;

public class LOTRCommandFastTravelCooldown extends CommandBase {
    public static int MAX_COOLDOWN = 1728000;

    @Override
    public String getCommandName() {
        return "fastTravelCooldown";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "commands.lotr.fastTravelCooldown.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        String function = null;
        int cooldown = -1;
        if(args.length == 1) {
            function = "max";
            cooldown = CommandBase.parseIntBounded(sender, args[0], 0, MAX_COOLDOWN);
        }
        else if(args.length >= 2) {
            function = args[0];
            cooldown = CommandBase.parseIntBounded(sender, args[1], 0, MAX_COOLDOWN);
        }
        if(function != null && cooldown >= 0) {
            int max = LOTRLevelData.getFTCooldownMax();
            int min = LOTRLevelData.getFTCooldownMin();
            if(function.equals("max")) {
                boolean updatedMin = false;
                max = cooldown;
                if(max < min) {
                    min = max;
                    updatedMin = true;
                }
                LOTRLevelData.setFTCooldown(max, min);
                CommandBase.func_152373_a(sender, this, "commands.lotr.fastTravelCooldown.setMax", max, LOTRLevelData.getHMSTime(max));
                if(updatedMin) {
                    CommandBase.func_152373_a(sender, this, "commands.lotr.fastTravelCooldown.updateMin", min);
                }
                return;
            }
            if(function.equals("min")) {
                boolean updatedMax = false;
                min = cooldown;
                if(min > max) {
                    max = min;
                    updatedMax = true;
                }
                LOTRLevelData.setFTCooldown(max, min);
                CommandBase.func_152373_a(sender, this, "commands.lotr.fastTravelCooldown.setMin", min, LOTRLevelData.getHMSTime(min));
                if(updatedMax) {
                    CommandBase.func_152373_a(sender, this, "commands.lotr.fastTravelCooldown.updateMax", max);
                }
                return;
            }
        }
        throw new WrongUsageException(this.getCommandUsage(sender), new Object[0]);
    }

    @Override
    public List addTabCompletionOptions(ICommandSender sender, String[] args) {
        if(args.length == 1) {
            return CommandBase.getListOfStringsMatchingLastWord(args, "max", "min");
        }
        return null;
    }

    @Override
    public boolean isUsernameIndex(String[] args, int i) {
        return false;
    }
}
