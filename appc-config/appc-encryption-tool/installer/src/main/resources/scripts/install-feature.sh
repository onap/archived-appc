###
# ============LICENSE_START=======================================================
# ONAP : APP-C
# ================================================================================
# Copyright (C) 2017-2018 AT&T Intellectual Property.  All rights reserved.
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
# ECOMP is a trademark and service mark of AT&T Intellectual Property.
###

#!/bin/bash

ODL_HOME=${ODL_HOME:-/opt/opendaylight/current}
ODL_KARAF_CLIENT=${ODL_KARAF_CLIENT:-${ODL_HOME}/bin/client}
ODL_KARAF_CLIENT_OPTS=${ODL_KARAF_CLIENT_OPTS:-""}
INSTALLERDIR=$(dirname $0)

REPOZIP=${INSTALLERDIR}/${features.boot}-${project.version}.zip

if [ -f ${REPOZIP} ]
then
    unzip -n -d ${ODL_HOME} ${REPOZIP}
else
    echo "ERROR : repo zip ($REPOZIP) not found"
    exit 1
fi

${ODL_KARAF_CLIENT} ${ODL_KARAF_CLIENT_OPTS} feature:repo-add ${features.repositories}
${ODL_KARAF_CLIENT} ${ODL_KARAF_CLIENT_OPTS} feature:install ${features.boot}
