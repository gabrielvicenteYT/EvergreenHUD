/*
 * EvergreenHUD - A mod to improve your heads-up-display.
 * Copyright (c) isXander [2019 - 2021].
 *
 * This work is licensed under the CC BY-NC-SA 4.0 License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-nc-sa/4.0
 */

package dev.isxander.evergreenhud.utils

fun getCallerClass(thread: Thread = Thread.currentThread(), depth: Int = 0, ignoreInternalCalls: Boolean = false): Class<*> {
    if (!ignoreInternalCalls) return Class.forName(thread.stackTrace[3 + depth].className)

    if (depth - 3 >= thread.stackTrace.size) throw IllegalArgumentException("Depth exceeds stack trace size!")

    val internal = thread.stackTrace[2].className
    for (i in 3 + depth until thread.stackTrace.size) {
        val element = thread.stackTrace[i]

        if (element.className != internal)
            return Class.forName(element.className)
    }


    throw IllegalStateException("Could not find non-internal call within stacktrace!")
}

var vmVersion: Int = -1
    get() {
        if (field < 0) {
            field = System.getProperty("java.class.version")?.let { (it.toFloat() - 44).toInt() }
                ?: System.getProperty("java.vm.specification.version")?.substringAfterLast('.')?.toInt()
                ?: 8
        }
        return field
    }
    private set