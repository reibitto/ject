package ject.ko

object KoreanText {
  def isKorean(c: Char): Boolean =
    c match {
      case code if code >= 0xac00 && code <= 0xd7a3 => true
      case code if code >= 0x1100 && code <= 0x11ff => true
      case code if code >= 0x3130 && code <= 0x318f => true
      case code if code >= 0xa960 && code <= 0xa97f => true
      case code if code >= 0xd7b0 && code <= 0xd7ff => true
      case _                                        => false
    }

  def isHanja(c: Char): Boolean =
    (c >= 0x2e80 && c <= 0x2fef) ||
      (c >= 0x3005 && c <= 0x3007) ||
      (c >= 0x3021 && c <= 0x3029) ||
      (c >= 0x3038 && c <= 0x303b) ||
      (c >= 0x3400 && c <= 0x4dbf) ||
      (c >= 0x4e00 && c <= 0x9fff) ||
      (c >= 0xf900 && c <= 0xfaff) ||
      (c >= 0x20000 && c <= 0xe0000)
}
