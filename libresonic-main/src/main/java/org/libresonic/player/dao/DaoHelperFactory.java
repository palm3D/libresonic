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

/**
 * @author Sindre Mehus
 * @version $Id$
 */
public class DaoHelperFactory {

    public static DaoHelper create() {
        //Uncomment HsqlDaoHelper for database configured in HsqlDaoHelper.java
        //Uncomment MysqlDaoHelper for database configured in MysqlDaoHelper.java
        //return new HsqlDaoHelper();
        return new MysqlDaoHelper();
    }
}
