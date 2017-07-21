import scala.util._
import ammonite.ops._
import ammonite.ops.ImplicitWd._

import $ivy.`org.wvlet::wvlet-log:1.2.3`
import wvlet.log._


class PredixException(s: String, e: Exception) extends Exception(s: String, e: Exception)

/*
 * StringUtils
 *
 */
object StringUtils {
  // stringToSeq
  def stringToSeq(s: String): Seq[String] = s.split(" ").toSeq.map(_.trim).filter(_ != "")

  // buildJsonResponse
  def buildJsonResponse(response: String): String = {
    val systemProvidedVars = response.toString.split("System-Provided:")(1).split("No user-defined env variables have been set")(0).trim.toString.replace("\n","").replace("'", "").replace("}{","},{").trim
    return "[" + systemProvidedVars + "]"
  }

  def checkCommandResultString(cr: String, logger: LoggerUtils) = {
    if (cr contains "ERROR") {
      logger.showAndAppendToLogFile("error", cr)
    } else if (cr contains "WARNING") {
      logger.showAndAppendToLogFile("warn", cr)
    } else {
      logger.showAndAppendToLogFile("info", cr)
    }
  }
}

/*
 * MapUtils
 *
 */
object MapUtils {

  // exitIfError
  def exitIfError(value: Map[String, String]) = {
    if (value.contains("errorCode"))
      System.exit(-1)
  }
}

/*
 * LoggerUtils
 *
 */
class LoggerUtils(logFolder: String, logFile: String) {

  private val logger = Logger.of[LoggerUtils]

  if (exists! pwd/logFolder) {
    rm! pwd/logFolder
  }
  mkdir! pwd/logFolder

  logger.resetHandler(new FileHandler(
    fileName = s"${pwd/logFolder/logFile}",
    formatter = LogFormatter.AppLogFormatter
  ))

  val pprint = (level: String, data: String) => level match {
    case "error" => println(s" $level: $data")
    case _ => println(data)
  }

  val info = (data: String) => logger.info(data)
  val warn = (data: String) => logger.warn(data)
  val error = (data: String) => logger.error(data)

  // showAndAppendToLogFile
  def showAndAppendToLogFile(level: String, data: String) = {
    level match {
      case "info"  => info(data)
      case "warn"  => warn(data)
      case "error" => error(data)
    }
    pprint(level, data)
  }

  // showAndAppendToLogFile
  def appendToLogFile(level: String, data: String) = level match {
    case "info"  => info(data)
    case "warn"  => warn(data)
    case "error" => error(data)
  }

}

/*
 * OSUtils
 *
 */
object OSUtils {
  // sleep
  def sleep(duration: Long) = Thread.sleep(duration)

  // readTextFile
  def readTextFile(pathToFile: Path): Try[String] = {
    Try(read(pathToFile))
  }

  // saveToFile
  def saveToFile(pathToFile: Path, data: String) = {
    write(pathToFile, data)
  }

  // deleteIfExistsAndCreate
  def deleteFolderIfExistsAndCreate(folderName: String) = {
    val folder = pwd/folderName
    if(exists! folder) {
      rm! folder
    }
    mkdir! folder
  }

  // deleteIfExistsAndCreate
  def deleteFolderIfExists(folderName: String) = {
    val folder = pwd/folderName
    if(exists! folder)
      rm! folder
  }
}

/*
 * NetUtils
 *
 */
object NetUtils {
  // checkInternet
  def checkInternet(url: String): Option[String] = {
    %%('curl, url).exitCode match {
      case x if(x == 0) => Some(" ** OK: Your internet connection works!")
      case x if(x != 0) => None
    }
  }

  // execNetCommand
  def execCommand(command: String): Option[CommandResult] = {
    val s = StringUtils.stringToSeq(command)
    val res: CommandResult = %%(s)
    res.exitCode match {
      case x if (x == 0) => Some(res)
      case x if (x != 0) => None
    }
  }
}

/*
 * GitUtils
 *
 */
object GitUtils {
  //cloneResos
  def cloneRepos(appReposUrl: String, appFolderName:String): Option[String] = {
     NetUtils.execCommand("git clone " + appReposUrl) match {
      case Some(x) =>
        if(x.exitCode == 0) {
          val appFolderInfo = stat! pwd/appFolderName
          Some(s""" ** ${appFolderInfo.name} cloned.""")
        } else {
          None
        }
      case None => None
    }
  }
}
