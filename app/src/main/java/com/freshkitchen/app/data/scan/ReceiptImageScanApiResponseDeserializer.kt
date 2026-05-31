package com.freshkitchen.app.data.scan

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/**
 * 실서버 `data` 안에 인식 목록이 `recognizedItems` 외 키/중첩으로 올 수 있어
 * 기본 Gson 데이터 클래스 매핑만으로는 빈 배열로 떨어지는 경우가 있음.
 */
class ReceiptImageScanApiResponseDeserializer : JsonDeserializer<ReceiptImageScanApiResponse> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): ReceiptImageScanApiResponse {
        if (!json.isJsonObject) {
            throw JsonParseException("Receipt scan envelope must be a JSON object")
        }
        val root = json.asJsonObject
        val status = root.get("status")?.takeIf { !it.isJsonNull }?.asInt ?: 0
        val code = root.get("code")?.takeIf { !it.isJsonNull }?.asString
        val message = root.get("message")?.takeIf { !it.isJsonNull }?.asString
        val dataEl = root.get("data")
        val data =
            when {
                dataEl == null || dataEl.isJsonNull -> null
                dataEl.isJsonObject -> parseReceiptData(dataEl.asJsonObject)
                else -> null
            }
        return ReceiptImageScanApiResponse(
            status = status,
            code = code,
            message = message,
            data = data,
        )
    }

    private fun parseReceiptData(obj: JsonObject): ReceiptImageScanData {
        val recognized =
            extractRecognizedItems(obj)
                ?: deepFindRecognizedArray(obj)?.let { mapJsonArrayToItems(it) }
                ?: emptyList()
        return ReceiptImageScanData(
            scanType = obj.stringOrNull("scanType"),
            imageAsset = parseImageAsset(obj.get("imageAsset")),
            purchasedAt = obj.stringOrNull("purchasedAt"),
            purchasedAtSourceType =
                obj.stringOrNull("purchasedAtSourceType")
                    ?: obj.stringOrNull("PurchasedAtSourceType"),
            recognizedItems = recognized,
            createdAt = obj.stringOrNull("createdAt"),
        )
    }

    private fun parseImageAsset(el: JsonElement?): ScanImageAsset? {
        if (el == null || el.isJsonNull || !el.isJsonObject) return null
        val o = el.asJsonObject
        return ScanImageAsset(
            imageAssetId = o.longOrNull("imageAssetId"),
            kind = o.stringOrNull("kind"),
            storageProvider = o.stringOrNull("storageProvider"),
            imageUrl = o.stringOrNull("imageUrl"),
        )
    }

    private fun extractRecognizedItems(obj: JsonObject): List<ReceiptRecognizedItem>? {
        val keys =
            listOf(
                "recognizedItems",
                "recognized_items",
                "RecognizedItems",
                "items",
            )
        for (k in keys) {
            val el = obj.get(k) ?: continue
            if (!el.isJsonArray) continue
            val mapped = mapJsonArrayToItems(el.asJsonArray)
            if (mapped.isNotEmpty()) return mapped
        }
        return null
    }

    private fun deepFindRecognizedArray(obj: JsonObject): JsonArray? {
        for (key in obj.keySet()) {
            val value = obj.get(key) ?: continue
            when {
                value.isJsonArray -> {
                    val arr = value.asJsonArray
                    if (arr.size() > 0 && arr[0].isJsonObject && looksLikeRecognizedRow(arr[0].asJsonObject)) {
                        return arr
                    }
                }
                value.isJsonObject -> {
                    deepFindRecognizedArray(value.asJsonObject)?.let { return it }
                }
            }
        }
        return null
    }

    private fun looksLikeRecognizedRow(o: JsonObject): Boolean {
        for (k in NAME_KEYS) {
            val p = o.get(k) ?: continue
            if (p.isJsonPrimitive && p.asString.trim().isNotEmpty()) return true
        }
        return false
    }

    private fun mapJsonArrayToItems(arr: JsonArray): List<ReceiptRecognizedItem> {
        val out = ArrayList<ReceiptRecognizedItem>(arr.size())
        for (el in arr) {
            if (!el.isJsonObject) continue
            val row = el.asJsonObject
            val name =
                NAME_KEYS.firstNotNullOfOrNull { key ->
                    row.stringOrNull(key)?.trim()?.takeIf { it.isNotEmpty() }
                } ?: continue
            out.add(
                ReceiptRecognizedItem(
                    name = name,
                    category = row.stringOrNull("category"),
                    registeredAt = row.stringOrNull("registeredAt") ?: row.stringOrNull("registered_at"),
                ),
            )
        }
        return out
    }

    private companion object {
        val NAME_KEYS =
            listOf(
                "name",
                "catalogName",
                "ingredientName",
                "productName",
                "title",
                "itemName",
                "ingredient_name",
            )
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive }?.asString

    private fun JsonObject.longOrNull(key: String): Long? =
        get(key)?.takeIf { !it.isJsonNull && it.isJsonPrimitive && it.asJsonPrimitive.isNumber }?.asLong
}
