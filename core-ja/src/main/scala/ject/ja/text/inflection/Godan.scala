package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Syllabary.Dan
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms
import zio.NonEmptyChunk

object Godan {

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(godanStem, attach("た")),
    Te.plain -> Transforms.withInitial { s =>
      NonEmptyChunk(
        godanStem,
        if (s.endsWith("ぐ") || s.endsWith("む") || s.endsWith("ぬ") || s.endsWith("ぶ"))
          attach("で")
        else
          attach("て")
      )
    },
    Conditional.plain -> Transforms(godanStem, attach("たら")),
    Provisional.plain -> Transforms(changeBase(Dan.E, "ば")),
    Potential.plain -> Transforms(changeBase(Dan.E, "る")),
    Passive.plain -> Transforms(changeBase(Dan.A, "れる")),
    Causative.plain -> Transforms(changeBase(Dan.A, "せる")),
    CausativePassive.plain -> Transforms(changeBase(Dan.A, "せられる")),
    Volitional.plain -> Transforms(changeBase(Dan.O, "う")),
    Alternative.plain -> Transforms(godanStem, attach("たり")),
    Imperative.plain -> Transforms(changeBase(Dan.E, "")),
    Sou.plain -> Transforms(changeBase(Dan.I, "そう")),
    Tai.plain -> Transforms(changeBase(Dan.I, "たい")),
    Progressive.plain -> Transforms(changeBase(Dan.I, "ている", "てる")),
    Form.of(Progressive, Past) -> Transforms(changeBase(Dan.E, "ていた", "てた")),
    Form.of(Potential, Te) -> Transforms(changeBase(Dan.E, "て")),
    Form.of(Potential, Past) -> Transforms(changeBase(Dan.E, "た")),
    Form.of(Passive, Stem) -> Transforms(changeBase(Dan.A, "れ")),
    Form.of(Passive, Te) -> Transforms(changeBase(Dan.A, "れて")),
    Form.of(Passive, Past) -> Transforms(changeBase(Dan.A, "れた")),
    Form.of(Tai, Te) -> Transforms(changeBase(Dan.I, "たくて")),
    // Polite
    NonPast.polite -> Transforms(changeBase(Dan.I, "ます")),
    Past.polite -> Transforms(changeBase(Dan.I, "ました")),
    Te.polite -> Transforms(changeBase(Dan.I, "まして")),
    Conditional.polite -> Transforms(changeBase(Dan.I, "ましたら")),
    Provisional.polite -> Transforms(changeBase(Dan.I, "ますなら")),
    Potential.polite -> Transforms(changeBase(Dan.E, "ます")),
    Passive.polite -> Transforms(changeBase(Dan.A, "れます")),
    Causative.polite -> Transforms(changeBase(Dan.A, "せます")),
    CausativePassive.polite -> Transforms(changeBase(Dan.A, "せられます")),
    Volitional.polite -> Transforms(changeBase(Dan.I, "ましょう")),
    Alternative.polite -> Transforms(changeBase(Dan.I, "ましたり")),
    Imperative.polite -> Transforms(changeBase(Dan.I, "なさい")),
    Progressive.polite -> Transforms(changeBase(Dan.I, "ています", "てます")),
    Form.of(Potential, Past).polite -> Transforms(changeBase(Dan.E, "ていました", "てました")),
    Form.of(Potential, Te).polite -> Transforms(changeBase(Dan.E, "まして")),
    Form.of(Potential, Past).polite -> Transforms(changeBase(Dan.E, "ました")),
    // Negative
    NonPast.negative -> Transforms(changeBase(Dan.A, "ない", "ぬ", "ず")),
    Past.negative -> Transforms(changeBase(Dan.A, "なかった")),
    Te.negative -> Transforms(changeBase(Dan.A, "なくて", "ないで")),
    Conditional.negative -> Transforms(changeBase(Dan.A, "なかったら")),
    Provisional.negative -> Transforms(changeBase(Dan.A, "なければ")),
    Potential.negative -> Transforms(changeBase(Dan.E, "ない")),
    Passive.negative -> Transforms(changeBase(Dan.A, "れない")),
    Causative.negative -> Transforms(changeBase(Dan.A, "せない")),
    CausativePassive.negative -> Transforms(changeBase(Dan.A, "せられない")),
    Volitional.negative -> Transforms(attach("まい")),
    Alternative.negative -> Transforms(changeBase(Dan.A, "なかったり")),
    Imperative.negative -> Transforms(attach("な")),
    Sou.negative -> Transforms(changeBase(Dan.I, "なさそう")),
    Form.of(Potential, Sou).negative -> Transforms(changeBase(Dan.E, "なさそう")),
    Tai.negative -> Transforms(changeBase(Dan.I, "たくない")),
    Progressive.negative -> Transforms(changeBase(Dan.I, "ていない", "てない")),
    Form.of(Progressive, Past).negative -> Transforms(changeBase(Dan.I, "ていなかった", "てなかった")),
    Form.of(Tai, Sou).negative -> Transforms(changeBase(Dan.I, "たくなさそう")),
    Form.of(Potential, Te).negative -> Transforms(changeBase(Dan.E, "なくて")),
    Form.of(Potential, Past).negative -> Transforms(changeBase(Dan.E, "なかった")),
    // Polite negative
    NonPast.polite.negative -> Transforms(changeBase(Dan.I, "ません")),
    Past.polite.negative -> Transforms(changeBase(Dan.I, "ませんでした")),
    Te.polite.negative -> Transforms(changeBase(Dan.I, "ませんで")),
    Conditional.polite.negative -> Transforms(changeBase(Dan.I, "ませんでしたら")),
    Provisional.polite.negative -> Transforms(changeBase(Dan.I, "ませんなら")),
    Potential.polite.negative -> Transforms(changeBase(Dan.E, "ません")),
    Passive.polite.negative -> Transforms(changeBase(Dan.A, "れません")),
    Causative.polite.negative -> Transforms(changeBase(Dan.A, "せません")),
    CausativePassive.polite.negative -> Transforms(changeBase(Dan.A, "せられません")),
    Volitional.polite.negative -> Transforms(changeBase(Dan.I, "ますまい")),
    Alternative.polite.negative -> Transforms(changeBase(Dan.I, "ませんでしたり")),
    Imperative.polite.negative -> Transforms(changeBase(Dan.I, "なさるな")),
    Progressive.polite.negative -> Transforms(changeBase(Dan.I, "ていません", "てません")),
    Form.of(Progressive, Past).polite.negative -> Transforms(changeBase(Dan.E, "ていませんでした", "てませんでした")),
    Form.of(Potential, Te).polite.negative -> Transforms(changeBase(Dan.E, "ませんでして")),
    Form.of(Potential, Past).polite.negative -> Transforms(changeBase(Dan.E, "ませんでした")),
    // Other
    Form.of(Stem) -> Transforms(changeBase(Dan.I, ""))
  )

  val deinflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms(ensureValidVerbEnding),
    Past.plain -> Transforms(attachGodanStem("た", "だ")),
    Te.plain -> Transforms(attachGodanStem("て", "で")),
    Conditional.plain -> Transforms(attachGodanStem("たら", "だら")),
    Provisional.plain -> Transforms(detach("ば"), shiftBase(Dan.E, Dan.U)),
    Potential.plain -> Transforms(detach("る"), shiftBase(Dan.E, Dan.U)),
    Passive.plain -> Transforms(detach("れる"), shiftBase(Dan.A, Dan.U)),
    Causative.plain -> Transforms(detach("せる"), shiftBase(Dan.A, Dan.U)),
    CausativePassive.plain -> Transforms(detach("せられる"), shiftBase(Dan.A, Dan.U)),
    Volitional.plain -> Transforms(detach("う"), shiftBase(Dan.O, Dan.U)),
    Alternative.plain -> Transforms(detach("たり", "だり"), shiftBase(Dan.E, Dan.U)),
    Imperative.plain -> Transforms(shiftBase(Dan.E, Dan.U)),
    Sou.plain -> Transforms(detach("そう"), shiftBase(Dan.I, Dan.U)),
    Tai.plain -> Transforms(detach("たい"), shiftBase(Dan.I, Dan.U)),
    Form.of(Potential, Te) -> Transforms(detach("て"), shiftBase(Dan.I, Dan.U)),
    Form.of(Potential, Past) -> Transforms(detach("た"), shiftBase(Dan.I, Dan.U)),
    Form.of(Passive, Stem) -> Transforms(detach("れ"), shiftBase(Dan.A, Dan.U)),
    Form.of(Passive, Te) -> Transforms(detach("れて"), shiftBase(Dan.A, Dan.U)),
    Form.of(Passive, Past) -> Transforms(detach("れた"), shiftBase(Dan.A, Dan.U)),
    Form.of(Tai, Te) -> Transforms(detach("たくて"), shiftBase(Dan.I, Dan.U)),
    // Polite
    NonPast.polite -> Transforms(detach("ます"), shiftBase(Dan.I, Dan.U)),
    Past.polite -> Transforms(detach("ました"), shiftBase(Dan.I, Dan.U)),
    Te.polite -> Transforms(detach("まして"), shiftBase(Dan.I, Dan.U)),
    Conditional.polite -> Transforms(detach("ましたら"), shiftBase(Dan.I, Dan.U)),
    Provisional.polite -> Transforms(detach("ますなら"), shiftBase(Dan.I, Dan.U)),
    Potential.polite -> Transforms(detach("ます"), shiftBase(Dan.E, Dan.U)),
    Passive.polite -> Transforms(detach("れます"), shiftBase(Dan.A, Dan.U)),
    Causative.polite -> Transforms(detach("せます"), shiftBase(Dan.A, Dan.U)),
    CausativePassive.polite -> Transforms(detach("せられます"), shiftBase(Dan.A, Dan.U)),
    Volitional.polite -> Transforms(detach("ましょう"), shiftBase(Dan.I, Dan.U)),
    Alternative.polite -> Transforms(detach("ましたり"), shiftBase(Dan.I, Dan.U)),
    Imperative.polite -> Transforms(detach("なさい"), shiftBase(Dan.I, Dan.U)),
    Form.of(Potential, Te).polite -> Transforms(detach("まして"), shiftBase(Dan.E, Dan.U)),
    Form.of(Potential, Past).polite -> Transforms(detach("ました"), shiftBase(Dan.E, Dan.U)),
    // Negative
    NonPast.negative -> Transforms(detach("ない", "ぬ", "ず"), shiftBase(Dan.A, Dan.U)),
    Past.negative -> Transforms(detach("なかった"), shiftBase(Dan.A, Dan.U)),
    Te.negative -> Transforms(detach("なくて", "ないで"), shiftBase(Dan.A, Dan.U)),
    Conditional.negative -> Transforms(detach("なかったら"), shiftBase(Dan.A, Dan.U)),
    Provisional.negative -> Transforms(detach("なければ"), shiftBase(Dan.A, Dan.U)),
    Potential.negative -> Transforms(detach("ない"), shiftBase(Dan.E, Dan.U)),
    Passive.negative -> Transforms(detach("れない"), shiftBase(Dan.A, Dan.U)),
    Causative.negative -> Transforms(detach("せない"), shiftBase(Dan.A, Dan.U)),
    CausativePassive.negative -> Transforms(detach("せられない"), shiftBase(Dan.A, Dan.U)),
    Volitional.negative -> Transforms(detach("まい"), ensureValidVerbEnding),
    Alternative.negative -> Transforms(detach("なかったり"), shiftBase(Dan.A, Dan.U)),
    Imperative.negative -> Transforms(detach("な"), ensureValidVerbEnding),
    Sou.negative -> Transforms(detach("なさそう"), shiftBase(Dan.I, Dan.U)),
    Form.of(Potential, Sou).negative -> Transforms(detach("なさそう"), shiftBase(Dan.E, Dan.U)),
    Tai.negative -> Transforms(detach("たくない"), shiftBase(Dan.I, Dan.U)),
    Form.of(Tai, Sou).negative -> Transforms(detach("たくなさそう"), shiftBase(Dan.I, Dan.U)),
    Form.of(Potential, Te).negative -> Transforms(detach("なくて"), shiftBase(Dan.E, Dan.U)),
    Form.of(Potential, Past).negative -> Transforms(detach("なかった"), shiftBase(Dan.E, Dan.U)),
    // Polite negative
    NonPast.polite.negative -> Transforms(detach("ません"), shiftBase(Dan.I, Dan.U)),
    Past.polite.negative -> Transforms(detach("ませんでした"), shiftBase(Dan.I, Dan.U)),
    Te.polite.negative -> Transforms(detach("ませんで"), shiftBase(Dan.I, Dan.U)),
    Conditional.polite.negative -> Transforms(detach("ませんでしたら"), shiftBase(Dan.I, Dan.U)),
    Provisional.polite.negative -> Transforms(detach("ませんなら"), shiftBase(Dan.I, Dan.U)),
    Potential.polite.negative -> Transforms(detach("ません"), shiftBase(Dan.E, Dan.U)),
    Passive.polite.negative -> Transforms(detach("れません"), shiftBase(Dan.A, Dan.U)),
    Causative.polite.negative -> Transforms(detach("せません"), shiftBase(Dan.A, Dan.U)),
    CausativePassive.polite.negative -> Transforms(detach("せられません"), shiftBase(Dan.A, Dan.U)),
    Volitional.polite.negative -> Transforms(detach("ますまい"), shiftBase(Dan.I, Dan.U)),
    Alternative.polite.negative -> Transforms(detach("ませんでしたり"), shiftBase(Dan.I, Dan.U)),
    Imperative.polite.negative -> Transforms(detach("なさるな"), shiftBase(Dan.I, Dan.U)),
    Form.of(Potential, Te).polite.negative -> Transforms(detach("ませんでして"), shiftBase(Dan.E, Dan.U)),
    Form.of(Potential, Past).polite.negative -> Transforms(detach("ませんでした"), shiftBase(Dan.E, Dan.U)),
    // Other
    Form.of(Stem) -> Transforms(shiftBase(Dan.I, Dan.U))
  )
}
