package ject

object RadicalQuery {
  val map: Map[Char, Char] = Seq(
    Seq('-', 'ー') -> '一',
    Seq('|')      -> '｜',
    Seq(',', '、') -> '丶',
    Seq('の', 'ノ') -> '丿',
    Seq('」')      -> '亅',
    Seq('に', 'ニ') -> '二',
    Seq('る', 'ル') -> '儿',
    Seq('は', 'ハ') -> '八',
    Seq(';', '；') -> '冫',
    Seq('か', 'カ') -> '力',
    Seq('く', 'ク') -> '勹',
    Seq('ひ', 'ヒ') -> '匕',
    Seq('こ', 'コ') -> '匚',
    Seq('+', '＋') -> '十',
    Seq('と', 'ト') -> '卜',
    Seq('む', 'ム') -> '厶',
    Seq('ろ', 'ロ') -> '口',
    Seq('た', 'タ') -> '夕',
    Seq('え', 'エ') -> '工',
    Seq('よ', 'ヨ') -> '彐',
    Seq('み', 'ミ') -> '彡',
    Seq('い', 'イ') -> '亻',
    Seq('り', 'リ') -> '刂',
    Seq('ま', 'マ') -> 'マ',
    Seq('ゆ', 'ユ') -> 'ユ',
    Seq('き', 'キ') -> '扌',
    Seq('ね', 'ネ') -> '礻',
    Seq('#', '＃') -> '井'
  ).flatMap {
    case (ks, v) =>
      ks.map(k => (k, v))
  }.toMap

  def normalize(s: String): String =
    s.map(c => map.getOrElse(c, c))
}
