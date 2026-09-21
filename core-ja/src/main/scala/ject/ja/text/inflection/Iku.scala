package ject.ja.text.inflection

import ject.ja.text.{ReversibleTransform, SuffixConjugator, Transforms}
import ject.ja.text.Form
import ject.ja.text.SubForm.*
import ject.ja.text.Transformation.*

object Iku {

  private val conjugate = SuffixConjugator(stemOf("く"), "く")

  private val conjugations: Map[Form, ReversibleTransform] = Map(
    // Plain
    Past.plain -> conjugate("った"),
    Te.plain -> conjugate("って"),
    Conditional.plain -> conjugate("ったら"),
    Provisional.plain -> conjugate("けば"),
    Potential.plain -> conjugate("ける"),
    Passive.plain -> conjugate("かれる"),
    Causative.plain -> conjugate("かせる", "かす"),
    CausativePassive.plain -> conjugate("かせられる"),
    Volitional.plain -> conjugate("こう"),
    Alternative.plain -> conjugate("ったり"),
    Imperative.plain -> conjugate("け"),
    Sou.plain -> conjugate("きそう"),
    Tai.plain -> conjugate("きたい"),
    Progressive.plain -> conjugate("っている", "ってる"),
    Form.of(Progressive, Past) -> conjugate("っていた", "ってた"),
    Form.of(Potential, Te) -> conjugate("けて"),
    Form.of(Potential, Past) -> conjugate("けた"),
    Form.of(Causative, Te) -> conjugate("かせて"),
    Form.of(Causative, Past) -> conjugate("かせた"),
    Form.of(Tai, Te) -> conjugate("きたくて"),
    // Polite
    NonPast.polite -> conjugate("きます"),
    Past.polite -> conjugate("きました"),
    Te.polite -> conjugate("きまして"),
    Conditional.polite -> conjugate("きましたら"),
    Provisional.polite -> conjugate("きますなら"),
    Potential.polite -> conjugate("けます"),
    Passive.polite -> conjugate("かれます"),
    Causative.polite -> conjugate("かせます", "かします"),
    CausativePassive.polite -> conjugate("かせられます"),
    Volitional.polite -> conjugate("きましょう"),
    Alternative.polite -> conjugate("きましたり"),
    Imperative.polite -> conjugate("きなさい"),
    Progressive.polite -> conjugate("っています", "ってます"),
    Form.of(Progressive, Past).polite -> conjugate("っていました", "ってました"),
    Form.of(Potential, Te).polite -> conjugate("けまして"),
    Form.of(Potential, Past).polite -> conjugate("けました"),
    Form.of(Causative, Te).polite -> conjugate("かせまして"),
    Form.of(Causative, Past).polite -> conjugate("かせました"),
    // Negative
    NonPast.negative -> conjugate("かない", "かぬ", "かず"),
    Past.negative -> conjugate("かなかった"),
    Te.negative -> conjugate("かなくて", "かないで"),
    Conditional.negative -> conjugate("かなかったら"),
    Provisional.negative -> conjugate("かなければ"),
    Potential.negative -> conjugate("けない"),
    Passive.negative -> conjugate("かれない"),
    Causative.negative -> conjugate("かせない", "かさない"),
    CausativePassive.negative -> conjugate("かせられない"),
    Volitional.negative -> conjugate("くまい"),
    Alternative.negative -> conjugate("かなかったり"),
    Imperative.negative -> conjugate("くな"),
    Sou.negative -> conjugate("かなそう"),
    Form.of(Potential, Sou).negative -> conjugate("けなそう"),
    Tai.negative -> conjugate("きたくない"),
    Progressive.negative -> conjugate("っていない", "ってない"),
    Form.of(Progressive, Past).negative -> conjugate("っていなかった", "ってなかった"),
    Form.of(Tai, Sou).negative -> conjugate("きたくなさそう"),
    Form.of(Potential, Te).negative -> conjugate("けなくて"),
    Form.of(Potential, Past).negative -> conjugate("けなかった"),
    Form.of(Causative, Te).negative -> conjugate("かせなくて"),
    Form.of(Causative, Past).negative -> conjugate("かせなかった"),
    // Polite negative
    NonPast.polite.negative -> conjugate("きません"),
    Past.polite.negative -> conjugate("きませんでした"),
    Te.polite.negative -> conjugate("きませんで"),
    Conditional.polite.negative -> conjugate("きませんでしたら"),
    Provisional.polite.negative -> conjugate("きませんなら"),
    Potential.polite.negative -> conjugate("けません"),
    Passive.polite.negative -> conjugate("かれません"),
    Causative.polite.negative -> conjugate("かせません", "かしません"),
    CausativePassive.polite.negative -> conjugate("かせられません"),
    Volitional.polite.negative -> conjugate("きますまい"),
    Alternative.polite.negative -> conjugate("きませんでしたり"),
    Imperative.polite.negative -> conjugate("きなさるな"),
    Progressive.polite.negative -> conjugate("っていません", "ってません"),
    Form.of(Progressive, Past).polite.negative -> conjugate("っていませんでした", "ってませんでした"),
    Form.of(Potential, Te).polite.negative -> conjugate("けませんでして"),
    Form.of(Potential, Past).polite.negative -> conjugate("けませんでした"),
    Form.of(Causative, Te).polite.negative -> conjugate("かせませんでして"),
    Form.of(Causative, Past).polite.negative -> conjugate("かせませんでした")
  )

  val inflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms.identity) ++ conjugations.view.mapValues(_.forward)

  val deinflections: Map[Form, Transform] =
    Map(NonPast.plain -> Transforms(ensureSuffix("く"))) ++ conjugations.view.mapValues(_.backward)
}
