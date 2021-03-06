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
package org.kuali.kfs.module.bc.document.service.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kuali.kfs.module.bc.BCConstants.Report.BuildMode;
import org.kuali.kfs.module.bc.BCPropertyConstants;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionObjectPick;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionPullup;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionReasonCodePick;
import org.kuali.kfs.module.bc.businessobject.BudgetConstructionSubFundPick;
import org.kuali.kfs.module.bc.document.dataaccess.BudgetReportsControlListDao;
import org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.service.PersistenceService;
import org.springframework.transaction.annotation.Transactional;

/**
 * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService
 */
@Transactional
public class BudgetReportsControlListServiceImpl implements BudgetReportsControlListService {
    protected BudgetReportsControlListDao budgetReportsControlListDao;
    protected BusinessObjectService businessObjectService;
    protected PersistenceService persistenceServiceOjb;

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#updateReportsControlList(java.lang.String,
     *      java.lang.Integer, java.lang.String, java.lang.String, org.kuali.kfs.module.bc.BCConstants.Report.BuildMode)
     */
    public void updateReportsControlList(String principalName, Integer universityFiscalYear, String chartOfAccountsCode, String organizationCode, BuildMode buildMode) {
        budgetReportsControlListDao.updateReportControlList(principalName, universityFiscalYear, chartOfAccountsCode, organizationCode, buildMode);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#updateReportsSubFundGroupSelectList(java.lang.String)
     */
    public void updateReportSubFundGroupSelectList(String principalName) {
        budgetReportsControlListDao.updateReportsSubFundGroupSelectList(principalName);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#updateReportObjectCodeSelectList(java.lang.String)
     */
    public void updateReportObjectCodeSelectList(String principalName) {
        budgetReportsControlListDao.updateReportsObjectCodeSelectList(principalName);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#updateReportReasonCodeSelectList(java.lang.String)
     */
    public void updateReportReasonCodeSelectList(String principalName) {
        budgetReportsControlListDao.updateReportsReasonCodeSelectList(principalName);
    }

    /**
     * Sets the budgetReportsControlListDao
     * 
     * @param budgetReportsControlListDao The budgetReportsControlListDao to set.
     */
    public void setBudgetReportsControlListDao(BudgetReportsControlListDao budgetReportsControlListDao) {
        this.budgetReportsControlListDao = budgetReportsControlListDao;
    }

    /**
     * Sets the businessObjectService attribute value.
     * 
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#retrieveSelectedOrganziations(java.lang.String)
     */
    public Collection<BudgetConstructionPullup> retrieveSelectedOrganziations(String principalName) {
        Map criteria = new HashMap();
        criteria.put(KFSPropertyConstants.KUALI_USER_PERSON_USER_IDENTIFIER, principalName);
        criteria.put(BCPropertyConstants.PULL_FLAG, new Integer(1));

        return businessObjectService.findMatching(BudgetConstructionPullup.class, criteria);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#retrieveSubFundList(java.lang.String)
     */
    public Collection<BudgetConstructionSubFundPick> retrieveSubFundList(String principalId) {
        // force OJB to go to DB since it is populated using JDBC
        persistenceServiceOjb.clearCache();

        Map criteria = new HashMap();
        criteria.put(KFSPropertyConstants.PERSON_UNIVERSAL_IDENTIFIER, principalId);

        return businessObjectService.findMatching(BudgetConstructionSubFundPick.class, criteria);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#retrieveObjectCodeList(java.lang.String)
     */
    public Collection<BudgetConstructionObjectPick> retrieveObjectCodeList(String principalId) {
        // force OJB to go to DB since it is populated using JDBC
        persistenceServiceOjb.clearCache();

        Map criteria = new HashMap();
        criteria.put(KFSPropertyConstants.PERSON_UNIVERSAL_IDENTIFIER, principalId);

        return businessObjectService.findMatching(BudgetConstructionObjectPick.class, criteria);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#retrieveReasonCodeList(java.lang.String)
     */
    public Collection<BudgetConstructionReasonCodePick> retrieveReasonCodeList(String principalId) {
        // force OJB to go to DB since it is populated using JDBC
        persistenceServiceOjb.clearCache();

        Map criteria = new HashMap();
        criteria.put(KFSPropertyConstants.PERSON_UNIVERSAL_IDENTIFIER, principalId);

        return businessObjectService.findMatching(BudgetConstructionReasonCodePick.class, criteria);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#updateObjectCodeSelectFlags(java.util.List)
     */
    public void updateObjectCodeSelectFlags(List<BudgetConstructionObjectPick> objectCodePickList) {
        budgetReportsControlListDao.updateObjectCodeSelectFlags(objectCodePickList);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#updateReasonCodeSelectFlags(java.util.List)
     */
    public void updateReasonCodeSelectFlags(List<BudgetConstructionReasonCodePick> reasonCodePickList) {
        budgetReportsControlListDao.updateReasonCodeSelectFlags(reasonCodePickList);
    }

    /**
     * @see org.kuali.kfs.module.bc.document.service.BudgetReportsControlListService#updateSubFundSelectFlags(java.util.List)
     */
    public void updateSubFundSelectFlags(List<BudgetConstructionSubFundPick> subFundPickList) {
        budgetReportsControlListDao.updateSubFundSelectFlags(subFundPickList);
    }

    /**
     * Gets the persistenceServiceOjb attribute.
     * 
     * @return Returns the persistenceServiceOjb
     */
    
    public PersistenceService getPersistenceServiceOjb() {
        return persistenceServiceOjb;
    }

    /**	
     * Sets the persistenceServiceOjb attribute.
     * 
     * @param persistenceServiceOjb The persistenceServiceOjb to set.
     */
    public void setPersistenceServiceOjb(PersistenceService persistenceServiceOjb) {
        this.persistenceServiceOjb = persistenceServiceOjb;
    }
}

