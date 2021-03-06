package com.inforstack.openstack.order;

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
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.inforstack.openstack.billing.invoice.Invoice;
import com.inforstack.openstack.order.sub.SubOrder;
import com.inforstack.openstack.tenant.Tenant;
import com.inforstack.openstack.user.User;

@Entity
@Table(name="purchase_order")
public class Order {
	
	@Id
	@GeneratedValue
	private int id;
	
	private String sequence;
	
	private Integer status;
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="tenant_id")
	private Tenant tenant;
	
	@ManyToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="created_by")
	private User createdBy;
	
	@Column(name="create_time")
	private Date createTime;
	
	@Column(name="active_begin")
	private Date activeBegin;
	
	@Column(name="active_end")
	private Date activeEnd;
	
	@Column(name="auto_pay")
	private boolean autoPay;
	
	@OneToMany(cascade=CascadeType.ALL, fetch=FetchType.LAZY, mappedBy="order")
	private List<SubOrder> subOrders;
	
	private BigDecimal amount;
	
	private BigDecimal balance;
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn(name="invoice_id")
	private Invoice invoice;
	
	@Column(name="last_pay_time")
	private Date lastPayTime;
	
	@Column(name="last_billing_time")
	private Date lastBillingTime;
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public List<SubOrder> getSubOrders() {
		return subOrders;
	}

	public void setSubOrders(List<SubOrder> subOrders) {
		this.subOrders = subOrders;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Tenant getTenant() {
		return tenant;
	}

	public void setTenant(Tenant tenant) {
		this.tenant = tenant;
	}

	public User getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(User createdBy) {
		this.createdBy = createdBy;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getActiveBegin() {
		return activeBegin;
	}

	public void setActiveBegin(Date activeBegin) {
		this.activeBegin = activeBegin;
	}

	public Date getActiveEnd() {
		return activeEnd;
	}

	public void setActiveEnd(Date activeEnd) {
		this.activeEnd = activeEnd;
	}

	public boolean getAutoPay() {
		return autoPay;
	}

	public void setAutoPay(boolean autoPay) {
		this.autoPay = autoPay;
	}

	public BigDecimal getAmount() {
		return amount;
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public BigDecimal getBalance() {
		return balance;
	}

	public void setBalance(BigDecimal balance) {
		this.balance = balance;
	}

	public Invoice getInvoice() {
		return invoice;
	}

	public void setInvoice(Invoice invoice) {
		this.invoice = invoice;
	}

	public Date getLastPayTime() {
		return lastPayTime;
	}

	public void setLastPayTime(Date lastPayTime) {
		this.lastPayTime = lastPayTime;
	}

	public Date getLastBillingTime() {
		return lastBillingTime;
	}

	public void setLastBillingTime(Date lastBillingTime) {
		this.lastBillingTime = lastBillingTime;
	}

}
