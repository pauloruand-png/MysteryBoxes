package com.mysterybox.command;

import com.mysterybox.MysteryBoxesPlugin;
import com.mysterybox.animation.RouletteAnimation;
import com.mysterybox.gui.PreviewGUI;
import com.mysterybox.model.Crate;
import com.mysterybox.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class CrateCommand implements CommandExecutor, TabCompleter {

    private final MysteryBoxesPlugin plugin;

    public CrateCommand(MysteryBoxesPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("ajuda")) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "givekey":
            case "dar":
                handleGiveKey(sender, args);
                break;

            case "setcrate":
            case "setar":
                handleSetCrate(sender, args);
                break;

            case "removecrate":
            case "remover":
                handleRemoveCrate(sender);
                break;

            case "preview":
            case "ver":
                handlePreview(sender, args);
                break;

            case "open":
            case "abrir":
                handleOpen(sender, args);
                break;

            case "list":
            case "listar":
                handleList(sender);
                break;

            case "reload":
            case "recarregar":
                handleReload(sender);
                break;

            default:
                sender.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cSubcomando desconhecido. Use &e/" + label + " help&c."));
                break;
        }

        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage(ColorUtil.color("&6&l====== &eSistema de Caixas Misteriosas &6&l======"));
        sender.sendMessage(ColorUtil.color("&e/" + label + " preview <caixa> &7- Ver os itens e chances da caixa"));
        sender.sendMessage(ColorUtil.color("&e/" + label + " open <caixa> &7- Abrir uma caixa virtualmente com sua chave"));
        sender.sendMessage(ColorUtil.color("&e/" + label + " list &7- Ver lista de caixas disponíveis"));

        if (sender.hasPermission("mysteryboxes.admin")) {
            sender.sendMessage(ColorUtil.color("&6--- Comandos Administrativos ---"));
            sender.sendMessage(ColorUtil.color("&e/" + label + " givekey <jogador> <caixa> [qtd] &7- Entregar chaves"));
            sender.sendMessage(ColorUtil.color("&e/" + label + " setcrate <caixa> &7- Define o bloco que você olha como caixa"));
            sender.sendMessage(ColorUtil.color("&e/" + label + " removecrate &7- Remove a caixa do bloco"));
            sender.sendMessage(ColorUtil.color("&e/" + label + " reload &7- Recarregar arquivos de configuração"));
        }
    }

    private void handleGiveKey(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mysteryboxes.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cUso correto: /caixa givekey <jogador> <caixa> [quantidade]"));
            return;
        }

        Player target = Bukkit.getPlayer(args[1]);
        if (target == null || !target.isOnline()) {
            String msg = plugin.getConfig().getString("messages.player-not-found", "&cJogador não encontrado.")
                    .replace("%player%", args[1]);
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + msg));
            return;
        }

        String crateId = args[2].toLowerCase();
        Crate crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            String msg = plugin.getConfig().getString("messages.invalid-crate", "&cCaixa não encontrada.")
                    .replace("%crate%", args[2]);
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + msg));
            return;
        }

        int amount = 1;
        if (args.length >= 4) {
            try {
                amount = Integer.parseInt(args[3]);
                if (amount <= 0) amount = 1;
            } catch (NumberFormatException e) {
                sender.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.invalid-amount")));
                return;
            }
        }

        ItemStack keyItem = plugin.getKeyManager().createKey(crateId, amount);
        if (keyItem == null) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cEsta caixa não possui uma chave configurada."));
            return;
        }

        HashMap<Integer, ItemStack> leftover = target.getInventory().addItem(keyItem);
        if (!leftover.isEmpty()) {
            for (ItemStack drop : leftover.values()) {
                target.getWorld().dropItemNaturally(target.getLocation(), drop);
            }
        }

        String keyName = crate.getKey() != null ? crate.getKey().getName() : "&eChave";

        String senderMsg = plugin.getConfig().getString("messages.key-given", "&aChave entregue!")
                .replace("%amount%", String.valueOf(amount))
                .replace("%key%", keyName)
                .replace("%player%", target.getName());
        sender.sendMessage(ColorUtil.color(plugin.getPrefix() + senderMsg));

        String receiverMsg = plugin.getConfig().getString("messages.key-received", "&aVocê recebeu chaves!")
                .replace("%amount%", String.valueOf(amount))
                .replace("%key%", keyName);
        target.sendMessage(ColorUtil.color(plugin.getPrefix() + receiverMsg));
    }

    private void handleSetCrate(CommandSender sender, String[] args) {
        if (!sender.hasPermission("mysteryboxes.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cComando exclusivo para jogadores dentro do servidor."));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cUso correto: /caixa setcrate <id_da_caixa>"));
            return;
        }

        String crateId = args[1].toLowerCase();
        Crate crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cCaixa misteriosa '" + args[1] + "' não existe!"));
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.getType().isAir()) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.must-look-at-block")));
            return;
        }

        plugin.getLocationManager().setCrate(targetBlock.getLocation(), crateId);
        String msg = plugin.getConfig().getString("messages.crate-set", "&aBloco definido com sucesso como %crate%!")
                .replace("%crate%", crate.getName());
        player.sendMessage(ColorUtil.color(plugin.getPrefix() + msg));
    }

    private void handleRemoveCrate(CommandSender sender) {
        if (!sender.hasPermission("mysteryboxes.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cComando exclusivo para jogadores."));
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || !plugin.getLocationManager().isCrate(targetBlock.getLocation())) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.not-a-crate")));
            return;
        }

        plugin.getLocationManager().removeCrate(targetBlock.getLocation());
        player.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.crate-removed")));
    }

    private void handlePreview(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cComando exclusivo para jogadores."));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cUso correto: /caixa preview <caixa>"));
            return;
        }

        String crateId = args[1].toLowerCase();
        Crate crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cCaixa não encontrada."));
            return;
        }

        new PreviewGUI(crate).open(player);
    }

    private void handleOpen(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cComando exclusivo para jogadores."));
            return;
        }

        if (args.length < 2) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cUso correto: /caixa open <caixa>"));
            return;
        }

        String crateId = args[1].toLowerCase();
        Crate crate = plugin.getCrateManager().getCrate(crateId);
        if (crate == null) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + "&cCaixa não encontrada."));
            return;
        }

        if (RouletteAnimation.isOpening(player.getUniqueId())) {
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.already-opening")));
            return;
        }

        if (!plugin.getKeyManager().hasKey(player, crateId)) {
            String keyName = crate.getKey() != null ? crate.getKey().getName() : "&eChave";
            String needKeyMsg = plugin.getConfig().getString("messages.need-key", "&cVocê precisa de uma %key%!")
                    .replace("%key%", keyName);
            player.sendMessage(ColorUtil.color(plugin.getPrefix() + needKeyMsg));
            return;
        }

        plugin.getKeyManager().consumeKey(player, crateId);
        new RouletteAnimation(plugin, player, crate).start();
    }

    private void handleList(CommandSender sender) {
        sender.sendMessage(ColorUtil.color("&6&l====== &eCaixas Misteriosas Disponíveis &6&l======"));
        for (Crate crate : plugin.getCrateManager().getCrates()) {
            sender.sendMessage(ColorUtil.color(" &7• &fID: &e" + crate.getId() + " &7- Nome: " + crate.getName() + " &7(" + crate.getRewards().size() + " prêmios)"));
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("mysteryboxes.admin")) {
            sender.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.no-permission")));
            return;
        }

        plugin.reloadConfig();
        plugin.getCrateManager().reload();
        plugin.getLocationManager().load();
        plugin.getLocationManager().startParticleTask();

        sender.sendMessage(ColorUtil.color(plugin.getPrefix() + plugin.getConfig().getString("messages.reload-success")));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> subs = new ArrayList<>(Arrays.asList("preview", "open", "list", "help"));
            if (sender.hasPermission("mysteryboxes.admin")) {
                subs.addAll(Arrays.asList("givekey", "setcrate", "removecrate", "reload"));
            }
            for (String s : subs) {
                if (s.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(s);
                }
            }
            return completions;
        }

        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("givekey")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(p.getName());
                    }
                }
                return completions;
            }
            if (sub.equals("preview") || sub.equals("open") || sub.equals("setcrate")) {
                for (String id : plugin.getCrateManager().getCrateIds()) {
                    if (id.toLowerCase().startsWith(args[1].toLowerCase())) {
                        completions.add(id);
                    }
                }
                return completions;
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("givekey")) {
            for (String id : plugin.getCrateManager().getCrateIds()) {
                if (id.toLowerCase().startsWith(args[2].toLowerCase())) {
                    completions.add(id);
                }
            }
            return completions;
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("givekey")) {
            return Arrays.asList("1", "5", "10", "32", "64");
        }

        return completions;
    }
}
