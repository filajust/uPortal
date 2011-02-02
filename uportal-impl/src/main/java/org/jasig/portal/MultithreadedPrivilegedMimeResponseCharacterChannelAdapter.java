/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal;

/**
 * Internal adapter for a multithreaded character channel that is also privileged.
 * @author  <a href="mailto:nbolton@unicon.net">Nick Bolton</a>
 * @version $Revision$
 * @see MultithreadedChannelAdapter
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class MultithreadedPrivilegedMimeResponseCharacterChannelAdapter extends
MultithreadedMimeResponseCharacterChannelAdapter implements IPrivilegedChannel {
    public MultithreadedPrivilegedMimeResponseCharacterChannelAdapter(IMultithreadedCharacterChannel channel, String uid)
    throws PortalException {
        super(channel, uid);
    }
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        ((IMultithreadedPrivileged)channel).setPortalControlStructures(pcs,uid);
    }
}