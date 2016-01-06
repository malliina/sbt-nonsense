import com.malliina.sbtutils.SbtProjects
import sbt._
import sbt.Keys._
import sbt.complete.DefaultParsers._

/**
 * A scala build file template.
 */
object NonsenseBuild extends Build {

  lazy val template = SbtProjects.testableProject("sbt-nonsense").settings(projectSettings: _*)

  val promptIn = inputKey[Unit]("Prompts and prints")
  val promptVersion = taskKey[String]("Prompts for a version, suggesting a default")
  val inMaybe = inputKey[Option[String]]("Reads an optional string.")
  val build = inputKey[Unit]("Does something, prompting if no input is given")

  lazy val projectSettings = commandLineSettings ++ Seq(
    version := "0.0.1",
    scalaVersion := "2.11.7",
    fork in Test := true,
    libraryDependencies += "com.malliina" %% "util-base" % "0.9.0"
  )

  val readCommandLineOrPrompt = Def.inputTaskDyn {
    val args = spaceDelimited("<arg>").parsed.headOption.map(_.trim).filter(_.nonEmpty)
    args.map(s => Def.task(s)) getOrElse promptVersion
  }

  def commandLineSettings = Seq(
    promptVersion := {
      val suggestedValue = version.value
      val userIn = SimpleReader.readLine(s"Specify version [$suggestedValue]: ").map(_.trim).filter(_.nonEmpty)
      userIn getOrElse suggestedValue
    },
    promptIn := {
      val log = streams.value.log
      val confirmedValue = promptVersion.value
      log info s"Using $confirmedValue"
    },
    inMaybe := {
      val params = spaceDelimited("<arg>").parsed.toList
      params.headOption.map(_.trim).filter(_.nonEmpty)
    },
    build := {
      val log = streams.value.log
      val input = readCommandLineOrPrompt.evaluated
      log info s"Running with $input"
    }
  )
}
