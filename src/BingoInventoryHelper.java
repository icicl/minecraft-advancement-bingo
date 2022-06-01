import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.FurnaceInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;

public class BingoInventoryHelper implements Listener {

    private final int FURNACE_SPEED;
    boolean full_inv;
    private final Main plugin;
    private int burntime;

    public BingoInventoryHelper(Main plugin) {
        this.plugin = plugin;
        this.FURNACE_SPEED = this.plugin.getConfig().getInt("furnace-speed");
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent e) {
        if (plugin.game == null || plugin.game.in_progress == false) {
            return;
        }
        Player player = (Player) e.getPlayer();
        BingoPlayer bplayer = this.plugin.game.get_player(player);
        if (
                bplayer.goals_remaining.contains(e.getAdvancement().getKey().getKey())
        ) {
            find_goal(bplayer, e.getAdvancement().getKey().getKey());
        }
    }




    public void find_goal(BingoPlayer bplayer, String adv) {
        bplayer.find(adv);
        for (BingoPlayer bp : plugin.game.players) {
            bp.update();
        }
        plugin.tabagent.updatePlayer(bplayer.player);
    }

    @EventHandler//fast smelt
    public void onFurnaceBurn(FurnaceBurnEvent event) {
        startUpdate((Furnace) event.getBlock().getState(), FURNACE_SPEED, event.getBlock());//event.getBurnTime()
    }

    private void startUpdate(Furnace block_, int speed, Block fblock) {
        FurnaceInventory finv = block_.getInventory();
        new BukkitRunnable() {
            public void run() {
                Furnace block = finv.getHolder();
                if (block == null) {
                    cancel();
                }
                block.setCookTime((short) (block.getCookTime() + speed - 1));
                block.setBurnTime((short) (block.getBurnTime() - speed + 1));
                //plugin.log(""+block.getCookTime()+" "+block.getCookTimeTotal()+" "+block.getBurnTime());
                //block.setBurnTime((short) (block.getBurnTime()-speed));
                //plugin.log(finv.getFuel().toString()+" "+finv.getSmelting()+" "+finv.getResult());
                if (block.getCookTime() >= block.getCookTimeTotal()) {
                    block.setCookTime((short) (block.getCookTimeTotal() - 1));
                    //new FurnaceSmeltEvent(fblock, finv.getSmelting(), finv.getResult());
                }
                block.update();
                if (block.getBurnTime() <= 0) {
                    //plugin.log("w"+burntime);
                    cancel();
                }
        /*if (block.getBurnTime() > 0) {
          plugin.log(""+block.getCookTime()+"*"+block.getBurnTime());
          block.update();
        } else {
          plugin.log(""+block.getCookTime()+"*"+block.getBurnTime());
          cancel();
        }*/
            }
        }.runTaskTimer(this.plugin, 1, 1);
    }

}
