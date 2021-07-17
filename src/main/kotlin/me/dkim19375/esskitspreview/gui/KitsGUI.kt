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

package me.dkim19375.esskitspreview.gui

import com.earth2me.essentials.I18n
import com.earth2me.essentials.Kit
import com.earth2me.essentials.MetaItemStack
import com.earth2me.essentials.textreader.IText
import com.earth2me.essentials.textreader.KeywordReplacer
import com.earth2me.essentials.textreader.SimpleTextInput
import dev.triumphteam.gui.builder.item.ItemBuilder
import dev.triumphteam.gui.guis.Gui
import dev.triumphteam.gui.guis.GuiItem
import me.dkim19375.dkimbukkitcore.function.color
import me.dkim19375.dkimbukkitcore.function.formatAll
import me.dkim19375.esskitspreview.ESSKitsPreview
import me.dkim19375.esskitspreview.util.toComponent
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.util.logging.Level

class KitsGUI(private val player: Player, private val kit: Kit, private val plugin: ESSKitsPreview) {
    private val rows = plugin.config.getInt("gui.rows", 6)
    private val title = (plugin.config.getString("gui.title") ?: "&8Previewing Kit &8&l%kit%&8..")
        .replace("%kit%", kit.name)
        .let {
            if (plugin.server.pluginManager.isPluginEnabled("PlaceholderAPI")) {
                return@let it.formatAll(player)
            }
            return@let it.color()
        }
    private val menu = Gui.gui()
        .rows(rows)
        .title(title.toComponent())
        .disableAllInteractions()
        .create()

    private val ess = plugin.essentials

    private fun reset() {
        for (i in 0 until rows * 9) {
            menu.removeItem(i)
        }
    }

    fun showPlayer() {
        setup()
        menu.open(player)
    }

    private fun setup() {
        reset()
        addItems()
        kit.items
    }

    private fun addItems() {
        for (item in getKitItems()) {
            menu.addItem(GuiItem(item))
        }
    }

    private fun getKitItems(): List<ItemStack> {
        val user = ess.getUser(player)
        val items = kit.items
        val itemList = mutableListOf<ItemStack>()
        try {
            val input: IText = SimpleTextInput(items)
            val output: IText = KeywordReplacer(input, user.source, ess, true, true)
            val allowUnsafe: Boolean = ess.settings.allowUnsafeEnchantments()
            val currencySymbol = ess.settings.currencySymbol.ifEmpty { "$" }
            for (kitItem in output.lines) {
                if (kitItem.startsWith("$") || kitItem.startsWith(currencySymbol) || kitItem.startsWith("/")) {
                    continue
                }
                if (kitItem.startsWith("@")) {
                    if (ess.serializationProvider == null) {
                        ess.logger.log(Level.WARNING, I18n.tl("kitError3", kit.name, user.name))
                        continue
                    }
                    itemList.add(
                        ess.serializationProvider.deserializeItem(Base64Coder.decodeLines(kitItem.substring(1)))
                    )
                    continue
                }
                val parts = kitItem.split(" +".toRegex()).toTypedArray()
                val parseStack: ItemStack = ess.itemDb.get(parts[0], if (parts.size > 1) parts[1].toInt() else 1)
                if (parseStack.type == Material.AIR) {
                    continue
                }
                val metaStack = MetaItemStack(parseStack)
                if (parts.size > 2) {
                    metaStack.parseStringMeta(null, allowUnsafe, parts, 2, ess)
                }
                itemList.add(metaStack.itemStack)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            return itemList.plus(getErrorItem(e))
        }
        return itemList
    }

    private fun getErrorItem(e: Exception): ItemStack {
        return ItemBuilder.from(Material.BARRIER)
            .flags(*ItemFlag.values())
            .name("Error! Please report this to a server administrator".toComponent())
            .lore(" ".toComponent(), "Error: ${e.localizedMessage}".toComponent())
            .build()
    }
}