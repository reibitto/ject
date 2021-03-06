import Build.Version
import sbt.Keys._
import sbt._
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
  .aggregate(core, examples, wordplay)
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
      "dev.zio"                %% "zio"                       % Version.zio,
      "dev.zio"                %% "zio-streams"               % Version.zio,
      "dev.zio"                %% "zio-process"               % "0.4.0",
      "dev.zio"                %% "zio-logging"               % "0.5.10",
      "org.scala-lang.modules" %% "scala-xml"                 % "2.0.0",
      "com.beachape"           %% "enumeratum"                % "1.6.1",
      "org.apache.lucene"       % "lucene-core"               % Version.lucene,
      "org.apache.lucene"       % "lucene-analyzers-common"   % Version.lucene,
      "org.apache.lucene"       % "lucene-queryparser"        % Version.lucene,
      "org.apache.lucene"       % "lucene-facet"              % Version.lucene,
      "org.apache.lucene"       % "lucene-highlighter"        % Version.lucene,
      "org.apache.lucene"       % "lucene-analyzers-kuromoji" % Version.lucene
    )
  )

lazy val examples = module("examples")
  .dependsOn(core)
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
      "me.shadaj" %%% "slinky-web" % "0.6.7",
      "me.shadaj" %%% "slinky-hot" % "0.6.7"
    ),
    Compile / npmDependencies ++= Seq(
      "react"       -> "16.13.1",
      "react-dom"   -> "16.13.1",
      "react-proxy" -> "1.1.8"
    ),
    Compile / npmDevDependencies ++= Seq(
      "file-loader"         -> "6.0.0",
      "style-loader"        -> "1.2.1",
      "css-loader"          -> "3.5.3",
      "html-webpack-plugin" -> "4.3.0",
      "copy-webpack-plugin" -> "5.1.1",
      "webpack-merge"       -> "4.2.2"
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
