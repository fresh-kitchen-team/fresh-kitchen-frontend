package com.example.myfrigelocal.data.scan

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import java.lang.reflect.Type

/** `detectedItems` 등 실서버 키 변형 대응 */
class FridgeImageScanApiResponseDeserializer : JsonDeserializer<FridgeImageScanApiResponse> {

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext,
    ): FridgeImageScanApiResponse {
        if (!json.isJsonObject) {
            throw JsonParseException("Fridge scan envelope must be a JSON object")
        }
        val root = json.asJsonObject
        val status = root.get("status")?.takeIf { !it.isJsonNull }?.asInt ?: 0
        val code = root.get("code")?.takeIf { !it.isJsonNull }?.asString
        val message = root.get("message")?.takeIf { !it.isJsonNull }?.asString
        val dataEl = root.get("data")
        val data =
            when {
                dataEl == null || dataEl.isJsonNull -> null
                dataEl.isJsonObject -> parseFridgeData(dataEl.asJsonObject)
                else -> null
            }
        return FridgeImageScanApiResponse(
            status = status,
            code = code,
            message = message,
            data = data,
        )
    }

    private fun parseFridgeData(obj: JsonObject): FridgeImageScanData {
        val detected =
            extractDetectedItems(obj)
                ?: deepFindDetectedArray(obj)?.let { mapJsonArrayToItems(it) }
                ?: emptyList()
        return FridgeImageScanData(
            scanType = obj.stringOrNull("scanType"),
            imageAsset = parseImageAsset(obj.get("imageAsset")),
            detectedItems = detected,
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

    private fun extractDetectedItems(obj: JsonObject): List<FridgeDetectedItem>? {
        val keys =
            listOf(
                "detectedItems",
                "detected_items",
                "DetectedItems",
                "recognizedItems",
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

    private fun deepFindDetectedArray(obj: JsonObject): JsonArray? {
        for (key in obj.keySet()) {
            val value = obj.get(key) ?: continue
            when {
                value.isJsonArray -> {
                    val arr = value.asJsonArray
                    if (arr.size() > 0 && arr[0].isJsonObject && looksLikeDetectedRow(arr[0].asJsonObject)) {
                        return arr
                    }
                }
                value.isJsonObject -> {
                    deepFindDetectedArray(value.asJsonObject)?.let { return it }
                }
            }
        }
        return null
    }

    private fun looksLikeDetectedRow(o: JsonObject): Boolean {
        for (k in NAME_KEYS) {
            val p = o.get(k) ?: continue
            if (p.isJsonPrimitive && p.asString.trim().isNotEmpty()) return true
        }
        return false
    }

    private fun mapJsonArrayToItems(arr: JsonArray): List<FridgeDetectedItem> {
        val out = ArrayList<FridgeDetectedItem>(arr.size())
        for (el in arr) {
            if (!el.isJsonObject) continue
            val row = el.asJsonObject
            val name =
                NAME_KEYS.firstNotNullOfOrNull { key ->
                    row.stringOrNull(key)?.trim()?.takeIf { it.isNotEmpty() }
                } ?: continue
            out.add(
                FridgeDetectedItem(
                    name = name,
                    category = row.stringOrNull("category"),
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
