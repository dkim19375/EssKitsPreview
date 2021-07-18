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

package me.dkim19375.esskitspreview

import com.earth2me.essentials.Essentials
import me.dkim19375.dkimbukkitcore.function.logInfo
import me.dkim19375.dkimbukkitcore.javaplugin.CoreJavaPlugin
import me.dkim19375.dkimcore.extension.containsIgnoreCase
import me.dkim19375.esskitspreview.command.KitsPreviewCmd
import me.dkim19375.esskitspreview.command.KitsPreviewTab
import me.dkim19375.esskitspreview.util.getIntOrNull
import me.dkim19375.esskitspreview.util.getStringListOrNull
import org.bukkit.Material
import java.util.logging.Level

@Suppress("MemberVisibilityCanBePrivate")
class ESSKitsPreview : CoreJavaPlugin() {
    val essentials by lazy { server.pluginManager.getPlugin("Essentials") as Essentials }

    override fun onEnable() {
        reloadConfig()
        registerCommands()
    }

    private fun registerCommands() {
        registerCommand("esskitspreview", KitsPreviewCmd(this), KitsPreviewTab(this))
    }

    override fun reloadConfig() {
        super.reloadConfig()
        config.getIntOrNull("close.slot")?.apply { checkNull("slot", "close.slot") }
        config.getString("close.type")?.let(Material::matchMaterial)?.apply { checkNull("item type", "close.type") }
        config.getString("close.name")?.apply { checkNull("item name", "close.name") }
        config.getStringListOrNull("close.lore")?.apply { checkNull("item lore", "close.lore") }
        config.getIntOrNull("back.slot")?.apply { checkNull("slot", "back.slot") }
        config.getString("back.type")?.let(Material::matchMaterial)?.apply { checkNull("item type", "back.type") }
        config.getString("back.name")?.apply { checkNull("item name", "back.name") }
        config.getStringListOrNull("back.lore")?.apply { checkNull("item lore", "back.lore") }
        config.getString("back.command")?.apply { checkNull("command", "back.command") }
    }

    private fun <T : Any> T?.checkNull(type: String, path: String): T? {
        this?.let { return@checkNull this }
        val warning = listOf("command", "item name", "item lore").containsIgnoreCase(type)
        logInfo(
            text = "Invalid/missing $type in config.yml! ($path)",
            level = if (warning) Level.WARNING else Level.SEVERE
        )
        return this
    }
}