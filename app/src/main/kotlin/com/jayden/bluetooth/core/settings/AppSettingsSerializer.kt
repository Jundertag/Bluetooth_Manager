package com.jayden.bluetooth.core.settings

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.jayden.bluetooth.datastore.proto.AppSettings
import java.io.InputStream
import java.io.OutputStream

object AppSettingsSerializer : Serializer<AppSettings> {
    override val defaultValue: AppSettings = AppSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppSettings {
        try {
            return AppSettings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("cannot read proto.", e)
        }
    }

    override suspend fun writeTo(t: AppSettings, output: OutputStream) {
        return t.writeTo(output)
    }
}