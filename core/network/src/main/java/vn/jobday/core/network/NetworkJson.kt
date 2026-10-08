package vn.jobday.core.network

import kotlinx.serialization.json.Json

internal val networkJson = Json {
    ignoreUnknownKeys = true
}