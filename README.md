# Highwheel sbt plugin

Highwheel sbt plugin is an sbt 1.x plugin that allow to run [highwheel-modules](https://github.com/fburato/highwheel-modules) analysis
on scala project as part of the build process run with sbt. 

The plugin introduces the following settings:

* `(highwheelSpecFiles: Seq[File])` are the files to the specification files to use for the analysis. Defaults
to `Seq(baseDirectory / "spec.hwm)"`
* `(highwheelAnalysisMode: String)` specifies the type of analysis to run. Can be `"loose"` or `"strict"` and defaults to
`strict`
* `(highwheelAnalysisPaths: Seq[File])` specifies the paths to add to the analysis. Defaults to `classDirectory in Compile`
* `(highwheelEvidenceLimit: Option[Int])` limits the pieces of evidence collected by the plugin to prove that a depdency exists or does not exist. A value of `None` entails all evidence is collected.


The plugin also introduces the following tasks:

* `highwheelAnalyse`: runs the analysis using the settings specified and fails if any error is thrown. It depends on the compile task.
* `highwheelBaseAnalyseTask`: same as `highwheelAnalyse`, but with no other task dependency 
## Installation

Add to your `project/plugins.sbt` file the following line:

```scala
//resolvers += Resolver.mavenLocal

addSbtPlugin("com.github.fburato" %% "sbt-highwheel" % "1.6")
```

Uncomment resolvers if you have installed highwheel or the pluing locally.

Add to your `build.sbt` the plugin as:

```scala
lazy val root = project.in(file("."))
  .enablePlugins(com.github.fburato.highwheelmodulessbt.AnalyserPlugin)
  .settings(...
  )
```

## Tips

When running a multi-modules builds it is possible to run, on the parent module, an analysis that takes into 
consideration all the output directories of the children modules. In order to do so, the children output directories
need to be added to the parent `highwheelAnalysisPaths` setting.

For example, if your multimodule build is defined in your `build.sbt` as:

```scala
lazy val core = project.in(file("core"))
lazy val web = project.in(file("web")).dependsOn(core)
lazy val datagateway = project.in(file("datagateway")).dependsOn(core)
lazy val root = project.in(file("."))
  .dependsOn(web, datagateway)
  .aggregate(core, web, datagateway)
  .enablePlugins(com.github.fburato.highwheelmodulessbt.AnalyserPlugin)
  .settings(
    name := "my-project",
    version := "0.1",
    scalaVersion := "2.12.5"
   )
```

You can have the parent project analysing all the sub modules by adding the setting:

`highwheelAnalysisPaths := Seq((classDirectory in core in Compile).value,(classDirectory in web in Compile).value,(classDirectory in datagateway in Compile).value)`

so 

```scala
lazy val core = project.in(file("core"))
lazy val web = project.in(file("web")).dependsOn(core)
lazy val datagateway = project.in(file("datagateway")).dependsOn(core)
lazy val root = project.in(file("."))
  .dependsOn(web, datagateway)
  .aggregate(core, web, datagateway)
  .enablePlugins(com.github.fburato.highwheelmodulessbt.AnalyserPlugin)
  .settings(
    name := "my-project",
    version := "0.1",
    scalaVersion := "2.12.5",
    highwheelAnalysisPaths := Seq((classDirectory in core in Compile).value,(classDirectory in web in Compile).value,(classDirectory in datagateway in Compile).value)
  )
```

Always in multi-modules build, you can create a task in the parent project that runs the analysis in all
submodules and the parent itself as follows:

```scala
lazy val core = project.in(file("core"))
lazy val web = project.in(file("web")).dependsOn(core)
lazy val datagateway = project.in(file("datagateway")).dependsOn(core)
lazy val analyseAll = taskKey[Unit]("Run analysis in all submodules and parent")
lazy val root = project.in(file("."))
  .dependsOn(web, datagateway)
  .aggregate(core, web, datagateway)
  .enablePlugins(com.github.fburato.highwheelmodulessbt.AnalyserPlugin)
  .settings(
    name := "my-project",
    version := "0.1",
    scalaVersion := "2.12.5",
    highwheelAnalysisPaths := Seq((classDirectory in core in Compile).value,(classDirectory in web in Compile).value,(classDirectory in datagateway in Compile).value),
    analyseAll := Def.sequential(
      highwheelAnalyse in core,
      highwheelAnalyse in web,
      highwheelAnalyse in datagateway,
      highwheelAnalyse
    ).value
  )
```

So, just add the new `taskKey` and use the sequential combinator on tasks to run the analysis task on every module in sequence.
