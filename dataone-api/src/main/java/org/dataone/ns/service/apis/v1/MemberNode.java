package org.dataone.ns.service.apis.v1;

/**
 * A full Tier 4 member node.
 * 
 * @see <a href="http://mule1.dataone.org/ArchitectureDocs-current/apis/MN_APIs.html">The DataONE Member Node
 *      specification</a>
 */
public interface MemberNode extends MNRead, MNAuthorization, MNStorage, MNReplication {
}
