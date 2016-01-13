/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p/>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p/>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p/>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.server.eslog.logger;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.common.io.stream.DataOutputStreamOutput;
import org.elasticsearch.common.io.stream.InputStreamStreamInput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Alban Marguet
 */
@Service
@Slf4j
public class ESPersistenceManager {
  private File backup;
  private StreamOutput output;
  private StreamInput input;
  private int size = 0;
  @Getter
  private boolean dataInBackup;
  @Getter
  private boolean setup;

  public void setupBackup(String filePath) {
    try {
      this.backup = new File(filePath);
      this.output =  new DataOutputStreamOutput(new DataOutputStream(new FileOutputStream(backup)));
      this.input = new InputStreamStreamInput(new DataInputStream(new FileInputStream(backup)));
      this.setup = true;
    }
    catch (FileNotFoundException e) {
      log.warn("ESPersistenceManager() - Backup file not found!");
    }
    catch (NullPointerException e) {
      log.warn("ESPersistenceManager() - No backup file found -> no backup mechanism enabled!");
    }
  }

  public void launchFallBackMechanism(BulkRequest request) {
    if (setup) {
      saveDataToFile(request);
    }
    else {
      log.info("launchFallBackMechanism() - Backup file not reachable.");
    }
  }

  private void saveDataToFile(BulkRequest request) {
    size = size + request.requests().size();
    for (ActionRequest action : request.requests()) {
      try {
        action.writeTo(output);
        dataInBackup = true;
      }
      catch (IOException e) {
        log.warn("saveDataToFile() - OutputStream to backup file has IO problems.");
      }
      catch (NullPointerException e) {
        log.warn("saveDataToFile() - No backup file has been found, unable to save data.");
      }
    }
  }

  public List<IndexRequest> retrieveBackupData() {
    List<IndexRequest> backUpRequests = new ArrayList<>();
    if (dataInBackup) {
      try {
        IndexRequest action;
        for (int i = 0; i < size; i++) {
          action = new IndexRequest();
          action.readFrom(input);
          backUpRequests.add(action);
        }
        size = 0;
        resetFile();
      }
      catch (NullPointerException e) {
        log.info("retrieveBackupData() - End of file.");
      }
      catch (EOFException e) {
        log.warn("retrieveBackupData() - Trying to read not existing data.");
      }
      catch (IOException e) {
        log.warn("retrieveBackupData() - Backup file has IO problems.");
      }
    }
    return backUpRequests;
  }

  private void resetFile() {
    try {
      new PrintWriter(backup).close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }
}