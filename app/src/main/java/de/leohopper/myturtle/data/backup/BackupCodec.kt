package de.leohopper.myturtle.data.backup

import kotlinx.serialization.json.Json

object BackupCodec {

    private val json = Json {
        encodeDefaults = true
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    fun encode(bundle: BackupBundle): String {
        return json.encodeToString(BackupBundle.serializer(), bundle)
    }

    fun decode(jsonString: String): BackupBundle {
        return json.decodeFromString(BackupBundle.serializer(), jsonString)
    }
}
