package ject.lucene

import ject.lucene.field.LuceneField
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.index.IndexWriter
import org.apache.lucene.index.IndexWriterConfig
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.IndexSearcher
import org.apache.lucene.search.Query
import org.apache.lucene.search.ScoreDoc
import org.apache.lucene.search.Sort
import org.apache.lucene.store.Directory
import zio.*
import zio.stream.ZStream

import java.nio.file.Path

abstract class LuceneReader[A: DocDecoder] {
  def directory: Directory
  def reader: DirectoryReader
  def searcher: IndexSearcher

  private val decoder: DocDecoder[A] = implicitly[DocDecoder[A]]

  def headOption(query: Query): Task[Option[A]] =
    ZIO.attemptBlocking {
      searcher.search(query, 1).scoreDocs.headOption.map { hit =>
        decoder.decode(searcher.storedFields().document(hit.doc))
      }
    }

  def take(query: Query, n: Int): Task[Seq[ScoredDoc[A]]] =
    ZIO.attemptBlocking {
      val hits = searcher.search(query, n).scoreDocs
      val storedFields = searcher.storedFields()

      hits.map { hit =>
        val doc = storedFields.document(hit.doc)
        ScoredDoc(decoder.decode(doc), hit.score)
      }.toSeq
    }

  def search(query: Query, hitsPerPage: Int = 10): ZStream[Any, Throwable, ScoredDoc[A]] =
    ZStream.unfoldChunkZIO(Option.empty[ScoreDoc]) { state =>
      ZIO.attemptBlocking {
        val docs = state match {
          case Some(scoreDoc) =>
            searcher.searchAfter(scoreDoc, query, hitsPerPage)

          case None =>
            searcher.search(query, hitsPerPage)
        }

        Option.when(docs.scoreDocs.nonEmpty) {
          val hits = docs.scoreDocs
          val storedFields = searcher.storedFields()

          val decodedDocs = hits.map { hit =>
            val doc = storedFields.document(hit.doc)
            ScoredDoc(decoder.decode(doc), hit.score)
          }

          (Chunk.fromIterable(decodedDocs), hits.lastOption)
        }
      }
    }

  def searchSorted(query: Query, sort: Sort, hitsPerPage: Int = 10): ZStream[Any, Throwable, ScoredDoc[A]] =
    ZStream.unfoldChunkZIO(Option.empty[ScoreDoc]) { state =>
      ZIO.attemptBlocking {
        val docs = state match {
          case Some(scoreDoc) =>
            searcher.searchAfter(scoreDoc, query, hitsPerPage, sort, true)

          case None =>
            searcher.search(query, hitsPerPage, sort, true)
        }

        Option.when(docs.scoreDocs.nonEmpty) {
          val hits = docs.scoreDocs
          val storedFields = searcher.storedFields()

          val decodedDocs = hits.map { hit =>
            val doc = storedFields.document(hit.doc)
            ScoredDoc(decoder.decode(doc), hit.score)
          }

          (Chunk.fromIterable(decodedDocs), hits.lastOption)
        }
      }
    }

  def searchRaw(
      queryString: String,
      defaultField: LuceneField = LuceneField.none,
      hitsPerPage: Int = 20
  ): ZStream[Any, Throwable, ScoredDoc[A]] = {
    val queryParser = new QueryParser(defaultField.entryName, decoder.analyzer)
    queryParser.setAllowLeadingWildcard(true)

    for {
      query   <- ZStream.fromZIO(ZIO.attempt(queryParser.parse(queryString)))
      results <- search(query, hitsPerPage)
    } yield results
  }

  def searchRawSorted(
      queryString: String,
      sort: Sort,
      defaultField: LuceneField = LuceneField.none,
      hitsPerPage: Int = 20
  ): ZStream[Any, Throwable, ScoredDoc[A]] = {
    val queryParser = new QueryParser(defaultField.entryName, decoder.analyzer)
    queryParser.setAllowLeadingWildcard(true)

    for {
      query   <- ZStream.fromZIO(ZIO.attempt(queryParser.parse(queryString)))
      results <- searchSorted(query, sort, hitsPerPage)
    } yield results
  }

  def list: ZStream[Any, Throwable, ScoredDoc[A]] =
    searchRaw("*:*")

  def buildQuery(queryString: String, defaultField: LuceneField = LuceneField.none): Task[Query] =
    ZIO.attempt(new QueryParser(defaultField.entryName, decoder.analyzer).parse(queryString))

  def createWriter(autoCommitOnRelease: Boolean): ZIO[Scope, Throwable, IndexWriter] =
    ZIO.attempt {
      val config = new IndexWriterConfig(decoder.analyzer)
      new IndexWriter(directory, config)
    }.withFinalizer { writer =>
      ZIO.attemptBlocking {
        if (autoCommitOnRelease) {
          writer.commit()
        }

        writer.close()
      }.orDie
    }
}

object LuceneReader {

  /** Builds a reader on top of an already-acquired `Directory` (e.g. a
    * `ByteBuffersDirectory` shared with a writer via
    * `LuceneDirectory.inMemory`). The directory itself is not closed when the
    * returned reader's scope ends — only the `DirectoryReader` opened on top of
    * it — since the directory may still be in use elsewhere (its lifecycle is
    * the caller's responsibility, e.g. via `LuceneDirectory`).
    */
  def makeReader[A <: LuceneReader[?]](
      directory: Directory
  )(makeFn: (Directory, DirectoryReader, IndexSearcher) => A): ZIO[Scope, Throwable, A] =
    for {
      reader   <- ZIO.fromAutoCloseable(ZIO.attempt(DirectoryReader.open(directory)))
      searcher <- ZIO.attempt(new IndexSearcher(reader))
    } yield makeFn(directory, reader, searcher)

  /** Builds a reader backed by files at `path` on disk, owning the underlying
    * `MMapDirectory`'s lifecycle (closed when the returned reader's scope
    * ends).
    */
  def makeReader[A <: LuceneReader[?]](
      path: Path
  )(makeFn: (Directory, DirectoryReader, IndexSearcher) => A): ZIO[Scope, Throwable, A] =
    for {
      directory <- LuceneDirectory.fromPath(path)
      result    <- makeReader(directory)(makeFn)
    } yield result

}
