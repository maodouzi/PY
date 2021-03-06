package com.inforstack.openstack.test.api;

import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.inforstack.openstack.api.OpenstackAPIException;
import com.inforstack.openstack.api.keystone.Access;
import com.inforstack.openstack.api.keystone.KeystoneService;
import com.inforstack.openstack.api.nova.flavor.Flavor;
import com.inforstack.openstack.api.nova.flavor.FlavorService;
import com.inforstack.openstack.api.nova.image.Image;
import com.inforstack.openstack.api.nova.image.ImageService;
import com.inforstack.openstack.api.nova.server.Address;
import com.inforstack.openstack.api.nova.server.SecurityGroup;
import com.inforstack.openstack.api.nova.server.Server;
import com.inforstack.openstack.api.nova.server.Server.Network;
import com.inforstack.openstack.api.nova.server.ServerService;
import com.inforstack.openstack.api.nova.server.impl.HardRebootServer;
import com.inforstack.openstack.api.nova.server.impl.StartServer;
import com.inforstack.openstack.api.nova.server.impl.StopServer;
import com.inforstack.openstack.configuration.ConfigurationDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-context.xml"})
public class NovaTest {

	@Autowired
	private FlavorService flavorService;
	
	@Autowired
	private ImageService imageService;
	
	@Autowired
	private ServerService serverService;
	
	@Autowired
	private KeystoneService keystoneService;
	
	@Autowired
	private ConfigurationDao configurationDao;
	
	private Access access;
	
	@Before
	public void setUp() throws Exception {
		String tenant = this.configurationDao.findByName(KeystoneService.TENANT_DEMO_ID).getValue();
		String username = this.configurationDao.findByName(KeystoneService.USER_ADMIN_NAME).getValue();
		String password = this.configurationDao.findByName(KeystoneService.USER_ADMIN_PASS).getValue();
		this.access = this.keystoneService.getAccess(username, password, tenant, true);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testListFlavors() {
		try {
			Flavor[] flavors = this.flavorService.listFlavors();
			Assert.assertNotNull(flavors);
			Assert.assertTrue(flavors.length > 0);
			System.out.println("\n\n\n");
			System.out.println("======= Flavors =======");
			for (Flavor flavor : flavors) {
				System.out.println("-----------------------");
				System.out.println("ID:        " + flavor.getId());
				System.out.println("Name:      " + flavor.getName());
				System.out.println("CPU:       " + flavor.getVcpus());
				System.out.println("RAM:       " + flavor.getRam());
				System.out.println("Disk:      " + flavor.getDisk());
				System.out.println("Ephemeral: " + flavor.getEphemeral());
				System.out.println("Factor:    " + flavor.getFactor());
				System.out.println("SWAP:      " + flavor.getSwap());
				System.out.println("Disabled:  " + flavor.isDisabled());
				System.out.println("Public:    " + flavor.isAccessPublic());
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateAndRemoveFlavor() {
		try {
			Flavor flavor = this.flavorService.createFlavor("flavor.test1", 1, 512, 10);
			if (flavor != null) {
				flavor = this.flavorService.getFlavor(flavor.getId());
				System.out.println("\n\n\n");
				System.out.println("======= Create Flavor =======");
				System.out.println("ID:        " + flavor.getId());
				System.out.println("Name:      " + flavor.getName());
				System.out.println("CPU:       " + flavor.getVcpus());
				System.out.println("RAM:       " + flavor.getRam());
				System.out.println("Disk:      " + flavor.getDisk());
				System.out.println("Ephemeral: " + flavor.getEphemeral());
				System.out.println("Factor:    " + flavor.getFactor());
				System.out.println("SWAP:      " + flavor.getSwap());
				System.out.println("Disabled:  " + flavor.isDisabled());
				System.out.println("Public:    " + flavor.isAccessPublic());
				
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
				this.flavorService.removeFlavor(flavor);
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListImages() {
		try {
			Image[] images = this.imageService.listImages();
			Assert.assertNotNull(images);
			Assert.assertTrue(images.length > 0);
			System.out.println("\n\n\n");
			System.out.println("======= Images =======");
			for (Image image : images) {
				System.out.println("----------------------");
				System.out.println("ID:        " + image.getId());
				System.out.println("Name:      " + image.getName());
				System.out.println("Created:   " + image.getCreated());
				System.out.println("Updated:   " + image.getUpdated());
				System.out.println("Tenant:    " + image.getTenant());
				System.out.println("User:      " + image.getUser());
				System.out.println("Status:    " + image.getStatus());
				System.out.println("Progress:  " + image.getProgress());
				System.out.println("MinDisk:   " + image.getMinDisk());
				System.out.println("MinRAM:    " + image.getMinRam());
				System.out.println("Server:    " + (image.getServer() != null ? image.getServer().getId() : null));
				System.out.println("Metadata:  [");
				Map<String, String> metadata = image.getMetadata();
				if (metadata != null) {
					Iterator<Entry<String, String>> it = metadata.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, String> entry = it.next();
						System.out.println(entry.getKey() + ":\t\t" + entry.getValue());
					}
				}
				System.out.println("]");
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListServers() {
		try {
			if (this.access != null) {
				Server[] servers = this.serverService.listServers(this.access, true);
				Assert.assertNotNull(servers);
				Assert.assertTrue(servers.length > 0);
				System.out.println("\n\n\n");
				System.out.println("======= Servers =======");
				for (Server server : servers) {
					System.out.println("----------- -----------");
					System.out.println("ID        : " + server.getId());
					System.out.println("Name      : " + server.getName());
					System.out.println("Status    : " + server.getStatus());
					System.out.println("Tenant    : " + server.getTenant());
					System.out.println("User      : " + server.getUser());
					System.out.println("Host ID   : " + server.getHostId());
					System.out.println("Host Name : " + server.getHostName());
					System.out.println("Key Name  : " + server.getKey());
					System.out.println("Image     : " + server.getImage().getId());
					System.out.println("Flavor    : " + (server.getFlavor() != null ? server.getFlavor().getId() : "None"));
					System.out.println("Addresses : {");
					Iterator<Entry<String, Address[]>> itAddress = server.getAddresses().entrySet().iterator();
					while (itAddress.hasNext()) {
						Entry<String, Address[]> entry = itAddress.next();
						System.out.println("        " + entry.getKey() + " : [");
						Address[] list = entry.getValue();
						for (Address address : list) {
							System.out.println("                {");
							System.out.println("                        Version  : " + address.getVersion());
							System.out.println("                        Addr     : " + address.getAddr());
							System.out.println("                }");
						}
						System.out.println("        ]");
					}
					System.out.println("}");
					System.out.println("Security  : [");
					for (SecurityGroup securityGroups : server.getSecurityGroups()) {
						System.out.println("        {");
						System.out.println("                Name  : " + securityGroups.getName());
						System.out.println("        }");
					}
					System.out.println("]");
					System.out.println("Updated   : " + server.getUpdated());
					System.out.println("Metadata  : [");
					Map<String, String> metadata = server.getMetadata();
					if (metadata != null) {
						Iterator<Entry<String, String>> it = metadata.entrySet().iterator();
						while (it.hasNext()) {
							Entry<String, String> entry = it.next();
							System.out.println(entry.getKey() + ":\t\t" + entry.getValue());
						}
					}
					System.out.println("]");
				}
			} else {
				fail("Can not get access");
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testStartAndStopServer() {
		try {
			if (this.access != null) {
				Server server = null;
				
				Server[] servers = this.serverService.listServers(this.access, false);
				if (servers.length > 0) {
					for (Server s : servers) {
						if (s.getStatus().equalsIgnoreCase("active")) {
							server = s;
							break;
						}
					}
				}
				if (server != null) {
					this.serverService.doServerAction(this.access, server, new StopServer());
					
					System.out.println("\n\n\nStopping server");
					System.out.println("ID    : " + server.getId());
					System.out.println("");
					int wait = 0;
					while (wait < 100) {
						server = this.serverService.getServer(access, server.getId(), false);
						if (server.getStatus().equalsIgnoreCase("stopped") && (server.getTask() == null || server.getTask().equalsIgnoreCase("none")) && server.getPower() != 1) {
							break;
						} else {
							System.out.println("--" + server.getStatus() + "|" + (server.getTask() != null ? server.getTask() : "None") + "|" + (server.getPower() == 1 ? "On" : "Off") + "...");
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						wait++;
					}
					
					this.serverService.doServerAction(this.access, server, new StartServer());
					System.out.println("\n\n\nStarting server");
					System.out.println("ID    : " + server.getId());
					System.out.println("");
					wait = 0;
					while (wait < 100) {
						server = this.serverService.getServer(access, server.getId(), false);
						if (server.getStatus().equalsIgnoreCase("active") && (server.getTask() == null || server.getTask().equalsIgnoreCase("none")) && server.getPower() == 1) {
							break;
						} else {
							System.out.println("--" + server.getStatus() + "|" + (server.getTask() != null ? server.getTask() : "None") + "|" + (server.getPower() == 1 ? "On" : "Off") + "...");
							try {
								Thread.sleep(200);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						wait++;
					}
				} else {
					fail("No active server");
				}
			} else {
				fail("Can not get access");
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testRebootServer() {
		try {
			if (this.access != null) {
				Server server = null;
				
				Server[] servers = this.serverService.listServers(this.access, false);
				if (servers.length > 0) {
					for (Server s : servers) {
						if (s.getStatus().equalsIgnoreCase("active")) {
							server = s;
							break;
						}
					}
				}
				if (server != null) {
					this.serverService.doServerAction(this.access, server, new HardRebootServer());
					System.out.println("\n\n\nRebooting server");
					System.out.println("ID    : " + server.getId());
					System.out.println("");
					int wait = 0;
					while (wait < 10000) {
						server = this.serverService.getServer(access, server.getId(), false);
						if (server.getStatus().equalsIgnoreCase("active") && (server.getTask() == null || server.getTask().equalsIgnoreCase("none")) && server.getPower() == 1) {
							break;
						} else {
							System.out.println("--" + server.getStatus() + "|" + (server.getTask() != null ? server.getTask() : "None") + "|" + (server.getPower() == 1 ? "On" : "Off") + "...");
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						wait++;
					}
				} else {
					fail("No active server");
				}
			} else {
				fail("Can not get access");
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateAndRemoveServer() {
		try {
			if (this.access != null) {
				Server[] servers = this.serverService.listServers(this.access, false);
				int size = servers.length;
				
				Server newServer = new Server();
				newServer.setName("Test Create Server");
				newServer.setImageRef("b431691a-1d37-4f1e-997d-7af8f37a748e");
				newServer.setFlavorRef("1");
				newServer.setMetadata(new HashMap<String, String>());
				newServer.getMetadata().put("1", "M1");
				newServer.getMetadata().put("2", "M2");
//				File[] files = new File[1];
//				files[0] = new File();
//				files[0].setPath("~/test.txt");	
//				files[0].setContents("ICAgICAgDQoiQSBjbG91ZCBkb2VzIG5vdCBrbm93IHdoeSBp dCBtb3ZlcyBpbiBqdXN0IHN1Y2ggYSBkaXJlY3Rpb24gYW5k IGF0IHN1Y2ggYSBzcGVlZC4uLkl0IGZlZWxzIGFuIGltcHVs c2lvbi4uLnRoaXMgaXMgdGhlIHBsYWNlIHRvIGdvIG5vdy4g QnV0IHRoZSBza3kga25vd3MgdGhlIHJlYXNvbnMgYW5kIHRo ZSBwYXR0ZXJucyBiZWhpbmQgYWxsIGNsb3VkcywgYW5kIHlv dSB3aWxsIGtub3csIHRvbywgd2hlbiB5b3UgbGlmdCB5b3Vy c2VsZiBoaWdoIGVub3VnaCB0byBzZWUgYmV5b25kIGhvcml6 b25zLiINCg0KLVJpY2hhcmQgQmFjaA==");
//				newServer.setPersonality(files);
				Network[] networks = new Network[1];
				networks[0] = new Network();
				networks[0].setUuid("9074c0c4-b258-4615-b1de-d05f18114f5c");
				newServer.setNetworks(networks);
				
				Server server = this.serverService.createServer(this.access, newServer);
				Assert.assertNotNull(server);
				Assert.assertFalse(server.getId().isEmpty());
				Assert.assertEquals(this.serverService.listServers(this.access, false).length, size + 1);
				
				System.out.println("\n\n\nCreate new server");
				System.out.println("New ID    : " + server.getId());
				System.out.println("");
				int wait = 0;
				while (wait < 100) {
					server = this.serverService.getServer(access, server.getId(), false);
					if (server.getStatus().equalsIgnoreCase("active") && server.getTask() == null && server.getPower() == 1) {
						break;
					} else {
						System.out.println("--" + server.getStatus() + "|" + (server.getTask() != null ? server.getTask() : "None") + "|" + (server.getPower() == 1 ? "On" : "Off") + "...");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					wait++;
				}
				
				System.out.println("New ID    : " + server.getId());
				System.out.println("Name      : " + server.getName());
				System.out.println("Status    : " + server.getStatus());
				System.out.println("Task      : " + server.getTask());
				System.out.println("Power     : " + server.getPower());
				System.out.println("Tenant    : " + server.getTenant());
				System.out.println("User      : " + server.getUser());
				System.out.println("Host ID   : " + server.getHostId());
				System.out.println("Host Name : " + server.getHostName());
				System.out.println("Key Name  : " + server.getKey());
				System.out.println("Image     : " + server.getImage().getId());
				System.out.println("Flavor    : " + server.getFlavor().getId());
				System.out.println("Addresses : {");
				Iterator<Entry<String, Address[]>> itAddress = server.getAddresses().entrySet().iterator();
				while (itAddress.hasNext()) {
					Entry<String, Address[]> entry = itAddress.next();
					System.out.println("        " + entry.getKey() + " : [");
					Address[] list = entry.getValue();
					for (Address address : list) {
						System.out.println("                {");
						System.out.println("                        Version  : " + address.getVersion());
						System.out.println("                        Addr     : " + address.getAddr());
						System.out.println("                }");
					}
					System.out.println("        ]");
				}
				System.out.println("}");
				System.out.println("Security  : [");
				for (SecurityGroup securityGroups : server.getSecurityGroups()) {
					System.out.println("        {");
					System.out.println("                Name  : " + securityGroups.getName());
					System.out.println("        }");
				}
				System.out.println("]");
				System.out.println("Updated   : " + server.getUpdated());
				System.out.println("Metadata  : [");
				Map<String, String> metadata = server.getMetadata();
				if (metadata != null) {
					Iterator<Entry<String, String>> it = metadata.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, String> entry = it.next();
						System.out.println(entry.getKey() + ":\t\t" + entry.getValue());
					}
				}
				System.out.println("]");
				
				this.serverService.removeServer(this.access, server);
				
				wait = 0;
				while (wait < 100) {
					try {
						server = this.serverService.getServer(access, server.getId(), false);
					} catch (OpenstackAPIException e) {
						break;
					}
					if (server == null) {
						break;
					} else {
						System.out.println("--" + server.getStatus() + "|" + (server.getTask() != null ? server.getTask() : "None") + "|" + (server.getPower() == 1 ? "On" : "Off") + "...");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					wait++;
				}
			} else {
				fail("Can not get access");
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

}
