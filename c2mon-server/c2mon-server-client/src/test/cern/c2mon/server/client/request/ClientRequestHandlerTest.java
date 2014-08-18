package cern.c2mon.server.client.request;

import java.util.ArrayList;
import java.util.List;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.command.ActiveMQTextMessage;
import org.easymock.EasyMock;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.server.cache.DeviceFacade;
import cern.c2mon.server.common.device.Device;
import cern.c2mon.server.common.device.DeviceCacheObject;
import cern.c2mon.server.test.broker.TestBrokerService;
import cern.c2mon.shared.client.device.DeviceClassNameResponse;
import cern.c2mon.shared.client.device.TransferDevice;
import cern.c2mon.shared.client.request.ClientRequestImpl;

/**
 * @author Justin Lewis Salmon
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "classpath:cern/c2mon/server/client/config/server-client-requesthandler-test.xml" })
public class ClientRequestHandlerTest {

  /** Component to test */
  @Autowired
  ClientRequestHandler clientRequestHandler;

  /** Mocked components */
  @Autowired
  DeviceFacade deviceFacadeMock;

  @Value("${jms.client.request.queue}")
  private String requestQueue;

  private static TestBrokerService testBrokerService = new TestBrokerService();

  private ApplicationContext applicationContext;

  @BeforeClass
  public static void startJmsBroker() throws Exception {
    testBrokerService.createAndStartBroker();
  }

  @AfterClass
  public static void stopBroker() throws Exception {
    testBrokerService.stopBroker();
  }

  @Test
  public void testHandleDeviceClassNamesRequest() throws JMSException {
    // TODO: turn this into an integration test using the actual JMS
    // infrastructure

    // Reset the mock
    EasyMock.reset(deviceFacadeMock);

    Session session = testBrokerService.getConnectionFactory().createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);

    Destination replyDestination = session.createTemporaryQueue();
    TextMessage message = new ActiveMQTextMessage();
    message.setJMSReplyTo(replyDestination);

    ClientRequestImpl<DeviceClassNameResponse> request = new ClientRequestImpl<>(DeviceClassNameResponse.class);
    message.setText(request.toJson());

    List<String> classNames = new ArrayList<>();
    classNames.add("test_device_class_name_1");
    classNames.add("test_device_class_name_2");

    // Expect the request handler to delegate and get class names from the
    // device cache
    EasyMock.expect(deviceFacadeMock.getDeviceClassNames()).andReturn(classNames);

    // Setup is finished, need to activate the mock
    EasyMock.replay(deviceFacadeMock);

    // Pass in a dummy message
    clientRequestHandler.onMessage(message, session);

    // Verify that everything happened as expected
    EasyMock.verify(deviceFacadeMock);
  }

  @Test
  public void testHandleDeviceRequest() throws JMSException {
    // TODO: turn this into an integration test using the actual JMS
    // infrastructure

    // Reset the mock
    EasyMock.reset(deviceFacadeMock);

    Session session = testBrokerService.getConnectionFactory().createConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);

    Destination replyDestination = session.createTemporaryQueue();
    TextMessage message = new ActiveMQTextMessage();
    message.setJMSReplyTo(replyDestination);

    ClientRequestImpl<TransferDevice> request = new ClientRequestImpl<>(TransferDevice.class);
    request.setRequestParameter("test_device_class_name_1");
    message.setText(request.toJson());

    Long deviceClassId = 1L;
    List<Device> devices = new ArrayList<>();
    Device device1 = new DeviceCacheObject(1000L, "test_device_1", deviceClassId);
    Device device2 = new DeviceCacheObject(2000L, "test_device_2", deviceClassId);
    devices.add(device1);
    devices.add(device2);

    // Expect the request handler to look up the device class ID
    EasyMock.expect(deviceFacadeMock.getDevices("test_device_class_name_1")).andReturn(devices);

    // Setup is finished, need to activate the mock
    EasyMock.replay(deviceFacadeMock);

    // Pass in a dummy message
    clientRequestHandler.onMessage(message, session);

    // Verify that everything happened as expected
    EasyMock.verify(deviceFacadeMock);
  }

}
