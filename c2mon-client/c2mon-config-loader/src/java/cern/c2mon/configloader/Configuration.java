/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * 
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * 
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/

package cern.c2mon.configloader;

import static java.lang.String.format;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author wbuczak
 */
public class Configuration {

    public static final int TIMESTAMP_NOT_SET = -1;

    private final long id;
    private final String name;
    private final String description;
    private final String author;
    private final long createTimestamp;
    private final String createTimestampStr;
    private final long applyTimestamp;

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Object getAuthor() {
        return author;
    }

    public long getCreateTimestamp() {
        return createTimestamp;
    }
    
    public String getCreateTimestampStr() {
        return createTimestampStr;
    }

    public long getApplyTimestamp() {
        return applyTimestamp;
    }

    /**
     * @param id
     * @param name
     * @param author
     * @param createTimestamp
     */
    public Configuration(final long id, final String name, final String description, final String author,
            final long createTimestamp, final long applyTimestamp) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.author = author;
        this.createTimestamp = createTimestamp;
        this.createTimestampStr = dateToStr(createTimestamp);
        this.applyTimestamp = applyTimestamp;
    }

    /**
     * @param createDate2
     * @return
     */
    private static String dateToStr(long timestamp) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(timestamp));
    }

    @Override
    public String toString() {
        return format("%d\t%s\t%s\t%s\t%s", id ,name ,description,author,createTimestampStr);
    }

}
