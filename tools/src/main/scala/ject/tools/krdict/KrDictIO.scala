package ject.tools.krdict

import ject.DefinitionLanguage
import ject.ko.docs.WordDoc
import ject.tools.yomichan.TermBankIO
import zio.stream.ZStream

import java.nio.file.Path

object KrDictIO {

  def load(dictionaryDirectory: Path, definitionLanguage: DefinitionLanguage): ZStream[Any, Throwable, WordDoc] =
    TermBankIO.load(dictionaryDirectory)
      // There are some weird duplicate entries in the source. Detecting that with this rule and filtering them out.
      .filter(_.reading.map(!_.endsWith("]")).getOrElse(true))
      .map { entry =>
        WordDoc(
          id = s"${dictionaryDirectory.getFileName}/${entry.sequenceNumber}",
          hangulTerms = Seq(entry.term),
          hanjaTerms = Seq.empty,
          pronunciation = entry.reading.toSeq,
          definitionsEnglish = if (definitionLanguage == DefinitionLanguage.English) entry.definitions else Seq.empty,
          definitionsJapanese = if (definitionLanguage == DefinitionLanguage.Japanese) entry.definitions else Seq.empty,
          definitionsKorean = if (definitionLanguage == DefinitionLanguage.Korean) entry.definitions else Seq.empty,
          partsOfSpeech = entry.definitionTags ++ entry.termTags
        )
    }
}
