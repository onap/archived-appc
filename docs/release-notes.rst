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

	- `APPC-312 <https://jira.onap.org/browse/APPC-312>`_ - APPC request is going to wrong request handler and rejecting request. 

	- `APPC-311 <https://jira.onap.org/browse/APPC-311>`_ - The APPC LCM Provider Healthcheck

	- `APPC-309 <https://jira.onap.org/browse/APPC-309>`_ - APPC LCM Provider URL missing in appc.properties. 

	- `APPC-307 <https://jira.onap.org/browse/APPC-307>`_ - Embed jackson-annotations dependency in appc-dg-common during run-time 

	- `APPC-276 <https://jira.onap.org/browse/APPC-276>`_ - Some Junit are breaking convention causing excessively long build
  
	- `APPC-248 <https://jira.onap.org/browse/APPC-248>`_ - There is an compatibility issue between PowerMock and Jacoco which causes Sonar coverage not to be captured. Fix is to move to Mockito.
	
	
**Known Issues**

The following issues remain open at the end of Beijing release. 

 	- `APPC-912 <https://jira.onap.org/browse/APPC-912>`_ - MalformedChunkCodingException in MDSALStoreImpl.getNodeName
	
	- `APPC-892 <https://jira.onap.org/browse/APPC-892>`_ - Cntl+4 to highlight and replace feature- Textbox is accepting space  and able to submit without giving any value

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

	  - It is impossible for us to test all aspect of the application. Scope of testing done in Beijing is captured on the following wiki:   https://wiki.onap.org/display/DW/APPC+Beijing+Testing+Scope+and+Status
	  - Currently APPC only supports OpenStack
	  - OpenStack Hypervisorcheck is turned off by default. If you want to invoke this functionality via the appc.properties, you need to enable it and ensure you have Admin level access to OpenStack.
	  - Integration with MultiCloud is supported for Standalone Restart (i.e., not via DGOrchestrator). For any other action, such as Stop, Start, etc.. via MultiCloud requires the MultiCloud identity URL to be either passed in the payload or defined in appc.properties.
	  - APPC needs Admin level access for Tenant level operations. 
	  - Currently, the "ModifyConfig" API and the implementation in the Master Directed Graph is only designed to work with the vFW Closed-Loop Demo.
  


===========

End of Release Notes

