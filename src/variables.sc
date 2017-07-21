/***********  Variable used to name all the service instances ***********/
val INSTANCE_PREPENDER = "your-name-predix-demo"

/***********  Front-end Configurations ***********/
// Name for your Frone End Application
val FRONT_END_APP_NAME = INSTANCE_PREPENDER + "-hello"

/***********  Output Folder Configurations ***********/
val CONFIGS_FOLDER_NAME = "configs"
val CONFIGS_FILE_NAME = "services_details.json"

/***********  Logs Configurations ***********/
val LOGS_FOLDER = "logs"

/**************************
# Optional configurations
**************************/

// GITHUB repo to pull predix-helloworls-webapp
val TEMP_APP_GIT_HUB_URL = "https://github.com/PredixDev/Predix-HelloWorld-WebApp.git"
// Name for the temp_app application
val TEMP_HELLO_APP = "Predix-HelloWorld-WebApp"

/***********  UAA Configurations ***********/

val UAA_SERVICE_NAME = "predix-uaa"
val UAA_PLAN = "Free"

// Name of your UAA instance - default already set
val UAA_INSTANCE_NAME = INSTANCE_PREPENDER + "-uaa"

/***********  Predix TimeSeries Configurations ***********/

// The name of the TimeSeries service you are binding to - default already set
val TIMESERIES_SERVICE_NAME = "predix-timeseries"

// Name of the TimeSeries plan (eg: Free) - default already set
val TIMESERIES_SERVICE_PLAN = "Free"

// Name of your TimeSeries instance - default already set
val TIMESERIES_INSTANCE_NAME = INSTANCE_PREPENDER + "-ts"

/***********  Predix Asset Configurations ***********/

// The name of the Asset service you are binding to - default already set
val ASSET_SERVICE_NAME = "predix-asset"

// Name of the Asset plan (eg: Free) - default already set
val ASSET_SERVICE_PLAN = "Free"

// Name of your Asset instance - default already set
val ASSET_INSTANCE_NAME = INSTANCE_PREPENDER + "-asset"

/***********  PostgreSQL Configurations ***********/

// The name of the PostgreSQL service you are binding to - default already set
val SQL_SERVICE_NAME = "postgres"

// Name of the PostgreSQL plan (eg: Free) - default already set
val SQL_SERVICE_PLAN = " shared-nr"

// Name of your PostgreSQL instance - default already set
val SQL_INSTANCE_NAME = INSTANCE_PREPENDER + "-sql"


/******************************************/
/*********** UAA Configurations ***********/
/******************************************/

// The username of the new user to authenticate with the application
val UAA_USER_NAME = "app_user"

// The email address of username above
val UAA_USER_EMAIL = "app_user@example.com"

// The password of the user above
val UAA_USER_PASSWORD = "secret"

// The secret of the Admin client ID (Administrator Credentails)
val UAA_ADMIN_SECRET = "secret"

// The generic client ID that will be created with necessary UAA scope/autherities
val UAA_CLIENTID_GENERIC = "app_client"

// The generic client ID password
val UAA_CLIENTID_GENERIC_SECRET = "secret"

// The generic group id
val UAA_GROUP_NAME = INSTANCE_PREPENDER + "-group"
