package ject.lucene.field

import enumeratum.EnumEntry
import enumeratum.EnumEntry.LowerCamelcase
import ject.lucene.Analyzers
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper
import org.apache.lucene.analysis.standard.StandardAnalyzer
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.index.Term

trait LuceneField extends EnumEntry with LowerCamelcase {
  def analyzer: Analyzer

  def term(term: String) = new Term(entryName, term)
}

object LuceneField {

  val none: LuceneField = new LuceneField {
    override val analyzer: Analyzer = Analyzers.standard
    override val entryName: String = ""
  }

  def perFieldAnalyzer[A <: LuceneField](fields: Seq[A]): PerFieldAnalyzerWrapper = {
    import scala.jdk.CollectionConverters.*

    new PerFieldAnalyzerWrapper(
      new StandardAnalyzer,
      fields.map(f => f.entryName -> f.analyzer).toMap.asJava
    )
  }
}
