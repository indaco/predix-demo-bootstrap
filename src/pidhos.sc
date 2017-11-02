// pidhos.sc
// plain immutable data-handling objects

/* PredixUAADetails */
case class PredixUAADetails(
  label: String,
  name: String,
  plan: String,
  dashboardUrl: String,
  issuerId: String,
  subdomain: String,
  uri: String,
  zoneId: String
) {
  override def toString() =
    s"""
    - label: $label
    - name: $name
    - plan: $plan
    - dashboardUrl: $dashboardUrl
    - issuerId: $issuerId
    - subdomain: $subdomain
    - uri: $uri
    - zoneId: $zoneId
    """
}

/* PredixTimeSeriesDetails */
case class PredixTimeSeriesDetails(
  label: String,
  name: String,
  plan: String,
  ingestUri: String,
  zoneId: String,
  userScope: String,
  ingestScope: String,
  queryUri: String,
  queryScope: String
) {
  override def toString() =
    s"""
    - label: $label
    - name: $name
    - plan: $plan
    - ingestUri: $ingestUri
    - zoneId: $zoneId
    - userScope: $userScope
    - ingestScope: $ingestScope
    - queryUri: $queryUri
    - queryScope: $queryScope
    """
}

/* PredixAssetDetails */
case class PredixAssetDetails(
  label: String,
  name: String,
  plan: String,
  instanceId: String,
  scriptEngineUri: String,
  uri: String,
  zoneId: String,
  oauthScope: String
) {
  override def toString() =
    s"""
    - label: $label
    - name: $name
    - plan: $plan
    - instanceId: $instanceId
    - scriptEngineUri: $scriptEngineUri
    - uri: $uri
    - zoneId: $zoneId
    - oauthScope: $oauthScope
    """
}

/* PredixSQLDetails */
case class PredixSQLDetails(
  label: String,
  name: String,
  plan: String,
  hostname: String,
  port: String,
  database: String,
  uuid: String,
  username: String,
  password: String
) {
  override def toString() =
    s"""
    - label: $label
    - name: $name
    - plan: $plan
    - hostname: $hostname
    - port: $port
    - database: $database
    - uuid: $uuid
    - username: $username
    - password: $password
    """
}

/* UAA User */
case class UAAUser(
  userName: String,
  password: String,
  emails: Array[String]) {

  def toJsonString = {
    //"{\"userName\":\"" + username + "\", \"password\":\"" + password + "\", \"emails\":[{\"value\":\"" + emails(0) +"\"}]}"
    val email = emails(0);
    s""" {"userName":"$userName", "password":"$password", "emails":[{"value":"$email"}]} """
  }
}

/* UAA Group **/
case class UAAGroup(
  displayName: String,
  description: String,
  members: Array[String]
) {
  def toJsonString() = {
    val member = members(0);
    s""" {"displayName":"$displayName", "description": "$description", "members":[{"origin": "uaa" , "type": "USER" ,"value":"$member"}]} """
  }
}

/* UAA Client */
case class UAAClient(
  clientId: String,
  clientSecret: String,
  authorizedGrantTypes: Array[String],
  allowProviders: Array[String],
  accessTokenValidity: Int,
  refreshTokenValidity: Int,
  autoApprove: Array[String],
  scopes: Array[String],
  authorities: Array[String],
  redirectUri: Array[String]
  ) {

  def toJsonString: String = {
    val authorizedGrantTypesJson = upickle.default.write(authorizedGrantTypes)
    val allowProvidersJson = upickle.default.write(allowProviders)
    val autoApproveJson = upickle.default.write(autoApprove)
    val scopesJson = upickle.default.write(scopes)
    val authoritiesJson = upickle.default.write(authorities)
    val redirectUriJson = upickle.default.write(redirectUri)
    s""" {"client_id": "$clientId", "client_secret": "$clientSecret", "name": "$clientId", "authorized_grant_types": $authorizedGrantTypesJson, "allowedproviders": $allowProvidersJson ,"authorities": $authoritiesJson, "scope": $scopesJson, "redirect_uri": $redirectUriJson,"autoapprove": $autoApproveJson, "access_token_validity": $accessTokenValidity, "refresh_token_validity": $refreshTokenValidity} """
  }
}
