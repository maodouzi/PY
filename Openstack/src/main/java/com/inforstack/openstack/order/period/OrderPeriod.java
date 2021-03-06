package com.inforstack.openstack.order.period;

import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.inforstack.openstack.billing.process.conf.BillingProcessConfiguration;
import com.inforstack.openstack.i18n.link.I18nLink;

@Entity
@Table(name="order_period")
public class OrderPeriod {
	
	@Id
	@GeneratedValue
	private Integer id;
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.LAZY)
	@JoinColumn
	private I18nLink name;
	
	@Column(name="period_type")
	private Integer periodType;
	
	@Column(name="period_qutity")
	private Integer periodQuantity;

	@Column(name="create_time")
	private Date createTime;
	
	@Column(name="pay_as_you_go")
	private boolean payAsYouGo;
	
	@ManyToOne(cascade=CascadeType.REFRESH, fetch=FetchType.LAZY)
	@JoinColumn(name="process_config_id")
	private BillingProcessConfiguration processConfig;
	
	private boolean deleted;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public I18nLink getName() {
		return name;
	}

	public void setName(I18nLink name) {
		this.name = name;
	}
	
	public Integer getPeriodType() {
		return periodType;
	}

	public void setPeriodType(Integer periodType) {
		this.periodType = periodType;
	}

	public Integer getPeriodQuantity() {
		return periodQuantity;
	}

	public void setPeriodQuantity(Integer periodQuantity) {
		this.periodQuantity = periodQuantity;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public boolean getPayAsYouGo() {
		return payAsYouGo;
	}

	public void setPayAsYouGo(boolean payAsYouGo) {
		this.payAsYouGo = payAsYouGo;
	}

	public BillingProcessConfiguration getProcessConfig() {
		return processConfig;
	}

	public void setProcessConfig(BillingProcessConfiguration processConfig) {
		this.processConfig = processConfig;
	}
	
}
