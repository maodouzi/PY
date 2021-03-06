package com.inforstack.openstack.billing.invoice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.inforstack.openstack.billing.process.BillingProcess;
import com.inforstack.openstack.controller.model.PaginationModel;
import com.inforstack.openstack.order.Order;
import com.inforstack.openstack.order.sub.SubOrder;
import com.inforstack.openstack.tenant.Tenant;

public interface InvoiceService {
	
	/**
	 * Create invoice with necessary fields
	 * @param startTime
	 * @param endTime
	 * @param amount
	 * @param tenant
	 * @param subOrder
	 * @param order
	 * @param billingProcess
	 * @return
	 */
	public Invoice createInvoice(Date startTime, Date endTime, BigDecimal amount, Tenant tenant, 
			SubOrder subOrder, Order order, BillingProcess billingProcess);
	
	/**
	 * delete invoices
	 * @param invoice managed
	 * @return
	 */
	public Invoice deleteInvoice(Invoice invoice);
	
	/**
	 * delete invoice
	 * @param invoiceId
	 * @return
	 */
	public Invoice deleteInvoice(int invoiceId);
	
	/**
	 * find invoice
	 * @param invoiceId
	 * @return
	 */
	public Invoice findInvoice(int invoiceId);
	
	/**
	 * Find invoices by createTime
	 * @param from create time >= form time, or null for no condition
	 * @param to create time <= to time, or null for no condition
	 * @return
	 */
	public PaginationModel<Invoice> findInvoice(int pageIndex, int pageSize, Integer tenantId, Date from, Date to);

	public BigDecimal payAmount(Invoice invoice, BigDecimal payAmount);

	public void paid(Invoice invoice);

	public void unpaid(Invoice invoice);

	public List<Invoice> findInvoicesByBillingProcess(int billingProcessId);

	public Invoice findInvoiceBySequence(String subject);

	public List<Invoice> findUnPaidInvoicesByOrder(int orderId);

	boolean fullPayment(int invoiceId, int paymentId);
	
}
