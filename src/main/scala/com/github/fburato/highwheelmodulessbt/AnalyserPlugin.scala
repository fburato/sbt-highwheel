package com.github.fburato.highwheelmodulessbt

import java.util

import sbt.Keys._
import sbt._
import com.github.fburato.highwheelmodules.AnalyserFacade
import com.github.fburato.highwheelmodules.AnalyserFacade.EventSink._
import com.github.fburato.highwheelmodules.AnalyserFacade._
import sbt.internal.util.ManagedLogger
import scala.collection.JavaConverters._

object AnalyserPlugin  extends AutoPlugin {

  object autoImport {
    val highwheelSpecFile = settingKey[File]("Path to the specification file")
    val highwheelAnalysisMode = settingKey[String]("Analysis mode. Either strict or loose")
    val highwheelAnalysisPaths = settingKey[Seq[File]]("Projects to add to the analysis")
    val highwheelAnalyse = taskKey[Unit]("Analyse output directories after compiling")
    val highwheelBaseAnalyseTask = taskKey[Unit]("Analyse output directories")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    highwheelSpecFile := baseDirectory.value / "spec.hwm",
    highwheelAnalysisMode := "strict",
    highwheelAnalysisPaths := Seq((classDirectory in Compile).value),
    highwheelBaseAnalyseTask := {
      val log = streams.value.log
      Analyser(log,highwheelSpecFile.value,highwheelAnalysisPaths.value,highwheelAnalysisMode.value)
    },
    highwheelAnalyse := (highwheelBaseAnalyseTask dependsOn (compile in Compile)).value
  )
}

object Analyser {

  def apply(log: ManagedLogger, specFile: File, analysisPaths: Seq[File],analysisMode: String): Unit = {
    log.info(s"Using specification file: ${specFile.getAbsolutePath}")

    val executionMode = getExecutionMode(analysisMode).getOrElse(throw new Exception("Analysis mode needs to be either 'strict' or 'loose'"))
    val facade = new AnalyserFacade(printer(log),pathSink(log),measureSink(log),strictAnalysisSink(log),looseAnalysisSink(log))
    facade.runAnalysis(new util.ArrayList(analysisPaths.map{ f => f.getAbsolutePath}.asJavaCollection),specFile.getAbsolutePath,executionMode)
  }

  private def printer(log: ManagedLogger): Printer = s => log.info(s)

  private def pathSink(log: ManagedLogger): PathEventSink = new PathEventSink {
    private val separator = ", "
    override def jars(list: util.List[String]): Unit =
      log.info("Jars: " + list.asScala.mkString(separator))

    override def ignoredPaths(list: util.List[String]): Unit =
      if(list.isEmpty)
        log.info("Ignored: ")
      else
        log.warn("Ignored: "+ list.asScala.mkString(separator))

    override def directories(list: util.List[String]): Unit =
      log.info("Directories: " + list.asScala.mkString(separator))
  }

  private def measureSink(log: ManagedLogger): MeasureEventSink = (module, fanIn, fanOut) =>
      log.info(f"  $module%20s --> fanIn: $fanIn%5d, fanOut: $fanOut%5d")

  private def strictAnalysisSink(log: ManagedLogger): StrictAnalysisEventSink = new StrictAnalysisEventSink {
    override def dependencyViolationsPresent(): Unit = log.error("The following dependencies violate the specification:")

    override def noDirectDependenciesViolationPresent(): Unit = log.error("The following direct dependencies violate the specification:")

    override def dependenciesCorrect(): Unit = log.info("No dependency violation detected")

    override def directDependenciesCorrect(): Unit = log.info("No direct dependency violation detected")

    override def dependencyViolation(sourceModule: String, destModule: String,
                                     expectedPath: util.List[String], actualPath: util.List[String]): Unit =
      log.error(f"  $sourceModule%s -> $destModule%s. Expected path: ${pathToString(expectedPath)}%s, Actual path: ${pathToString(actualPath)}%s")

    override def noDirectDependencyViolation(source: String, dest: String): Unit = log.error(s"  $source -> $dest")
  }

  private def looseAnalysisSink(log: ManagedLogger): LooseAnalysisEventSink = new LooseAnalysisEventSink {
    override def allDependenciesPresent(): Unit = log.info("All dependencies specified exist")

    override def undesiredDependencyViolationsPresent(): Unit = log.error("The following dependencies violate the specification:")

    override def absentDependencyViolation(source: String, dest: String): Unit = log.error(s"  $source -> $dest")

    override def absentDependencyViolationsPresent(): Unit = log.error("The following dependencies do not exist:")

    override def noUndesiredDependencies(): Unit = log.info("No dependency violation detected")

    override def undesiredDependencyViolation(sourceModule: String, destModule: String, path: util.List[String]): Unit =
      log.error(f"  $sourceModule%s -> $destModule%s. Actual path: ${pathToString(path)}%s")

  }

  private def pathToString(path: util.List[String]): String =
    if(path.isEmpty)
      "(empty)"
    else
      path.asScala.mkString(" -> ")

  def getExecutionMode(s: String): Option[ExecutionMode] = s match {
    case "strict" => Some(ExecutionMode.STRICT)
    case "loose" => Some(ExecutionMode.LOOSE)
    case _ => None
  }
}