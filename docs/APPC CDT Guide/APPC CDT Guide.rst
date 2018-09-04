.. ============LICENSE_START==========================================
.. ===================================================================
.. Copyright © 2018 AT&T Intellectual Property. All rights reserved.
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

.. _appc_cdt_guide:

===================
APPC CDT User Guide
===================

Introduction
============

This document is the APPC Controller Design Tool (CDT) User Guide for self-service
onboarding of VNF’s. VNF owners can create templates and other artifacts
for APPC Configure command (used to apply a post-instantiation
configuration) as well as other life cycle commands.

A complete list of all APPC supported commands is contained in the
APPC LCM API Guide available on onap.readthedocs.io.

Overview of APPC Support for VNF Configuration and Lifecycle Commands
======================================================================

APPC is an ONAP component that performs functions to manage the
lifecycle of VNF’s and their component. The picture below summarizes the
APP-C design intent.

|image0|

Some lifecycle commands are supported on any VNF type, such as commands
executed using OpenStack or for generic REST calls such as for
HealthCheck. Other commands require models called templates to be
created in the APPC Controller Design Tool(CDT) by the VNF owner.

Templates are needed for lifecycle commands such as for
post-instantiation configuration and for passing payloads to a Chef or
Ansible server. Templates contain static and instance-specific
parameters in a structured language (currently limited to xml and JSON).
The APPC CDT allows a VNF technology owner to identify the
instance specific parameters with a unique name.

At run time, the instance specific parameter values are populated in the
template to replace the parameter name.

|image1|

|image2|

|image3|

Overview of the Onboarding Process
==================================

Pre-Requisites for Onboarding:
------------------------------

-  The VNF must support the below listed device protocols along with OpenStack for VM-level commands:
   - Netconf, 
   - Chef, 
   - Ansible,
   - REST  - The REST protocol is used for REST interfaces to a VNF. Currently, the only action that can use REST is HealthCheck.
   - RESTCONF - The RESTCONF protocal is used only for VNFs that support Netconf and are able to be mounted in OpenDayLight (ODL). Use the protocal NETCONF-XML if the VNF is not ODL mountable.
   
-  In order to build the templates needed for lifecycle commands, the
   VNF owner will be asked to upload either an xml file (for Netconf) or
   a JSON file (for Chef or Ansible). This file contains the parameter
   values in a valid schema that would be sent to either the VNF or the
   Chef/Ansible server to execute the action. For more detail on
   Netconf, Chef, or Ansible, see the ONAP vendor guidelines at:

   https://wiki.onap.org/pages/viewpage.action?pageId=1015852&preview=/1015849/1017888/VNF%20Management%20Requirements%20for%20OpenECOMP.pdf

-  The VNF related key identifiers (vnf-type, vnfc-type,
   vnfc-function-code) that will be stored in A&AI have been defined.

Onboarding Process Steps:
-------------------------

1. Use the APPC CDT GUI to populate **reference data**
   describing the VNF and action to be onboarded.

    -  Select the VNF, action, and protocol to be on-boarded.

    -  Describe the VM/VNFC components that comprise the VNF’s. APPC
       will use this VM/VNFC data to update A&AI when configuring the VNF
       at run time.

2. Create a **template** from a “golden” configuration file.

	-  Upload a “golden configuration” file (described later) into the APPC CDT GUI.

	-  Manually edit the configuration file to associate parameter names
	   with instance-specific values you wish to parameterize (e.g., IP addresses).

	-  This creates a template file, which will be used at run-time to
	   create the payload that APPC sends to the VNF or to Ansible/Chef.

	-  Alternative: If the golden configuration changes, rather than
	   manually re-creating an updated template, you can *automatically*
	   create an updated template by **Merging** a current parameter
	   name-value pairs file with the new configuration file. APPC will
	   scan the new configuration file, and automatically replace values
	   with parameter names.

3. Create a **parameter definition** file describing instance-specific
   parameters in the template.

	-  Once you have a template, use the **Synchronize Template Parameters** button to
	   automatically create/update a parameter definition file (and a
	   parameter name-value pair file) based on the template.

	-  You can then populate/update the fields describing each parameter.

	-  If the parameters will be populated from external systems (such as INSTAR), you can upload
	   a “key file” that automatically populates key fields used to retrieve
	   data from the external system.

	-  If the parameters will be populated from A&AI, you can select the
	   rules and key fields used to retrieve data from A&AI.

	-  The parameter definition file will be used at run time to
	   automatically obtain parameter values from external system, A&AI, or a user
	   input spreadsheet for insertion into the template.

4. Create a **parameter name-value pair file** for those parameters.

	-  Once you have a template, use the **Synchronize Template Parameters** button to
	   automatically create a parameter name-value pair file (and a
	   parameter definition file) based on the template.

	-  The parameter name-value file serves as a guide for populating
	   instance-specific data into the user input spreadsheet used at run
	   time. The parameter name-value file can also be used to automatically
	   create a template via the **Merge** function as described in step 2.
	   
	- You can also use the **Synchronize With Name Values** button to update the parameter definitions to match an existing parameter name-values pair file.

5. **Test** the template in your test environment using the **TEST** function of APPC CDT

	-  Use the **Save All to APP-C** button in the CDT GUI to save the
	   artifacts for your VNF to APPC.  This makes the current version of artifacts available to both the APPC CDT and APPC Run Time. 

	-  Prepare a “user input” excel file on your PC and upload it to the APPC CDT.

	-  **Execute** the onboarded action on the VNF. View test progress and test results. . 
	
The screen shots in following sections illustrate how to use the APPC CDT GUI for each step.

Artifacts used for Onboarding:
------------------------------

For a given VNF, each action must be on-boarded separately. Actions can
be on-boarded in any order; it is not required that “Configure” be the first action onboarded.

You will create 1 Reference Data file for each VNF, and a set of up to 3
files for each action you are onboarding:

	1. Template
	2. Parameter definition file (pd\_configure)
	3. Parameter name-value pair file (param\_configure)

For example, onboarding the “vABC” VNF for 2 actions (Configure and
ConfigModify) may result in these 7 files:

	1. reference\_AllAction\_vABC\_0.0.1V
	2. template\_Configure\_vABC\_0.0.1V
	3. pd\_Configure\_vABC\_0.0.1V
	4. param\_Configure\_vABC\_0.0.1V
	5. template\_ConfigModify\_vABC\_0.0.1V
	6. pd\_ConfigModify\_vABC\_0.0.1V
	7. param\_ConfigModify\_vABC\_0.0.1V

A **Template** is required for the Ansible, Chef and Netconf protocols.

The **Parameter Definition** and **Parameter Name-Value Pair** artifacts
are typically used with the Configure and ConfigModify templates and are
optional for templates of other actions.

OpenStack and REST protocols do not use a template or parameter
definitions or name-value pairs.

Using the APPC Controller Design Tool for VNF Onboarding
========================================================

Go to the APPC CDT GUI in the test environment using a Firefox browser.

http://<server>:<port>

- Where:  
	- <server> = The server IP or host where CDT is deployed.
	- <port> = By default 8080 for a HEAT deployed CDT or 30289 for an OOM deplyed CDT.

|image4|

Clicking on “About Us” will display the current software version and who to contact for support. The contact information is configurable. What is display in diagram is just an example.

|image5|

Choose “My VNF’s”.

If you have not used APPC CDT previously, you will be asked to 
enter your user id. Your work will be stored under this user id. There
is no password required at this time.

Note: If multiple self-service users will be working on a set of VNF’s,
it is suggested that you enter a group\_name rather than your user\_id.
This group name would be used by all users in the group so that all
users can view/edit the same set of artifacts.

If you have previously used APPC CDT, you user id will
automatically be selected.

|image6|

The “My VNFs” GUI displays a list of the vnf-type/vnfc-types which are
stored under your userid in the APPC database. You can choose either
“Create New VNF” or “View/Edit” for one of your existing VNF’s.

|image7|

If you have not created any VNF artifacts in the current release of the
APPC CDT, you will see a screen like this; click “Create new
VNF” to begin.

VNF artifacts created using earlier versions of the APPC CDT
can be uploaded and then edited/saved, as shown on later screens. You
should not have to re-create these VNF artifacts.

|image8|

If you choose to create a new VNF, you will see a pop-up box like this.

|image9|

Enter the VNF Type (and optional VNFC Type) and click next. (The optional VNFC check box is explained later)

Alternatively, you can leave the VNF type blank and choose “PROCEED
ANYWAY” if you want to proceed to the Reference Data screen where you
can populate the VNF reference data by uploading an existing Reference
File or by manually entering it.

You must populate the VNF field if uploading the existing file does not
populate it.

Populate reference data describing the VNF and action to be onboarded
---------------------------------------------------------------------

|image10|

|image11|

|image12|

|image13|

|image14|

Note 1: When downloading your work to APPC; the system will download
only the artifacts that have been updated in the current session. You
may not see all 4 artifacts unless you visit/edit the reference,
template, parameter and parameter definition screens.

Note 2: When downloading files, the system will display a pop-up window
for each file, but the windows are all placed on top of each other. You
can drag the pop-up windows if you want to see them all at the same
time.

|



|

When using the Mozilla Firefox browser, selecting “Download to PC will display a dialog box giving you a choice of opening or saving the files, and an option to “Do this automatically for files like this for now on”. Choosing “save” and checking this option is a convenient way to easily save multiple downloaded artifacts from APP-C to your PC

|image15a|

Note regarding VNFC Type
~~~~~~~~~~~~~~~~~~~~~~~~

There are a limited number of VNF’s that are identified by both VNF type and VNFC type. When adding a new VNF of this kind to APP-C, enter the VNF type and check the VNFC box in the pop-up window, and choose NEXT.

Alternatively, you can leave the VNF type blank and choose “PROCEED ANYWAY” if you want to proceed to the Reference Data screen where you can populate the VNF reference data by uploading an existing Reference File or by manually entering it. 

|image15b|

On the subsequent Reference screen, you must add the VNFC type(s).

|image15c|

Enter the new VNFC type and click ADD to add it to a drop-down list of VNFC types for this VNF.  Repeat for each VNFC type you wish to add.

|image15d|

Choose the desired VNFC Type from the drop-down list of VNFC types.

|image15e|

In the VNFC section, you must re-enter the VNFC type to match what you previously selected.

|image15f|

Populate OpenStack actions for a VM
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

You can also onboard OpenStack commands for the VM level components of
the VNF

Under ‘Action’, select “OpenStack Actions” and then under ‘Protocol’
select “OpenStack”.

You must populate the ‘VNF type’ if it is not already populated.

|image16|

Next, upload an excel file of VM capabilities for your VNF. The excel
must list OpenStack actions in the first column, and then have a column
for each VM type (i.e., VNFC Function Code) showing which actions are
applicable for that VM type, as shown Excel sample below:

|imageA|

APPC will import the data from the excel and display the results.

|image17|

The Template and Parameter Definition tabs do not apply to OpenStack
commands.

**REFERENCE DATA SCREEN HELP**

+--------------------------+------------------------------------------------------------------------------------------------------------------+
| **Field/Object**         | **Description**                                                                                                  |
+==========================+==================================================================================================================+
|                                                     **VNF Reference Data**                                                                  |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| Action                   | The action to be executed on the VNF, e.g., “CONFIGURE” (see table below).                                       |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| VNF Type                 | The name of the VNF, e.g. vDBE.                                                                                  |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| VNFC Type                | NA when describing a VNF; When describing a VNFC, enter the VNFC name e.g.,MSC, SSC, MMC, etc.                   |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| Device Protocol          | Choose desired protocol e.g., NETCONF-XML (see table below).                                                     |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| Template                 | Will there be a template created for this VNF and action? Yes/No.                                                |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| User Name                | Enter the user name used to configure the VNF e.g., “admin” or “root”.                                           |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| Port Number              | Enter the port number used to configure the VNF, e.g., 22.                                                       |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| Context URL              | Enter the context portion of the REST URL (Currently used only for the HealthCheck action with REST protocol).   |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
|                                                      **VNFC information**                                                                   |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| VNFC Type                | Enter the VNFC name e.g. MSC, SSC, MMC, etc.                                                                     |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| VNFC Function Code       | Enter the standard 3 character value for the VNFC.                                                               |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| IP Address V4 OAM VIP    | Select Y to store the O&AM VIP address with the VNFC record; otherwise select N.                                 |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| Group Notation Type      | Select the naming scheme for VNFC/VM instances (first-vnfc-name, fixed value, relative value)                    |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| Group Notation Value     | For first-vnfc-name type, enter text such as “pair” or “group”.                                                  |
|                          |                                                                                                                  |
|                          | For fixed value type, enter any alpha-numeric text “1”, “test” etc.                                              |
|                          |                                                                                                                  |
|                          | For relative value type, enter a number “1”, “2”, “4”, etc                                                       |
+--------------------------+------------------------------------------------------------------------------------------------------------------+
| Number of VM’s           | Enter the # of VM’s for this VNFC.                                                                               |
+--------------------------+------------------------------------------------------------------------------------------------------------------+

|

This table shows which actions and protocols are currently available for
on-boarding with the Beijing release.

+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
|  **Action**                            |**Netconf-XML**| **Ansible**   | **Chef**   | **REST**   | **OpenStack**  |**Protocol is**|
|                                        |**Restconf**   |               |            |            | **(VM Level)** |**Not**        |
|                                        |               |               |            |            |                |**Applicable** |
+========================================+===============+===============+============+============+================+===============+
| **ActionStatus**                       |               |               |            |            |                |     NA        |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **AttachVolume**                       |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Audit**                              | YES           | YES           | YES        | YES        |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **CheckLock**                          |               |               |            |            |                |     NA        |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Configure**                          | YES           | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Config Modify**                      | YES           | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Config Backup**                      |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Config Restore**                     |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **ConfigScaleOut**                     | YES           | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **DetachVolume**                       |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **DistributeTraffic**                  |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Evacuate**                           |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **HealthCheck**                        |               | YES           | YES        | YES        |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Lock**                               |               |               |            |            |                |      NA       |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Migrate**                            |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **QuiesceTraffic**                     |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Rebuild**                            |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Restart**                            |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **ResumeTraffic**                      |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Snapshot**                           |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Start**                              |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Start Application**                  |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Stop**                               |               |               |            |            | YES            |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Stop Application**                   |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Sync**                               | YES           | YES           | YES        |  YES       |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **Unlock**                             |               |               |            |            |                |       NA      |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **UpgradeBackout**                     |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **UpgradeBackup**                      |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **UpgradePostCheck**                   |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **UpgradePreCheck**                    |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
| **UpgradeSoftware**                    |               | YES           | YES        |            |                |               |
+----------------------------------------+---------------+---------------+------------+------------+----------------+---------------+
 


Create a template from a “golden” configuration file
----------------------------------------------------

There are several ways to create a template in APPC CDT:

-  Start from golden instance config file; manually add parameters
   (described in this section)

-  Start with a template file, manually add more parameters. (described
   in section Synchronizing a Template)

-  Start with config file; create updated template by **merging**
   name-value pairs. (described in Create a file containing name-value pairs for parameters section)

Start with a working configuration for a “golden instance” of the VNF
(xml if Netconf) or the payload to be downloaded to the Chef or Ansible
server (JSON).

Open the XML or JSON file in Notepad ++ and verify that the format is
schema compliant. If the xml file is for a post-instantiation
configuration, then modify the config to include only statements that
are to be added (merged) with any configuration that is on the VNF
instance after instantiation. For example, remove statements that might
change root passwords, etc.

Optionally, add Velocity statements to the file, if desired, to handle
special constructs such as variable lists, template defined constants,
conditional statements, etc.

Here are links with more information about the Velocity java-based
template engine:

    http://velocity.apache.org/engine/2.0/vtl-reference.html

    http://velocity.apache.org/engine/2.0/user-guide.html

This screen shows a sample Golden Configuration file that has been
uploaded to APP-C CDT.

|image18|

Next, designate instance-specific values as parameters, using this
procedure:

    1) Highlight the instance-specific value (such as “node0 )  with the cursor and then type “CTRL” and “4”

    |image19|

    2) Type the name you want to use for this parameter into the pop-up window and click SUBMIT

    |image20|

    3) The system will display your parameter name after the value you highlighted

    |image21|

    4) Repeat for each instance-specific value that you wish to turn into a parameter.

*Summary of editing commands:*

 - CTRL+4 to add a parameter (also saves previous unsaved parameter)
 - CTRL+S to save a parameter
 - CTRL+Z to undo the last edit

Notes on naming Parameters:

-  Choose meaningful, unique parameter names for each parameter. If the
   same parameter value appears in multiple places in the config, the
   parameter name which is assigned to the first instance will be
   automatically assigned to all instances. However, you may choose a
   different parameter name for each instance of the parameter value
   (except when using the MERGE function).

-  Use only dash (-) or underline (\_) as separators between words in
   the name.

-  The name should not contain spaces or any other special characters.

-  Do not use parameter names which are sub-strings of other parameter
   names. For example, don’t use field1 and field12 as parameter names.

In the template, the first instance of a parameter will be highlighted in green and subsequent instances of the same parameter will be highlighted in orange. 

Synchronizing a Template
~~~~~~~~~~~~~~~~~~~~~~~~

Once you have named all the parameters (this example shows 3
parameters), click the “SYNCHRONIZE TEMPLATE PARAMETERS”  button to automatically create a
parameter definition file and a parameter name-value file. The next
sections describe these files.

It may take a few seconds for the system to synchronize; when it is
complete, you will be taken to the Parameter Definition screen.

Remember to use the SAVE and/or DOWNLOAD buttons on the Reference Data
screen to preserve your work.

|image23|

Modifying an Existing Template
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In addition to creating new templates, you can also modify an existing
template by adding or removing parameter names.

To add a new parameter name, follow the steps in the Create a template from a "golden" configuration file section above.
SYNCHRONIZE TEMPLATE PARAMETERS  to add the new parameter to the name/value and parameter
definition GUI.

To remove an existing parameter name, remove the parameter name (i.e.,
${name}) using the backspace key and replace with the static value. Then
SYNCHRONIZE TEMPLATE PARAMETERS  to remove the parameter from the name/value and parameter
definition GUI.

If the available template has parameter names (as opposed to the golden configuration/ base config typically shared by VNF owners), you can upload that template and manually add the braces around the parameter names.  Then click on SYNCHRONIZE TEMPLATE PARAMETERS to generate the PD file with source as Manual.

Remember to use the SAVE and/or DOWNLOAD buttons on the Reference Data
screen to preserve your work.

Create a parameter definition file describing instance-specific parameters in the template 
------------------------------------------------------------------------------------------

Clicking the “SYNCHRONIZE TEMPLATE PARAMETERS” button after creating a template will automatically create/update a parameter definition file for
that template (and a parameter name-value file described in the next
section). Alternatively, you can upload an existing parameter definition
file from your PC.

You can view or edit the definition fields for each parameter via the
Parameter Definition screen. Note that any edits to the parameter names
would be overwritten by a subsequent SYNCHRONIZE TEMPLATE PARAMETERS.

|image24|

Select a Source for each parameter
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

There are three choices for the source:

1. **External Systems**. APPC will automatically obtain parameter values from
   an external system (typically IP addresses for VNF’s). First, obtain a
   “key file” for your VNF. Then use the
   “Upload Key File” button on the Parameter Definition screen. APPC
   will automatically populate key names and values used to retrieve data
   from an external system.


2. **A&AI**. APPC will automatically obtain parameter values from
   A&AI (typically VNF/VNFC/VM identifiers). After selecting “A&AI”,
   select a rule type and APPC will automatically populate the key
   names and values. For rule types that include a list, populate the
   ‘Filter By Field’ and ‘Filter By Value’.

    |image26|

3. **Manual**. APPC will use a manually-created excel to populate
   parameter values. Later section describes this User Input Spreadsheet.

Remember to use the SAVE and/or DOWNLOAD buttons on the Reference Data
screen to preserve your work.

Create a file containing name-value pairs for parameters
--------------------------------------------------------

Clicking the “SYNCHRONIZE TEMPLATE PARAMETERS” button after creating a template (see section
Synchronizing a Template) will automatically create/update a parameter name-value pair file
for that template (and a parameter definition file described in the
previous section).

Navigate to the Template tab and “Param Values” subtab to view/edit
parameter name-value pairs.

If you make any edits, remember to use the SAVE and/or DOWNLOAD buttons
on the Reference Data screen to preserve your work.

|image27|

Option: Using MERGE to automatically create a template from a parameter name-value pair file
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

APPC CDT also provides a way to create a template from an
existing parameter name-value pair file. This is useful when the
configuration of the VNF has changed. Rather than manually recreating
the template, you can use the MERGE function to automatically add
parameter names based on a valid name-value pairs file from a previous
template.

First, navigate to the Template tab and “Param Values” subtab and click
on UPLOAD PARAM FILE

|image28|

Then navigate to the Template configuration screen. Upload a
configuration file that contains values you wish to turn into
parameters.

|image29|

Next, click “MERGE FROM PARAM”. APPC will automatically associate the parameter values in the uploaded configuration with parameter names from the parameter name/value. If duplicate parameter values are found in the configuration, APP-C will highlight the duplicate value & name in orange and let the user edit the parameter name.  When the duplicate parameter name has been successfully replaced with a unique name, the highlight will change from orange to green..

After using the MERGE FROM PARAM button to create a template, you can use the
SYNCHRONIZE TEMPLATE PARAMETERS button to create/update the parameter definition file and
name-value files.

Remember to use the SAVE and/or DOWNLOAD buttons on the Reference Data
screen to preserve your work.

|image30|


Option: Synchronize with Name/Values
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
There may be a scenario where you have created or uploaded a template, and SYNCHONIZED TEMPLATE PARAMETERS, and then you want to remove some name-value pairs. APPC provides a SYNCHRONIZE WITH NAME VALUES button that will automatically synchronize the parameter definitions with your updated name value pairs.


  - Step 1: Create or upload template
  
  - Step 2: SYNCHRONIZE TEMPLATE PARAMETERS. (APPC will update Parameter Definition file and Name-Value Pair file to match Template.)
  
  - Step 3: Manually edit Name-Value Pair file (or upload a changed Name-Value Pair file)
  
  - Step 4: SYNCHRONIZE WITH NAME VALUES. (see screen shot below)(APPC will update Parameter Definition file to match Name-Value Pair file.)
  
  - Step 5: Examine Parameter Definitions to confirm they now match updated Name-Value Pair file.


|image30a|
  
  
Test the template in a lab using APPC CDT Test Function
=======================================================

The APPC CDT **TEST** action is used to initiate configuration
and other lifecycle commands

**Prerequisites:**
   - A. Testing requires an instance of the target VNF to be reachable from your test environment.
   - B.	You have created the on-boarding artifacts (e.g., reference file, template, etc) for the target VNF type and action in CDT and saved  them to APPC. 
   - C.	You have created a user input spreadsheet for the VNF and action you wish to test. 

**Steps to Test a template:**
   - 1.	Choose the TEST function on the APPC CDT home page
   - 2.	Upload the user input spreadsheet
   - 3.	Click on EXECUTE TEST
   - 4.	View test progress; poll for test status if necessary.
   - 5.	View Test Results


User Input Spreadsheet
----------------------

The following steps are used to prepare a user input spreadsheet for the
VNF instance and action to be tested.

1. Start with this generic user input excel spreadsheet.

    :download:`Generic 1802 User Input Spreadsheet v.02.xlsx` (compatible with excel 2013)

    Update the user-input sections of the spreadsheet.

     - a) Upload Data tab: choose action, populate VNF-ID

     - b) >Action< tab: Select the tab for the action being tested. Choose a protocol and enter required action identifiers & request parameter values. Enter any payload parameter names and values required for this associated template. (copy/paste from a name-value pair file or other source).

    The screen shots on the following pages show the user input sections highlighted in yellow.

2. Save the spreadsheet with a name for your VNF instance and action.

“Upload Data” tab – Select action to be tested and populate any action
identifiers such as vnf-id.

|image31|

Action tab: This example is for the ConfigModify action, so the
“ConfigModify” tab is shown. Choose a protocol and enter required action
identifiers & request parameter values. Enter any payload parameter
names and values required for this associated template. (You may
copy/paste from a name-value pair file or other source).

|image32|

Using APPC CDT TEST Function
----------------------------

**Steps to use the “TEST” function of the APPC Design Tool**

1. Choose the TEST function on the APPC Design Tool home page
  
   |image33|
  
2. Upload the user input spreadsheet
3. Click on EXECUTE TEST 
4. View test progress; poll for test status if necessary.
  
   |image34|
  
   |image35|
  
5. View Test Results
  
   |image36|


Note on populating southbound properties:
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

When a new vnf-type is created or a new action is added to an existing
vnf-type using the CDT tool and the Reference Data artifact is loaded to
APPC, an update is made to the APPC run-time southbound properties
file for the vnf-type.   The southbound properties are needed for
connecting to a VNF instance or Ansible server.  The southbound
properties contain the following information:

``{vnf\_type}.{protocol}.{action}.user = {value}``

``{vnf\_type}.{protocol}.{action}.port = {value}``

``{vnf\_type}.{protocol}.{action}.password = {value}``

``{vnf\_type}.{protocol}.{action}.url = {value}``

The user, port, and url values are contained in the Reference Data
artifact, if populated by the self-service user. 

The current process that creates the southbound properties from the Reference Data only updates the southbound properties file on a single APPC node in the ODL cluster..   


APP-C Design Tool - File Descriptions
=====================================

+--------------------------------------------------------------------------------------------------------------------------------------+-------------------+
| **File Description**                                                                                                                 | **File Format**   |
+======================================================================================================================================+===================+
|                                                                                                                                      |                   |
| **Pre-template Config file** –contains a ‘golden’ or working configuration (for Netconf) or JSON data block (for Chef or Ansible).   | XML, JSON         |
|                                                                                                                                      |                   |
+--------------------------------------------------------------------------------------------------------------------------------------+-------------------+
|                                                                                                                                      |                   |
| **Reference file**  – describes a VNF in terms of its subtending VM’s and VNFC’s and the actions/protocols being onboarded.          | XML, JSON         |
|                                                                                                                                      |                   |
+--------------------------------------------------------------------------------------------------------------------------------------+-------------------+
|                                                                                                                                      |                   |
| **Template file** – a configuration file with parameters for instance-specific fields.                                               | XML               |
|                                                                                                                                      |                   |
+--------------------------------------------------------------------------------------------------------------------------------------+-------------------+
|                                                                                                                                      |                   |
| **Parameter Definition file** (aka pd\_Configure) contains **parameter definitions** associated with a template.                     | YAML              |
|                                                                                                                                      |                   |
+--------------------------------------------------------------------------------------------------------------------------------------+-------------------+
|                                                                                                                                      |                   |
| **Name-Value file** (aka param\_Configure) contains name-value pairs for parameters associated with a template.                      | JSON              |
|                                                                                                                                      |                   |
+--------------------------------------------------------------------------------------------------------------------------------------+-------------------+
|                                                                                                                                      |                   |
| **Key data file** – contains external system data to populate a PD configure file.                                                   | TXT               |
|                                                                                                                                      |                   |
+--------------------------------------------------------------------------------------------------------------------------------------+-------------------+



.. |image0| image:: media/image0.png
   :width: 7.88889in 
   :height: 4.43750in 
.. |image1| image:: media/image1.png
   :width: 8.72292in
   :height: 4.51788in
.. |image2| image:: media/image2.png
   :width: 8.75000in
   :height: 4.58908in
.. |image3| image:: media/image3.png
   :width: 8.70833in
   :height: 4.89844in
.. |image4| image:: media/image4.png
   :width: 7.46875in
   :height: 4.19310in
.. |image5| image:: media/image5.png
   :width: 7.23958in
   :height: 3.87172in
.. |image6| image:: media/image6.png
   :width: 7.58491in
   :height: 4.26651in
.. |image7| image:: media/image7.png
   :width: 9.43750in
   :height: 5.30859in
.. |image8| image:: media/image8.png
   :width: 7.86980in
   :height: 4.72917in
.. |image9| image:: media/image9.png
   :width: 7.56250in
   :height: 4.54450in
.. |image10| image:: media/image10.png
   :width: 9.01042in
   :height: 5.06836in
.. |image11| image:: media/image11.png
   :width: 9.44792in
   :height: 5.31445in
.. |image12| image:: media/image12.png
   :width: 9.48958in
   :height: 5.33789in
.. |image13| image:: media/image13.png
   :width: 9.48125in
   :height: 5.33320in
.. |image14| image:: media/image14.png
   :width: 9.25926in
   :height: 5.20833in
.. |image15| image:: media/image15.png
   :width: 9.05556in
   :height: 5.09375in
.. |image15a| image:: media/image15a.png 
.. |image15b| image:: media/image15b.png 
.. |image15c| image:: media/image15c.png 
.. |image15d| image:: media/image15d.png 
.. |image15e| image:: media/image15e.png 
.. |image15f| image:: media/image15f.png  
.. |image16| image:: media/image16.png
   :width: 5.79167in
   :height: 3.74135in
.. |imageA| image:: media/imageA.png
   :width: 5.79167in
   :height: 3.74135in  
.. |image17| image:: media/image17.png
   :width: 6.13542in
   :height: 4.97745in
.. |image18| image:: media/image18.png
   :width: 9.00000in
   :height: 5.27639in
.. |image19| image:: media/image19.png
   :width: 5.43423in
   :height: 1.83333in
.. |image20| image:: media/image20.png
   :width: 5.44473in
   :height: 1.93750in
.. |image21| image:: media/image21.png
   :width: 5.32292in
   :height: 1.92771in
.. |image23| image:: media/image23.png
   :width: 7.54167in
   :height: 4.24219in
.. |image24| image:: media/image24.png
   :width: 7.48148in
   :height: 4.20833in
.. |image26| image:: media/image26.png
   :width: 6.87789in
   :height: 3.78125in
.. |image27| image:: media/image27.png
   :width: 7.97170in
   :height: 4.48408in
.. |image28| image:: media/image28.png
   :width: 8.56604in
   :height: 4.81840in
.. |image29| image:: media/image29.png
   :width: 9.00943in
   :height: 5.06781in
.. |image30| image:: media/image30.png
   :width: 8.07407in
   :height: 4.54167in
.. |image30a| image:: media/image30a.png   
.. |image31| image:: media/image31.png
   :width: 9.00000in
   :height: 5.18958in
.. |image32| image:: media/image32.png
   :width: 9.00000in
   :height: 5.18958in
.. |image33| image:: media/image33.png
   :width: 9.00000in
   :height: 5.18958in
.. |image34| image:: media/image34.png
   :width: 9.00000in
   :height: 5.18958in
.. |image35| image:: media/image35.png
   :width: 9.00000in
   :height: 5.18958in
.. |image36| image:: media/image36.png
   :width: 9.00000in
   :height: 5.18958in


