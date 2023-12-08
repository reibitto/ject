package ject

sealed trait DefinitionLanguage

object DefinitionLanguage {
  case object English extends DefinitionLanguage
  case object Japanese extends DefinitionLanguage
  case object Korean extends DefinitionLanguage
}
