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
 
 - Added support for the following LCM actions: 
 
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
	
	  - ConfigScaleOut (more details can be found in teh APPC Epic: `APPC-431 <https://jira.onap.org/browse/APPC-431>`)
	  
    - To support general operations 
	
	  - ActionStatus
	  
    - A desciption of all of the above LCM actions can be found in the APPC LCM API Guide on readthedoc:

 - Contributed the APPC Controller Design Tool (CDT), which enables self-serve capabilities by allowing users to model their VNF/VNFC for consumption by APPC to use in the execution of requests to perform life cycle management activities.
 
	- More details on the APPC CDT can be found in the APPC CDT User Guide in readthedocs.
	- Additional information on how the APPC CDT tool was used to model the vLB and build teh artifacts needed by APPC to execute teh ConfigScaleOut action can be found at the following wiki pages: https://wiki.onap.org/pages/viewpage.action?pageId=33065185 
	
 - Additional contributions as part of Beijing include: 
 
	- Support for Platform Maturity requirements, including:
	
       - Increased security
	   
		  - Added security to ODL web-based API access via AAF (see `APPC-404 <https://jira.onap.org/browse/APPC-404>` for additional details)
		  - Addressed critical alerts reported via Nexus IQ to the extent possible (see `APPC-656 <https://jira.onap.org/browse/APPC-656>`)
		  
       - Stability
	   
		  - Executed 72 hour stability test on both Heat and OOM deployed environments using JMeter to drive a steady set of transactions over the 72 hour period (see the following wiki page for more details: https://wiki.onap.org/display/DW/ONAP+APPC+72+Hour+Stability+Test+Results )
		  
       - Resiliency
	   
		  - Support for OOM deployment, which enables resiliency via use of Kubernetes (see `APPC-414 <https://jira.onap.org/browse/APPC-414>` for additional details) 
		  
	- Upgraded OpenDaylight (ODL) version to Nitrogen
      
      


**Bug Fixes**

	- The following defects that were documented as known issues in Amsterdam have been fixed in Beijing release:
	
	 	- `APPC-316 <https://jira.onap.org/browse/APPC-316>`_ - Null payload issue for Stop Application
	
	    - `APPC-315 <https://jira.onap.org/browse/APPC-315>`_ - appc-request-handler is giving error java.lang.NoClassDefFoundError 

	    - `APPC-312 <https://jira.onap.org/browse/APPC-312>`_ - APPC request is going to wrong request handler and rejecting request. 
	
	    - `APPC-311 <https://jira.onap.org/browse/APPC-311>`_ - The APPC LCM Provider Healthcheck
	
	    - `APPC-309 <https://jira.onap.org/browse/APPC-309>`_ - APPC LCM Provider URL missing in appc.properties. 
	
	    - `APPC-307 <https://jira.onap.org/browse/APPC-307>`_ - Embed jackson-annotations dependency in appc-dg-common during run-time 
	
	    - `APPC-276 <https://jira.onap.org/browse/APPC-276>`_ - Some Junit are breaking convention causing excessively long build
	  
	    - `APPC-248 <https://jira.onap.org/browse/APPC-248>`_ - There is an compatibility issue between PowerMock and Jacoco which causes Sonar coverage not to be captured. Fix is to move to Mockito.
	
	
**Known Issues**

 	- `APPC-912 <https://jira.onap.org/browse/APPC-912>`_ - MalformedChunkCodingException in MDSALStoreImpl.getNodeName
	
	- `APPC-892 <https://jira.onap.org/browse/APPC-892>`_ - Cntl+4 to highlight and replace feature- Textbox is accepting space  and able to submit without giving any value

	- `APPC-869 <https://jira.onap.org/browse/APPC-869>`_ - VM Snapshot error occurs during image validation.
	
	- `APPC-814 <https://jira.onap.org/browse/APPC-814>`_ - Update openecomp-tosca-datatype namespace  
	
	- `APPC-340 <https://jira.onap.org/browse/APPC-340>`_ - APPC rejecting request even for decimal of millisecond timestamp difference
	 
	- `APPC-154 <https://jira.onap.org/browse/APPC-154>`_ - Logging issue - Request REST API of APPC has RequestID (MDC) in Body or Payload section instead of Header.
	
	
**Security Issues**

	- Communication over DMaaP currently does not use secure topics in this release. This has dependency on DMaaP to enable. 
	- The following Nexus IQ issues are currently open on the Beijing release: 
	  
	   - Critical Threat Level per Nexus IQ Report (Note that these have been assessed for Beijing and determined not to be a risk for APPC based on usage)
	   
	     - CVE-2017-7525 - Component: org.codehaus.jackson : jackson-mapper-asl : 1.9.13 - Filename: jackson-mapper-asl-1.9.13.jar
		 - CVE-2017-7525 - Component: org.codehaus.jackson : jackson-mapper-asl : 1.9.2 - Filename: jackson-mapper-asl-1.9.2.jar
		 - CVE-2017-7525 -  Component: com.fasterxml.jackson.core : jackson-databind : 2.8.1 - Filename: jackson-databind-2.8.1.jar
		 - CVE-2017-7525 - Component: com.fasterxml.jackson.core : jackson-databind : 2.3.2 - Filename: jackson-databind-2.3.2.jar
		 - SONATYPE-2017-0355 - Component: com.fasterxml.jackson.core : jackson-core : 2.3.2 - Filename: jackson-core-2.3.2.jar
		 - SONATYPE-2017-0359 - Component: oorg.apache.karaf.jaas : org.apache.karaf.jaas.modules : 4.0.10 - 4.0.10 - Filename: org.apache.karaf.jaas.modules-4.0.10.jar
         	 - SONATYPE-2017-0359 - Component: org.apache.httpcomponents : httpclient : 4.5.2 - Filename: httpclient-4.5.2.jar
		 - CVE-2017-1000028 - Component: org.glassfish.grizzly : grizzly-http : 2.3.28 - Filename: grizzly-http-2.3.28.jar
		 - SONATYPE-2017-0355 -  Component: com.fasterxml.jackson.core : jackson-core : 2.8.1 - Filename: jackson-core-2.8.1.jar
       
	   - Severe Threat Level per Nexus IQ Report
	   
	     - CVE-2018-10237 - Component: com.google.guava : guava : 22.0 - Filename: guava-22.0.jar
		 - CVE-2018-10237 - Component: com.google.guava : guava : 18.0 - Filename: guava-18.0.jar
		 - SONATYPE-2016-0397 -  Component: com.fasterxml.jackson.core : jackson-core : 2.3.2 - Filename: jackson-core-2.3.2.jar
		 - SONATYPE-2017-0356 - Component: io.netty : netty-handler : 4.1.8.Final - Filename: netty-handler-4.1.8.Final.jar
		 - CVE-2016-5725 - Component: com.jcraft : jsch : 0.1.52 - Filename: jsch-0.1.52.jar
		 - CVE-2015-5262 - Component: org.apache.karaf.jaas : org.apache.karaf.jaas.modules : 4.0.10 - Filename: org.apache.karaf.jaas.modules-4.0.10.jar
		 - CVE-2016-5725 - Component: com.jcraft : jsch : 0.1.51 - Filename: jsch-0.1.51.jar

		 
	Full report from Nexus IQ - :download:`appc-Build-20180518-NexusIQ Report.pdf` Copy of full Nexus IQ Report
	
**Other**

- Limitations, Constraints and other worthy notes

  - It is impossible for us to test all aspect of the application. Scope of testing done in Beijing is captured on the following wiki: https://wiki.onap.org/display/DW/APPC+Beijing+Testing+Scope+and+Status
  - Currently APPC only supports OpenStack
  - OpenStack Hypervisorcheck is turned off by default. If you want to invoke this functionality via the appc.properties, you need to enable it and ensure you have Admin level access to OpenStack.
  - Integration with MultiCloud is supported for Standalone Restart (i.e., not via DGOrchestrator). For any other action, such as Stop, Start, etc.. via MultiCloud requires the MultiCloud identity URL to be either passed in the payload or defined in appc.properties.
  - APPC needs Admin level access for Tenant level operations. 
  - Currently, the "ModifyConfig" API and the implementation in the Master Directed Graph is only designed to work with the vFW Closed-Loop Demo.
  


===========

End of Release Notes

