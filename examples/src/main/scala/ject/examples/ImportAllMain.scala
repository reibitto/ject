package ject.examples

import zio.*

object ImportAllMain extends ZIOAppDefault {

  def run: Task[Unit] =
    // Kanjidic writes to its own index directory, independent of the JMDict/Yomichan chain
    // (which both write to data/lucene/word-ja and must run in order), so it's safe to run in parallel.
    KanjidicMain.run.zipPar(JMDictMain.run *> YomichanMain.run *> KrDictMain.run).unit

}
