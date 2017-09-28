/*******************************************************************************
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
 ******************************************************************************/

package cern.c2mon.server.elasticsearch.tag.config;

import cern.c2mon.server.elasticsearch.client.ElasticsearchClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cern.c2mon.server.common.listener.ConfigurationEventListener;
import cern.c2mon.server.common.tag.Tag;
import cern.c2mon.shared.client.configuration.ConfigConstants.Action;

/**
 * Listens for tag configuration events, converts them to
 * {@link TagConfigDocument} instances and forwards them for indexing.
 *
 * @author Szymon Halastra
 */
@Slf4j
@Component
public class TagConfigDocumentListener implements ConfigurationEventListener {

  private final ElasticsearchClient elasticsearchClient;

  private final TagConfigDocumentIndexer indexer;

  private final TagConfigDocumentConverter converter;

  @Autowired
  public TagConfigDocumentListener(final ElasticsearchClient elasticsearchClient, final TagConfigDocumentIndexer indexer, final TagConfigDocumentConverter converter) {
    this.elasticsearchClient = elasticsearchClient;
    this.indexer = indexer;
    this.converter = converter;
  }

  @Override
  public void onConfigurationEvent(Tag tag, Action action) {
    if (this.elasticsearchClient.getProperties().isEnabled()) {
      try {
        switch (action) {
          case CREATE:
            converter.convert(tag).ifPresent(indexer::indexTagConfig);
            break;
          case UPDATE:
            converter.convert(tag).ifPresent(indexer::updateTagConfig);
            break;
          case REMOVE:
            converter.convert(tag).ifPresent(indexer::removeTagConfig);
            break;
          default:
            break;
        }
      } catch (Exception e) {
        throw new RuntimeException("Error indexing tag configuration", e);
      }
    }
  }
}
