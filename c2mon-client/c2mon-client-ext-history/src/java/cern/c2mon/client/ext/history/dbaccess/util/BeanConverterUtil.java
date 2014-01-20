/******************************************************************************
 * This file is part of the Technical Infrastructure Monitoring (TIM) project. See http://ts-project-tim.web.cern.ch
 * Copyright (C) 2004 - 2011 CERN This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA. Author: TIM team, tim.support@cern.ch
 *****************************************************************************/
package cern.c2mon.client.ext.history.dbaccess.util;

import java.sql.Timestamp;

import org.apache.log4j.Logger;

import cern.c2mon.client.common.tag.ClientDataTagValue;
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
    private static final Logger LOG = Logger.getLogger(BeanConverterUtil.class);

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
     * @param clientDataTagRequestCallback Callback to get access to attributes in the {@link ClientDataTagValue}.
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
