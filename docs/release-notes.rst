.. ============LICENSE_START==========================================
.. ===================================================================
.. Copyright © 2017-2019 AT&T Intellectual Property. All rights reserved.
.. ===================================================================
.. Licensed under the Creative Commons License, Attribution 4.0 Intl.  (the "License");
.. you may not use this documentation except in compliance with the License.
.. You may obtain a copy of the License at
..
..  https://creativecommons.org/licenses/by/4.0/
..
.. Unless required by applicable law or agreed to in writing, software
.. distributed under the License is distributed on an "AS IS" BASIS,
.. WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
.. See the License for the specific language governing permissions and
.. limitations under the License.
.. ============LICENSE_END============================================
.. _release_notes:

Release Notes
=============

.. note
..	* This Release Notes must be updated each time the team decides to Release new artifacts.
..	* The scope of this Release Notes is for this particular component. In other words, each ONAP component has its Release Notes.
..	* This Release Notes is cumulative, the most recently Released artifact is made visible in the top of this Release Notes.
..	* Except the date and the version number, all the other sections are optional but there must be at least one section describing the purpose of this new release.
..	* This note must be removed after content has been added.


Abstract
========

This document provides the release notes for the Application Controller Project's Frankfurt release.

Summary
=======

The Application Controller (APPC) performs functions to manage the lifecycle of VNFs and their components providing model driven configuration, abstracts cloud/VNF interfaces for repeatable actions, uses vendor agnostic mechanisms (NETCONF, Chef via Chef Server and Ansible) and enables automation.

Release Data
============

Version: 1.7.2
--------------

:Release Date: 2020-5-23


New features
------------

         - Upgraded OpenDaylight (ODL) version to Neon SR1

         - Added support for the following LCM actions (a desciption of all of the above LCM actions can be found in the APPC LCM API Guide on readthedoc):

                - ActivateNESw

                - ConfigScaleIn

                - DownloadNESw

                - GetConfig

                - LicenseManagement

                - PostEvacuate

                - PostMigrate

                - PostRebuild

                - PreConfigure

                - PreEvacuate

                - PreMigrate

                - PreRebuild

                - Provisioning

                - StartTraffic

                - StatusTraffic

                - StopTraffic

         - Move northbound DMAAP adapter out from ODL OSGI Karaf base

         - vnfc/vf-module/v-server operations support for ansible LCMs

         - Resource resolution via CDS


Known Limitations, Issues and Workarounds
=========================================

System Limitations
------------------

 - OpenStack Restriction:

                - Currently APPC only supports OpenStack.

                - Admin level access for Tenant level operations.

                - OpenStack Hypervisorcheck is turned off by default.

 - Netconf Restriction:

                - Currently APPC only tested with Honeycomb. 

Known Vulnerabilities
---------------------

* `AAF-987 <https://jira.onap.org/browse/AAF-987>`_ - Bath function in AAF can not be functioned with different users and roles, which are associated with Opendaylight AAA users. 

Workarounds
-----------


Security Notes
--------------

 - Password removal from helm charts

 - Allow overriding of keystore and truststore in APPC helm charts

 - All application processes are running non-root user in containers

References
==========

For more information on the ONAP Frankfurt release, please see:

#. `ONAP Home Page`_
#. `ONAP Documentation`_
#. `ONAP Release Downloads`_
#. `ONAP Wiki Page`_


.. _`ONAP Home Page`: https://www.onap.org
.. _`ONAP Wiki Page`: https://wiki.onap.org
.. _`ONAP Documentation`: https://docs.onap.org
.. _`ONAP Release Downloads`: https://git.onap.org


..      ==========================
..      * * *     EL ALTO    * * *
..      ==========================


Version: 1.6.4
--------------

:Release Date: 2019-9-30

The El Alto added the following feature, bug fixes and security enhancements:

**New Features**

	 - Upgraded OpenDaylight (ODL) version to Fluorine SR2

**Bug Fixes**

      - `APPC-1319 <https://jira.onap.org/browse/APPC-1319>`_ - apidoc shows ""undefined"" when netconf successfully mounted
      - `APPC-1584 <https://jira.onap.org/browse/APPC-1584>`_ - Incorrect Package name in Audit Directed Graph
      - `APPC-1587 <https://jira.onap.org/browse/APPC-1587>`_ - Publish config field mismatch in onap documentaion & Audit DG
      - `APPC-1588 <https://jira.onap.org/browse/APPC-1588>`_ - Publish config filed missing in Sync LCM in documentation
      - `APPC-1589 <https://jira.onap.org/browse/APPC-1589>`_ - Cvaas directory is not mounted in docker image,Dublin Release
      - `APPC-1590 <https://jira.onap.org/browse/APPC-1590>`_ - Sync & Audit Payload to include the file name
      - `APPC-1604 <https://jira.onap.org/browse/APPC-1604>`_ - APPC Not Picking up Mesasges from Dmaap
      - `APPC-1613 <https://jira.onap.org/browse/APPC-1613>`_ - Exception for LCM request with parameter read from A&AI
      - `APPC-1627 <https://jira.onap.org/browse/APPC-1627>`_ - Daexim directory owned by root - access denied during boot
      - `APPC-1634 <https://jira.onap.org/browse/APPC-1634>`_ - Mark the fields transient of RequestFailedException.java Serializable class to full-fill Serializable class contract,
      - `APPC-1635 <https://jira.onap.org/browse/APPC-1635>`_ - Mark the fields transient of EventMessage.java Serializable class to full-fill Serializable class contract
      - `APPC-1639 <https://jira.onap.org/browse/APPC-1639>`_ - Error during CDT SQL query
      - `APPC-1713 <https://jira.onap.org/browse/APPC-1713>`_ - Appc eelf logging resource bundle error after ODL upgrade
      - `APPC-1736 <https://jira.onap.org/browse/APPC-1736>`_ - change mountpoint for pax property file

**Known Issues**

      - `APPC-1710 <https://jira.onap.org/browse/APPC-1710>`_ - Need for "ReadWriteMany" access on storage when deploying on Kubernetes?
         - to work around this is to add "accessMode: ReadWriteOnce" to values.yaml in APPC helm chart
      - `APPC-1766 <https://jira.onap.org/browse/APPC-1766>`_ - openStackEncryptedPassword value is not encrypted
         - to work around this is to change "provider1.tenant1.password={{.Values.config.openStackEncryptedPassword}}" to "provider1.tenant1.password=<non-encrypted plaintext password>" in APPC helm chart's appc.properties.


**Security Notes**

*Fixed Security Issues*

      - `OJSI-25 <https://jira.onap.org/browse/OJSI-25>`_ - SQL Injection in APPC (CVE-2019-12316)
      - `OJSI-104 <https://jira.onap.org/browse/OJSI-104>`_ - appc exposes plain text HTTP endpoint using port 30211
      - `OJSI-113 <https://jira.onap.org/browse/OJSI-113>`_ - appc exposes plain text HTTP endpoint using port 30230
      - `OJSI-146 <https://jira.onap.org/browse/OJSI-146>`_ - appc-cdt exposes plain text HTTP endpoint using port 30289
      - `OJSI-185 <https://jira.onap.org/browse/OJSI-185>`_ - appc exposes ssh service on port 30231
	SSH is exposed by ODL in order to use NETCONF within SSH session based on `RFC-6242 <https://tools.ietf.org/html/rfc6242>` so currently it cannot be avoided.
	Taken into account that this design is well documented in RFC, we no longer consider this to be a security issue but only a hardening opportunity.

Version: 1.5.3
--------------

:Release Date: 2019-6-19

**New Features**

The Dublin release added the following functionality:

	 - Upgraded OpenDaylight (ODL) version to Fluorine SR1

	 - Migrated CDT docker to node.js base docker image

	 - Added support for the following LCM actions (a desciption of all of the above LCM actions can be found in the APPC LCM API Guide on readthedoc):

	 - To support in-place software upgrade:

		- DistributeTrafficCheck

	 - Added Multiple standalone ansible servers support

	 - Additional contributions as part of Dublin include:

		- Test Coverage increased to 83.8%

		- Support for Platform Maturity requirements, including:

		   - Security

		   	 - Applicaton runs as non-root user in all APPC dockers

		   	 - Migrated to https for CDT GUI

		   - Stability

			  - Executed 72 hour stability test on both Heat and OOM deployed environments using JMeter to drive a steady set of transactions over the 72 hour period (see the following wiki page for more details: https://wiki.onap.org/display/DW/APPC+72+Hour+Stability+Testing+Dublin )

		   - Resiliency

			  - Support for OOM deployment, which enables resiliency via use of Kubernetes (see https://wiki.onap.org/display/DW/APPC+Resiliency for additional details)


**Bug Fixes**

      - `APPC-1242 <https://jira.onap.org/browse/APPC-1242>`_ - vFWCL ModifyConfig only works on one node in an APPC cluster.
      - `APPC-1263 <https://jira.onap.org/browse/APPC-1263>`_ - Two methods of Artifact Transformer in appc-config-params will always return null.
      - `APPC-1264 <https://jira.onap.org/browse/APPC-1264>`_ - Errors in unit tests in config-generator package.
      - `APPC-1270 <https://jira.onap.org/browse/APPC-1270>`_ - Unit tests in ccadaptor code not testing correctly.
      - `APPC-1274 <https://jira.onap.org/browse/APPC-1274>`_ - APPC DG : RestAdapter plugin : lack of documentation.
      - `APPC-1303 <https://jira.onap.org/browse/APPC-1303>`_ - DependencyModelParser works toward incorrectly spelled property name.
      - `APPC-1331 <https://jira.onap.org/browse/APPC-1331>`_ - Test cases in appc-common cause intermittent failures.
      - `APPC-1367 <https://jira.onap.org/browse/APPC-1367>`_ - APPC returns UnknownHostException during Netconf operations.
      - `APPC-1435 <https://jira.onap.org/browse/APPC-1435>`_ - APPC Ansible Server keeps crashing.
      - `APPC-1441 <https://jira.onap.org/browse/APPC-1441>`_ - incompatible cherrypy version in ansible server container.
      - `APPC-1463 <https://jira.onap.org/browse/APPC-1463>`_ - Error in ControllerImpl class.
      - `APPC-1472 <https://jira.onap.org/browse/APPC-1472>`_ - Not possible to call ansible healthcheck from SO VnfConfigUpdate workflow.
      - `APPC-1479 <https://jira.onap.org/browse/APPC-1479>`_ - Logic error in ScheduledPublishingPolicyImpl.
      - `APPC-1480 <https://jira.onap.org/browse/APPC-1480>`_ - MetricRegistryImpl code has problems with casting.
      - `APPC-1489 <https://jira.onap.org/browse/APPC-1489>`_ - SO VnfConfigUpdate workflow fails with timeout error.
      - `APPC-1528 <https://jira.onap.org/browse/APPC-1528>`_ - APPC DB table creation failed.
      - `APPC-1537 <https://jira.onap.org/browse/APPC-1537>`_ - UNIQUE KEY is too long in DEVICE_AUTHENTICATION.
      - `APPC-1542 <https://jira.onap.org/browse/APPC-1542>`_ - ExecuteNodeActionImpl is not instatiate.
      - `APPC-1545 <https://jira.onap.org/browse/APPC-1545>`_ - Problem with Ansible handling in EncryptionToolDGWrapper.
      - `APPC-1548 <https://jira.onap.org/browse/APPC-1548>`_ - "MariaDB 10.2.4 adds ""ROWS"" as an SQL keyword".
      - `APPC-1574 <https://jira.onap.org/browse/APPC-1574>`_ - FileParameters not supported for Ansible LCM action.
      - `APPC-1576 <https://jira.onap.org/browse/APPC-1576>`_ - FileParameters content is wrongly processed.
      - `APPC-1577 <https://jira.onap.org/browse/APPC-1577>`_ - Ansible Server  playbook execution does not work.
      - `APPC-1583 <https://jira.onap.org/browse/APPC-1583>`_ - ansible user privileges problem.
      - `APPC-1584 <https://jira.onap.org/browse/APPC-1584>`_ - Incorrect Package name in Audit Directed Graph.
      - `APPC-1589 <https://jira.onap.org/browse/APPC-1589>`_ - Cvaas directory is not mounted in docker image.
      - `APPC-1593 <https://jira.onap.org/browse/APPC-1593>`_ - CDT doesn't push info to DB.
      - `APPC-1600 <https://jira.onap.org/browse/APPC-1600>`_ - "APPC DB doesn't have any artifact for ""artifact-type""=""APPC-CONFIG""".
      - `APPC-1604 <https://jira.onap.org/browse/APPC-1604>`_ - APPC Not Picking up Mesasges from Dmaap.
      - `APPC-1610 <https://jira.onap.org/browse/APPC-1610>`_ - Config vFW Netconf URI should be stream-count:stream-count intead of sample-plugin:pg-streams.
      - `APPC-1611 <https://jira.onap.org/browse/APPC-1611>`_ - VNF_DG_MAPPING and PROCESS_FLOW_REFERENCE tables are empty.
      - `APPC-1612 <https://jira.onap.org/browse/APPC-1612>`_ - InventoryNames parameter support for APPC Ansible LCM.

**Known Issues**

      - `APPC-1613 <https://jira.onap.org/browse/APPC-1613>`_ - Exception for LCM request with parameter read from A&AI.
         - to work around this is to switch to the  fixed parameter in the template or passed as configuration parameter in stead of using A&AI that APPC received the value from the request.

**Security Notes**

*Fixed Security Issues*

      - `OJSI-146 <https://jira.onap.org/browse/OJSI-146>`_ - In default deployment APPC (appc-cdt) exposes HTTP port 30289 outside of cluster.
      - `OJSI-104 <https://jira.onap.org/browse/OJSI-104>`_ - In default deployment APPC (appc) exposes HTTP port 30211 outside of cluster.

*Known Security Issues*

      - CVE-2019-12316 `OJSI-25 <https://jira.onap.org/browse/OJSI-25>`_ - SQL Injection in APPC
      - `OJSI-29 <https://jira.onap.org/browse/OJSI-29>`_ - Unsecured Swagger UI Interface in AAPC
      - CVE-2019-12124 `OJSI-63 <https://jira.onap.org/browse/OJSI-63>`_ - APPC exposes Jolokia Interface which allows to read and overwrite any arbitrary file
      - `OJSI-95 <https://jira.onap.org/browse/OJSI-95>`_ - appc-cdt allows to impersonate any user by setting USER_ID
      - `OJSI-112 <https://jira.onap.org/browse/OJSI-112>`_ - In default deployment APPC (appc-dgbuilder) exposes HTTP port 30228 outside of cluster.
      - `OJSI-113 <https://jira.onap.org/browse/OJSI-113>`_ - In default deployment APPC (appc) exposes HTTP port 30230 outside of cluster.
      - `OJSI-185 <https://jira.onap.org/browse/OJSI-185>`_ - appc exposes ssh service on port 30231

*Known Vulnerabilities in Used Modules*

Quick Links:

 	- `APPC project page <https://wiki.onap.org/display/DW/Application+Controller+Project>`_

 	- `Passing Level Badge information for APPC <https://bestpractices.coreinfrastructure.org/en/projects/1579>`_

 	- `Silver Level Badge information for APPC <https://bestpractices.coreinfrastructure.org/en/projects/1579?criteria_level=1>`_

 	- `Project Vulnerability Review Table for APPC <https://wiki.onap.org/pages/viewpage.action?pageId=51282466>`_

**Other**

- Limitations, Constraints and other worthy notes:

	- OpenStack Restriction:

		- Currently APPC only supports OpenStack.

		- Admin level access for Tenant level operations.

		- OpenStack Hypervisorcheck is turned off by default.


Version: 1.4.4
--------------

:Release Date: 2019-1-31


**Bug Fixes**

The Casablanca maintenance release fixed the following bugs:

	- `APPC-1247 <https://jira.onap.org/browse/APPC-1247>`_ - java.lang.NoClassDefFoundError when publishing DMAAP message

	- `CCSDK-741 <https://jira.onap.org/browse/CCSDK-741>`_ - Removed Work-around required for vCPE use case to correct the error described in CCSDK ticket.

Special Note for `APPC-1367 <https://jira.onap.org/browse/APPC-1367>`_ - APPC fails healthcheck with 404 error:

       During testing, we found there is a timing issue. When using OOM to deploy to k8s environment the clustered MariaDB database is not accessible at the time when the APPC pod is trying to insert the DG into MariaDb. This would cause the healthcheck issue. The workaround to solve this issue is redeploying the APPC pod.

Version: 1.4.3
--------------

:Release Date: 2018-11-30


**New Features**

The Casablanca release added the following functionality:

	 - Upgraded OpenDaylight (ODL) version to Oxygen

	 - Upgraded to Karaf 4.1.5

	 - Migrated DB from mysql to maria db with galeira, tested on k8s clustering platform

	 - Added an ansible docker container, tested for DistributeTraffic LCM action

	 - Added support for the following LCM actions (a desciption of all of the above LCM actions can be found in the APPC LCM API Guide on readthedoc):

		- To support in-place software upgrade:

		  - DistributeTraffic

		- To support storage management in OpenStack

		  - Reboot with hard and soft option

	 - Additional contributions as part of Casablanca include:

		- Support for Platform Maturity requirements, including:

		   - Increased security

			  - Enabled bath feature from AAF, CDT GUI and APIDOC can be used when AAF enbled  (see `APPC-1237 <https://jira.onap.org/browse/APPC-1237>`_ for additional details)
			  - Addressed critical alerts reported via Nexus IQ to the extent possible (see `APPC-770 <https://jira.onap.org/browse/APPC-770>`_ and wiki: https://wiki.onap.org/pages/viewpage.action?pageId=40927352 )

		   - Stability

			  - Executed 72 hour stability test on both Heat and OOM deployed environments using JMeter to drive a steady set of transactions over the 72 hour period (see the following wiki page for more details: https://wiki.onap.org/display/DW/APPC+72+Hour+Stability+Testing+Casablanca )

		   - Resiliency

			  - Support for OOM deployment, which enables resiliency via use of Kubernetes (see https://wiki.onap.org/display/DW/APPC+Resiliency for additional details)


**Bug Fixes**

	- `APPC-1009 <https://jira.onap.org/browse/APPC-1009>`_ - An incorrect regex in appc-provider-model was causing intermittent unit test failures. This is now fixed.

	- `APPC-1021 <https://jira.onap.org/browse/APPC-1021>`_ - An unnecessary pseudoterminal allocation for SSH connection was causing problems when trying to connect to a ConfD NETCONF Server.

	- `APPC-1107 <https://jira.onap.org/browse/APPC-1107>`_ - Database problems were causing artifacts created in CDT to not save to APPC. These have been fixed.

	- `APPC-1111 <https://jira.onap.org/browse/APPC-1111>`_ - TestDmaapConsumerImpl.testFetch method was taking 130+ seconds to run test. Build time is shorter now.

	- `APPC-1112 <https://jira.onap.org/browse/APPC-1112>`_ - Several unit tests in TimeTest.java had intermittent failures.

	- `APPC-1157 <https://jira.onap.org/browse/APPC-1157>`_ - Mockito package was removed from the APPC client jar. It was causing conflicts with other applications using APPC client library.

	- `APPC-1184 <https://jira.onap.org/browse/APPC-1184>`_ - The APPC LCM API documentation was outdated and did not reflect the correct endpoints.

	- `APPC-1186 <https://jira.onap.org/browse/APPC-1186>`_ - VNF-Level OpenStack actions such as Restart were failing if the optional identity-url was omitted from the payload of the request.

	- `APPC-1188 <https://jira.onap.org/browse/APPC-1188>`_ - Exception was occurring if force flag was set to false in a request from policy.

	- `APPC-1192 <https://jira.onap.org/browse/APPC-1192>`_ - CDT was not updating the DEVICE_INTERFACE_PROTOCOL table, so APPC was unable to get the protocol during lcm actions.

	- `APPC-1205 <https://jira.onap.org/browse/APPC-1205>`_ - Artifacts manually entered into CDT were not saving correctly, while artifacts created by uploading a template were.

	- `APPC-1207 <https://jira.onap.org/browse/APPC-1207>`_ - Logging constants were missing in several features, causing incorrect logging messages.

	- `APPC-1218 <https://jira.onap.org/browse/APPC-1218>`_ - Aai connection had certificate errors and path build exceptions.

	- `APPC-1224 <https://jira.onap.org/browse/APPC-1224>`_ - SubRequestID was not being relayed back to Policy in DMaaP Response messages.

	- `APPC-1226 <https://jira.onap.org/browse/APPC-1226>`_ - Mock code to mimic backend execution for Reboot was causing problems and has been removed.

	- `APPC-1227 <https://jira.onap.org/browse/APPC-1227>`_ - APPC was not able to read VNF templates created with CDT.

	- `APPC-1230 <https://jira.onap.org/browse/APPC-1230>`_ - APPC was using the GenericRestart DG instead of DGOrchestrator.

	- `APPC-1231 <https://jira.onap.org/browse/APPC-1231>`_ - APPC was not updating the TRANSACTIONS table correctly when an operation completed.

	- `APPC-1233 <https://jira.onap.org/browse/APPC-1233>`_ - DGOrchestrator was incorrectly being given an output.payload parameter instead of output-payload.

	- `APPC-1234 <https://jira.onap.org/browse/APPC-1234>`_ - AppC Open Day Light login was responding 401 unauthorized when AAF was enabled.

	- `APPC-1237 <https://jira.onap.org/browse/APPC-1237>`_ - APPC was not properly url-encoding AAF credentials.

	- `APPC-1243 <https://jira.onap.org/browse/APPC-1243>`_ - Container was not preserving mysql data after kubectl edit statefulset.

	- `APPC-1244 <https://jira.onap.org/browse/APPC-1244>`_ - Ansible Server would never start in oom.

**Known Issues**

	- `APPC-1247 <https://jira.onap.org/browse/APPC-1247>`_ - java.lang.NoClassDefFoundError when publishing DMAAP message
	    - This issue is relevant during the vCPE use case.
	    - Due to this defect, the VM will perform four start/stop sequences, instead of the normal one.
	    - After the four start/stop sequences, the VM will be left in the correct state that it should be in.

	- Work-around required for vCPE use case to correct the error described in: `CCSDK-741 <https://jira.onap.org/browse/CCSDK-741>`_
	    - CCSDK aai adapter doesn't recognize generic-vnf attribute in the response, as it is not defined by aai_schema XSD
	    - To work around this, several steps must be performed as described here:

	        1. Add a restapi template file into the appc docker containers
	            a. Enter the appc docker container (docker exec... or kubectl exec...)
	            b. Create a directory: /opt/onap/appc/templates
	            c. Download this file `aai-named-query.json <https://gerrit.onap.org/r/gitweb?p=appc/deployment.git;a=blob_plain;f=vcpe-workaround-files/aai-named-query.json;hb=refs/heads/casablanca>`_ and place it in that directory
	        2. Replace the generic restart DG with a new one
	            a. Download the `APPC_Generic_Restart.xml <https://gerrit.onap.org/r/gitweb?p=appc/deployment.git;a=blob_plain;f=vcpe-workaround-files/APPC_method_Generic_Restart_3.0.0.xml;hb=refs/heads/casablanca>`_
	            b. Edit the file. Find the parameter definition lines for restapiUrl, restapiUser, restapiPassword (lines 52-54) and replace these with the correct values for your aai server.
	            c. Copy this file into the appc docker containers to the /opt/onap/appc/svclogic/graphs directory (you will be replacing the old version of the file with this copy)
	        3. Load the new DG file
	            a. In the appc docker containers, enter the "/opt/appc/svclogic/bin directory
	            b. Run install-converted-dgs.sh

Quick Links:

 	- `APPC project page <https://wiki.onap.org/display/DW/Application+Controller+Project>`_

 	- `Passing Badge information for APPC <https://bestpractices.coreinfrastructure.org/en/projects/1579>`_

 	- `Project Vulnerability Review Table for APPC <https://wiki.onap.org/pages/viewpage.action?pageId=40927352>`_

**Other**

- Limitations, Constraints and other worthy notes:

	- OpenStack Restriction:

		- Currently APPC only supports OpenStack.

		- Admin level access for Tenant level operations.

		- OpenStack Hypervisorcheck is turned off by default.


	- Integration with MultiCloud is supported for Standalone Restart (i.e., not via DGOrchestrator). For any other action, such as Stop, Start, etc.. via MultiCloud requires the MultiCloud identity URL to be either passed in the payload or defined in appc.properties.



Version: 1.3.0
--------------


:Release Date: 2018-06-07


**New Features**

The Beijing release added the following functionality:

	 - Added support for the following LCM actions (a desciption of all of the above LCM actions can be found in the APPC LCM API Guide on readthedoc):

		- To support in-place software upgrade:

		  - QuiesceTraffic
		  - ResumeTraffic
		  - UpgradeSoftware
		  - UpgradePreCheck
		  - UpgradePostCheck
		  - UpgradeBackup
		  - UpgradeBackout

		- To support storage management in OpenStack

		  - AttachVolume
		  - DetachVolume

		- To support Manual Scale Out use case

		  - ConfigScaleOut (more details can be found in teh APPC Epic: `APPC-431 <https://jira.onap.org/browse/APPC-431>`_ )

		- To support general operations

		  - ActionStatus


	 - Contributed the APPC Controller Design Tool (CDT), which enables self-serve capabilities by allowing users to model their VNF/VNFC for consumption by APPC to use in the execution of requests to perform life cycle management activities.

		- More details on the APPC CDT can be found in the APPC CDT User Guide in readthedocs.
		- Additional information on how the APPC CDT tool was used to model the vLB and build teh artifacts needed by APPC to execute teh ConfigScaleOut action can be found at the following wiki pages: https://wiki.onap.org/pages/viewpage.action?pageId=33065185

	 - Additional contributions as part of Beijing include:

		- Support for Platform Maturity requirements, including:

		   - Increased security

			  - Added security to ODL web-based API access via AAF (see `APPC-404 <https://jira.onap.org/browse/APPC-404>`_ for additional details)
			  - Addressed critical alerts reported via Nexus IQ to the extent possible (see `APPC-656 <https://jira.onap.org/browse/APPC-656>`_ )

		   - Stability

			  - Executed 72 hour stability test on both Heat and OOM deployed environments using JMeter to drive a steady set of transactions over the 72 hour period (see the following wiki page for more details: https://wiki.onap.org/display/DW/ONAP+APPC+72+Hour+Stability+Test+Results )

		   - Resiliency

			  - Support for OOM deployment, which enables resiliency via use of Kubernetes (see `APPC-414 <https://jira.onap.org/browse/APPC-414>`_ for additional details)

		- Upgraded OpenDaylight (ODL) version to Nitrogen




**Bug Fixes**

The following defects that were documented as known issues in Amsterdam have been fixed in Beijing release:

	- `APPC-316 <https://jira.onap.org/browse/APPC-316>`_ - Null payload issue for Stop Application

	- `APPC-315 <https://jira.onap.org/browse/APPC-315>`_ - appc-request-handler is giving error java.lang.NoClassDefFoundError

	- `APPC-312 <https://jira.onap.org/browse/APPC-312>`_ - APPC request is going to wrong request handler and rejecting request

	- `APPC-311 <https://jira.onap.org/browse/APPC-311>`_ - The APPC LCM Provider Healthcheck

	- `APPC-309 <https://jira.onap.org/browse/APPC-309>`_ - APPC LCM Provider URL missing in appc.properties.

	- `APPC-307 <https://jira.onap.org/browse/APPC-307>`_ - Embed jackson-annotations dependency in appc-dg-common during run-time

	- `APPC-276 <https://jira.onap.org/browse/APPC-276>`_ - Some Junit are breaking convention causing excessively long build

	- `APPC-248 <https://jira.onap.org/browse/APPC-248>`_ - There is an compatibility issue between PowerMock and Jacoco which causes Sonar coverage not to be captured. Fix is to move to Mockito.


**Known Issues**

The following issues remain open at the end of Beijing release. Please refer to Jira for further details and workaround, if available.

        - `APPC-987 <https://jira.onap.org/browse/APPC-987>`_ - APPC Investigate TRANSACTION Table Aging. See **Other** section for further information

	- `APPC-977 <https://jira.onap.org/browse/APPC-977>`_ - Procedures needed for enabling AAF support in OOM. See **Other** section for further information

        - `APPC-973 <https://jira.onap.org/browse/APPC-973>`_ - Fix delimiter string for xml-download CDT action

	- `APPC-940 <https://jira.onap.org/browse/APPC-940>`_ - APPC CDT Tool is not updating appc_southbound.properties with the URL supplied for REST

        - `APPC-929 <https://jira.onap.org/browse/APPC-929>`_ - LCM API - ConfigScaleOut- Payload parameter to be manadatory set to "true"

	- `APPC-912 <https://jira.onap.org/browse/APPC-912>`_ - MalformedChunkCodingException in MDSALStoreImpl.getNodeName

	- `APPC-892 <https://jira.onap.org/browse/APPC-892>`_ - Cntl+4 to highlight and replace feature-Textbox is accepting space  and able to submit without giving any value

	- `APPC-869 <https://jira.onap.org/browse/APPC-869>`_ - VM Snapshot error occurs during image validation.

	- `APPC-814 <https://jira.onap.org/browse/APPC-814>`_ - Update openecomp-tosca-datatype namespace

	- `APPC-340 <https://jira.onap.org/browse/APPC-340>`_ - APPC rejecting request even for decimal of millisecond timestamp difference

	- `APPC-154 <https://jira.onap.org/browse/APPC-154>`_ - Logging issue - Request REST API of APPC has RequestID (MDC) in Body or Payload section instead of Header.


**Security Notes**

APPC code has been formally scanned during build time using NexusIQ and all Critical vulnerabilities have been addressed, items that remain open have been assessed for risk and determined to be false positive. The APPC open Critical security vulnerabilities and their risk assessment have been documented as part of the `project <https://wiki.onap.org/pages/viewpage.action?pageId=25438971>`_.

Additionally, communication over DMaaP currently does not use secure topics in this release. This has dependency on DMaaP to enable.


Quick Links:
 	- `APPC project page <https://wiki.onap.org/display/DW/Application+Controller+Project>`_

 	- `Passing Badge information for APPC <https://bestpractices.coreinfrastructure.org/en/projects/1579>`_

 	- `Project Vulnerability Review Table for APPC <https://wiki.onap.org/pages/viewpage.action?pageId=25438971>`_

**Other**

- Limitations, Constraints and other worthy notes

	- An issue was discovered with usage of AAF in an OOM deployed environment after the Beijing release was created. The issue was twofold (tracked under `APPC-977 <https://jira.onap.org/browse/APPC-977>`_):

	     - Needed APPC configuration files were missing in Beijing OOM , and
	     - AAF updated their certificates to require 2way certs, which requires APPC updates

          Additionally, in a Heat deployed environment, a manual workaround will be required to authorize with AAF if they are using 2way certificates.  For instruction on workaround steps needed depending on type of deployment, please refer to the following wiki: https://wiki.onap.org/display/DW/AAF+Integration+with+APPC.

        - During the testing of the vCPE/vMUX closed loop scenarios in an OOM deployed environment, an issue was encountered where transactions were not being deleted from the TRANSACTION table and was blocking other Restart request from completing successfully (tracked under `APPC-987 <https://jira.onap.org/browse/APPC-987>`_). A workaround is available and documented in the Jira ticket.

        - It is impossible for us to test all aspect of the application. Scope of testing done in Beijing is captured on the following wiki:   https://wiki.onap.org/display/DW/APPC+Beijing+Testing+Scope+and+Status

	- Currently APPC only supports OpenStack

	- OpenStack Hypervisorcheck is turned off by default. If you want to invoke this functionality via the appc.properties, you need to enable it and ensure you have Admin level access to OpenStack.

	- Integration with MultiCloud is supported for Standalone Restart (i.e., not via DGOrchestrator). For any other action, such as Stop, Start, etc.. via MultiCloud requires the MultiCloud identity URL to be either passed in the payload or defined in appc.properties.

	- APPC needs Admin level access for Tenant level operations.

	- Currently, the "ModifyConfig" API and the implementation in the Master Directed Graph is only designed to work with the vFW Closed-Loop Demo.


Version: 1.2.0
--------------

:Release Date: 2017-11-16


**New Features**

The Amsterdam release continued evolving the design driven architecture of and functionality for APPC.
APPC aims to be completely agnostic and make no assumption about the network.

The main goal of the Amsterdam release was to:
 - Support the vCPE use case as part of the closed loop action to perform a Restart on the vGMUX
 - Demonstrate integration with MultiCloud as a proxy to OpenStack
 - Continue supporting the vFW closed loop use case as part of regression from the seed contribution.

Other key features added in this release include:
 - Support for Ansible
   - The Ansible Extension for APP-C allows management of VNFs that support Ansible. Ansible is a an open-source VNF management framework that provides an almost cli like set of tools in a structured form. APPC supports Ansible through the following three additions: An Ansible server interface, Ansible Adapter, and Ansible Directed Graph.
 - Support for Chef
   - The Chef Extension for APPC allows management of VNFs that support Chef through the following two additions: a Chef Adapter and Chef Directed Graph.
 - LifeCycle Management (LCM) APIs via standalone DGs or via the DGOrchestrator architecture to trigger actions on VMs, VNFs, or VNFCs
 - OAM APIs to manage the APPC application itself
 - Upgrade of OpenDaylight to Carbon version



**Bug Fixes**

	- This is technically the first release of APPC, previous release was the seed code contribution. As such, the defects fixed in this release were raised during the course of the release. Anything not closed is captured below under Known Issues. If you want to review the defects fixed in the Amsterdam release, refer to `Jira <https://jira.onap.org/issues/?filter=10570&jql=project%20%3D%20APPC%20AND%20issuetype%20%3D%20Bug%20AND%20status%20%3D%20Closed%20AND%20fixVersion%20%3D%20%22Amsterdam%20Release%22>`_.

	- Please also refer to the notes below. Given the timeframe and resource limitations, not all functions of the release could be validated. Items that were validated are documented on the wiki at the link provide below. If you find issues in the course of your work with APPC, please open a defect in the Application Controller project of Jira (jira.onpa.org)

**Known Issues**

	- `APPC-312 <https://jira.onap.org/browse/APPC-312>`_ - APPC request is going to wrong request handler and rejecting request. Configure request failing with following error: ``REJECTED Action Configure is not supported on VM level``.

	- `APPC-311 <https://jira.onap.org/browse/APPC-311>`_ - The APPC LCM Provider Healthcheck, which does a healthceck on a VNF, is failing. No known workaround at this time.

	- `APPC-309 <https://jira.onap.org/browse/APPC-309>`_ - The property: ``appc.LCM.provider.url=http://127.0.0.1:8181/restconf/operations/appc-provider-lcm`` is missing from appc.properties in the appc deployment.  The property can be manually added as a workaround, then bounce the appc container.

	- `APPC-307 <https://jira.onap.org/browse/APPC-307>`_ - Missing jackson-annotations dependency in appc-dg-common - This issue results in Rebuild operation via the APPC Provider not to work. Use instead Rebuild via the APPC LCM Provider using DGOrchestrator.

	- `APPC-276 <https://jira.onap.org/browse/APPC-276>`_ - A number of junit testcases need to be reworked because they are causing APPC builds to take much  longer to complete. This issue does not cause the build to fail, just take longer. You can comment out these junit in your local build if this is a problem.

	- `APPC-248 <https://jira.onap.org/browse/APPC-248>`_ - There is an compatibility issue between PowerMock and Jacoco which causes Sonar coverage not to be captured. There is no functional impact on APPC.

	- `APPC-154 <https://jira.onap.org/browse/APPC-154>`_ - Logging issue - Request REST API of APPC has RequestID (MDC) in Body or Payload section instead of Header.


**Security Issues**
	- Communication over DMaaP currently does not use secure topics in this release.
	- AAF is deactivated by default in this release and was not validated or committed as part of the Amsterdam Release.


**Other**

- Limitations, Constraints and other worthy notes

  - LCM Healthcheck and Configure actions do not work.
  - The APPC actions validated in this release are captured here: https://wiki.onap.org/display/DW/APPC+Testing+Scope+and+Status
  - Currently APPC only supports OpenStack
  - OpenStack Hypervisorcheck is turned off by default. If you want to invoke this functionality via the appc.properties, you need to enable it and ensure you have Admin level access to OpenStack.
  - Integration with MultiCloud is supported for Standalone Restart (i.e., not via DGOrchestrator). For any other action, such as Stop, Start, etc.. via MultiCloud requires the MultiCloud identity URL to be either passed in the payload or defined in appc.properties.
  - APPC needs Admin level access for Tenant level operations.
  - Currently, if DGs are modified in appc.git repo, they must be manually moved to the appc/deployment repo.
  - Currently, the "ModifyConfig" API and the implementation in the Master Directed Graph is only designed to work with the vFW Closed-Loop Demo.


===========

End of Release Notes
