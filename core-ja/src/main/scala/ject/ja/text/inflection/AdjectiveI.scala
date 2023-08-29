package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object AdjectiveI {

  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(adjectiveIStem, attach("かった")),
    Te.plain -> Transforms(adjectiveIStem, attach("くて")),
    Conditional.plain -> Transforms(adjectiveIStem, attach("かったら")),
    Provisional.plain -> Transforms(adjectiveIStem, attach("ければ")),
    Alternative.plain -> Transforms(adjectiveIStem, attach("かったり")),
    Sou.plain -> Transforms(adjectiveIStem, attach("そう")),
    Adverb.plain -> Transforms(adjectiveIStem, attach("く")),
    Ki.plain -> Transforms(adjectiveIStem, attach("き")),
    // Negative
    NonPast.negative -> Transforms(adjectiveIStem, attach("くない")),
    Past.negative -> Transforms(adjectiveIStem, attach("くなかった")),
    Te.negative -> Transforms(adjectiveIStem, attach("くなくて")),
    Conditional.negative -> Transforms(adjectiveIStem, attach("くなかったら")),
    Provisional.negative -> Transforms(adjectiveIStem, attach("くなければ")),
    Alternative.negative -> Transforms(adjectiveIStem, attach("くなかったり")),
    Sou.negative -> Transforms(adjectiveIStem, attach("くなさそう")),
    Adverb.negative -> Transforms(adjectiveIStem, attach("くなく")),
    // Other
    Noun.plain -> Transforms(adjectiveIStem, attach("さ"))
  )

  val deinflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain -> Transforms.identity,
    Past.plain -> Transforms(detach("かった"), attach("い")),
    Te.plain -> Transforms(detach("くて"), attach("い")),
    Conditional.plain -> Transforms(detach("かったら"), attach("い")),
    Provisional.plain -> Transforms(detach("ければ"), attach("い")),
    Alternative.plain -> Transforms(detach("かったり"), attach("い")),
    Sou.plain -> Transforms(detach("そう"), attach("い")),
    Adverb.plain -> Transforms(detach("く"), attach("い")),
    Ki.plain -> Transforms(detach("き"), attach("い")),
    // Negative
    NonPast.negative -> Transforms(detach("くない"), attach("い")),
    Past.negative -> Transforms(detach("くなかった"), attach("い")),
    Te.negative -> Transforms(detach("くなくて"), attach("い")),
    Conditional.negative -> Transforms(detach("くなかったら"), attach("い")),
    Provisional.negative -> Transforms(detach("くなければ"), attach("い")),
    Alternative.negative -> Transforms(detach("くなかったり"), attach("い")),
    Sou.negative -> Transforms(detach("くなさそう"), attach("い")),
    Adverb.negative -> Transforms(detach("くなく"), attach("い")),
    // Other
    Noun.plain -> Transforms(detach("さ"), attach("い"))
  )
}
