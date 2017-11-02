#!/usr/bin/env amm

/* Predix  Demo Bootstrap Script
 *
 * Run this script to instantiate the following services on Predix:
 * UAA, Timeseries, Asset and PostgreSQL.
 * The script configures each service with the necessary
 * authorities and scopes, create a UAA user, create UAA client id etc.
 * to provide a ready-to-use Predix environment for your demos.
 *
 * version: 0.1
 * copyright: 2017 - Mirco Veltri
 * license: GPL, see LICENSE for more details
 */

import scala.util._
import ammonite.ops._
import ammonite.ops.ImplicitWd._

import $file.src.variables, variables._
import $file.src.pidhos, pidhos._
import $file.src.misc_utils, misc_utils._
import $file.src.predix_utils, predix_utils._
import $file.src.uaa_utils, uaa_utils._

@main
def main() = {
  val EMPTY_STRING = "__THIS_IS_AN_ERROR_STRING__"
  // Delete cloned app directory
  OSUtils.deleteFolderIfExists(TEMP_HELLO_APP)
  // Logger setup
  val appLogger = new LoggerUtils(LOGS_FOLDER, "predix-demo.log")
  appLogger.showAndAppendToLogFile("info", "\n===========================================================================")

  // Check if internet connection works
  appLogger.showAndAppendToLogFile("info", "\n * Checking internet connection...")
  NetUtils.checkInternet("google.com") match {
    case Some(i) => appLogger.showAndAppendToLogFile("info", i)
    case None => {
      appLogger.showAndAppendToLogFile("error", "** ERROR: Check your internet connection.")
      System.exit(-1)
    }
  }

  // Login to Predix Cloud Foundry
  appLogger.showAndAppendToLogFile("info", "\n * Logging to Predix..., Current Directory = " + cwd)
  PredixUtils.cfLogin()

  // Clone Starter App for binding
  appLogger.showAndAppendToLogFile("info", s"\n * Cloning Github repos $TEMP_APP_GIT_HUB_URL ...")
  GitUtils.cloneRepos(
    TEMP_APP_GIT_HUB_URL,
    TEMP_HELLO_APP) match {
      case Some(s) => appLogger.showAndAppendToLogFile("info", s.toString)
      case None => {
        appLogger.showAndAppendToLogFile("error", " ** ERROR cloning the project repository.")
        System.exit(-1)
      }
    }

  // Push the Starter App to Predix
  appLogger.showAndAppendToLogFile("info","\n * Pushing the app to Predix...")
  appLogger.showAndAppendToLogFile("info", s""" ** cf push $FRONT_END_APP_NAME -f ${pwd/TEMP_HELLO_APP.toString}/manifest.yml""")
  PredixUtils.cfPush(
    FRONT_END_APP_NAME,
    pwd/TEMP_HELLO_APP)

  /***********  UAA Service ***********/

  // Create UAA Service
  appLogger.showAndAppendToLogFile("info", s"\n * Create service if it does not exist: $UAA_SERVICE_NAME")
  PredixUtils.createUAAService(
    UAA_SERVICE_NAME,
    UAA_PLAN,
    UAA_INSTANCE_NAME,
    UAA_ADMIN_SECRET) match {
      case Some(s) => StringUtils.checkCommandResultString(s, appLogger)
      case None => System.exit(-1)
    }

  // Binding Sample App to UAA
  appLogger.showAndAppendToLogFile("info", s"\n * Binding $UAA_INSTANCE_NAME to $FRONT_END_APP_NAME...")
  PredixUtils.cfBind(FRONT_END_APP_NAME, UAA_INSTANCE_NAME) match {
    case Some(s) => StringUtils.checkCommandResultString(s, appLogger)
    case None => System.exit(1)
  }

  // Getting app's ENV variables
  appLogger.showAndAppendToLogFile("info", s"\n * Getting VCAP for $FRONT_END_APP_NAME ...")
  var uaaDetails = None: Option[PredixUAADetails]
  PredixUtils.getVCAPJson(FRONT_END_APP_NAME) match {
    case Some(s) =>
      val u = upickle.json.read(s)(0)
      uaaDetails = Some((PredixUAADetails.apply _).tupled(PredixUtils.getUAAConfigfromVCAP(u)))
    case None => System.exit(-1)
  }

  // extracted to be used for services creation
  val uaaIssuerId = uaaDetails match {
    case Some(s) => s.issuerId
    case None => EMPTY_STRING
  }

  /***********  TimeSeries Service ***********/

  // Create TimeSeries Service
  appLogger.showAndAppendToLogFile("info", s"\n * Create service if it does not exist: $TIMESERIES_SERVICE_NAME")
  PredixUtils.createTimeSeriesService(
    TIMESERIES_SERVICE_NAME,
    TIMESERIES_SERVICE_PLAN,
    TIMESERIES_INSTANCE_NAME,
    uaaIssuerId) match {
      case Some(s) => StringUtils.checkCommandResultString(s, appLogger)
      case None => System.exit(-1)
    }

  // Binding Sample App to TimeSeries Service
  appLogger.showAndAppendToLogFile("info", s"\n * Binding $TIMESERIES_INSTANCE_NAME to $FRONT_END_APP_NAME...")
  PredixUtils.cfBind(FRONT_END_APP_NAME, TIMESERIES_INSTANCE_NAME) match {
    case Some(s) => StringUtils.checkCommandResultString(s, appLogger)
    case None => System.exit(1)
  }

  /***********  Asset Service ***********/

  // Create Asset Service
  appLogger.showAndAppendToLogFile("info", s"\n * Create service if it does not exist: $ASSET_SERVICE_NAME")
  PredixUtils.createAssetService(
    ASSET_SERVICE_NAME,
    ASSET_SERVICE_PLAN,
    ASSET_INSTANCE_NAME,
    uaaIssuerId) match {
      case Some(s) => StringUtils.checkCommandResultString(s, appLogger)
      case None => System.exit(-1)
    }

  // Binding Sample App to Asset Service
  appLogger.showAndAppendToLogFile("info", s"\n * Binding $ASSET_INSTANCE_NAME to $FRONT_END_APP_NAME...")
  PredixUtils.cfBind(FRONT_END_APP_NAME, ASSET_INSTANCE_NAME) match {
    case Some(s) => StringUtils.checkCommandResultString(s, appLogger)
    case None => System.exit(-1)
  }

  /***********  SQL Service ***********/

  // Create SQL Service
  appLogger.showAndAppendToLogFile("info", s"\n * Create service if it does not exist: $SQL_SERVICE_NAME")
  PredixUtils.createSQLService(
    SQL_SERVICE_NAME,
    SQL_SERVICE_PLAN,
    SQL_INSTANCE_NAME,
    uaaIssuerId) match {
      case Some(s) => StringUtils.checkCommandResultString(s, appLogger)
      case None => System.exit(-1)
    }
  // Binding Sample App to SQL Service
  appLogger.showAndAppendToLogFile("info", s"\n * Binding $SQL_INSTANCE_NAME to $FRONT_END_APP_NAME...")
  PredixUtils.cfBind(FRONT_END_APP_NAME, SQL_INSTANCE_NAME) match {
    case Some(s) => StringUtils.checkCommandResultString(s, appLogger)
    case None => System.exit(-1)
  }

  /***********  Getting app's ENV variables ***********/
  var tsDetails = None: Option[PredixTimeSeriesDetails]
  var assetDetails = None: Option[PredixAssetDetails]
  var sqlDetails = None: Option[PredixSQLDetails]
  var configJsonFileContent = None: Option[String]

  // Getting app's ENV variables for TimeSeries, Asset and SQL
  appLogger.showAndAppendToLogFile("info", s"\n * Getting VCAP for $FRONT_END_APP_NAME ...")
  PredixUtils.getVCAPJson(FRONT_END_APP_NAME) match {
    case Some(s) =>
      val u = upickle.json.read(s)(0)
      tsDetails = Some( (PredixTimeSeriesDetails.apply _).tupled(PredixUtils.getTimeSeriesConfigfromVCAP(u)) )
      assetDetails = Some( (PredixAssetDetails.apply _).tupled(PredixUtils.getAssetConfigfromVCAP(u)) )
      sqlDetails = Some( (PredixSQLDetails.apply _).tupled(PredixUtils.getSQLConfigfromVCAP(u)) )
      configJsonFileContent = Some(u.toString)
    case None => System.exit(-1)
  }

  // Show app's ENV variables for UAA
  appLogger.showAndAppendToLogFile("info", "\n** Predix UAA Details:")
  appLogger.showAndAppendToLogFile("info", uaaDetails.getOrElse(EMPTY_STRING).toString)

  // Show app's ENV variables for TimeSeries
  appLogger.showAndAppendToLogFile("info", "** Predix TimeSeries Details:")
  appLogger.showAndAppendToLogFile("info", tsDetails.getOrElse(EMPTY_STRING).toString)

  // Show app's ENV variables for Asset
  appLogger.showAndAppendToLogFile("info", "** Predix Asset Details:")
  appLogger.showAndAppendToLogFile("info", assetDetails.getOrElse(EMPTY_STRING).toString)

  // Show app's ENV variables for SQL
  appLogger.showAndAppendToLogFile("info", "** Predix Postgresql Details:")
  appLogger.showAndAppendToLogFile("info", sqlDetails.getOrElse(EMPTY_STRING).toString)

  // Save the entire services details to a local Json file
  appLogger.showAndAppendToLogFile("info", "===========================================================================")
  OSUtils.deleteFolderIfExistsAndCreate(CONFIGS_FOLDER_NAME)
  OSUtils.saveToFile(pwd/CONFIGS_FOLDER_NAME/CONFIGS_FILE_NAME, configJsonFileContent.getOrElse(EMPTY_STRING))
  appLogger.showAndAppendToLogFile("info", "\n*** JSON file saved as: " + pwd/CONFIGS_FOLDER_NAME/CONFIGS_FILE_NAME)
  appLogger.showAndAppendToLogFile("info", "\n===========================================================================")

  // Read the services details Json file
  val servicesConfigs = OSUtils.readTextFile(pwd/CONFIGS_FOLDER_NAME/CONFIGS_FILE_NAME) match {
    case Success(lines) => upickle.json.read(lines.toString)
    case Failure(f) =>
      appLogger.showAndAppendToLogFile("error", f.toString)
      System.exit(-1)
  }

  /***********  UAA operations ***********/

  // Get UAA token for Admin
  val adminToken: Map[String, String] = UAAUtils.getToken(uaaDetails.get, true, appLogger)
  MapUtils.exitIfError(adminToken)
  appLogger.appendToLogFile("info", " ** Admin Token: " + adminToken("token"))

  // Add a UAA User
  val addUserRes: Map[String, String] =
    UAAUtils.addUser(uaaDetails.get, UAA_USER_NAME, UAA_USER_PASSWORD, UAA_USER_EMAIL, adminToken("token"), appLogger)
  MapUtils.exitIfError(addUserRes)
  appLogger.showAndAppendToLogFile("info", " ** User Id: " + addUserRes("userId"))
  appLogger.showAndAppendToLogFile("info", " ** Username: " + addUserRes("userName"))

  // Create one generic UAA Group and the ones needed by Time Series Service adding the previously created user to them.
  val createGroupRes: Map[String, String] =
    UAAUtils.createGroupAndAddUser(uaaDetails.get, UAA_GROUP_NAME, addUserRes, adminToken("token"), appLogger)
  MapUtils.exitIfError(createGroupRes)
  appLogger.showAndAppendToLogFile("info", " ** Group Id: " + createGroupRes("groupId"))
  appLogger.showAndAppendToLogFile("info", " ** Group Name: " + createGroupRes("groupName"))

  val createTSUserGroupRes: Map[String, String] =
    UAAUtils.createGroupAndAddUser(uaaDetails.get, tsDetails.get.userScope, addUserRes, adminToken("token"), appLogger)
  MapUtils.exitIfError(createTSUserGroupRes)
  appLogger.showAndAppendToLogFile("info", " ** Group Id: " + createTSUserGroupRes("groupId"))
  appLogger.showAndAppendToLogFile("info", " ** Group Name: " + createTSUserGroupRes("groupName"))

  val createTSIngestGroupRes: Map[String, String] =
    UAAUtils.createGroupAndAddUser(uaaDetails.get, tsDetails.get.ingestScope, addUserRes, adminToken("token"), appLogger)
  MapUtils.exitIfError(createTSIngestGroupRes)
  appLogger.showAndAppendToLogFile("info", " ** Group Id: " + createTSIngestGroupRes("groupId"))
  appLogger.showAndAppendToLogFile("info", " ** Group Name: " + createTSIngestGroupRes("groupName"))

  val createTSQueryGroupRes: Map[String, String] =
    UAAUtils.createGroupAndAddUser(uaaDetails.get, tsDetails.get.queryScope, addUserRes, adminToken("token"), appLogger)
  MapUtils.exitIfError(createTSQueryGroupRes)
  appLogger.showAndAppendToLogFile("info", " ** Group Id: " + createTSQueryGroupRes("groupId"))
  appLogger.showAndAppendToLogFile("info", " ** Group Name: " + createTSQueryGroupRes("groupName"))

  // Create a UAA Client with all the required scopes, authorities, etc.
  val createClientRes: Map[String, String] =
    UAAUtils.addClient(uaaDetails.get, UAA_CLIENTID_GENERIC, UAA_CLIENTID_GENERIC_SECRET, tsDetails.get, assetDetails.get, adminToken("token"), appLogger)
  MapUtils.exitIfError(createClientRes)
  appLogger.showAndAppendToLogFile("info", " ** Client Id: " + createClientRes("clientId"))
  appLogger.showAndAppendToLogFile("info", " ** Client Name: " + createClientRes("clientName"))

  // Close the script
  appLogger.showAndAppendToLogFile("info", "\n===========================================================================")
  appLogger.showAndAppendToLogFile("info", "\n*** DONE! Log file saved as: " + pwd/'logs/"predix-demo.log")
  appLogger.showAndAppendToLogFile("info", "\n===========================================================================\n")

}
