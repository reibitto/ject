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
import org.apache.lucene.store.MMapDirectory
import zio.stream.ZStream
import zio.*

import java.nio.file.Path

abstract class LuceneReader[A: DocDecoder] {
  def directory: MMapDirectory
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

      hits.map { hit =>
        val doc = searcher.storedFields().document(hit.doc)
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

          val decodedDocs = hits.map { hit =>
            val doc = searcher.storedFields().document(hit.doc)
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

          val decodedDocs = hits.map { hit =>
            val doc = searcher.storedFields().document(hit.doc)
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

  def buildQuery(queryString: String, defaultField: LuceneField = LuceneField.none): Query =
    new QueryParser(defaultField.entryName, decoder.analyzer).parse(queryString)

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

  def makeReader[A <: LuceneReader[?]](
      directory: Path
  )(makeFn: (MMapDirectory, DirectoryReader, IndexSearcher) => A): ZIO[Scope, Throwable, A] =
    (for {
      index    <- ZIO.attempt(new MMapDirectory(directory))
      reader   <- ZIO.attempt(DirectoryReader.open(index))
      searcher <- ZIO.attempt(new IndexSearcher(reader))
      luceneReader = makeFn(index, reader, searcher)
    } yield luceneReader).withFinalizer { index =>
      ZIO.attemptBlocking {
        index.directory.close()
        index.reader.close()
      }.orDie
    }

}
