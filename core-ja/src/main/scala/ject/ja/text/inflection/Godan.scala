package ject.ja.text.inflection

import ject.ja.text.{DanShiftConjugator, GodanTaConjugator, GodanTeConjugator, ReversibleTransform, Transforms}
import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Syllabary.Dan
import ject.ja.text.Transformation.*

object Godan {

  // Endings attached after shifting the verb's final kana to a fixed dan within its own row, e.g. 書く -> 書け(ば).
  private val aDan = DanShiftConjugator(Dan.A)
  private val eDan = DanShiftConjugator(Dan.E)
  private val iDan = DanShiftConjugator(Dan.I)
  private val oDan = DanShiftConjugator(Dan.O)

  private val conjugations: Map[Form, ReversibleTransform] = Map(
    // Plain
    Past.plain -> GodanTaConjugator(""),
    Te.plain -> GodanTeConjugator(""),
    Conditional.plain -> GodanTaConjugator("ら"),
    Provisional.plain -> eDan("ば"),
    Potential.plain -> eDan("る"),
    Passive.plain -> aDan("れる"),
    Causative.plain -> aDan("せる"),
    CausativePassive.plain -> aDan("せられる"),
    Volitional.plain -> oDan("う"),
    Alternative.plain -> GodanTaConjugator("り"),
    Imperative.plain -> eDan(""),
    Sou.plain -> iDan("そう"),
    Tai.plain -> iDan("たい"),
    Progressive.plain -> GodanTeConjugator("いる", "る"),
    Form.of(Progressive, Past) -> GodanTeConjugator("いた", "た"),
    Form.of(Potential, Te) -> eDan("て"),
    Form.of(Potential, Past) -> eDan("た"),
    Form.of(Passive, Stem) -> aDan("れ"),
    Form.of(Passive, Te) -> aDan("れて"),
    Form.of(Passive, Past) -> aDan("れた"),
    Form.of(Causative, Te) -> aDan("せて"),
    Form.of(Causative, Past) -> aDan("せた"),
    Form.of(Tai, Te) -> iDan("たくて"),
    // Polite
    NonPast.polite -> iDan("ます"),
    Past.polite -> iDan("ました"),
    Te.polite -> GodanTeConjugator("まして"),
    Conditional.polite -> iDan("ましたら"),
    Provisional.polite -> iDan("ますなら"),
    Potential.polite -> eDan("ます"),
    Passive.polite -> aDan("れます"),
    Causative.polite -> aDan("せます"),
    CausativePassive.polite -> aDan("せられます"),
    Volitional.polite -> iDan("ましょう"),
    Alternative.polite -> iDan("ましたり"),
    Imperative.polite -> iDan("なさい"),
    Progressive.polite -> GodanTeConjugator("います", "ます"),
    Form.of(Progressive, Past).polite -> GodanTeConjugator("いました", "ました"),
    Form.of(Potential, Te).polite -> eDan("まして"),
    Form.of(Potential, Past).polite -> eDan("ました"),
    Form.of(Causative, Te).polite -> aDan("せまして"),
    Form.of(Causative, Past).polite -> aDan("せました"),
    // Negative
    NonPast.negative -> aDan("ない", "ぬ", "ず"),
    Past.negative -> aDan("なかった"),
    Te.negative -> aDan("なくて", "ないで"),
    Conditional.negative -> aDan("なかったら"),
    Provisional.negative -> aDan("なければ"),
    Potential.negative -> eDan("ない"),
    Passive.negative -> aDan("れない"),
    Causative.negative -> aDan("せない"),
    CausativePassive.negative -> aDan("せられない"),
    Alternative.negative -> aDan("なかったり"),
    Sou.negative -> iDan("なさそう"),
    Form.of(Potential, Sou).negative -> eDan("なさそう"),
    Tai.negative -> iDan("たくない"),
    Progressive.negative -> GodanTeConjugator("いない", "ない"),
    Form.of(Progressive, Past).negative -> GodanTeConjugator("いなかった", "なかった"),
    Form.of(Tai, Sou).negative -> iDan("たくなさそう"),
    Form.of(Potential, Te).negative -> eDan("なくて"),
    Form.of(Potential, Past).negative -> eDan("なかった"),
    Form.of(Causative, Te).negative -> aDan("せなくて"),
    Form.of(Causative, Past).negative -> aDan("せなかった"),
    // Polite negative
    NonPast.polite.negative -> iDan("ません"),
    Past.polite.negative -> iDan("ませんでした"),
    Te.polite.negative -> iDan("ませんで"),
    Conditional.polite.negative -> iDan("ませんでしたら"),
    Provisional.polite.negative -> iDan("ませんなら"),
    Potential.polite.negative -> eDan("ません"),
    Passive.polite.negative -> aDan("れません"),
    Causative.polite.negative -> aDan("せません"),
    CausativePassive.polite.negative -> aDan("せられません"),
    Volitional.polite.negative -> iDan("ますまい"),
    Alternative.polite.negative -> iDan("ませんでしたり"),
    Imperative.polite.negative -> iDan("なさるな"),
    Progressive.polite.negative -> GodanTeConjugator("いません", "ません"),
    Form.of(Progressive, Past).polite.negative -> GodanTeConjugator("いませんでした", "ませんでした"),
    Form.of(Potential, Te).polite.negative -> eDan("ませんでして"),
    Form.of(Potential, Past).polite.negative -> eDan("ませんでした"),
    Form.of(Causative, Te).polite.negative -> aDan("せませんでして"),
    Form.of(Causative, Past).polite.negative -> aDan("せませんでした"),
    // Other
    Form.of(Stem) -> iDan("")
  )

  // まい and な both attach directly to the plain dictionary form unlike every other ending above, so they can't be
  // expressed via the shared conjugators.
  private val volitionalNegative: ReversibleTransform =
    ReversibleTransform(
      forward = Transforms(attach("まい")),
      backward = Transforms(detach("まい"), ensureValidVerbEnding)
    )

  private val imperativeNegative: ReversibleTransform =
    ReversibleTransform(
      forward = Transforms(attach("な")),
      backward = Transforms(detach("な"), ensureValidVerbEnding)
    )

  private val specialCases: Map[Form, ReversibleTransform] = Map(
    Volitional.negative -> volitionalNegative,
    Imperative.negative -> imperativeNegative
  )

  val inflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity) ++
      (conjugations ++ specialCases).view.mapValues(_.forward)

  val deinflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms(ensureValidVerbEnding)) ++
      (conjugations ++ specialCases).view.mapValues(_.backward)
}
