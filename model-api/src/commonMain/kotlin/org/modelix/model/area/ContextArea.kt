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
package org.modelix.model.area

import org.modelix.model.api.IModelList
import org.modelix.model.api.IReferenceResolutionScope
import org.modelix.model.api.concat

@Deprecated("Use IReferenceResolutionScope.contextScope")
object ContextArea {

    fun getArea(): IArea? = IReferenceResolutionScope.contextScope.getValue()?.asArea()

    fun <T> withAdditionalContext(area: IArea, runnable: () -> T): T {
        val activeContext: IModelList? = IReferenceResolutionScope.contextScope.getValue() as IModelList?
        return if (activeContext == null) {
            IReferenceResolutionScope.contextScope.computeWith(area, runnable)
        } else {
            IReferenceResolutionScope.contextScope.computeWith(activeContext.concat(area.asModelList()), runnable)
        }
    }

    fun <T> offer(area: IArea, r: () -> T): T {
        val current = IReferenceResolutionScope.contextScope.getValue() as IModelList?
        return if (current == null || !current.getModels().contains(area)) {
            IReferenceResolutionScope.contextScope.computeWith(area, r)
        } else {
            r()
        }
    }
}
