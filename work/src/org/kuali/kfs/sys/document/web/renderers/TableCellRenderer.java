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
package org.kuali.kfs.sys.document.web.renderers;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.Tag;

import org.kuali.core.bo.BusinessObject;
import org.kuali.kfs.sys.businessobject.AccountingLine;
import org.kuali.kfs.sys.document.web.AccountingLineTableCell;

/**
 * Renders a cell within a table
 */
public class TableCellRenderer implements Renderer {
    private AccountingLineTableCell cell;

    /**
     * Resets the cell to null
     * @see org.kuali.kfs.sys.document.web.renderers.Renderer#clear()
     */
    public void clear() {
        this.cell = null;
    }

    /**
     * Renders the table cell as a header cell as well as rendering all children renderable elements of the cell
     * @see org.kuali.kfs.sys.document.web.renderers.Renderer#render(javax.servlet.jsp.PageContext, javax.servlet.jsp.tagext.Tag)
     */
    public void render(PageContext pageContext, Tag parentTag) throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            out.write(buildBeginningTag());
            cell.renderChildrenElements(pageContext, parentTag);
            out.write(buildEndingTag());
        }
        catch (IOException ioe) {
            throw new JspException("Difficulty rendering table cell", ioe);
        }
    }
    
    /**
     * Builds the opening cell tag, ie <td>
     * @return the opening cell tag
     */
    protected String buildBeginningTag() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        builder.append(getTagName());
        if (cell.getColSpan() > 1) {
            builder.append(" cellspan=\"");
            builder.append(cell.getColSpan());
            builder.append('"');
        }
        if (cell.getRowSpan() > 1) {
            builder.append(" rowspan=\"");
            builder.append(cell.getRowSpan());
            builder.append('"');
        }
        builder.append(" class=\"infoline\"");
        if (verticallyAlignTowardsTop()) {
            builder.append(" valign=\"top\"");
        }
        builder.append(">\n");
        return builder.toString();
    }
    
    /**
     * Builds the closing cell tag, ie </td>
     * @return the closing cell tag
     */
    protected String buildEndingTag() {
        StringBuilder builder = new StringBuilder();
        builder.append("</");
        builder.append(getTagName());
        builder.append(">");
        return builder.toString();
    }
    
    /**
     * Returns the name of the cell tag we want to create - in this case, "td"
     * @return the String td, which is the tag name of the tags we want to produce
     */
    protected String getTagName() {
        return "td";
    }

    /**
     * Gets the cell attribute. 
     * @return Returns the cell.
     */
    public AccountingLineTableCell getCell() {
        return cell;
    }

    /**
     * Sets the cell attribute value.
     * @param cell The cell to set.
     */
    public void setCell(AccountingLineTableCell cell) {
        this.cell = cell;
    }
    
    /**
     * Determines if the cell should be veritically aligned to the top
     * @return true if the cell should vertically align to the top; false otherwise
     */
    protected boolean verticallyAlignTowardsTop() {
        return true;
    }
}
