/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.modelix.editor

import kotlinx.html.TagConsumer
import kotlinx.html.classes
import kotlinx.html.div
import org.w3c.dom.HTMLElement

class JSCaretSelectionView(selection: CaretSelection, val editor: JsEditorComponent) : SelectionView<CaretSelection>(selection) {

    override fun <T> produceHtml(consumer: TagConsumer<T>) {
        consumer.div("caret own") {
            val textLength = selection.layoutable.cell.getVisibleText()?.length ?: 0
            if (textLength == 0) {
                // A typical case is a StringLiteral editor for an empty string.
                // There is no space around the empty text cell.
                // 'leftend' or 'rightend' styles would look like the caret is set into one of the '"' cells.
            } else if (selection.end == 0) {
                classes += "leftend"
            } else if (selection.end == textLength) {
                classes += "rightend"
            }
        }
    }

    override fun update() {
        val layoutable = selection.layoutable
        val textElement = GeneratedHtmlMap.getOutput(selection.layoutable) ?: return
        val caretElement = GeneratedHtmlMap.getOutput(this) ?: return
        updateCaretBounds(textElement, selection.end, editor.getMainLayer(), caretElement)
    }

    companion object {
        fun updateCaretBounds(textElement: HTMLElement, caretPos: Int, coordinatesElement: HTMLElement?, caretElement: HTMLElement) {
            val text = textElement.innerText
            val textLength = text.length
            val cellAbsoluteBounds = textElement.getAbsoluteInnerBounds()
            val cellRelativeBounds = cellAbsoluteBounds.relativeTo(coordinatesElement?.getAbsoluteBounds() ?: ZERO_BOUNDS)
            val characterWidth = if (textLength == 0) 0.0 else cellAbsoluteBounds.width / textLength
            val caretX = cellRelativeBounds.x + caretPos * characterWidth
            val leftEnd = caretPos == 0
            val rightEnd = caretPos == textLength
            val caretOffsetX = if (rightEnd && !leftEnd) -4 else -1
            val caretOffsetY = if (leftEnd || rightEnd) -1 else 0
            caretElement.style.height = "${cellRelativeBounds.height}px"
            caretElement.style.left = "${caretX + caretOffsetX}px"
            caretElement.style.top = "${cellRelativeBounds.y + caretOffsetY}px"
        }
    }
}