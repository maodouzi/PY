package com.inforstack.openstack.api.nova.server.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.inforstack.openstack.api.OpenstackAPIException;
import com.inforstack.openstack.api.RequestBody;
import com.inforstack.openstack.api.keystone.Access;
import com.inforstack.openstack.api.keystone.Access.Service.EndPoint.Type;
import com.inforstack.openstack.api.keystone.KeystoneService;
import com.inforstack.openstack.api.nova.flavor.Flavor;
import com.inforstack.openstack.api.nova.flavor.FlavorService;
import com.inforstack.openstack.api.nova.image.Image;
import com.inforstack.openstack.api.nova.image.ImageService;
import com.inforstack.openstack.api.nova.server.Address;
import com.inforstack.openstack.api.nova.server.Server;
import com.inforstack.openstack.api.nova.server.ServerAction;
import com.inforstack.openstack.api.nova.server.ServerService;
import com.inforstack.openstack.configuration.Configuration;
import com.inforstack.openstack.configuration.ConfigurationDao;
import com.inforstack.openstack.instance.Instance;
import com.inforstack.openstack.instance.InstanceDao;
import com.inforstack.openstack.instance.InstanceStatus;
import com.inforstack.openstack.instance.InstanceStatusDao;
import com.inforstack.openstack.instance.UserActionDao;
import com.inforstack.openstack.instance.VirtualMachine;
import com.inforstack.openstack.instance.VirtualMachineDao;
import com.inforstack.openstack.utils.OpenstackUtil;
import com.inforstack.openstack.utils.RestUtils;

@Service("serverService")
@Transactional
public class ServerServiceImpl implements ServerService {

	@Autowired
	private ConfigurationDao configurationDao;
	
	@Autowired
	private KeystoneService tokenService;
	
	@Autowired
	private FlavorService flavorService;
	
	@Autowired
	private ImageService imageService;
	
	@Autowired
	private InstanceDao instanceDao;
	
	@Autowired
	private VirtualMachineDao vmDao;
	
	@Autowired
	private InstanceStatusDao instanceStatusDao;
	
	@Autowired
	private UserActionDao userActionDao;
	
	public static final class Servers {
		
		private Server[] servers;

		public Server[] getServers() {
			return servers;
		}

		public void setServers(Server[] servers) {
			this.servers = servers;
		}
		
	}
	
	@Override
	public Server[] listServers(Access access, boolean flavorAndImage) throws OpenstackAPIException {
		Server[] servers = null;
		Configuration endpoint = this.configurationDao.findByName(ENDPOINT_SERVERS_DETAIL);
		if (endpoint != null) {
			String url = getEndpoint(access, Type.INTERNAL, endpoint.getValue());
			Servers response = RestUtils.get(url, access, Servers.class);
			if (response != null) {
				servers = response.getServers();
				for (Server server : servers) {
					if (flavorAndImage) {
						Flavor flavor = this.flavorService.getFlavor(server.getFlavor().getId());
						server.setFlavor(flavor);
						Image image = this.imageService.getImage(server.getImage().getId());
						server.setImage(image);
					}
				}
			}
		}
		return servers;
	}
	
	public static final class ServerBody implements RequestBody {
		
		private Server server;

		public Server getServer() {
			return server;
		}

		public void setServer(Server server) {
			this.server = server;
		}
		
	}
	
	@Override
	public Server getServer(Access access, String id, boolean flavorAndImage) throws OpenstackAPIException {
		Server server = null;
		Configuration endpointServer = this.configurationDao.findByName(ENDPOINT_SERVER);
		if (access != null && endpointServer != null) {
			String url = getEndpoint(access, Type.INTERNAL, endpointServer.getValue());
			try {
				ServerBody response = RestUtils.get(url, access, ServerBody.class, id);
				server = response.getServer();
				if (flavorAndImage) {
					Flavor flavor = this.flavorService.getFlavor(server.getFlavor().getId());
					server.setFlavor(flavor);
					Image image = this.imageService.getImage(server.getImage().getId());
					server.setImage(image);
				}
			} catch (OpenstackAPIException e) {
				RestUtils.handleError(e);
			}
		}
		return server;
	}

	@Override
	public Server createServer(final Access access, Server server) throws OpenstackAPIException {
		Server newServer = null;
		Configuration endpoint = this.configurationDao.findByName(ENDPOINT_SERVERS);
		if (access != null && endpoint != null) {
			String url = getEndpoint(access, Type.INTERNAL, endpoint.getValue());
			ServerBody request = new ServerBody();
			request.setServer(server);
			ServerBody response = RestUtils.postForObject(url, access, request, ServerBody.class);
			newServer = response.getServer();
			newServer = this.getServer(access, newServer.getId(), true);
			
			final String id = newServer.getId();
			
			final ServerService self = (ServerService) OpenstackUtil.getBean(ServerService.class);
			if (self != null) {
				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						while (true) {
							try {
								Server s = ServerServiceImpl.this.getServerDetail(access, id);
								if (s != null) {
									String status = s.getStatus();
									String task = s.getTask();
									self.updateServerStatus(s.getId(), status, task);
									if (status.equalsIgnoreCase("active") || status.equalsIgnoreCase("error")) {
										self.updateServerAddress(s.getId(), s);
										break;
									}
								} else {
									break;
								}
								Thread.sleep(1000);
							} catch (OpenstackAPIException e) {
								break;
							} catch (InterruptedException e) {
								break;
							}
						}
					}
					
				}, "Creating server " + server.getId());
				thread.start();
			}
		}
		return newServer;
	}

	@Override
	public void removeServer(final Access access, final String uuid) throws OpenstackAPIException {
		if (access != null && uuid != null && !uuid.trim().isEmpty()) {
			Configuration endpoint = this.configurationDao.findByName(ENDPOINT_SERVER);
			if (endpoint != null) {
				String url = getEndpoint(access, Type.INTERNAL, endpoint.getValue());
				RestUtils.delete(url, access, uuid);
				
				final ServerService self = (ServerService) OpenstackUtil.getBean("serverService");
				if (self != null) {
					self.updateServerStatus(uuid, "pending", "pending");
					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {
							while (true) {
								try {
									Server s = ServerServiceImpl.this.getServerDetail(access, uuid);
									if (s != null) {
										String status = s.getStatus();
										String task = s.getTask();
										self.updateServerStatus(s.getId(), status, task);
										if (status.equalsIgnoreCase("error")) {
											break;
										}
									} else {
										self.updateServerStatus(uuid, "deleted", null);
										break;
									}
									Thread.sleep(1000);
								} catch (OpenstackAPIException e) {
									break;
								} catch (InterruptedException e) {
									break;
								}
							}
						}
						
					}, "Removing server " + uuid);
					thread.start();
				}
			}
		}
	}

	@Override
	public void doServerAction(final Access access, final String uuid, ServerAction action) throws OpenstackAPIException {
		if (access != null && uuid != null && !uuid.trim().isEmpty()) {
			Configuration endpoint = this.configurationDao.findByName(ENDPOINT_SERVER_ACTION);
			if (endpoint != null) {
				String url = getEndpoint(access, Type.INTERNAL, endpoint.getValue());
				RestUtils.postForLocation(url, access, action, uuid);
				
				final ServerService self = (ServerService) OpenstackUtil.getBean("serverService");
				if (self != null) {
					self.updateServerStatus(uuid, "pending", "pending");
					Thread thread = new Thread(new Runnable() {

						@Override
						public void run() {
							while (true) {
								try {
									Server s = ServerServiceImpl.this.getServerDetail(access, uuid);
									if (s != null) {
										String status = s.getStatus();
										String task = s.getTask();
										self.updateServerStatus(s.getId(), status, task);
										if (task == null || task.equalsIgnoreCase("none")) {
											break;
										}
									} else {
										break;
									}
									Thread.sleep(500);
								} catch (OpenstackAPIException e) {
									break;
								} catch (InterruptedException e) {
									break;
								}
							}
						}
						
					}, "Updating server " + uuid);
					thread.start();
				}
			}
		}
	}
	
	public static final class VNCRequest {
		
		private String type;

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
		
	}
	
	public static final class VNCRequestBody implements RequestBody {
		
		@JsonProperty("os-getVNCConsole")
		private VNCRequest request;

		public VNCRequest getRequest() {
			return request;
		}

		public void setRequest(VNCRequest request) {
			this.request = request;
		}
		
	}
	
	public static final class VNCResponse {
		
		private String type;
		
		private String url;

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
		
	}
	
	public static final class VNCResponseBody implements RequestBody {
		
		@JsonProperty("console")
		private VNCResponse response;

		public VNCResponse getResponse() {
			return response;
		}

		public void setResponse(VNCResponse response) {
			this.response = response;
		}
		
	}
	
	@Override
	public String getVNCLink(Access access, String uuid, String type) throws OpenstackAPIException {
		String link = null;
		if (access != null && uuid != null && !uuid.trim().isEmpty()) {
			Configuration endpoint = this.configurationDao.findByName(ENDPOINT_SERVER_ACTION);
			if (endpoint != null) {
				String url = getEndpoint(access, Type.INTERNAL, endpoint.getValue());
				VNCRequest vncRequest = new VNCRequest();
				vncRequest.setType(type);
				VNCRequestBody request = new VNCRequestBody();
				request.setRequest(vncRequest);
				VNCResponseBody response = RestUtils.postForObject(url, access, request, VNCResponseBody.class, uuid);
				if (response != null) {
					link = response.getResponse().getUrl();
				}
			}
		}
		return link;
	}
	
	@Override
	public void resizeServer(final Access access, final String uuid, final String flavorId) throws OpenstackAPIException {
		if (access != null && uuid != null && !uuid.trim().isEmpty() && flavorId != null && !flavorId.trim().isEmpty()) {
			Server server = this.getServer(access, uuid, false);
			if (server != null && !server.getFlavor().getId().equalsIgnoreCase(flavorId)) {
				Flavor flavor = this.flavorService.getFlavor(flavorId);
				if (flavor != null) {
					Configuration endpoint = this.configurationDao.findByName(ENDPOINT_SERVER_ACTION);
					if (endpoint != null) {
						final String url = getEndpoint(access, Type.INTERNAL, endpoint.getValue());
						ServerAction action = new ResizeServer(flavorId);
						RestUtils.postForLocation(url, access, action, uuid);
						
						ServerService self = (ServerService) OpenstackUtil.getBean("serverService");
						if (self != null) {
							self.updateServerStatus(uuid, "pending", "pending");
							Thread thread = new Thread(new Runnable() {
								@Override
								public void run() {
									boolean confirmed = false;
									while (true) {
										try {
											Thread.sleep(1000);
											Server s = ServerServiceImpl.this.getServerDetail(access, uuid);
											if (s != null) {
												String status = s.getStatus();
												String task = s.getTask();
												ServerService self = (ServerService) OpenstackUtil.getBean("serverService");
												self.updateServerStatus(s.getId(), status, task);
												if (status.equalsIgnoreCase("resized") && !confirmed) {
													RestUtils.postForLocation(url, access, new ConfirmResizeServer(), uuid);
													confirmed = true;
												} else if ((task == null || task.equalsIgnoreCase("none")) && status.equalsIgnoreCase("active")) {
													self.updateServerFlavor(uuid, flavorId);
													break;
												}
											} else {
												break;
											}
										} catch (OpenstackAPIException e) {
											break;
										} catch (InterruptedException e) {
											break;
										}
									}
								}
								
							}, "Resizing server " + uuid);
							thread.start();
						}
					}
				}
			}
		}		
	}
	
	@Override
	public void updateServerStatus(String uuid, String status, String task) {
		Instance instance = this.instanceDao.findByObject("uuid", uuid);
		if (instance != null) {
			Date now = new Date();
			String oldStatus = instance.getStatus();
			if (!oldStatus.equals(status)) {
				InstanceStatus instanceStatus = new InstanceStatus();
				instanceStatus.setUuid(uuid);
				instanceStatus.setStatus(status);
				instanceStatus.setUpdateTime(now);				
				this.instanceStatusDao.persist(instanceStatus);
			}
			instance.setStatus(status);
			instance.setTask(task != null ? task : "");
			instance.setUpdateTime(now);
		}
	}
	
	@Override
	public void updateServerAddress(String uuid, Server server) {
		VirtualMachine vm = this.vmDao.findByObject("uuid", uuid);
		Map<String, Address[]> addresses = server.getAddresses();
		if (!addresses.isEmpty()) {
			Iterator<Address[]> it = addresses.values().iterator();
			if (it.hasNext()) {
				Address[] addr = it.next();
				if (addr.length > 0) {
					vm.setFixedIp(addr[0].getAddr());
				}
			}
		}
		this.vmDao.persist(vm);		
	}
	
	@Override
	public void updateServerFlavor(String uuid, String flavorId) {
		VirtualMachine vm = this.vmDao.findByObject("uuid", uuid);
		vm.setFlavor(flavorId);
		this.vmDao.persist(vm);		
	}
	
	private Server getServerDetail(Access access, String id) throws OpenstackAPIException {
		Server server = null;
		Configuration endpointServer = this.configurationDao.findByName(ENDPOINT_SERVER);
		if (access != null && endpointServer != null) {
			String url = getEndpoint(access, Type.INTERNAL, endpointServer.getValue());
			try {
				ServerBody response = RestUtils.get(url, access, ServerBody.class, id);
				server = response.getServer();
			} catch (OpenstackAPIException e) {
				RestUtils.handleError(e);
			}
		}
		return server;
	}
	
	private static String getEndpoint(Access access, Type type, String suffix) {
		return RestUtils.getEndpoint(access, "nova", type, suffix);
	}

}
