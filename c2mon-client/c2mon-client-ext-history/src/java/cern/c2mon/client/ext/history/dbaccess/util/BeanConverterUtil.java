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
package cern.c2mon.client.ext.history.dbaccess.util;

import java.sql.Timestamp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.client.ext.history.ClientDataTagRequestCallback;
import cern.c2mon.client.ext.history.common.HistorySupervisionEvent;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.dbaccess.beans.HistoryRecordBean;
import cern.c2mon.client.ext.history.dbaccess.beans.SupervisionRecordBean;
import cern.c2mon.client.ext.history.updates.HistorySupervisionEventImpl;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagMode;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import cern.c2mon.shared.common.type.TypeConverter;

/**
 * Converts beans into common interfaces
 * 
 * @author vdeila
 */
public final class BeanConverterUtil {

    /** Log4j logger for this class */
    private static final Logger LOG = LoggerFactory.getLogger(BeanConverterUtil.class);

    /**
     * Converts a {@link HistoryRecordBean} object to a {@link TagValueUpdate}
     * 
     * @param bean the bean to convert
     * @return the converted object
     */
    public static HistoryTagValueUpdate toTagValueUpdate(final HistoryRecordBean bean) {
        return toTagValueUpdate(bean, null);
    }

    /**
     * Converts a {@link HistoryRecordBean} object to a {@link TagValueUpdate}
     * 
     * @param bean the bean to convert
     * @param clientDataTagRequestCallback Callback to get access to attributes in the {@link Tag}.
     * @return the converted object
     */
    public static HistoryTagValueUpdate toTagValueUpdate(final HistoryRecordBean bean,
            final ClientDataTagRequestCallback clientDataTagRequestCallback) {
        bean.convertIntoLocalTimeZone();
        TagMode mode;
        try {
            mode = TagMode.values()[bean.getTagMode()];
        } catch (final Exception e) {
            mode = TagMode.OPERATIONAL;
            LOG.warn(String.format("Invalid tag mode, \"%d\", for tag %d!", bean.getTagMode(), bean.getTagId()), e);
        }

        final HistoryTagValueUpdateImpl value = new HistoryTagValueUpdateImpl(bean.getTagId(),
                bean.getDataTagQuality(), TypeConverter.cast(bean.getTagValue(), bean.getTagDataType()),
                bean.getTagTime(), bean.getDaqTime(), bean.getServerTime(), new Timestamp(bean.getLogDate().getTime()),
                bean.getTagValueDesc() == null ? "" : bean.getTagValueDesc(), mode);
        value.setDataType(bean.getTagDataType());
        value.setDaqTimestamp(bean.getDaqTime());
        value.setInitialValue(bean.isFromInitialSnapshot());

        return value;
    }

    /**
     * Converts a {@link SupervisionRecordBean} object to a {@link SupervisionEvent}
     * 
     * @param bean the bean to convert
     * @return the converted object
     */
    public static HistorySupervisionEvent toSupervisionEvent(final SupervisionRecordBean bean) {
        bean.convertIntoLocalTimeZone();
        HistorySupervisionEventImpl result = new HistorySupervisionEventImpl(bean.getEntity(), bean.getId(),
                bean.getStatus(), bean.getDate(), bean.getMessage());
        result.setInitialValue(bean.isInitialValue());
        return result;
    }

    /** hidden constructor, utility class */
    private BeanConverterUtil() {
    }
}
