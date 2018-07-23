package org.onap.appc.licmgr.objects;

public class LicenseModelBuilder {
    private String entitlementPoolUuid;
    private String licenseKeyGroupUuid;

    public LicenseModelBuilder setEntitlementPoolUuid(String entitlementPoolUuid){
        this.entitlementPoolUuid = entitlementPoolUuid;
        return this;
    }

    public LicenseModelBuilder setLicenseKeyGroupUuid(String licenseKeyGroupUuid){
        this.licenseKeyGroupUuid = licenseKeyGroupUuid;
        return this;
    }

    public boolean isReady() {
        return entitlementPoolUuid != null && licenseKeyGroupUuid != null;
    }

    public LicenseModel build(){
        return new LicenseModel(entitlementPoolUuid, licenseKeyGroupUuid);
    }
}
