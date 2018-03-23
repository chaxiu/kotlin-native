fun main(args : Array<String>) {
  for (s in args) {
      println(s)
  }
}

enum class Zzz(val f: () -> Unit) {
    Z1({})
    Z2({Z1});

    fun foo() = f()
}