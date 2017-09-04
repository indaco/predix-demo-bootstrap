// uaa_utils.sc
import scalaj.http._
import ammonite.ops._
import ammonite.ops.ImplicitWd._

import $ivy.`commons-codec:commons-codec:1.10`
import org.apache.commons.codec.binary.{ Base64 => ApacheBase64 }

import $file.variables, variables._
import $file.pidhos, pidhos._
import $file.misc_utils, misc_utils.LoggerUtils

/* Base64 Utils */
object Base64 {
  def decode(encoded: String) = new String(ApacheBase64.decodeBase64(encoded.getBytes))
  def encode(decoded: String) = new String(ApacheBase64.encodeBase64(decoded.getBytes))
}

/* UAA Utils */
object UAAUtils {
  // getToken
  def getToken(
    uaaDetails: PredixUAADetails,
    isAdmin: Boolean,
    logger: LoggerUtils): Map[String, String] = {
      logger.showAndAppendToLogFile("info", "\n * Executing getToken...")
      val realmStr = if(isAdmin) s"""admin:$UAA_ADMIN_SECRET""" else s"""$UAA_CLIENTID_GENERIC:$UAA_CLIENTID_GENERIC_SECRET"""
      val authKey = Base64.encode(realmStr)
      val h = Map(
        "content-type"  -> "application/x-www-form-urlencoded",
        "Authorization" -> s"""Basic $authKey""")
      val res = Http(uaaDetails.issuerId)
        .headers(h)
        .param("grant_type", "client_credentials")
        .asString
      val parsedResBody = upickle.json.read(res.body)
      res.isSuccess match {
        case true =>
          logger.showAndAppendToLogFile("info", s""" ** Logged in.""")
          val tokenType = parsedResBody("token_type").toString.replace("\"","")
          val accessToken = parsedResBody("access_token").toString.replace("\"","")
          Map(
            "okCode" -> res.code.toString,
            "token" -> s"""$tokenType $accessToken"""
          )
        case false =>
          logger.showAndAppendToLogFile("error", s" ** ERROR logging to UAA: ${parsedResBody("message")}")
          Map(
            "errorCode" -> res.code.toString,
            "errorMessage" -> parsedResBody("message").toString.replace("\"", "")
          )
      }
  } // End of getToken

  // addUser
  def addUser(
    uaaDetails: PredixUAADetails,
    userName: String,
    password: String,
    email: String,
    adminToken: String,
    logger: LoggerUtils): Map[String, String] = {
      logger.showAndAppendToLogFile("info", "\n * Executing addUser...")
      val createUserBodyAsJson = (UAAUser.apply _).tupled((userName, password, Array(email))).toJsonString
      val h = Map(
        "content-type"  -> "application/json",
        "Authorization" -> adminToken)
      val res = Http(uaaDetails.uri+"/Users")
        .headers(h)
        .postData(createUserBodyAsJson)
        .asString
      val parsedResBody = upickle.json.read(res.body)
      res.isSuccess match {
        case true  =>
          logger.showAndAppendToLogFile("info", s""" ** User created.""");
          Map(
            "userId" -> parsedResBody("id").toString.replace("\"", ""),
            "userName" -> parsedResBody("userName").toString.replace("\"", "")
          )
        case false =>
          logger.showAndAppendToLogFile("error", s" ** ERROR Adding User: ${parsedResBody("message")}")
          Map(
            "errorCode" -> res.code.toString,
            "errorMessage" -> parsedResBody("message").toString.replace("\"", "")
          )
      }
  } // End of addUser

  // createGroupAndAddUser
  def createGroupAndAddUser(
    uaaDetails: PredixUAADetails,
    groupName: String,
    user: Map[String, String],
    adminToken: String,
    logger: LoggerUtils): Map[String, String] = {
      logger.showAndAppendToLogFile("info", "\n * Executing createGroupAndAddUser...")
      val createGroupBodyAsJson = (UAAGroup.apply _).tupled( (groupName, groupName, Array(user("userId"))) ).toJsonString
      val h = Map(
        "content-type"  -> "application/json",
        "Authorization" -> adminToken)
      val res = Http(uaaDetails.uri+"/Groups")
        .headers(h)
        .postData(createGroupBodyAsJson)
        .asString
      val parsedResBody = upickle.json.read(res.body)
      res.isSuccess match {
        case true =>
          logger.showAndAppendToLogFile("info",s""" ** Group created and "${user("userName")}" is a member of it.""")
          Map(
            "groupId" -> parsedResBody("id").toString.replace("\"", ""),
            "groupName" -> parsedResBody("displayName").toString.replace("\"", "")
          )
        case false =>
          logger.showAndAppendToLogFile("error", s""" ** ERROR Creating the UAA Group: ${parsedResBody("message")}""")
          Map(
            "errorCode" -> res.code.toString,
            "errorMessage" -> parsedResBody("message").toString.replace("\"", "")
          )
      }
  } // End of createGroupAndAddUser

  // addClient
  def addClient(
    uaaDetails: PredixUAADetails,
    clientId: String,
    clientSecret: String,
    tsDetails: PredixTimeSeriesDetails,
    assetDetails: PredixAssetDetails,
    adminToken: String,
    logger: LoggerUtils): Map[String, String] = {
      logger.showAndAppendToLogFile("info", "\n * Executing addClient...")
      val createClientBodyAsJson = _generateCreateClientBodyAsJson(clientId, clientSecret, tsDetails, assetDetails)
      val h = Map(
        "content-type"  -> "application/json",
        "Authorization" -> adminToken
      )
      val res = Http(uaaDetails.uri+"/oauth/clients")
        .headers(h)
        .postData(createClientBodyAsJson)
        .asString
      val parsedResBody = upickle.json.read(res.body)
      //println(parsedResBody.toString)
      res.isSuccess match {
        case true => {
          logger.showAndAppendToLogFile("info", " ** Client created")
          Map(
            "clientId" -> parsedResBody("client_id").toString.replace("\"", ""),
            "clientName" -> parsedResBody("name").toString.replace("\"", "")
          )
        }
        case false => {
          logger.showAndAppendToLogFile("error", s""" ** ERROR Creating Client: error:(${parsedResBody("error")}), message: ${parsedResBody("error_description")}""")
          Map(
            "errorCode" -> res.code.toString,
            "errorMessage" -> parsedResBody("message").toString.replace("\"", "")
          )
        }
      }
  } // addClient


  /*********** Private methods ***********/

  private def _generateCreateClientBodyAsJson(
    clientId: String,
    clientSecret: String,
    tsDetails: PredixTimeSeriesDetails,
    assetDetails: PredixAssetDetails): String = {
      val authorizedGrantTypes = _generateClientGrantTypes()
      val allowProviders = _generateClientAllowProviders()
      val accessTokenValidity = 31557600 // 1y
      val refreshTokenValidity = 31557600 // 1y
      val autoApprove = Array("openid")
      val scopes = _generateClientScopeList(tsDetails, assetDetails)
      val authorities = _generateClientAuthoritiesList(tsDetails, assetDetails)
      val redirectUri = Array("http://test1.com")

      val createClientBody = (UAAClient.apply _).tupled(clientId, clientSecret, authorizedGrantTypes, allowProviders, accessTokenValidity, refreshTokenValidity, autoApprove, scopes, authorities, redirectUri)
      return createClientBody.toJsonString
  }

  private def _generateClientScopeList(
    tsDetails: PredixTimeSeriesDetails,
    assetDetails: PredixAssetDetails): Array[String] = {
      Array("uaa.resource", "openid", "scim.read", "scim.write",
        tsDetails.userScope, tsDetails.ingestScope, tsDetails.queryScope,
        assetDetails.oauthScope)
  }

  private def _generateClientGrantTypes(): Array[String] = {
    Array("authorization_code", "password", "client_credentials", "refresh_token")
  }

  private def _generateClientAuthoritiesList(
    tsDetails: PredixTimeSeriesDetails,
    assetDetails: PredixAssetDetails): Array[String] = {
      Array("uaa", "uaa.resources", "openid", "scim.read", "scim.write",
        tsDetails.userScope, tsDetails.ingestScope, tsDetails.queryScope,
        assetDetails.oauthScope)
  }

  private def _generateClientAllowProviders(): Array[String] = {
    Array("uaa")
  }

} // End of UAAUtils object
