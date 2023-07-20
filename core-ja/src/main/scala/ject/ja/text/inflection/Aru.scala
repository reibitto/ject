package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object Aru {

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> attach("あった"),
    Te.plain -> attach("あって"),
    Conditional.plain -> attach("あったら"),
    Provisional.plain -> attach("あれば"),
    Volitional.plain -> attach("あろう"),
    Alternative.plain -> attach("あったり"),
    Sou.plain -> attach("ありそう"),
    // Polite
    NonPast.polite -> attach("あります"),
    Past.polite -> attach("ありました"),
    Te.polite -> attach("ありまして"),
    Conditional.polite -> attach("ありましたら"),
    Provisional.polite -> attach("ありますなら"),
    Volitional.polite -> attach("ありましょう"),
    Alternative.polite -> attach("ありましたり"),
    // Negative
    NonPast.negative -> attach("ない"),
    Past.negative -> attach("なかった"),
    Te.negative -> attach("なくて"),
    Conditional.negative -> attach("なかったら"),
    Provisional.negative -> attach("なければ"),
    Volitional.negative -> attach("あるまい"),
    Alternative.negative -> attach("なかったり"),
    Sou.negative -> attach("なさそう"),
    // Polite negative
    NonPast.polite.negative -> attach("ありません"),
    Past.polite.negative -> attach("ありませんでした"),
    Te.polite.negative -> attach("ありませんで"),
    Conditional.polite.negative -> attach("ありませんでしたら"),
    Provisional.polite.negative -> attach("ありませんなら"),
    Volitional.polite.negative -> attach("ありますまい"),
    Alternative.polite.negative -> attach("ありませんでしたり")
  )

  val deinflections: Map[Form, Transform] = Map.empty // TODO: Implement
}
