import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;
import java.util.Random;

public class Main extends JavaPlugin {

    public GameState game;
    public TabAgent tabagent;
    public GameDaemon daemon;
    private long seed = new Random().nextLong();
    public final MaterialDisplay materialDisplay = new MaterialDisplay();

    public void onEnable() {
        PluginCommand bingoCommand = this.getCommand("adv");
        bingoCommand.setExecutor(new BingoCommandExecutor(this));
        bingoCommand.setTabCompleter(new TabCompletionManager(this));
        saveDefaultConfig();
        Bukkit
                .getPluginManager()
                .registerEvents(new BingoInventoryHelper(this), this);
        tabagent = new TabAgent(this);
        Bukkit.getPluginManager().registerEvents(tabagent, this);
        Bukkit.getLogger().info("Advancement Bingo plugin has been enabled!");
        daemon = new GameDaemon(this);
        daemon.runTaskTimer(this, 0, 20);
        //getMaterials(true);
    }

    public void onDisable() {
        Bukkit.getLogger().info("Advancement Bingo plugin has been disabled.");
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long newSeed) {
        this.seed = newSeed;
    }

    public List<String> getAdvancements(boolean advanced) {//todo use time instead of bool
        Random rand = new Random(this.seed);
        List<String> advs = new ArrayList();
        List<Integer> weights = new ArrayList();
        int temp_weight;
        List<Integer> weight_list;
        for (String cfg : getConfig().getConfigurationSection("advancements").getKeys(false)) {
            weight_list = getConfig().getIntegerList("advancements." + cfg + ".weight");
            temp_weight = weight_list.get(0) + (advanced ? weight_list.get(1) : 0);
            if (temp_weight > 0) {
                weights.add(temp_weight);
                advs.add(cfg);
            }

            final Dictionary MATERIALS_DISPLAY;
            MATERIALS_DISPLAY = materialDisplay.get();
            byte[][] pixels = (byte[][]) MATERIALS_DISPLAY.get(
                    getConfig().getString("advancements." + cfg + ".icon").split(":")[1]
            );
            if (pixels == null) {
                Bukkit
                        .getLogger()
                        .severe(
                                "No texture pixel map found for " +
                                        getConfig().getString("advancements." + cfg + ".icon")
                        );
            }


        }
        List<String> res = new ArrayList();
        int totw = 0;
        int r;
        int i;
        int SIZE = 5;
        while (advs.size() > 0 && res.size() < SIZE*SIZE) {
            totw = 0;
            for (int ii : weights) {
                totw += ii;
            }
            r = rand.nextInt(totw);
            i = 0;
            while (true) {
                r -= weights.get(i);
                if (r < 0) {
                    res.add(advs.remove(i));
                    weights.remove(i);
                    break;
                }
                i++;
            }
        }
        return res;
    }

    public void log(String msg) {
        Bukkit.getLogger().warning(msg);
    }

    public void log(int msg) {
        Bukkit.getLogger().warning("" + msg);
    }

}
