import sbt._
import sbt.Keys._
import sbtwelcome._

inThisBuild(
  List(
    organization := "com.github.reibitto",
    homepage := Some(url("https://github.com/reibitto/ject")),
    licenses := List("Apache-2.0" -> url("https://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("reibitto", "reibitto", "reibitto@users.noreply.github.com", url("https://reibitto.github.io"))
    )
  )
)

lazy val root = project
  .in(file("."))
  .aggregate(core, coreJapanese, coreKorean, examples, wordplay)
  .settings(
    name := "ject",
    addCommandAlias("fmt", "all root/scalafmtSbt root/scalafmtAll"),
    addCommandAlias("fmtCheck", "all root/scalafmtSbtCheck root/scalafmtCheckAll"),
    addCommandAlias("wordplay-dev", ";wordplay/fastOptJS::startWebpackDevServer;~wordplay/fastOptJS"),
    addCommandAlias("wordplay-build", "wordplay/fullOptJS::webpack"),
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
      UsefulTask("a", "~compile", "Compile all modules with file-watch enabled"),
      UsefulTask(
        "b",
        "examples/runMain ject.examples.JMDictMain",
        "Download Japanese-English dictionary and create Lucene index"
      ),
      UsefulTask(
        "c",
        "examples/runMain ject.examples.KanjidicMain",
        "Download kanjidic and create Lucene index"
      ),
      UsefulTask("d", "fmt", "Run scalafmt on the entire project"),
      UsefulTask("e", "wordplay-dev", "Start wordplay at localhost:8080 with hot reloading enabled")
    )
  )

lazy val core = module("ject", Some("core"))
  .settings(
    fork := true,
    run / baseDirectory := file("."),
    libraryDependencies ++= Seq(
      "dev.zio" %% "zio" % V.zio,
      "dev.zio" %% "zio-streams" % V.zio,
      "dev.zio" %% "zio-process" % V.zioProcess,
      "com.beachape" %% "enumeratum" % V.enumeratum,
      "org.apache.lucene" % "lucene-core" % V.lucene,
      "org.apache.lucene" % "lucene-analyzers-common" % V.lucene,
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
    libraryDependencies ++= Seq(
      "org.scala-lang.modules" %% "scala-xml" % V.scalaXml,
      "org.apache.lucene" % "lucene-analyzers-kuromoji" % V.lucene
    )
  )

lazy val coreKorean = module("ject-ko", Some("core-ko"))
  .dependsOn(core)
  .settings(
    fork := true,
    run / baseDirectory := file("."),
    libraryDependencies ++= Seq(
      "org.apache.lucene" % "lucene-analyzers-nori" % V.lucene,
      "com.softwaremill.sttp.client3" %% "zio" % V.sttp,
      "org.jsoup" % "jsoup" % V.jsoup,
      "org.slf4j" % "slf4j-nop" % V.slf4j
    )
  )

lazy val examples = module("examples")
  .dependsOn(coreJapanese, coreKorean)
  .settings(
    fork := true,
    run / baseDirectory := file("."),
    publish / skip := true
  )

lazy val wordplay = module("wordplay")
  .settings(
    fork := true,
    Test / fork := false,
    run / baseDirectory := file("."),
    publish / skip := true,
    libraryDependencies ++= Seq(
      "me.shadaj" %%% "slinky-web" % V.slinky,
      "me.shadaj" %%% "slinky-hot" % V.slinky
    ),
    Compile / npmDependencies ++= Seq(
      "react" -> "16.13.1",
      "react-dom" -> "16.13.1",
      "react-proxy" -> "1.1.8"
    ),
    Compile / npmDevDependencies ++= Seq(
      "file-loader" -> "6.0.0",
      "style-loader" -> "1.2.1",
      "css-loader" -> "3.5.3",
      "html-webpack-plugin" -> "4.3.0",
      "copy-webpack-plugin" -> "5.1.1",
      "webpack-merge" -> "4.2.2"
    ),
    webpack / version := "4.43.0",
    startWebpackDevServer / version := "3.11.0",
    webpackResources := baseDirectory.value / "webpack" * "*",
    fastOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-fastopt.config.js"),
    fullOptJS / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js"),
    Test / webpackConfigFile := Some(baseDirectory.value / "webpack" / "webpack-core.config.js"),
    fastOptJS / webpackDevServerExtraArgs := Seq("--inline", "--hot"),
    fastOptJS / webpackBundlingMode := BundlingMode.LibraryOnly(),
    Test / requireJsDomEnv := true
  )
  .enablePlugins(ScalaJSBundlerPlugin)

def module(projectId: String, moduleFile: Option[String] = None): Project =
  Project(id = projectId, base = file(moduleFile.getOrElse(projectId)))
    .settings(Build.defaultSettings(projectId))

ThisBuild / organization := "com.github.reibitto"
