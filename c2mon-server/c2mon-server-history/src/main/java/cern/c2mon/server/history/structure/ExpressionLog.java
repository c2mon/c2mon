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

package cern.c2mon.server.history.structure;

import java.sql.Timestamp;

import cern.c2mon.pmanager.IFallback;
import cern.c2mon.pmanager.fallback.exception.DataFallbackException;
import cern.c2mon.shared.client.expression.Expression;
import cern.c2mon.shared.util.json.GsonFactory;
import com.google.gson.Gson;
import lombok.Data;

/**
 * Bean which represents a Loggable {@link Expression}.
 *
 * @author Franz Ritter
 */
@Data
public class ExpressionLog implements IFallback {

  private static final Gson GSON = GsonFactory.createGson();

  private long tagId;

  private Timestamp exprTimestamp;

  private String exprName;

  private String exprValue;

  private String exprDataType;

  private String exprInfo;

  public ExpressionLog() {
  }

  public ExpressionLog(Expression expression, Long tagId, Timestamp serverTimestamp) {
    this.tagId = tagId;
    this.exprTimestamp = serverTimestamp;
    this.exprName = expression.getName();
    this.exprValue = expression.getResult() != null ? expression.getResult().toString() : "";
    this.exprDataType = expression.getDataType();
    if (expression.getMetadata() != null && expression.getMetadata().getMetadata() != null) {
      this.exprInfo = expression.getMetadata().getMetadata().get("info") != null ?
          expression.getMetadata().getMetadata().get("info").toString() : "";
    }
  }

  @Override
  public IFallback getObject(String line) throws DataFallbackException {
    return GSON.fromJson(line, ExpressionLog.class);
  }

  @Override
  public String toString() {
    return GSON.toJson(this);
  }

  public String getId() {
    return exprName;
  }

}
