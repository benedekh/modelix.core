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
import kotlinx.html.div
import kotlinx.html.span

data class CellSelection(val cell: Cell): Selection() {
    fun getEditor(): EditorComponent? = cell.editorComponent

    override fun isValid(): Boolean {
        return getEditor() != null
    }

    override fun update(editor: EditorComponent): Selection? {
        return cell.data.cellReferences.asSequence()
            .flatMap { editor.resolveCell(it) }
            .map { CellSelection(it) }
            .firstOrNull()
    }

    override fun processKeyDown(event: JSKeyboardEvent): Boolean {
        val editor = getEditor() ?: throw IllegalStateException("Not attached to any editor")
        when (event.knownKey) {
            KnownKeys.ArrowUp -> {
                if (event.modifiers.meta) {
                    cell.ancestors().firstOrNull { it.getProperty(CommonCellProperties.selectable) }
                        ?.let { editor.changeSelection(CellSelection(it)) }
                }
            }
            else -> {}
        }

        return true
    }

    fun getLayoutables(): List<Layoutable> {
        val editor = getEditor() ?: return emptyList()
        val rootText = editor.getRootCell().layout
        return cell.layout.lines.asSequence().flatMap { it.words }
            .filter { it.getLine()?.getText() === rootText }.toList()
    }
}