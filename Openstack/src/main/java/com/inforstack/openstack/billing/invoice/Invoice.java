package com.inforstack.openstack.billing.invoice;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import com.inforstack.openstack.billing.process.BillingProcess;
import com.inforstack.openstack.order.Order;
import com.inforstack.openstack.order.sub.SubOrder;
import com.inforstack.openstack.payment.Payment;
import com.inforstack.openstack.tenant.Tenant;

@Entity
public class Invoice {
	
	@Id
	@GeneratedValue
	private Integer id;
	
	private int status;
	
	@Column(name="start_time")
	private Date startTime;
	
	@Column(name="end_time")
	private Date endTime;
	
	private BigDecimal amount;
	
	private BigDecimal banlance;
	
	@Column(name="pay_time")
	private Date payTime;
	
	@Column(name="create_time")
	private Date createTime;
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="tenant_id")
	private Tenant tenant;
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="sub_order_id")
	private SubOrder subOrder;
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="order_id")
	private Order order;
	
	@ManyToOne
	@JoinColumn(name="billing_process_id")
	private BillingProcess billingProcess;
	
	@ManyToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinTable(name = "billing_payment", 
			joinColumns = { @JoinColumn(name = "billing_id", insertable=false, updatable = false) }, 
			inverseJoinColumns = { @JoinColumn(name = "payment_id", insertable=false, updatable = false) }
	)
	private List<Payment> payments;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}
	
	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public BillingProcess getBillingProcess() {
		return billingProcess;
	}

	public void setBillingProcess(BillingProcess billingProcess) {
		this.billingProcess = billingProcess;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public SubOrder getSubOrder() {
		return subOrder;
	}

	public void setSubOrder(SubOrder subOrder) {
		this.subOrder = subOrder;
	}

	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public List<Payment> getPayments() {
		return payments;
	}

	public void setPayments(List<Payment> payments) {
		this.payments = payments;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getBanlance() {
		return banlance;
	}

	public void setBanlance(BigDecimal banlance) {
		this.banlance = banlance;
	}
	
}