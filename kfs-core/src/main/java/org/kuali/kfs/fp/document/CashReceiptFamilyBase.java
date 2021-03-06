/*
 * The Kuali Financial System, a comprehensive financial management system for higher education.
 * 
 * Copyright 2005-2014 The Kuali Foundation
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kuali.kfs.fp.document;

import java.sql.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.fp.businessobject.CapitalAccountingLines;
import org.kuali.kfs.fp.businessobject.CapitalAssetInformation;
import org.kuali.kfs.integration.cam.CapitalAssetManagementModuleService;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.businessobject.AccountingLineBase;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntry;
import org.kuali.kfs.sys.businessobject.GeneralLedgerPendingEntrySourceDetail;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.document.service.DebitDeterminerService;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.util.ObjectUtils;

/**
 * Abstract class which defines behavior common to CashReceipt-like documents.
 */
abstract public class CashReceiptFamilyBase extends CapitalAccountingLinesDocumentBase implements CapitalAssetEditable {
    protected static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(CashReceiptFamilyBase.class);
    protected String campusLocationCode; // TODO Needs to be an actual object - also need to clarify this
    protected Date depositDate;

    /**
     * Constructs a CashReceiptFamilyBase
     */
    public CashReceiptFamilyBase() {
        setCampusLocationCode(KFSConstants.CashReceiptConstants.DEFAULT_CASH_RECEIPT_CAMPUS_LOCATION_CODE);
    }

    /**
     * Documents in the CashReceiptFamily do not perform Sufficient Funds checking
     *
     * @see org.kuali.kfs.sys.document.AccountingDocumentBase#documentPerformsSufficientFundsCheck()
     */
    @Override
    public boolean documentPerformsSufficientFundsCheck() {
        return false;
    }

    /**
     * Gets the campusLocationCode attribute.
     *
     * @return Returns the campusLocationCode.
     */
    public String getCampusLocationCode() {
        return campusLocationCode;
    }

    /**
     * Sets the campusLocationCode attribute value.
     *
     * @param campusLocationCode The campusLocationCode to set.
     */
    public void setCampusLocationCode(String campusLocationCode) {
        this.campusLocationCode = campusLocationCode;
    }

    /**
     * Gets the depositDate attribute.
     *
     * @return Returns the depositDate.
     */
    public Date getDepositDate() {
        return depositDate;
    }

    /**
     * Sets the depositDate attribute value.
     *
     * @param depositDate The depositDate to set.
     */
    public void setDepositDate(Date depositDate) {
        this.depositDate = depositDate;
    }


    /**
     * Total for a Cash Receipt according to the spec should be the sum of the amounts on accounting lines belonging to object codes
     * having the 'income' object type, less the sum of the amounts on accounting lines belonging to object codes having the
     * 'expense' object type.
     *
     * @see org.kuali.kfs.sys.document.AccountingDocument#getSourceTotal()
     */
    @Override
    public KualiDecimal getSourceTotal() {
        KualiDecimal total = KualiDecimal.ZERO;
        AccountingLineBase al = null;
        if (ObjectUtils.isNull(getSourceAccountingLines()) || getSourceAccountingLines().isEmpty()) {
            refreshReferenceObject(KFSPropertyConstants.SOURCE_ACCOUNTING_LINES);
        }
        Iterator iter = getSourceAccountingLines().iterator();
        while (iter.hasNext()) {
            al = (AccountingLineBase) iter.next();
            try {
                KualiDecimal amount = al.getAmount().abs();
                if (amount != null && amount.isNonZero()) {
                    if (isDebit(al)) {
                        total = total.subtract(amount);
                    }
                    else { // in this context, if it's not a debit, it's a credit
                        total = total.add(amount);
                    }
                }
            }
            catch (Exception e) {
                // Possibly caused by accounting lines w/ bad data
                LOG.error("Error occured trying to compute Cash receipt total, returning 0", e);
                return KualiDecimal.ZERO;
            }
        }
        return total;
    }

    /**
     * Cash Receipts only have source lines, so this should always return 0.
     *
     * @see org.kuali.kfs.sys.document.AccountingDocument#getTargetTotal()
     */
    @Override
    public KualiDecimal getTargetTotal() {
        return KualiDecimal.ZERO;
    }

    /**
     * Overrides the base implementation to return an empty string.
     *
     * @see org.kuali.kfs.sys.document.AccountingDocument#getSourceAccountingLinesSectionTitle()
     */
    @Override
    public String getSourceAccountingLinesSectionTitle() {
        return KFSConstants.EMPTY_STRING;
    }

    /**
     * Overrides the base implementation to return an empty string.
     *
     * @see org.kuali.kfs.sys.document.AccountingDocument#getTargetAccountingLinesSectionTitle()
     */
    @Override
    public String getTargetAccountingLinesSectionTitle() {
        return KFSConstants.EMPTY_STRING;
    }

    /**
     * Returns true if accounting line is debit
     *
     * @param financialDocument
     * @param accountingLine
     * @param true if accountline line
     * @see IsDebitUtils#isDebitConsideringType(FinancialDocumentRuleBase, FinancialDocument, AccountingLine)
     * @see org.kuali.rice.krad.rule.AccountingLineRule#isDebit(org.kuali.rice.krad.document.FinancialDocument,
     *      org.kuali.rice.krad.bo.AccountingLine)
     */
    @Override
    public boolean isDebit(GeneralLedgerPendingEntrySourceDetail postable) {
        // error corrections are not allowed
        DebitDeterminerService isDebitUtils = SpringContext.getBean(DebitDeterminerService.class);
        //TODO ??? we now allow error correction on AD and CCR, so can't do this checking here; do it in subclasses if needed.
        //isDebitUtils.disallowErrorCorrectionDocumentCheck(this);
        return isDebitUtils.isDebitConsideringType(this, postable);
    }

    /**
     * Overrides to set the entry's description to the description from the accounting line, if a value exists.
     *
     * @param financialDocument submitted accounting document
     * @param accountingLine accounting line in accounting document
     * @param explicitEntry general ledger pending entry
     * @see org.kuali.module.financial.rules.FinancialDocumentRuleBase#customizeExplicitGeneralLedgerPendingEntry(org.kuali.rice.krad.document.FinancialDocument,
     *      org.kuali.rice.krad.bo.AccountingLine, org.kuali.module.gl.bo.GeneralLedgerPendingEntry)
     */
    @Override
    public void customizeExplicitGeneralLedgerPendingEntry(GeneralLedgerPendingEntrySourceDetail postable, GeneralLedgerPendingEntry explicitEntry) {
        String accountingLineDescription = postable.getFinancialDocumentLineDescription();
        if (StringUtils.isNotBlank(accountingLineDescription)) {
            explicitEntry.setTransactionLedgerEntryDescription(accountingLineDescription);
        }
    }

    /**
     * @see org.kuali.kfs.fp.document.CapitalAssetEditable#getCapitalAssetInformation()
     */
    @Override
    public List<CapitalAssetInformation> getCapitalAssetInformation() {
        return ObjectUtils.isNull(capitalAssetInformation) ? null : capitalAssetInformation;
    }

    /**
     * @see org.kuali.kfs.fp.document.CapitalAssetEditable#setCapitalAssetInformation(org.kuali.kfs.fp.businessobject.CapitalAssetInformation)
     */
    @Override
    public void setCapitalAssetInformation(List<CapitalAssetInformation> capitalAssetInformation) {
        this.capitalAssetInformation = capitalAssetInformation;
    }

    protected CapitalAssetManagementModuleService getCapitalAssetManagementModuleService() {
        return SpringContext.getBean(CapitalAssetManagementModuleService.class);
    }

    /**
     * Note: This method is only shared by subclasses which implement Correctable.
     * Upon error correction, negates amount in each capital asset accounting line,
     * and updates the documentNumber to point to the new document.
     */
    protected void correctCapitalAccountingLines() {
        for (CapitalAccountingLines capacctline: capitalAccountingLines) {
            capacctline.setDocumentNumber(documentNumber);
            capacctline.setAmount(capacctline.getAmount().negated());
        }
    }

    /**
     * Returns true if the document is error corrected.
     */
    public boolean isErrorCorrected() {
        return StringUtils.isNotEmpty(getFinancialSystemDocumentHeader().getFinancialDocumentInErrorNumber());
    }

}
