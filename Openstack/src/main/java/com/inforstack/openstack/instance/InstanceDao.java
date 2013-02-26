package com.inforstack.openstack.instance;

import java.util.List;

import com.inforstack.openstack.basic.BasicDao;
import com.inforstack.openstack.tenant.Tenant;

public interface InstanceDao extends BasicDao<Instance> {

	public List<Instance> listInstancesByTenant(Tenant tenant);
	
	public List<Instance> listInstancesBySubOrder(Integer subOrderId);
	
}
