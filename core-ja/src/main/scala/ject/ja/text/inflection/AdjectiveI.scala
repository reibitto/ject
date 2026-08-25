package ject.ja.text.inflection

import ject.ja.text.{ReversibleTransform, SuffixConjugator, Transforms}
import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*

object AdjectiveI {

  private val conjugate = SuffixConjugator(adjectiveIStem, "い")

  private val conjugations: Map[Form, ReversibleTransform] = Map(
    // Plain
    Past.plain -> conjugate("かった"),
    Te.plain -> conjugate("くて"),
    Ge.plain -> conjugate("げ"),
    Conditional.plain -> conjugate("かったら"),
    Provisional.plain -> conjugate("ければ"),
    Alternative.plain -> conjugate("かったり"),
    Sou.plain -> conjugate("そう"),
    Adverb.plain -> conjugate("く"),
    Ki.plain -> conjugate("き"),
    // Negative
    NonPast.negative -> conjugate("くない"),
    Past.negative -> conjugate("くなかった"),
    Te.negative -> conjugate("くなくて"),
    Conditional.negative -> conjugate("くなかったら"),
    Provisional.negative -> conjugate("くなければ"),
    Alternative.negative -> conjugate("くなかったり"),
    Sou.negative -> conjugate("くなさそう"),
    Adverb.negative -> conjugate("くなく"),
    // Other
    Noun.plain -> conjugate("さ")
  )

  val inflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity) ++ conjugations.view.mapValues(_.forward)

  val deinflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity) ++ conjugations.view.mapValues(_.backward)
}
