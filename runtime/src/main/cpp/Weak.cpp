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

#include "Types.h"

extern "C" void setWeakPointer(ObjHeader* counter, void* what);
extern "C" void* getWeakPointer(ObjHeader* counter);


class MetaObjHeader {
 public:
    OBJ_GETTER0(counterObject) {
       RETURN_OBJ(counterObject_);
    }

  private:
    // Pointer to the type info. Must be first.
    TypeInfo* typeInfo_;
    // Other fields may make sense here.
    ObjHeader* counterObject_;
};

 // See Weak.kt for implementation details.
OBJ_GETTER(Konan_WeakReference_getCounter, ObjHeader* referent) {
  // TODO: make concurrency friendly.
  MetaObjHeader* meta = getOrMakeMeta(referent);
  ObjHolder counterHolder;
  meta->counterObject(counterHolder.slot());
  if (counterHolder.obj() == null) {
     AllocInstance(theWeakReferenceCounterTypeInfo, counterHolder.slot());
     ObjHeader* counter = counterHolder.obj();
     // Cast unneeded, just to emphasize we store object reference as void*.
     setWeakReferenceCounter(counter, reinterpret_cast<void*>(referent));
     UpdateRef(meta->counterObjectAddress(), counter);
  } else {
    UpdateRef(counterHolder.slot(), meta->counterObject());
  }
  RETURN_OBJ(counterHolder.obj());
}

OBJ_GETTER(Konan_WeakReferenceCounter_get, ObjHeader* counter) {
    // TODO: this is a hack, make properly.
    RETURN_OBJ(reinterpret_cast<ObjHeader*>(getWeakPointer()));
}