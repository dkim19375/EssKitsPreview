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

package me.dkim19375.esskitspreview.util

import me.dkim19375.dkimbukkitcore.data.HelpMessage
import me.dkim19375.dkimbukkitcore.data.HelpMessageFormat
import me.dkim19375.dkimbukkitcore.function.color
import me.dkim19375.dkimbukkitcore.function.getMaxHelpPages
import me.dkim19375.dkimbukkitcore.function.showHelpMessage
import me.dkim19375.esskitspreview.ESSKitsPreview
import me.dkim19375.esskitspreview.enumclass.ErrorType
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

val commands = listOf(
    HelpMessage("help", "Shows help menu", "esskitspreview.command"),
    HelpMessage("reload", "Reload the config files", "esskitspreview.reload"),
    HelpMessage("<kit name>", "Display a kit", "esskitspreview.preview")
)

private val plugin by lazy { JavaPlugin.getPlugin(ESSKitsPreview::class.java) }
private val format = HelpMessageFormat(
    topBar = "&1----------------------------------------------".color(),
    header = "&a%name% v%version".color(),
    bottomBar = "&1----------------------------------------------".color()
)

fun CommandSender.sendHelpMessage(label: String, error: ErrorType? = null, page: Int = 1) {
    showHelpMessage(label, error?.description, page, commands, plugin, format)
}

fun CommandSender.sendMessage(message: ErrorType) = sendMessage("${ChatColor.RED}${message.description}")