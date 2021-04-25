package ject.lucene

import ject.lucene.field.LuceneField
import org.apache.lucene.analysis.Analyzer
import org.apache.lucene.document.Document
import org.apache.lucene.index.{ DirectoryReader, IndexWriter, IndexWriterConfig }
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{ IndexSearcher, Query, ScoreDoc }
import org.apache.lucene.store.MMapDirectory
import zio.stream.ZStream
import zio.{ Chunk, Task, TaskManaged, ZManaged }

import java.nio.file.Path

class LuceneIndex[A: DocumentDecoder](directory: Path) {
  // TODO: Properly capture these in ZManaged
  val index: MMapDirectory        = new MMapDirectory(directory)
  val decoder: DocumentDecoder[A] = implicitly[DocumentDecoder[A]]
  val analyzer: Analyzer          = decoder.analyzer

  lazy val reader: DirectoryReader = DirectoryReader.open(index)
  lazy val searcher: IndexSearcher = new IndexSearcher(reader)

  def searchQuery(query: Query, hitsPerPage: Int = 10): ZStream[Any, Throwable, A] =
    ZStream.unfoldChunkM(Option.empty[ScoreDoc]) { state =>
      Task {
        val docs = state match {
          case Some(scoreDoc) =>
            searcher.searchAfter(scoreDoc, query, hitsPerPage)

          case None =>
            searcher.search(query, hitsPerPage)
        }

        if (docs.scoreDocs.isEmpty) {
          None
        } else {
          val hits = docs.scoreDocs

          val decodedDocs = hits.toIterable.map { hit =>
            val docId         = hit.doc
            val doc: Document = searcher.doc(docId)

            decoder.decode(doc)
          }

          Some(Chunk.fromIterable(decodedDocs), hits.lastOption)
        }
      }
    }

  def searchRaw(
    queryString: String,
    defaultField: LuceneField = LuceneField.none,
    hitsPerPage: Int = 10
  ): ZStream[Any, Throwable, A] = {
    val queryParser = new QueryParser(defaultField.entryName, analyzer)
    queryParser.setAllowLeadingWildcard(true)

    for {
      query   <- ZStream.fromEffect(Task(queryParser.parse(queryString)))
      results <- searchQuery(query, hitsPerPage)
    } yield results
  }

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
