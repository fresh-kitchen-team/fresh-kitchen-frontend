package com.freshkitchen.app.data.repository

import com.freshkitchen.app.network.ItemDto
import com.freshkitchen.app.ui.screens.chat.RecipeMatchedItemUi

/**
 * Resolves aiPayload [RecipeMatchedItemUi] rows to inventory [ItemDto] for consume.
 *
 * Same name in storage → earliest [ItemDto.expiryDate]; tie or all null → lowest [ItemDto.id] (first).
 */
object RecipeConsumeResolver {

    fun enrichMatchedItems(
        apiItems: List<RecipeMatchedItemUi>,
        inventory: List<ItemDto>,
    ): List<RecipeMatchedItemUi> =
        apiItems.map { item ->
            val picked = pickItemByName(item.name, inventory)
            if (picked == null) {
                item.copy(isAvailable = false)
            } else {
                item.copy(
                    itemId = picked.id,
                    name = item.name.trim().ifBlank { picked.name.trim() },
                    storageLabel = picked.storage.toKoreanStorageLabel(),
                    emoji = picked.emoji?.trim()?.takeIf { it.isNotEmpty() },
                    representativeImage = picked.representativeImage,
                    isAvailable = true,
                )
            }
        }

    /**
     * For each selected row, pick a distinct inventory item by name (expiry rule).
     * Already-picked ids are excluded so duplicate names consume different stock rows.
     */
    fun resolveConsumeIdsForRows(
        selectedRows: List<RecipeMatchedItemUi>,
        inventory: List<ItemDto>,
    ): List<Long> {
        val consumedIds = mutableSetOf<Long>()
        val result = mutableListOf<Long>()
        for (row in selectedRows) {
            val pool = inventory.filter { it.id !in consumedIds }
            val picked = pickItemByName(row.name, pool) ?: continue
            consumedIds.add(picked.id)
            result.add(picked.id)
        }
        return result
    }

    fun pickItemByName(name: String, inventory: List<ItemDto>): ItemDto? {
        val target = name.trim()
        if (target.isEmpty()) return null

        val matches = inventory.filter { it.name.trim() == target }
        if (matches.isEmpty()) return null

        val withExpiry = matches.filter { !it.expiryDate.isNullOrBlank() }
        if (withExpiry.isNotEmpty()) {
            val earliestDate = withExpiry.minOf { it.expiryDate!! }
            val earliestCandidates = withExpiry.filter { it.expiryDate == earliestDate }
            return earliestCandidates.minByOrNull { it.id } ?: earliestCandidates.first()
        }

        return matches.minByOrNull { it.id } ?: matches.first()
    }

    private fun String.toKoreanStorageLabel(): String = when (trim().uppercase()) {
        "FRIDGE" -> "냉장실"
        "FREEZER" -> "냉동실"
        "PANTRY" -> "팬트리"
        else -> this
    }
}
