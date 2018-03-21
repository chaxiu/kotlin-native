/*
 * Copyright 2010-2018 JetBrains s.r.o.
 *
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

package konan.ref

import konan.cinterop.COpaquePointer

/**
 *   Theory of operations:
 *
 *  Weak references in Kotlin/Native are implemented in the following way. Whenever weak reference to an
 * object is created, we atomically modify type info pointer in the object to point into a metaobject.
 * This metaobject contains a strong reference to the counter object (instance of WeakReferenceCounter class).
 * Every other weak reference contains a strong reference to the counter object.
 *
 *      [weak1]  [weak2]
 *         \      /
 *         V     V
 *      --[Counter] <--
 *     |               |
 *     |               |
 *      ->[Object] -> [Meta]
 *
 *   References from weak reference objects to the counter and from the metaobject to the counter are strong,
 *  and from the counter to the object is nullably weak. So whenever an object dies, if it has a metaobject,
 *  it is traversed to find a counter object, and atomically nullify reference to the object. Afterward, all attempts
 *  to get the object would yield null.
 */
@ExportTypeInfo("theWeakReferenceCounterTypeInfo")
internal class WeakReferenceCounter {
    // Actual object pointer.
    var pointer: COpaquePointer?

    @SymbolName("Konan_WeakReferenceCounter_get")
    internal external fun get(): Any?
}

@ExportForCppRuntime
internal fun setWeakPointer(counter: WeakReferenceCounter, pointer: COpaquePointer?): Unit {
    counter.pointer = pointer
}

@ExportForCppRuntime
internal fun getWeakPointer(counter: WeakReferenceCounter) = counter.pointer

@SymbolName("Konan_WeakReference_getCounter")
external private fun getCounter(referent: Any): WeakReferenceCounter

class WeakReference<T> {
    constructor(referent: T) {
        if (referent == null) throw Error("Weak reference to null?")
        pointer = getCounter(referent)
    }

    private var pointer: WeakReferenceCounter?

    inline reified fun get() = pointer.get() as T?

    fun clear() {
        pointer = null
    }
}
