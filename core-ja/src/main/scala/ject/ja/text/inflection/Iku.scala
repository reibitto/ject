package ject.ja.text.inflection

import ject.ja.text.SubForm._
import ject.ja.text.Transformation._
import ject.ja.text.Form
import ject.ja.text.Transforms

object Iku {
  val inflections: Map[Form, Transform] = Map(
    // Plain
    NonPast.plain                            -> Transforms.identity,
    Past.plain                               -> Transforms.pure("行った"),
    Te.plain                                 -> Transforms.pure("行って"),
    Conditional.plain                        -> Transforms.pure("行ったら"),
    Provisional.plain                        -> Transforms.pure("行けば"),
    Potential.plain                          -> Transforms.pure("行ける"),
    Passive.plain                            -> Transforms.pure("行かれる"),
    Causative.plain                          -> Transforms.pure("行かせる", "行かす"),
    CausativePassive.plain                   -> Transforms.pure("行かせられる"),
    Volitional.plain                         -> Transforms.pure("行こう"),
    Alternative.plain                        -> Transforms.pure("行ったり"),
    Imperative.plain                         -> Transforms.pure("行け"),
    Sou.plain                                -> Transforms.pure("行きそう"),
    Tai.plain                                -> Transforms.pure("行きたい"),
    Form.of(Past, Potential)                 -> Transforms.pure("行けた"),
    // Polite
    NonPast.polite                           -> Transforms.pure("行きます"),
    Past.polite                              -> Transforms.pure("行きました"),
    Te.polite                                -> Transforms.pure("行きまして"),
    Conditional.polite                       -> Transforms.pure("行きましたら"),
    Provisional.polite                       -> Transforms.pure("行きますなら"),
    Potential.polite                         -> Transforms.pure("行けます"),
    Passive.polite                           -> Transforms.pure("行かれます"),
    Causative.polite                         -> Transforms.pure("行かせます", "行かします"),
    CausativePassive.polite                  -> Transforms.pure("行かせられます"),
    Volitional.polite                        -> Transforms.pure("行きましょう"),
    Alternative.polite                       -> Transforms.pure("行きましたり"),
    Imperative.polite                        -> Transforms.pure("行きなさい"),
    Form.of(Past, Potential).polite          -> Transforms.pure("行けました"),
    // Negative
    NonPast.negative                         -> Transforms.pure("行かない", "行かぬ", "行かず"),
    Past.negative                            -> Transforms.pure("行かなかった"),
    Te.negative                              -> Transforms.pure("行かなくて", "行かないで"),
    Conditional.negative                     -> Transforms.pure("行かなかったら"),
    Provisional.negative                     -> Transforms.pure("行かなければ"),
    Potential.negative                       -> Transforms.pure("行けない"),
    Passive.negative                         -> Transforms.pure("行かれない"),
    Causative.negative                       -> Transforms.pure("行かせない", "行かさない"),
    CausativePassive.negative                -> Transforms.pure("行かせられない"),
    Volitional.negative                      -> Transforms.pure("行くまい"),
    Alternative.negative                     -> Transforms.pure("行かなかったり"),
    Imperative.negative                      -> Transforms.pure("行くな"),
    Sou.negative                             -> Transforms.pure("行かなそう"),
    Form.of(Potential, Sou).negative         -> Transforms.pure("行けなそう"),
    Tai.negative                             -> Transforms.pure("行きたくない"),
    Form.of(Tai, Sou).negative               -> Transforms.pure("行きたくなさそう"),
    Form.of(Past, Potential).negative        -> Transforms.pure("行けなかった"),
    // Polite negative
    NonPast.polite.negative                  -> Transforms.pure("行きません"),
    Past.polite.negative                     -> Transforms.pure("行きませんでした"),
    Te.polite.negative                       -> Transforms.pure("行きませんで"),
    Conditional.polite.negative              -> Transforms.pure("行きませんでしたら"),
    Provisional.polite.negative              -> Transforms.pure("行きませんなら"),
    Potential.polite.negative                -> Transforms.pure("行けません"),
    Passive.polite.negative                  -> Transforms.pure("行かれません"),
    Causative.polite.negative                -> Transforms.pure("行かせません", "行かしません"),
    CausativePassive.polite.negative         -> Transforms.pure("行かせられません"),
    Volitional.polite.negative               -> Transforms.pure("行きますまい"),
    Alternative.polite.negative              -> Transforms.pure("行きませんでしたり"),
    Imperative.polite.negative               -> Transforms.pure("行きなさるな"),
    Form.of(Past, Potential).polite.negative -> Transforms.pure("行けませんでした")
  )

  val deinflections: Map[Form, Transform] = Map.empty // TODO: Implement
}
