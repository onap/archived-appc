## Introduction
* Run 'mvn clean install' on this project in order to build and compile all artifacts contained in it.
* The action above will produce a .zip file under the installer directory of each bundle, which can be installed during Docker Instantiation (Please see master README for more information on this).

## Handling YANG Models
* After running 'mvn clean install', this will generate some code from the yang models.
* Modify the yang model file under the model project.
* Follow the comments in the generated provider class to wire your new provider into the generated 
code.
* Modify the generated provider model to respond to and handle the yang model. Depending on what
you added to your model you may need to inherit additional interfaces or make other changes to
the provider model.

## Structure of a ODL Karaf Feature in APP-C
* model
    - Provides the yang model for your application. This is your primary northbound interface.
* provider
    - This is where the JAVA code is implemented. This part will define what the Karaf Feature will accomplish and how it will respond to a yang model.
* features
    - Defines the contents of a Karaf Feature. If you add dependencies on third-party bundles, then you will need to
      modify the features.xml to list out the dependencies.
* installer
    - Bundles all of the jars and third party dependencies (minus ODL dependencies) into a single
      .zip file with the necessary configuration files in order to install the Karaf Feature by means of calling the Karaf Client (which will ultimately run "feature:install <FEATURE_NAME>") in order to install the feature.
      

