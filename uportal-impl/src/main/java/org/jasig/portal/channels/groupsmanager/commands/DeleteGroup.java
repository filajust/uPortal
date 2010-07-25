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

package  org.jasig.portal.channels.groupsmanager.commands;

import java.util.Iterator;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.channels.groupsmanager.Utility;
import org.jasig.portal.groups.IEntityGroup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This command deletes an IEntityGroup and removes all of it's associations.
 * It then gathers all of the xml
 * nodes for the parent group and removes the child node of the removed member.
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class DeleteGroup extends GroupsManagerCommand {

   public DeleteGroup () {
   }

   /**
    * This is the public method
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      //ChannelStaticData staticData = sessionData.staticData;
      ChannelRuntimeData runtimeData= sessionData.runtimeData;

      Utility.logMessage("DEBUG", "DeleteGroup::execute(): Start");
      Document model = getXmlDoc(sessionData);
      String delId = getCommandArg(runtimeData);
      Element delElem = GroupsManagerXML.getElementByTagNameAndId(model, GROUP_TAGNAME, delId);
      Element pn = ((Element)delElem.getParentNode());
      if (pn !=null){
       sessionData.highlightedGroupID = pn.getAttribute("id");
      }
      String delKey = delElem.getAttribute("key");
      String elemName = delElem.getAttribute("name");
      String retMsg;
      Node parentNode;
      Node deletedNode;
      Utility.logMessage("DEBUG", "DeleteGroup::execute(): Group: " + elemName + "will be deleted");
      if (Utility.areEqual(delElem.getAttribute("searchResults"), "true")){
        // if it is search results, just delete the node and skip the rest
        delElem.getParentNode().removeChild(delElem);
      }
      else{
         IEntityGroup delGroup = sessionData.lockedGroup;
         if (delGroup == null) {
            retMsg = "Unable to retrieve Group!";
            sessionData.feedback = retMsg;
            return;
         }
         Utility.logMessage("DEBUG", "DeleteGroup::execute(): About to delete group: "
               + elemName);
         // remove permissions associated with group
         deletePermissions(delGroup);
         // delete the group
         delGroup.delete();
         Utility.logMessage("DEBUG", "DeleteGroup::execute(): About to delete xml nodes for group: "
               + elemName);
         // remove all xml nodes for this group
         Iterator deletedNodes = GroupsManagerXML.getNodesByTagNameAndKey(model, GROUP_TAGNAME,
               delKey);
         IEntityGroup parentEntGrp = null;
         String hasMbrs = "duh";
         while (deletedNodes.hasNext()) {
            deletedNode = (Node)deletedNodes.next();
            parentNode = deletedNode.getParentNode();
            String nodeKey = ((Element)parentNode).getAttribute("key");
            if (parentEntGrp == null || !parentEntGrp.getKey().equals(nodeKey)) {
               if (!Utility.areEqual(nodeKey, "")){
                  parentEntGrp = GroupsManagerXML.retrieveGroup(nodeKey);
                  hasMbrs = String.valueOf(parentEntGrp.hasMembers());
                  parentNode.removeChild(deletedNode);
               }
               else{
                  //Search elements have a null "key"
                  parentNode.removeChild(deletedNode);
                  NodeList nl = parentNode.getChildNodes();
                  if (nl.getLength() > 0){
                     hasMbrs = "true";
                  }
                  else{
                     hasMbrs = "false";
                  }
               }
            }
            ((Element)parentNode).setAttribute("hasMembers", hasMbrs);
         }

         /** Remove the permission elements in the xmlDoc */
         Node principalNode = model.getDocumentElement().getElementsByTagName("principal").item(0);
         NodeList permElems = model.getElementsByTagName("permission");
         /** If we delete from the bottom up, the NodeList elements shift down
          *  everytime we delete an element. Since the elements that we are looking
          *  for are sequential and because we increment the counter at the end of
          *  the loop, the element that we should process next slips down into the
          *  slot that we just processed. We therefore end up deleting every other
          *  element. The solution is to delete from the top down.
          */
         for (int i = permElems.getLength() - 1; i > -1; i--) {
            Element permElem = (Element)permElems.item(i);
            if (permElem.getAttribute("target").equals(delKey)) {
               principalNode.removeChild(permElem);
            }
         }
         sessionData.mode=BROWSE_MODE;
      }
      Utility.logMessage("DEBUG", "DeleteGroup::execute(): Finished");
   }
}