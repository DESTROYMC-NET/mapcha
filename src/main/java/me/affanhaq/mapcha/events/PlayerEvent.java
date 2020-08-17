package me.affanhaq.mapcha.events;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.affanhaq.mapcha.Mapcha;
import me.affanhaq.mapcha.player.CaptchaPlayer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.Random;

import static me.affanhaq.mapcha.Mapcha.Config.*;

public class PlayerEvent implements Listener {

    private final Mapcha mapcha;

    public PlayerEvent(Mapcha mapcha) {
        this.mapcha = mapcha;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // player has permission to bypass the captcha
        if (player.hasPermission(permission) || player.isOp()) {
            Bukkit.getScheduler().scheduleSyncRepeatingTask(mapcha, () -> sendPlayerToServer(player), 10, 120);
            return;
        }

        // creating a captcha player
        CaptchaPlayer captchaPlayer = new CaptchaPlayer(player, genCaptcha(), mapcha).cleanPlayer();

        // making a map for the player
        String version = Bukkit.getVersion();
        ItemStack itemStack;
        if (version.contains("1.13") || version.contains("1.14") || version.contains("1.15") || version.contains("1.16")) {
            itemStack = new ItemStack(Material.valueOf("LEGACY_EMPTY_MAP"));
        } else {
            itemStack = new ItemStack(Material.valueOf("EMPTY_MAP"));
        }
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName("Mapcha");
        itemMeta.setLore(Collections.singletonList("Open the map to see the captcha."));
        itemStack.setItemMeta(itemMeta);

        // giving the player the map and adding them to the captcha array
        captchaPlayer.getPlayer().getInventory().setItemInHand(itemStack);
        mapcha.getPlayerManager().addPlayer(captchaPlayer);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onLeave(PlayerQuitEvent event) {

        CaptchaPlayer captchaPlayer = mapcha.getPlayerManager().getPlayer(event.getPlayer());

        if (captchaPlayer == null) {
            return;
        }

        // giving the player their items back
        captchaPlayer.resetInventory();
        mapcha.getPlayerManager().removePlayer(captchaPlayer);
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {

        // checking the the player is filling the captcha
        CaptchaPlayer player = mapcha.getPlayerManager().getPlayer(event.getPlayer());
        if (player != null) {

            // captcha success
            if (event.getMessage().equals(player.getCaptcha())) {
                player.getPlayer().sendMessage(captchaSuccessMessage);
                player.resetInventory();
                mapcha.getPlayerManager().removePlayer(player);
                event.getPlayer().sendMessage(ChatColor.GREEN + "Success! Joining main server...");
                Bukkit.getScheduler().runTask(mapcha, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "luckperms user " + player.getPlayer().getUniqueId() + " permission settemp mapcha.bypass true 24hr"));
                Bukkit.getScheduler().scheduleSyncRepeatingTask(mapcha, () -> sendPlayerToServer(player.getPlayer()), 10, 120);
            } else {
                if (player.getTries() >= (captchaTries - 1)) { // kicking the player because he's out of tries
                    Bukkit.getScheduler().runTask(mapcha, () -> player.getPlayer().kickPlayer(captchaFailMessage));
                } else { // telling the player to try again
                    player.setTries(player.getTries() + 1);
                    player.getPlayer().sendMessage(captchaRetryMessage.replace("{CURRENT}", String.valueOf(player.getTries())).replace("{MAX}", String.valueOf(captchaTries)));
                }
            }

            event.setCancelled(true);
        }
    }

    /**
     * @return a random string with len 4
     */
    private String genCaptcha() {
        String charset = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder random = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            random.append(charset.charAt(new Random().nextInt(charset.length() - 1)));
        }
        return random.toString();
    }

    /**
     * Sends a player to a connected server after the captcha is completed.
     *
     * @param player the player to send
     */
    private void sendPlayerToServer(Player player) {
        if (successServer != null && !successServer.isEmpty()) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("Connect");
            out.writeUTF(successServer);
            player.sendPluginMessage(mapcha, "BungeeCord", out.toByteArray());
        }
    }

}