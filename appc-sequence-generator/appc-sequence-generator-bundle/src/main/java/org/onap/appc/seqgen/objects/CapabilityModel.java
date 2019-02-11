package org.onap.appc.seqgen.objects;

import java.util.List;
import java.util.Map;

public class CapabilityModel {

    private List<String> vnfCapabilities;
    private List<String> vfModuleCapabilities;
    private Map<String, List<String>> vmCapabilities;
    private List<String> vnfcCapabilities;

    public CapabilityModel() {
    }
    
    public CapabilityModel( List<String> vnfCapabilities,
                            List<String> vfModuleCapabilities,
                            Map<String, List<String>> vmCapabilities,
                            List<String> vnfcCapabilities) {

        this.vnfCapabilities = vnfCapabilities;
        this.vfModuleCapabilities = vfModuleCapabilities;
        this.vmCapabilities = vmCapabilities;
        this.vnfcCapabilities = vnfcCapabilities;
    }
    public List<String> getVnfCapabilities() {
        return vnfCapabilities;
    }
    public List<String> getVfModuleCapabilities() {
        return vfModuleCapabilities;
    }
    public Map<String,List<String>> getVmCapabilities() {
        return vmCapabilities;
    }
    public List<String> getVnfcCapabilities() {
        return vnfcCapabilities;
    }
    @Override
    public String toString() {
        return "CapabilitiesModel = " + "vnf=" + getVnfCapabilities() +
                "vfModule=" + getVfModuleCapabilities() +
                "vm=" + getVmCapabilities() +
                "vnfc=" + getVnfcCapabilities();
    }
}
