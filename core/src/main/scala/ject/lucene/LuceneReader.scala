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
import zio.Chunk
import zio.Task
import zio.TaskManaged
import zio.stream.ZStream

import java.nio.file.Path

final case class LuceneReader[A: DocDecoder](
  directory: MMapDirectory,
  reader: DirectoryReader,
  searcher: IndexSearcher
) {
  val decoder: DocDecoder[A] = implicitly[DocDecoder[A]]

  def headOption(query: Query): Task[Option[A]] =
    Task {
      searcher.search(query, 1).scoreDocs.headOption.map { hit =>
        decoder.decode(searcher.doc(hit.doc))
      }
    }

  def take(query: Query, n: Int): Task[Seq[ScoredDoc[A]]] =
    Task {
      val hits = searcher.search(query, n).scoreDocs

      hits.toIterable.map { hit =>
        val doc = searcher.doc(hit.doc)
        ScoredDoc(decoder.decode(doc), hit.score)
      }.toSeq
    }

  def search(query: Query, hitsPerPage: Int = 20): ZStream[Any, Throwable, ScoredDoc[A]] =
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
            val doc = searcher.doc(hit.doc)
            ScoredDoc(decoder.decode(doc), hit.score)
          }

          Some(Chunk.fromIterable(decodedDocs), hits.lastOption)
        }
      }
    }

  def searchSorted(query: Query, sort: Sort, hitsPerPage: Int = 20): ZStream[Any, Throwable, ScoredDoc[A]] =
    ZStream.unfoldChunkM(Option.empty[ScoreDoc]) { state =>
      Task {
        val docs = state match {
          case Some(scoreDoc) =>
            searcher.searchAfter(scoreDoc, query, hitsPerPage, sort, true)

          case None =>
            searcher.search(query, hitsPerPage, sort, true)
        }

        if (docs.scoreDocs.isEmpty) {
          None
        } else {
          val hits = docs.scoreDocs

          val decodedDocs = hits.toIterable.map { hit =>
            val doc = searcher.doc(hit.doc)
            ScoredDoc(decoder.decode(doc), hit.score)
          }

          Some(Chunk.fromIterable(decodedDocs), hits.lastOption)
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
      query   <- ZStream.fromEffect(Task(queryParser.parse(queryString)))
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
      query   <- ZStream.fromEffect(Task(queryParser.parse(queryString)))
      results <- searchSorted(query, sort, hitsPerPage)
    } yield results
  }

  def buildQuery(queryString: String, defaultField: LuceneField = LuceneField.none): Query =
    new QueryParser(defaultField.entryName, decoder.analyzer).parse(queryString)

  def createWriter(autoCommitOnRelease: Boolean): TaskManaged[IndexWriter] =
    Task {
      val config = new IndexWriterConfig(decoder.analyzer)
      new IndexWriter(directory, config)
    }.toManaged { writer =>
      Task {
        if (autoCommitOnRelease) {
          writer.commit()
        }

        writer.close()
      }.orDie
    }
}

object LuceneReader {
  def make[A: DocDecoder](directory: Path): TaskManaged[LuceneReader[A]] =
    (for {
      index    <- Task(new MMapDirectory(directory))
      reader   <- Task(DirectoryReader.open(index))
      searcher <- Task(new IndexSearcher(reader))
    } yield new LuceneReader[A](index, reader, searcher)).toManaged { index =>
      Task {
        index.directory.close()
        index.reader.close()
      }.orDie
    }
}
