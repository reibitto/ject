import sbtwelcome.*

inThisBuild(
  List(
    organization := "com.github.reibitto",
    homepage := Some(uri("https://github.com/reibitto/ject")),
    licenses := List("Apache-2.0" -> uri("https://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("reibitto", "reibitto", "reibitto@users.noreply.github.com", uri("https://reibitto.github.io"))
    )
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(core, coreJapanese, coreKorean, tools, examples)
  .settings(
    name := "ject-root",
    publish / skip := true,
    addCommandAlias("fmt", "all root/scalafmtSbt root/scalafmtAll"),
    addCommandAlias("fmtCheck", "all root/scalafmtSbtCheck root/scalafmtCheckAll"),
    logo :=
      s"""
         |    o8o                         .
         |    `"'                       .o8
         |   oooo  .ooooo.   .ooooo.  .o888oo
         |   `888 d88' `88b d88' `"Y8   888
         |    888 888ooo888 888         888
         |    888 888    .o 888   .o8   888 .
         |    888 `Y8bod8P' `Y8bod8P'   "888"
         |    888
         |.o. 88P
         |`Y888P       ${version.value}
         |
         |""".stripMargin,
    usefulTasks := Seq(
      UsefulTask("~compile", "Compile all modules with file-watch enabled"),
      UsefulTask(
        "examples/runMain ject.examples.JMDictMain",
        "Download Japanese-English dictionary and create Lucene index"
      ),
      UsefulTask(
        "examples/runMain ject.examples.KanjidicMain",
        "Download kanjidic and create Lucene index"
      ),
      UsefulTask(
        "examples/runMain ject.examples.YomichanMain",
        "Use custom Yomichan dictionaries to create Lucene indexes"
      ),
      UsefulTask(
        "examples/runMain ject.examples.KrDictMain",
        "Import kr-dict Korean -> English/Japanese dictionary"
      ),
      UsefulTask(
        "examples/runMain ject.examples.ImportAllMain",
        "Import all dictionaries in sequence"
      ),
      UsefulTask("fmt", "Run scalafmt on the entire project")
    )
  )

lazy val core = module("ject", Some("core"))
  .settings(
    fork := true,
    run / baseDirectory := file("."),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % V.zio,
      "dev.zio" %% "zio-streams" % V.zio,
      "com.beachape" %% "enumeratum" % V.enumeratum,
      "org.apache.lucene" % "lucene-core" % V.lucene,
      "org.apache.lucene" % "lucene-analysis-common" % V.lucene,
      "org.apache.lucene" % "lucene-queryparser" % V.lucene,
      "org.apache.lucene" % "lucene-facet" % V.lucene,
      "org.apache.lucene" % "lucene-highlighter" % V.lucene
    )
  )

lazy val coreJapanese = module("ject-ja", Some("core-ja"))
  .dependsOn(core)
  .settings(
    fork := true,
    run / baseDirectory := file("."),
    Test / baseDirectory := file("."),
    libraryDependencies ++= Seq(
      "org.apache.lucene" % "lucene-analysis-kuromoji" % V.lucene
    )
  )

lazy val coreKorean = module("ject-ko", Some("core-ko"))
  .dependsOn(coreJapanese)
  .settings(
    fork := true,
    run / baseDirectory := file("."),
    libraryDependencies ++= Seq(
      "org.apache.lucene" % "lucene-analysis-nori" % V.lucene
    )
  )

lazy val tools = module("ject-tools", Some("tools"))
  .dependsOn(coreJapanese, coreKorean)
  .settings(
    fork := true,
    run / baseDirectory := file("."),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio-process" % V.zioProcess,
      "org.scala-lang.modules" %% "scala-xml" % V.scalaXml,
      "com.softwaremill.sttp.client3" %% "zio" % V.sttp,
      "io.circe" %% "circe-core" % V.circe,
      "io.circe" %% "circe-parser" % V.circe,
      "org.slf4j" % "slf4j-nop" % V.slf4j
    )
  )

lazy val examples = module("examples")
  .dependsOn(tools)
  .settings(
    fork := true,
    run / baseDirectory := file("."),
    publish / skip := true,
    libraryDependencies ++= Seq(
      "com.lihaoyi" %% "pprint" % V.pprint
    ),
    javaOptions ++= Seq(
      "--add-modules=jdk.incubator.vector",
      "-Dorg.apache.lucene.store.MMapDirectory.enableMemorySegments=false"
    )
  )

def module(projectId: String, moduleFile: Option[String] = None): Project =
  Project(id = projectId, base = file(moduleFile.getOrElse(projectId)))
    .settings(Build.defaultSettings(projectId))

// These are used to set the working directory for each module's forked `run` task, but sbt's
// lintUnused check can't detect task-only usages, so it always flags them as unused.
Global / excludeLintKeys ++= Set(
  core / run / baseDirectory,
  coreJapanese / run / baseDirectory,
  coreJapanese / Test / baseDirectory,
  coreKorean / run / baseDirectory,
  tools / run / baseDirectory,
  examples / run / baseDirectory
)

ThisBuild / organization := "com.github.reibitto"

ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}
