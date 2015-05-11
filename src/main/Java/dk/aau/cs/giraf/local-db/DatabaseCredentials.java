import com.mysql.jdbc.Connection;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Tim on 06-05-2015.
 */
public abstract class DatabaseCredentials
{
    private String Username;
    public String getUsername() {
        return Username;
    }
    protected void setUsername(String username) {
        Username = username;
    }

    private String Password;
    public String getPassword() {
        return Password;
    }
    protected void setPassword(String password) {
        Password = password;
    }

    private String Url;
    public String getUrl() {
        return Url;
    }
    protected void setUrl(String url) {
        Url = url;
    }

    public enum serverType {SYMMETRIC_DS, TEST_SERVER, PRODUCTION_SERVER};

    public static Connection getDatabaseConnection(serverType pServer) throws SQLException {
        DatabaseCredentials credits = getDatabaseCredentials(pServer);
        if (credits == null) return null;
        return (Connection) DriverManager.getConnection(credits.getUrl(), credits.getUsername(), credits.getPassword());
    }

    private static DatabaseCredentials getDatabaseCredentials(serverType pServer) {
        switch (pServer) {
            case SYMMETRIC_DS:
                return new SymmetricDS_DatabaseCredentials();
            case TEST_SERVER:
                return new TestServer_DatabaseCredentials();
            case PRODUCTION_SERVER:
                return new ProductionServer_DatabaseCredentials();
            default:
                return null;
        }
    }

    private static class SymmetricDS_DatabaseCredentials extends DatabaseCredentials {
        SymmetricDS_DatabaseCredentials() {
            this.setUsername("syncuser");
            this.setPassword("RtnRT3dnak8j8P6KR75d");
            this.setUrl("jdbc:mysql://cs-cust06-int.cs.aau.dk:3306/girafSDS");
        }
    }

    private static class TestServer_DatabaseCredentials extends DatabaseCredentials {
        TestServer_DatabaseCredentials() {
            this.setUsername("devuser");
            this.setPassword("L2PhTyJW8vnrbChM");
            this.setUrl("jdbc:mysql://cs-cust06-int.cs.aau.dk:3306/girafdev");
        }
    }

    private static class ProductionServer_DatabaseCredentials extends DatabaseCredentials {
        ProductionServer_DatabaseCredentials() {
            /* This database isn't created yet */
            this.setUsername("sqluser");
            this.setPassword("GtmyhZsv30u2LpyYdt1Q");
            this.setUrl("jdbc:mysql://cs-cust06-int.cs.aau.dk:3306/giraf");
        }
    }
}
