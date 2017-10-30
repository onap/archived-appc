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

Application Controller (APPC) OAM API Guide
===========================================

This guide describes the APPC OAM API that allows the user to manage and control the APPC application/component itself.

APPC OAM Overview
-----------------

APPC **OAM** API commands affect the state of the APPC application instance itself; whereas the APPC **LCM** API commands are issued via APPC and affect the state of VM/VNF/VNFCs.

The APPC OAM API currently provides the following commands:

+--------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------+
| **APPC OAM API**   | **Description**                                                                                                                                              |
+====================+==============================================================================================================================================================+
| maintenance-mode   | Puts the APPC instance into maintenance mode. When APPC is in Maintenance Mode,                                                                              |
|                    |                                                                                                                                                              |
|                    | -  APPC will stop accepting new requests                                                                                                                     |
|                    |                                                                                                                                                              |
|                    | -  The action will be considered complete once all existing requests have completed                                                                          |
+--------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------+
| get-appc-state     | Returns the current state of the running APPC instance                                                                                                       |
+--------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------+
| get-metrics        | Gets list of registered Metrics in APP-C                                                                                                                     |
+--------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------+
| stop               | Force stop an APPC instance. In this mode, all APPC bundles will be stopped via an OSGI API                                                                  |
+--------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------+
| restart            | Restarts an APPC instance, picking up any configuration changes. This command invokes the stop command followed by the start command.                        |
+--------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------+
| start              | Starts the APPC instance; enables appc-provider-lcm so that it can begin to accept LCM request. This includes starting any APPC bundles which are stopped.   |
+--------------------+--------------------------------------------------------------------------------------------------------------------------------------------------------------+

The APPC database data is not affected by any of the OAM operations. The existing APPC configurations are not affected unless a Stop or Restart command is issues (just putting the APPC into maintenance mode and then starting it again is not sufficient). Notifications are sent out for state transitions. Refer to table below for details on state transitions supported.

Detailed Description
--------------------

All APIs are implemented via RESTCONF and can be access via the Application Controller dashboard.

To access the dashboard, go to: ``https://<controller-ip>:8282/apidoc/explorer/index.html``

You should see something similar to below.

	.. image:: media/AppcAPIdocdiagram.png

The current set of OAM APIs currently available are:

-  maintenance-mode
-  get-appc-state
-  get-metrics
-  stop
-  restart
-  start

These commands operate on the APPC instance. Some usage notes below

-  After a Start or Restart, the APPC should be fully operational, meaning that all APPC bundles have been started, MYSQL is running, and the APPC is accepting new requests

-  When issuing the API which puts the APPC into maintenance mode, wait for all existing operations to complete

   -  If the existing operations still have not completed after a maximum timeout of 60 minutes, the API will abort and be considered as having failed

-  Issuing a Stop does not wait for existing operations to complete

   -  If you want to wait for all existing operations to complete and then stop the APPC, then first issue the API to put the APPC into maintenance mode and \ *then* issue a Stop API

   -  There is a flag on the stop operation to make it "brutal" in order to skip waiting for requests to complete

-  If you change the configuration parameters before performing a restart, it should pick up the new configuration parameters

-  A healthy APPC instance is one which:

   -  Can talk to DMaaP
   -  Can talk to A&AI
   -  Has all Karaf bundles from an APPC installation running
   -  Has MYSQL running

OAM-API State Transitions 
--------------------------

The table below documents the current state transition behaves. This is currently hard coded.

+--------------------------+-------------------+----------------------------+----------------+-----------------+----------------------------------+-----------------------+------------------+-------------+
| **OAM API Command**      |                                                 **APPC Status**                                                                                                               |
+==========================+===================+============================+================+=================+==================================+=======================+==================+=============+
|                          | **Started**       | **In Maintenance Mode**    | **Stopped**    | **Starting**    | **Entering Maintenance Mode**    | **Force Stopping**    | **Restarting**   | **Error**   |
+--------------------------+-------------------+----------------------------+----------------+-----------------+----------------------------------+-----------------------+------------------+-------------+
| Start                    | Reject            | Accept                     | Accept         | Reject          | Reject                           | Reject                | Reject           | Accept      |
+--------------------------+-------------------+----------------------------+----------------+-----------------+----------------------------------+-----------------------+------------------+-------------+
| Enter Maintenance Mode   | Accept            | Reject                     | Reject         | Reject          | Reject                           | Reject                | Reject           | Reject      |
+--------------------------+-------------------+----------------------------+----------------+-----------------+----------------------------------+-----------------------+------------------+-------------+
| Force Stop               | Accept            | Accept                     | Reject         | Accept          | Accept                           | Reject                | Reject           | Accept      |
+--------------------------+-------------------+----------------------------+----------------+-----------------+----------------------------------+-----------------------+------------------+-------------+
| Restart                  | Accept            | Accept                     | Accept         | Accept          | Accept                           | Reject                | Reject           | Accept      |
+--------------------------+-------------------+----------------------------+----------------+-----------------+----------------------------------+-----------------------+------------------+-------------+

OAM Commands
------------

This section descripted the OAM API commands that are available for the Amsterdam release.

Maintenance Mode API
~~~~~~~~~~~~~~~~~~~~

**Functional Description:**

-  Puts an APP-C instance into the Maintenance Mode state
-  Waits for all currently executing or queued requests to complete
-  Can only be issued from the Started state

 

**Request (Input) Example:**

**POST** ``https://10.147.104.163:8443/restconf/operations/appc-oam:maintenance-mode``

  ::

      {
        "input": {
          "common-header": {
            "flags": {
              "request-timeout": "0"
            },
            "request-id": " ecompController ",
            "originator-id": "demo-oam-maintenanceMode-id#1"
           }
        }
      }

     

**Response (Output) Example:**

  **Maintenance-mode Response – Success Case**
  
   ::

       {
         "output": {
           "status": {
             "code": 100,
             "message": "ACCEPTED - request accepted"
           },
           "common-header": {
             "request-id": "demo-oam-maintenanceMode-id#1",
             "originator-id": "ecompController"
           }
         }
       }

    
  **Maintenance-mode Response – Rejection Case**

   ::
	
       {
         "output": {
           "status": {
             "code": 300,
             "message": "REJECTED - Invalid State Transition"
           },
           "common-header": {
             "request-id": "demo-oam-maintenanceMode-id#1",
             "originator-id": "ecompController"
           }
         }
       }

	   
**Audit Log Examples- Success Case**

  ::

    2017-06-02T13:58:55Z\|2017-06-02T13:58:55Z\|demo-oam-maintenance-mode-id#1\|\|qtp1068080075-58
    -
    /restconf/operations/appc-oam:maintenance-mode\|appc\|maintenance\_mode\|ecompController\|COMPLETE\|100\|ACCEPTED
    - request accepted\|\|INFO
    \|\|127.0.0.1\|9\|localhost\|\|org.openecomp.appc.oam.AppcOam\|\|\|\|\|\|\|APPC0154W
    Application APPC is entering maintenance mode...

    2017-06-02T13:58:55Z\|2017-06-02T13:59:05Z\|demo-oam-maintenance-mode-id#1\|\|org.openecomp.appc.oam-bundle
    scheduledExecutor\|appc\|maintenance\_mode\|ecompController\|COMPLETE\|400\|SUCCESS
    - request has been processed successfully\|\|INFO
    \|\|127.0.0.1\|10033\|localhost\|\|\|\|\|\|\|\|\|APPC0155W
    Application APPC is in maintenance mode

Get APPC State API
~~~~~~~~~~~~~~~~~~

**Functional Description:**

-  Retrieves the current state of the APP-C instance. 

   -  If none of the other APPC State APIs have been used yet (i.e.; ``appc-oam:start``, ``appc-oam:maintenance-mode``, ``appc-oam:stop``, ``appc-oam:restart``), this command will read all the APPC-LCM bundles states and pick up the lowest bundle state as its response.

-  The APPC States versus the OSGI Bundle state mapping is defined as
   follows:

+---------------------------+-------------------------+
| **Appc State**            | **OSGi Bundle State**   |
+===========================+=========================+
| EnteringMaintenanceMode   | ACTIVE                  |
+---------------------------+-------------------------+
| Error                     |                         |
+---------------------------+-------------------------+
| Instantiated              | INSTALLED               |
+---------------------------+-------------------------+
| MaintenanceMode           | ACTIVE                  |
+---------------------------+-------------------------+
| NotInstantiated           | UNINSTALLED             |
+---------------------------+-------------------------+
| Restarting                |                         |
+---------------------------+-------------------------+
| Started                   | ACTIVE                  |
+---------------------------+-------------------------+
| Starting                  | STARTING                |
+---------------------------+-------------------------+
| Stopped                   | RESOLVED                |
+---------------------------+-------------------------+
| Stopping                  | STOPPING                |
+---------------------------+-------------------------+
| Unknown                   |                         |
+---------------------------+-------------------------+

**Request (Input) example:**

**POST**  ``https://10.147.104.163:8443/restconf/operations/appc-oam:get-appc-state``

**Response (Output) example:**

  **Response: Get-Appc-Status – when APPC in Running state**
	
    ::
  
       {
         "output": {
           "state": "Started"
         }
       }

  **Response: Get-Appc-Status – when APPC in Maintenance Mode state**

    ::	

       {
         "output": {
           "state": "MaintenanceMode"
         }
       }

  **Response: Get-Appc-Status – when APPC in Entering-Maintenance-Mode state**

    ::
	  
       {
         "output": {
           "state": "EnteringMaintenanceMode"
         }
       }

  **Response: Get-Appc-Status – when APPC in Error state** 
  
   ::

       {
         "output": {
           "state": "Error"
       }


Get Metrics API
~~~~~~~~~~~~~~~

**Functional Description:**

-  This operation gets list of registered Metrics in APPC.
-  Metrics service must be enabled.

**Request (Input) example:**

**POST** ``https://10.147.104.163:8443/restconf/operations/appc-oam:get-metrics``

**Response (Output) example:**

    **Response: get-metrics-Status – when APPC Metrics service is not enabled**
	
   ::

       {
         "errors": {
           "error": [
             {
               "error-type": "application",
               "error-tag": "operation-failed",
               "error-message": "Metric Service not enabled",
               "error-info": "<severity>error</severity>"
             }
           ]
         }
       }


Stop API
~~~~~~~~

**Functional Description:**

-  Force stops the APPC bundles that accept LCM requests
-  Does not wait for any currently executing or queued requests to complete
-  Can be issued from the Started, Maintenance Mode, Starting or Entering Maintenance Mode states,

**Request (Input) example:**

**POST** ``https://10.147.104.163:8443/restconf/operations/appc-oam:stop``
 
  :: 

       {
         "input": {
            "common-header": {
              "flags": {
                "request-timeout": "0"
              },
              "request-id": "ecompController",,
              "originator-id": " demo-oam-stop-id#1"
            }
          }
       }

**Response (Output) example:**

  **Stop Response – Success Case**  Expand source
  
  ::

		{
		   "output": {
			 "status": {
			   "code": 100,
			   "message": "ACCEPTED - request accepted"
			 },
			 "common-header": {
			   "request-id": "demo-oam-stop-id#1",
			   "originator-id": "ecompController"
			 }
			}
		}

Restart API
~~~~~~~~~~~

**Functional Description:**

-  Restarts an APP-C instance
-  Does not wait for any currently executing or queued requests to complete
-  Can be issued from any state
-  Restart command will

   -  Tell dispatcher to start to reject new APPC LCM operation requests
   -  Immediately kill all currently running APPC LCM operations
   -  Stops all APPC bundles
   -  Stop MYSQL
   -  Start MYSQL
   -  Start all APPC Bundles
   -  Tell dispatcher to allow APPC to start accepting operations
   -  Return success

-  APPC DB data should not be affected
-  Any configuration parameters which were changed prior to the restart have been picked up

**Request (Input) example:**

**POST** ``https://10.147.104.163:8443/restconf/operations/appc-oam:restart``
 
  ::

    {
      "input": {
        "common-header" : {
          "originator-id" : "ecompController",
          "request-id" : "demo-oam-restart-id#1"
        }
      }
    }

**Response (Output) example:**

    **Restart Response – Success Case**
	
       ::
	 
		{
		  "output": {
		    "status": {
		      "code": 100,
		      "message": "ACCEPTED - request accepted"
		    },
		    "common-header": {
		      "request-id": "demo-oam-restart-id#1",
		      "originator-id": "ecompController"
		    }
		  }
		}

    **Restart Response – Rejection case**  Expand source
	
       ::

		{
		  "output": {
		    "status": {
		      "code": 300,
		      "message": "REJECTED - Restart API is not allowed when APPC is in the Restarting state."
		    },
		    "common-header": {
		      "request-id": "demo-oam-restart-id#1",
		      "originator-id": "ecompController"
		    }
		  }
		}

**Audit Log Examples - Success Case**

  ::

		C2017-06-23T16:11:02Z\|2017-06-23T16:11:02Z\|demo-oam-restart-id#1\|\|qtp1752316482-134
		-
		/restconf/operations/appc-oam:restart\|appc\|restart\|ecompController\|COMPLETE\|100\|ACCEPTED
		- request accepted\|\|INFO
		\|\|127.0.0.1\|13\|localhost\|\|org.openecomp.appc.oam.AppcOam\|\|\|\|\|\|\|APPC0162W
		Application APPC is Restarting

		2017-06-23T16:11:02Z\|2017-06-23T16:11:51Z\|demo-oam-restart-id#1\|\|org.openecomp.appc.oam-bundle
		scheduledExecutor\|appc\|restart\|ecompController\|COMPLETE\|400\|SUCCESS
		- request has been processed successfully\|\|INFO
		\|\|127.0.0.1\|49198\|localhost\|\|org.openecomp.appc.oam.AppcOam\|\|\|\|\|\|\|APPC0157I
		Application APPC is Started



Start API
~~~~~~~~~

**Functional Description:**

-  Starts an APP-C instance
-  Can only be issued from the Stopped or Maintenance Mode states    

**Request (Input) example:**

**POST** ``https://10.147.104.163:8443/restconf/operations/appc-oam:start``
     
  ::	

    {
      "input": {
        "common-header": {
          "flags": {
            "request-timeout": "0"
          },
          "request-id": "ecompController",
          "originator-id": "demo-oam-start-id#1"
        }
      }
    }

     
**Response (Output) example:**

    **Response: appc-oam:start – Success case**
	
	  ::

		{
		  "output": {
		    "status": {
		      "code": 100,
		      "message": "ACCEPTED - request accepted"
		    },
		    "common-header": {
		      "request-id": "demo-oam-start-id#1",
		      "originator-id": "ecompController"
		    }
		  }
		}

    **Response: appc-oam-status – Rejection case**
	
	  ::

		{
		  "output": {
		    "status": {
		      "code": 300,
		      "message": "REJECTED - Invalid State Transition"
		    },
		    "common-header": {
		      "request-id": "demo-oam-start-id#1",
		      "originator-id": "ecompController"
		    }
		  }
		}

**Audit Log Examples**

    **Audit Log - Rejection** 
	
	  ::

		2017-06-02T13:58:39Z\|2017-06-02T13:58:39Z\|\|\|qtp1068080075-57 -
		/restconf/operations/appc-oam:start\|\|\|\|ERROR\|300\|REJECTED -
		Invalid State Transition\|\|INFO
		\|\|\|15\|\|\|org.openecomp.appc.oam.AppcOam\|\|\|\|\|\|\|APPC0156I
		Application APPC is starting...

    **Audit Log - Success case**
	
	  ::

		2017-06-02T13:59:16Z\|2017-06-02T13:59:16Z\|demo-oam-start-id#1\|\|qtp1068080075-58-
		/restconf/operations/appc-oam:start\|appc\|start\|ecompController\|COMPLETE\|100\|ACCEPTED
		- request accepted\|\|INFO
		\|\|127.0.0.1\|2\|localhost\|\|org.openecomp.appc.oam.AppcOam\|\|\|\|\|\|\|APPC0156I
		Application APPC is starting...
		2017-06-02T13:59:16Z\|2017-06-02T13:59:17Z\|demo-oam-start-id#1\|\|org.openecomp.appc.oam-bundle
		scheduledExecutor\|appc\|start\|ecompController\|COMPLETE\|400\|SUCCESS
		- request has been processed successfully\|\|INFO
		\|\|127.0.0.1\|1007\|localhost\|\|\|\|\|\|\|\|\|APPC0157I
		Application APPC is started
