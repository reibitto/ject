package ject.ja

import ject.ja.text.{Deinflection, Form, WordType}
import ject.ja.text.inflection.{AdjectiveI, Aru, Godan, Ichidan, Iku, Suru}
import ject.ja.text.Transformation.Transform
import zio.*
import zio.test.*

object InflectionSpec extends ZIOSpecDefault {

  // A small sample of common hiragana used to generate synthetic "stems" for property-based round-trip tests.
  // These don't need to be real dictionary words; the property under test is purely algebraic (inflect then
  // deinflect recovers the original string), independent of whether the word actually exists.
  private val hiraganaChars: Seq[Char] =
    Seq(
      'あ', 'い', 'う', 'え', 'お', 'か', 'き', 'く', 'け', 'こ', 'さ', 'し', 'す', 'せ', 'そ', 'た', 'ち', 'つ', 'て', 'と', 'な', 'に', 'ぬ',
      'ね', 'の', 'は', 'ひ', 'ふ', 'へ', 'ほ', 'ま', 'み', 'む', 'め', 'も', 'や', 'ゆ', 'よ', 'ら', 'り', 'る', 'れ', 'ろ', 'わ', 'が', 'ぎ',
      'ぐ', 'げ', 'ご', 'ざ', 'じ', 'ず', 'ぜ', 'ぞ', 'だ', 'ぢ', 'づ', 'で', 'ど', 'ば', 'び', 'ぶ', 'べ', 'ぼ', 'ぱ', 'ぴ', 'ぷ', 'ぺ', 'ぽ'
    )

  /** Generates a random dictionary-form word for a word type by appending its
    * fixed ending (e.g. "る" for ichidan) to a random 1-3 mora stem.
    */
  private def wordGen(ending: String): Gen[Any, String] =
    Gen.stringBounded(1, 3)(Gen.elements(hiraganaChars*)).map(_ + ending)

  /** Inflects `word` into `form` and asserts that deinflecting the result
    * (using the same targeted form) yields `word` among its candidates. This is
    * the core round-trip property: inflection and deinflection must agree on
    * every declared form.
    */
  private def checkRoundTrip(wordType: WordType, form: Form, transform: Transform, word: String): TestResult =
    transform(word) match {
      case Left(error) =>
        assertTrue(false).label(s"'$word' failed to inflect for $form: $error")

      case Right(inflectedForms) =>
        inflectedForms.map { inflected =>
          val deinflectedCandidates =
            Deinflection.deinflect(inflected, wordType, form).map(_.toChunk).getOrElse(Chunk.empty)

          assertTrue(deinflectedCandidates.contains(word))
            .label(s"'$word' -> '$inflected' in (${deinflectedCandidates.mkString(", ")})")
        }.reduce(_ && _)
    }

  private def exampleSuite(
      name: String,
      wordType: WordType,
      testWord: String,
      inflections: Map[Form, Transform]
  ) =
    suite(name)(
      inflections.map { case (form, transform) =>
        test(form.render)(checkRoundTrip(wordType, form, transform, testWord))
      }.toSeq*
    )

  private def propertySuite(
      name: String,
      wordType: WordType,
      generator: Gen[Any, String],
      inflections: Map[Form, Transform]
  ) =
    suite(name)(
      inflections.map { case (form, transform) =>
        test(form.render) {
          check(generator)(word => checkRoundTrip(wordType, form, transform, word))
        }
      }.toSeq*
    )

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("InflectionSpec")(
      suite("inflection and deinflection works both ways (examples)")(
        exampleSuite("ichidan", WordType.VerbIchidan, "食べる", Ichidan.inflections),
        exampleSuite("godan", WordType.VerbGodan, "書く", Godan.inflections),
        exampleSuite("suru", WordType.VerbSuru, "勉強する", Suru.inflections),
        exampleSuite("aru", WordType.VerbAru, "ある", Aru.inflections),
        exampleSuite("iku", WordType.VerbIku, "行く", Iku.inflections),
        exampleSuite("adjective-i", WordType.AdjectiveI, "高い", AdjectiveI.inflections)
      ),
      suite("inflection and deinflection works both ways (generated words)")(
        propertySuite("ichidan", WordType.VerbIchidan, wordGen("る"), Ichidan.inflections),
        propertySuite("godan", WordType.VerbGodan, wordGen("く"), Godan.inflections),
        propertySuite("suru", WordType.VerbSuru, wordGen("する"), Suru.inflections),
        propertySuite("aru", WordType.VerbAru, wordGen("ある"), Aru.inflections),
        propertySuite("iku", WordType.VerbIku, wordGen("く"), Iku.inflections),
        propertySuite("adjective-i", WordType.AdjectiveI, wordGen("い"), AdjectiveI.inflections)
      )
    )
}
