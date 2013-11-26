package cern.c2mon.daq.common.messaging.impl;
import static org.junit.Assert.*;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.common.messaging.impl.SourceCommandExecutor;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.tim.shared.daq.command.SourceCommandTagReport;
import cern.tim.shared.daq.command.SourceCommandTagValue;

public class SourceCommandExecutorTest {

    private SourceCommandTagValue command;
    private ICommandRunner commandRunner;

    @Before
    public void setUp() {
        command = new SourceCommandTagValue(1L, "name", 1L, (short) 1, "asd", "Boolean");
        commandRunner = createMock(ICommandRunner.class);
    }
    
    @Test
    public void testCallSuccess() throws InterruptedException, EqCommandTagException {
        SourceCommandExecutor executor = new SourceCommandExecutor(commandRunner, command);
        commandRunner.runCommand(command);
        expectLastCall().andReturn("");
        replay(commandRunner);
        executor.start();
        Thread.sleep(100);
        assertEquals(SourceCommandTagReport.STATUS_OK, executor.getSourceCommandTagReport().getStatus());
        assertNull(executor.getSourceCommandTagReport().getDescription());
        verify(commandRunner);
    }
    
    @Test
    public void testCallSuccessReturnHello() throws InterruptedException, EqCommandTagException {
        SourceCommandExecutor executor = new SourceCommandExecutor(
                new ICommandRunner() {
                    @Override
                    public String runCommand(SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
                        return "hello";
                    }
                }, command);
        executor.start();
        Thread.sleep(10);
        assertTrue(executor.getSourceCommandTagReport().getStatus() == SourceCommandTagReport.STATUS_OK);
        assertEquals(executor.getSourceCommandTagReport().getReturnValue(), "hello");
    }
    
    @Test
    public void testCallNoResponse() throws InterruptedException {
        SourceCommandExecutor executor = new SourceCommandExecutor(
                new ICommandRunner() {
                    @Override
                    public String runCommand(SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
                       try {
                        Thread.sleep(1000000000L);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return "hello";
                    }
                }, command);
        executor.start();
        Thread.sleep(10);
        assertTrue(executor.getSourceCommandTagReport().getStatus() == SourceCommandTagReport.STATUS_NOK_TIMEOUT);
    }
    
    @Test
    public void testException() throws InterruptedException {
        SourceCommandExecutor executor = new SourceCommandExecutor(
                new ICommandRunner() {
                    @Override
                    public String runCommand(SourceCommandTagValue sourceCommandTagValue) throws EqCommandTagException {
                       throw new RuntimeException();
                    }
                }, command);
        executor.start();
        Thread.sleep(50);
        assertTrue(executor.getSourceCommandTagReport().getStatus() == SourceCommandTagReport.STATUS_NOK_FROM_EQUIPMENTD);
    }
}
