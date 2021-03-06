package org.gbif.d1.mn.rest.provider;

import org.gbif.d1.mn.auth.CertificateUtils;

import java.lang.reflect.Type;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import com.google.common.collect.ImmutableList;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.types.v1.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A provider of sessions which are built from the certificate presented with client requests.
 */
@Provider
public final class SessionProvider implements InjectableProvider<Authenticate, Type> {

  /**
   * This exists only because we need to surface the detail code in the exception.
   */
  private class SessionInjectable implements Injectable<Session> {

    private final HttpServletRequest request;

    private SessionInjectable(HttpServletRequest request) {
      this.request = request;
    }

    /**
     * Provides the new session built from the certificate in the request, or throws an exception.
     * 
     * @throws InvalidToken Should it be impossible to create a session from the given request
     * @throws NullPointerException If the request is null
     */
    @Override
    public Session getValue() {
      Session session = certificateUtils.newSession(request);
      if (session != null) { // cannot be null today, but future proof
        LOG.info("Successfully authenticated subject[{}]", session.getSubject());
      }
      return session;
    }
  }

  private static final Logger LOG = LoggerFactory.getLogger(SessionProvider.class);

  @Context
  private HttpServletRequest request;

  private final CertificateUtils certificateUtils;

  // not for others to instantiate
  private SessionProvider(List<String> extensionOIDs) {
    certificateUtils = CertificateUtils.newInstance(extensionOIDs);
  }

  public static SessionProvider newWithDeclaredOIDs(List<String> extensionOIDs) {
    return new SessionProvider(extensionOIDs);
  }

  public static SessionProvider newWithDefaults() {
    return new SessionProvider(ImmutableList.<String>of());
  }

  @Override
  public Injectable<Session> getInjectable(ComponentContext ic, Authenticate a, Type c) {
    if (c.equals(Session.class)) {
      return new SessionInjectable(request);
    }
    return null;
  }

  @Override
  public ComponentScope getScope() {
    return ComponentScope.PerRequest;
  }
}
