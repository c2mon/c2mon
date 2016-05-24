package cern.c2mon.server.eslog.structure.types;

import cern.c2mon.shared.util.json.GsonFactory;
import com.google.gson.Gson;

import java.util.function.Supplier;

/**
 * Represents a provider for a single {@link Gson} instance.
 */
public enum GsonSupplier implements Supplier<Gson> {
  INSTANCE(GsonFactory.createGson());

  protected final Gson gson;

  GsonSupplier(final Gson gson) {
    this.gson = gson;
  }

  @Override
  public Gson get() {
    return this.gson;
  }

}
