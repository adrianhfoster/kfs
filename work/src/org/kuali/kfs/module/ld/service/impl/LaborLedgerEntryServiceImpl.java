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
package org.kuali.module.labor.service.impl;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.kuali.module.labor.bo.LedgerEntry;
import org.kuali.module.labor.dao.LaborLedgerEntryDao;
import org.kuali.module.labor.service.LaborLedgerEntryService;
import org.springframework.transaction.annotation.Transactional;

/**
 * This class implements LaborLedgerEntryService to provide the access to labor ledger entries in data stores.
 * 
 * @see org.kuali.module.labor.bo.LedgerEntry
 */
@Transactional
public class LaborLedgerEntryServiceImpl implements LaborLedgerEntryService {

    private LaborLedgerEntryDao laborLedgerEntryDao;

    /**
     * @see org.kuali.module.labor.service.LaborLedgerEntryService#save(org.kuali.module.labor.bo.LedgerEntry)
     */
    public void save(LedgerEntry ledgerEntry) {
        laborLedgerEntryDao.save(ledgerEntry);
    }

    /**
     * @see org.kuali.module.labor.service.LaborLedgerEntryService#getMaxSquenceNumber(org.kuali.module.labor.bo.LedgerEntry)
     */
    public Integer getMaxSequenceNumber(LedgerEntry ledgerEntry) {
        return laborLedgerEntryDao.getMaxSquenceNumber(ledgerEntry);
    }

    /**
     * @see org.kuali.module.labor.service.LaborLedgerEntryService#find(java.util.Map)
     */
    public Iterator<LedgerEntry> find(Map<String, String> fieldValues) {
        return laborLedgerEntryDao.find(fieldValues);
    }

    /**
     * @see org.kuali.module.labor.service.LaborLedgerEntryService#findEmployeesWithPayType(java.util.Map, java.util.List, java.util.Map)
     */
    public List<String> findEmployeesWithPayType(Map<Integer, Set<String>> payPeriods, List<String> balanceTypes, Map<String, Set<String>> earnCodePayGroupMap) {
        return laborLedgerEntryDao.findEmployeesWithPayType(payPeriods, balanceTypes, earnCodePayGroupMap);
    }
    
    /**
     * @see org.kuali.module.labor.service.LaborLedgerEntryService#isEmployeeWithPayType(java.lang.String, java.util.Map, java.util.List, java.util.Map)
     */
    public boolean isEmployeeWithPayType(String emplid, Map<Integer, Set<String>> payPeriods, List<String> balanceTypes, Map<String, Set<String>> earnCodePayGroupMap) {
        return laborLedgerEntryDao.isEmployeeWithPayType(emplid, payPeriods, balanceTypes, earnCodePayGroupMap);
    }
    
    /**
     * @see org.kuali.module.labor.service.LaborLedgerEntryService#deleteLedgerEntriesPriorToYear(java.lang.Integer, java.lang.String)
     */
    public void deleteLedgerEntriesPriorToYear(Integer fiscalYear, String chartOfAccountsCode) {
        laborLedgerEntryDao.deleteLedgerEntriesPriorToYear(fiscalYear, chartOfAccountsCode);       
    }
    
    /**
     * Sets the laborLedgerEntryDao attribute value.
     * 
     * @param laborLedgerEntryDao The laborLedgerEntryDao to set.
     */
    public void setLaborLedgerEntryDao(LaborLedgerEntryDao laborLedgerEntryDao) {
        this.laborLedgerEntryDao = laborLedgerEntryDao;
    }
}