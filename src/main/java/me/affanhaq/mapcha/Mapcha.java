package me.affanhaq.mapcha;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.affanhaq.keeper.Keeper;
import me.affanhaq.keeper.data.ConfigValue;
import me.affanhaq.mapcha.handlers.CaptchaHandler;
import me.affanhaq.mapcha.handlers.MapHandler;
import me.affanhaq.mapcha.handlers.PlayerHandler;
import me.affanhaq.mapcha.player.CaptchaPlayerManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static me.affanhaq.mapcha.Mapcha.Config.sendToSuccessServer;
import static me.affanhaq.mapcha.Mapcha.Config.successServerName;
import static org.bukkit.ChatColor.*;

public class Mapcha extends JavaPlugin {

    private final CaptchaPlayerManager playerManager = new CaptchaPlayerManager();
    private final Set<UUID> completedCache = new HashSet<>();

    /**
     * Sends a player to a connected server after the captcha is completed.
     *
     * @param player the player to send
     */
    public static void sendPlayerToServer(JavaPlugin javaPlugin, Player player) {
        if (sendToSuccessServer && successServerName != null && !successServerName.isEmpty()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(successServerName);
            player.sendPluginMessage(javaPlugin, "BungeeCord", out.toByteArray());
        }
    }

    @Override
    public void onEnable() {
        new Keeper(this).register(new Config()).load();

        // registering events
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerHandler(this), this);
        pluginManager.registerEvents(new MapHandler(this), this);
        pluginManager.registerEvents(new CaptchaHandler(this), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    public CaptchaPlayerManager getPlayerManager() {
        return playerManager;
    }

    public Set<UUID> getCompletedCache() {
        return completedCache;
    }

    public static class Config {
        public static String permission = "mapcha.bypass";

        @ConfigValue("prefix")
        public static String prefix = "[" + GREEN + "Mapcha" + RESET + "]";

        @ConfigValue("captcha.cache")
        public static boolean useCompletedCache = false;

        @ConfigValue("captcha.tries")
        public static int tries = 5;

        @ConfigValue("captcha.time")
        public static int timeLimit = 30;

        @ConfigValue("server.enabled")
        public static boolean sendToSuccessServer = true;

        @ConfigValue("server.name")
        public static String successServerName = "main";

        @ConfigValue("messages.success")
        public static String successMessage = GREEN + "Success! Joining main server...";

        @ConfigValue("messages.retry")
        public static String retryMessage = RED + "Captcha failed, please try again! ({CURRENT}/{MAX})";

        @ConfigValue("messages.fail")
        public static String failMessage = RED + "Captcha failed!";
    }
}
