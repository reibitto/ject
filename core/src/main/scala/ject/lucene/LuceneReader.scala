package ject.lucene

import ject.lucene.field.LuceneField
import org.apache.lucene.index.{DirectoryReader, IndexWriter, IndexWriterConfig}
import org.apache.lucene.queryparser.classic.QueryParser
import org.apache.lucene.search.{IndexSearcher, Query, ScoreDoc, Sort}
import org.apache.lucene.store.MMapDirectory
import zio.{Chunk, Scope, Task, ZIO}
import zio.stream.ZStream

import java.nio.file.Path

final case class LuceneReader[A: DocDecoder](
    directory: MMapDirectory,
    reader: DirectoryReader,
    searcher: IndexSearcher
) {
  val decoder: DocDecoder[A] = implicitly[DocDecoder[A]]

  def headOption(query: Query): Task[Option[A]] =
    ZIO.attempt {
      searcher.search(query, 1).scoreDocs.headOption.map { hit =>
        decoder.decode(searcher.storedFields().document(hit.doc))
      }
    }

  def take(query: Query, n: Int): Task[Seq[ScoredDoc[A]]] =
    ZIO.attempt {
      val hits = searcher.search(query, n).scoreDocs

      hits.map { hit =>
        val doc = searcher.storedFields().document(hit.doc)
        ScoredDoc(decoder.decode(doc), hit.score)
      }.toSeq
    }

  def search(query: Query, hitsPerPage: Int = 10): ZStream[Any, Throwable, ScoredDoc[A]] =
    ZStream.unfoldChunkZIO(Option.empty[ScoreDoc]) { state =>
      ZIO.attempt {
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
      ZIO.attempt {
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

  def buildQuery(queryString: String, defaultField: LuceneField = LuceneField.none): Query =
    new QueryParser(defaultField.entryName, decoder.analyzer).parse(queryString)

  def createWriter(autoCommitOnRelease: Boolean): ZIO[Scope, Throwable, IndexWriter] =
    ZIO.attempt {
      val config = new IndexWriterConfig(decoder.analyzer)
      new IndexWriter(directory, config)
    }.withFinalizer { writer =>
      ZIO.attempt {
        if (autoCommitOnRelease) {
          writer.commit()
        }

        writer.close()
      }.orDie
    }
}

object LuceneReader {

  def make[A: DocDecoder](directory: Path): ZIO[Scope, Throwable, LuceneReader[A]] =
    (for {
      index    <- ZIO.attempt(new MMapDirectory(directory))
      reader   <- ZIO.attempt(DirectoryReader.open(index))
      searcher <- ZIO.attempt(new IndexSearcher(reader))
    } yield new LuceneReader[A](index, reader, searcher)).withFinalizer { index =>
      ZIO.attempt {
        index.directory.close()
        index.reader.close()
      }.orDie
    }
}
