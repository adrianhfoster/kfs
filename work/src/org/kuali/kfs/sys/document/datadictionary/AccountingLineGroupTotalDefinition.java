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
package org.kuali.kfs.sys.document.datadictionary;

import org.apache.commons.lang.StringUtils;
import org.kuali.core.datadictionary.exception.AttributeValidationException;
import org.kuali.kfs.sys.document.web.renderers.GroupTotalRenderer;
import org.kuali.kfs.sys.document.web.renderers.Renderer;

/**
 * The definition of an accounting line group total renderer, which will display an accounting line
 * group total as a standard "Total: " + amount.
 */
public class AccountingLineGroupTotalDefinition extends TotalDefinition {
    private String totalProperty;
    
    /**
     * Gets the totalProperty attribute. 
     * @return Returns the totalProperty.
     */
    public String getTotalProperty() {
        return totalProperty;
    }

    /**
     * Sets the totalProperty attribute value.
     * @param totalProperty The totalProperty to set.
     */
    public void setTotalProperty(String totalProperty) {
        this.totalProperty = totalProperty;
    }

    /**
     * Uses GroupTotalRenderer to render the total
     * @see org.kuali.kfs.sys.document.datadictionary.TotalDefinition#getTotalRenderer()
     */
    @Override
    public Renderer getTotalRenderer() {
        GroupTotalRenderer renderer = new GroupTotalRenderer();
        renderer.setTotalProperty(totalProperty);
        return renderer;
    }

    /**
     * Validates that a total property has been added
     * @see org.kuali.core.datadictionary.DataDictionaryDefinition#completeValidation(java.lang.Class, java.lang.Class)
     */
    public void completeValidation(Class rootBusinessObjectClass, Class otherBusinessObjectClass) {
        if (StringUtils.isBlank(totalProperty)) {
            throw new AttributeValidationException("Please specify a totalProperty for the AccountingLineGroupTotalRenderer");
        }
    }

}
