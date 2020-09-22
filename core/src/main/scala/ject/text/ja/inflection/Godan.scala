package ject.text.ja.inflection

import ject.text.ja.Form
import ject.text.ja.SubForm._
import ject.text.ja.Syllabary.Dan
import ject.text.ja.Transformation._

object Godan {
  val inflections: Map[Form, Transforms] = Map(
    // Plain
    NonPast.plain                            -> Seq.empty,
    Past.plain                               -> Seq(godanStem, attach("た")),
    Te.plain                                 -> Seq(godanStem, attach("て")),
    Conditional.plain                        -> Seq(godanStem, attach("たら")),
    Provisional.plain                        -> Seq(changeBase(Dan.E, "ば")),
    Potential.plain                          -> Seq(changeBase(Dan.E, "る")),
    Passive.plain                            -> Seq(changeBase(Dan.A, "れる")),
    Causative.plain                          -> Seq(changeBase(Dan.A, "せる")),
    CausativePassive.plain                   -> Seq(changeBase(Dan.A, "せられる")),
    Volitional.plain                         -> Seq(changeBase(Dan.O, "う")),
    Alternative.plain                        -> Seq(godanStem, attach("たり")),
    Imperative.plain                         -> Seq(changeBase(Dan.E, "")),
    Sou.plain                                -> Seq(changeBase(Dan.I, "そう")),
    Tai.plain                                -> Seq(changeBase(Dan.I, "たい")),
    Form.of(Past, Potential)                 -> Seq(changeBase(Dan.E, "た")),
    // Polite
    NonPast.polite                           -> Seq(changeBase(Dan.I, "ます")),
    Past.polite                              -> Seq(changeBase(Dan.I, "ました")),
    Te.polite                                -> Seq(changeBase(Dan.I, "まして")),
    Conditional.polite                       -> Seq(changeBase(Dan.I, "ましたら")),
    Provisional.polite                       -> Seq(changeBase(Dan.I, "ますなら")),
    Potential.polite                         -> Seq(changeBase(Dan.E, "ます")),
    Passive.polite                           -> Seq(changeBase(Dan.A, "れます")),
    Causative.polite                         -> Seq(changeBase(Dan.A, "せます")),
    CausativePassive.polite                  -> Seq(changeBase(Dan.A, "せられます")),
    Volitional.polite                        -> Seq(changeBase(Dan.I, "ましょう")),
    Alternative.polite                       -> Seq(changeBase(Dan.I, "ましたり")),
    Imperative.polite                        -> Seq(changeBase(Dan.I, "なさい")),
    Form.of(Past, Potential).polite          -> Seq(changeBase(Dan.E, "ました")),
    // Negative
    NonPast.negative                         -> Seq(changeBase(Dan.A, "ない")),
    Past.negative                            -> Seq(changeBase(Dan.A, "なかった")),
    Te.negative                              -> Seq(changeBase(Dan.A, "なくて")),
    Conditional.negative                     -> Seq(changeBase(Dan.A, "なかったら")),
    Provisional.negative                     -> Seq(changeBase(Dan.A, "なければ")),
    Potential.negative                       -> Seq(changeBase(Dan.E, "ない")),
    Passive.negative                         -> Seq(changeBase(Dan.A, "れない")),
    Causative.negative                       -> Seq(changeBase(Dan.A, "せない")),
    CausativePassive.negative                -> Seq(changeBase(Dan.A, "せられない")),
    Volitional.negative                      -> Seq(attach("まい")),
    Alternative.negative                     -> Seq(changeBase(Dan.A, "なかったり")),
    Imperative.negative                      -> Seq(attach("な")),
    Sou.negative                             -> Seq(changeBase(Dan.I, "なさそう")),
    Form.of(Potential, Sou).negative         -> Seq(changeBase(Dan.E, "なさそう")),
    Tai.negative                             -> Seq(changeBase(Dan.I, "たくない")),
    Form.of(Tai, Sou).negative               -> Seq(changeBase(Dan.I, "たくなさそう")),
    Form.of(Past, Potential).negative        -> Seq(changeBase(Dan.E, "なかった")),
    // Polite negative
    NonPast.polite.negative                  -> Seq(changeBase(Dan.I, "ません")),
    Past.polite.negative                     -> Seq(changeBase(Dan.I, "ませんでした")),
    Te.polite.negative                       -> Seq(changeBase(Dan.I, "ませんで")),
    Conditional.polite.negative              -> Seq(changeBase(Dan.I, "ませんでしたら")),
    Provisional.polite.negative              -> Seq(changeBase(Dan.I, "ませんなら")),
    Potential.polite.negative                -> Seq(changeBase(Dan.E, "ません")),
    Passive.polite.negative                  -> Seq(changeBase(Dan.A, "れません")),
    Causative.polite.negative                -> Seq(changeBase(Dan.A, "せません")),
    CausativePassive.polite.negative         -> Seq(changeBase(Dan.A, "せられません")),
    Volitional.polite.negative               -> Seq(changeBase(Dan.I, "ますまい")),
    Alternative.polite.negative              -> Seq(changeBase(Dan.I, "ませんでしたり")),
    Imperative.polite.negative               -> Seq(changeBase(Dan.I, "なさるな")),
    Form.of(Past, Potential).polite.negative -> Seq(changeBase(Dan.E, "ませんでした"))
  )
}
