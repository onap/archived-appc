package org.onap.appc.adapter.netconf.internal;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.onap.appc.adapter.netconf.ConnectionDetails;
import org.onap.appc.adapter.netconf.NetconfConnectionDetails;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

import javax.sql.rowset.CachedRowSet;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.logging.Logger;

public class TestNetconfDataAccessServiceImpl {
    NetconfDataAccessServiceImpl netconfDataAccessService;
    private String schema;
    private DbLibService dbLibServiceMocked;

    @Before
    public void SetUp() {
        schema = "test-netconf-adaptor";
        dbLibServiceMocked = new DbLibService() {
            @Override
            public CachedRowSet getData(String s, ArrayList<String> arrayList, String s1) throws SQLException {
                CachedRowSet cachedRowSetMocked = Mockito.mock(CachedRowSet.class);
                return cachedRowSetMocked;
            }

            @Override
            public boolean writeData(String s, ArrayList<String> arrayList, String s1) throws SQLException {
                return false;
            }

            @Override
            public boolean isActive() {
                return false;
            }

            @Override
            public Connection getConnection() throws SQLException {
                return null;
            }

            @Override
            public Connection getConnection(String username, String password) throws SQLException {
                return null;
            }

            @Override
            public <T> T unwrap(Class<T> iface) throws SQLException {
                return null;
            }

            @Override
            public boolean isWrapperFor(Class<?> iface) throws SQLException {
                return false;
            }

            @Override
            public PrintWriter getLogWriter() throws SQLException {
                return null;
            }

            @Override
            public void setLogWriter(PrintWriter out) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public void setLoginTimeout(int seconds) throws SQLException {

            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };

        netconfDataAccessService = new NetconfDataAccessServiceImpl();
        netconfDataAccessService.setSchema(schema);
        netconfDataAccessService.setDbLibService(dbLibServiceMocked);
    }

    @Test
    public void testRetrieveConfigFileName() throws IOException {
        String response = netconfDataAccessService.retrieveConfigFileName("test");

        Assert.assertEquals("", response);
    }

    @Test
    public void testRetrieveConnectionDetails() throws IOException {
        ConnectionDetails netconfConnectionDetails = new ConnectionDetails();

        boolean response = netconfDataAccessService.retrieveConnectionDetails("test", netconfConnectionDetails);

        Assert.assertEquals(false, response);
    }

    @Test
    public void testRetrieveNetconfConnectionDetails() throws IOException {
        NetconfConnectionDetails netconfConnectionDetails = new NetconfConnectionDetails();

        boolean response = netconfDataAccessService.retrieveNetconfConnectionDetails("test", netconfConnectionDetails);

        Assert.assertEquals(true, response);
    }

    @Test
    public void testLogDeviceInteraction() throws IOException {
        NetconfConnectionDetails netconfConnectionDetails = new NetconfConnectionDetails();

        boolean response = netconfDataAccessService.logDeviceInteraction("test", "",
                                                                         "", "");

        Assert.assertEquals(true, response);
    }
}
