#ifndef RUNTIME_ATOMIC_H
#define RUNTIME_ATOMIC_H

inline int atomicAdd(int* where, int what) {
#ifndef KONAN_NO_THREADS
  return __sync_add_and_fetch(where, what);
#else
  return *where += what;
#endif
}


#endif // RUNTIME_ATOMIC_H