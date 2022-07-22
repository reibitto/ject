package ject.ja

object RadicalQuery {

  val map: Map[Char, Char] = Seq(
    Seq('-', 'ー') -> '一',
    Seq('|') -> '｜',
    Seq(',', '、', '\\') -> '丶',
    Seq('ノ', 'の') -> '丿',
    Seq('」') -> '亅',
    Seq('ニ', 'に') -> '二',
    Seq('^') -> '人',
    Seq('ル', 'る') -> '儿',
    Seq('ハ', 'は') -> '八',
    Seq(';', '；') -> '冫',
    Seq('n', 'N') -> '几',
    Seq('u', 'U') -> '凵',
    Seq('カ', 'か') -> '力',
    Seq('ク', 'く') -> '勹',
    Seq('ヒ', 'ひ') -> '匕',
    Seq('こ', 'コ', 'C') -> '匚',
    Seq('+', '＋') -> '十',
    Seq('ト', 'と') -> '卜',
    Seq('ム', 'む') -> '厶',
    Seq('ロ', 'ろ') -> '口',
    Seq('タ', 'た') -> '夕',
    Seq('P', 'p') -> '尸',
    Seq('W', 'w') -> '山',
    Seq('M', 'm') -> '爪',
    Seq('H', 'h') -> '廾',
    Seq('*') -> '米',
    Seq('"') -> '丷',
    Seq('B', 'b') -> '阝',
    Seq('エ', 'え') -> '工',
    Seq('ヨ', 'よ') -> '彐',
    Seq('ミ', 'み') -> '彡',
    Seq('イ', 'い') -> '亻',
    Seq('リ', 'り') -> '刂',
    Seq('マ', 'ま') -> 'マ',
    Seq('ユ', 'ゆ') -> 'ユ',
    Seq('キ', 'き') -> '扌',
    Seq('ネ', 'ね') -> '礻',
    Seq('#', '＃') -> '井'
  ).flatMap { case (ks, v) =>
    ks.map(k => k -> v)
  }.toMap

  def normalize(s: String): String =
    s.map(c => map.getOrElse(c, c))
}
