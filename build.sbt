import Build.Version
import sbt.Keys._
import sbt._
import sbtwelcome._

lazy val root = project
  .in(file("."))
  .aggregate(
    core
  )
  .settings(
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
         |`Y888P              ${version.value}
         |
         |""".stripMargin,
    usefulTasks := Seq(
      UsefulTask("a", "ject/run", "Download dictionary and create Lucene index"),
      UsefulTask("b", "~compile", "Compile all modules with file-watch enabled"),
      UsefulTask("c", "fmt", "Run scalafmt on the entire project")
    )
  )

lazy val core = module("ject", Some("core"))
  .settings(
    fork := true,
    baseDirectory in run := file("."),
    libraryDependencies ++= Seq(
      "dev.zio"                %% "zio"                       % Version.zio,
      "dev.zio"                %% "zio-streams"               % Version.zio,
      "dev.zio"                %% "zio-process"               % "0.1.0",
      "dev.zio"                %% "zio-logging"               % "0.5.0",
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

def module(projectId: String, moduleFile: Option[String] = None): Project =
  Project(id = projectId, base = file(moduleFile.getOrElse(projectId)))
    .settings(Build.defaultSettings(projectId))
