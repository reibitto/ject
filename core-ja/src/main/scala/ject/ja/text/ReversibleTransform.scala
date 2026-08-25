package ject.ja.text

import ject.ja.text.Transformation.*

/** A conjugation rule declared once, as a single suffix (or set of candidate
  * suffixes), that yields both the forward inflection `Transform` and its
  * mirror-image backward deinflection `Transform`.
  */
final case class ReversibleTransform(forward: Transform, backward: Transform)

/** Builds `ReversibleTransform`s for a verb/adjective class whose forms are
  * produced by stripping a fixed `rootSuffix` (e.g. "る" for ichidan verbs, "する"
  * for suru verbs) off the dictionary form via `stem`, then attaching a
  * conjugated ending.
  */
final case class SuffixConjugator(stem: Transform, rootSuffix: String) {

  def apply(suffix: String, suffixes: String*): ReversibleTransform =
    ReversibleTransform(
      forward = Transforms(stem, attach(suffix, suffixes*)),
      backward = Transforms(detach((suffix +: suffixes)*), attach(rootSuffix))
    )
}

/** Builds `ReversibleTransform`s for godan endings formed by shifting the
  * verb's final kana to `dan` within its own row (e.g. 書く -> 書け for the E-dan)
  * and attaching a suffix directly, with no connecting て/だ mora.
  */
final case class DanShiftConjugator(dan: Syllabary.Dan) {

  def apply(suffix: String, suffixes: String*): ReversibleTransform =
    ReversibleTransform(
      forward = Transforms(changeBase(dan, suffix, suffixes*)),
      backward = Transforms(detach((suffix +: suffixes)*), shiftBase(dan, Syllabary.Dan.U))
    )
}

/** Builds `ReversibleTransform`s for godan endings built on top of the euphonic
  * て/で-form (e.g. 書いて, 泳いで) via `godanStemTe`, such as the progressive ている/てる
  * family. The backward direction detaches the given ending to recover the
  * て/で-form, then reconstructs the dictionary form from it via
  * `attachGodanStem`.
  */
object GodanTeConjugator {

  def apply(suffix: String, suffixes: String*): ReversibleTransform =
    ReversibleTransform(
      forward = Transforms(godanStemTe, attach(suffix, suffixes*)),
      backward = Transforms(detach((suffix +: suffixes)*), attachGodanStem("て", "で"))
    )
}

/** Same as `GodanTeConjugator`, but for endings built on top of the euphonic
  * た/だ-form (e.g. 書いた, 泳いだ) via `godanStemTa`, such as plain past.
  */
object GodanTaConjugator {

  def apply(suffix: String, suffixes: String*): ReversibleTransform =
    ReversibleTransform(
      forward = Transforms(godanStemTa, attach(suffix, suffixes*)),
      backward = Transforms(detach((suffix +: suffixes)*), attachGodanStem("た", "だ"))
    )
}
