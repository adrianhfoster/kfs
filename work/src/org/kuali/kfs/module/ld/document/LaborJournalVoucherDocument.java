/*
 * Copyright 2007 The Kuali Foundation.
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
package org.kuali.module.labor.document;

import java.util.ArrayList;
import java.util.List;

import org.kuali.core.bo.DocumentType;
import org.kuali.core.document.AmountTotaling;
import org.kuali.core.exceptions.ValidationException;
import org.kuali.core.rule.event.KualiDocumentEvent;
import org.kuali.kfs.bo.AccountingLineParser;
import org.kuali.kfs.bo.TargetAccountingLine;
import org.kuali.kfs.util.SpringServiceLocator;
import org.kuali.module.financial.document.JournalVoucherDocument;
import org.kuali.module.labor.LaborConstants.JournalVoucherOffsetType;
import org.kuali.module.labor.bo.ExpenseTransferSourceAccountingLine;
import org.kuali.module.labor.bo.LaborJournalVoucherDetail;
import org.kuali.module.labor.bo.LaborLedgerAccountingLineParser;
import org.kuali.module.labor.bo.LaborLedgerPendingEntry;
import org.kuali.module.labor.bo.TestSourceAccountingLine;

// @latex.ClassSignatureStart
/**
 * Labor Distribution implementation of the <code>{@link org.kuali.module.financial.document.JournalVoucherDocument}</code>
 */
public class LaborJournalVoucherDocument extends JournalVoucherDocument implements LaborLedgerPostingDocument, AmountTotaling {
// @latex.ClassSignatureStop
    private static org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(LaborJournalVoucherDocument.class);

    private String offsetTypeCode = JournalVoucherOffsetType.NO_OFFSET.typeCode;
    private List<LaborLedgerPendingEntry> laborLedgerPendingEntries;   
    private DocumentType documentType;
       
    /**
     * Constructs a LaborJournalVoucherDocument.java.
     */
    public LaborJournalVoucherDocument() {
        super();
        setLaborLedgerPendingEntries(new ArrayList<LaborLedgerPendingEntry>());
    }

    /**
     * @see org.kuali.kfs.document.AccountingDocumentBase#getSourceAccountingLineClass()
     */
    @Override
    public Class getSourceAccountingLineClass() { 
        return LaborJournalVoucherDetail.class;
    }

    /**
     * @see org.kuali.module.labor.document.LaborLedgerPostingDocument#getLaborLedgerPendingEntry(int)
     */
    public LaborLedgerPendingEntry getLaborLedgerPendingEntry(int index) {
        while (laborLedgerPendingEntries.size() <= index) {
            laborLedgerPendingEntries.add(new LaborLedgerPendingEntry());
        }
        return laborLedgerPendingEntries.get(index);
    }

    /**
     * @see org.kuali.kfs.document.AccountingDocumentBase#prepareForSave(org.kuali.core.rule.event.KualiDocumentEvent)
     */
    @Override
    public void prepareForSave(KualiDocumentEvent event) {
        super.prepareForSave(event);
        if (!SpringServiceLocator.getLaborLedgerPendingEntryService().generateLaborLedgerPendingEntries(this)) {
            logErrors();
            throw new ValidationException("labor ledger LLPE generation failed");
        }
    }
    
    /**
     * Gets the offsetTypeCode attribute.
     * 
     * @return Returns the offsetTypeCode.
     */
    public String getOffsetTypeCode() {
        return offsetTypeCode;
    }

    /**
     * Sets the offsetTypeCode attribute value.
     * 
     * @param offsetTypeCode The offsetTypeCode to set.
     */
    public void setOffsetTypeCode(String offsetTypeCode) {
        this.offsetTypeCode = offsetTypeCode;
    }

    /**
     * Gets the laborLedgerPendingEntries attribute.
     * 
     * @return Returns the laborLedgerPendingEntries.
     */
    public List<LaborLedgerPendingEntry> getLaborLedgerPendingEntries() {
        return laborLedgerPendingEntries;
    }

    /**
     * Sets the laborLedgerPendingEntries attribute value.
     * 
     * @param laborLedgerPendingEntries The laborLedgerPendingEntries to set.
     */
    public void setLaborLedgerPendingEntries(List<LaborLedgerPendingEntry> laborLedgerPendingEntries) {
        this.laborLedgerPendingEntries = laborLedgerPendingEntries;
    }

    /**
     * Gets the documentType attribute. 
     * @return Returns the documentType.
     */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /**
     * Sets the documentType attribute value.
     * @param documentType The documentType to set.
     */
    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }
}
