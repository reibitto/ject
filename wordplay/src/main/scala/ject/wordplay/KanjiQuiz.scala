package ject.wordplay

import scala.collection.immutable.ArraySeq
import scala.util.Random

final case class KanjiQuiz(top: String, left: String, bottom: String, right: String, answers: Set[String])

object KanjiQuiz {

  def apply(front: String, back: String, answers: String): KanjiQuiz = {
    val IndexedSeq(top, left) = Random.shuffle(front.toIndexedSeq)
    val IndexedSeq(bottom, right) = Random.shuffle(back.toIndexedSeq)

    KanjiQuiz(top.toString, left.toString, bottom.toString, right.toString, answers.map(_.toString).toSet)
  }

  val all: ArraySeq[KanjiQuiz] = ArraySeq(
    KanjiQuiz("戦策", "奪図", "略"),
    KanjiQuiz("進美", "粧石", "化"),
    KanjiQuiz("惨失", "北退", "敗"),
    KanjiQuiz("初空", "栓元", "耳"),
    KanjiQuiz("風名", "身激", "刺"),
    KanjiQuiz("出末", "紀帯", "世"),
    KanjiQuiz("非苦", "民解", "難"),
    KanjiQuiz("油電", "力迫", "圧気"),
    KanjiQuiz("梅大", "男戸", "雨"),
    KanjiQuiz("現重", "目員", "役"),
    KanjiQuiz("当薬", "長面", "局"),
    KanjiQuiz("子白", "手本", "熊"),
    KanjiQuiz("生就", "躍動", "活"),
    KanjiQuiz("風満", "風流", "潮"),
    KanjiQuiz("銃美", "明援", "声"),
    KanjiQuiz("理経", "緒来", "由"),
    KanjiQuiz("会余", "算画", "計"),
    KanjiQuiz("初悪", "中幻", "夢"),
    KanjiQuiz("機過", "速感", "敏"),
    KanjiQuiz("特発", "意入", "注"),
    KanjiQuiz("続思", "番国", "出"),
    KanjiQuiz("防本", "符色", "音"),
    KanjiQuiz("内安", "理配", "心"),
    KanjiQuiz("音道", "団天", "楽"),
    KanjiQuiz("色弱", "絶品", "気"),
    KanjiQuiz("完万", "国部", "全"),
    KanjiQuiz("成少", "末金", "年"),
    KanjiQuiz("調昭", "牛服", "和"),
    KanjiQuiz("駅代", "解当", "弁"),
    KanjiQuiz("欠頂", "滴検", "点")
  )
}
