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
package org.kuali.module.labor;

import org.kuali.kfs.KFSConstants;
import org.kuali.kfs.KFSPropertyConstants;

/**
 * This class contains the constants used by Labor Distribution.
 */
public class LaborPropertyConstants {
    public static final String POSITION_NUMBER = "positionNumber";
    public static final String EMPL_ID = "emplid";
    public static final String JULY_1_BUDGET_AMOUNT = "july1BudgetAmount";
    public static final String JULY_1_BUDGET_FTE_QUANTITY = "july1BudgetFteQuantity";
    public static final String JULY_1_BUDGET_TIME_PERCENT = "july1BudgetTimePercent";

    public enum AccountingPeriodProperties {
        JULY     (KFSPropertyConstants.MONTH1_AMOUNT, KFSConstants.MONTH1),
        AUGUST   (KFSPropertyConstants.MONTH2_AMOUNT, KFSConstants.MONTH2),
        SEPTEMBER(KFSPropertyConstants.MONTH3_AMOUNT, KFSConstants.MONTH3),
        OCTOBER  (KFSPropertyConstants.MONTH4_AMOUNT, KFSConstants.MONTH4),
        NOVEMBER (KFSPropertyConstants.MONTH5_AMOUNT, KFSConstants.MONTH5),
        DECEMBER (KFSPropertyConstants.MONTH6_AMOUNT, KFSConstants.MONTH6),
        JANUARY  (KFSPropertyConstants.MONTH7_AMOUNT, KFSConstants.MONTH7),
        FEBRUARY (KFSPropertyConstants.MONTH8_AMOUNT, KFSConstants.MONTH8),
        MARCH    (KFSPropertyConstants.MONTH9_AMOUNT, KFSConstants.MONTH9),
        APRIL    (KFSPropertyConstants.MONTH10_AMOUNT, KFSConstants.MONTH10),
        MAY      (KFSPropertyConstants.MONTH11_AMOUNT, KFSConstants.MONTH11),
        JUNE     (KFSPropertyConstants.MONTH12_AMOUNT, KFSConstants.MONTH12),
        YEAR_END (KFSPropertyConstants.MONTH13_AMOUNT, KFSConstants.MONTH13);
        
        public String propertyName;
        public String typeCode;
        
        private AccountingPeriodProperties(String propertyName, String typeCode) {
            this.propertyName = propertyName;
            this.typeCode = typeCode;
        }        
    }
}
