package ject.text.ja.inflection

import ject.text.ja.Transformation._
import ject.text.ja.Form
import ject.text.ja.SubForm._

object Ichidan {
  val inflections: Map[Form, Transforms] = Map(
    // Plain
    NonPast.plain                            -> Seq.empty,
    Past.plain                               -> Seq(ichidanStem, attach("た")),
    Te.plain                                 -> Seq(ichidanStem, attach("て")),
    Conditional.plain                        -> Seq(ichidanStem, attach("たら")),
    Provisional.plain                        -> Seq(ichidanStem, attach("れば")),
    Potential.plain                          -> Seq(ichidanStem, attach("られる")),
    Passive.plain                            -> Seq(ichidanStem, attach("られる")),
    Causative.plain                          -> Seq(ichidanStem, attach("させる")),
    CausativePassive.plain                   -> Seq(ichidanStem, attach("させられる")),
    Volitional.plain                         -> Seq(ichidanStem, attach("よう")),
    Alternative.plain                        -> Seq(ichidanStem, attach("たり")),
    Imperative.plain                         -> Seq(ichidanStem, attach("ろ")),
    Sou.plain                                -> Seq(ichidanStem, attach("そう")),
    Tai.plain                                -> Seq(ichidanStem, attach("たい")),
    Form.of(Past, Potential)                 -> Seq(ichidanStem, attach("られた")),
    // Polite
    NonPast.polite                           -> Seq(ichidanStem, attach("ます")),
    Past.polite                              -> Seq(ichidanStem, attach("ました")),
    Te.polite                                -> Seq(ichidanStem, attach("まして")),
    Conditional.polite                       -> Seq(ichidanStem, attach("ましたら")),
    Provisional.polite                       -> Seq(ichidanStem, attach("ますなら")),
    Potential.polite                         -> Seq(ichidanStem, attach("られます")),
    Passive.polite                           -> Seq(ichidanStem, attach("られます")),
    Causative.polite                         -> Seq(ichidanStem, attach("させます")),
    CausativePassive.polite                  -> Seq(ichidanStem, attach("させられます")),
    Volitional.polite                        -> Seq(ichidanStem, attach("ましょう")),
    Alternative.polite                       -> Seq(ichidanStem, attach("ましたり")),
    Imperative.polite                        -> Seq(ichidanStem, attach("なさい")),
    Form.of(Past, Potential).polite          -> Seq(ichidanStem, attach("られました")),
    // Negative
    NonPast.negative                         -> Seq(ichidanStem, attach("ない")),
    Past.negative                            -> Seq(ichidanStem, attach("なかった")),
    Te.negative                              -> Seq(ichidanStem, attach("なくて")),
    Conditional.negative                     -> Seq(ichidanStem, attach("なかったら")),
    Provisional.negative                     -> Seq(ichidanStem, attach("なければ")),
    Potential.negative                       -> Seq(ichidanStem, attach("られない")),
    Passive.negative                         -> Seq(ichidanStem, attach("られない")),
    Causative.negative                       -> Seq(ichidanStem, attach("させない")),
    CausativePassive.negative                -> Seq(ichidanStem, attach("させられない")),
    Volitional.negative                      -> Seq(ichidanStem, attach("まい")),
    Alternative.negative                     -> Seq(ichidanStem, attach("なかったり")),
    Imperative.negative                      -> Seq(attach("な")),
    Sou.negative                             -> Seq(ichidanStem, attach("なさそう")),
    Form.of(Potential, Sou).negative         -> Seq(ichidanStem, attach("られなさそう")),
    Tai.negative                             -> Seq(ichidanStem, attach("たくない")),
    Form.of(Tai, Sou).negative               -> Seq(ichidanStem, attach("たくなさそう")),
    Form.of(Past, Potential).negative        -> Seq(ichidanStem, attach("られなかった")),
    // Polite negative
    NonPast.polite.negative                  -> Seq(ichidanStem, attach("ません")),
    Past.polite.negative                     -> Seq(ichidanStem, attach("ませんでした")),
    Te.polite.negative                       -> Seq(ichidanStem, attach("ませんで")),
    Conditional.polite.negative              -> Seq(ichidanStem, attach("ませんでしたら")),
    Provisional.polite.negative              -> Seq(ichidanStem, attach("ませんなら")),
    Potential.polite.negative                -> Seq(ichidanStem, attach("られません")),
    Passive.polite.negative                  -> Seq(ichidanStem, attach("られません")),
    Causative.polite.negative                -> Seq(ichidanStem, attach("させません")),
    CausativePassive.polite.negative         -> Seq(ichidanStem, attach("させられません")),
    Volitional.polite.negative               -> Seq(ichidanStem, attach("ますまい")),
    Alternative.polite.negative              -> Seq(ichidanStem, attach("ませんでしたり")),
    Imperative.polite.negative               -> Seq(ichidanStem, attach("なさるな")),
    Form.of(Past, Potential).polite.negative -> Seq(ichidanStem, attach("られませんでした"))
  )
}
