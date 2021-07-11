/*
 * Copyright (C) isXander [2019 - 2021]
 * This program comes with ABSOLUTELY NO WARRANTY
 * This is free software, and you are welcome to redistribute it
 * under the certain conditions that can be found here
 * https://www.gnu.org/licenses/gpl-3.0.en.html
 *
 * If you have any questions or concerns, please create
 * an issue on the github page that can be found here
 * https://github.com/isXander/EvergreenHUD
 *
 * If you have a private concern, please contact
 * isXander @ business.isxander@gmail.com
 */

package dev.isxander.evergreenhud.elements

import club.chachy.event.on
import co.uk.isxander.xanderlib.utils.BreakException
import co.uk.isxander.xanderlib.utils.Constants.mc
import co.uk.isxander.xanderlib.utils.json.BetterJsonObject
import dev.isxander.evergreenhud.config.ElementConfig
import dev.isxander.evergreenhud.config.MainConfig
import dev.isxander.evergreenhud.settings.ConfigProcessor
import dev.isxander.evergreenhud.settings.JsonValues
import dev.isxander.evergreenhud.settings.Setting
import dev.isxander.evergreenhud.settings.impl.*
import net.minecraft.client.gui.GuiChat
import net.minecraftforge.client.event.RenderGameOverlayEvent
import org.reflections.Reflections
import java.util.*
import java.util.concurrent.atomic.AtomicReference


class ElementManager : ConfigProcessor {

    /**
     * all registered elements
     */
    private val availableElements: MutableMap<String, Class<out Element>> = HashMap()
    /**
     * @return the elements that are currently being rendered
     */
    val currentElements: List<Element> = ArrayList()

    /* Config */
    val mainConfig: MainConfig = MainConfig(this)
    val elementConfig: ElementConfig = ElementConfig(this)

    /* Settings */
    val settings: MutableList<Setting<*, *>> = ArrayList()

    @BooleanSetting(name = "Enabled", category = "General", description = "Display any elements you have created.")
    var enabled = true

    init {
        collectSettings()
        findAndRegisterElements()

        on<RenderGameOverlayEvent.Post>()
            .filter { it.type == RenderGameOverlayEvent.ElementType.ALL }
            .filter { enabled }
            .subscribe { renderElements(it.partialTicks) }
    }

    private fun findAndRegisterElements() {
        val reflections = Reflections("")
        for (clazz in reflections.getTypesAnnotatedWith(ElementMeta::class.java)) {
            val annotation = clazz.getAnnotation(ElementMeta::class.java)
            availableElements[annotation.id] = clazz as Class<out Element>
        }
    }

    fun getAvailableElements(): Map<String, Class<out Element>> {
        return Collections.unmodifiableMap(availableElements)
    }

    /**
     * Create a new instance of an element
     * without the need to catch exceptions
     *
     * @param id the internal id of the element
     * @return element instance
     */
    fun getNewElementInstance(id: String?): Element? {
        val elementClass = availableElements[id] ?: return null
        return elementClass.newInstance()
    }

    /**
     * @param element get the identifier of an instance of an element
     */
    fun getElementId(element: Element): String? {
        val name = AtomicReference<String>()
        try {
            for (e in availableElements) {
                if (e.value == e.javaClass) {
                    name.set(e.key)
                    break
                }
            }
        } catch (ignored: BreakException) {
        }
        return name.get()
    }

    fun renderElements(partialTicks: Float) {
        mc.mcProfiler.startSection("Element Render")

        val inChat = mc.currentScreen is GuiChat
        val inDebug = mc.gameSettings.showDebugInfo
        val inGui = mc.currentScreen != null && mc.currentScreen !is GuiScreenElements && mc.currentScreen !is GuiChat

        for (e in currentElements) {
            if (mc.inGameHasFocus && !inDebug || e.showInChat && inChat || e.showInDebug && inDebug && !(!e.showInChat && inChat) || e.showUnderGui && inGui) {
                e.render(partialTicks, RenderOrigin.HUD)
            }
        }
        mc.mcProfiler.endSection()
    }

    private fun collectSettings() {
        val classes = ArrayList<Class<*>>()
        var clazz: Class<*>? = this::class.java
        while (clazz != null) {
            classes.add(clazz)
            clazz = clazz.superclass
        }
        for (declaredClass in classes) {
            for (field in declaredClass.declaredFields) {
                val boolean = field.getAnnotation(BooleanSetting::class.java)
                if (boolean != null)
                    settings.add(BooleanSettingWrapped(boolean, this, field))

                val int = field.getAnnotation(IntSetting::class.java)
                if (int != null)
                    settings.add(IntSettingWrapped(int, this, field))

                val float = field.getAnnotation(FloatSetting::class.java)
                if (float != null)
                    settings.add(FloatSettingWrapped(float, this, field))

                val string = field.getAnnotation(StringSetting::class.java)
                if (string != null)
                    settings.add(StringSettingWrapped(string, this, field))

                val stringList = field.getAnnotation(StringListSetting::class.java)
                if (string != null)
                    settings.add(StringListSettingWrapped(stringList, this, field))

                val enum = field.getAnnotation(EnumSetting::class.java)
                if (enum != null)
                    settings.add(EnumSettingWrapped(enum, this, field))

                val color = field.getAnnotation(ColorSetting::class.java)
                if (color != null)
                    settings.add(ColorSettingWrapped(color, this, field))
            }
        }

    }

    override fun generateJson(): BetterJsonObject {
        val json = BetterJsonObject()

        for (setting in settings) {
            if (!setting.shouldSave()) continue

            if (setting.getDefaultJsonValue() == setting.getJsonValue()) continue

            when (setting.jsonValue) {
                JsonValues.STRING -> json.addProperty(setting.getJsonKey(), setting.getJsonValue() as String)
                JsonValues.BOOLEAN -> json.addProperty(setting.getJsonKey(), setting.getJsonValue() as Boolean)
                JsonValues.FLOAT -> json.addProperty(setting.getJsonKey(), setting.getJsonValue() as Float)
                JsonValues.INT -> json.addProperty(setting.getJsonKey(), setting.getJsonValue() as Int)
            }
        }

        return json
    }

    override fun processJson(json: BetterJsonObject) {
        for (key in json.allKeys) {
            for (setting in settings) {
                if (setting.getJsonKey() == key) {
                    when (setting.jsonValue) {
                        JsonValues.STRING -> setting.setJsonValue(json.optString(key, setting.getDefaultJsonValue() as String))
                        JsonValues.BOOLEAN -> setting.setJsonValue(json.optBoolean(key, setting.getDefaultJsonValue() as Boolean))
                        JsonValues.FLOAT -> setting.setJsonValue(json.optFloat(key, setting.getDefaultJsonValue() as Float))
                        JsonValues.INT -> setting.setJsonValue(json.optInt(key, setting.getDefaultJsonValue() as Int))
                    }

                    break
                }
            }
        }
    }

}