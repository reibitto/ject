package ject.examples

import zio.*

object ImportAllMain extends ZIOAppDefault {

  val dryRun: Boolean = false

  def run: UIO[Unit] =
    for {
      _ <- KanjidicMain.run
      _ <- JMDictMain.run
      _ <- YomichanMain.run
    } yield ()

}
