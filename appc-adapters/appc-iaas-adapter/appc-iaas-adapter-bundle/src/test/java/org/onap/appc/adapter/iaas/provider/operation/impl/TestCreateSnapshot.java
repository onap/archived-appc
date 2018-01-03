package org.onap.appc.adapter.iaas.provider.operation.impl;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.onap.appc.exceptions.APPCException;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.atLeastOnce;
import com.att.cdp.exceptions.ZoneException;
import com.att.cdp.zones.model.Image;
import com.att.cdp.zones.model.Server;
import com.att.cdp.zones.model.Server.Status;

public class TestCreateSnapshot {

    @Test
    public void createSnapshotRunning() throws ZoneException {
        MockGenerator mg = new MockGenerator(Status.RUNNING);
        Server server = mg.getServer();
        Image image = mock(Image.class);
        doReturn("1234567").when(image).getId();
        doReturn(mg.getContext()).when(image).getContext();
        doReturn("wrong image name").when(image).getName();
        doReturn(com.att.cdp.zones.model.Image.Status.ACTIVE).when(image).getStatus();
        doReturn(image).when(mg.getImageService()).getImageByName(any());
        CreateSnapshot rbs = new CreateSnapshot();
        rbs.setProviderCache(mg.getProviderCacheMap());
        try {
            rbs.executeProviderOperation(mg.getParams(), mg.getSvcLogicContext());
        } catch (APPCException e) {
            Assert.fail("Exception during CreateSnapshot.executeProviderOperation");
        }
        ArgumentCaptor<String> createSnapshotCaptor = ArgumentCaptor.forClass(String.class);
        verify(server).createSnapshot(createSnapshotCaptor.capture());
        ArgumentCaptor<String> getImageNameCaptor = ArgumentCaptor.forClass(String.class);
        verify(mg.getImageService(), atLeastOnce()).getImageByName(getImageNameCaptor.capture());
        assertEquals(createSnapshotCaptor.getValue(), getImageNameCaptor.getValue());
    }
}
