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

.. _appc_client_library:

==================================================
Application Controller (APPC) Client Library Guide
==================================================


Introduction
============

Target Audience
---------------

This document is for an advanced technical audience, which includes engineers and technicians. Document revisions occur with the release of new software versions.

Related Documentation
---------------------

For additional information, see

        :ref:`appc_api_guide`


Client Library Background
=========================

This guide discusses the Application Controller (APPC) Client Library and how to use it.

About the Client Library
------------------------

The APPC client library provides consumers of APPC capabilities with a strongly-typed Java interface and encapsulates the actual interaction with the APPC component over an asynchronous messaging channel such as UEB.

Consumer Logic
--------------

The client application that consumes APPC’s capability for VNF lifecycle management (the APPC client library) can be implemented against the lightweight and strongly-typed Java API exposed by the APPC client library. The library does not try to impose architectural constraints upon clients, but instead provides support for different options and styles of API. It is the responsibility of the client application to select the most suitable paradigm to use; for example, a client may choose to use blocking calls as opposed to asynchronous notifications.

VNF Lifecycle Management API
----------------------------

The API represents a relatively thin layer that consists mainly of business interfaces with strongly-typed APIs and a data object model created for the convenience of the consumer application. The original YANG schema used by the APPC component and the  underlying MD-SAL layer on the server-side generates these artifacts.

APP-C Client Library Flow
-------------------------

    |image0|

Asynchronous Flow
^^^^^^^^^^^^^^^^^

-  The APPC Client Library is called using an asynchronous API using a full command object, which is mapped to a JSON representation.
-  The APPC client calls the UEB client and sends the JSON command to a configured topic.
-  The APPC client pulls response messages from the configured topic.
-  On receiving the response for the command, the APPC client runs the relevant callback method of the consumer ResponseHandler.

Synchronous Flow
^^^^^^^^^^^^^^^^

-  The APPC Client Library is called using a synchronous API using a full command object, which is mapped to a JSON representation.
-  The APPC client calls the UEB client and sends the JSON command to a configured topic.
-  The APPC client pulls response messages from the configured topic.
-  On receiving the **final** response for the command, the APPC client returns the response object with a final status.

Client Library Usage
====================

Jar Files
---------

The Java application that runs the APPC client kit uses the following jar files:

    -  com.att.appc.client.client-kit
    -  com.att.appc.client.client-lib

The client library JAR files are located in the repository under ``com\\att\\appc\\client``.

Initialization
--------------

Initialize the client by calling the following method:

``AppcClientServiceFactoryProvider.getFactory(AppcLifeCycleManagerServiceFactory.class).createLifeCycleManagerStateful()``

Specify the following configuration properties as method parameters:

    -  "topic.read"
    -  "topic.read.timeout"
    -  "topic.write"
    -  "client.key"
    -  "client.secret"
    -  "client.name"
    -  "client.name.id"
    -  "poolMembers"
    -  “client.response.timeout”
    -  “client.graceful.shutdown.timeout”

Shutdown
--------

Shutdown the client by calling the following method:

``void shutdownLifeCycleManager(boolean isForceShutdown)``

If the ``isForceShutdown`` flag is set to false, the client shuts down as soon as all responses for pending requests are received, or upon configurable timeout. (``client.graceful.shutdown.timeout``).

If the ``isForceShutdown`` flag is set to true, the client shuts down immediately.

Invoking LCM Commands
---------------------

Invoke the LCM commands by:

    -  Creating input objects, such as AuditInput, LiveUpgradeInput, with relevant command information.
    -  Executing commands asynchronously, for example:

``void liveUpgrade(LiveUpgradeInput liveUpgradeInput, ResponseHandler<LiveUpgradeOutput> listener) throws AppcClientException;)``

In this case, client should implement the ResponseHandler<T> interface.

    -  Executing commands synchronously, for example:

``LiveUpgradeOutput liveUpgrade(LiveUpgradeInput liveUpgradeInput) throws AppcClientException;)``


Client API
==========

After initializing the client, a returned Object of type LifeCycleManagerStateful defines all the Life Cycle Management APIs
 supported by APPC.

The interface contains two definitions for each RPC: one for Asynchronous call mode, and one for Synchronous.

In Asynchronous mode, client consumer should provide a callback function of type:

    ``ResponseHandler<RPC-NAMEOutput>``

where ``RPC-NAME`` is the command name, such as Audit or Snapshot.

There may be multiple calls to the ResponseHandler for each response returned by APPC. For example, first 100 ‘accept’ is returned, then 400 ‘success’.

LifeCycleManagerStateful Interface
----------------------------------

Generated from the APPC Yang model, this interface defines the services and request/response requirements for the ONAP APPC component. For example, for LCM Command Audit, the following is defined:

``@RPC(name="audit", outputType=AuditOutput.class)``

``AuditOutput audit(AuditInput auditInput) throws AppcClientException;``

For a Synchronous call to Audit, the consumer thread is blocked until a response is received or a timeout exception is thrown.

``@RPC(name="audit", outputType=AuditOutput.class)``

``void audit(AuditInput auditInput, ResponseHandler<AuditOutput> listener) throws AppcClientException;``

For an Asynchronous call to Audit, a callback should be provided so that when a response is received the listener is called.

API documentation
-----------------

The API documentation is also available as a swagger page generated from files at /client-kit/target/resources.

appc-provider-lcm
-----------------

This defines the services and request/response requirements for the APPC component.

Methods
-------

The methods should match the actions described in the LCM API Guide. For each method:

**Consumes**

This API call consumes the following media types using the**Content-Type** request header:

    -  ``application/json``

**Request body**

The request body is the action name followed by Input (e.g., AuditInput)

**Return type**

The return type is the action name followed by Output (e.g., OutputInput)

**Produces**

This API call produces the following media types according to the **Accept** request header; the **Content-Type** response header conveys the media type.

    -  ``application/json``

**Responses**

200 Successful operation

401 Unauthorized

500 Internal server error

.. |image0| image:: image2.png
   :width: 5.60495in
   :height: 4.55272in
