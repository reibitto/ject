# Ject

![Scala CI](https://github.com/reibitto/ject/workflows/Scala%20CI/badge.svg)

## What is it?

Ject is a library to help with the creation of Lucene indexes for dictionaries and performing searches on those indexes.

Currently only Japanese is supported with [JMDict](http://www.edrdg.org/jmdict/edict_doc.html), but the plan is to
support multiple dictionaries for multiple languages.

The motivation for creating this library is to enable real-time dictionary searches into
[Command Center](https://github.com/reibitto/command-center) via a plugin.

## Usage

To create the JMDict Lucene index, simply run `examples/runMain ject.examples.JMDictMain` after starting up `sbt`.

Similarly for creating the Lucene index for Kanjidic: `examples/runMain ject.examples.KanjidicMain`

You can also use this project as a library by adding the following dependency:

```scala
"com.github.reibitto" %% "ject" % "0.2.0"
```

But be warned the API, schemas, etc will likely change quite a bit in the future. I just wanted to get this library
out there first so that it can be used in Command Center. I'll put more thought into a clean design later as I start
supporting other dictionaries and languages.
