import scala.util._
import scalaj.http._
import ammonite.ops._
import ammonite.ops.ImplicitWd._

import $file.variables, variables._
import $file.misc_utils, misc_utils._

/*
 * PredixUtils
 *
 */
object PredixUtils {
  // cfLogin
  def cfLogin() = {
    %('cf, "login")
  } // End of cfLogin

  // cfPush
  def cfPush(appName: String, pathToApp: Path) = {
    val cmdString = s"""cf push $appName -f ${pathToApp.toString}/manifest.yml"""
    %(StringUtils.stringToSeq(cmdString))
  } // End of cfPush

  // doesItExists
  def doesItExists(command:String, name: String): Boolean = {
    val statementResult: Option[CommandResult] = NetUtils.execCommand(command)
    val s = statementResult.getOrElse(false)
    if(s.toString.contains(name)) true else false
  } // End of doesItExists

  // createService
  def createService(serviceName: String, serviceRequest: String): Option[String] = {
    if(doesItExists("cf s", serviceName)) {
      Some(s" ** WARNING: Service instance already exists: $serviceName")
    } else {
      NetUtils.execCommand(serviceRequest) match {
        case Some(s) =>
          s.exitCode match {
            case x if(x == 0) => Some(s" ** Service $serviceName created!")
            case x if(x != 0) => Some(s" ** ERROR creating the service $serviceName")
          }
        case None => None
      }
    }
  } // End of createService

  // createUAAService
  def createUAAService(
    uaaServiceName: String,
    uaaServicePlan:String,
    uaaInstanceName:String,
    uaaAdminSecret:String): Option[String] = {
      val createUAACommand = s"""cf cs $uaaServiceName $uaaServicePlan $uaaInstanceName -c {"adminClientSecret":"$uaaAdminSecret"} """
      createService(uaaInstanceName, createUAACommand)
  } // End of createUAAService

  // createTimeSeriesService
  def createTimeSeriesService(
    tsServiceName: String,
    tsServicePlan: String,
    tsInstanceName: String,
    trustedIssuerIds: String): Option[String] = {
      val createTSCommand = s"""cf cs $tsServiceName $tsServicePlan $tsInstanceName -c {"trustedIssuerIds":["$trustedIssuerIds"]} """
      createService(tsInstanceName, createTSCommand)
  } // End of createTimeSeriesService

  // createAssetService
  def createAssetService(
    assetServiceName: String,
    assetServicePlan: String,
    assetInstanceName: String,
    trustedIssuerIds: String): Option[String] = {
      val createAssetCommand = s"""cf cs $assetServiceName $assetServicePlan $assetInstanceName -c {"trustedIssuerIds":["$trustedIssuerIds"]} """
      createService(assetInstanceName, createAssetCommand)
  } // End of createAssetService

  // createSQLService
  def createSQLService(
    sqlServiceName: String,
    sqlServicePlan: String,
    sqlInstanceName: String,
    trustedIssuerIds: String): Option[String] = {
      val createSQLCommand = s"""cf cs $sqlServiceName $sqlServicePlan $sqlInstanceName -c {"trustedIssuerIds":["$trustedIssuerIds"]} """
      createService(sqlInstanceName, createSQLCommand)
  } // End of createSQLService

  
  // cfBind
  def cfBind(appName: String, serviceName: String): Option[String] = {
    val statementResult: Option[CommandResult] = NetUtils.execCommand(s"cf bs $appName $serviceName")
    statementResult match {
      case Some(s) =>
        s.exitCode match {
          case x if (x == 0) => Some(s" ** Service $serviceName binded to $appName.")
          case x if (x != 0) => Some(s" ** ERROR binding $appName to $serviceName")
        }
      case None => None
    }
  } // Enf of cfBind

  // cfUnbind
  def cfUnbind(appName: String, serviceName: String): Option[String] = {
    val statementResult: Option[CommandResult] = NetUtils.execCommand(s"cf us $appName $serviceName")
    statementResult match {
      case Some(x) =>
        if(x.exitCode != 0) {
          Some(s" ** ERROR unbinding $appName to $serviceName")
        } else {
          Some(s" ** Service $serviceName unbinded to $appName.")
        }
      case None => None
    }
  } // Enf of cfUnbind

  // cfRestage
  def cfRestage(appName: String): Option[String] = {
    val statementResult: Option[CommandResult] = NetUtils.execCommand(s"cf restage $appName")
    statementResult match {
      case Some(x) =>
        if(x.exitCode != 0) {
          Some(s" ** ERROR restaging $appName")
        } else {
          Some(s" ** DONE.")
        }
      case None => None
    }
  } // Enf of cfRestage

  // getVCAPJson
  def getVCAPJson(appName: String): Option[String] = {
    val statementResult: Option[CommandResult]  = NetUtils.execCommand(s"cf env $appName")
    statementResult match {
      case Some(s) =>
        s.exitCode match {
          case s if(s == 0) => Some(StringUtils.buildJsonResponse(statementResult.toString))
          case s if(s != 0) => None
        }
      case None => None
    }
  } // End of getVCAPJson

  // getUAAConfigfromVCAP
  def getUAAConfigfromVCAP(jsonContent: upickle.Js.Value) = (
      jsonContent("VCAP_SERVICES")(UAA_SERVICE_NAME)(0)("label").toString.replace("\"", ""),
      jsonContent("VCAP_SERVICES")(UAA_SERVICE_NAME)(0)("name").toString.replace("\"", ""),
      jsonContent("VCAP_SERVICES")(UAA_SERVICE_NAME)(0)("plan").toString.replace("\"", ""),
      jsonContent("VCAP_SERVICES")(UAA_SERVICE_NAME)(0)("credentials")("dashboardUrl").toString.replace("\"", ""),
      jsonContent("VCAP_SERVICES")(UAA_SERVICE_NAME)(0)("credentials")("issuerId").toString.replace("\"", ""),
      jsonContent("VCAP_SERVICES")(UAA_SERVICE_NAME)(0)("credentials")("subdomain").toString.replace("\"", ""),
      jsonContent("VCAP_SERVICES")(UAA_SERVICE_NAME)(0)("credentials")("uri").toString.replace("\"", ""),
      jsonContent("VCAP_SERVICES")(UAA_SERVICE_NAME)(0)("credentials")("zone")("http-header-value").toString.replace("\"", "")
    ) // End of getUAAConfigfromVCAP

  // getTimeSeriesConfigfromVCAP
  def getTimeSeriesConfigfromVCAP(jsonContent: upickle.Js.Value) = (
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("label").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("name").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("plan").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("credentials")("ingest")("uri").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("credentials")("ingest")("zone-http-header-value").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("credentials")("ingest")("zone-token-scopes")(0).toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("credentials")("ingest")("zone-token-scopes")(1).toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("credentials")("query")("uri").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(TIMESERIES_SERVICE_NAME)(0)("credentials")("query")("zone-token-scopes")(1).toString.replace("\"", "")
  ) // End of getTimeSeriesConfigfromVCAP

  // getAssetConfigfromVCAP
  def getAssetConfigfromVCAP(jsonContent: upickle.Js.Value) = (
    jsonContent("VCAP_SERVICES")(ASSET_SERVICE_NAME)(0)("label").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(ASSET_SERVICE_NAME)(0)("name").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(ASSET_SERVICE_NAME)(0)("plan").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(ASSET_SERVICE_NAME)(0)("credentials")("instanceId").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(ASSET_SERVICE_NAME)(0)("credentials")("scriptEngine_uri").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(ASSET_SERVICE_NAME)(0)("credentials")("uri").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(ASSET_SERVICE_NAME)(0)("credentials")("zone")("http-header-value").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(ASSET_SERVICE_NAME)(0)("credentials")("zone")("oauth-scope").toString.replace("\"", "")
  ) // End of getAssetConfigfromVCAP

  // getSQLConfigfromVCAP
  def getSQLConfigfromVCAP(jsonContent: upickle.Js.Value) = (
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("label").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("name").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("plan").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("credentials")("host").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("credentials")("port").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("credentials")("database").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("credentials")("instance_id").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("credentials")("username").toString.replace("\"", ""),
    jsonContent("VCAP_SERVICES")(SQL_SERVICE_NAME)(0)("credentials")("password").toString.replace("\"", "")
  )
   // End of getSQLConfigfromVCAP


} // Enf of PredixUtils object
