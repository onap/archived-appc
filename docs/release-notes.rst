.. ============LICENSE_START==========================================
.. ===================================================================
.. Copyright © 2017-2018 AT&T Intellectual Property. All rights reserved.
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

Release Notes
=============

.. note
..	* This Release Notes must be updated each time the team decides to Release new artifacts.
..	* The scope of this Release Notes is for this particular component. In other words, each ONAP component has its Release Notes.
..	* This Release Notes is cumulative, the most recently Released artifact is made visible in the top of this Release Notes.
..	* Except the date and the version number, all the other sections are optional but there must be at least one section describing the purpose of this new release.
..	* This note must be removed after content has been added.


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

	- `APPC-862 <https://jira.onap.org/browse/APPC-862>`_ - Configscaleout visible based on properties file

	- `APPC-1009 <https://jira.onap.org/browse/APPC-1009>`_ - Regex in appc-provider-model yang causes intermittent unit test failures

	- `APPC-1021 <https://jira.onap.org/browse/APPC-1021>`_ - Unnecessary pseudoterminal allocation for SSH connection

	- `APPC-1037 <https://jira.onap.org/browse/APPC-1037>`_ - Deployment project version for cdt is incorrect

	- `APPC-1072 <https://jira.onap.org/browse/APPC-1072>`_ - CDT Build failing due to problem in mdbootstrap

	- `APPC-1087 <https://jira.onap.org/browse/APPC-1087>`_ - Incorrect docker image versions in HEAT deployment

	- `APPC-1092 <https://jira.onap.org/browse/APPC-1092>`_ - licence header in template-configuration.component.html hasn't been commented properly
  
	- `APPC-1107 <https://jira.onap.org/browse/APPC-1107>`_ - CDT Artifacts Do Not Save
	
	- `APPC-1109 <https://jira.onap.org/browse/APPC-1109>`_ - Multiple versions of CDT in tmp folder during docker build cause problems

	- `APPC-1110 <https://jira.onap.org/browse/APPC-1110>`_ - appc-dg-shared feature fails to install

	- `APPC-1111 <https://jira.onap.org/browse/APPC-1111>`_ - TestDmaapConsumerImpl.testFetch method takes 130+ seconds to run test

	- `APPC-1112 <https://jira.onap.org/browse/APPC-1112>`_ - Several tests in TimeTest.java are failing

	- `APPC-1157 <https://jira.onap.org/browse/APPC-1157>`_ - Remove Mockito from Client jar

	- `APPC-1175 <https://jira.onap.org/browse/APPC-1175>`_ - Appc daily docker job is failing

	- `APPC-1184 <https://jira.onap.org/browse/APPC-1184>`_ - APPC LCM-API outdated

	- `APPC-1185 <https://jira.onap.org/browse/APPC-1185>`_ - Rest calls failing with rpc error

	- `APPC-1186 <https://jira.onap.org/browse/APPC-1186>`_ - Fix Rrestart fail if identity-uril is omitted

	- `APPC-1187 <https://jira.onap.org/browse/APPC-1187>`_ - APIDOC Explorer will not expand appc-provider-lcm

	- `APPC-1188 <https://jira.onap.org/browse/APPC-1188>`_ - fix unexpected error when policy-requested action force == FALSE

	- `APPC-1189 <https://jira.onap.org/browse/APPC-1189>`_ - Fix boot-able volume when rebuild LCM action triggerred 

	- `APPC-1191 <https://jira.onap.org/browse/APPC-1191>`_ - Database errors during installation

	- `APPC-1192 <https://jira.onap.org/browse/APPC-1192>`_ - CDT missed to update the table

	- `APPC-1202 <https://jira.onap.org/browse/APPC-1202>`_ - CDT artifacts fail to upload to appc

	- `APPC-1205 <https://jira.onap.org/browse/APPC-1205>`_ - Cdt reference data does not send when data is manually entered

	- `APPC-1206 <https://jira.onap.org/browse/APPC-1206>`_ - appc vm init failure because of docker-compose incorrect path

	- `APPC-1207 <https://jira.onap.org/browse/APPC-1207>`_ - Logging constants are missing in several features

	- `APPC-1215 <https://jira.onap.org/browse/APPC-1215>`_ - No Such Method Exception from GraphExecutor class 

	- `APPC-1218 <https://jira.onap.org/browse/APPC-1218>`_ - Aai connection has a certificate error

	- `APPC-1219 <https://jira.onap.org/browse/APPC-1219>`_ - appc-iaas-adaptor unable to load provider class

	- `APPC-1220 <https://jira.onap.org/browse/APPC-1220>`_ - NoClassDefFound when sending OS STOP

	- `APPC-1221 <https://jira.onap.org/browse/APPC-1221>`_ - update provide1 portion in appc.properties for Windriver lab

	- `APPC-1224 <https://jira.onap.org/browse/APPC-1224>`_ - Not relaying back SubRequestID back to Policy in DMaaP Response messages

	- `APPC-1225 <https://jira.onap.org/browse/APPC-1225>`_ - APPC failing health checks

	- `APPC-1226 <https://jira.onap.org/browse/APPC-1226>`_ - Mock code to mimic backend execution for Reboot needs to be removed

	- `APPC-1227 <https://jira.onap.org/browse/APPC-1227>`_ - APPC can't read VNF templates build with CDT

	- `APPC-1230 <https://jira.onap.org/browse/APPC-1230>`_ - Update the sqldump data

	- `APPC-1231 <https://jira.onap.org/browse/APPC-1231>`_ - TRANSACTIONS table not getting updated

	- `APPC-1232 <https://jira.onap.org/browse/APPC-1232>`_ - SO error when building config scale out request using APPC client

	- `APPC-1233 <https://jira.onap.org/browse/APPC-1233>`_ - correct health check DG

	- `APPC-1234 <https://jira.onap.org/browse/APPC-1234>`_ - AppC Open Day Light login responds 401 unauthorised

	- `APPC-1237 <https://jira.onap.org/browse/APPC-1237>`_ - APPC not properly url-encoding AAF credentials

	- `APPC-1238 <https://jira.onap.org/browse/APPC-1238>`_ - allottedResourceRole is null

	- `APPC-1239 <https://jira.onap.org/browse/APPC-1239>`_ - Error in appc/deployment/Jmeter JMX 

	- `APPC-1241 <https://jira.onap.org/browse/APPC-1241>`_ - APPC fails to restart vGMUX in vCPE closed loop 

	- `APPC-1243 <https://jira.onap.org/browse/APPC-1243>`_ - Container doesnt preserve mysql data after kubectl edit statefulset

	- `APPC-1244 <https://jira.onap.org/browse/APPC-1244>`_ - Ansible Server never starts

**Known Issue**

	- `APPC-1247 <https://jira.onap.org/browse/APPC-1247>`_ - java.lang.NoClassDefFoundError when publishing DMAAP message

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


