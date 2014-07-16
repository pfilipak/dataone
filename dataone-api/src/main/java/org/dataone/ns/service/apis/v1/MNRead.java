package org.dataone.ns.service.apis.v1;

import java.io.InputStream;
import java.util.Date;

import org.dataone.ns.service.exceptions.InvalidRequest;
import org.dataone.ns.service.exceptions.InvalidToken;
import org.dataone.ns.service.exceptions.NotImplemented;
import org.dataone.ns.service.exceptions.SynchronizationFailed;
import org.dataone.ns.service.types.v1.Checksum;
import org.dataone.ns.service.types.v1.DescribeResponse;
import org.dataone.ns.service.types.v1.Event;
import org.dataone.ns.service.types.v1.Identifier;
import org.dataone.ns.service.types.v1.Log;
import org.dataone.ns.service.types.v1.Node;
import org.dataone.ns.service.types.v1.ObjectList;
import org.dataone.ns.service.types.v1.Session;
import org.dataone.ns.service.types.v1.SystemMetadata;

/**
 * Interface definition for the Tier 1 services.
 * 
 * @see <a
 *      href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html</a>
 */
public interface MNRead {

  /**
   * This method provides a lighter weight mechanism than {@link MNRead#getSystemMetadata(Session, String)} ()}
   * for a client to determine basic properties of the referenced object. The response should indicate properties that
   * are typically returned in a HTTP HEAD request: the date late modified, the size of the object, the type of the
   * object (the SystemMetadata.formatId).
   * 
   * @throws throws InvalidToken, NotAuthorized, NotImplemented,
   *         ServiceFailure, NotFound
   */
  DescribeResponse describe(Session session, Identifier pid);

  /**
   * Retrieve an object identified by id from the node.
   * 
   * @throws throws InvalidToken, NotAuthorized, NotImplemented, ServiceFailure,
   *         NotFound,
   *         InsufficientResources
   */
  InputStream get(Session session, Identifier pid);

  /**
   * Returns a document describing the capabilities of the Member Node.
   * 
   * @throws throws NotImplemented, ServiceFailure
   */
  Node getCapabilities(Session session);

  /**
   * Returns {@link Checksum} for the specified object using an accepted hashing algorithm. The result is used to
   * determine if two instances referenced by a PID are identical, hence it is necessary that MNs can ensure that the
   * returned checksum is valid for the referenced object either by computing it on the fly or by using a cached value
   * that is certain to be correct.
   * 
   * @throws InvalidRequest, InvalidToken,
   *         NotAuthorized,
   *         NotImplemented, ServiceFailure, NotFound
   */
  Checksum getChecksum(Session session, Identifier pid, String checksumAlgorithm);

  /**
   * Retrieve log information from the Member Node for the specified slice parameters. Log entries will only return
   * PIDs.
   * 
   * @throws InvalidRequest, InvalidToken, NotAuthorized, NotImplemented, ServiceFailure
   */
  Log getLogRecords(Session session, Date fromDate, Date toDate, Event event, Identifier pidFilter, Integer start,
    Integer count);

  /**
   * Called by a target Member Node to fulfill the replication request originated by a Coordinating Node calling
   * {@link MNReplication#replicate(Session, SystemMetadata, String)}. This is a request to make a replica copy
   * of the object, and differs from a call to GET /object in that it should be logged as a replication event rather
   * than a read event on that object.
   * 
   * @throws InvalidToken, NotAuthorized, NotImplemented,
   *         ServiceFailure, NotFound, InsufficientResources
   */
  InputStream getReplica(Session session, Identifier pid);

  /**
   * Describes the object identified by id by returning the associated system metadata object.
   * 
   * @throws throws NotAuthorized, NotFound, ServiceFailure,
   *         InvalidToken, InsufficientResources
   */
  SystemMetadata getSystemMetadata(Session session, Identifier pid);

  /**
   * Retrieve the list of objects present on the MN that match the calling parameters.
   * <p>
   * This method is required to support the process of Member Node synchronization. At a minimum, this method MUST be
   * able to return a list of objects that match "fromDate < SystemMetadata.dateSysMetadataModified". but is expected to
   * also support date range (by also specifying toDate), and should also support slicing of the matching set of records
   * by indicating the starting index of the response (where 0 is the index of the first item) and the count of elements
   * to be returned.
   * <p>
   * Note that date time precision is limited to one millisecond. If no timezone information is provided, the UTC will
   * be assumed.
   * <p>
   * Access control for this method MUST be configured to allow calling by Coordinating Nodes and MAY be configured to
   * allow more general access.
   * 
   * @throws InvalidRequest, InvalidToken, NotAuthorized, NotImplemented, ServiceFailure
   */
  ObjectList listObjects(Session session, Date fromDate, Date toDate, String formatId, Boolean replicaStatus,
    Integer start, Integer count);

  /**
   * Returns a human readable form of the time for easy debugging since the specification is ambiguous.
   * 
   * @throws NotImplemented, ServiceFailure, InsufficientResources
   */
  String ping(Session session);

  /**
   * This is a callback method used by a CN to indicate to a MN that it cannot complete synchronization of the science
   * metadata identified by pid. When called, the MN should take steps to record the problem description and notify an
   * administrator or the data owner of the issue.
   * 
   * @throws InvalidToken, NotAuthorized,
   *         NotImplemented,
   *         ServiceFailure
   */
  boolean synchronizationFailed(Session session, SynchronizationFailed message);
}