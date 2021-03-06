package com.inforstack.openstack.instance;

import java.util.List;
import java.util.Map;

import com.inforstack.openstack.item.DataCenter;
import com.inforstack.openstack.order.period.OrderPeriod;
import com.inforstack.openstack.tenant.Tenant;
import com.inforstack.openstack.user.User;

public interface InstanceService {
	
	public Map<String, Float> getUsagePrice(String uuid);
	
	public DataCenter getDataCenterFromInstance(Instance instance);
	
	public OrderPeriod getPeriodFromInstance(Instance stance);
	
	public List<Instance> findInstanceFromTenant(Tenant tenant, String includeStatus, String excludeStatus);
	
	public List<Instance> findInstanceFromTenant(Tenant tenant, int type, String includeStatus, String excludeStatus);
	
	public Instance findInstanceFromUUID(String uuid);
	
	public Instance findSubInstanceFromUUID(String uuid, int type);
	
	public List<VirtualMachine> findVirtualMachineFromTenant(Tenant tenant, String includeStatus, String excludeStatus);
	
	public VirtualMachine findVirtualMachineFromUUID(String uuid);
	
	public List<VolumeInstance> findVolumeFromTenant(Tenant tenant, String includeStatus, String excludeStatus);
	
	public VolumeInstance findVolumeInstanceFromUUID(String uuid);
	
	public List<IP> findIPFromTenant(Tenant tenant, String includeStatus, String excludeStatus);
	
	public IP findIPFromUUID(String uuid);
	
	public void createInstance(User user, Tenant tenant, int orderId);
	
	public void updateInstanceName(User user, Tenant tenant, String uuid, String name);
	
	public void removeVM(User user, Tenant tenant, String serverId, boolean freeAttachedResouces);

	public Instance findInstanceById(Integer instanceId);
	
	public void attachVolume(User user, Tenant tenant, String volumeId, String serverId);
	
	public void detachVolume(User user, Tenant tenant, String volumeId, String serverId);
	
	public void removeVolume(User user, Tenant tenant, String volumeId);
	
	public void associateIP(User user, Tenant tenant, String ipId, String serverId);
	
	public void disassociateIP(User user, Tenant tenant, String ipId, String serverId);
	
	public void removeIP(User user, Tenant tenant, String ipId);
	
}
