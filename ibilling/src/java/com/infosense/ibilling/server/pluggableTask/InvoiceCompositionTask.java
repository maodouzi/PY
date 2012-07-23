/*
 * JBILLING CONFIDENTIAL
 * _____________________
 *
 * [2003] - [2012] Enterprise jBilling Software Ltd.
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Enterprise jBilling Software.
 * The intellectual and technical concepts contained
 * herein are proprietary to Enterprise jBilling Software
 * and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden.
 */

package com.infosense.ibilling.server.pluggableTask;

import java.math.BigDecimal;

import com.infosense.ibilling.server.invoice.NewInvoiceDTO;
import com.infosense.ibilling.server.process.PeriodOfTime;

public interface InvoiceCompositionTask {
    public void apply(NewInvoiceDTO invoice, Integer userId) throws TaskException;
    
    public BigDecimal calculatePeriodAmount(BigDecimal fullPrice, PeriodOfTime period);
}