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
.. ECOMP is a trademark and service mark of AT&T Intellectual Property.

.. _appc_api_guide:

==================
APPC LCM API Guide
==================

Introduction
============

This guide describes the APPC API that allows you to manage and control the life cycle of controlled virtual network functions (VNFs).


Target Audience
---------------
This document is intended for an advanced technical audience, such as the engineers or architects who need to use this guide to develop an interfacing application. The guide assumes a knowledge of the Open Network Automation Platform (ONAP) components and features, and familiarity with JSON notation.


Life Cycle Management Commands
==============================

APPC receives commands from external ONAP components, such as SO, Policy, DCAE, or the Portal, to manage the life cycle of virtual applications and their components.

A virtual application is composed of the following layers of network technology:

- Virtual Network Function (VNF)
- Virtual Network Function Component (VNFC)
- Virtual Machine (VM)

A Life Cycle Management (LCM) command may affect one or more of these layers.

An LCM command is sent as a request to the APPC using an HTTP POST request or in a message on a message bus (DMaaP).  A request may result in either a single synchronous response or multiple asynchronous responses:

- An **asynchronous** command, which is sent as an authorized and valid request, results in at least two discrete response events:
    - an accept response, to indicate that the request is accepted for processing
    - a final response to indicate the status and outcome of the request processing
    - An unauthorized or invalid request results in a single ERROR response.

- A **synchronous** command, such as Lock or Unlock, results in a single response that is either SUCCESS or ERROR.

**NOTE:** For both asynchronous or synchronous commands, the first response is always returned using the same transport that the initial action used. For example, if the action request was via the message bus (such as when it originates from Policy), then the response is also via the message bus. However, if the request was via a direct HTTP call, the response is similarly a synchronous HTTP response.


Message Bus and the LCM API Client Library
------------------------------------------

The recommended approach for sending/receiving requests to APPC is via the message bus.   To support this approach, an APPC client library is available and should be used.  The client library aims to provide consumers of APPC capabilities with a strongly-typed Java interface and to encapsulate the actual interaction with APPC component via the message bus.

For more details, see the APPC Client Library Guide at:

  :ref:`appc_client_library`


The client library supports both synchronous and asynchronous flows as follows.

Asynchronous Flow
^^^^^^^^^^^^^^^^^

- The APPC Client Library is called via an asynchronous API using a full command object, which is mapped to a JSON representation.
- The APPC client calls the message bus client and sends the JSON command to a configured topic.
- The APPC client pulls response messages from the configured topic.
- On receiving the response for the command, APPC client runs the relevant callback method of the consumer ResponseHandler.

Synchronous Flow
^^^^^^^^^^^^^^^^

- The APPC Client Library is called via a synchronous API using a full command object, which is mapped to a JSON representation.
- The APPC client calls the message bus client and sends the JSON command to a configured topic.
- The APPC client pulls response messages from the configured topic.
- On receiving the final response for the command, the APPC client returns the response object with a final status.

The client library adds the following wrapper around request and responses to the LCM API (described below)::

    {
        "version" : "2.0",
        "cambria.partition" : "<TOPIC>",
        "correlation-id" :"<CORRELATION_ID>",
        "rpc-name" : "<RPC_NME>",
        "type" : <MESSAGE_TYPE>
        "body" : <RPC_SPECIFIC_BODY>
    }



Table 1 Request / Response Message Fields

+----------------------+----------------------------------------------------------------------------------------------------------------+---------------------+
| **Field**            | **Description**                                                                                                | **Required**        |
+======================+================================================================================================================+=====================+
| version              | Indicates the version of the message bus protocol with APPC. Version 2.0 should be used.                       |     Yes             |
+----------------------+----------------------------------------------------------------------------------------------------------------+---------------------+
| cambria. partition   | Indicates the specific topic partition that the message is intended for. For example:                          |     No              |
|                      |                                                                                                                |                     |
|                      | -  For incoming messages, this value should be ``APPC``.                                                       |                     |
|                      |                                                                                                                |                     |
+----------------------+----------------------------------------------------------------------------------------------------------------+---------------------+
| correlation- id      | Correlation ID used for associating responses in APPC Client Library.                                          |     Yes             |
|                      | Built as: ``<request-id>-<sub-request-id>``                                                                    |                     |
+----------------------+----------------------------------------------------------------------------------------------------------------+---------------------+
| rpc-name             | The target Remote Processing Call (RPC) name which should match the LCM command name. For example:``configure``|     Yes             |
|                      |                                                                                                                |                     |
|                      | The convention for RPC names and the target URL is that multi-word command names should have a dash between    |                     |
|                      | words, e.g.,                                                                                                   |                     |
|                      | /restconf/operations/appc-provider-lcm:action-status                                                           |                     |
+----------------------+----------------------------------------------------------------------------------------------------------------+---------------------+
| type                 | Message type: request, response or error                                                                       |     Yes             |
+----------------------+----------------------------------------------------------------------------------------------------------------+---------------------+
| body                 | Contains the input or output LCM command content, which is either the request or response                      |                     |
|                      | The body field format is identical to the equivalent HTTP Rest API command based on the specific RPC name.     |     Yes             |
|                      | For example::                                                                                                  |                     |
|                      |                                                                                                                |                     |
|                      |     {                                                                                                          |                     |
|                      |     "input" : {                                                                                                |                     |
|                      |                 "common-header" : {...}                                                                        |                     |
|                      |                 "action" : "configure",                                                                        |                     |
|	               |		 "action-identifiers" : {...},                                                                  |                     |
|                      |                 "payload": "..."                                                                               |                     |
|                      |     }                                                                                                          |                     |
+----------------------+----------------------------------------------------------------------------------------------------------------+---------------------+


Generic Request Format
----------------------

The LCM API general request format is applicable for both POST HTTP API and for the message body received via the message bus.

LCM Request
^^^^^^^^^^^

The LCM request comprises a common header and a section containing the details of the LCM action.
The LCM request conforms to the following structure::

    {
    "input": {
                "common-header": {"timestamp": "<TIMESTAMP>",
                                        "api-ver": "<API_VERSION>",
                                        "originator-id": "<SYSTEM_ID>",
                                        "request-id": "<REQUEST_ID>",
                                        "sub-request-id": "<SUBREQUEST_ID>",
                                        "flags": {
                                                   "mode": "<EXCLUSIVE|NORMAL>",
                                                   "force": "<TRUE|FALSE>",
                                                   "ttl": "<TTL_VALUE>"
                                                 }
                                 },
                "action": "<COMMAND_ACTION>",
                "action-identifiers": {
                                        "vnf-id": "<VNF_ID>",
                                        "vnfc-name": "<VNFC_NAME>",
                                        "vserver-id": "VSERVER_ID"
                                      },
                ["payload": "<PAYLOAD>"]
             }
    }


Table 2 LCM Request Fields

+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     **Field**             |     **Description**                                                                                                                                                                                                                                                                                                                                                         |     **Required?**   |
+===========================+=============================================================================================================================================================================================================================================================================================================================================================================+=====================+
|     input                 |     The block that defines the details of the input to the command processing. Contains the common-header details.                                                                                                                                                                                                                                                          |     Yes             |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     common- header        |     The block that contains the generic details about a request.                                                                                                                                                                                                                                                                                                            |     Yes             |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     timestamp             |     The time of the request, in ISO 8601 format, ZULU offset. For example: 2016-08-03T08:50:18.97Z.                                                                                                                                                                                                                                                                         |     Yes             |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     APPC will reject the request if timestamp is in the future (due to clock error), or timestamp is too old (compared to TTL flag)                                                                                                                                                                                                                                         |                     |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     api-ver               |     Identifies the API version, in X.YY format, where X denotes the major version increased with each APPC release, and YY is the minor release version.                                                                                                                                                                                                                    |     Yes             |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     2.00 should be used for all LCM API requests                                                                                                                                                                                                                                                                                                                            |                     |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     originator-id         |     An identifier of the calling system limited to a length of 40 characters.                                                                                                                                                                                                                                                                                               |     Yes             |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     It can be used for addressing purposes, such as to return an asynchronous response to the correct destination, in particular where there are multiple consumers of APPC APIs.                                                                                                                                                                                           |                     |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     request-id            |     The UUID for the request ID, limited to a length of 40 characters. The unique OSS/BSS identifier for the request ID that triggers the current LCM action. Multiple API calls can be made with the same request-id.                                                                                                                                                      |     Yes             |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     The request-id is stored throughout the operations performed during a single request.                                                                                                                                                                                                                                                                                   |                     |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     sub-request-id        |     Uniquely identifies a specific LCM or control action, limited to a length of 40 characters. Persists throughout the life cycle of a single request.                                                                                                                                                                                                                     |     No              |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     flags                 | Generic flags that apply to all LCM actions:                                                                                                                                                                                                                                                                                                                                |     No              |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           | -  "MODE" :                                                                                                                                                                                                                                                                                                                                                                 |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |    -  "EXCLUSIVE" - reject requests on this VNF while another request is in progress, or                                                                                                                                                                                                                                                                                    |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |    -  "NORMAL" - allow requests (pending additional validations) on this VNF if there is another request is in progress.                                                                                                                                                                                                                                                    |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           | -  "FORCE" :                                                                                                                                                                                                                                                                                                                                                                |                     |
|                           |       - **TRUE** – forces APPC to process the request regardless of whether there is another request for the VNF or VM in progress.                                                                                                                                                                                                                                         |                     |
|                           |       - **FALSE** – default value. Will return an error if there is another action in progress on the same VNF or VM, unless the two actions are allowed in parallel based on a Request Management Model stored in APPC. The model allows some non-disruptive actions such as Lock, Unlock, CheckLock, and ActionStatus to be performed in conjunction with other actions.  |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           | -  "TTL": <0....N> - The timeout value is used to determine if the request timeout has been exceeded (i.e., if the TTL value is less than the current time minus the timestamp, the request is rejected). The value is in seconds.                                                                                                                                          |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     If no TTL value provided, the default/configurable TTL value is to be used.                                                                                                                                                                                                                                                                                             |                     |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     action                |     The action to be taken by APPC, for example: Test, Start                                                                                                                                                                                                                                                                                                                |     Yes             |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     These are case-sensitive; e.g.,”Restart” is correct; “restart” is incorrect.                                                                                                                                                                                                                                                                                            |                     | 
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     ***NOTE:** The specific value for the action parameter is provided for each command.                                                                                                                                                                                                                                                                                    |                     |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     action-identifiers    |     A block containing the action arguments. These are used to specify the object upon which APPC LCM command is to operate. At least one action-identifier must be specified (note that vnf-id is mandatory). For actions that are at the VM level, the action-identifiers provided would be vnf-id and vserver-id.                                                        |     Yes             |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     service-instance-id   |     Identifies a specific service instance that the command refers to. When multiple APPC instances are used and applied to a subset of services, this will become significant. The field is mandatory when the vnf-id is empty. Currently not used.                                                                                                                        |     No              |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     vnf-id                |     Identifies the VNF instance to which this action is to be applied. Required for actions.                                                                                                                                                                                                                                                                                |     Yes             |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     vnfc-name             |     Identifies the VNFC instance to which this action is to be applied. Required if the action applied to a specific VNFC. Currently not used.                                                                                                                                                                                                                              |     No              |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     vserver-id            |     Identifies a specific VM instance to which this action is to be applied. Required if the action applied to a specific VM. (Populate the vserver-id field with the UUID of the VM)                                                                                                                                                                                       |     No              |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     vf-module-id          |     Identifies a specific VF module to which this action is to be applied. Required if the action applied to a specific VF module.                                                                                                                                                                                                                                          |     No              |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     payload               |     An action-specific open-format field.                                                                                                                                                                                                                                                                                                                                   |     No              |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     The payload can be any valid JSON string value. JSON escape characters need to be added when an inner JSON string is included within the payload, for example:                                                                                                                                                                                                          |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |        ``"{\" vnf-host-ip-address\": \"<VNF-HOST-IP-ADDRESS>\"}"``                                                                                                                                                                                                                                                                                                          |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     The payload is typically used to provide parametric data associated with the command, such as a list of configuration parameters.                                                                                                                                                                                                                                       |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     Note that not all LCM commands need have a payload.                                                                                                                                                                                                                                                                                                                     |                     |
|                           |                                                                                                                                                                                                                                                                                                                                                                             |                     |
|                           |     ***NOTE:** See discussion below on the use of payloads for self-service actions.                                                                                                                                                                                                                                                                                        |                     |
+---------------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+

Request Processing and Validation Logic
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

When a new request is received, APPC applies the following validation logic. For any failure, the request is rejected and an error (300 range) is returned.

1. If the request has timeout (i.e., the difference between current
   time and the request timestamp value is greater than TTL value in
   request), a timeout error is returned.

2. If the request is a duplicate of an existing request in progress
   (same request-id, sub-request-id, originator-id), a duplicate error
   is returned.

3. If there is a Lock on the vnf-id, reject any new action if it is not
   associated with the locking request-id, a lockout error is returned.

4. If the Force flag = Y, then allow the new action regardless of
   whether there is an action in progress.

5. If the Mode flag = Exclusive on a request in progress, any new
   request is rejected until the request in progress is completed.

6. If request is received and there are one or more requests in
   progress, then the new request is evaluated to determine if there is
   any overlap in scope with the existing requests (for example, a new
   VNF level request would overlap with another request in progress).

   a. If there is no overlap between the new request and requests in
      progress, the new request is accepted. 

   b. If there is overlap, then only special cases are allowed in
      parallel (for example, Audit and HealthCheck are allowed).


Generic Response Format
-----------------------


This section describes the generic response format.

The response format is applicable for both POST HTTP API and for the message body received via the message bus.


LCM Response
^^^^^^^^^^^^

The LCM response comprises a common header and a section containing the payload and action details.

The LCM response conforms to the following structure::

    {
        "output": {
                    "common-header": {
                                        "api-ver": "<API_VERSION>",
                                        "flags": {
                                                   "ttl": <TTL_VALUE>,
                                                   "force": "<TRUE|FALSE>",
                                                   "mode": "<EXCLUSIVE|NORMAL>"
                                                 },
                                        "originator-id": "<SYSTEM_ID>",
                                        "request-id": "<REQUEST_ID>",
                                        "sub-request-id": "<SUBREQUEST_ID>",
                                        "timestamp": "2016-08-08T23:09:00.11Z",
                                     },
                    "payload": "<PAYLOAD>",
                    [Additional fields],
                    "status": {
                                "code": <RESULT_CODE>,
                                "message": "<RESULT_MESSAGE>"
                              }
                  }
    }


Table 3 LCM Response Fields

+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     **Field**        |     **Description**                                                                                                                                                                                                       |     **Required?**   |
+======================+===========================================================================================================================================================================================================================+=====================+
|     output           |     The block that defines the details of the output of the command processing. Contains the ``common-header`` details.                                                                                                   |     Yes             |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     common- header   |     The block that contains the generic details about a request.                                                                                                                                                          |     Yes             |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     api-ver          |     Identifies the API version, in X.YY format, where X denotes the major version increased with each APPC release, and YY is the minor release version.                                                                  |     Yes             |
|                      |                                                                                                                                                                                                                           |                     |
|                      |     -  2.00 should be used for all LCM API requests                                                                                                                                                                       |                     |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     originator-id    |     An identifier of the calling system limited to a length of 40 characters.                                                                                                                                             |     Yes             |
|                      |                                                                                                                                                                                                                           |                     |
|                      |     It can be used for addressing purposes, such as to return an asynchronous response to the correct destination, in particular where there are multiple consumers of APPC APIs.                                         |                     |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     request-id       |     The UUID for the request ID, limited to a length of 40 characters. The unique OSS/BSS identifier for the request ID that triggers the current LCM action. Multiple API calls can be made with the same request- id.   |     Yes             |
|                      |                                                                                                                                                                                                                           |                     |
|                      |     The request-id is stored throughout the operations performed during a single request.                                                                                                                                 |                     |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     sub-request-id   |     Uniquely identifies a specific LCM or control action, limited to a length of 40 characters. Persists throughout the life cycle of a single request.                                                                   |     No              |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     timestamp        |     The time of the request, in ISO 8601 format, ZULU offset. For example: ``2016-08-03T08:50:18.97Z``.                                                                                                                   |     Yes             |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     status           |     The status describes the outcome of the command processing. Contains a ``code`` and a ``message`` providing success or failure details.                                                                               |     Yes             |
|                      |                                                                                                                                                                                                                           |                     |
|                      |     ***NOTE:** See* status *for code values.*                                                                                                                                                                             |                     |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     payload          |     An open-format field.                                                                                                                                                                                                 |     No              |
|                      |                                                                                                                                                                                                                           |                     |
|                      |     The payload can be any valid JSON string value. JSON escape characters need to be added when an inner JSON string is included within the payload, for example: ``"{\\"upload\_config\_id\\": \\"<value\\"}"``.        |                     |
|                      |                                                                                                                                                                                                                           |                     |
|                      |     The payload is typically used to provide parametric data associated with the response to the command.                                                                                                                 |                     |
|                      |                                                                                                                                                                                                                           |                     |
|                      |     Note that not all LCM commands need have a payload.                                                                                                                                                                   |                     |
|                      |                                                                                                                                                                                                                           |                     |
|                      |     ***NOTE:** The specific value(s) for the response payload, where relevant, is provided for in each* command *description.*                                                                                            |                     |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     [Field name]     |     Additional fields can be provided in the response, if needed, by specific commands.                                                                                                                                   |     No              |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     code             |     A unique pre-defined value that identifies the exact nature of the success or failure status.                                                                                                                         |     No              |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+
|     message          |     The description of the success or failure status.                                                                                                                                                                     |     No              |
+----------------------+---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+


Status Codes
------------

The status code is returned in the response message as the ``code`` parameter, and the description as the message parameter.

The different responses are categorized as follows:

**ACCEPTED**

    Request is valid and accepted for processing.

**ERROR**

    Request invalid or incomplete.

**REJECT**

    Request rejected during processing due to invalid data, such as an
    unsupported command.

**SUCCESS**

    Request is valid and completes successfully.

**FAILURE**

    The request processing resulted in failure.

    A FAILURE response is always returned asynchronously via the message
    bus.

**PARTIAL SUCCESS**

    The request processing resulted in partial success where at least
    one step in a longer process completed successfully.

    A PARTIAL SUCCESS response is always returned asynchronously via the
    message bus.

**PARTIAL FAILURE**

    The request processing resulted in partial failure.

    A PARTIAL FAILURE response is always returned asynchronously via the
    message bus.

+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|     **Category**      |     **Code**   |     **Message / Description**                                                                                                        |
+=======================+================+======================================================================================================================================+
|     ACCEPTED          |     100        |     ACCEPTED - Request accepted                                                                                                      |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|     ERROR             |     200        |     UNEXPECTED ERROR - ${detailedErrorMsg}                                                                                           |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|     REJECT            |     300        |     REJECTED - ${detailedErrorMsg}                                                                                                   |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     301        |     INVALID INPUT PARAMETER -${detailedErrorMsg}                                                                                     |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     302        |     MISSING MANDATORY PARAMETER - Parameter ${paramName} is missing                                                                  |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     303        |     REQUEST PARSING FAILED - ${detailedErrorMsg}                                                                                     |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     304        |     NO TRANSITION DEFINED - No Transition Defined for ${actionName} action and ${currentState} state                                 |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     305        |     ACTION NOT SUPPORTED - ${actionName} action is not supported                                                                     |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     306        |     VNF NOT FOUND - VNF with ID ${vnfId} was not found                                                                               |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     307        |     DG WORKFLOW NOT FOUND - No DG workflow found for the combination of ${dgModule} module ${dgName} name and ${dgVersion} version   |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     308        |     WORKFLOW NOT FOUND - No workflow found for VNF type                                                                              |
|                       |                |                                                                                                                                      |
|                       |                |     ${vnfTypeVersion} and ${actionName} action                                                                                       |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     309        |     UNSTABLE VNF - VNF ${vnfId} is not stable to accept the command                                                                  |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     310        |     LOCKING FAILURE -${detailedErrorMsg}                                                                                             |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     311        |     EXPIREDREQUEST. The request processing time exceeded the maximum available time                                                  |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     312        |     DUPLICATEREQUEST. The request already exists                                                                                     |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     313        |     MISSING VNF DATA IN A&AI - ${attributeName} not found for VNF ID =                                                               |
|                       |                |                                                                                                                                      |
|                       |                |     ${vnfId}                                                                                                                         |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     315        |     MULTIPLE REQUESTS USING SEARCH CRITERIA: ${parameters}                                                                           |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     316        |     POLICY VALIDATION FAILURE - Request rejected as per the request validation policy                                                |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|     SUCCESS           |     400        |     The request was processed successfully                                                                                           |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|     FAILURE           |     401        |     DG FAILURE - ${ detailedErrorMsg }                                                                                               |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     402        |     NO TRANSITION DEFINED - No Transition Defined for ${ actionName} action and ${currentState} state                                |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     403        |     UPDATE\_AAI\_FAILURE - failed to update AAI. ${errorMsg}                                                                         |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     404        |     EXPIRED REQUEST FAILURE - failed during processing because TTL expired                                                           |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     405        |     UNEXPECTED FAILURE - ${detailedErrorMsg}                                                                                         |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     406        |     UNSTABLE VNF FAILURE - VNF ${vnfId} is not stable to accept the command                                                          |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|                       |     450        |     REQUEST NOT SUPPORTED                                                                                                            |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|     PARTIAL SUCCESS   |     500        |     PARTIAL SUCCESS                                                                                                                  |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+
|     PARTIAL FAILURE   |     501 -      |     PARTIAL FAILURE                                                                                                                  |
|                       |     599        |                                                                                                                                      |
+-----------------------+----------------+--------------------------------------------------------------------------------------------------------------------------------------+


Malformed Message Response
--------------------------

A malformed message is an invalid request based on the LCM API YANG scheme specification. APPC rejects malformed requests as implemented by ODL infrastructure level.

**Response Format for Malformed Requests**::

    {
      "errors": {
                  "error": [
                            {
                              "error-type": "protocol",
                              "error-tag": "malformed-message",
                              "error-message": "<ERROR-MESSAGE>",
                              "error-info": "<ERROR-INFO>"
                            }
                           ]
                }
    }


**Example Response**::

    {
      "errors": {
                  "error": [
                            {
                              "error-type": "protocol",
                              "error-tag": "malformed-message",
                              "error-message": "Error parsing input: Invalid value 'Stopp' for
                               enum type. Allowed values are: [Sync, Audit, Stop, Terminate]",
                              "error-info": "java.lang.IllegalArgumentException: Invalid value
                                'Stopp' for enum type. Allowed values are: [Sync, Audit, Stop,
                                Terminate]..."
                            }
                           ]
                }
    }



API Scope
=========

Defines the level at which the LCM command operates for the current release of APPC and the VNF types which are supported for each command.


Commands, or actions, can be performed at one or more of the following scope levels:


+-----------------+----------------------------------------------------------------------------------------+
| **VNF**         | Commands can be applied at the level of a specific VNF instance using the vnf-id.      |
+-----------------+----------------------------------------------------------------------------------------+
| **VF-Module**   | Commands can be applied at the level of a specific VF-Module using the vf-module-id.   |
+-----------------+----------------------------------------------------------------------------------------+
| **VNFC**        | Commands can be applied at the level of a specific VNFC instance using a vnfc-name.    |
+-----------------+----------------------------------------------------------------------------------------+
| **VM**          | Commands can be applied at the level of a specific VM instance using a vserver-id.     |
+-----------------+----------------------------------------------------------------------------------------+


**VNF/VM Types Supported**

Commands, or actions, may be currently supported on all VNF types or a limited set of VNF types. Note that the intent is to support all actions on all VNF types which have been successfully onboarded in a self-service mode.

  - **Any** Currently supported on any vnf-type.

  - **Any (requires self-service onboarding)** Currently supported on any vnf-type which has been onboarded using the APPC self-service onboarding process. See further discussion on self-service onboarding below.


+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     **Command**        | **VNF**   |  **VF-Module**   |     **VNFC**   | **VM**   |     **VNF/VM Types Supported**                             |
+========================+===========+==================+================+==========+============================================================+
|     ActionStatus       | Yes       |                  |                |          |     Any                                                    |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     AttachVolume       |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Audit              | Yes       |                  |                |          |     Any (requires self-service onboarding)                 |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     CheckLock          | Yes       |                  |                |          |     Any                                                    |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Configure          | Yes       |                  |                |          |     Any (requires self-service onboarding)                 |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     ConfigBackup       | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     ConfigModify       | Yes       |                  |                |          |     Any (requires self-service onboarding)                 |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     ConfigRestore      | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     ConfigScaleOut     | Yes       |                  |                |          |     Any (requires self-service onboarding)                 |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     DetachVolume       |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Evacuate           |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     HealthCheck        | Yes       |                  |                |          |     Any (requires self-service onboarding)                 |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Lock               | Yes       |                  |                |          |     Any                                                    |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Migrate            |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     QuiesceTraffic     | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Reboot             |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Rebuild            |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Restart            |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     ResumeTraffic      | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Snapshot           |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Start              |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     StartApplication   | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Stop               |           |                  |                | Yes      |     Any (uses OpenStack command)                           |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     StopApplication    | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Sync               | Yes       |                  |                |          |     Any (requires self-service onboarding)                 |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     Unlock             | Yes       |                  |                |          |     Any                                                    |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     UpgradeBackout     | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     UpgradeBackup      | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     UpgradePostCheck   | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     UpgradePreCheck    | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+
|     UpgradeSoftware    | Yes       |                  |                |          | Chef and Ansible only (requires self-service onboarding)   |
+------------------------+-----------+------------------+----------------+----------+------------------------------------------------------------+



Self-Service VNF Onboarding
---------------------------

The APPC architecture is designed for VNF self-service onboarding (i.e., a VNF owner or vendor through the use of tools can enable a new VNF to support the LCM API actions that are designate as self-service). The VNF must support one or more of the following interface protocols:

-  Netconf with uploadable Yang model (requires a Netconf server running
   on the VNF)

-  Chef (requires a Chef client running on the VNF)

-  Ansible (does not require any changes to the VNF software)

The self-service onboarding process is done using an APPC Design GUI (also referred to as CDT) which interacts with an APPC instance which is dedicated to self-service onboarding. The steps in the onboarding process using the APPC Design GUI are:

-  Define the VNF capabilities (set of actions that the VNF can
   support).

-  Create a template and parameter definitions for actions which use the
   Netconf, Chef, or Ansible protocols. The template is an xml or JSON
   block which defines the “payload” which is included in the request
   that is downloaded the VNF (if Netconf) or Chef/Ansible server.

-  Test actions which have templates/parameter definitions.

-  Upload the VNF definition, template, and parameter definition
   artifacts to SDC which distributes them to all APPC instances in the
   same environment (e.g., production).

For more details, see the APPC CDT Onboarding User Guide.



LCM Commands
============

The LCM commands that are valid for the current release.

ActionStatus
------------

The ActionStatus command returns that state of any action request that has been previously submitted to an APPC instance for a specified VNF. This enables the client to know the status of a previous request and helps them decide if they should reissue a request.

+--------------------------+----------------------------------------------------------+
| **Target URL**           | /restconf /operations/ appc-provider-lcm:action-status   |
+--------------------------+----------------------------------------------------------+
| **Action**               | ActionStatus                                             |
+--------------------------+----------------------------------------------------------+
| **Action-Identifiers**   | vnf-id                                                   |
+--------------------------+----------------------------------------------------------+
| **Payload Parameters**   | See below                                                |
+--------------------------+----------------------------------------------------------+
| **Revision History**     | New in Beijing                                           |
+--------------------------+----------------------------------------------------------+

|

+-----------------------------+------------------------------------------------------------+--------------------+-------------------------------------+
|     **Payload Parameter**   |     **Description**                                        |     **Required**   |     **Example**                     |
+=============================+============================================================+====================+=====================================+
| request-id                  |     Request id from the previously submitted request       | Yes                |     "request-id": "123456789"       |
+-----------------------------+------------------------------------------------------------+--------------------+-------------------------------------+
| sub-request ID              |     Sub-Request id from the previously submitted request   | optional           |     "sub-request-id": "123456789"   |
+-----------------------------+------------------------------------------------------------+--------------------+-------------------------------------+
| originator-id               |     Originator id from the previously submitted request    | optional           |     "originator-id": "123456789"    |
+-----------------------------+------------------------------------------------------------+--------------------+-------------------------------------+


ActionStatus Response:
^^^^^^^^^^^^^^^^^^^^^^

A successful response contains a payload with the following:

+-----------------------------+-----------------------------------------------------------------------+--------------------+------------------------------+
|     **Payload Parameter**   |     **Description**                                                   |     **Required**   |     **Example**              |
+=============================+=======================================================================+====================+==============================+
| status-reason               |     Contains more details about status                                | No                 |                              |
+-----------------------------+-----------------------------------------------------------------------+--------------------+------------------------------+
| status                      |     IN_PROGRESS – The request has been accepted and is in progress    | No                 |     "status": "SUCCESSFUL"   |
|                             |                                                                       |                    |                              |
|                             |     SUCCESSFUL – The request returned success message                 |                    |                              |
|                             |                                                                       |                    |                              |
|                             |     FAILED – The request failed and returned an error message         |                    |                              |
|                             |                                                                       |                    |                              |
|                             |     ABORTED – the request aborted                                     |                    |                              |
|                             |                                                                       |                    |                              |
|                             |     NOT_FOUND – The request is not found                              |                    |                              |
+-----------------------------+-----------------------------------------------------------------------+--------------------+------------------------------+

If the ActionStatus request was rejected or could not be processed, it returns a valid error code or error message (but no payload).Example below:

    ``"message": "MULTIPLE REQUESTS FOUND - using search criteria:
    request- id=c09ac7d1-de62-0016-2000-e63701125559 AND
    vnf-id=ctsf0007v", "code": 315``

AttachVolume
------------

The AttachVolume command attaches a cinder volume to a VM via an Openstack command.

Cinder is a Block Storage service for OpenStack. It's designed to present storage resources to end users that can be consumed by the OpenStack Compute Project (Nova). The short description of Cinder is that it virtualizes the management of block storage devices and provides end users with a self service API to request and consume those resources without requiring any knowledge of where their  storage is actually deployed or on what type of device.

    NOTE: The command implementation is based on Openstack
    functionality. For further details, see
    http://developer.openstack.org/api-ref/compute/.

+--------------------------+----------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:attach-volume     |
+--------------------------+----------------------------------------------------------+
| **Action**               | AttachVolume                                             |
+--------------------------+----------------------------------------------------------+
| **Action-Identifiers**   | vnf-id, vserver-id                                       |
+--------------------------+----------------------------------------------------------+
| **Payload Parameters**   | See table                                                |
+--------------------------+----------------------------------------------------------+
| **Revision History**     | New in Beijing                                           |
+--------------------------+----------------------------------------------------------+

|

+-----------------------------+------------------------------------------------------+--------------------+---------------------------------------------------------------------------------------------------------------------------+
|     **Payload Parameter**   |     **Description**                                  |     **Required**   |     **Example**                                                                                                           |
+=============================+======================================================+====================+===========================================================================================================================+
| volumeId                    |     The UUID of the volume to attach.                | Yes                |     "volumeId": "a26887c6-c47b-4654-abb5-dfadf7d3f803",                                                                   |
+-----------------------------+------------------------------------------------------+--------------------+---------------------------------------------------------------------------------------------------------------------------+
| device                      |     The device identifier                            | Yes                |     "device": "/dev/vdb"                                                                                                  |
+-----------------------------+------------------------------------------------------+--------------------+---------------------------------------------------------------------------------------------------------------------------+
| vm-id                       |     TThe self- link URL of the VM.                   | Yes                |     "vm-id": http://135.25.246.162:8774/v2/64af07e991424b8e9e54eca27d5c0d48/servers/b074cd1b-8d53-412e-a102-351cc51ac10a" |
+-----------------------------+------------------------------------------------------+--------------------+---------------------------------------------------------------------------------------------------------------------------+
| Identity-url                |     The identity URL used to access the resource     | Yes                |     "identity-url": "http://135.25.246.162:5000/v2.0"                                                                     |
+-----------------------------+------------------------------------------------------+--------------------+---------------------------------------------------------------------------------------------------------------------------+

AttachVolume Response:
^^^^^^^^^^^^^^^^^^^^^^

Success: A successful AttachVolume returns a success status code 400.

Failure: A failed AttachVolume returns a failure code 401 and the failure message. Failure messages can include:

-  badRequest
-  unauthorized
-  forbidden
-  itemNotFound


Audit
-----

The Audit command compares the configuration of the VNF associated with the current request against the most recent configuration that is stored in APPC's configuration database.

A successful Audit means that the current VNF configuration matches the latest APPC stored configuration.

A failed Audit indicates that the configurations do not match.

This command can be applied to any VNF type. The only restriction is that the VNF has been onboarded in self-service mode (which requires that the VNF supports a request to return the running configuration).

The Audit action does not require any payload parameters.

**NOTE:** Audit does not return a payload containing details of the comparison, only the Success/Failure status.


+------------------------------+------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:audit     |
+------------------------------+------------------------------------------------------+
|     **Action**               |     Audit                                            |
+------------------------------+------------------------------------------------------+
|     **Action-Identifiers**   |     vnf-id                                           |
+------------------------------+------------------------------------------------------+
|     **Payload Parameters**   |     See below                                        |
+------------------------------+------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                       |
+------------------------------+------------------------------------------------------+

|

+----------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+----------------------------------+
|     **Parameter**    |     **Description**                                                                                                                                       |     **Required?**   |     **Example**                  |
+======================+===========================================================================================================================================================+=====================+==================================+
|     publish-config   |     \* If the publish\_config field is set to Y in the payload, then always send the running configuration from the VNF using the message bus             |     Yes             |     "publish-config": "<Y\|N>"   |
|                      |                                                                                                                                                           |                     |                                  |
|                      |     \* If the publish\_config field is set to N in the payload, then:                                                                                     |                     |                                  |
|                      |                                                                                                                                                           |                     |                                  |
|                      |     - If the result of the audit is ‘match’ (latest APPC config and the running config match), do not send the running configuration                      |                     |                                  |
|                      |                                                                                                                                                           |                     |                                  |
|                      |     - If the result of the audit is ‘no match’, then send the running configuration                                                                       |                     |                                  |
+----------------------+-----------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+----------------------------------+

Audit Response
^^^^^^^^^^^^^^

The audit response returns an indication of success or failure of the audit. If a new configuration is uploaded to the APPC database, the payload contains the ‘upload\_config\_id’ and values for any records created. In addition, the configuration is sent to the bus which may be received by an external configuration storage system.


CheckLock
---------

The CheckLock command returns true if the specified VNF is locked; otherwise, false is returned.

A CheckLock command is deemed successful if the processing completes without error, whether the VNF is locked or not. The command returns only a single response with a final status.

Note that APPC locks the target VNF during any VNF command processing, so a VNF can have a locked status even if no Lock command has been explicitly called.

The CheckLock command returns a specific response structure that extends the default LCM response.

The CheckLock action does not require any payload parameters.

+------------------------------+--------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:checklock   |
+------------------------------+--------------------------------------------------------+
|     **Action**               |     CheckLock                                          |
+------------------------------+--------------------------------------------------------+
|     **Action-Identifiers**   |     vnf-id                                             |
+------------------------------+--------------------------------------------------------+
|     **Payload Parameters**   |     None                                               |
+------------------------------+--------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                         |
+------------------------------+--------------------------------------------------------+

CheckLock Response
^^^^^^^^^^^^^^^^^^

The CheckLock command returns a customized version of the LCM
response.


+---------------------+---------------------------------------------------------------------------------------+--------------------+---------------------------------+
|     **Parameter**   |     **Description**                                                                   |     **Required**   | **?Example**                    |
+=====================+=======================================================================================+====================+=================================+
|     locked          |     "TRUE"\|"FALSE" - returns TRUE if the specified VNF is locked, otherwise FALSE.   |     No             |     "locked": "<TRUE\|FALSE>"   |
+---------------------+---------------------------------------------------------------------------------------+--------------------+---------------------------------+


**Example**::

    {
      "output": {
                  "status": {
                              "code": <RESULT_CODE>, "message": "<RESULT_MESSAGE>"
                            },
                  "common-header": {
                                     "api-ver": "<API_VERSION>",
                                     "request-id": "<ECOMP\_REQUEST_ID>", "originator-id":
                                     "<ECOMP_SYSTEM_ID>",
                                     "sub-request-id": "<ECOMP_SUBREQUEST_ID>", "timestamp":
                                     "2016-08-08T23:09:00.11Z",
                                     "flags": {
                                                "ttl": <TTL_VALUE>, "force": "<TRUE|FALSE>",
                                                "mode": "<EXCLUSIVE|NORMAL>"
                                              }
                                   },
                  "locked": "<TRUE|FALSE>"
    }


Configure
---------

Configure a VNF or a VNFC on the VNF after instantiation.

A set of configuration parameter values specified in the configuration template is included in the request. Other configuration parameter values may be obtained from an external system.

A successful Configure request returns a success response.

A failed Configure action returns a failure response and the specific failure messages in the response block.

+------------------------------+--------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:configure   |
+------------------------------+--------------------------------------------------------+
|     **Action**               |     Configure                                          |
+------------------------------+--------------------------------------------------------+
|     **Action-Identifiers**   |     vnf-id                                             |
+------------------------------+--------------------------------------------------------+
|     **Payload Parameters**   |     See below                                          |
+------------------------------+--------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                         |
+------------------------------+--------------------------------------------------------+

|

+---------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-----------------------------------------------------------------+
|     **Payload Parameter**       |     **Description**                                                                                                                                                                                            |     **Required?**   |     **Example**                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
+=================================+================================================================================================================================================================================================================+=====================+=================================================================+
|     request-parameters          |     vnf-host-ip-address: optional if Netconf or other direct interface to the VNF.   If not provided, APPC will look for the host-ip-address in the A&AI VNF oam ipv4 address field.                           |     No              |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |     "payload":                                                  |
|                                 |     vnfc-type:  must be included if template is vnfc specific                                                                                                                                                  |                     |     "{ \\"request-parameters                                    |
|                                 |                                                                                                                                                                                                                |                     |     \\": {                                                      |
|                                 |                                                                                                                                                                                                                |                     |     \\"vnf-host-ip-address\\":                                  |
|                                 |                                                                                                                                                                                                                |                     |     \\”value\\”,                                                |
|                                 |                                                                                                                                                                                                                |                     |     \\”vnfc-type\\”: \\”value\\”’                               |
|                                 |                                                                                                                                                                                                                |                     |     }                                                           |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
|                                 |                                                                                                                                                                                                                |                     |                                                                 |
+---------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+                                                                 |
|     configuration-parameters    |     A set of instance specific configuration parameters should be specified. If provided, APPC replaces variables in the configuration template with the values supplied.                                      |     No              |      \\"configuration- parameters\\": {\\"<CONFIG- PARAMS>\\"}  |
+---------------------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-----------------------------------------------------------------+


Configure Response
^^^^^^^^^^^^^^^^^^

The Configure response returns an indication of success or failure of the request. 

**Success:** A successful Configure returns a success status code 400.
**Failure:** A failed Configure returns a failure code 401 and the failure message.  

If successful, the return payload contains the ‘upload_config_id’ and values for any records created in the APPC DB. In addition, the configuration is sent to the ONAP Data Router bus  which may be received by an external configuration storage system.

If APPC in unable to update A&AI with the VNFC records, a 501 intermediate error message returned prior to the final 400 or 401 success message.

ConfigModify
------------

Modifies the configuration on a VNF or VNFC in service.

This command is executed either directly on the VNF (such as for Netconf) or using an Ansible playbook or Chef cookbook.

Request Structure:

+--------------------------+--------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:config-modify   |
+--------------------------+--------------------------------------------------------+
| **Action**               | ConfigModify                                           |
+--------------------------+--------------------------------------------------------+
| **Action-Identifiers**   | vnf-id                                                 |
+--------------------------+--------------------------------------------------------+
| **Payload Parameters**   | request-parameters, configuration-parameters           |
+--------------------------+--------------------------------------------------------+
| **Revision History**     | Unchanged in this release.                             |
+--------------------------+--------------------------------------------------------+

Request Payload Parameters:

+-------------------------+----------------------------------------+-----------------+-------------------------------------------------------+
| **Payload Parameter**   | **Description**                        | **Required?**   |     **Example**                                       |
+=========================+========================================+=================+=======================================================+
| request-parameters      | vnf-host-ip-address: optional if       | No              |     "payload":                                        |
|                         | Netconf or other direct interface      |                 |     "{\\"request-parameters \\":                      |
|                         | to the VNF. If not provided, it is     |                 |     {\\"vnf-host-ip-address\\": \\”value\\",          |
|                         | obtained from A&AI                     |                 |     \\”vnfc-type\\”: \\”value\\”                      |
|                         |                                        |                 |     }                                                 |
|                         |                                        |                 |                                                       |
|                         | vnfc-type: must be included if template|                 |                                                       |
|                         | is vnfc specific                       |                 |                                                       |
|                         |                                        |                 |     \\"configuration- parameters\\": {\\"name1\\":    |
|                         |                                        |                 |     \\”value1\\”,\\"name2\\":                         |
|                         |                                        |                 |     \\”value2\\”                                      |
|                         |                                        |                 |     }                                                 |
|                         |                                        |                 |     }                                                 |
+-------------------------+----------------------------------------+-----------------+                                                       |
| configuration-          | A set of instance specific             | No              |                                                       |
| parameters              | configuration parameters should        |                 |                                                       |
|                         | be specified.                          |                 |                                                       |
+-------------------------+----------------------------------------+-----------------+-------------------------------------------------------+

ConfigModify Response
^^^^^^^^^^^^^^^^^^^^^

**Success:** A successful ConfigModify returns a success status code 400.

If successful, the return payload contains the ‘upload_config_id’ and values associated with the configuration stored in the APPC DB. In addition, the configuration is sent to the message bus which may be received by an external configuration storage system.

**Failure:** A failed ConfigModify returns a failure code 401 and the failure message.

ConfigBackup
------------

Stores the current VNF configuration on a local file system (not in APPC). This is limited to Ansible and Chef. There can only be one stored configuration (if there is a previously saved configuration, it is replaced with the current VNF configuration).

A successful ConfigBackup request returns a success response.

A failed ConfigBackup action returns a failure response code and the specific failure message in the response block.

+------------------------------+-----------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:configbackup   |
+------------------------------+-----------------------------------------------------------+
|     **Action**               |     ConfigBackup                                          |
+------------------------------+-----------------------------------------------------------+
|     **Action-Identifiers**   |     Vnf-id                                                |
+------------------------------+-----------------------------------------------------------+
|     **Payload Parameters**   |     See below                                             |
+------------------------------+-----------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                            |
+------------------------------+-----------------------------------------------------------+

|

+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-------------------------------------------------------------------+
|     **Payload Parameter**       |     **Description**                                                                                                                                                                |     **Required?**   |     **Example**                                                   |
+=================================+====================================================================================================================================================================================+=====================+===================================================================+
|     request-parameters          |     Not used. This request is limited to Ansible and Chef only.                                                                                                                    |     No              | "payload": \\"configuration-parameters\\": {\\"<CONFIG-PARAMS>\\"}|
|                                 |                                                                                                                                                                                    |                     |                                                                   |
|                                 |                                                                                                                                                                                    |                     |                                                                   |
|                                 |                                                                                                                                                                                    |                     |                                                                   |
|                                 |                                                                                                                                                                                    |                     |                                                                   |
|                                 |                                                                                                                                                                                    |                     |                                                                   |
+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+                                                                   |
|     configuration-parameters    |     A set of instance specific configuration parameters should be specified, as required by the Chef cookbook or Ansible playbook.                                                 |     No              |                                                                   |
+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-------------------------------------------------------------------+

ConfigBackup Response
^^^^^^^^^^^^^^^^^^^^^

The ConfigBackup response returns an indication of success or failure of the request.

**Success:** A successful ConfigBackup returns a success status code 400.
**Failure:** A failed ConfigBackup returns a failure code 401 and the failure message.  


ConfigRestore
-------------

Applies a previously saved configuration to the active VNF configuration. This is limited to Ansible and Chef. There can only be one stored configuration.

A successful ConfigRestore request returns a success response.

A failed ConfigRestore action returns a failure response code and the specific failure message in the response block.

+------------------------------+------------------------------------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:config-restore                                |
+------------------------------+------------------------------------------------------------------------------------------+
|     **Action**               |     ConfigRestore                                                                        |
+------------------------------+------------------------------------------------------------------------------------------+
|     **Action-Identifiers**   |     Vnf-id                                                                               |
+------------------------------+------------------------------------------------------------------------------------------+
|     **Payload Parameters**   |     See below                                                                            |
+------------------------------+------------------------------------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                                                           |
+------------------------------+------------------------------------------------------------------------------------------+

|

+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-----------------------------------------------------------------+
|     **Parameter**               |     **Description**                                                                                                                                                                |     **Required?**   |     **Example**                                                 |
+=================================+====================================================================================================================================================================================+=====================+=================================================================+
|     request-parameters          |     Not used. This request is limited to Ansible and Chef only.                                                                                                                    |     No              |     "payload":                                                  |
|                                 |                                                                                                                                                                                    |                     |     \\"configuration-parameters\\": {\\"<CONFIG- PARAMS>\\"}    |
+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+                                                                 |
|     configuration-parameters    |     A set of instance specific configuration parameters should be specified, as required by the Chef cookbook or Ansible playbook.                                                 |     No              |                                                                 |
+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-----------------------------------------------------------------+

ConfigRestore Response
^^^^^^^^^^^^^^^^^^^^^^

**Success:** A successful ConfigRestore returns a success status code 400.

If successful, the return payload contains the ‘upload_config_id’ and values associated with the configuration stored in the APPC DB.  In addition, the configuration is sent to the ONAP Data Router bus which may be received by an external configuration storage system.

**Failure:** A failed ConfigRestore returns a failure code 401 and the failure message.



ConfigScaleOut
--------------

The ConfigScaleOut command is used to apply any actions on a VNF as part of a ScaleOut flow. Actions could include updating the VNF configuration or running a set of other tasks.

The ConfigScaleOut action can have multiple APPC templates associated with it.  APPC retrieves the VfModuleModelName from A&AI (model.model-vers.model-name), which is used as the unique identifier to select the correct APPC template.
APPC creates or updates VNFC records in A&AI for the newly instantiated VM’s.  The orchestration-status of the VNFC’s is set to CONFIGURED.

This action is supported via the Netconf (limited to configuration changes), Chef, and Ansible protocols.

|

+------------------------------+------------------------------------------------------------------------------------------+
|     **Target URL**           |     /restconf /operations/appc-provider-lcm:config-scale-out                             |
+------------------------------+------------------------------------------------------------------------------------------+
|     **Action**               |     ConfigScaleOut                                                                       |
+------------------------------+------------------------------------------------------------------------------------------+
|     **Action-Identifiers**   |     Vnf-id                                                                               |
+------------------------------+------------------------------------------------------------------------------------------+
|     **Payload Parameters**   |     See below                                                                            |
+------------------------------+------------------------------------------------------------------------------------------+
|     **Revision History**     |     New in Beijing                                                                       |
+------------------------------+------------------------------------------------------------------------------------------+

|

+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+---------------------------------------------+
|     **Payload Parameter**       |     **Description**                                                                                                                                              |     **Required?**   |     **Example**                             |
+=================================+==================================================================================================================================================================+=====================+=============================================+
|     request-parameters          |     vnf-host-ip-address: optional if Netconf or other direct interface to the VNF.   If not provided, the vnf-host-ip-address will be obtained from A&AI.        |     No              |      "payload":                             |
|                                 +------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+      "{\\"request-parameters \\":           |
|                                 |     vf-module-id:  used to determine the A&AI VM inventory associated with ConfigScaleOut.                                                                       |     Yes             |      {                                      |
|                                 +------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+      \\"vnf-host-ip-address\\":             |
|                                 |     controller-template-id: optional. This is a unique identifier that will identify the template associated with the ConfigScaleOut.                            |                     |      \\”value\\”,                           |
|                                 |     Will be needed if A&AI does not contain the template identifier.                                                                                             |     No              |      \\”vf-module-id\\”: \\”value\\”,       |
+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+      \\”controller-template-id\\”:          |                                                         
|     configuration-parameters    |     A set of instance specific configuration parameters should be specified. If provided, APP-C replaces variables in the configuration template with the        |     No              |      \\”value\\”                            |
|                                 |     values supplied.                                                                                                                                             |                     |      }                                      |
|                                 |                                                                                                                                                                  |                     |                                             |
|                                 |                                                                                                                                                                  |                     |      \\"configuration-parameters\\":        |
|                                 |                                                                                                                                                                  |                     |        {\\"<CONFIG- PARAMS>\\"}             |
|                                 |                                                                                                                                                                  |                     |                                             |
+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+---------------------------------------------+

ConfigScaleOut Response
^^^^^^^^^^^^^^^^^^^^^^^

**Success:**  

 - A successful ConfigScaleOut returns a success status code 400 when completed.
 
**Failure:** 

 - A failed ConfigScaleOut returns a failure code 401 and the failure message. 
 - If the ConfigScaleOut is successfully performed on the VNF but there is a failure to update A&AI inventory, an intermediate failure message with failure code 501 is returned prior to the final 400 success message.


DetachVolume
------------

The DetachVolume command detaches a cinder volume from a VM via an Openstack command.

Cinder is a Block Storage service for OpenStack. It's designed to present storage resources to end users that can be consumed by the OpenStack Compute Project (Nova). The short description of Cinder is that it virtualizes the management of block storage devices and provides end users with a self-service API to request and consume those resources without requiring any knowledge of where their storage is actually deployed or on what type of device.

NOTE: The command implementation is based on Openstack functionality. For further details, see http://developer.openstack.org/api-ref/compute/.

+--------------------------+----------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:detach-volume     |
+--------------------------+----------------------------------------------------------+
| **Action**               | DetachVolume                                             |
+--------------------------+----------------------------------------------------------+
| **Action-Identifiers**   | vnf-id, vserver-id                                       |
+--------------------------+----------------------------------------------------------+
| **Payload Parameters**   | See table                                                |
+--------------------------+----------------------------------------------------------+
| **Revision History**     | New in Beijing                                           |
+--------------------------+----------------------------------------------------------+

Request Payload Parameters:

+-----------------------------+----------------------------------------------------------------+--------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Payload Parameter**   |     **Description**                                            |     **Required**   |     **Example**                                                                                                                |
+=============================+================================================================+====================+================================================================================================================================+
| volumeId                    |     The UUID of the volume to detach.                          | Yes                |     "volumeId": "a26887c6-c47b-4654-abb5-dfadf7d3f803"                                                                         |
+-----------------------------+----------------------------------------------------------------+--------------------+--------------------------------------------------------------------------------------------------------------------------------+
| vm-id                       |     The self- link URL of the VM.                              | Yes                |     "vm-id": http://135.25.246.162:8774/v2/64af07e991424b8e9e54eca27d5c0d48/servers/b074cd1b-8d53-412e-a102-351cc51ac10a"      |
+-----------------------------+----------------------------------------------------------------+--------------------+--------------------------------------------------------------------------------------------------------------------------------+
| Identity-url                |     The identity URL used to access the resource               | Yes                |     "identity-url": "http://135.25.246.162:5000/v2.0"                                                                          |
+-----------------------------+----------------------------------------------------------------+--------------------+--------------------------------------------------------------------------------------------------------------------------------+

DetachVolume Response:
^^^^^^^^^^^^^^^^^^^^^^

**Success:** A successful DetachVolume returns a success status code 400.

**Failure:** A failed DetachVolume returns a failure code 401 and the failure message. Failure messages can include:

	-  badRequest
	-  unauthorized
	-  forbidden
	-  itemNotFound
	-  conflict


Evacuate
--------

Evacuates a specified VM from its current host to another. After a successful evacuate, a rebuild VM is performed if a snapshot is available (and the VM boots from a snapshot).

The host on which the VM resides needs to be down.

If the target host is not specified in the request, it will be selected by relying on internal rules to evacuate. The Evacuate action will fail if the specified target host is not UP/ENABLED.

After Evacuate, the rebuild VM can be disabled by setting the optional `rebuild-vm` parameter to false.

A successful Evacuate action returns a success response. A failed Evacuate action returns a failure.

**NOTE:** The command implementation is based on Openstack functionality. For further details, see http://developer.openstack.org/api-ref/compute/.

+------------------------------+-------------------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:evacuate                     |
+------------------------------+-------------------------------------------------------------------------+
|     **Action**               |     Evacuate                                                            |
+------------------------------+-------------------------------------------------------------------------+
|     **Action-identifiers**   |     Vnf-id, vserver-id                                                  |
+------------------------------+-------------------------------------------------------------------------+
|     **Payload Parameters**   |     vm-id, identity-url, tenant-id, rebuild-vm, targethost-id           |
+------------------------------+-------------------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                                          |
+------------------------------+-------------------------------------------------------------------------+

|

+----------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+---------------------------------------+
|     **Parameter**    |     **Description**                                                                                                                                                              |     **Required?**   |     **Example**                       |
+======================+==================================================================================================================================================================================+=====================+=======================================+
|     vm-id            |     The unique identifier (UUID) of the resource. For backwards- compatibility, this can be the self-link URL of the VM.                                                         |     Yes             |     "payload":                        |
|                      |                                                                                                                                                                                  |                     |     "{\\"vm-id\\": \\"<VM-ID>         |
|                      |                                                                                                                                                                                  |                     |     \\",                              |
|                      |                                                                                                                                                                                  |                     |     \\"identity-url\\":               |
|                      |                                                                                                                                                                                  |                     |     \\"<IDENTITY-URL>\\",             |
|                      |                                                                                                                                                                                  |                     |     \\"tenant-id\\": \\"<TENANT-ID>   |
|                      |                                                                                                                                                                                  |                     |     \\",                              |
|                      |                                                                                                                                                                                  |                     |     \\"rebuild-vm\\": \\"false\\",    |
|                      |                                                                                                                                                                                  |                     |     \\"targethost-id\\":              |
|                      |                                                                                                                                                                                  |                     |     \\"nodeblade7\\"}"                |
+----------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+                                       |
|     identity-url     |     The identity URL used to access the resource                                                                                                                                 |     Yes             |                                       |
+----------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+                                       |
|     tenant-id        |     The id of the provider tenant that owns the resource                                                                                                                         |     Yes             |                                       |
+----------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+                                       |
|     rebuild- vm      |     A boolean flag indicating if a Rebuild is to be performed after an Evacuate. The default action is to do a Rebuild. It can be switched off by setting the flag to "false".   |     No              |                                       |
+----------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+                                       |
|     targethost- id   |     A target hostname indicating the host the VM is evacuated to. By default, the cloud determines the target host.                                                              |     No              |                                       |
+----------------------+----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+---------------------------------------+

Evacuate Response:
^^^^^^^^^^^^^^^^^^
**Success:** A successful Evacuate returns a success status code 400.
**Failure:** A failed Evacuate returns a failure code 401 and the failure message.



HealthCheck
-----------

This command runs a VNF health check and returns the result.

The VNF level HealthCheck is a check over the entire scope of the VNF. The VNF must be 100% healthy, ready to take requests and provide services, with all VNF required capabilities ready to provide services and with all active and standby resources fully ready with no open MINOR, MAJOR or CRITICAL alarms.


+------------------------------+-----------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:health-check   |
+------------------------------+-----------------------------------------------------------+
|     **Action**               |     HealthCheck                                           |
+------------------------------+-----------------------------------------------------------+
|     **Action-Identifiers**   |     Vnf-id                                                |
+------------------------------+-----------------------------------------------------------+
|     **Payload Parameters**   |     See below                                             |
+------------------------------+-----------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release                             |
+------------------------------+-----------------------------------------------------------+


Request Payload Parameters:

+---------------------+-----------------------------------+---------------------+-------------------------------------+
|     **Parameter**   |     **Description**               |     **Required?**   |     **Example**                     |
+=====================+===================================+=====================+=====================================+
| request-parameters  |     host-ip-address -             |     No              |  "payload":                         |
|                     |     Required only if REST         |                     |  "{\\"request-parameters \\":       |
|                     |     service. This is the ip       |                     |  "{\\"host-ip-address\\":           |
|                     |     address associated with the   |                     |  \\"10.222.22.2\\" }"               |
|                     |     VM running the REST           |                     |                                     |
|                     |     service.                      |                     |                                     |
+---------------------+-----------------------------------+---------------------+-------------------------------------+


HealthCheck Response
^^^^^^^^^^^^^^^^^^^^

**Success:** The HealthCheck returns a 400 success message if the test completes. A JSON payload is returned indicating state (healthy, unhealthy), scope identifier, time-stamp and one or more blocks containing info and fault information.

    Examples::

		{
		  "identifier": "scope represented", 
		  "state": "healthy",
		  "time": "01-01-1000:0000"

		}

		{
		   "identifier": "scope represented", 
		   "state": "unhealthy",
			{[
		   "info": "System threshold exceeded details", 
		   "fault":
			 {
			   "cpuOverall": 0.80,
			   "cpuThreshold": 0.45
			 }
			]},
		   "time": "01-01-1000:0000"
		}

**Failure:** If the VNF is unable to run the HealthCheck. APP-C returns the error code 401 and the http error message.


Lock
----

Use the Lock command to ensure exclusive access during a series of critical LCM commands.

The Lock action will return a successful result if the VNF is not already locked or if it was locked with the same request-id, otherwise the action returns a response with a reject status code.

Lock is a command intended for APPC and does not execute an actual VNF command. Instead, lock will ensure that ONAP is granted exclusive access to the VNF.

When a VNF is locked, any subsequent sequential commands with same request-id will be accepted. Commands associated with other request-ids will be rejected.

APPC locks the target VNF during any VNF command processing. If a lock action is then requested on that VNF, it will be rejected because the VNF was already locked, even though no actual lock command was explicitly invoked.

The lock automatically clears after 900 seconds (15 minutes). This 900 second value can be adjusted in the properties file

+------------------------------+---------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:lock   |
+------------------------------+---------------------------------------------------+
|     **Action**               |     Lock                                          |
+------------------------------+---------------------------------------------------+
|     **Action-Identifier**    |     Vnf-id                                        |
+------------------------------+---------------------------------------------------+
|     **Payload Parameters**   |     None                                          |
+------------------------------+---------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                    |
+------------------------------+---------------------------------------------------+

Lock Response
^^^^^^^^^^^^^

The Lock returns a 400 Success response if the Lock is successfully applied.

The Lock returns a 401 Failure response with the failure message if the Lock is not successful.


Migrate
-------

Migrates a running target VM from its current host to another.

A destination node will be selected by relying on internal rules to migrate. Migrate calls a command in order to perform the operation.

Migrate suspends the guest virtual machine, and moves an image of the guest virtual machine's disk to the destination host physical machine. The guest virtual machine is then resumed on the destination host physical machine and the disk storage that it used on the source host physical machine is freed.

The migrate action will leave the VM in the same Openstack state the VM had been in prior to the migrate action. If a VM was stopped before migration, a separate VM-level restart command would be needed to restart the VM after migration.


**NOTE:** The command implementation is based on Openstack functionality. For further details, see http://developer.openstack.org/api-ref/compute/.


+--------------------------------+-----------------------------------------------------------------------------------------------+
| **Input Block**                | api-ver should be set to 2.00 for current version of Migrate                                  |
+--------------------------------+-----------------------------------------------------------------------------------------------+
|     **Target URL**             |     /restconf/operations/appc-provider-lcm:migrate                                            |
+--------------------------------+-----------------------------------------------------------------------------------------------+
|     **Action**                 |     Migrate                                                                                   |
+--------------------------------+-----------------------------------------------------------------------------------------------+
|     **Action-Identifiers**     |     Vnf-id, vserver-id                                                                        |
+--------------------------------+-----------------------------------------------------------------------------------------------+
|     **Payload Parameters**     |     vm-id, identity-url, tenant-id                                                            |
+--------------------------------+-----------------------------------------------------------------------------------------------+
|     **Revision History**       |     Unchanged in this release.                                                                |
+--------------------------------+-----------------------------------------------------------------------------------------------+

Payload Parameters

+---------------------+-------------------------------------------------------------------------+---------------------+-----------------------------------------------+
| **Parameter**       |     **Description**                                                     |     **Required?**   |     **Example**                               |
+=====================+=========================================================================+=====================+===============================================+
|     vm-id           |     The unique identifier (UUID) of                                     |     Yes             |                                               |
|                     |     the resource. For backwards- compatibility, this can be the self-   |                     |                                               |
|                     |     link URL of the VM.                                                 |                     |     "payload":                                |
|                     |                                                                         |                     |     "{\\"vm-id\\": \\"<VM-ID>\\",             |
|                     |                                                                         |                     |     \\"identity-url\\":                       |
|                     |                                                                         |                     |     \\"<IDENTITY-URL>\\",                     |
+---------------------+-------------------------------------------------------------------------+---------------------+	    \\"tenant-id\\": \\"<TENANT-ID>\\"}"      |
|     identity- url   |     The identity url used to access the resource                        |     Yes             |                                               |
|                     |                                                                         |                     |                                               |
+---------------------+-------------------------------------------------------------------------+---------------------+					              |
|     tenant-id       |     The id of the provider tenant that owns the resource                |     Yes             |                                               |
+---------------------+-------------------------------------------------------------------------+---------------------+-----------------------------------------------+


Migrate Response
^^^^^^^^^^^^^^^^

**Success:** A successful Migrate returns a success status code 400.

**Failure:** A failed Migrate returns a failure code 401 and the failure message.


QuiesceTraffic
--------------

The QuiesceTraffic LCM action gracefully stops the traffic on the VNF (i.e., no service interruption for traffic in progress). All application processes are assumed to be running but no traffic is being processed.

This command is executed using an Ansible playbook or Chef cookbook.
    
Request Structure:

+--------------------------+----------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:quiesce-traffic   |
+--------------------------+----------------------------------------------------------+
| **Action**               | QuiesceTraffic                                           |
+--------------------------+----------------------------------------------------------+
| **Action-identifiers**   | vnf-id                                                   |
+--------------------------+----------------------------------------------------------+
| **Payload Parameters**   | operations-timeout                                       |
+--------------------------+----------------------------------------------------------+
| **Revision History**     | New in Beijing                                           |
+--------------------------+----------------------------------------------------------+

Request Payload Parameters:

+-----------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+------------------------------------------------+
| **Parameter**         |     **Description**                                                                                                                                                                                  |     **Required?**   |     **Example**                                |
+=======================+======================================================================================================================================================================================================+=====================+================================================+
| operations-timeout    |     This is the maximum time in seconds that the command will run before APPC returns a timeout error. If the APPC template has a lower timeout value, the APPC template timeout value is applied.   |     Yes             |     "payload":                                 |
|                       |                                                                                                                                                                                                      |                     |     "{\\"operations-timeout\\": \\"3600\\"}”   |
+-----------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+------------------------------------------------+

QuiesceTraffic Response
^^^^^^^^^^^^^^^^^^^^^^^

The response does not include any payload parameters.

**Success:** A successful quiesce returns a success status code 400 after all traffic has been quiesced.

   If a quiesce command is executed and the traffic has been previously quiesced, it should return a success status.

**Failure:** A failed quiesce returns a failure code 401 and the failure message from the Ansible or Chef server in the response payload block.

    A specific error message is returned if there is a timeout error.

Reboot
-------

The Reboot is used to reboot a VM.

 
There are two types supported: HARD and SOFT. A SOFT reboot attempts a graceful shutdown and restart of the server. A HARD reboot attempts a forced shutdown and restart of the server. The HARD reboot corresponds to the power cycles of the server.

**NOTE:** The command implementation is based on OpenStack functionality.  For further details, see http://developer.openstack.org/api-ref/compute/.

+------------------------------+-----------------------------------------------------------------------------------------------+
| **Input Block**              | api-ver should be set to 2.00 for current version of Reboot                                   |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:reboot                                             |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Action**               |     Reboot                                                                                    |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Action-identifiers**   |     Vnf-id, vserver-id                                                                        |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Payload Parameters**   |     See table below                                                                           |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Revision History**     |     New in R3 release.                                                                        |
+------------------------------+-----------------------------------------------------------------------------------------------+

Payload Parameters

+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+
| **Parameter**   |     **Description**                           | **Required?**   | **Example**                             |
+=================+===============================================+=================+=========================================+
| type            |     The type of reboot.  Values are           | No              |                                         |
|                 |     HARD and SOFT.  If not                    |                 |                                         |
|                 |     specified, SOFT reboot is                 |                 | "payload":                              |
|                 |     performed.                                |                 | "{\\"type\\": \\"HARD\\",               |
|                 |                                               |                 |   \\"vm-id\\": \\"<VM-ID>\\",           |
|                 |                                               |                 | \\"identity-url\\":                     |
|                 |                                               |                 | \\"<IDENTITY-URL>\\"                    |
|                 |                                               |                 | }"                                      | 
+-----------------+-----------------------------------------------+-----------------+                                         |
| vm-id           |     The unique identifier (UUID) of           | Yes             |                                         |
|                 |     the resource. For backwards-              |                 |                                         |
|                 |     compatibility, this can be the self-      |                 |                                         |
|                 |     link URL of the VM.                       |                 |                                         |
|                 |                                               |                 |                                         |
|                 |                                               |                 |                                         |
|                 |                                               |                 |                                         |
|                 |                                               |                 |                                         |
+-----------------+-----------------------------------------------+-----------------+                                         |
| identity-url    |     The identity url used to access the       | Yes             |                                         |
|                 |     resource.                                 |                 |                                         |
+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+

Reboot Response
^^^^^^^^^^^^^^^

**Success:** A successful Rebuild returns a success status code 400.  

**Failure:** A failed Rebuild returns a failure code 401 and the failure message.

Rebuild
-------

Recreates a target VM instance to a known, stable state.

Rebuild calls an OpenStack command immediately and therefore does not expect any prerequisite operations to be performed, such as shutting off a VM.

Rebuild VM uses the snapshot provided by the snapshot-id (if provided).  If not provided, the latest snapshot is used.  If there are no snapshots, it uses the (original) Glance image.

APPC rejects a rebuild request if it determines the VM boots from a Cinder Volume


**NOTE:** The command implementation is based on Openstack functionality. For further details, see http://developer.openstack.org/api-ref/compute/.


+------------------------------+-----------------------------------------------------------------------------------------------+
| **Input Block**              | api-ver should be set to 2.00 for current version of Rebuild                                  |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:rebuild                                            |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Action**               |     Rebuild                                                                                   |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Action-identifiers**   |     Vnf-id, vserver-id                                                                        |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Payload Parameters**   |     See table below                                                                           |
+------------------------------+-----------------------------------------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                                                                |
+------------------------------+-----------------------------------------------------------------------------------------------+


Payload Parameters

+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+
| **Parameter**   |     **Description**                           | **Required?**   | **Example**                             |
+=================+===============================================+=================+=========================================+
| vm-id           |     The unique identifier (UUID) of           | Yes             |                                         |
|                 |     the resource. For backwards-              |                 |                                         |
|                 |     compatibility, this can be the self-      |                 | "payload":                              |
|                 |     link URL of the VM.                       |                 | "{\\"vm-id\\": \\"<VM-ID>               |
|                 |                                               |                 | \\",                                    |
|                 |                                               |                 | \\"identity-url\\":                     |
|                 |                                               |                 | \\"<IDENTITY-URL>\\",                   |
|                 |                                               |                 | \\"tenant-id\\": \\"<TENANT- ID>\\"}"   |
+-----------------+-----------------------------------------------+-----------------+ \\"snapshot-id\\": \\"<SNAPSHOT- ID>\\" |
| identity- url   |     The identity url used to access the       | Yes             | }"                                      |
|                 |     resource.                                 |                 |                                         |
+-----------------+-----------------------------------------------+-----------------+                                         |
| tenant-id       |     The id of the provider tenant that owns   | Yes             |                                         |
|                 |     the resource.                             |                 |                                         |
+-----------------+-----------------------------------------------+-----------------+                                         |
| snapshot-id     |  The snapshot-id of a previously saved image. | No              |                                         |       
+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+

Rebuild Response
^^^^^^^^^^^^^^^^

**Success:** A successful Rebuild returns a success status code 400.  

**Failure:** A failed Rebuild returns a failure code 401 and the failure message.

Restart
-------

Use the Restart command to restart a VM.    

+------------------------------+-----------------------------------------------------------------------------------------------------------------+
|     **Input Block**          |     api-ver should be set to 2.00 for current version of Restart                                                |
+------------------------------+-----------------------------------------------------------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:restart                                                              |
+------------------------------+-----------------------------------------------------------------------------------------------------------------+
|     **Action**               |     Restart                                                                                                     |
+------------------------------+-----------------------------------------------------------------------------------------------------------------+
|     **Action-identifiers**   |     vnf-id and vserver-id are required                                                                          |
+------------------------------+-----------------------------------------------------------------------------------------------------------------+
|     **Payload Parameters**   |     See table below                                                                                             |
+------------------------------+-----------------------------------------------------------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release                                                                                   |
+------------------------------+-----------------------------------------------------------------------------------------------------------------+

Payload Parameters for **VM Restart**

+---------------------+-------------------------------------------------------------------------+---------------------+------------------------------------+
| **Parameter**       |     **Description**                                                     |     **Required?**   |     **Example**                    |
+=====================+=========================================================================+=====================+====================================+
|     vm-id           |     The unique identifier (UUID) of                                     |     Yes             |                                    |
|                     |     the resource. For backwards- compatibility, this can be the self-   |                     |                                    |
|                     |     link URL of the VM                                                  |                     |     "payload":                     |
|                     |                                                                         |                     |     "{\\"vm-id\\": \\"<VM-ID>\\",  |
|                     |                                                                         |                     |     \\"identity-url\\":            |
+---------------------+-------------------------------------------------------------------------+---------------------+     \\"<IDENTITY-URL>\\",          |
|     identity- url   |     The identity url used to access the resource                        |     No              |     \\"tenant-id\\": \\"<TENANT-   |
|                     |                                                                         |                     |     ID>\\"}"                       |
+---------------------+-------------------------------------------------------------------------+---------------------+ 				   |
|     tenant-id       |     The id of the provider tenant that owns the resource                |     No              |                                    |
+---------------------+-------------------------------------------------------------------------+---------------------+------------------------------------+

ResumeTraffic
-------------

The ResumeTraffic LCM action resumes processing traffic on a VNF that has been previously quiesced.

This command is executed using an Ansible playbook or Chef cookbook.

Request Structure: The payload does not have any parameters.

+--------------------------+---------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:resume-traffic   |
+--------------------------+---------------------------------------------------------+
| **Action**               | ResumeTraffic                                           |
+--------------------------+---------------------------------------------------------+
| **Action-identifiers**   | vnf-id                                                  |
+--------------------------+---------------------------------------------------------+
| **Payload Parameters**   |                                                         |
+--------------------------+---------------------------------------------------------+
| **Revision History**     | New in Beijing                                          |
+--------------------------+---------------------------------------------------------+

ResumeTraffic Response
^^^^^^^^^^^^^^^^^^^^^^

**Success:** A successful ResumeTraffic returns a success status code 400 after traffic has been resumed.

If a ResumeTraffic command is executed and the traffic is currently being processed, it should return a success status

**Failure:** A failed ResumeTraffic returns a failure code 401 and the failure message from the Ansible or Chef server in the response payload block.


Snapshot
--------

Creates a snapshot of a VM.

The Snapshot command returns a customized response containing a reference to the newly created snapshot instance if the action is successful.

This command can be applied to a VM in any VNF type. The only restriction is that the particular VNF should be built based on the generic heat stack.

Note: Snapshot is not reliable unless the VM is in a stopped, paused, or quiesced (no traffic being processed) status. It is up to the caller to ensure that the VM is in one of these states.

**NOTE:** The command implementation is based on Openstack functionality. For further details, see http://developer.openstack.org/api-ref/compute/.

+------------------------------+-----------------------------------------------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:snapshot                                                 |
+------------------------------+-----------------------------------------------------------------------------------------------------+
|     **Action**               |     Snapshot                                                                                        |
+------------------------------+-----------------------------------------------------------------------------------------------------+
|     **Action-identifiers**   |     vnf-id, vserver-id                                                                              |
+------------------------------+-----------------------------------------------------------------------------------------------------+
|     **Payload Parameters**   |     vm-id, identity-url, tenant-id                                                                  |
+------------------------------+-----------------------------------------------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                                                                      |
+------------------------------+-----------------------------------------------------------------------------------------------------+

Payload Parameters

+---------------------+-------------------------------------------------------------------------+---------------------+----------------------------------------+
| **Parameter**       |     **Description**                                                     |     **Required?**   |     **Example**                        |
+=====================+=========================================================================+=====================+========================================+
|     vm-id           |     The self-link URL of the VM                                         |     Yes             |                                        |
|                     |                                                                         |                     |     "payload":                         |
|                     |                                                                         |                     |     "{\\"vm-id\\": \\"<VM-ID>\\",      |
|                     |                                                                         |                     |     \\"identity-url\\":                |
|                     |                                                                         |                     |     \\"<IDENTITY-URL>\\",              |
|                     |                                                                         |                     |     \\"tenant-id\\":\\"<TENANT-ID>\\"}"|
+---------------------+-------------------------------------------------------------------------+---------------------+		                               |
|     identity- url   |     The identity url used to access the resource                        |     No              |                                        |
|                     |                                                                         |                     |                                        |
+---------------------+-------------------------------------------------------------------------+---------------------+                                        |
|     tenant-id       |     The id of the provider tenant that owns the resource                |     No              |                                        |
+---------------------+-------------------------------------------------------------------------+---------------------+----------------------------------------+

Snapshot Response
^^^^^^^^^^^^^^^^^

The Snapshot command returns an extended version of the LCM response.

The Snapshot response conforms to the standard response format.


Start
-----

Use the Start command to start a VM that is stopped.

**NOTE:** The command implementation is based on Openstack functionality. For further details, see http://developer.openstack.org/api-ref/compute/.

+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:start                                                                               |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Action**               |     Start                                                                                                                      |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Action-identifiers**   |     vnf-id and vserver-id are required                                                                                         |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Payload Parameters**   |     See table below                                                                                                            |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release                                                                                                  |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+

Payload Parameters

+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+
| **Parameter**   |     **Description**                           | **Required?**   | **Example**                             |
+=================+===============================================+=================+=========================================+
| vm-id           |     The unique identifier (UUID) of           | Yes             |                                         |
|                 |     the resource. For backwards-              |                 | "payload":                              |
|                 |     compatibility, this can be the self-      |                 | "{\\"vm-id\\": \\"<VM-ID>               |
|                 |     link URL of the VM.                       |                 | \\",                                    |
|                 |                                               |                 | \\"identity-url\\":                     |
|                 |                                               |                 | \\"<IDENTITY-URL>\\",                   |
|                 |                                               |                 | \\"tenant-id\\": \\"<TENANT- ID>\\"}"   |
+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+
| identity- url   |     The identity url used to access the       | No              |                                         |
|                 |     resource                                  |                 |                                         |
+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+
| tenant-id       |     The id of the provider tenant that owns   | No              |                                         |
|                 |     the resource                              |                 |                                         |
+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+


StartApplication
----------------

Starts the VNF application, if needed, after a VM is instantiated/configured or after VM start or restart. Supported using Chef cookbook or Ansible playbook only.

A successful StartApplication request returns a success response.

A failed StartApplication action returns a failure response code and the specific failure message in the response block.

+------------------------------+---------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:start-application  |
+------------------------------+---------------------------------------------------------------+
|     **Action**               |     StartApplication                                          |
+------------------------------+---------------------------------------------------------------+
|     **Action-Identifiers**   |     Vnf-id                                                    |
+------------------------------+---------------------------------------------------------------+
|     **Payload Parameters**   |     See table below                                           |
+------------------------------+---------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                                |
+------------------------------+---------------------------------------------------------------+

|

+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-----------------------------------------------------------------+
|     **Payload Parameter**       |     **Description**                                                                                                                                                                |     **Required?**   |     **Example**                                                 |
+=================================+====================================================================================================================================================================================+=====================+=================================================================+
|                                 |                                                                                                                                                                                    |                     |  "payload":                                                     |
|     configuration- parameters   |     A set of instance specific configuration parameters should be specified, as required by the Chef cookbook or Ansible playbook.                                                 |     No              |  "{\\"configuration- parameters\\": {\\"<CONFIG- PARAMS>\\"}    |
+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-----------------------------------------------------------------+

StartApplication Response
^^^^^^^^^^^^^^^^^^^^^^^^^

The StartApplication response returns an indication of success or failure of the request.

Stop
----

Use the Stop command to stop a VM that was running.

**NOTE:** The command implementation is based on Openstack functionality. For further details, see http://developer.openstack.org/api-ref/compute/.

+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:stop                                                                                |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Action**               |     Stop                                                                                                                       |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Action-identifiers**   |     vnf-id and vserver-id are required.                                                                                        |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Payload Parameters**   |     See table below                                                                                                            |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release                                                                                                  |
+------------------------------+--------------------------------------------------------------------------------------------------------------------------------+

Payload Parameters

+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+
| **Parameter**   |     **Description**                           | **Required?**   | **Example**                             |
+=================+===============================================+=================+=========================================+
| vm-id           |     The unique identifier (UUID) of           | Yes             |                                         |
|                 |     the resource. For backwards-              |                 | "payload":                              |
|                 |     compatibility, this can be the self-      |                 | "{\\"vm-id\\": \\"<VM-ID>               |
|                 |     link URL of the VM.                       |                 | \\",                                    |
|                 |                                               |                 | \\"identity-url\\":                     |
|                 |                                               |                 | \\"<IDENTITY-URL>\\",                   |
|                 |                                               |                 | \\"tenant-id\\": \\"<TENANT- ID>\\"}"   |
+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+
| identity- url   |     The identity url used to access the       | No              |                                         |
|                 |     resource                                  |                 |                                         |
+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+
| tenant-id       |     The id of the provider tenant that owns   | No              |                                         |
|                 |     the resource                              |                 |                                         |
+-----------------+-----------------------------------------------+-----------------+-----------------------------------------+


StopApplication
---------------

Stops the VNF application gracefully (not lost traffic), if needed, prior to a Stop command. Supported using Chef cookbook or Ansible playbook only.

A successful StopApplication request returns a success response.

A failed StopApplication action returns a failure response code and the specific failure message in the response block.

+------------------------------+--------------------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:stopapplication   |
+------------------------------+--------------------------------------------------------------+
|     **Action**               |     StopApplication                                          |
+------------------------------+--------------------------------------------------------------+
|     **Action-Identifiers**   |     Vnf-id                                                   |
+------------------------------+--------------------------------------------------------------+
|     **Payload Parameters**   |     See table below                                          |
+------------------------------+--------------------------------------------------------------+
|     **Revision History**     |     Unchanged in this release                                |
+------------------------------+--------------------------------------------------------------+

|

+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-----------------------------------------------------------------+
|     **Payload Parameter**       |     **Description**                                                                                                                                                                |     **Required?**   |     **Example**                                                 |
+=================================+====================================================================================================================================================================================+=====================+=================================================================+
|     configuration- parameters   |     A set of instance specific configuration parameters should be specified, as required by the Chef cookbook or Ansible playbook.                                                 |     No              |     "payload":                                                  |
|                                 |                                                                                                                                                                                    |                     |     \\"configuration- parameters\\": {\\"<CONFIG- PARAMS>\\"}   |
+---------------------------------+------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+---------------------+-----------------------------------------------------------------+


StopApplication Response
^^^^^^^^^^^^^^^^^^^^^^^^

The StopApplication response returns an indication of success or failure of the request.

Sync
----

The Sync action updates the current configuration in the APPC store with the running configuration from the device.

A successful Sync returns a success status.

A failed Sync returns a failure response status and failure messages in the response payload block.

This command can be applied to any VNF type. The only restriction is that the VNF has been onboarded in self-service mode (which requires that the VNF supports a request to return the running configuration).

+------------------------------+---------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:sync   |
+------------------------------+---------------------------------------------------+
|     **Action**               |     Sync                                          |
+------------------------------+---------------------------------------------------+
|     **Action-identifiers**   |     Vnf-id                                        |
+------------------------------+---------------------------------------------------+
|     **Payload Parameters**   |     None                                          |
+------------------------------+---------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                    |
+------------------------------+---------------------------------------------------+

Unlock
------

Run the Unlock command to release the lock on a VNF and allow other clients to perform LCM commands on that VNF.

Unlock is a command intended for APPC and does not execute an actual VNF command. Instead, unlock will release the VNF from the exclusive access held by the specific request-id allowing other requests for the VNF to be accepted.

The Unlock command will result in success if the VNF successfully unlocked or if it was already unlocked, otherwise commands will be rejected.

The Unlock command will only return success if the VNF was locked with same request-id.

The Unlock command returns only one final response with the status of the request processing.

Note: APPC locks the target VNF during any command processing. If an Unlock action is then requested on that VNF with a different request-id, it will be rejected because the VNF is already locked for another process, even though no actual lock command was explicitly invoked.

+------------------------------+-----------------------------------------------------+
|     **Target URL**           |     /restconf/operations/appc-provider-lcm:unlock   |
+------------------------------+-----------------------------------------------------+
|     **Action**               |     Unlock                                          |
+------------------------------+-----------------------------------------------------+
|     **Action-identifiers**   |     Vnf-id                                          |
+------------------------------+-----------------------------------------------------+
|     **Payload Parameters**   |     see table below                                 |
+------------------------------+-----------------------------------------------------+
|     **Revision History**     |     Unchanged in this release.                      |
+------------------------------+-----------------------------------------------------+

|

+---------------------------------+-------------------------------------------------------------------------+---------------------+----------------------------------+
|     **Payload Parameter**       |     **Description**                                                     |     **Required?**   |     **Example**                  |
+=================================+=========================================================================+=====================+==================================+
|     request-id                  |     Request id from the previously submitted request                    |     Yes             |    "request-id": "123456789"     |
+---------------------------------+-------------------------------------------------------------------------+---------------------+----------------------------------+


UpgradeBackout
--------------

The UpgradeBackout LCM action does a backout after an UpgradeSoftware is completed (either successfully or unsuccessfully).

This command is executed using an Ansible playbook or Chef cookbook.

Request Structure: The request payload includes an upgrade identifier.

+--------------------------+----------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:upgrade-backout   |
+--------------------------+----------------------------------------------------------+
| **Action**               | UpgradeBackout                                           |
+--------------------------+----------------------------------------------------------+
| **Action-identifiers**   | vnf-id                                                   |
+--------------------------+----------------------------------------------------------+
| **Payload Parameters**   | existing-software-version, new-software-version          |
+--------------------------+----------------------------------------------------------+
| **Revision History**     | New in Beijing                                           |
+--------------------------+----------------------------------------------------------+

Request Payload Parameters:

+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+
| **Parameter**         |     **Description**                 |     **Required?**   |     **Example**                                                                               |
+=======================+=====================================+=====================+===============================================================================================+
| existing-software-    |     The existing software version   |     Yes             |     "payload":                                                                                |
| version               |     prior to the upgrade            |                     |     "{\\"existing-software-version\\": \\"3.1\\", "{\\"new-software-version\\": \\"3.2\\"}”   |
+-----------------------+-------------------------------------+---------------------+                                                                                               |
| new-software-         |     The new software                |     Yes             |                                                                                               |
| version               |     version after the               |                     |                                                                                               |
|                       |     upgrade                         |                     |                                                                                               |
+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+

UpgradeBackout Response
^^^^^^^^^^^^^^^^^^^^^^^

**Success:** A successful backout returns a success status code 400.

**Failure:** A failed backout returns a failure code 401 and the failure message from the Ansible or Chef server in the response payload block.

UpgradeBackup
-------------

The UpgradeBackup LCM action does a full backup of the VNF data prior to an upgrade. The backup is done on the Ansible or Chef server in a location that is specified in the playbook or cookbook. If there is an existing backup, it is overwritten by the new backup.

This command is executed using an Ansible playbook or Chef cookbook.
  
Request Structure: The payload does not have any parameters required.

+--------------------------+---------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:upgrade-backup   |
+--------------------------+---------------------------------------------------------+
| **Action**               | UpgradeBackup                                           |
+--------------------------+---------------------------------------------------------+
| **Action-identifiers**   | vnf-id                                                  |
+--------------------------+---------------------------------------------------------+
| **Payload Parameters**   | existing-software-version, new-software-version         |
+--------------------------+---------------------------------------------------------+
| **Revision History**     | New in Beijing.                                         |
+--------------------------+---------------------------------------------------------+

Request Payload Parameters:

+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+
| **Parameter**         |     **Description**                 |     **Required?**   |     **Example**                                                                               |
+=======================+=====================================+=====================+===============================================================================================+
| existing-software-    |     The existing software version   |     Yes             |     "payload":                                                                                |
| version               |     prior to the upgrade            |                     |     "{\\"existing-software-version\\": \\"3.1\\", "{\\"new-software-version\\": \\"3.2\\"}”   |
+-----------------------+-------------------------------------+---------------------+                                                                                               |
| new-software-         |     The new software                |     Yes             |                                                                                               |
| version               |     version after the               |                     |                                                                                               |
+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+

UpgradeBackup Response
^^^^^^^^^^^^^^^^^^^^^^

**Success:** A successful backup returns a success status code 400.

**Failure:** A failed backup returns a failure code 401 and the failure message from the Ansible or Chef server in the response payload block.

UpgradePostCheck
----------------

The UpgradePostCheck LCM action checks that the VNF upgrade has been successful completed and all processes are running properly.

This command is executed using an Ansible playbook or Chef cookbook.

Request Structure:

+--------------------------+-------------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:upgrade-post-check   |
+--------------------------+-------------------------------------------------------------+
| **Action**               | UpgradePostCheck                                            |
+--------------------------+-------------------------------------------------------------+
| **Action-identifiers**   | vnf-id                                                      |
+--------------------------+-------------------------------------------------------------+
| **Payload Parameters**   | existing-software-version, new-software-version             |
+--------------------------+-------------------------------------------------------------+
| **Revision History**     | New in Beijing                                              |
+--------------------------+-------------------------------------------------------------+

Request Payload Parameters:

+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+
| **Parameter**         |     **Description**                 |     **Required?**   |     **Example**                                                                               |
+=======================+=====================================+=====================+===============================================================================================+
| existing- software-   |     The existing software version   |     Yes             |     "payload":                                                                                |
|  version              |     prior to the upgrade            |                     |     "{\\"existing-software-version\\": \\"3.1\\", "{\\"new-software-version\\": \\"3.2\\"}”   |
+-----------------------+-------------------------------------+---------------------+                                                                                               |
| new-software-         |     The new software                |     Yes             |                                                                                               |
| version               |     version after the               |                     |                                                                                               |
+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+

UpgradePostCheck Response
^^^^^^^^^^^^^^^^^^^^^^^^^

**Success:** If the UpgradePostCheck run successfully, it returns a success status code 400. The response payload contains the results of the check (Completed or Failed).

Response Payload Parameters:

+---------------+-----------------------------+-------------+------------------------------------------------------------------------------+
| **Parameter** |     **Description**         |**Required?**|     **Example**                                                              |
+===============+=============================+=============+==============================================================================+
| Upgrade-      |     Returns the status      |     Yes     |                                                                              |
| Status        |     of the upgradw          |             |     "payload":                                                               |
|               |     post-check. Indicates   |             |     "{\\"upgrade-status\\": \\"Completed\\"}”                                |
|               |     Completed or Failed     |             |     "payload": "{\\"upgrade-status\\":                                       |
|               |                             |             |     \\"Failed\\",\\"message\\": \\"Version 3.2 is not running properly\\" }” |
+---------------+-----------------------------+-------------+                                                                              |
| Message       |     If Not Available,       |             |                                                                              |
|               |     message contains        |             |                                                                              |
|               |     explanation.            |             |                                                                              |
+---------------+-----------------------------+-------------+------------------------------------------------------------------------------+

**Failure:** If the UpgradePostCheck could not be run, it returns a failure code 401 and the failure message from the Ansible or Chef server in the response payload block.

UpgradePreCheck
---------------

The UpgradePreCheck LCM action checks that the VNF has the correct software version needed for a software upgrade. This command can be executed on a running VNF (i.e. processing traffic).

This command is executed using an Ansible playbook or Chef cookbook.

Request Structure:

+--------------------------+------------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:upgrade-pre-check   |
+--------------------------+------------------------------------------------------------+
| **Action**               | UpgradePreCheck                                            |
+--------------------------+------------------------------------------------------------+
| **Action-identifiers**   | vnf-id                                                     |
+--------------------------+------------------------------------------------------------+
| **Payload Parameters**   | existing-software-version, new-software-version            |
+--------------------------+------------------------------------------------------------+
| **Revision History**     | New in Beijing                                             |
+--------------------------+------------------------------------------------------------+

Request Payload Parameters:

+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+
| **Parameter**         |     **Description**                 |     **Required?**   |     **Example**                                                                               |
+=======================+=====================================+=====================+===============================================================================================+
| existing-software-    |     The existing software version   |     Yes             |     "payload":                                                                                |
| version               |     prior to the upgrade            |                     |     "{\\"existing-software-version\\": \\"3.1\\", "{\\"new-software-version\\": \\"3.2\\"}”   |
+-----------------------+-------------------------------------+---------------------+                                                                                               |
| new-software-         |     The new software                |     Yes             |                                                                                               |
| version               |     version after the               |                     |                                                                                               |
|                       |     upgrade                         |                     |                                                                                               |
+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+

UpgradePreCheck Response
^^^^^^^^^^^^^^^^^^^^^^^^

**Success:** If the UpgradePreCheck runs successfully, it returns a success status code 400. The response payload contains the results of the check (Available or Not Available for upgrade).

Response Payload Parameters:

+-----------------+---------------------------+---------------------+----------------------------------------------------------------------------------------------------------------------------------+
| **Parameter**   |     **Description**       |     **Required?**   |     **Example**                                                                                                                  |
+=================+===========================+=====================+==================================================================================================================================+
| upgrade-status  |     Returns the status    |     Yes             |                                                                                                                                  |
|                 |     of the upgrade pre-   |                     |     "payload":                                                                                                                   |
|                 |     check. Indicates      |                     |     "{\\"upgrade-status\\": \\"Available\\"}”                                                                                    |
|                 |     Available or Not      |                     |                                                                                                                                  |
|                 |     Available             |                     |     "payload":                                                                                                                   |
|                 |                           |                     |     "{\\"upgrade-status\\": \\"Not Available\\",\\"message\\": \\"Current software version 2.9 cannot be upgraded to 3.1\\" }”   |
+-----------------+---------------------------+---------------------+                                                                                                                                  |
|     message     |     If Not Available,     |                     |                                                                                                                                  |
|                 |     message contains      |                     |                                                                                                                                  |
|                 |     explanation.          |                     |                                                                                                                                  |
+-----------------+---------------------------+---------------------+----------------------------------------------------------------------------------------------------------------------------------+

**Failure:** If an UpgradePreCheck fails to run, it returns a failure code 401 and the failure message from the Ansible or Chef server in the response payload block.

UpgradeSoftware
---------------

The UpgradeSoftware LCM action upgrades the target VNF to a new version. It is expected that the VNF is in a quiesced status (not processing traffic).

This command is executed using an Ansible playbook or Chef cookbook.
  
Request Structure: The request payload includes the new-software-version.

+--------------------------+-----------------------------------------------------------+
| **Target URL**           | /restconf/operations/appc-provider-lcm:upgrade-software   |
+--------------------------+-----------------------------------------------------------+
| **Action**               | UpgradeSoftware                                           |
+--------------------------+-----------------------------------------------------------+
| **Action-identifiers**   | vnf-id                                                    |
+--------------------------+-----------------------------------------------------------+
| **Payload Parameters**   | existing-software-version, new-software-version           |
+--------------------------+-----------------------------------------------------------+
| **Revision History**     | New in Beijing                                            |
+--------------------------+-----------------------------------------------------------+

 Request Payload Parameters:

+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+
| **Parameter**         |     **Description**                 |     **Required?**   |     **Example**                                                                               |
+=======================+=====================================+=====================+===============================================================================================+
| existing- software-   |     The existing software version   |     Yes             |     "payload":                                                                                |
| version               |      prior to the upgrade           |                     |     "{\\"existing-software-version\\": \\"3.1\\", "{\\"new-software-version\\": \\"3.2\\"}”   |
+-----------------------+-------------------------------------+---------------------+                                                                                               |
| new-software          |     The new software                |     Yes             |                                                                                               |
| version               |     version after the               |                     |                                                                                               |
|                       |     upgrade                         |                     |                                                                                               |
+-----------------------+-------------------------------------+---------------------+-----------------------------------------------------------------------------------------------+

UpgradeSoftware Response
^^^^^^^^^^^^^^^^^^^^^^^^

**Success:** A successful upgrade returns a success status code 400.

If an UpgradeSoftware command is executed and the software has been previously upgraded to this version, it should return a success status.

**Failure:** A failed upgrade returns a failure code 401 and the failure message from the Ansible or Chef server in the response payload block. A failure does not assume that the software upgrade has been rolled back.

Notes regarding the Upgrade commands
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Ansible playbooks / Chef cookbooks:

-  All Ansible playbooks/cookbooks for the Upgrade commands will be
   stored in the same directory on the server. The directory name will
   be of the format: 
   
        {existing-software-version_new-software-version}. 
		
		The path to the directory is the same for all upgrades (for example: vnf-type/softwareupgrade).

-  The playbooks for upgrades should use a standard naming convention
   (for example: SoftwareUpgrade_{existing-software-version_new-software-version}).

APPC template: The APPC templates for the Upgrade commands can be common across upgrades for the vnf-type if the path and filenames are standardized.

-  The template will contain the directory path/playbook name which
   would be parameterized.

    {vnf-type}/UpgradeSoftware/${existing_software_version}_${new-software-version}/
    SoftwareUpgrade_{existing-software-version_new-software-version}.


