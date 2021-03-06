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
package org.kuali.kfs.fp.document.web.struts;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.kuali.kfs.fp.businessobject.Check;
import org.kuali.kfs.fp.document.CashReceiptDocument;
import org.kuali.kfs.fp.document.service.CashReceiptCoverSheetService;
import org.kuali.kfs.fp.document.service.CashReceiptService;
import org.kuali.kfs.fp.document.validation.event.AddCheckEvent;
import org.kuali.kfs.fp.document.validation.event.DeleteCheckEvent;
import org.kuali.kfs.fp.document.validation.event.UpdateCheckEvent;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.kfs.sys.KFSKeyConstants;
import org.kuali.kfs.sys.KFSPropertyConstants;
import org.kuali.kfs.sys.context.SpringContext;
import org.kuali.rice.core.api.config.property.ConfigurationService;
import org.kuali.rice.kew.api.exception.WorkflowException;
import org.kuali.rice.kns.util.KNSGlobalVariables;
import org.kuali.rice.kns.util.WebUtils;
import org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase;
import org.kuali.rice.krad.service.DocumentService;
import org.kuali.rice.krad.service.KualiRuleService;
import org.kuali.rice.krad.util.GlobalVariables;

/**
 *
 */
public class CashReceiptAction extends CapitalAccountingLinesActionBase {
    /**
     * Adds handling for check updates
     *
     * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm,
     *      javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm cform = (CashReceiptForm) form;

        if (cform.hasDocumentId()) {
            CashReceiptDocument cdoc = cform.getCashReceiptDocument();

            // handle change of checkEntryMode
            processCheckEntryMode(cform, cdoc);

            // handle changes to checks (but only if current checkEntryMode is 'detail')
            if (CashReceiptDocument.CHECK_ENTRY_DETAIL.equals(cdoc.getCheckEntryMode())) {
                cdoc.setTotalCheckAmount(cdoc.calculateCheckTotal()); // recalc b/c changes to the amounts could have happened
                cdoc.setTotalConfirmedCheckAmount(cdoc.calculateConfirmedCheckTotal());
                processChecks(cdoc, cform);
            }

            // generate errors for negative cash detail and total amounts (especially for the recalculate button)
            SpringContext.getBean(CashReceiptService.class).areCashAmountsInvalid(cdoc);
        }

        // proceed as usual
        ActionForward result = super.execute(mapping, form, request, response);
        return result;

    }

    /**
     * Prepares and streams CR PDF Cover Sheet
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward printCoverSheet(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm crForm = (CashReceiptForm) form;

        // get directory of tempate
        String directory = SpringContext.getBean(ConfigurationService.class).getPropertyValueAsString(KFSConstants.EXTERNALIZABLE_HELP_URL_KEY);

        // retrieve document
        String documentNumber = request.getParameter(KFSPropertyConstants.DOCUMENT_NUMBER);

        CashReceiptDocument document = (CashReceiptDocument) SpringContext.getBean(DocumentService.class).getByDocumentHeaderId(documentNumber);

        // since this action isn't triggered by a post, we don't have the normal document data
        // so we have to set the document into the form manually so that later authz processing
        // has a document object instance to work with
        crForm.setDocument(document);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CashReceiptCoverSheetService coverSheetService = SpringContext.getBean(CashReceiptCoverSheetService.class);
        coverSheetService.generateCoverSheet(document, directory, baos);
        String fileName = documentNumber + "_cover_sheet.pdf";
        WebUtils.saveMimeOutputStreamAsFile(response, "application/pdf", baos, fileName);

        return null;
    }

    /**
     * This method processes the check entry mode to determine if the user is entering checks or if they are just entering the
     * total.
     *
     * @param crForm
     * @param crDoc
     */
    protected void processCheckEntryMode(CashReceiptForm crForm, CashReceiptDocument crDoc) {
        String formMode = crForm.getCheckEntryMode();
        String docMode = crDoc.getCheckEntryMode();

        if (CashReceiptDocument.CHECK_ENTRY_DETAIL.equals(formMode) || CashReceiptDocument.CHECK_ENTRY_TOTAL.equals(formMode)) {
            if (!formMode.equals(docMode)) {
                if (formMode.equals(CashReceiptDocument.CHECK_ENTRY_DETAIL)) {
                    // save current checkTotal, for future restoration
                    crForm.setCheckTotal(crDoc.getTotalCheckAmount());

                    // change mode
                    crDoc.setCheckEntryMode(formMode);
                    crDoc.setTotalCheckAmount(crDoc.calculateCheckTotal());

                    // notify user
                    KNSGlobalVariables.getMessageList().add(KFSKeyConstants.CashReceipt.MSG_CHECK_ENTRY_INDIVIDUAL);
                }
                else {
                    // restore saved checkTotal
                    crDoc.setTotalCheckAmount(crForm.getCheckTotal());

                    // change mode
                    crDoc.setCheckEntryMode(formMode);

                    // notify user
                    KNSGlobalVariables.getMessageList().add(KFSKeyConstants.CashReceipt.MSG_CHECK_ENTRY_TOTAL);
                }
            }
        }
    }

    /**
     * This method handles iterating over the check list and generating check events to apply rules to.
     *
     * @param cdoc
     * @param cform
     */
    protected void processChecks(CashReceiptDocument cdoc, CashReceiptForm cform) {
        List formChecks = cdoc.getChecks();

        int index = 0;
        Iterator i = formChecks.iterator();
        while (i.hasNext()) {
            Check formCheck = (Check) i.next();

            // only generate update events for specific action methods
            String methodToCall = cform.getMethodToCall();
            if (UPDATE_EVENT_ACTIONS.contains(methodToCall)) {
                SpringContext.getBean(KualiRuleService.class).applyRules(new UpdateCheckEvent(KFSPropertyConstants.DOCUMENT + "." + KFSPropertyConstants.CHECK + "[" + index + "]", cdoc, formCheck));
            }
            index++;
        }
    }

    /**
     * Adds Check instance created from the current "new check" line to the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward addCheck(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm crForm = (CashReceiptForm) form;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        Check newCheck = crForm.getNewCheck();
        newCheck.setDocumentNumber(crDoc.getDocumentNumber());

        // check business rules
        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AddCheckEvent(KFSConstants.NEW_CHECK_PROPERTY_NAME, crDoc, newCheck));
        if (rulePassed) {
            // add check
            crDoc.addCheck(newCheck);

            // clear the used newCheck
            crForm.setNewCheck(crDoc.createNewCheck());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Adds confirmed Check instance created from the current "new check" line to the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward addConfirmedCheck(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm crForm = (CashReceiptForm) form;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        Check newCheck = crForm.getNewConfirmedCheck();
        newCheck.setDocumentNumber(crDoc.getDocumentNumber());

        // check business rules
        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new AddCheckEvent(KFSConstants.NEW_CHECK_PROPERTY_NAME, crDoc, newCheck));
        if (rulePassed) {
            // add check
            crDoc.addConfirmedCheck(newCheck);

            // clear the used newCheck
            crForm.setNewConfirmedCheck(crDoc.createNewConfirmedCheck());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes the selected check (line) from the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward deleteCheck(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm crForm = (CashReceiptForm) form;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        int deleteIndex = getLineToDelete(request);
        Check oldCheck = crDoc.getCheck(deleteIndex);


        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new DeleteCheckEvent(KFSConstants.EXISTING_CHECK_PROPERTY_NAME, crDoc, oldCheck));

        if (rulePassed) {
            // delete check
            crDoc.removeCheck(deleteIndex);

            // delete baseline check, if any
            if (crForm.hasBaselineCheck(deleteIndex)) {
                crForm.getBaselineChecks().remove(deleteIndex);
            }
        }
        else {
            GlobalVariables.getMessageMap().putError("document.check[" + deleteIndex + "]", KFSKeyConstants.Check.ERROR_CHECK_DELETERULE, Integer.toString(deleteIndex));
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Deletes the selected check (line) from the document
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward deleteConfirmedCheck(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm crForm = (CashReceiptForm) form;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        int deleteIndex = getLineToDelete(request);
        Check oldCheck = crDoc.getConfirmedCheck(deleteIndex);


        boolean rulePassed = SpringContext.getBean(KualiRuleService.class).applyRules(new DeleteCheckEvent(KFSConstants.EXISTING_CHECK_PROPERTY_NAME, crDoc, oldCheck));

        if (rulePassed) {
            // delete check
            crDoc.removeConfirmedCheck(deleteIndex);

            // delete baseline check, if any
            if (crForm.hasBaselineCheck(deleteIndex)) {
                crForm.getBaselineChecks().remove(deleteIndex);
            }
        }
        else {
            GlobalVariables.getMessageMap().putError("document.confirmedCheck[" + deleteIndex + "]", KFSKeyConstants.Check.ERROR_CHECK_DELETERULE, Integer.toString(deleteIndex));
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Changes the current check-entry mode, if necessary
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward changeCheckEntryMode(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {

        CashReceiptForm crForm = (CashReceiptForm) form;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        String formMode = crForm.getCheckEntryMode();
        String docMode = crDoc.getCheckEntryMode();

        if (CashReceiptDocument.CHECK_ENTRY_DETAIL.equals(formMode) || CashReceiptDocument.CHECK_ENTRY_TOTAL.equals(formMode)) {
            if (!formMode.equals(docMode)) {

                if (formMode.equals(CashReceiptDocument.CHECK_ENTRY_DETAIL)) {
                    // save current checkTotal, for future restoration
                    crForm.setCheckTotal(crDoc.getTotalCheckAmount());

                    // change mode
                    crDoc.setCheckEntryMode(formMode);
                    crDoc.setTotalCheckAmount(crDoc.calculateCheckTotal());

                    // notify user
                    KNSGlobalVariables.getMessageList().add(KFSKeyConstants.CashReceipt.MSG_CHECK_ENTRY_INDIVIDUAL);
                }
                else {
                    // restore saved checkTotal
                    crDoc.setTotalCheckAmount(crForm.getCheckTotal());

                    // change mode
                    crDoc.setCheckEntryMode(formMode);

                    // notify user
                    KNSGlobalVariables.getMessageList().add(KFSKeyConstants.CashReceipt.MSG_CHECK_ENTRY_TOTAL);
                }
            }
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }


    /**
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase#createDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void createDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.createDocument(kualiDocumentFormBase);

        CashReceiptForm crForm = (CashReceiptForm) kualiDocumentFormBase;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        // We don't need to call initializeCampusLocationCode or initializeCashChangeDetails anymore,
        // because they are called in CashReceiptDocument().

        initDerivedCheckValues(crForm);
    }

    /**
     * @see org.kuali.kfs.sys.web.struts.KualiAccountingDocumentActionBase#loadDocument(org.kuali.rice.kns.web.struts.form.KualiDocumentFormBase)
     */
    @Override
    protected void loadDocument(KualiDocumentFormBase kualiDocumentFormBase) throws WorkflowException {
        super.loadDocument(kualiDocumentFormBase);

        initDerivedCheckValues((CashReceiptForm) kualiDocumentFormBase);
    }

    /**
     * Copy all original checks to cash manager confirmed checks
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward copyAllChecks(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm crForm = (CashReceiptForm) form;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        Check c, confirmedCheck;
        for (Iterator<Check> i = crDoc.getChecks().iterator(); i.hasNext();) {
            c = i.next();
            confirmedCheck = crForm.getNewConfirmedCheck();
            confirmedCheck.setDocumentNumber(c.getDocumentNumber());
            confirmedCheck.setCheckDate(c.getCheckDate());
            confirmedCheck.setCheckNumber(c.getCheckNumber());
            confirmedCheck.setAmount(c.getAmount());
            confirmedCheck.setDescription(c.getDescription());

            crDoc.addConfirmedCheck(confirmedCheck);
            // clear the used newCheck
            crForm.setNewConfirmedCheck(crDoc.createNewConfirmedCheck());
        }

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Copies all original currency and coin amounts to cash manager confirmed currency and coin amounts.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward copyAllCurrencyAndCoin(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm crForm = (CashReceiptForm) form;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        crDoc.getConfirmedCurrencyDetail().copyAmounts(crDoc.getCurrencyDetail());
        crDoc.getConfirmedCoinDetail().copyAmounts(crDoc.getCoinDetail());

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Copies all original change currency and coin amounts to cash manager confirmed change currency and coin amounts.
     *
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     * @throws Exception
     */
    public ActionForward copyAllChangeCurrencyAndCoin(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        CashReceiptForm crForm = (CashReceiptForm) form;
        CashReceiptDocument crDoc = crForm.getCashReceiptDocument();

        crDoc.getConfirmedChangeCurrencyDetail().copyAmounts(crDoc.getChangeCurrencyDetail());
        crDoc.getConfirmedChangeCoinDetail().copyAmounts(crDoc.getChangeCoinDetail());

        return mapping.findForward(KFSConstants.MAPPING_BASIC);
    }

    /**
     * Initializes form values which must be derived form document contents (i.e. those which aren't directly available from the
     * document)
     *
     * @param cform
     */
    protected void initDerivedCheckValues(CashReceiptForm cform) {
        CashReceiptDocument cdoc = cform.getCashReceiptDocument();

        cform.setCheckEntryMode(cdoc.getCheckEntryMode());
        cform.setCheckTotal(cdoc.getTotalCheckAmount());

        cform.getBaselineChecks().clear();
        cform.getBaselineChecks().addAll(cform.getCashReceiptDocument().getChecks());
    }

    /**
     * Overridden to guarantee that form of copied document is set to whatever the entry mode of the document is
     * @see org.kuali.rice.kns.web.struts.action.KualiTransactionalDocumentActionBase#copy(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public ActionForward copy(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ActionForward forward = super.copy(mapping, form, request, response);
        initDerivedCheckValues((CashReceiptForm)form);
        return forward;
    }

}

