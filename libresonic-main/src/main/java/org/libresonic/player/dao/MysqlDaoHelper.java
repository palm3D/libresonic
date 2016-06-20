/*
 * This file is part of Libresonic.
 *
 *  Libresonic is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Libresonic is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Libresonic.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  Copyright 2015 (C) Sindre Mehus
 */
package org.libresonic.player.dao;

import java.io.File;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.libresonic.player.Logger;
import org.libresonic.player.dao.schema.Schema;
import org.libresonic.player.dao.schema.mysql.Schema61;
import org.libresonic.player.service.SettingsService;

/**
 * DAO helper class which creates the data source, and updates the database schema.
 *
 * @author Bernardus Jansen
 * Based on HsqlDaoHelper by Sindre Mehus
 */
public class MysqlDaoHelper implements DaoHelper {

    private static final Logger LOG = Logger.getLogger(MysqlDaoHelper.class);

    private Schema[] schemas = {new Schema61()};
    private DataSource dataSource;
    private static boolean shutdownHookAdded;

    public MysqlDaoHelper() {
        dataSource = createDataSource();
        checkDatabase();
        addShutdownHook();
    }

    private void addShutdownHook() {
        if (shutdownHookAdded) {
            return;
        }
        shutdownHookAdded = true;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.err.println("Shutting down database...");
                getJdbcTemplate().execute("shutdown");
                System.err.println("Shutting down database - Done!");
            }
        });
    }

    /**
     * Returns a JDBC template for performing database operations.
     *
     * @return A JDBC template.
     */
    public JdbcTemplate getJdbcTemplate() {
        return new JdbcTemplate(dataSource);
    }

    public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    private DataSource createDataSource() {
        DriverManagerDataSource ds = new DriverManagerDataSource();
        ds.setDriverClassName("org.mariadb.jdbc.Driver");
        ds.setUrl("jdbc:mariadb://localhost:3306/libresonic");
        ds.setUsername("lsdbuser");
        ds.setPassword("lspasswd");

        return ds;
    }

    private void checkDatabase() {
        LOG.info("Checking database schema.");
        try {
            for (Schema schema : schemas) {
                schema.execute(getJdbcTemplate());
            }
            LOG.info("Done checking database schema.");
        } catch (Exception x) {
            LOG.error("Failed to initialize database.", x);
        }
    }
}
