package me.affanhaq.mapcha;

import me.affanhaq.mapcha.events.MapEvent;
import me.affanhaq.mapcha.events.PlayerEvent;
import me.affanhaq.mapcha.player.CaptchaPlayerManager;
import me.ihaq.keeper.Keeper;
import me.ihaq.keeper.data.ConfigValue;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import static org.bukkit.ChatColor.*;

public class Mapcha extends JavaPlugin {

    private final CaptchaPlayerManager playerManager = new CaptchaPlayerManager();

    @Override
    public void onEnable() {
        new Keeper(this)
                .register(new Config())
                .load();

        // registering events
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(new PlayerEvent(this), this);
        pluginManager.registerEvents(new MapEvent(this), this);

        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    public CaptchaPlayerManager getPlayerManager() {
        return playerManager;
    }

    public static class Config {

        public static String permission = "mapcha.bypass";

        @ConfigValue("tries")
        public static int captchaTries = 5;

        @ConfigValue("time_limit")
        public static int captchaTimeLimit = 60;

        @ConfigValue("success_server")
        public static String successServer = "main";

        @ConfigValue("messages.success")
        public static String captchaSuccessMessage = GREEN + "Success! Joining main server...";

        @ConfigValue("messages.retry")
        public static String captchaRetryMessage = RED + "Captcha failed, please try again! ({CURRENT}/{MAX})";

        @ConfigValue("messages.fail")
        public static String captchaFailMessage = RED + "Captcha failed!";
    }

}