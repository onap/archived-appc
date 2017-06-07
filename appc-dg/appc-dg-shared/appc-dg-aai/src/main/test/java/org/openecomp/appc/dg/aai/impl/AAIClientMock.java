/*-
 * ============LICENSE_START=======================================================
 * APPC
 * ================================================================================
 * Copyright (C) 2017 AT&T Intellectual Property. All rights reserved.
 * Copyright (C) 2017 Amdocs
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 * ECOMP is a trademark and service mark of AT&T Intellectual Property.
 */

package org.openecomp.appc.dg.aai.impl;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.openecomp.sdnc.sli.SvcLogicContext;
import org.openecomp.sdnc.sli.SvcLogicException;
import org.openecomp.sdnc.sli.SvcLogicResource;
import org.openecomp.sdnc.sli.aai.AAIClient;
import org.openecomp.sdnc.sli.aai.AAIServiceException;
import org.openecomp.sdnc.sli.aai.data.*;
import org.openecomp.sdnc.sli.aai.data.notify.NotifyEvent;
import org.openecomp.sdnc.sli.aai.data.v1507.VServer;
import org.openecomp.sdnc.sli.aai.update.Update;


public class AAIClientMock implements AAIClient {

    Map<String, String> mockAAI = new HashMap<>();

    public void setMockAAI(Map<String, String> mockAAI) {
        this.mockAAI = mockAAI;
    }

    @Override
    public AAIResponse requestSdnZoneQuery(String s, String s1, String s2) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postNetworkVceData(String s, Vce vce) throws AAIServiceException {
        return false;
    }

    @Override
    public Vce requestNetworkVceData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean deleteNetworkVceData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public ServiceInstance requestServiceInterfaceData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public ServiceInstance requestServiceInterfaceData(String s, String s1, String s2) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postServiceInterfaceData(String s, String s1, String s2, ServiceInstance serviceInstance) throws AAIServiceException {
        return false;
    }

    @Override
    public SearchResults requestServiceInstanceURL(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public Vpe requestNetworkVpeData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postNetworkVpeData(String s, Vpe vpe) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteNetworkVpeData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public Vserver requestVServerData(String s, String s1, String s2, String s3) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postVServerData(String s, String s1, String s2, String s3, Vserver vserver) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteVServerData(String s, String s1, String s2, String s3, String s4) throws AAIServiceException {
        return false;
    }

    @Override
    public URL requestVserverURLNodeQuery(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public String getTenantIdFromVserverUrl(URL url) {
        return null;
    }

    @Override
    public String getCloudOwnerFromVserverUrl(URL url) {
        return null;
    }

    @Override
    public String getCloudRegionFromVserverUrl(URL url) {
        return null;
    }

    @Override
    public String getVServerIdFromVserverUrl(URL url, String s) {
        return null;
    }

    @Override
    public Vserver requestVServerDataByURL(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public VplsPe requestNetworkVplsPeData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postNetworkVplsPeData(String s, VplsPe vplsPe) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteNetworkVplsPeData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public Complex requestNetworkComplexData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postNetworkComplexData(String s, Complex complex) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteNetworkComplexData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public CtagPool requestCtagPoolData(String s, String s1, String s2) throws AAIServiceException {
        return null;
    }

    @Override
    public VServer dataChangeRequestVServerData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public CtagPool dataChangeRequestCtagPoolData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public VplsPe dataChangeRequestVplsPeData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public Vpe dataChangeRequestVpeData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public DvsSwitch dataChangeRequestDvsSwitchData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public PServer dataChangeRequestPServerData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public OamNetwork dataChangeRequestOAMNetworkData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public AvailabilityZone dataChangeRequestAvailabilityZoneData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public Complex dataChangeRequestComplexData(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean dataChangeDeleteVServerData(URL url) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean dataChangeDeleteCtagPoolData(URL url) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean dataChangeDeleteVplsPeData(URL url) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean dataChangeDeleteVpeData(URL url) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean dataChangeDeleteDvsSwitchData(URL url) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean dataChangeDeleteOAMNetworkData(URL url) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean dataChangeDeleteAvailabilityZoneData(URL url) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean dataChangeDeleteComplexData(URL url) throws AAIServiceException {
        return false;
    }

    @Override
    public GenericVnf requestGenericVnfData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postGenericVnfData(String s, GenericVnf genericVnf) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteGenericVnfData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public DvsSwitch requestDvsSwitchData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postDvsSwitchData(String s, DvsSwitch dvsSwitch) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteDvsSwitchData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public PInterface requestPInterfaceData(String s, String s1) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postPInterfaceData(String s, String s1, PInterface pInterface) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deletePInterfaceData(String s, String s1, String s2) throws AAIServiceException {
        return false;
    }

    @Override
    public PhysicalLink requestPhysicalLinkData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postPhysicalLinkData(String s, PhysicalLink physicalLink) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deletePhysicalLinkData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public PServer requestPServerData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postPServerData(String s, PServer pServer) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deletePServerData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public L3Network requestL3NetworkData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public L3Network requestL3NetworkQueryByName(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postL3NetworkData(String s, L3Network l3Network) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteL3NetworkData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public VpnBinding requestVpnBindingData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean deleteVpnBindingData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public VnfImage requestVnfImageData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public VnfImage requestVnfImageDataByVendorModel(String s, String s1) throws AAIServiceException {
        return null;
    }

    @Override
    public VnfImage requestVnfImageDataByVendorModelVersion(String s, String s1, String s2) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean sendNotify(NotifyEvent notifyEvent, String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public SitePairSet requestSitePairSetData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postSitePairSetData(String s, SitePairSet sitePairSet) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteSitePairSetData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public Service requestServiceData(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postServiceData(String s, Service service) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean deleteServiceData(String s, String s1) throws AAIServiceException {
        return false;
    }

    @Override
    public QueryResponse requestNodeQuery(String s, String s1, String s2) throws AAIServiceException {
        return null;
    }

    @Override
    public String requestDataByURL(URL url) throws AAIServiceException {
        return null;
    }

    @Override
    public GenericVnf requestGenericVnfeNodeQuery(String s) throws AAIServiceException {
        return null;
    }

    @Override
    public Tenant requestTenantData(String s, String s1, String s2) throws AAIServiceException {
        return null;
    }

    @Override
    public Tenant requestTenantDataByName(String s, String s1, String s2) throws AAIServiceException {
        return null;
    }

    @Override
    public boolean postTenantData(String s, String s1, String s2, Tenant tenant) throws AAIServiceException {
        return false;
    }

    @Override
    public boolean updateAnAIEntry(Update update) throws AAIServiceException {
        return false;
    }

    @Override
    public QueryStatus backup(Map<String, String> map, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus restore(Map<String, String> map, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus isAvailable(String s, String s1, String s2, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus exists(String s, String s1, String s2, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus query(String s, boolean b, String s1, String key, String prefix, String s4, SvcLogicContext ctx) throws SvcLogicException {
        if (s.equals("generic-vnf") && key.equals("vnf-id = 'test_VNF'") && ctx != null) {
            for (Map.Entry<String, String> entry : mockAAI.entrySet()) {
                ctx.setAttribute(prefix + "." + entry.getKey(), entry.getValue());
            }
            return QueryStatus.SUCCESS;


        } else if (key.equals("vnf-id = 'test_VNF1'")){
            return QueryStatus.NOT_FOUND;
        }
        else if (key.equals("vnf-id = 'test_VNF3'")){
            throw new SvcLogicException();
        }
        else {
            return QueryStatus.FAILURE;
        }


    }

    @Override
    public QueryStatus reserve(String s, String s1, String s2, String s3, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus save(String s, boolean b, boolean b1, String s1, Map<String, String> map, String s2, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus release(String s, String s1, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus delete(String s, String s1, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus notify(String s, String s1, SvcLogicContext svcLogicContext) throws SvcLogicException {
        return null;
    }

    @Override
    public QueryStatus update(String s, String key, Map<String, String> data, String prefix, SvcLogicContext ctx) throws SvcLogicException {
        if (s.equals("generic-vnf") && key.equals("vnf-id = 'test_VNF'") && ctx != null) {
            for (Map.Entry<String, String> entry : data.entrySet()) {
                mockAAI.put(entry.getKey(), entry.getValue());
            }
            return QueryStatus.SUCCESS;


        } else if (key.equals("vnf-id = 'test_VNF1'")){
            return QueryStatus.NOT_FOUND;
        }
        else if (key.equals("vnf-id = 'test_VNF3'")){
            throw new SvcLogicException();
        }
        else {
            return QueryStatus.FAILURE;
        }

    }
}
