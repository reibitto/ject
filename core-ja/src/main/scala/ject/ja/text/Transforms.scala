package ject.ja.text

import ject.ja.text.Transformation.multiParam
import ject.ja.text.Transformation.Transform
import zio.NonEmptyChunk

object Transforms {

  def apply(transforms: Transform*): Transform = { (s: String) =>
    transforms.headOption match {
      case None => Right(NonEmptyChunk.single(s))
      case Some(headTransform) =>
        transforms.tail.foldLeft(headTransform(s)) { case (acc, f) =>
          acc.flatMap(b => multiParam(f)(b))
        }
    }
  }

  def withInitial(f: String => NonEmptyChunk[Transform]): Transform =
    (s: String) => Transforms.apply(f(s)*)(s)

  def identity: Transform = s => Right(NonEmptyChunk.single(s))

  def pure(s: String): Transform = _ => Right(NonEmptyChunk.single(s))

  def pure(s: String, ss: String*): Transform = _ => Right(NonEmptyChunk(s, ss*))
}
