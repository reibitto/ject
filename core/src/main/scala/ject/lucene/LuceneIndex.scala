package ject.lucene

import java.nio.file.Path

import ject.lucene.field.LuceneField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.{ DirectoryReader, IndexWriter, IndexWriterConfig }
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{ IndexSearcher, Query }
import org.apache.lucene.store.MMapDirectory
import zio.{ Task, TaskManaged, ZManaged }

class LuceneIndex[A: DocumentDecoder](directory: Path) {
  // TODO: Properly capture these in ZManaged
  val index: MMapDirectory        = new MMapDirectory(directory)
  val decoder: DocumentDecoder[A] = implicitly[DocumentDecoder[A]]
  val analyzer: Analyzer          = decoder.analyzer

  lazy val reader: DirectoryReader = DirectoryReader.open(index)
  lazy val searcher: IndexSearcher = new IndexSearcher(reader)

  def searchQuery(query: Query, hitsPerPage: Int = 10): Task[IndexedSeq[A]] =
    Task {
      val docs = searcher.search(query, hitsPerPage)
      val hits = docs.scoreDocs

      hits.toIndexedSeq.map { hit =>
        val docId         = hit.doc
        val doc: Document = searcher.doc(docId)

        decoder.decode(doc)
      }
    }

  // TODO: Switch to paginated Stream
  def searchRaw(
    queryString: String,
    defaultField: LuceneField = LuceneField.none,
    hitsPerPage: Int = 10
  ): Task[IndexedSeq[A]] =
    for {
      query   <- Task(new QueryParser(defaultField.entryName, analyzer).parse(queryString))
      results <- searchQuery(query, hitsPerPage)
    } yield results

  def buildQuery(queryString: String, defaultField: LuceneField = LuceneField.none): Query =
    new QueryParser(defaultField.entryName, analyzer).parse(queryString)

  def createWriter: IndexWriter = {
    val config = new IndexWriterConfig(analyzer)
    new IndexWriter(index, config)
  }
}

object LuceneIndex {
  def make[A: DocumentDecoder](directory: Path): TaskManaged[LuceneIndex[A]] =
    ZManaged.succeed(new LuceneIndex[A](directory))
}
