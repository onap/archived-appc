#!/bin/bash

###
# ============LICENSE_START=======================================================
# ONAP : APP-C
# ================================================================================
# Copyright (C) 2017 AT&T Intellectual Property.  All rights reserved.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# ============LICENSE_END=========================================================
###


export BVC_DIR=/opt/app/bvc
export DG_DIR=${BVC_DIR}/dg

if (( "$#" != 1 )) 
then
    echo "DG JSON Directory is Missing"
exit 1
fi

export DG_JSON_DIR=$1
echo "Processing DG JSON directory : ${DG_JSON_DIR}"

cd ${DG_JSON_DIR}

######################################################################
rm -rf ${DG_JSON_DIR}/xml

################## To Genetare XML from JSON ########################
$JAVA_HOME/bin/java -cp ${DG_DIR}/lib/dg-loader-provider.jar org.openecomp.sdnc.dg.loader.DGXMLGenerator ${DG_JSON_DIR} ${DG_JSON_DIR}/xml

################## To Load DG XML ########################
$JAVA_HOME/bin/java -cp ${DG_DIR}/lib/dg-loader-provider.jar org.openecomp.sdnc.dg.loader.DGXMLLoad ${DG_JSON_DIR}/xml ${BVC_DIR}/properties/dblib.properties

################## To Activate ########################
$JAVA_HOME/bin/java -cp ${DG_DIR}/lib/dg-loader-provider.jar org.openecomp.sdnc.dg.loader.DGXMLActivate ${DG_JSON_DIR}/dg_activate.txt ${BVC_DIR}/properties/dblib.properties

exit 0
