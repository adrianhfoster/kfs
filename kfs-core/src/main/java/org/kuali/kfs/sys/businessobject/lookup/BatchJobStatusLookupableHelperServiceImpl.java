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
package org.kuali.kfs.sys.businessobject.lookup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.batch.BatchJobStatus;
import org.kuali.kfs.sys.batch.service.SchedulerService;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.kfs.sys.service.impl.KfsModuleServiceImpl;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.coreservice.framework.parameter.ParameterService;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.services.IdentityManagementService;
import org.kuali.rice.kns.document.authorization.BusinessObjectRestrictions;
import org.kuali.rice.kns.lookup.HtmlData;
import org.kuali.rice.kns.lookup.HtmlData.AnchorHtmlData;
import org.kuali.rice.kns.lookup.KualiLookupableHelperServiceImpl;
import org.kuali.rice.krad.bo.BusinessObject;
import org.kuali.rice.krad.service.KualiModuleService;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.util.UrlFactory;

public class BatchJobStatusLookupableHelperServiceImpl extends KualiLookupableHelperServiceImpl {

    private static final org.apache.log4j.Logger LOG = org.apache.log4j.Logger.getLogger(BatchJobStatusLookupableHelperServiceImpl.class);

    private SchedulerService schedulerService;
    private ConfigurationService configurationService;
    private ParameterService parameterService;
    private KualiModuleService kualiModuleService;
    private IdentityManagementService identityManagementService;

    @Override
    public List<? extends BusinessObject> getSearchResults(Map<String, String> fieldValues) {
        super.setBackLocation((String) fieldValues.get(KFSConstants.BACK_LOCATION));
        super.setDocFormKey((String) fieldValues.get(KFSConstants.DOC_FORM_KEY));
        List<BatchJobStatus> allJobs = schedulerService.getJobs();
        List<BatchJobStatus> jobs = new ArrayList<BatchJobStatus>();

        String namespaceCode = fieldValues.get("namespaceCode");
        String nameValue = fieldValues.get("name");
        Pattern namePattern = null;
        if (!StringUtils.isEmpty(nameValue)) {
            namePattern = Pattern.compile(nameValue.replace("*", ".*"), Pattern.CASE_INSENSITIVE);
        }
        String schedulerGroup = fieldValues.get("group");
        String jobStatus = fieldValues.get("status");
        for (BatchJobStatus job : allJobs) {
            if (!StringUtils.isEmpty(namespaceCode) &&
                    (!namespaceCode.equalsIgnoreCase(job.getNamespaceCode()) && job.getNamespaceCode()!=null)) {
                continue;
            }
            if (namePattern != null && !namePattern.matcher(job.getName()).matches()) {
                continue; // match failed, skip this entry
            }
            if (!StringUtils.isEmpty(schedulerGroup) && !schedulerGroup.equalsIgnoreCase(job.getGroup())) {
                continue;
            }
            if (!StringUtils.isEmpty(jobStatus) && !jobStatus.equalsIgnoreCase(job.getStatus())) {
                continue;
            }
            jobs.add(job);
        }

        return jobs;
    }

    public boolean doesModuleServiceHaveJobStatus(BatchJobStatus job){
        if(job!=null) {
            KfsModuleServiceImpl moduleService = (KfsModuleServiceImpl)getKualiModuleService().getResponsibleModuleServiceForJob(job.getName());
            //This means this job is externalized and we do not want to show any action urls for it.
            return (moduleService!=null && moduleService.isExternalJob(job.getName()));
        }
        return false;
    }
    
    /***
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#getCustomActionUrls(org.kuali.rice.krad.bo.BusinessObject, java.util.List)
     */
    @Override
    public List<HtmlData> getCustomActionUrls(BusinessObject businessObject, List pkNames) {
        if (businessObject instanceof BatchJobStatus) {
            BatchJobStatus job = (BatchJobStatus) businessObject;
            if(doesModuleServiceHaveJobStatus(job)) {
                return getEmptyActionUrls();
            }
            String linkText = "Modify";
            Map<String,String> permissionDetails = new HashMap<String,String>(1);
            permissionDetails.put(KimConstants.AttributeConstants.NAMESPACE_CODE, job.getNamespaceCode() );
            
            if ( !SpringContext.getBean(IdentityManagementService.class).hasPermissionByTemplateName(
                    GlobalVariables.getUserSession().getPerson().getPrincipalId(), 
                    KRADConstants.KNS_NAMESPACE, 
                    KFSConstants.PermissionTemplate.MODIFY_BATCH_JOB.name, 
                    permissionDetails ) ) {
                linkText = "View";
            }
            String href = configurationService.getPropertyValueAsString(KFSConstants.APPLICATION_URL_KEY) + "/batchModify.do?methodToCall=start&name="+(UrlFactory.encode(job.getName()))+("&group=")+(UrlFactory.encode(job.getGroup()));
            List<HtmlData> anchorHtmlDataList = new ArrayList<HtmlData>();
            AnchorHtmlData anchorHtmlData = new AnchorHtmlData(href, KFSConstants.START_METHOD, linkText);
            anchorHtmlDataList.add(anchorHtmlData);
            return anchorHtmlDataList;
        }
        return getEmptyActionUrls();
    }

    /***
     * @see org.kuali.rice.kns.lookup.AbstractLookupableHelperServiceImpl#getActionUrlTitleText(org.kuali.rice.krad.bo.BusinessObject, java.lang.String, java.util.List)
     */
    @Override
    protected String getActionUrlTitleText(BusinessObject businessObject, String displayText, List pkNames, BusinessObjectRestrictions businessObjectRestrictions){
        BatchJobStatus job = (BatchJobStatus) businessObject;
        String titleText = displayText+" "
            +getDataDictionaryService().getDataDictionary().getBusinessObjectEntry(getBusinessObjectClass().getName()).getObjectLabel()
            +" "
            +configurationService.getPropertyValueAsString(TITLE_ACTION_URL_PREPENDTEXT_PROPERTY);
        titleText += "Name="+job.getName()+" Group="+job.getGroup();
        return titleText;
    }
    
    public void setSchedulerService(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    public void setParameterService(ParameterService parameterService) {
        this.parameterService = parameterService;
    }

    public void setConfigurationService(ConfigurationService configurationService) {
        this.configurationService = configurationService;
    }

    public KualiModuleService getKualiModuleService() {
        if ( kualiModuleService == null ) {
            kualiModuleService = SpringContext.getBean(KualiModuleService.class);
        }
        return kualiModuleService;
    }

    public IdentityManagementService getIdentityManagementService() {
        if ( identityManagementService == null ) {
            identityManagementService = SpringContext.getBean(IdentityManagementService.class);
        }
        return identityManagementService;
    }

}

