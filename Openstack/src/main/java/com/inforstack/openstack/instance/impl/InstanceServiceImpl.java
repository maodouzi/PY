package com.inforstack.openstack.instance.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inforstack.openstack.api.OpenstackAPIException;
import com.inforstack.openstack.api.cinder.CinderService;
import com.inforstack.openstack.api.cinder.Volume;
import com.inforstack.openstack.api.cinder.VolumeType;
import com.inforstack.openstack.api.keystone.Access;
import com.inforstack.openstack.api.keystone.KeystoneService;
import com.inforstack.openstack.api.nova.server.Server;
import com.inforstack.openstack.api.nova.server.ServerService;
import com.inforstack.openstack.api.quantum.FloatingIP;
import com.inforstack.openstack.api.quantum.QuantumService;
import com.inforstack.openstack.basic.BasicDaoImpl.CursorResult;
import com.inforstack.openstack.instance.AttachTaskService;
import com.inforstack.openstack.instance.AttributeMap;
import com.inforstack.openstack.instance.IP;
import com.inforstack.openstack.instance.IPDao;
import com.inforstack.openstack.instance.Instance;
import com.inforstack.openstack.instance.InstanceDao;
import com.inforstack.openstack.instance.InstanceService;
import com.inforstack.openstack.instance.InstanceStatusDao;
import com.inforstack.openstack.instance.VirtualMachine;
import com.inforstack.openstack.instance.VirtualMachineDao;
import com.inforstack.openstack.instance.VolumeInstance;
import com.inforstack.openstack.instance.VolumeInstanceDao;
import com.inforstack.openstack.item.DataCenter;
import com.inforstack.openstack.item.DataCenterDao;
import com.inforstack.openstack.item.FlavorDao;
import com.inforstack.openstack.item.ImageDao;
import com.inforstack.openstack.item.ItemSpecification;
import com.inforstack.openstack.item.NetworkTypeDao;
import com.inforstack.openstack.item.Profile;
import com.inforstack.openstack.item.VolumeTypeDao;
import com.inforstack.openstack.network.Network;
import com.inforstack.openstack.network.NetworkService;
import com.inforstack.openstack.order.Order;
import com.inforstack.openstack.order.OrderService;
import com.inforstack.openstack.order.period.OrderPeriod;
import com.inforstack.openstack.order.sub.SubOrder;
import com.inforstack.openstack.order.sub.SubOrderService;
import com.inforstack.openstack.tenant.Tenant;
import com.inforstack.openstack.user.User;
import com.inforstack.openstack.user.UserService;
import com.inforstack.openstack.utils.Constants;

@Service("instanceService")
@Transactional
public class InstanceServiceImpl implements InstanceService {
	
	@Autowired
	private InstanceDao instanceDao;
	
	@Autowired
	private VirtualMachineDao virtualMachineDao;
	
	@Autowired
	private VolumeInstanceDao volumeInstanceDao;
	
	@Autowired
	private IPDao ipDao;
	
	@Autowired
	private InstanceStatusDao instanceStatusDao;
	
	@Autowired
	private DataCenterDao dataCenterDao;
	
	@Autowired
	private ImageDao imageDao;
	
	@Autowired
	private FlavorDao flavorDao;
	
	@Autowired
	private VolumeTypeDao volumeTypeDao;
	
	@Autowired
	private NetworkTypeDao networkTypeDao;
		
	@Autowired
	private KeystoneService keystoneService;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private CinderService cinderService;
	
	@Autowired
	private QuantumService quantumService;
	
	@Autowired
	private NetworkService networkService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private SubOrderService subOrderService;
	
	@Autowired
	private AttachTaskService attachTaskService;
	
	@Autowired
	private UserService userService;
	
	private static final class IPConfig {
		
		private String extenal;
		
		private boolean web;

		public String getExtenal() {
			return extenal;
		}

		public void setExtenal(String extenal) {
			this.extenal = extenal;
		}

		public boolean isWeb() {
			return web;
		}

		public void setWeb(boolean web) {
			this.web = web;
		}
		
	}
	
	@Override
	public Map<String, Float> getUsagePrice(String uuid) {
		HashMap<String, Float> prices = new HashMap<String, Float>();
		Instance instance = this.instanceDao.findByObject("uuid", uuid);
		if (instance.getType() == Constants.INSTANCE_TYPE_VM) {
			List<SubOrder> subOrders = instance.getSubOrders();
			for (SubOrder subOrder : subOrders) {
				if (subOrder.getItem().getProfile() != null) {
					Profile profile = subOrder.getItem().getProfile();
					if (profile.getCpu() != null) {
						prices.put(Constants.USAGE_CPU, profile.getCpu().getDefaultPrice());
					}
					if (profile.getMemory() != null) {
						prices.put(Constants.USAGE_MEMORY, profile.getMemory().getDefaultPrice());
					}
					if (profile.getDisk() != null) {
						prices.put(Constants.USAGE_DISK, profile.getDisk().getDefaultPrice());
					}
					if (profile.getNetwork() != null) {
						prices.put(Constants.USAGE_NETWORK, profile.getNetwork().getDefaultPrice());
					}
				}
			}
		}
		return prices;
	}
	
	@Override
	public DataCenter getDataCenterFromInstance(Instance instance) {
		DataCenter dataCenter = null;
		int dataCenterId = Integer.parseInt(instance.getRegion());
		dataCenter = this.dataCenterDao.findById(dataCenterId);
		if (dataCenter != null) {
			dataCenter.getName().getId();
		}
		return dataCenter;
	}
	
	@Override
	public OrderPeriod getPeriodFromInstance(Instance instance) {
		SubOrder subOrder = this.subOrderService.findFirstSubOrderByInstanceId(instance.getId());
		OrderPeriod period = subOrder.getOrderPeriod();
		period.getName().getId();
		return period;
	}
	
	@Override
	public List<Instance> findInstanceFromTenant(Tenant tenant, String includeStatus, String excludeStatus) {
		List<Instance> instanceList = new ArrayList<Instance>();
		CursorResult<Integer> orders = this.orderService.findAll(tenant.getId(), null);
		
		while(orders.hasNext()){
			Order order = orderService.findOrderById(orders.getNext());
			if(order == null) continue;
			
			List<SubOrder> subOrders = order.getSubOrders();
			for (SubOrder subOrder : subOrders) {
				List<Instance> instances = this.instanceDao.listInstancesBySubOrder(subOrder.getId(), 0, includeStatus, excludeStatus);
				instanceList.addAll(instances);
			}
		}
		orders.close();
		
		return instanceList;
	} 
	
	@Override
	public List<Instance> findInstanceFromTenant(Tenant tenant, int type, String includeStatus, String excludeStatus) {
		List<Instance> instanceList = new ArrayList<Instance>();
		CursorResult<Integer> orders = this.orderService.findAll(tenant.getId(), null);
		
		while (orders.hasNext()) {
			Order order = orderService.findOrderById(orders.getNext());
			if(order == null) continue;
			
			List<SubOrder> subOrders = order.getSubOrders();
			for (SubOrder subOrder : subOrders) {
				Instance instance = subOrder.getInstance();
				if (instance != null && instance.getType() == type) {
					if ((includeStatus == null || instance.getStatus().equalsIgnoreCase(includeStatus)) && (excludeStatus == null || !instance.getStatus().equalsIgnoreCase(excludeStatus))) {
						instance.getSubOrders().get(0);
						instanceList.add(instance);
					}
				}
			}
		}
		orders.close();
		return instanceList;
	}
	
	@Override
	public Instance findInstanceFromUUID(String uuid) {
		Instance instance = this.instanceDao.findByObject("uuid", uuid);
		return instance;
	}
	
	@Override
	public Instance findSubInstanceFromUUID(String uuid, int type) {
		Instance instance = this.instanceDao.findByObject("uuid", uuid);
		List<Instance> subInstances = instance.getSubInstance();
		instance = null;
		for (Instance subInstance : subInstances) {
			if (subInstance.getType() == type) {
				instance = subInstance;
				break;
			}
		}
		return instance;
	}
	
	@Override
	public List<VirtualMachine> findVirtualMachineFromTenant(Tenant tenant, String includeStatus, String excludeStatus) {
		List<VirtualMachine> virtualMachineList = new ArrayList<VirtualMachine>();
		List<Instance> instances = this.instanceDao.listInstancesByTenant(tenant, Constants.INSTANCE_TYPE_VM, includeStatus, excludeStatus);
		for (Instance instance : instances) {
			virtualMachineList.add(this.virtualMachineDao.findByObject("uuid", instance.getUuid()));
		}
		return virtualMachineList;
	}
	
	@Override
	public VirtualMachine findVirtualMachineFromUUID(String uuid) {
		return this.virtualMachineDao.findByObject("uuid", uuid);
	}
	
	@Override
	public List<VolumeInstance> findVolumeFromTenant(Tenant tenant, String includeStatus, String excludeStatus) {
		List<VolumeInstance> volumeList = new ArrayList<VolumeInstance>();
		List<Instance> instances = this.instanceDao.listInstancesByTenant(tenant, Constants.INSTANCE_TYPE_VOLUME, includeStatus, excludeStatus);
		for (Instance instance : instances) {
			volumeList.add(this.volumeInstanceDao.findByObject("uuid", instance.getUuid()));
		}
		return volumeList;
	}

	@Override
	public VolumeInstance findVolumeInstanceFromUUID(String uuid) {
		return this.volumeInstanceDao.findByObject("uuid", uuid);
	}
	
	@Override
	public List<IP> findIPFromTenant(Tenant tenant, String includeStatus, String excludeStatus) {
		List<IP> ipList = new ArrayList<IP>();
		List<Instance> instances = this.instanceDao.listInstancesByTenant(tenant, Constants.INSTANCE_TYPE_IP, includeStatus, excludeStatus);
		for (Instance instance : instances) {
			ipList.add(this.ipDao.findByObject("uuid", instance.getUuid()));
		}
		return ipList;
	}

	@Override
	public IP findIPFromUUID(String uuid) {
		return this.ipDao.findByObject("uuid", uuid);
	}

	@Override
	public void createInstance(User user, Tenant tenant, int orderId) {
			Access access = null;
			Server newServer = null;
			Volume newVolume = null;
			String password = this.userService.getOpenstackUserPassword();
			try {
				access = this.keystoneService.getAccess(user.getUsername(), password, tenant.getUuid(), true);
				if (access != null) {
					Order order = this.orderService.findOrderById(orderId);
					if (order != null) {
						String dataCenterRef = this.getDataCenterFromOrder(order);
						if (dataCenterRef != null) {
							DataCenter dataCenter = this.dataCenterDao.findById(Integer.parseInt(dataCenterRef));
							VirtualMachine vm = null;
							VolumeInstance vi = null;
							IP ip = null;
							Server server = this.getServerFromOrder(order);
							if (server != null) {
								String tenantName = tenant.getName();
								String networkName = tenantName + "_" + dataCenterRef + "_private";
								
								List<Network> networks = this.networkService.listNetworksFromTenant(tenant, dataCenter.getId());
								Network network = null;
								if (networks.isEmpty()) {
									network = this.networkService.createNetwork(user, tenant, networkName, dataCenter);
								} else {
									for (Network n : networks) {
										if (n.getName().equalsIgnoreCase(networkName)) {
											network = n;
											break;
										}
									}
									if (network == null) {
										network = this.networkService.createNetwork(user, tenant, networkName, dataCenter);
									}
								}
								if (network != null) {
									com.inforstack.openstack.api.nova.server.Server.Network[] osNetworks = new com.inforstack.openstack.api.nova.server.Server.Network[1];
									osNetworks[0] = new com.inforstack.openstack.api.nova.server.Server.Network();
									osNetworks[0].setUuid(network.getUuid());
									server.setNetworks(osNetworks);
								}
								newServer = this.serverService.createServer(access, server);
								if (newServer != null) {
									vm = this.bindServerToSubOrder(newServer, order, dataCenterRef, tenant);
									server = newServer;
								}
							}
							
							Volume volume = this.getVolumeFromOrder(order);
							if (volume != null && volume.getType() != null && !volume.getType().isEmpty()) {
								newVolume = this.cinderService.createVolume(access, volume.getName(), "", volume.getSize(), false, volume.getType(), volume.getZone());
								if (newVolume != null) {
									vi = this.bindVolumeToSubOrder(newVolume, order, vm, dataCenterRef, tenant);
									if (server != null && server.getId() != null && !server.getId().isEmpty()) {
										this.attachTaskService.addTask(Constants.ATTACH_TASK_TYPE_VOLUME, vm.getUuid(), vi.getUuid(), user.getUsername(), password, tenant.getUuid());
									}
								}
							}
							
							IPConfig ipConfig = this.getIPFromOrder(order);
							if (ipConfig != null) {
								FloatingIP floatingIP = this.quantumService.createFloatingIP(access, ipConfig.getExtenal());
								if (floatingIP != null) {
									ip = this.bindIPToSubOrder(floatingIP, order, vm, dataCenterRef, tenant);
								}
								if (server != null && server.getId() != null && !server.getId().isEmpty()) {
									this.attachTaskService.addTask(Constants.ATTACH_TASK_TYPE_IP, vm.getUuid(), ip.getUuid(), user.getUsername(), password, tenant.getUuid());
								}
							}
						}
					}
				}
			} catch (OpenstackAPIException e) {
				throw new RuntimeException(e);
			} finally {
				// TODO: [ricky]remove openstack instances after error
				if (access != null) {
					
				}
			}
	}

	@Override
	public void updateInstanceName(User user, Tenant tenant, String uuid, String name) {
		if (name != null) {
			Instance instance = this.instanceDao.findByObject("uuid", uuid);
			if (instance != null) {
				instance.setName(name);
			}
			if (instance.getType() == Constants.INSTANCE_TYPE_VM) {
				VirtualMachine vm = this.virtualMachineDao.findByObject("uuid", uuid);
				if (vm != null) {
					vm.setName(name);
				}
			} else if (instance.getType() == Constants.INSTANCE_TYPE_VOLUME) {
				VolumeInstance vi = this.volumeInstanceDao.findByObject("uuid", uuid);
				if (vi != null) {
					vi.setName(name);
				}
			}
		}
	}

	@Override
	public void removeVM(User user, Tenant tenant, String serverId, boolean freeAttachedResouces) {
		Instance instance = this.instanceDao.findByObject("uuid", serverId);
		if (instance != null && instance.getType() == Constants.INSTANCE_TYPE_VM) {
			Access access = null;
			try {
				String password = this.userService.getOpenstackUserPassword();
				access = this.keystoneService.getAccess(user.getUsername(), password, tenant.getUuid(), true);
				if (access != null) {
					List<Instance> subInstances = instance.getSubInstance();
					if (subInstances.size() > 0) {
						for (Instance subInstance : subInstances) {
							int type = subInstance.getType();
							String attachId = subInstance.getUuid();
							switch (type) {
								case Constants.INSTANCE_TYPE_VOLUME: {
									this.attachTaskService.addTask(Constants.DELETE_TASK_TYPE_VOLUME, serverId, attachId, user.getUsername(), password, tenant.getUuid());
									break;
								}
								case Constants.INSTANCE_TYPE_IP: {
									this.attachTaskService.addTask(Constants.DELETE_TASK_TYPE_IP, serverId, attachId, user.getUsername(), password, tenant.getUuid());
									break;
								}
							}
						}
					}
					this.serverService.removeServer(access, serverId);
				}
			} catch (OpenstackAPIException e) {
				
			}
		}
	}
	
	private Instance registerInstance(int type, String id, String name, String dataCenterRef, Tenant tenant) {
		Date now = new Date();
		Instance instance = new Instance();
		instance.setType(type);
		instance.setUuid(id);
		instance.setName(name);
		instance.setCreateTime(now);
		instance.setUpdateTime(now);
		instance.setStatus("new");
		instance.setTask("");
		instance.setRegion(dataCenterRef);
		instance.setTenant(tenant);
		
		this.instanceDao.persist(instance);
		return instance;
	}
	
	private String getDataCenterFromOrder(Order order) {
		String dataCenterRef = null;
		List<SubOrder> subOrders = order.getSubOrders();
		for (SubOrder subOrder : subOrders) {
			int osType = subOrder.getItem().getOsType();
			String refId = subOrder.getItem().getRefId();
			if (osType == ItemSpecification.OS_TYPE_DATACENTER_ID) {
				dataCenterRef = refId;
			}
		}
		return dataCenterRef;
	}
	
	private Server getServerFromOrder(Order order) {
		Server server = null;
		String dataCenterRef = null;
		String imageRef = null;
		String flavorRef = null;
		String serverName = "New Instance";
		List<SubOrder> subOrders = order.getSubOrders();
		for (SubOrder subOrder : subOrders) {
			int osType = subOrder.getItem().getOsType();
			String refId = subOrder.getItem().getRefId();
			switch (osType) {
				case ItemSpecification.OS_TYPE_DATACENTER_ID: {
					dataCenterRef = refId;
					String name = AttributeMap.getInstance().get(subOrder.getId(), "name");
					if (name != null && !name.isEmpty()) {
						serverName = name;
					}
					break;
				}
				case ItemSpecification.OS_TYPE_IMAGE_ID: {
					imageRef = refId;
					String name = AttributeMap.getInstance().get(subOrder.getId(), "name");
					if (name != null && !name.isEmpty()) {
						serverName = name;
					}
					break;
				}
				case ItemSpecification.OS_TYPE_FLAVOR_ID: {
					flavorRef = refId;
					String name = AttributeMap.getInstance().get(subOrder.getId(), "name");
					if (name != null && !name.isEmpty()) {
						serverName = name;
					}
					break;
				}
			}
		}
		if (dataCenterRef != null && imageRef != null && flavorRef != null) {
			int dataCenterId = Integer.parseInt(dataCenterRef);
			imageRef = this.imageDao.getImageRefId(dataCenterId, imageRef);
			flavorRef = this.flavorDao.getFlavorRefId(dataCenterId, flavorRef);
			if (flavorRef != null && imageRef != null) {
				server = new Server();
				server.setName(serverName);
				server.setImageRef(imageRef);
				server.setFlavorRef(flavorRef);
			}
		}
		return server;
	}
	
	private Volume getVolumeFromOrder(Order order) {
		Volume volume = null;
		
		String volumeName = "New Volume";
		String dataCenterRef = null;
		String volumeTypeRef = null;
		List<SubOrder> subOrders = order.getSubOrders();
		for (SubOrder subOrder : subOrders) {
			int osType = subOrder.getItem().getOsType();
			String refId = subOrder.getItem().getRefId();
			switch (osType) {
				case ItemSpecification.OS_TYPE_DATACENTER_ID: {
					dataCenterRef = refId;
					break;
				}
				case ItemSpecification.OS_TYPE_VOLUME_ID: {
					volumeTypeRef = refId;
					String name = AttributeMap.getInstance().get(subOrder.getId(), "name");
					if (name != null) {
						volumeName = name;
					}
					break;
				}
			}
		}		
		if (dataCenterRef != null && volumeTypeRef != null) {
			int dataCenterId = Integer.parseInt(dataCenterRef);
			volumeTypeRef = this.volumeTypeDao.getVolumeRefId(dataCenterId, volumeTypeRef);
			VolumeType vt = null;
			try {
				vt = this.cinderService.getVolumeType(volumeTypeRef);
			} catch (OpenstackAPIException e) {
			}
			
			if (vt != null) {
				volume = new Volume();
				volume.setName(volumeName);
				volume.setType(volumeTypeRef);
				volume.setSize(Integer.parseInt(vt.getName()));
			}
		}
		return volume;
	}
	
	private IPConfig getIPFromOrder(Order order) {
		IPConfig ipConfig = null;
		List<SubOrder> subOrders = order.getSubOrders();
		String dataCenterRef = null;
		String ipRef = null;
		for (SubOrder subOrder : subOrders) {
			int osType = subOrder.getItem().getOsType();
			String refId = subOrder.getItem().getRefId();
			if (osType == ItemSpecification.OS_TYPE_DATACENTER_ID) {
				dataCenterRef = refId;
			} else if (osType == ItemSpecification.OS_TYPE_NETWORK_ID) {
				ipRef = refId;
			}
		}
		if (dataCenterRef != null && ipRef != null) {
			int dataCenterId = Integer.parseInt(dataCenterRef);
			DataCenter dataCenter = this.dataCenterDao.findById(dataCenterId);
			if (dataCenter != null) {
				ipConfig = new IPConfig();
				ipConfig.setExtenal(dataCenter.getExternalNet());
				ipConfig.setWeb(this.networkTypeDao.isWebSite(dataCenterId, ipRef));
			}
		}
		return ipConfig;
	}
	
	private VirtualMachine bindServerToSubOrder(Server server, Order order, String dataCenterRef, Tenant tenant) {
		VirtualMachine vm = null;
		List<SubOrder> subOrders = order.getSubOrders();
		for (SubOrder subOrder : subOrders) {
			int osType = subOrder.getItem().getOsType();
			switch (osType) {
			case ItemSpecification.OS_TYPE_PERIOD_ID:
				vm = new VirtualMachine();
				vm.setUuid(server.getId());
				vm.setName(server.getName());
				vm.setImage(server.getImage().getId());
				vm.setFlavor(server.getFlavor().getId());
				this.virtualMachineDao.persist(vm);
				Instance instance = this.registerInstance(Constants.INSTANCE_TYPE_VM, server.getId(), server.getName(), dataCenterRef, tenant);
				subOrder.setInstance(instance);
			case ItemSpecification.OS_TYPE_FLAVOR_ID:
			case ItemSpecification.OS_TYPE_IMAGE_ID:
			case ItemSpecification.OS_TYPE_DATACENTER_ID:
				subOrder.setUuid(server.getId());
				break;
			}
		}
		return vm;
	}
	
	private VolumeInstance bindVolumeToSubOrder(Volume volume, Order order, VirtualMachine vm, String dataCenterRef, Tenant tenant) {
		VolumeInstance vi = null;
		List<SubOrder> subOrders = order.getSubOrders();
		for (SubOrder subOrder : subOrders) {
			int osType = subOrder.getItem().getOsType();
			switch (osType) {
			case ItemSpecification.OS_TYPE_VOLUME_ID:
				subOrder.setUuid(volume.getId());
				vi = new VolumeInstance();
				vi.setUuid(volume.getId());
				vi.setName(volume.getName());
				vi.setSize(volume.getSize());
				if (vm != null) {
					vi.setVm(vm.getUuid());					
				}
				this.volumeInstanceDao.persist(vi);
				Instance instance = this.registerInstance(Constants.INSTANCE_TYPE_VOLUME, volume.getId(), volume.getName(), dataCenterRef, tenant);
				if (vm != null) {
					Instance parentInstance = this.instanceDao.findByObject("uuid", vm.getUuid());
					List<Instance> subInstances = parentInstance.getSubInstance();
					subInstances.add(instance);
					parentInstance.setSubInstance(subInstances);
					instance.setParent(parentInstance);
					this.instanceDao.persist(instance);
					this.instanceDao.persist(parentInstance);
				}
				subOrder.setInstance(instance);
				break;
			}
		}
		return vi;
	}
	
	private IP bindIPToSubOrder(FloatingIP floatingIP, Order order, VirtualMachine vm, String dataCenterRef, Tenant tenant) {
		IP ip = null;
		List<SubOrder> subOrders = order.getSubOrders();
		for (SubOrder subOrder : subOrders) {
			int osType = subOrder.getItem().getOsType();
			switch (osType) {
			case ItemSpecification.OS_TYPE_NETWORK_ID:
				subOrder.setUuid(floatingIP.getId());
				ip = new IP();
				ip.setUuid(floatingIP.getId());
				ip.setAddress(floatingIP.getFloatingIP());
				if (vm != null) {
					ip.setVm(vm.getUuid());					
				}
				this.ipDao.persist(ip);
				Instance instance = this.registerInstance(Constants.INSTANCE_TYPE_IP, floatingIP.getId(), "", dataCenterRef, tenant);
				instance.setStatus("available");
				if (vm != null) {
					Instance parentInstance = this.instanceDao.findByObject("uuid", vm.getUuid());
					List<Instance> subInstances = parentInstance.getSubInstance();
					subInstances.add(instance);
					parentInstance.setSubInstance(subInstances);
					instance.setParent(parentInstance);
					this.instanceDao.persist(parentInstance);
				}
				this.instanceDao.persist(instance);
				subOrder.setInstance(instance);
				break;
			}
		}
		return ip;
	}

	@Override
	public Instance findInstanceById(Integer instanceId) {
		return instanceDao.findById(instanceId);
	}

	@Override
	public void attachVolume(User user, Tenant tenant, String volumeId, String serverId) {
		String username = user.getUsername();
		String password = this.userService.getOpenstackUserPassword();
		try {
			Access access = this.keystoneService.getAccess(username, password, tenant.getUuid(), true);
			if (access != null) {
				Instance volInstance = this.instanceDao.findByObject("uuid", volumeId);
				Instance vmInstance = this.instanceDao.findByObject("uuid", serverId);
				if (volInstance != null && volInstance.getType() == Constants.INSTANCE_TYPE_VOLUME && vmInstance != null && vmInstance.getType() == Constants.INSTANCE_TYPE_VM) {
					if (!this.hasSubInstance(vmInstance, Constants.INSTANCE_TYPE_VOLUME) && volInstance.getStatus().equalsIgnoreCase("available")) {
						VolumeInstance vi = this.volumeInstanceDao.findByObject("uuid", volumeId);
						vi.setVm(serverId);
						this.volumeInstanceDao.persist(vi);
						this.cinderService.attachVolume(access, serverId, volumeId, null);
						volInstance.setParent(vmInstance);
						List<Instance> subInstances = vmInstance.getSubInstance();
						subInstances.add(volInstance);
						vmInstance.setSubInstance(subInstances);
						this.instanceDao.persist(vmInstance);
						this.instanceDao.persist(volInstance);
					}
				}
			}
		} catch (OpenstackAPIException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void detachVolume(User user, Tenant tenant, String volumeId, String serverId) {
		String username = user.getUsername();
		String password = this.userService.getOpenstackUserPassword();
		try {
			Access access = this.keystoneService.getAccess(username, password, tenant.getUuid(), true);
			if (access != null) {
				Instance volInstance = this.instanceDao.findByObject("uuid", volumeId);
				Instance vmInstance = this.instanceDao.findByObject("uuid", serverId);
				if (volInstance != null && volInstance.getType() == Constants.INSTANCE_TYPE_VOLUME && vmInstance != null && vmInstance.getType() == Constants.INSTANCE_TYPE_VM) {
					if (volInstance.getStatus().equalsIgnoreCase("in-use") && volInstance.getParent().getId() == vmInstance.getId()) {
						VolumeInstance vi = this.volumeInstanceDao.findByObject("uuid", volumeId);
						if (vi.getVm().equalsIgnoreCase(serverId)) {
							vi.setVm(null);
							this.volumeInstanceDao.persist(vi);
							this.cinderService.detachVolume(access, serverId, volumeId);
							volInstance.setParent(null);
							List<Instance> subInstances = vmInstance.getSubInstance();
							subInstances.remove(volInstance);
							vmInstance.setSubInstance(subInstances);
							this.instanceDao.persist(vmInstance);
							this.instanceDao.persist(volInstance);
						}
					}
				}
			}
		} catch (OpenstackAPIException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeVolume(User user, Tenant tenant, String volumeId) {
		String username = user.getUsername();
		String password = this.userService.getOpenstackUserPassword();
		try {
			Access access = this.keystoneService.getAccess(username, password, tenant.getUuid(), true);
			if (access != null) {
				Instance volInstance = this.instanceDao.findByObject("uuid", volumeId);
				if (volInstance != null && volInstance.getType() == Constants.INSTANCE_TYPE_VOLUME) {
					if (!volInstance.getStatus().equalsIgnoreCase("in-use")) {
						this.cinderService.removeVolume(access, volumeId);
					}
				}
			}
		} catch (OpenstackAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void associateIP(User user, Tenant tenant, String ipId, String serverId) {
		String username = user.getUsername();
		String password = this.userService.getOpenstackUserPassword();
		try {
			Access access = this.keystoneService.getAccess(username, password, tenant.getUuid(), true);
			if (access != null) {
				Instance ipInstance = this.instanceDao.findByObject("uuid", ipId);
				Instance vmInstance = this.instanceDao.findByObject("uuid", serverId);
				if (ipInstance != null && ipInstance.getType() == Constants.INSTANCE_TYPE_IP && vmInstance != null && vmInstance.getType() == Constants.INSTANCE_TYPE_VM) {
					if (!this.hasSubInstance(vmInstance, Constants.INSTANCE_TYPE_IP) && ipInstance.getStatus().equalsIgnoreCase("available")) {
						IP ip = this.ipDao.findByObject("uuid", ipId);
						ip.setVm(serverId);
						this.ipDao.persist(ip);
						this.quantumService.associateFloatingIP(access, serverId, ipId);
						ipInstance.setParent(vmInstance);
						List<Instance> subInstances = vmInstance.getSubInstance();
						subInstances.add(ipInstance);
						vmInstance.setSubInstance(subInstances);
						this.instanceDao.persist(vmInstance);
						this.instanceDao.persist(ipInstance);
					}
				}
			}
		} catch (OpenstackAPIException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void disassociateIP(User user, Tenant tenant, String ipId, String serverId) {
		String username = user.getUsername();
		String password = this.userService.getOpenstackUserPassword();
		try {
			Access access = this.keystoneService.getAccess(username, password, tenant.getUuid(), true);
			if (access != null) {
				Instance ipInstance = this.instanceDao.findByObject("uuid", ipId);
				Instance vmInstance = this.instanceDao.findByObject("uuid", serverId);
				if (ipInstance != null && ipInstance.getType() == Constants.INSTANCE_TYPE_IP && vmInstance != null && vmInstance.getType() == Constants.INSTANCE_TYPE_VM) {
					if (ipInstance.getStatus().equalsIgnoreCase("in-use") && ipInstance.getParent().getId() == vmInstance.getId()) {
						IP ip = this.ipDao.findByObject("uuid", ipId);
						if (ip.getVm().equalsIgnoreCase(serverId)) {
							ip.setVm(null);
							this.ipDao.persist(ip);
							this.quantumService.disassociateFloatingIP(access, ipId);
							ipInstance.setParent(null);
							List<Instance> subInstances = vmInstance.getSubInstance();
							subInstances.remove(ipInstance);
							vmInstance.setSubInstance(subInstances);
							this.instanceDao.persist(vmInstance);
							this.instanceDao.persist(ipInstance);
						}
					}
				}
			}
		} catch (OpenstackAPIException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void removeIP(User user, Tenant tenant, String ipId) {
		String username = user.getUsername();
		String password = this.userService.getOpenstackUserPassword();
		try {
			Access access = this.keystoneService.getAccess(username, password, tenant.getUuid(), true);
			if (access != null) {
				Instance ipInstance = this.instanceDao.findByObject("uuid", ipId);
				if (ipInstance != null && ipInstance.getType() == Constants.INSTANCE_TYPE_IP) {
					if (!ipInstance.getStatus().equalsIgnoreCase("in-use")) {
						this.quantumService.removeFloatingIP(access, ipId);
					}
				}
			}
		} catch (OpenstackAPIException e) {
			throw new RuntimeException(e);
		}
	}
	
	private boolean hasSubInstance(Instance instance, int type) {
		boolean has = false;
		if (instance.getType() == Constants.INSTANCE_TYPE_VM) {
			List<Instance> subInstances = instance.getSubInstance();
			for (Instance sub : subInstances) {
				if (sub.getType() == type) {
					has = true;
					break;
				}
			}
		}
		return has;
	}

}
