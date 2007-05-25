<%--
 Copyright 2006 The Kuali Foundation.
 
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

<c:if test="${KualiForm.canPrintCoverSheet}">
   <div align="center">
        <a href='financialDisbursementVoucher.do?methodToCall=printDisbursementVoucherCoverSheet&<c:out value="${Constants.FINANCIAL_DOCUMENT_NUMBER}"/>=<c:out value="${KualiForm.document.documentNumber}"/>'>
            <font color="red"><bean:message key="label.document.disbursementVoucher.printCoverSheet"/></font>
        </a>
        <html:img src="${ConfigProperties.kr.externalizable.images.url}icon-pdf.png" title="print cover sheet" alt="print cover sheet" width="16" height="16"/>
   </div>
   <br>
</c:if>
