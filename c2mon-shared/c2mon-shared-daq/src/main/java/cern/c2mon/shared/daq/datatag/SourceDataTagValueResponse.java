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


package cern.c2mon.shared.daq.datatag;


import java.util.ArrayList;
import java.util.Collection;

import cern.c2mon.shared.daq.messaging.DAQResponse;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cern.c2mon.shared.common.datatag.DataTagValueUpdate;


/**
 * This is a wrapper class for DataTagValueUpdate messages used inside reponse
 * messages for the DataTagValueUpdate requests
 *
 * imported as-is from TIM1
 */
@Data
public class SourceDataTagValueResponse implements DAQResponse {

  private static final Logger LOG = LoggerFactory.getLogger(SourceDataTagValueRequest.class);

  private String status;

  private String errorMessage;

  private ArrayList<DataTagValueUpdate> dataTagValueUpdates = new ArrayList();

  public static final String STATUS_OK = "OK";
  public static final String STATUS_EXECUTION_FAILED = "FAILED";

  public SourceDataTagValueResponse() {
  }

  public SourceDataTagValueResponse(final DataTagValueUpdate pDataTagVAlueUpdate) {
    this.dataTagValueUpdates.add(pDataTagVAlueUpdate);
    this.status = STATUS_OK;
  }


  public SourceDataTagValueResponse(final Collection pDataTagVAlueUpdates) {
    this.dataTagValueUpdates = new ArrayList(pDataTagVAlueUpdates);
    this.status = STATUS_OK;
  }


  public SourceDataTagValueResponse(final String pExecutionErrorMessage) {
    this.status = STATUS_EXECUTION_FAILED;
    this.errorMessage = pExecutionErrorMessage;
  }

  @JsonIgnore
  @Deprecated
  public void addDataTagValueUpdate(final DataTagValueUpdate pDataTagValueUpdate) {

  }

  @JsonIgnore
  public boolean isStatusOK() {
    if (this.status.equals(STATUS_OK))
      return true;
    else
      return false;
  }


  public String getErrorMessage() {
    return this.errorMessage;
  }

  @JsonIgnore
  @Deprecated
  public DataTagValueUpdate getDataTagValueUpdate(int index) {
    DataTagValueUpdate dtvUpdate = null;
    try {
      dtvUpdate = this.dataTagValueUpdates.get(index);
    } catch (Exception ex) {
    }

    return dtvUpdate;
  }

  /**
   * returns a collection of DataTagValueUpdate objects
   *
   * @return
   */
  @JsonIgnore
  @Deprecated
  public Collection getAllDataTagValueUpdatesObjects() {
    return this.dataTagValueUpdates;
  }


  /**
   * returns the number of DataTagValueUpdate objects iside the wrapper
   *
   * @return
   */
  @JsonIgnore
  @Deprecated
  public int getDataTagValueUpdatesCount() {
    return dataTagValueUpdates.size();
  }


  /**
   * returns the overal number of DataTagValue objects in the wrapper
   *
   * @return
   */
  @JsonIgnore
  @Deprecated
  public int getDataTagValueUpdateTagsCount() {
    int counter = 0;

    for (int i = 0; i < this.dataTagValueUpdates.size(); i++)
      counter += ((DataTagValueUpdate) dataTagValueUpdates.get(i)).getValues().size();

    return counter;
  }


  /**
   * returns a collection of DataTagValue objects
   *
   * @return
   */
  @JsonIgnore
  public Collection getAllDataTagValueObjects() {
    Collection result = new ArrayList();

    for (int i = 0; i < this.dataTagValueUpdates.size(); i++) {
      result.addAll(((DataTagValueUpdate) dataTagValueUpdates.get(i)).getValues());
    }

    return result;
  }
}
