package org.onap.appc.adapter.ssh.sshd;

import org.junit.Before;
import org.junit.Test;
import org.onap.appc.adapter.ssh.SshConnectionDetails;
import org.onap.ccsdk.sli.core.dblib.DbLibService;

import javax.sql.rowset.CachedRowSet;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.logging.Logger;

import static org.junit.Assert.assertEquals;

public class SshdDataAccessServiceTest {

    private SshdDataAccessService sshdDataAccessService;
    private DbLibService db;
    @Before
    public void setUp() {
        sshdDataAccessService = new SshdDataAccessService();
        db = new DbLibService() {
            @Override
            public CachedRowSet getData(String s, ArrayList<String> arrayList, String s1) throws SQLException {
                return null;
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
            public void setLoginTimeout(int seconds) throws SQLException {

            }

            @Override
            public int getLoginTimeout() throws SQLException {
                return 0;
            }

            @Override
            public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                return null;
            }
        };
    }

    @Test
    public void testSetSchema() {
        sshdDataAccessService.setSchema("test");
        assertEquals("test", sshdDataAccessService.getSchema());
    }

    @Test
    public void testSetDbLibService() {
        sshdDataAccessService.setDbLibService(db);
        assertEquals("false", sshdDataAccessService.getDbLibService().isActive());
    }

    @Test(expected = NullPointerException.class)
    public void testRetrieveConnectionDetails() {
        SshConnectionDetails connectionDetails = new SshConnectionDetails();
        sshdDataAccessService.retrieveConnectionDetails("test", connectionDetails);
    }

    @Test(expected = NullPointerException.class)
    public void testRetrieveConfigFileName() {
        SshConnectionDetails connectionDetails = new SshConnectionDetails();
        sshdDataAccessService.retrieveConfigFileName("test");
    }
}
