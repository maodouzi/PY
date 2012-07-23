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
package com.infosense.ibilling.server.order.db;

import java.util.List;

import com.infosense.ibilling.server.util.Constants;
import com.infosense.ibilling.server.util.db.AbstractDAS;

public class OrderProcessDAS extends AbstractDAS<OrderProcessDTO> {
    
    //used to check of the order has any invoices (non deleted not cancelled)
    public List<Integer> findActiveInvoicesForOrder(Integer orderId) {

        String hql = "select pr.invoice.id" +
                     "  from OrderProcessDTO pr " +
                     "  where pr.purchaseOrder.id = :orderId" +
                     "    and pr.invoice.deleted = 0" + 
                     "    and pr.isReview = 0";

        List<Integer> data = getSession()
                        .createQuery(hql)
                        .setParameter("orderId", orderId)
                        .setComment("OrderProcessDAS.findActiveInvoicesForOrder " + orderId)
                        .list();
        return data;
    }
    
}