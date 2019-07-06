package com.github.fburato.highwheelmodulessbt

import java.util

import sbt.Keys._
import sbt._
import com.github.fburato.highwheelmodules.core.AnalyserFacade
import com.github.fburato.highwheelmodules.core.AnalyserFacade.EventSink._
import com.github.fburato.highwheelmodules.core.AnalyserFacade._
import com.github.fburato.highwheelmodules.utils.Pair
import sbt.internal.util.ManagedLogger
import scala.collection.JavaConverters._

object AnalyserPlugin  extends AutoPlugin {

  object autoImport {
    val highwheelSpecFiles = settingKey[Seq[File]]("Paths to the specification files")
    val highwheelAnalysisPaths = settingKey[Seq[File]]("Projects to add to the analysis")
    val highwheelAnalyse = taskKey[Unit]("Analyse output directories after compiling")
    val highwheelBaseAnalyseTask = taskKey[Unit]("Analyse output directories")
    val highwheelEvidenceLimit = settingKey[Option[Int]]("Amount of dependencies to show in case of error, None for all the evidence")
  }

  import autoImport._

  override lazy val projectSettings = Seq(
    highwheelSpecFiles := Seq(baseDirectory.value / "spec.hwm"),
    highwheelAnalysisPaths := Seq((classDirectory in Compile).value),
    highwheelEvidenceLimit := Some(0),
    highwheelBaseAnalyseTask := {
      val log = streams.value.log
      Analyser(log,highwheelSpecFiles.value,highwheelAnalysisPaths.value,highwheelEvidenceLimit.value)
    },
    highwheelAnalyse := (highwheelBaseAnalyseTask dependsOn (compile in Compile)).value
  )
}

object Analyser {

  def apply(log: ManagedLogger, specFiles: Seq[File], analysisPaths: Seq[File],evidenceLimit: Option[Int]): Unit = {
    val paths = specFiles.map(_.getAbsolutePath())
    log.info(s"Using specification files: '${paths.mkString(",")}'")

    val facade = new AnalyserFacade(printer(log),pathSink(log),measureSink(log),strictAnalysisSink(log,evidenceLimit),looseAnalysisSink(log,evidenceLimit))
    facade.runAnalysis(new util.ArrayList(analysisPaths.map{ f => f.getAbsolutePath}.asJavaCollection),
      new util.ArrayList(paths.asJavaCollection),
      evidenceLimit.map(new Integer(_)).asJava
    )
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

  private def strictAnalysisSink(log: ManagedLogger, evidenceLimit: Option[Int]): StrictAnalysisEventSink = new StrictAnalysisEventSink {
    override def dependencyViolationsPresent(): Unit = log.error("The following dependencies violate the specification:")

    override def noDirectDependenciesViolationPresent(): Unit = log.error("The following direct dependencies violate the specification:")

    override def dependenciesCorrect(): Unit = log.info("No dependency violation detected")

    override def directDependenciesCorrect(): Unit = log.info("No direct dependency violation detected")

    override def dependencyViolation(sourceModule: String, destModule: String,
                                     expectedPath: util.List[String], actualPath: util.List[String], usagePath: util.List[util.List[Pair[String,String]]]): Unit = {
      log.error(f"  $sourceModule%s -> $destModule%s. Expected path: ${pathToString(expectedPath)}%s, Actual module path: ${pathToString(actualPath)}%s")
      def logEvidence(): Unit = {
        log.error("    Actual usage path:")
        printEvidence(log, actualPath, usagePath)
      }
      evidenceLimit match {
        case None => logEvidence()
        case Some(x) if x > 0 => logEvidence()
        case _ => ()
      }
    }

    override def noDirectDependencyViolation(source: String, dest: String): Unit = log.error(s"  $source -> $dest")
  }

  private def looseAnalysisSink(log: ManagedLogger, evidenceLimit: Option[Int]): LooseAnalysisEventSink = new LooseAnalysisEventSink {
    override def allDependenciesPresent(): Unit = log.info("All dependencies specified exist")

    override def undesiredDependencyViolationsPresent(): Unit = log.error("The following dependencies violate the specification:")

    override def absentDependencyViolation(source: String, dest: String): Unit = log.error(s"  $source -> $dest")

    override def absentDependencyViolationsPresent(): Unit = log.error("The following dependencies do not exist:")

    override def noUndesiredDependencies(): Unit = log.info("No dependency violation detected")

    override def undesiredDependencyViolation(sourceModule: String, destModule: String, path: util.List[String], usagePath: util.List[util.List[Pair[String,String]]]): Unit = {
      log.error(f"  $sourceModule%s -> $destModule%s, Actual module path: ${pathToString(path)}%s\n")
      def logEvidence(): Unit = {
        log.error("    Actual usage path:")
        printEvidence(log, path, usagePath)
      }
      evidenceLimit match {
        case None => logEvidence()
        case Some(x) if x > 0 => logEvidence()
        case _ => ()
      }
    }
  }

  private def pathToString(path: util.List[String]): String =
    if(path.isEmpty)
      "(empty)"
    else
      path.asScala.mkString(" -> ")

  private def printEvidence(log: ManagedLogger, modulePath: util.List[String], usagePath: util.List[util.List[Pair[String,String]]]): Unit = {
    def scalaVersion(modulePath: List[String], connections: List[List[Pair[String,String]]]): Unit = (modulePath, connections)  match {
      case (source :: dest :: otherModules, sourceEvidences :: otherEvidences) =>
        log.error(s"      $source -> $dest:")
        for(evidence <- sourceEvidences)
          yield log.error(s"        ${evidence.first} -> ${evidence.second}")
        scalaVersion(dest :: otherModules, otherEvidences)
      case (_,_) => ()
    }

    scalaVersion(modulePath.asScala.toList,usagePath.asScala.toList map ( l => l.asScala.toList))
  }
}