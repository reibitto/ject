package ject.utils

object StringExtensions {

  implicit class StringExtension(val self: String) extends AnyVal {

    def isSurroundedWith(s: String): Boolean =
      self.startsWith(s) && self.endsWith(s)

    def normalizeNewlines: String =
      self.replaceAll("\\r\\n?", "\n")

    def codePointIterator: Iterator[String] =
      new Iterator[String] {
        val text: String = self
        var index: Int = 0

        override def hasNext: Boolean = index < text.length

        override def next(): String = {
          val codePoint = Character.codePointAt(text, index)
          val charCount = Character.charCount(codePoint)
          val codePointString = self.substring(index, index + charCount)

          index += charCount

          codePointString
        }
      }

    def codePointIterable: Iterable[String] = Iterable.from(codePointIterator)
  }
}
