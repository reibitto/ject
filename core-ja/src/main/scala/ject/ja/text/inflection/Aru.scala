package ject.ja.text.inflection

import ject.ja.text.{ReversibleTransform, SuffixConjugator, Transforms}
import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*

object Aru {

  private val conjugate = SuffixConjugator(stemOf("ある", allowEmptyStem = true), "ある")

  private val conjugations: Map[Form, ReversibleTransform] = Map(
    // Plain
    Past.plain -> conjugate("あった"),
    Te.plain -> conjugate("あって"),
    Conditional.plain -> conjugate("あったら"),
    Provisional.plain -> conjugate("あれば"),
    Volitional.plain -> conjugate("あろう"),
    Alternative.plain -> conjugate("あったり"),
    Sou.plain -> conjugate("ありそう"),
    // Polite
    NonPast.polite -> conjugate("あります"),
    Past.polite -> conjugate("ありました"),
    Te.polite -> conjugate("ありまして"),
    Conditional.polite -> conjugate("ありましたら"),
    Provisional.polite -> conjugate("ありますなら"),
    Volitional.polite -> conjugate("ありましょう"),
    Alternative.polite -> conjugate("ありましたり"),
    // Negative
    NonPast.negative -> conjugate("ない"),
    Past.negative -> conjugate("なかった"),
    Te.negative -> conjugate("なくて"),
    Conditional.negative -> conjugate("なかったら"),
    Provisional.negative -> conjugate("なければ"),
    Volitional.negative -> conjugate("あるまい"),
    Alternative.negative -> conjugate("なかったり"),
    Sou.negative -> conjugate("なさそう"),
    // Polite negative
    NonPast.polite.negative -> conjugate("ありません"),
    Past.polite.negative -> conjugate("ありませんでした"),
    Te.polite.negative -> conjugate("ありませんで"),
    Conditional.polite.negative -> conjugate("ありませんでしたら"),
    Provisional.polite.negative -> conjugate("ありませんなら"),
    Volitional.polite.negative -> conjugate("ありますまい"),
    Alternative.polite.negative -> conjugate("ありませんでしたり")
  )

  val inflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity) ++ conjugations.view.mapValues(_.forward)

  val deinflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity) ++ conjugations.view.mapValues(_.backward)
}
