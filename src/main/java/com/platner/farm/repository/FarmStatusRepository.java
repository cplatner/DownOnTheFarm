package com.platner.farm.repository;

import com.platner.farm.models.FarmAction;
import com.platner.farm.models.FarmStatus;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

// NB: Not a repository in the Spring sense
@Component
public class FarmStatusRepository implements InitializingBean, DisposableBean {

    private static final String DBFILE = "down-on-the-farm.db";
    /** Message for most missing data conditions */
    private static final String THE_END = "A great dark cloud covers your farm.  This must be the end.";
    private static final String INITSQL = """
            CREATE TABLE Activity (
                activityId    INTEGER NOT NULL,
                name    TEXT NOT NULL,
                PRIMARY KEY(activityId AUTOINCREMENT)
            );
            
            CREATE TABLE Disaster (
                disasterId    INTEGER NOT NULL,
                text    TEXT NOT NULL,
                PRIMARY KEY(disasterId AUTOINCREMENT)
            );
            
            CREATE TABLE DisasterActivity (
                disasterId    INTEGER NOT NULL,
                activityId    INTEGER NOT NULL,
                PRIMARY KEY(disasterId,activityId),
                FOREIGN KEY(disasterId) REFERENCES Disaster(disasterId),
                FOREIGN KEY(activityId) REFERENCES Activity(activityId)
            );
            
            INSERT INTO Activity (activityId, name) VALUES (1, 'plow');
            INSERT INTO Activity (activityId, name) VALUES (2, 'plant');
            INSERT INTO Activity (activityId, name) VALUES (3, 'irrigate');
            INSERT INTO Activity (activityId, name) VALUES (4, 'harvest');
            
            INSERT INTO Disaster (disasterId, text) VALUES (1, 'A tornado sweeps through a small town near your farm, and destroys your barn in the process');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (1, 1); --plow
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (1, 2); --plant
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (1, 3); --irrigate
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (1, 4); --harvest
            
            INSERT INTO Disaster (disasterId, text) VALUES (2, 'A combine catches fire, burns, and ignites the field too');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (2, 4); --harvest
            
            INSERT INTO Disaster (disasterId, text) VALUES (3, 'A hailstorm destroys your crops');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (3, 3); --irrigate
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (3, 4); --harvest
            
            INSERT INTO Disaster (disasterId, text) VALUES (4, 'Flooding in the spring delays planting, and the crops never ripen');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (4, 1); --plow
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (4, 2); --plant
            
            INSERT INTO Disaster (disasterId, text) VALUES (5, 'An early freeze kills all of the crops');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (5, 3); --irrigate
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (5, 4); --harvest
            
            INSERT INTO Disaster (disasterId, text) VALUES (6, 'A late freeze kills all of the crops');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (6, 2); --plant
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (6, 3); --irrigate
            
            INSERT INTO Disaster (disasterId, text) VALUES (7, 'A thunderstorm starts fires in the fields, and all of the crops are lost');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (7, 4); --harvest
            
            INSERT INTO Disaster (disasterId, text) VALUES (8, 'A derecho sweeps through your fields, and destroys all of the crops');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (8, 3); --irrigate
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (8, 4); --harvest
            
            INSERT INTO Disaster (disasterId, text) VALUES (9, 'A late-spring blizzard delays planting, and the crops never ripen');
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (9, 1); --plow
            INSERT INTO DisasterActivity (disasterId, activityId) VALUES (9, 2); --plant
            """;

    private static final String GETRANDOMSTATUSSQL = """
            -- Grab one random disaster
            SELECT text FROM Disaster d
            INNER JOIN DisasterActivity da ON da.disasterId = d.disasterId
            INNER JOIN Activity a ON a.activityId = da.activityId
            WHERE a.name = ?
            ORDER BY random()
            LIMIT 1
            """;

    private static final Logger LOG = LoggerFactory.getLogger(FarmStatusRepository.class);
    private Connection connection;

    public FarmStatus getStatus(final @NonNull FarmAction action) {
        String message = null;
        try {
//            connection = DriverManager.getConnection("jdbc:sqlite:" + DBFILE);
            try (PreparedStatement statement = connection.prepareStatement(GETRANDOMSTATUSSQL)) {
                statement.setQueryTimeout(30);
                statement.setString(1, action.getAction().toString());
                ResultSet rs = statement.executeQuery();
                message = rs.getString(1);
            }
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return new FarmStatus(message != null ? message : THE_END, action.getAction());
    }

    @Override
    public void destroy() throws Exception {
        LOG.info("Closing Database");
        try {
            connection.close();
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        } finally {
            if (!connection.isClosed()) {
                connection.close();
            }
        }
    }

    @Override
    public void afterPropertiesSet() {
        initializeDatabase();
    }

    void initializeDatabase() {
        LOG.info("Initializing Database");
        try {
            File dbfile = new File(DBFILE);
            if (dbfile.exists()) {
                dbfile.delete();
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + DBFILE);
            try (Statement statement = connection.createStatement()) {
                statement.setQueryTimeout(30);
                executeStatements(statement, INITSQL);
            }
            LOG.info("Database initialized");
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void executeStatements(Statement statement, String query) throws SQLException {
        String[] queries = query.split(";");
        for (String q : queries) {
            if (q.trim().isEmpty() || q.trim().startsWith("--")) {
                continue;
            }
            statement.addBatch(q);
        }
        statement.executeBatch();
    }
}
