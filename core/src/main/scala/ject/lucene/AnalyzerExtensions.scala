package ject.lucene

import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute

import scala.collection.mutable

object AnalyzerExtensions {
  implicit class AnalyzerExtension(val self: Analyzer) extends AnyVal {
    def tokensFor(text: String): Seq[String] = {
      val buffer = new mutable.ArrayDeque[String](initialSize = 8)
      val stream = self.tokenStream("", text)
      val attr   = stream.addAttribute(classOf[CharTermAttribute])
      stream.reset()

      while (stream.incrementToken())
        buffer.addOne(attr.toString)

      buffer.toSeq
    }
  }
}
