package ject.utils

object StringExtensions {

  implicit class StringExtension(val self: String) extends AnyVal {

    def isSurroundedWith(s: String): Boolean =
      self.startsWith(s) && self.endsWith(s)
  }
}
