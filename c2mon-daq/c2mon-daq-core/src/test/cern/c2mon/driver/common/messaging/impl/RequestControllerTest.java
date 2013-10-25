package cern.c2mon.driver.common.messaging.impl;

import static org.easymock.classextension.EasyMock.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import cern.c2mon.driver.common.ICommandRunner;
import cern.c2mon.driver.common.conf.core.ConfigurationController;
import cern.c2mon.driver.common.conf.core.EquipmentConfiguration;
import cern.c2mon.driver.common.conf.core.ProcessConfiguration;
import cern.c2mon.driver.common.conf.core.RunOptions;
import cern.c2mon.driver.common.messaging.impl.RequestController;
import cern.c2mon.driver.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.common.datatag.DataTagAddress;
import cern.tim.shared.daq.command.SourceCommandTag;
import cern.tim.shared.daq.command.SourceCommandTagValue;
import cern.tim.shared.daq.config.Change;
import cern.tim.shared.daq.config.ChangeReport;
import cern.tim.shared.daq.config.CommandTagAdd;
import cern.tim.shared.daq.config.CommandTagRemove;
import cern.tim.shared.daq.config.CommandTagUpdate;
import cern.tim.shared.daq.config.DataTagAdd;
import cern.tim.shared.daq.config.DataTagRemove;
import cern.tim.shared.daq.config.DataTagUpdate;
import cern.tim.shared.daq.config.EquipmentConfigurationUpdate;
import cern.tim.shared.daq.config.ProcessConfigurationUpdate;
import cern.tim.shared.daq.datatag.SourceDataTag;
import cern.tim.shared.daq.datatag.SourceDataTagValueRequest;
import cern.tim.shared.daq.datatag.SourceDataTagValueResponse;

public class RequestControllerTest {
    private ConfigurationController configurationControllerMock;
    
    @Before
    public void setUp() {
        Class<ConfigurationController> clazz = ConfigurationController.class;
        configurationControllerMock = createMock(clazz, clazz.getMethods());
    }
    
    @Test
    public void testApplyChange() {
        RequestController requestController = new RequestController(configurationControllerMock);
        List<Change> changes = new ArrayList<Change>();
        /*
         *  Values in this case don't matter.
         *  Just add to the configuration controller and check 
         *  if the MessageHandler does the right thing.
         */
        DataTagAdd dataTagAdd = new DataTagAdd();
        changes.add(dataTagAdd);
        configurationControllerMock.onDataTagAdd(dataTagAdd);
        expectLastCall().andReturn(new ChangeReport(1L));
        
        DataTagRemove dataTagRemove = new DataTagRemove();
        changes.add(dataTagRemove);
        configurationControllerMock.onDataTagRemove(dataTagRemove);
        expectLastCall().andReturn(new ChangeReport(1L));
        
        DataTagUpdate dataTagUpdate = new DataTagUpdate();
        changes.add(dataTagUpdate);
        configurationControllerMock.onDataTagUpdate(dataTagUpdate);
        expectLastCall().andReturn(new ChangeReport(1L));
        
        CommandTagAdd commandTagAdd = new CommandTagAdd();
        changes.add(commandTagAdd);
        configurationControllerMock.onCommandTagAdd(commandTagAdd);
        expectLastCall().andReturn(new ChangeReport(1L));
        
        CommandTagRemove commandTagremove = new CommandTagRemove();
        changes.add(commandTagremove);
        configurationControllerMock.onCommandTagRemove(commandTagremove);
        expectLastCall().andReturn(new ChangeReport(1L));
        
        CommandTagUpdate commandTagUpdate = new CommandTagUpdate();
        changes.add(commandTagUpdate);
        configurationControllerMock.onCommandTagUpdate(commandTagUpdate);
        expectLastCall().andReturn(new ChangeReport(1L));
        
        EquipmentConfigurationUpdate equipmentConfigurationUpdate = new EquipmentConfigurationUpdate();
        changes.add(equipmentConfigurationUpdate);
        configurationControllerMock.onEquipmentConfigurationUpdate(equipmentConfigurationUpdate);
        expectLastCall().andReturn(new ChangeReport(1L));
        
        ProcessConfigurationUpdate processConfigurationUpdate = new ProcessConfigurationUpdate();
        changes.add(processConfigurationUpdate);
        configurationControllerMock.onProcessConfigurationUpdate(processConfigurationUpdate);
        expectLastCall().andReturn(new ChangeReport(1L));
        
        replay(configurationControllerMock);
        for (Change change : changes) {
            requestController.applyChange(change);
        }
        verify(configurationControllerMock);
    }
    
    @Test
    public void testExecuteCommand() throws EqCommandTagException {
        ConfigurationController configurationController = getBasicConfigurationController();
        
        ICommandRunner commandRunner = createMock(ICommandRunner.class);
        RequestController requestController = new RequestController(configurationController);
        requestController.putCommandRunner(1L, commandRunner);
        
        SourceCommandTagValue sourceCommandTagValue = new SourceCommandTagValue(1L, null, 1L, (short) 0, null, null);
        // MessageHandler should try to call this.
        commandRunner.runCommand(sourceCommandTagValue);
        expectLastCall().andReturn("");
        
        replay(commandRunner);
        requestController.executeCommand(sourceCommandTagValue);
        verify(commandRunner);
    }

    @Test
    public void testOnSourceDataTagValueUpdateRequestProcess() {
        ConfigurationController configurationController = getBasicConfigurationController();
        
        RequestController handler = new RequestController(configurationController);
        SourceDataTagValueRequest valueRequest = 
            new SourceDataTagValueRequest(SourceDataTagValueRequest.TYPE_PROCESS, 1L);
        
        SourceDataTagValueResponse response = 
            handler.onSourceDataTagValueUpdateRequest(valueRequest);
        
        assertTrue(response.getAllDataTagValueObjects().size() == 3);
    }
    
    @Test
    public void testOnSourceDataTagValueUpdateRequestWrongProcessId() {
        ConfigurationController configurationController = getBasicConfigurationController();
        
        RequestController handler = new RequestController(configurationController);
        SourceDataTagValueRequest valueRequest = 
            new SourceDataTagValueRequest(SourceDataTagValueRequest.TYPE_PROCESS, 2L);
        
        SourceDataTagValueResponse response = 
            handler.onSourceDataTagValueUpdateRequest(valueRequest);
        
        assertTrue(response.getAllDataTagValueObjects().size() == 0);
        assertFalse(response.isStatusOK());
        assertTrue(response.getErrorMessage().contains("does not have id: " + 2));
    }

    @Test
    public void testOnSourceDataTagValueUpdateRequestEquipment() {
        ConfigurationController configurationController = getBasicConfigurationController();
        
        RequestController handler = new RequestController(configurationController);
        SourceDataTagValueRequest valueRequest = 
            new SourceDataTagValueRequest(SourceDataTagValueRequest.TYPE_EQUIPMENT, 2L);
        
        SourceDataTagValueResponse response = 
            handler.onSourceDataTagValueUpdateRequest(valueRequest);
        
        assertTrue(response.getAllDataTagValueObjects().size() == 2);
    }
    
    @Test
    public void testOnSourceDataTagValueUpdateRequestEquipmentWrongId() {
        ConfigurationController configurationController = getBasicConfigurationController();
        
        RequestController handler = new RequestController(configurationController);
        SourceDataTagValueRequest valueRequest = 
            new SourceDataTagValueRequest(SourceDataTagValueRequest.TYPE_EQUIPMENT, 20L);
        
        SourceDataTagValueResponse response = 
            handler.onSourceDataTagValueUpdateRequest(valueRequest);
        
        assertTrue(response.getAllDataTagValueObjects().size() == 0);
        assertFalse(response.isStatusOK());
        assertTrue(response.getErrorMessage().contains("does not have equipment with id: " + 20L));
    }

    @Test
    public void testOnSourceDataTagValueUpdateRequestDataTag() {
        ConfigurationController configurationController = getBasicConfigurationController();
        
        RequestController handler = new RequestController(configurationController);
        SourceDataTagValueRequest valueRequest = 
            new SourceDataTagValueRequest(SourceDataTagValueRequest.TYPE_DATATAG, 1L);
        
        SourceDataTagValueResponse response = 
            handler.onSourceDataTagValueUpdateRequest(valueRequest);
        
        assertTrue(response.getAllDataTagValueObjects().size() == 1);
    }
    
    @Test
    public void testOnSourceDataTagValueUpdateRequestDataTagWrongId() {
        ConfigurationController configurationController = getBasicConfigurationController();
        
        RequestController handler = new RequestController(configurationController);
        SourceDataTagValueRequest valueRequest = 
            new SourceDataTagValueRequest(SourceDataTagValueRequest.TYPE_DATATAG, 20L);
        
        SourceDataTagValueResponse response = 
            handler.onSourceDataTagValueUpdateRequest(valueRequest);
        
        assertTrue(response.getAllDataTagValueObjects().size() == 0);
        assertFalse(response.isStatusOK());
        assertTrue(response.getErrorMessage().contains(" does not have a data tag with id: " + 20L));
    }
    
    /**
     * @return configurationController
     */
    private ConfigurationController getBasicConfigurationController() {
      // We need run options for being use with fir the new PIK (default is we send the PIK)
      RunOptions runOptions = new RunOptions();

      ConfigurationController configurationController = new ConfigurationController(runOptions, null);
      ProcessConfiguration processConfiguration = new ProcessConfiguration();
      EquipmentConfiguration equipmentConfiguration = new EquipmentConfiguration();
      EquipmentConfiguration equipmentConfiguration2 = new EquipmentConfiguration();
      SourceCommandTag commandTag = new SourceCommandTag(1L, "hello");
      DataTagAddress address = new DataTagAddress();
      SourceDataTag sourceDataTag = new SourceDataTag(1L, "asd", false, (short)0, "Integer", address);
      SourceDataTag sourceDataTag2 = new SourceDataTag(2L, "asd", false, (short)0, "Integer", address);
      SourceDataTag sourceDataTag3 = new SourceDataTag(3L, "asd", false, (short)0, "Integer", address);
      processConfiguration.setProcessID(1L);
      equipmentConfiguration.setId(1L);
      sourceDataTag.update(25);
      sourceDataTag2.update(25);
      sourceDataTag3.update(25);
      configurationController.setProcessConfiguration(processConfiguration);
      processConfiguration.getEquipmentConfigurations().put(1L, equipmentConfiguration);
      processConfiguration.getEquipmentConfigurations().put(2L, equipmentConfiguration2);
      equipmentConfiguration.getCommandTags().put(1L, commandTag);
      equipmentConfiguration.getDataTags().put(1L, sourceDataTag);
      equipmentConfiguration2.getDataTags().put(2L, sourceDataTag2);
      equipmentConfiguration2.getDataTags().put(3L, sourceDataTag3);
      return configurationController;
    }
}
