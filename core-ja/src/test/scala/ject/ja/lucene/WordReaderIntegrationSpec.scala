package ject.ja.lucene

import ject.ja.docs.WordDoc
import ject.ja.text.WordSearchStrategy
import ject.lucene.LuceneDirectory
import ject.SearchPattern
import zio.*
import zio.test.*

/** End-to-end tests against an in-memory Lucene index built from a small sample
  * dictionary, rather than assuming the WordWriter/WordReader/WordField wiring
  * is correct. Exercises the same code path as production, backed by memory
  * instead of disk.
  */
object WordReaderIntegrationSpec extends ZIOSpecDefault {

  private val sampleEntries: Seq[WordDoc] = Seq(
    WordDoc(
      id = "1",
      kanjiTerms = Seq("食べる"),
      readingTerms = Seq("たべる"),
      definitions = Seq("to eat"),
      tags = Seq.empty,
      partsOfSpeech = Seq("v1"),
      priority = 1.0,
      frequency = 100
    ),
    WordDoc(
      id = "2",
      kanjiTerms = Seq("4日", "四日"),
      readingTerms = Seq("よっか"),
      definitions = Seq("4th day of the month; four days"),
      tags = Seq.empty,
      partsOfSpeech = Seq.empty,
      priority = 1.0,
      frequency = 50
    ),
    WordDoc(
      id = "3",
      kanjiTerms = Seq.empty,
      readingTerms = Seq("ぴいちくぱあちく"),
      definitions = Seq("chirping, chattering (of birds/people)"),
      tags = Seq.empty,
      partsOfSpeech = Seq.empty,
      priority = 1.0,
      frequency = 10
    ),
    // Mirrors a real JMDict entry whose only reading is katakana ("コーヒー"), with no hiragana alternative,
    // as is conventional for loanwords.
    WordDoc(
      id = "4",
      kanjiTerms = Seq("珈琲"),
      readingTerms = Seq("コーヒー"),
      definitions = Seq("coffee"),
      tags = Seq.empty,
      partsOfSpeech = Seq.empty,
      priority = 1.0,
      frequency = 80
    ),
    // Mirrors the real JMDict entry for たぐい (ent_seq 1596870), which bundles 4 alternate kanji forms.
    // Paired with the lower-priority single-form entry below, this guards against a higher-priority entry
    // being outranked purely for having more alternate terms.
    WordDoc(
      id = "5-high-priority-many-alternates",
      kanjiTerms = Seq("類い", "類", "比い", "比"),
      readingTerms = Seq("たぐい"),
      definitions = Seq("kind, sort, type"),
      tags = Seq.empty,
      partsOfSpeech = Seq.empty,
      priority = 1.0,
      frequency = 100
    ),
    WordDoc(
      id = "6-low-priority-single-alternate",
      kanjiTerms = Seq("類い"),
      readingTerms = Seq("たぐい"),
      definitions = Seq("kind, sort, type"),
      tags = Seq.empty,
      partsOfSpeech = Seq.empty,
      priority = 0.6,
      frequency = 100
    )
  )

  private def withSampleIndex[A](
      strategy: WordSearchStrategy = WordSearchStrategy.IndexInflections
  )(f: WordReader => Task[A]): Task[A] =
    ZIO.scoped {
      for {
        // The writer and reader below must share this same instance to see each other's data (see
        // LuceneDirectory.inMemory).
        directory <- LuceneDirectory.inMemory
        _         <- ZIO.scoped(
               WordWriter
                 .make(directory, WordDoc.docEncoder(strategy))
                 .flatMap(_.addBulk(sampleEntries*))
             )
        reader <- WordReader.make(directory, strategy)
        result <- f(reader)
      } yield result
    }

  private def idsFound(reader: WordReader, query: String): Task[List[String]] =
    reader.search(SearchPattern(query)).runCollect.map(_.toList.map(_.doc.id))

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("WordReader integration")(
      test("finds an entry by exact kanji") {
        withSampleIndex()(idsFound(_, "食べる")).map(ids => assertTrue(ids.contains("1")))
      },
      test("finds an entry by exact reading, written in hiragana") {
        withSampleIndex()(idsFound(_, "たべる")).map(ids => assertTrue(ids.contains("1")))
      },
      test("finds a hiragana reading via a katakana query") {
        withSampleIndex()(idsFound(_, "タベル")).map(ids => assertTrue(ids.contains("1")))
      },
      test("finds a half-width-digit kanji term via a full-width-digit query") {
        withSampleIndex()(idsFound(_, "４日")).map(ids => assertTrue(ids.contains("2")))
      },
      test("finds the same entry via the original half-width-digit spelling") {
        withSampleIndex()(idsFound(_, "4日")).map(ids => assertTrue(ids.contains("2")))
      },
      test("finds a hiragana reading via a katakana query with long vowel marks") {
        // The entry spells the vowel out ("ぴいちくぱあちく") rather than using a long vowel mark, but someone
        // searching for this mimetic word would likely type the katakana rendering ("ピーチクパーチク").
        withSampleIndex()(idsFound(_, "ピーチクパーチク")).map(ids => assertTrue(ids.contains("3")))
      },
      test("finds an entry via a conjugated (inflected) form") {
        withSampleIndex()(idsFound(_, "食べた")).map(ids => assertTrue(ids.contains("1")))
      },
      test("finds an entry via a kanji prefix search") {
        withSampleIndex()(idsFound(_, "食べ*")).map(ids => assertTrue(ids.contains("1")))
      },
      test("finds an entry via a kanji wildcard search") {
        withSampleIndex()(idsFound(_, "食?る")).map(ids => assertTrue(ids.contains("1")))
      },
      test("finds a katakana-only reading (no hiragana form given) via the exact katakana query") {
        withSampleIndex()(idsFound(_, "コーヒー")).map(ids => assertTrue(ids.contains("4")))
      },
      test("finds a katakana-only reading via its hiragana equivalent, with no hiragana form indexed at all") {
        // No hiragana reading is stored for this entry, only "コーヒー". It works because normalization is
        // applied on both sides: the reading folds to "こおひい" at index time and the query folds to the same
        // "こおひい" at query time, so they meet in the middle.
        withSampleIndex()(idsFound(_, "こおひい")).map(ids => assertTrue(ids.contains("4")))
      },
      test("finds a katakana-only reading via its kanji spelling") {
        withSampleIndex()(idsFound(_, "珈琲")).map(ids => assertTrue(ids.contains("4")))
      },
      test(
        "ranks a higher-priority entry first even when it has more alternate kanji forms than a " +
          "lower-priority competitor for the same word"
      ) {
        withSampleIndex()(idsFound(_, "類い")).map { ids =>
          assertTrue(ids.indexOf("5-high-priority-many-alternates") < ids.indexOf("6-low-priority-single-alternate"))
        }
      },
      test("does not find anything for a query matching no entry") {
        withSampleIndex()(idsFound(_, "存在しない架空の言葉")).map(ids => assertTrue(ids.isEmpty))
      }
    ) +
      suite("WordReader integration (DeinflectQuery strategy)")(
        test("finds an entry via a conjugated (inflected) form, without indexing any inflected forms") {
          withSampleIndex(WordSearchStrategy.DeinflectQuery)(idsFound(_, "食べた"))
            .map(ids => assertTrue(ids.contains("1")))
        },
        test("still finds an entry by its exact dictionary-form kanji/reading") {
          for {
            byKanji   <- withSampleIndex(WordSearchStrategy.DeinflectQuery)(idsFound(_, "食べる"))
            byReading <- withSampleIndex(WordSearchStrategy.DeinflectQuery)(idsFound(_, "たべる"))
          } yield assertTrue(byKanji.contains("1"), byReading.contains("1"))
        }
      )
}
