# Predix Demo Bootstrap - Utility Script

The script creates instances on Predix for the following services: UAA, Timeseries, Asset, PostgreSQL and configures them to provide a ready-to-use Predix environment for your demos  (necessary authorities, scopes, create a UAA user, create UAA client_id, UAA groups etc.)

My intention was not to create a perfect piece of software but a usable one.

## Prerequisites

Make sure to have the required prerequisites in place:

- A working internet connection :-)
- An account on Predix.io
- [Git](https://git-scm.com/downloads)
- [Cloud Foundry CLI](https://github.com/cloudfoundry/cli)
- [Ammonite](http://www.lihaoyi.com/Ammonite/) that lets you use the Scala language for scripting purposes.

## How to use it?

This script has been developed and tested against Scala 2.12.x Java 1.8.0_73 on Linux (Ubuntu) and Mac OSX 10.x.

Below the steps for Linux and Mac users:

1. Configure networks and proxies
2. Execute the following steps to clone the repos and run the script

```
$ git clone https://github.com/indaco/predix-demo-setup
$ cd predix-demo-setup
$ amm main.sc
```

#### Notes

`src/variables.sc` file contains the default settings for the script (e.g. predix service plan, service names, instance names, client_id, user details etc.). Adapt them to your preferences.

### Results

The scripts generates a JSON file with details about the services created. It can be used for further reference. It is exactly the output for `cf env appName` command.

## And Now?

Well done, your Predix environment is ready for you!

It's time now to ingest some dummy data or create some dummy asset so, use the [Predix Tool Kit ](https://predix-starter.run.aws-usw02-pr.ice.predix.io) if are already familiar with it or have a look on [Predix.io](https://www.predix.io/) to learn how to use the [Predix Machine](https://docs.predix.io/en-US/content/service/edge_software_and_services/machine/) for a real-life scenario.

--------------------------------------------------------------------------------

#### DISCLAIMER

This is not an official development neither from the GE Digital's Predix Team.
