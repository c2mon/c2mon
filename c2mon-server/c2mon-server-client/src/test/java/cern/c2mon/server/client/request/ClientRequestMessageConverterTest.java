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
package cern.c2mon.server.client.request;

import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.command.CommandReport;
import cern.c2mon.shared.client.command.CommandTagHandle;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.process.ProcessNameResponse;
import cern.c2mon.shared.client.request.ClientRequest;
import cern.c2mon.shared.client.request.ClientRequestImpl;
import cern.c2mon.shared.client.request.ClientRequestResult;
import cern.c2mon.shared.client.request.JsonRequest;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.client.tag.TagUpdate;
import cern.c2mon.shared.client.tag.TagValueUpdate;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.junit.Test;

import javax.jms.JMSException;
import javax.jms.TextMessage;

import static cern.c2mon.shared.client.request.ClientRequest.RequestType;
import static cern.c2mon.shared.client.request.ClientRequest.ResultType;
import static junit.framework.Assert.*;

public class ClientRequestMessageConverterTest {

  @Test
  public void testSupervisionMessageConversion() {
    requestAndAssert(SupervisionEvent.class, RequestType.SUPERVISION_REQUEST, ResultType.SUPERVISION_EVENT_LIST);
  }

  @Test
  public void testActiveAlarmsMessageConversion() {
    JsonRequest<AlarmValue> request = new ClientRequestImpl<>(
        ResultType.TRANSFER_ACTIVE_ALARM_LIST,
        RequestType.ACTIVE_ALARMS_REQUEST,
        10000);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertSame(receivedRequest.getRequestType(), RequestType.ACTIVE_ALARMS_REQUEST);
      assertSame(receivedRequest.getResultType(), ResultType.TRANSFER_ACTIVE_ALARM_LIST);
      assertEquals(10000, receivedRequest.getTimeout());
    }
    catch (JMSException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testCommandTagHandleMessageConversion() {
    JsonRequest<CommandTagHandle> request = new ClientRequestImpl<>(CommandTagHandle.class);

    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertSame(receivedRequest.getRequestType(), RequestType.COMMAND_HANDLE_REQUEST);
      assertSame(receivedRequest.getResultType(), ResultType.TRANSFER_COMMAND_HANDLES_LIST);
      assertTrue(receivedRequest.requiresObjectResponse());
    }
    catch (JMSException e) {
      fail(e.getMessage());
    }
  }

  @Test
  public void testTransferTagMessageConversion() {
    requestAndAssert(TagUpdate.class, RequestType.TAG_REQUEST, ResultType.TRANSFER_TAG_LIST);
  }


  @Test
  public void testTransferTagValueMessageConversion() {
    requestAndAssert(TagValueUpdate.class, RequestType.TAG_REQUEST, ResultType.TRANSFER_TAG_VALUE_LIST);
  }

  @Test
  public void testAlarmValueMessageConversion() {
    requestAndAssert(AlarmValue.class, RequestType.ALARM_REQUEST, ResultType.TRANSFER_ALARM_LIST);
  }

  @Test
  public void testTagConfigMessageConversion() {
    requestAndAssert(TagConfig.class, RequestType.TAG_CONFIGURATION_REQUEST, ResultType.TRANSFER_TAG_CONFIGURATION_LIST);
  }

  @Test
  public void testConfigurationReportMessageConversion() {
    requestAndAssert(ConfigurationReport.class, RequestType.APPLY_CONFIGURATION_REQUEST, ResultType.TRANSFER_CONFIGURATION_REPORT);
  }

  @Test
  public void testExecuteCommandMessageConversion() {
    requestAndAssert(CommandReport.class,RequestType.EXECUTE_COMMAND_REQUEST, ResultType.TRANSFER_COMMAND_REPORT);
  }

  @Test
  public void testProcessNamesMessageConversion() {
    requestAndAssert(ProcessNameResponse.class,RequestType.PROCESS_NAMES_REQUEST, ResultType.TRANSFER_PROCESS_NAMES);
  }

  @Test
  public void testDeviceClassNamesMessageConversion() {
    requestAndAssert(DeviceClassNameResponse.class, RequestType.DEVICE_CLASS_NAMES_REQUEST,ResultType.TRANSFER_DEVICE_CLASS_NAMES);
  }

  @Test
  public void testDevicesMessageConversion() {
    requestAndAssert(TransferDevice.class, RequestType.DEVICE_REQUEST,ResultType.TRANSFER_DEVICE_LIST);
  }

  private <T extends ClientRequestResult> void requestAndAssert(Class<T> requestClass,
                                                                RequestType expectedRequestType,
                                                                ResultType expectedResultType) {
    ClientRequestImpl<T> request = new ClientRequestImpl<>(requestClass);
    TextMessage message = new ActiveMQTextMessage();
    try {
      message.setText(request.toJson());
      ClientRequest receivedRequest = ClientRequestMessageConverter.fromMessage(message);

      assertSame(expectedRequestType, receivedRequest.getRequestType());
      assertSame(expectedResultType, receivedRequest.getResultType());
    }
    catch (JMSException e) {
      fail(e.getMessage());
    }
  }
}
