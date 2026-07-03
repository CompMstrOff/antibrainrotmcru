package com.antibrainrot;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;

public class ChatListener implements Listener {

    private final AntiBrainrotPlugin plugin;

    public ChatListener(AntiBrainrotPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        // Получаем сообщение как обычный текст (без цветов/форматирования)
        String plainMessage = PlainTextComponentSerializer.plainText().serialize(event.message());
        String normalized = normalize(plainMessage);

        List<String> blockedWords = plugin.getConfig().getStringList("blocked-words");

        for (String rawWord : blockedWords) {
            String word = normalize(rawWord);
            if (word.isEmpty()) {
                continue;
            }

            if (normalized.contains(word)) {
                // Удаляем сообщение из чата (не даём ему отобразиться)
                event.setCancelled(true);

                Player player = event.getPlayer();
                handleViolation(player, rawWord);
                return; // одного совпадения достаточно
            }
        }
    }

    /**
     * Приводим строку к нижнему регистру и убираем "мусорные" разделители
     * (пробелы, точки, дефисы, подчёркивания), чтобы ловить обходы вида
     * "с и к с   с е в е н" или "6-7".
     */
    private String normalize(String input) {
        String lower = input.toLowerCase();
        return lower.replaceAll("[\\s._\\-]+", "");
    }

    private void handleViolation(Player player, String matchedWord) {
        String template = plugin.getConfig().getString(
                "broadcast-message",
                "[Anti-Brainrot\uD83D\uDC80] Лол! Игрок {player} страдает брэинротом."
        );
        String finalText = template.replace("{player}", player.getName());

        Component messageComponent = Component.text(finalText)
                .color(NamedTextColor.RED)
                .decorate(TextDecoration.BOLD);

        boolean broadcastToAll = plugin.getConfig().getBoolean("broadcast-to-all", true);
        boolean logToConsole = plugin.getConfig().getBoolean("log-to-console", true);

        // Bukkit API безопаснее вызывать в основном потоке,
        // а чат-эвент в Paper асинхронный.
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (broadcastToAll) {
                Bukkit.broadcast(messageComponent);
            } else {
                player.sendMessage(messageComponent);
            }

            if (logToConsole) {
                plugin.getLogger().info("Игрок " + player.getName()
                        + " заблокирован за использование запрещённого слова: " + matchedWord);
            }
        });
    }
}
