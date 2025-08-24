package ject.ja

import ject.ja.text.inflection.{Godan, Ichidan}
import ject.ja.text.Deinflection
import ject.ja.text.WordType
import zio.*
import zio.test.*

object InflectionSpec extends ZIOSpecDefault {

  def spec: Spec[TestEnvironment & Scope, Any] =
    suite("InflectionSpec")(
      suite("inflection and deinflection works both ways")(
        suite("ichidan") {
          Ichidan.inflections.map { case (form, transform) =>
            val verb = "食べる"
            val inflected = transform(verb)

            test(s"${form.render}") {
              inflected.toOption.get.map { s =>
                val deinflectedCandidates = Deinflection
                  .deinflect(s, WordType.VerbIchidan, form)
                  .map(_.toChunk)
                  .getOrElse(Chunk.empty)

                assertTrue(deinflectedCandidates.contains(verb))
                  .label(s"$verb -> $s in (${deinflectedCandidates.mkString(", ")})")
              }.reduce(_ && _)
            }
          }
        },
        suite("godan") {
          Godan.inflections.map { case (form, transform) =>
            val verb = "書く"
            val inflected = transform(verb)

            test(s"${form.render}") {
              inflected.toOption.get.map { s =>
                val deinflectedCandidates = Deinflection
                  .deinflect(s, WordType.VerbGodan, form)
                  .map(_.toChunk)
                  .getOrElse(Chunk.empty)

                assertTrue(deinflectedCandidates.contains(verb))
                  .label(s"$verb -> $s in (${deinflectedCandidates.mkString(", ")})")
              }.reduce(_ && _)
            }
          }
        }
      )
    ) @@ TestAspect.ignore // TODO: Enable after adding all the definflections
}
