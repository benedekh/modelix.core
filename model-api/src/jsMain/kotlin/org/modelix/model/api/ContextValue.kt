package org.modelix.model.api

actual class ContextValue<E> {
    private var value: E?

    actual constructor() {
        value = null
    }

    actual constructor(defaultValue: E) {
        value = defaultValue
    }

    actual fun getValue(): E? {
        return value
    }

    actual fun <T> computeWith(newValue: E, r: () -> T): T {
        val oldValue = value
        value = newValue
        try {
            return r()
        } finally {
            value = oldValue
        }
    }
}
