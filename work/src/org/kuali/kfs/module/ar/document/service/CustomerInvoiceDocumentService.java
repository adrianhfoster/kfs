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
package org.kuali.kfs.module.ar.document.service;

import java.util.Collection;
import java.util.List;

import org.kuali.kfs.module.ar.businessobject.Customer;
import org.kuali.kfs.module.ar.businessobject.CustomerInvoiceDetail;
import org.kuali.kfs.module.ar.businessobject.NonInvoicedDistribution;
import org.kuali.kfs.module.ar.document.CustomerInvoiceDocument;
import org.kuali.rice.kns.util.KualiDecimal;

public interface CustomerInvoiceDocumentService {
    
    public Collection<CustomerInvoiceDocument> getAllCustomerInvoiceDocuments(); 
    
    public Collection<CustomerInvoiceDocument> getAllCustomerInvoiceDocumentsWithoutWorkflowInfo();
    
    /**
     * 
     * Retrieves all invoice documents that are Open with outstanding balances, including workflow
     * headers.
     * 
     * @return A collection of CustomerInvoiceDocument documents, or an empty list of no matches.
     */
    public Collection<CustomerInvoiceDocument> getAllOpenCustomerInvoiceDocuments();
    
    /**
     * Retrieves all invoice documents that are Open with outstanding balances.
     * 
     * Will NOT retrieve workflow headers, so results of this are not suitable for using to route, save, 
     * or otherwise perform workflow operations upon.
     * 
     * @return
     */
    public Collection<CustomerInvoiceDocument> getAllOpenCustomerInvoiceDocumentsWithoutWorkflow();

    /**
     * 
     * Retrieves all Open Invoices for this given Customer Number.
     * 
     * IMPORTANT - Workflow headers and status are not retrieved by this method, only the raw Customer 
     * Invoice Document from the Database.  If you need a full workflow document, you can do use 
     * DocumentService to retrieve each by document number.
     * 
     * @param customerNumber
     * @return
     */
    public Collection<CustomerInvoiceDocument> getOpenInvoiceDocumentsByCustomerNumber(String customerNumber);

    /**
     * 
     * Retrieves all Open Invoices for the given Customer Name and Customer Type Code
     * 
     * Note that the customerName field is turned into a 'LIKE customerName*' query.
     * 
     * IMPORTANT - Workflow headers and status are not retrieved by this method, only the raw Customer 
     * Invoice Document from the Database.  If you need a full workflow document, you can do use 
     * DocumentService to retrieve each by document number.
     * 
     * @param customerName
     * @param customerTypeCode
     * @return
     */
    public Collection getOpenInvoiceDocumentsByCustomerNameByCustomerType(String customerName, String customerTypeCode);
    
    /**
     * 
     * Retrieves all Open Invoices for the given Customer Name.
     * 
     * Note that this is a leading substring search, so whatever is entered into the customerName 
     * field is turned into a 'LIKE customerName*' query.
     * 
     * IMPORTANT - Workflow headers and status are not retrieved by this method, only the raw Customer 
     * Invoice Document from the Database.  If you need a full workflow document, you can do use 
     * DocumentService to retrieve each by document number.
     * 
     * @param customerName
     * @return
     */
    public Collection<CustomerInvoiceDocument> getOpenInvoiceDocumentsByCustomerName(String customerName);
    
    /**
     * 
     * Retrieves all Open Invoices for the given Customer Type Code.
     * 
     * IMPORTANT - Workflow headers and status are not retrieved by this method, only the raw Customer 
     * Invoice Document from the Database.  If you need a full workflow document, you can do use 
     * DocumentService to retrieve each by document number.
     * 
     * @param customerTypeCode
     * @return
     */
    public Collection<CustomerInvoiceDocument> getOpenInvoiceDocumentsByCustomerType(String customerTypeCode);
    
    /**
     * This method sets up default values for customer invoice document on initiation.
     * @param document
     */
    public void setupDefaultValuesForNewCustomerInvoiceDocument( CustomerInvoiceDocument document );
    
    /**
     * This method sets up default values for customer invoice document when copied.
     * @param customerInvoiceDocument
     */
    public void setupDefaultValuesForCopiedCustomerInvoiceDocument( CustomerInvoiceDocument customerInvoiceDocument );
    
    /**
     * If the customer number and address identifiers are present, display customer information
     */
    public void loadCustomerAddressesForCustomerInvoiceDocument(CustomerInvoiceDocument customerInvoiceDocument);

    /**
     * @param customerNumber
     * @return
     */
    public Collection<CustomerInvoiceDocument> getCustomerInvoiceDocumentsByCustomerNumber(String customerNumber);
    
    /**
     * @param customerInvoiceDocumentNumber
     * @return
     */
    public Collection<CustomerInvoiceDetail> getCustomerInvoiceDetailsForCustomerInvoiceDocument(String customerInvoiceDocumentNumber);
    
    /**
     * @param customerInvoiceDocument
     * @return
     */
    public Collection<CustomerInvoiceDetail> getCustomerInvoiceDetailsForCustomerInvoiceDocument(CustomerInvoiceDocument customerInvoiceDocument);
    
    /**
     * @param invoiceNumber
     * @return
     */
    public Customer getCustomerByOrganizationInvoiceNumber(String invoiceNumber);
    
    /**
     * @param organizationInvoiceNumber
     * @return
     */
    public CustomerInvoiceDocument getInvoiceByOrganizationInvoiceNumber(String organizationInvoiceNumber);

    /**
     * @param documentNumber
     * @return
     */
    public Customer getCustomerByInvoiceDocumentNumber(String documentNumber);
    
    /**
     * @param invoiceDocumentNumber
     * @return
     */
    public CustomerInvoiceDocument getInvoiceByInvoiceDocumentNumber(String invoiceDocumentNumber);
    
    public Collection<CustomerInvoiceDocument> getCustomerInvoiceDocumentsByBillingChartAndOrg(String chartOfAccountsCode, String organizationCode); 
    
    public List<CustomerInvoiceDocument> getCustomerInvoiceDocumentsByProcessingChartAndOrg(String chartOfAccountsCode, String organizationCode);
    
    public Collection<CustomerInvoiceDocument> getCustomerInvoiceDocumentsByAccountNumber(String accountNumber);
    
    /**
     * @param documentNumber
     * @return
     */
//    public Collection<InvoicePaidApplied> getInvoicePaidAppliedsForInvoice(String documentNumber);
    
    /**
     * @param documentNumber
     * @return
     */
    public Collection<NonInvoicedDistribution> getNonInvoicedDistributionsForInvoice(String documentNumber);

    /**
     * @param documentNumber
     * @return
     */
    public KualiDecimal getNonInvoicedTotalForInvoice(String documentNumber);

    /**
     * @param invoice
     * @return
     */
    public KualiDecimal getNonInvoicedTotalForInvoice(CustomerInvoiceDocument invoice);
    
    /**
     * @param documentNumber
     * @return
     */
    public KualiDecimal getPaidAppliedTotalForInvoice(String documentNumber);
    
    /**
     * @param invoice
     * @return
     */
    public KualiDecimal getPaidAppliedTotalForInvoice(CustomerInvoiceDocument invoice);
    
   /**
     * This method updates the open invoice indicator if amounts have been completely paid off
     * @param invoice
     */
    public void closeCustomerInvoiceDocumentIfFullyPaidOff(CustomerInvoiceDocument customerInvoiceDocument, KualiDecimal totalAmountAppliedByDocument);
    
    /**
     * This method updates the open invoice indicator if amounts have been completely paid off
     * @param invoice
     */
    public void closeCustomerInvoiceDocument(CustomerInvoiceDocument customerInvoiceDocument);    
    
     /**
     * This method...
     * @param customerInvoiceDocumentNumber
     * @return
     */
    public KualiDecimal getOpenAmountForCustomerInvoiceDocument(String customerInvoiceDocumentNumber);
    
    
    /**
     * This method...
     * @param customerInvoiceDocumentNumber
     * @return
     */
    public KualiDecimal getOpenAmountForCustomerInvoiceDocument(CustomerInvoiceDocument customerInvoiceDocument);

    public KualiDecimal getOriginalTotalAmountForCustomerInvoiceDocument(CustomerInvoiceDocument customerInvoiceDocument);

}

