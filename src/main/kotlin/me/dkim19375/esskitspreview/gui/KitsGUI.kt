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
import me.dkim19375.esskitspreview.ESSKitsPreview
import me.dkim19375.esskitspreview.util.formatStr
import me.dkim19375.esskitspreview.util.getIntOrNull
import me.dkim19375.esskitspreview.util.getStringListOrNull
import me.dkim19375.esskitspreview.util.toComponent
import org.bukkit.Bukkit
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
        .formatStr()
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
        val config = plugin.config
        val closeSlot = config.getIntOrNull("close.slot")
        val closeType = config.getString("close.type")?.let(Material::matchMaterial)
        val closeName = config.getString("close.name")?.formatStr()
        val closeLore = config.getStringListOrNull("close.lore")?.map(String::formatStr)
        val backSlot = config.getIntOrNull("back.slot")
        val backType = config.getString("back.type")?.let(Material::matchMaterial)
        val backName = config.getString("back.name")?.formatStr()
        val backLore = config.getStringListOrNull("back.lore")?.map(String::formatStr)
        val command = config.getString("back.command")?.formatStr()
        val kitItems = getKitItems().listIterator()
        for (i in 0 until rows * 9) {
            if (closeType != null && closeSlot == i) {
                menu.setItem(i, GuiItem(getSpecialItem(closeType, closeName, closeLore)) {
                    it.isCancelled = true
                    menu.close(player)
                })
                continue
            }
            if (backType != null && backSlot == i) {
                menu.setItem(i, GuiItem(getSpecialItem(backType, backName, backLore)) {
                    it.isCancelled = true
                    command?.let { Bukkit.dispatchCommand(player, it) }
                })
                continue
            }
            if (!kitItems.hasNext()) {
                continue
            }
            menu.setItem(i, GuiItem(kitItems.next()) {
                it.isCancelled = true
            })
        }
    }

    private fun getSpecialItem(type: Material, name: String?, lore: List<String>?): ItemStack =
        ItemBuilder.from(type).apply {
            if (name != null) {
                name(name.toComponent())
            }
            if (lore != null) {
                lore(lore.map(String::toComponent))
            }
        }.build()

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