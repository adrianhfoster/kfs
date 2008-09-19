<%--
 Copyright 2005-2007 The Kuali Foundation.
 
 Licensed under the Educational Community License, Version 1.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.opensource.org/licenses/ecl1.php
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
--%>

<%@ include file="/jsp/kfs/kfsTldHeader.jsp"%>
<%@ tag description="render the capital edit tag that contains the given capital asset info"%>

<c:set var="capitalAssetInfo" value="${KualiForm.document.capitalAssetInformation}" />
<c:set var="capitalAssetInfoName" value="document.capitalAssetInformation" />

<c:set var="newCapitalAssetInfo" value="${KualiForm.capitalAssetInformation}" />	
<c:set var="newCapitalAssetInfoName" value="capitalAssetInformation" />

<kul:tab tabTitle="Capital Edit" defaultOpen="false" tabErrorKey="${KFSConstants.EDIT_CAPITAL_ASSET_INFORMATION_ERRORS}" >
     <div class="tab-container" align="center">     
	 <h3>Capital Asset Information</h3>
	 
	 <c:choose>
	 	<c:when test="${not empty capitalAssetInfo}">
	 		<fin:capitalAssetInfo capitalAssetInfo="${capitalAssetInfo}" capitalAssetInfoName="${capitalAssetInfoName}"/>
	 	</c:when>
	 	<c:otherwise>
	 		<fin:capitalAssetInfo capitalAssetInfo="${newCapitalAssetInfo}" capitalAssetInfoName="${newCapitalAssetInfoName}"/>
	 	</c:otherwise>
	 </c:choose>
    </div>	
</kul:tab>	 