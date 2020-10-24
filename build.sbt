import Build.Version
import sbt.Keys._
import sbt._
import sbtwelcome._

lazy val root = project
  .in(file("."))
  .aggregate(core, wordplay)
  .settings(
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
         |`Y888P              ${version.value}
         |
         |""".stripMargin,
    usefulTasks := Seq(
      UsefulTask("a", "ject/run", "Download dictionary and create Lucene index"),
      UsefulTask("b", "~compile", "Compile all modules with file-watch enabled"),
      UsefulTask("c", "fmt", "Run scalafmt on the entire project"),
      UsefulTask("d", "wordplay-dev", "Start wordplay at localhost:8080 with hot reloading enabled")
    )
  )

lazy val core = module("ject", Some("core"))
  .settings(
    fork := true,
    baseDirectory in run := file("."),
    libraryDependencies ++= Seq(
      "dev.zio"                %% "zio"                       % Version.zio,
      "dev.zio"                %% "zio-streams"               % Version.zio,
      "dev.zio"                %% "zio-process"               % "0.2.0",
      "dev.zio"                %% "zio-logging"               % "0.5.3",
      "org.scala-lang.modules" %% "scala-xml"                 % "1.3.0",
      "com.beachape"           %% "enumeratum"                % "1.6.1",
      "org.apache.lucene"       % "lucene-core"               % Version.lucene,
      "org.apache.lucene"       % "lucene-analyzers-common"   % Version.lucene,
      "org.apache.lucene"       % "lucene-queryparser"        % Version.lucene,
      "org.apache.lucene"       % "lucene-facet"              % Version.lucene,
      "org.apache.lucene"       % "lucene-highlighter"        % Version.lucene,
      "org.apache.lucene"       % "lucene-analyzers-kuromoji" % Version.lucene
    )
  )

lazy val wordplay = module("wordplay")
  .settings(
    fork := true,
    baseDirectory in run := file("."),
    libraryDependencies ++= Seq(
      "me.shadaj" %%% "slinky-web" % "0.6.6",
      "me.shadaj" %%% "slinky-hot" % "0.6.6"
    ),
    npmDependencies in Compile ++= Seq(
      "react"       -> "16.13.1",
      "react-dom"   -> "16.13.1",
      "react-proxy" -> "1.1.8"
    ),
    npmDevDependencies in Compile ++= Seq(
      "file-loader"         -> "6.0.0",
      "style-loader"        -> "1.2.1",
      "css-loader"          -> "3.5.3",
      "html-webpack-plugin" -> "4.3.0",
      "copy-webpack-plugin" -> "5.1.1",
      "webpack-merge"       -> "4.2.2"
    ),
    version in webpack := "4.43.0",
    version in startWebpackDevServer := "3.11.0",
    webpackResources := baseDirectory.value / "webpack" * "*",
    webpackConfigFile in fastOptJS := Some(baseDirectory.value / "webpack" / "webpack-fastopt.config.js"),
    webpackConfigFile in fullOptJS := Some(baseDirectory.value / "webpack" / "webpack-opt.config.js"),
    webpackConfigFile in Test := Some(baseDirectory.value / "webpack" / "webpack-core.config.js"),
    webpackDevServerExtraArgs in fastOptJS := Seq("--inline", "--hot"),
    webpackBundlingMode in fastOptJS := BundlingMode.LibraryOnly(),
    requireJsDomEnv in Test := true
  )
  .enablePlugins(ScalaJSBundlerPlugin)

def module(projectId: String, moduleFile: Option[String] = None): Project =
  Project(id = projectId, base = file(moduleFile.getOrElse(projectId)))
    .settings(Build.defaultSettings(projectId))

ThisBuild / organization := "com.github.reibitto"
ThisBuild / version := Build.JectVersion
