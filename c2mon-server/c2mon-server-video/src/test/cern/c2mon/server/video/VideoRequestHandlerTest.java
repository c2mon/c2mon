package cern.c2mon.server.video;

import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import cern.c2mon.shared.video.VideoConnectionProperties;
import cern.c2mon.shared.video.VideoRequest;
import cern.tim.shared.client.command.RbacAuthorizationDetails;
import cern.tim.util.json.GsonFactory;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:cern/c2mon/server/video/config/server-video-test.xml" })
public class VideoRequestHandlerTest {

  private final String VIDEO_SYSTEM_NAME = "MANOS";

  /** Json message serializer/deserializer */
  private static final Gson GSON = GsonFactory.createGson();

  @Autowired
  private VideoConnectionMapper videoConnectionMapper;    

  private VideoRequestHandler requestHandler;

  @Before
  public void setUp() {

    requestHandler = new VideoRequestHandler(videoConnectionMapper);
  }

  @Test
  public void testConnectionPropertiesRequest() throws SQLException {

    VideoRequest request = new VideoRequest(VIDEO_SYSTEM_NAME, VideoConnectionProperties.class);

    String response = requestHandler.handleVideoRequest(request);
    System.out.println("testConnectionPropertiesRequest requestHandler:" + response);

    Type collectionType = new TypeToken<Collection<VideoConnectionProperties>>() { } .getType();
    Collection<VideoConnectionProperties> vcpList = GSON.fromJson(response, collectionType);

    assert (vcpList.size() == 2); // the query must return 2 valid VCP's (defined in data.sql)

    Iterator iter = vcpList.iterator();
    while (iter.hasNext()) {

      Object o = iter.next();

      assert (o instanceof VideoConnectionProperties);
      if (o instanceof VideoConnectionProperties) {

        System.out.println(((VideoConnectionProperties) o).getLogin());
      }
    }
  }  

  @Test
  public void testAuthorizationDetailsRequest() throws SQLException {

    VideoRequest request = new VideoRequest(VIDEO_SYSTEM_NAME, RbacAuthorizationDetails.class);

    String response = requestHandler.handleVideoRequest(request);

    System.out.println("requestHandler:" + response);

    RbacAuthorizationDetails details = GSON.fromJson(response, RbacAuthorizationDetails.class);

    assert (details.getRbacDevice().equals("DEVICE3"));
    assert (details.getRbacProperty().equals("PROPERTY3"));
    assert (details.getRbacClass().equals("Class3"));
  }  
}
