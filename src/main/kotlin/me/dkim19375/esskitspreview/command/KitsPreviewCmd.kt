/*
 * MIT License
 *
 * Copyright (c) 2021 dkim19375
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.dkim19375.esskitspreview.command

import com.earth2me.essentials.Kit
import me.dkim19375.dkimcore.extension.runCatchingOrNull
import me.dkim19375.esskitspreview.ESSKitsPreview
import me.dkim19375.esskitspreview.enumclass.ErrorType
import me.dkim19375.esskitspreview.gui.KitsGUI
import me.dkim19375.esskitspreview.util.sendHelpMessage
import me.dkim19375.esskitspreview.util.sendMessage
import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class KitsPreviewCmd(private val plugin: ESSKitsPreview) : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (!sender.hasPermission("esskitspreview.command")) {
            sender.sendMessage(ErrorType.NO_PERMISSION)
            return true
        }
        if (args.isEmpty()) {
            sender.sendHelpMessage(label)
            return true
        }
        when (args[0].lowercase()) {
            "help" -> {
                sender.sendHelpMessage(label, null, args.getOrNull(1)?.toIntOrNull() ?: 1)
                return true
            }
            "reload" -> {
                if (!sender.hasPermission("esskitspreview.reload")) {
                    sender.sendMessage(ErrorType.NO_PERMISSION)
                    return true
                }
                plugin.reloadConfig()
                sender.sendMessage("${ChatColor.GREEN}Successfully reloaded the config file!")
                return true
            }
            else -> {
                if (!sender.hasPermission("esskitspreview.preview")) {
                    sender.sendMessage(ErrorType.NO_PERMISSION)
                    return true
                }
                if (sender !is Player) {
                    sender.sendMessage(ErrorType.MUST_BE_PLAYER)
                    return true
                }
                val kit = getKit(args[0])
                if (kit == null) {
                    sender.sendMessage(ErrorType.UNKNOWN_KIT)
                    return true
                }
                if (!sender.hasPermission("esskitspreview.preview.${kit.name.lowercase()}")) {
                    sender.sendMessage(ErrorType.NO_PERMISSION)
                    return true
                }
                KitsGUI(sender, kit, plugin).showPlayer()
                return true
            }
        }
    }

    private fun getKit(name: String): Kit? = runCatchingOrNull { Kit(plugin.essentials.kits.matchKit(name), plugin.essentials) }
}