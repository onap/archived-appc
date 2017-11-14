.. ============LICENSE_START==========================================
.. ===================================================================
.. Copyright © 2017 AT&T Intellectual Property. All rights reserved.
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
.. ECOMP is a trademark and service mark of AT&T Intellectual Property.

Release Notes
=============

.. note
..	* This Release Notes must be updated each time the team decides to Release new artifacts.
..	* The scope of this Release Notes is for this particular component. In other words, each ONAP component has its Release Notes.
..	* This Release Notes is cumulative, the most recently Released artifact is made visible in the top of this Release Notes.
..	* Except the date and the version number, all the other sections are optional but there must be at least one section describing the purpose of this new release.
..	* This note must be removed after content has been added.


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

 	- `APPC-316 <https://jira.onap.org/browse/APPC-316>`_ - Null payload issue for Stop Application; Stop action will not work with a null payload. Ensure when testing Stop action that payload is included. 
	
	- `APPC-315 <https://jira.onap.org/browse/APPC-315>`_ - appc-request-handler is giving error java.lang.NoClassDefFoundError - This error results in vnf state and lock not being released. The workaround is to manually remove the entries from the VNF_LOCK_MANAGEMENT and VNF_STATE_MANAGEMENT tables.
	   - ``DELETE FROM VNF_LOCK_MANAGEMENT where RESOURCE_ID="vnf-id"``
	   - ``DELETE FROM VNF_STATE_MANAGEMENT where VNF_ID="vnf-id"``

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

