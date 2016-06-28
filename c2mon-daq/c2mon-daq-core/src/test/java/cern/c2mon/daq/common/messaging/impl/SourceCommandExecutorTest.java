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
package cern.c2mon.daq.common.messaging.impl;
import static org.junit.Assert.*;

import static org.easymock.EasyMock.*;

import org.junit.Before;
import org.junit.Test;

import cern.c2mon.daq.common.ICommandRunner;
import cern.c2mon.daq.common.messaging.impl.SourceCommandExecutor;
import cern.c2mon.daq.tools.equipmentexceptions.EqCommandTagException;
import cern.c2mon.shared.daq.command.SourceCommandTagReport;
import cern.c2mon.shared.daq.command.SourceCommandTagValue;

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
        Thread.sleep(20);
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
