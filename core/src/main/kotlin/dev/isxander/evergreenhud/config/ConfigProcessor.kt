/*
 | EvergreenHUD - A mod to improve on your heads-up-display.
 | Copyright (C) isXander [2019 - 2021]
 |
 | This program comes with ABSOLUTELY NO WARRANTY
 | This is free software, and you are welcome to redistribute it
 | under the certain conditions that can be found here
 | https://www.gnu.org/licenses/lgpl-3.0.en.html
 |
 | If you have any questions or concerns, please create
 | an issue on the github page that can be found here
 | https://github.com/isXander/EvergreenHUD
 |
 | If you have a private concern, please contact
 | isXander @ business.isxander@gmail.com
 */

package dev.isxander.evergreenhud.config

import com.uchuhimo.konf.Config
import dev.isxander.evergreenhud.compatibility.universal.LOGGER
import dev.isxander.evergreenhud.settings.DataType
import dev.isxander.evergreenhud.settings.Setting
import dev.isxander.evergreenhud.settings.SettingAdapter
import dev.isxander.evergreenhud.settings.impl.*
import dev.isxander.evergreenhud.settings.providers.AdapterProvider
import dev.isxander.evergreenhud.settings.providers.PropertyProvider
import dev.isxander.evergreenhud.utils.*
import java.awt.Color
import java.lang.ClassCastException
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

interface ConfigProcessor {

    var conf: Config

    fun addSettingToConfig(setting: Setting<*, *>, data: Config): Config {
        var value: Config = data[setting.category]
        if (setting.subcategory != "") value = value[setting.subcategory]

        value[setting.nameSerializedKey] = when (setting.dataType) {
            DataType.STRING -> setting.serializedValue as String
            DataType.BOOLEAN -> setting.serializedValue as Boolean
            DataType.FLOAT -> setting.serializedValue as Float
            DataType.INT -> setting.serializedValue as Int
        }

        return data
    }

    fun setSettingFromConfig(data: Config, setting: Setting<*, *>) {
        when (setting.dataType) {
            DataType.BOOLEAN -> setting.serializedValue = data.getOrNull(setting.nameSerializedKey) ?: setting.defaultSerializedValue as Boolean
            DataType.FLOAT -> setting.serializedValue = data.getOrNull(setting.nameSerializedKey) ?: setting.defaultSerializedValue as Float
            DataType.INT -> setting.serializedValue = data.getOrNull(setting.nameSerializedKey) ?: setting.defaultSerializedValue as Int
            DataType.STRING -> setting.serializedValue = data.getOrNull(setting.nameSerializedKey) ?: setting.defaultSerializedValue as String
        }
    }

    fun collectSettings(instance: Any, settingProcessor: (Setting<*, *>) -> Unit) {
        val classes = arrayListOf(instance::class)
        classes.addAll(instance::class.allSuperclasses)
        for (declaredClass in classes) {
            for (property in declaredClass.declaredMemberProperties) {
                property.isAccessible = true

                @Suppress("UNCHECKED_CAST")
                try {
                    when {
                        property.hasAnnotation<BooleanSetting>() ->
                            settingProcessor.invoke(BooleanSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Boolean>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Boolean>
                                    )
                            ))
                        property.hasAnnotation<ColorSetting>() ->
                            settingProcessor.invoke(ColorSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Color>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Color>
                                    )
                            ))
                        property.hasAnnotation<OptionSetting>() ->
                            settingProcessor.invoke(OptionSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<OptionContainer.Option>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, OptionContainer.Option>
                                    )
                            ))
                        property.hasAnnotation<FloatSetting>() ->
                            settingProcessor.invoke(FloatSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Float>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Float>
                                    )
                            ))
                        property.hasAnnotation<IntSetting>() ->
                            settingProcessor.invoke(IntSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<Int>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, Int>
                                    )
                            ))
                        property.hasAnnotation<StringListSetting>() ->
                            settingProcessor.invoke(StringListSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<String>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, String>
                                    )
                            ))
                        property.hasAnnotation<StringSetting>() ->
                            settingProcessor.invoke(StringSettingWrapped(
                                property.findAnnotation()!!,
                                if (property.getter.call(instance) is SettingAdapter<*>)
                                    AdapterProvider(property.call(instance) as SettingAdapter<String>)
                                else
                                    PropertyProvider(
                                        instance,
                                        property as KMutableProperty1<out Any, String>
                                    )
                            ))
                    }
                } catch (e: ClassCastException) {
                    LOGGER.err("---------------------------")
                    LOGGER.err("FAILED TO COLLECT SETTING!")
                    LOGGER.err("Setting is incorrect type!")
                    LOGGER.err("Offender: ${property.name}")
                    LOGGER.err("Class: ${declaredClass.qualifiedName ?: "UNKNOWN"}")
                    LOGGER.err("---------------------------")
                }
            }
        }
    }

}