/*
 * Copyright 2008 The Kuali Foundation.
 * 
 * Licensed under the Educational Community License, Version 1.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.kfs.module.ar.document.service.impl;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.ar.ArConstants;
import org.kuali.kfs.module.ar.businessobject.AccountsReceivableDocumentHeader;
import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceWriteoffLookupResult;
import org.kuali.kfs.module.ar.businessobject.OrganizationAccountingDefault;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.kfs.module.ar.document.CustomerInvoiceWriteoffDocument;
import org.kuali.kfs.module.ar.document.service.AccountsReceivableDocumentHeaderService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceDocumentService;
import org.kuali.kfs.module.ar.document.service.CustomerInvoiceWriteoffDocumentService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.businessobject.ChartOrgHolder;
import org.kuali.kfs.sys.businessobject.FinancialSystemDocumentHeader;
import org.kuali.kfs.sys.service.FinancialSystemUserService;
import org.kuali.kfs.sys.service.ParameterService;
import org.kuali.kfs.sys.service.UniversityDateService;
import org.kuali.rice.kns.service.BusinessObjectService;
import org.kuali.rice.kns.util.KualiDecimal;
import org.kuali.rice.kns.util.ObjectUtils;

public class CustomerInvoiceWriteoffDocumentServiceImpl implements CustomerInvoiceWriteoffDocumentService {

    private ParameterService parameterService;
    private UniversityDateService universityDateService;
    private FinancialSystemUserService financialSystemUserService;
    private BusinessObjectService businessObjectService;
    private AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService;
    private CustomerInvoiceDocumentService customerInvoiceDocumentService;

    /**
     * @see org.kuali.kfs.module.ar.document.service.CustomerInvoiceWriteoffDocumentService#setupDefaultValuesForNewCustomerInvoiceWriteoffDocument(org.kuali.kfs.module.ar.document.CustomerInvoiceWriteoffDocument)
     */
    public void setupDefaultValuesForNewCustomerInvoiceWriteoffDocument(CustomerInvoiceWriteoffDocument customerInvoiceWriteoffDocument) {

        // update status
        customerInvoiceWriteoffDocument.setStatusCode(ArConstants.CustomerInvoiceWriteoffStatuses.IN_PROCESS);

        // set accounts receivable document header
        AccountsReceivableDocumentHeader accountsReceivableDocumentHeader = accountsReceivableDocumentHeaderService.getNewAccountsReceivableDocumentHeaderForCurrentUser();
        accountsReceivableDocumentHeader.setDocumentNumber(customerInvoiceWriteoffDocument.getDocumentNumber());
        accountsReceivableDocumentHeader.setCustomerNumber(customerInvoiceWriteoffDocument.getCustomerInvoiceDocument().getAccountsReceivableDocumentHeader().getCustomerNumber());
        customerInvoiceWriteoffDocument.setAccountsReceivableDocumentHeader(accountsReceivableDocumentHeader);

        // if writeoffs are generated based on organization accounting default, populate those fields now
        String writeoffGenerationOption = parameterService.getParameterValue(CustomerInvoiceWriteoffDocument.class, ArConstants.GLPE_WRITEOFF_GENERATION_METHOD);
        boolean isUsingOrgAcctDefaultWriteoffFAU = ArConstants.GLPE_WRITEOFF_GENERATION_METHOD_ORG_ACCT_DEFAULT.equals(writeoffGenerationOption);
        if (isUsingOrgAcctDefaultWriteoffFAU) {

            Integer currentUniversityFiscalYear = universityDateService.getCurrentFiscalYear();
            ChartOrgHolder currentUser = financialSystemUserService.getOrganizationByModuleId(KFSConstants.Modules.CHART);

            Map<String, Object> criteria = new HashMap<String, Object>();
            criteria.put("universityFiscalYear", currentUniversityFiscalYear);
            criteria.put("chartOfAccountsCode", customerInvoiceWriteoffDocument.getCustomerInvoiceDocument().getBillByChartOfAccountCode());
            criteria.put("organizationCode", customerInvoiceWriteoffDocument.getCustomerInvoiceDocument().getBilledByOrganizationCode());

            OrganizationAccountingDefault organizationAccountingDefault = (OrganizationAccountingDefault) businessObjectService.findByPrimaryKey(OrganizationAccountingDefault.class, criteria);
            if (ObjectUtils.isNotNull(organizationAccountingDefault)) {
                customerInvoiceWriteoffDocument.setChartOfAccountsCode(organizationAccountingDefault.getWriteoffChartOfAccountsCode());
                customerInvoiceWriteoffDocument.setAccountNumber(organizationAccountingDefault.getWriteoffAccountNumber());
                customerInvoiceWriteoffDocument.setSubAccountNumber(organizationAccountingDefault.getWriteoffSubAccountNumber());
                customerInvoiceWriteoffDocument.setFinancialObjectCode(organizationAccountingDefault.getWriteoffFinancialObjectCode());
                customerInvoiceWriteoffDocument.setFinancialSubObjectCode(organizationAccountingDefault.getWriteoffFinancialSubObjectCode());
                customerInvoiceWriteoffDocument.setProjectCode(organizationAccountingDefault.getWriteoffProjectCode());
                customerInvoiceWriteoffDocument.setOrganizationReferenceIdentifier(organizationAccountingDefault.getWriteoffOrganizationReferenceIdentifier());
            }
        }
    }
    

    public boolean isCustomerInvoiceWriteoffDocumentApproved(String customerInvoiceWriteoffDocumentNumber) {
        Map criteria = new HashMap();
        criteria.put("documentNumber", customerInvoiceWriteoffDocumentNumber);
        criteria.put("documentHeader.financialDocumentStatusCode", KFSConstants.DocumentStatusCodes.APPROVED);
        return businessObjectService.countMatching(CustomerInvoiceWriteoffDocument.class, criteria) == 1;
    }    

    public Collection<CustomerInvoiceWriteoffLookupResult> getCustomerInvoiceDocumentsForInvoiceWriteoffLookup() {
        // change this service call to a service method that actually takes in the lookup parameters
        Collection<CustomerInvoiceDocument> customerInvoiceDocuments = customerInvoiceDocumentService.getAllCustomerInvoiceDocumentsWithoutWorkflowInfo(); 
        return getPopulatedCustomerInvoiceWriteoffLookupResults(customerInvoiceDocuments);
    }
    
    /**
     * This helper method returns a list of customer invoice writeoff lookup result BO's based off a collection of customer invoice documents
     * @param customerInvoiceDocuments
     * @return
     */
    protected Collection<CustomerInvoiceWriteoffLookupResult> getPopulatedCustomerInvoiceWriteoffLookupResults(Collection<CustomerInvoiceDocument> customerInvoiceDocuments){
        Collection<CustomerInvoiceWriteoffLookupResult> populatedCustomerInvoiceWriteoffLookupResults = new ArrayList<CustomerInvoiceWriteoffLookupResult>();
        
        Iterator iter = getCustomerInvoiceDocumentsByCustomerNumberMap(customerInvoiceDocuments).entrySet().iterator();
        CustomerInvoiceWriteoffLookupResult customerInvoiceWriteoffLookupResult = null;
        while(iter.hasNext()) {
            
            Map.Entry entry = (Map.Entry)iter.next();
            String customerNumber = (String)entry.getKey();
            List<CustomerInvoiceDocument> list = (List<CustomerInvoiceDocument>)entry.getValue();
            
            //just get data from first invoice for customer data
            Customer customer = ((CustomerInvoiceDocument)list.get(0)).getCustomer();
            customerInvoiceWriteoffLookupResult = new CustomerInvoiceWriteoffLookupResult();
            customerInvoiceWriteoffLookupResult.setCustomerName(customer.getCustomerName());
            customerInvoiceWriteoffLookupResult.setCustomerNumber(customer.getCustomerNumber());
            customerInvoiceWriteoffLookupResult.setCustomerType(customer.getCustomerType() != null ? customer.getCustomerType().getCustomerTypeDescription() : "");
            customerInvoiceWriteoffLookupResult.setCustomerTotal(new KualiDecimal(100.00));
            customerInvoiceWriteoffLookupResult.setCustomerInvoiceDocuments(list);
            
            populatedCustomerInvoiceWriteoffLookupResults.add(customerInvoiceWriteoffLookupResult);
        }
  
        return populatedCustomerInvoiceWriteoffLookupResults;
    }
    
    /**
     * This helper method returns a map of a list of invoices by customer number
     * @param customerInvoiceDocuments
     * @return
     */
    protected Map<String, List<CustomerInvoiceDocument>> getCustomerInvoiceDocumentsByCustomerNumberMap(Collection<CustomerInvoiceDocument> customerInvoiceDocuments){
        //use a map to sort invoices by customer number
        Map<String, List<CustomerInvoiceDocument>> customerInvoiceDocumentsByCustomerNumberMap = new HashMap<String, List<CustomerInvoiceDocument>>();
        for( CustomerInvoiceDocument customerInvoiceDocument : customerInvoiceDocuments){
            String customerNumber = customerInvoiceDocument.getAccountsReceivableDocumentHeader().getCustomerNumber();
            if( customerInvoiceDocumentsByCustomerNumberMap.containsKey(customerNumber) ){
                ((List<CustomerInvoiceDocument>)customerInvoiceDocumentsByCustomerNumberMap.get(customerNumber)).add(customerInvoiceDocument);
            } else {
                List<CustomerInvoiceDocument> customerInvoiceDocumentsForCustomerNumber = new ArrayList<CustomerInvoiceDocument>();
                customerInvoiceDocumentsForCustomerNumber.add(customerInvoiceDocument);
                customerInvoiceDocumentsByCustomerNumberMap.put(customerNumber, customerInvoiceDocumentsForCustomerNumber);
            }
        }
        
        return customerInvoiceDocumentsByCustomerNumberMap;
    }

    public ParameterService getParameterService() {
        return parameterService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public UniversityDateService getUniversityDateService() {
        return universityDateService;
    }

    public void setUniversityDateService(UniversityDateService universityDateService) {
        this.universityDateService = universityDateService;
    }

    public FinancialSystemUserService getFinancialSystemUserService() {
        return financialSystemUserService;
    }

    public void setFinancialSystemUserService(FinancialSystemUserService financialSystemUserService) {
        this.financialSystemUserService = financialSystemUserService;
    }

    public BusinessObjectService getBusinessObjectService() {
        return businessObjectService;
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    public CustomerInvoiceDocumentService getCustomerInvoiceDocumentService() {
        return customerInvoiceDocumentService;
    }

    public void setCustomerInvoiceDocumentService(CustomerInvoiceDocumentService customerInvoiceDocumentService) {
        this.customerInvoiceDocumentService = customerInvoiceDocumentService;
    }

    public AccountsReceivableDocumentHeaderService getAccountsReceivableDocumentHeaderService() {
        return accountsReceivableDocumentHeaderService;
    }

    public void setAccountsReceivableDocumentHeaderService(AccountsReceivableDocumentHeaderService accountsReceivableDocumentHeaderService) {
        this.accountsReceivableDocumentHeaderService = accountsReceivableDocumentHeaderService;
    }


}
