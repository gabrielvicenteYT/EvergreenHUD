/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2022].
 *
 * This work is licensed under the GPL-3 License.
 * To view a copy of this license, visit https://www.gnu.org/licenses/gpl-3.0.en.html
 */

package dev.isxander.evergreenhud

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.isxander.evergreenhud.addons.AddonLoader
import dev.isxander.evergreenhud.config.convert.ConfigConverter
import dev.isxander.evergreenhud.elements.ElementManager
import dev.isxander.evergreenhud.config.profile.ProfileManager
import dev.isxander.evergreenhud.utils.registerKeyBind
import dev.isxander.evergreenhud.event.ClientTickEvent
import dev.isxander.evergreenhud.event.EventBus
import dev.isxander.evergreenhud.event.RenderHudEvent
import dev.isxander.evergreenhud.event.ServerDamageEntityEventManager
import dev.isxander.evergreenhud.metrics.UniqueUsersMetric
import dev.isxander.evergreenhud.ui.BlacklistedScreen
import dev.isxander.evergreenhud.ui.UpdateScreen
import dev.isxander.evergreenhud.packets.client.registerElementsPacket
import dev.isxander.evergreenhud.repo.ReleaseChannel
import dev.isxander.evergreenhud.repo.RepoManager
import dev.isxander.evergreenhud.ui.ConfigConverterScreen
import dev.isxander.evergreenhud.ui.ElementDisplay
import dev.isxander.evergreenhud.utils.*
import dev.isxander.evergreenhud.utils.hypixel.locraw.LocrawManager
import kotlinx.coroutines.runBlocking
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.SharedConstants
import net.minecraft.client.util.InputUtil
import net.minecraft.text.LiteralText
import org.bundleproject.libversion.Version
import org.lwjgl.glfw.GLFW
import java.io.File

object EvergreenHUD : ClientModInitializer {
    const val NAME = "__GRADLE_NAME__"
    const val ID = "__GRADLE_ID__"
    const val REVISION = "__GRADLE_REVISION__"
    const val VERSION_STR = "__GRADLE_VERSION__"
    val VERSION = Version.of(VERSION_STR)
    const val LOADER = "fabric"

    val RELEASE_CHANNEL: ReleaseChannel
        get() =
            if (VERSION.prerelease == null) ReleaseChannel.RELEASE
            else ReleaseChannel.BETA

    val dataDir = File(mc.runDirectory, "evergreenhud")
    val eventBus = EventBus()
    val locrawManager = LocrawManager()

    lateinit var profileManager: ProfileManager private set
    lateinit var elementManager: ElementManager private set
    lateinit var addonLoader: AddonLoader private set

    val isReplayModLoaded = FabricLoader.getInstance().isModLoaded("replaymod")

    var postInitialized = false
        private set

    var firstLaunch = false
        private set

    /**
     * Initialises the whole mod
     *
     * @since 2.0
     * @author isXander
     */
    override fun onInitializeClient() {
        logger.info("Starting EvergreenHUD $VERSION_STR for ${SharedConstants.getGameVersion().name}")

        val startTime = System.currentTimeMillis()
        mc.profiler.push("EvergreenHUD Startup")

        firstLaunch = !dataDir.exists()
        dataDir.mkdirs()

        logger.debug("Initialising element manager...")
        elementManager = ElementManager()

        logger.debug("Discovering addons...")
        addonLoader = AddonLoader()
        logger.debug("Adding addon element sources...")
        addonLoader.addSources(elementManager)
        logger.debug("Invoking pre-initialization addon entrypoints...")
        addonLoader.invokePreinitEntrypoints()

        logger.debug("Loading configs...")
        profileManager = ProfileManager().apply { load() }
        elementManager.apply {
            globalConfig.load()
            elementConfig.load()
        }

        logger.debug("Registering hooks...")

        ClientCommandManager.DISPATCHER.register(LiteralArgumentBuilder.literal<FabricClientCommandSource>("evergreenhud").executes {
            GuiHandler.displayGui(ElementDisplay())
            1
        })

        registerKeyBind(
            GLFW.GLFW_KEY_HOME,
            InputUtil.Type.KEYSYM,
            name = "evergreenhud.key.opengui",
            category = "evergreenhud.keycategory"
        ) {
            mc.setScreen(ElementDisplay())
        }

        registerKeyBind(
            GLFW.GLFW_KEY_UNKNOWN,
            InputUtil.Type.KEYSYM,
            name = "evergreenhud.key.toggle",
            category = "evergreenhud.keycategory"
        ) {
            elementManager.enabled = !elementManager.enabled
            mc.inGameHud?.chatHud?.addMessage(evergreenHudPrefix + LiteralText("Toggled mod."))
        }

        logger.debug("Registering events...")
        registerEvents()

        logger.debug("Registering packet listeners...")
        registerElementsPacket()

        logger.debug("Invoking addon entrypoints...")
        addonLoader.invokeInitEntrypoints()

        runAsync {
            logger.debug("Calling Metrics APIs")
            runBlocking { UniqueUsersMetric.putApi() }
        }

        mc.profiler.pop()
        logger.info("Finished loading EvergreenHUD. Took ${System.currentTimeMillis() - startTime} ms.")
    }

    fun onPostInitialize() {
        if (!postInitialized) {
            if (!FabricLoader.getInstance().isDevelopmentEnvironment) {
                if (elementManager.checkForUpdates) {
                    logger.info("Getting information from API...")
                    runAsync {
                        val response = runBlocking { RepoManager.getResponse() }

                        val latest = response.latest ?: return@runAsync
                        if (latest < VERSION) {
                            logger.info("Found update.")
                            mc.setScreen(UpdateScreen(latest.toString(), mc.currentScreen))
                        }
                    }
                }
            } else {
                logger.info("Skipping update due to being in a development environment.")
            }

            if (firstLaunch) {
                logger.info("Welcome to EvergreenHUD! Detected first launch.")

                logger.debug("Detecting other HUD mod configs...")

                var lastGui = mc.currentScreen
                for (converter in ConfigConverter.all) {
                    if (converter.detect()) {
                        logger.info("Found ${converter.name} config! Displaying GUI.")
                        lastGui = ConfigConverterScreen(converter, lastGui)
                    }
                }

                if (lastGui is ConfigConverterScreen)
                    mc.setScreen(lastGui)
            }
        }

        postInitialized = true
    }

    private fun registerEvents() {
        ClientTickEvents.END_CLIENT_TICK.register {
            eventBus.post(ClientTickEvent())
        }

        HudRenderCallback.EVENT.register { matrices, tickDelta ->
            eventBus.post(RenderHudEvent(matrices, tickDelta))
        }

        ServerDamageEntityEventManager(eventBus)
    }
}
