# Intro 

VizGrimoireJS aims at providing a framework for software metrics visualization using HTML, 
CSS and JavaScript as main technologies.

This is the Orchestrator of the PaaS layer, a core component of the INDIGO project. It receives high-level deployment requests and coordinates the deployment process over the IaaS platforms or Mesos.

## Q. What libraries are used by this front-end?

- Bootstrap 3.1.1
- Jasny Boostrap 3.1.3
- JQuery 1.11.1
- VizGrimoireJS-lib

## Q. How do I generate the HTML?

make

## Q. How do I clean the generated HTML?

1.1.1 MySQL
-----------

The MySQL server needs the following customized settings (use `my.cnf` file to edit those settings):
- `max_allowed_packet = 256M`

1.2 INSTALLING
--------------

## Q. Where do I include the JSON files?

### Compile the code
To compile the project you need to be in the same folder as the `pom.xml` file and type:
```
mvn clean install
```
This command compiles the code and creates a war package, `orchestrator.war`, which will be put inside the `docker` folder.
If you want run only unit-test type:
```
mvn test
```
otherwise if you want run integration test type (or use surefire.skip=true property to skip unit tests)
```
mvn integration-test
```
### Build the Docker image

It is located at browser/data/metrics.json

1.3 RUNNING
--------------
### With MySQL dockerized on the same host
The Orchestrator can be run in 3 steps:

1. Run the MySQL deployments database with the command:

## Q. What if I don't want to generate all the HTML files?

2. Run the MySQL workflow database with the command:

    ```
    sudo docker run --name databaseWorkflow -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=workflow -d mysql:5.7
    ```

3. Run the Orchestrator with the command:

	**`IMPORTANT`**: Remember to replace `ORCHESTRATOR_URL` with the base URL which the Orchestrator is available to (it MUST be accessible for the ElasticCluster callbacks!).

    ```
    sudo docker run --name orchestrator --link databaseWorkflow:databaseWorkflow --link databaseOrchestrator:databaseOrchestrator \
    -p 80:8080 -e ORCHESTRATOR_URL="<public_orchestrator_url, like http://localhost:80>" -d indigodatacloud/orchestrator
    ```

### With external databases

The Orchestrator can also be run using already deployed DBs; you just need to start it with the command
```
sudo docker run --name orchestrator1 -h orchestrator1 -e ORCHESTRATOR_DB_ENDPOINT=DOMAIN_NAME:PORT \
  -e ORCHESTRATOR_DB_NAME=SCHEMA_NAME -e ORCHESTRATOR_DB_USER=DB_USER -e ORCHESTRATOR_DB_PWD=DB_USER_PASSWORD  \
  -e WORKFLOW_DB_ENDPOINT=DOMAIN_NAME:PORT -e WORKFLOW_DB_NAME=SCHEMA_NAME -e WORKFLOW_DB_USER=DB_USER \
  -e WORKFLOW_DB_PWD=DB_USER_PASSWORD -p 80:8080 -d indigodatacloud/orchestrator
```
using as parameters (`DOMAIN_NAME`, `PORT`, `SCHEMA_NAME`, `DB_USER`, `DB_USER_PASSWORD`) the correct values.

1.4 CONFIGURING
--------------
Besides those used to link the Orchestrator to the database, there are other environment variables that can be set in order to configure the Orchestrator behaviour.

### Configure security
 1. `SECURITY_ENABLE`: if set to `true` enable AAI OAuth2 authentication and authorization
 2. `OIDC_ISSUERS`: String containing a list of comma separated values of whitelisted AAI token issuers
 3. `OIDC_CLIENT_ID`: The OAuth2 client ID
 4. `OIDC_CLIENT_SECRET` The OAuth2 client secret
 
### Configure Chronos
 1. `CHRONOS_AUTH_FILE_PATH`: the path to the property file containing credentials for Chronos and OneData service space for Chronos.
You can also edit the file `chronos/chronos.properties` directly in the deployment folder.
