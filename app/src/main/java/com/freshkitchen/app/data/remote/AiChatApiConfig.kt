package com.freshkitchen.app.data.remote

/**
 * AI chat endpoints that are not yet fixed in Swagger.
 *
 * Set [AI_SETTING_UPDATE_PATH] when the backend publishes the setting API path
 * (relative to `http://api.app-fresh.com/`, e.g. `ai/v1/chat/setting`).
 */
object AiChatApiConfig {
    /** Empty until the AI setting API URL is finalized. */
    const val AI_SETTING_UPDATE_PATH: String = ""
}
