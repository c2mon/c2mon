/**
 * This package contains the interfaces used by the ConfigurationLoader
 * bean for applying individual configuration elements. The impementations
 * can be found in the .impl subpackage.
 * 
 * <p>All "create", "update" and "remove" methods will run within a DB
 * transaction (propagation REQUIRED). Runtime exceptions will cause a 
 * transaction rollback and all concerned cache elements will be removed
 * from the cache and reloaded from the DB at the next access.
 * 
 * <p>In general, a configuration is split into configuration elements. In
 * terms of transactions, each element runs in it's own transaction and
 * is either entirely or not at all applied.
 * 
 * <p>Notice the interfaces are required for the Spring AOP transactional
 * proxy to work (see Spring docs).
 */
package cern.c2mon.server.configuration.handler;