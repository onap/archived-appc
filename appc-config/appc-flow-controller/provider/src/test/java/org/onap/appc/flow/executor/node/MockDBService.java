package org.onap.appc.flow.executor.node;


import org.onap.appc.flow.controller.dbervices.FlowControlDBService;

public class MockDBService extends FlowControlDBService {
	 private static MockDBService mockDgGeneralDBService = null;
	    private static MockSvcLogicResource serviceLogic = new MockSvcLogicResource();

	    public MockDBService() {	
	    	super(serviceLogic);
	        if (mockDgGeneralDBService != null) {
	            mockDgGeneralDBService = new MockDBService(serviceLogic);
	        }

	    }

	    public MockDBService(MockSvcLogicResource serviceLogic2) {
	        super(serviceLogic);
	    }

	    public static MockDBService initialise() {
	        if (mockDgGeneralDBService == null) {
	            mockDgGeneralDBService = new MockDBService(serviceLogic);
	        }
	        return mockDgGeneralDBService;
	    }
}
