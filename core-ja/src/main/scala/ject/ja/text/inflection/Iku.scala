package ject.ja.text.inflection

import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*
import ject.ja.text.Transforms

object Iku {

  val inflections: Map[Form, Transform] =
    Map(
      // Plain
      NonPast.plain -> Transforms.identity,
      Past.plain -> Transforms(stemOf("く"), attach("った")),
      Te.plain -> Transforms(stemOf("く"), attach("って")),
      Conditional.plain -> Transforms(stemOf("く"), attach("ったら")),
      Provisional.plain -> Transforms(stemOf("く"), attach("けば")),
      Potential.plain -> Transforms(stemOf("く"), attach("ける")),
      Passive.plain -> Transforms(stemOf("く"), attach("かれる")),
      Causative.plain -> Transforms(stemOf("く"), attach("かせる", "かす")),
      CausativePassive.plain -> Transforms(stemOf("く"), attach("かせられる")),
      Volitional.plain -> Transforms(stemOf("く"), attach("こう")),
      Alternative.plain -> Transforms(stemOf("く"), attach("ったり")),
      Imperative.plain -> Transforms(stemOf("く"), attach("け")),
      Sou.plain -> Transforms(stemOf("く"), attach("きそう")),
      Tai.plain -> Transforms(stemOf("く"), attach("きたい")),
      Progressive.plain -> Transforms(stemOf("く"), attach("っている", "ってる")),
      Form.of(Progressive, Past) -> Transforms(stemOf("く"), attach("っていた", "ってた")),
      Form.of(Potential, Te) -> Transforms(stemOf("く"), attach("けて")),
      Form.of(Potential, Past) -> Transforms(stemOf("く"), attach("けた")),
      Form.of(Causative, Te) -> Transforms(stemOf("く"), attach("かせて")),
      Form.of(Causative, Past) -> Transforms(stemOf("く"), attach("かせた")),
      Form.of(Tai, Te) -> Transforms(stemOf("く"), attach("きたくて")),
      // Polite
      NonPast.polite -> Transforms(stemOf("く"), attach("きます")),
      Past.polite -> Transforms(stemOf("く"), attach("きました")),
      Te.polite -> Transforms(stemOf("く"), attach("きまして")),
      Conditional.polite -> Transforms(stemOf("く"), attach("きましたら")),
      Provisional.polite -> Transforms(stemOf("く"), attach("きますなら")),
      Potential.polite -> Transforms(stemOf("く"), attach("けます")),
      Passive.polite -> Transforms(stemOf("く"), attach("かれます")),
      Causative.polite -> Transforms(stemOf("く"), attach("かせます", "かします")),
      CausativePassive.polite -> Transforms(stemOf("く"), attach("かせられます")),
      Volitional.polite -> Transforms(stemOf("く"), attach("きましょう")),
      Alternative.polite -> Transforms(stemOf("く"), attach("きましたり")),
      Imperative.polite -> Transforms(stemOf("く"), attach("きなさい")),
      Progressive.polite -> Transforms(stemOf("く"), attach("っています", "ってます")),
      Form.of(Progressive, Past).polite -> Transforms(stemOf("く"), attach("っていました", "ってました")),
      Form.of(Potential, Te).polite -> Transforms(stemOf("く"), attach("けまして")),
      Form.of(Potential, Past).polite -> Transforms(stemOf("く"), attach("けました")),
      Form.of(Causative, Te).polite -> Transforms(stemOf("く"), attach("かせまして")),
      Form.of(Causative, Past).polite -> Transforms(stemOf("く"), attach("かせました")),
      // Negative
      NonPast.negative -> Transforms(stemOf("く"), attach("かない", "かぬ", "かず")),
      Past.negative -> Transforms(stemOf("く"), attach("かなかった")),
      Te.negative -> Transforms(stemOf("く"), attach("かなくて", "かないで")),
      Conditional.negative -> Transforms(stemOf("く"), attach("かなかったら")),
      Provisional.negative -> Transforms(stemOf("く"), attach("かなければ")),
      Potential.negative -> Transforms(stemOf("く"), attach("けない")),
      Passive.negative -> Transforms(stemOf("く"), attach("かれない")),
      Causative.negative -> Transforms(stemOf("く"), attach("かせない", "かさない")),
      CausativePassive.negative -> Transforms(stemOf("く"), attach("かせられない")),
      Volitional.negative -> Transforms(stemOf("く"), attach("くまい")),
      Alternative.negative -> Transforms(stemOf("く"), attach("かなかったり")),
      Imperative.negative -> Transforms(stemOf("く"), attach("くな")),
      Sou.negative -> Transforms(stemOf("く"), attach("かなそう")),
      Form.of(Potential, Sou).negative -> Transforms(stemOf("く"), attach("けなそう")),
      Tai.negative -> Transforms(stemOf("く"), attach("きたくない")),
      Progressive.negative -> Transforms(stemOf("く"), attach("っていない", "ってない")),
      Form.of(Progressive, Past).negative -> Transforms(stemOf("く"), attach("っていなかった", "ってなかった")),
      Form.of(Tai, Sou).negative -> Transforms(stemOf("く"), attach("きたくなさそう")),
      Form.of(Potential, Te).negative -> Transforms(stemOf("く"), attach("けなくて")),
      Form.of(Potential, Past).negative -> Transforms(stemOf("く"), attach("けなかった")),
      Form.of(Causative, Te).negative -> Transforms(stemOf("く"), attach("かせなくて")),
      Form.of(Causative, Past).negative -> Transforms(stemOf("く"), attach("かせなかった")),
      // Polite negative
      NonPast.polite.negative -> Transforms(stemOf("く"), attach("きません")),
      Past.polite.negative -> Transforms(stemOf("く"), attach("きませんでした")),
      Te.polite.negative -> Transforms(stemOf("く"), attach("きませんで")),
      Conditional.polite.negative -> Transforms(stemOf("く"), attach("きませんでしたら")),
      Provisional.polite.negative -> Transforms(stemOf("く"), attach("きませんなら")),
      Potential.polite.negative -> Transforms(stemOf("く"), attach("けません")),
      Passive.polite.negative -> Transforms(stemOf("く"), attach("かれません")),
      Causative.polite.negative -> Transforms(stemOf("く"), attach("かせません", "かしません")),
      CausativePassive.polite.negative -> Transforms(stemOf("く"), attach("かせられません")),
      Volitional.polite.negative -> Transforms(stemOf("く"), attach("きますまい")),
      Alternative.polite.negative -> Transforms(stemOf("く"), attach("きませんでしたり")),
      Imperative.polite.negative -> Transforms(stemOf("く"), attach("きなさるな")),
      Progressive.polite.negative -> Transforms(stemOf("く"), attach("っていません", "ってません")),
      Form.of(Progressive, Past).polite.negative -> Transforms(stemOf("く"), attach("っていませんでした", "ってませんでした")),
      Form.of(Potential, Te).polite.negative -> Transforms(stemOf("く"), attach("けませんでして")),
      Form.of(Potential, Past).polite.negative -> Transforms(stemOf("く"), attach("けませんでした")),
      Form.of(Causative, Te).polite.negative -> Transforms(stemOf("く"), attach("かせませんでして")),
      Form.of(Causative, Past).polite.negative -> Transforms(stemOf("く"), attach("かせませんでした"))
    )

  val deinflections: Map[Form, Transform] = Map.empty // TODO: Implement
}
