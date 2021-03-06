package com.inforstack.openstack.test.api;

import static org.junit.Assert.fail;

import java.util.Date;
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
import com.inforstack.openstack.api.cinder.Attachment;
import com.inforstack.openstack.api.cinder.CinderService;
import com.inforstack.openstack.api.cinder.Volume;
import com.inforstack.openstack.api.cinder.VolumeAttachment;
import com.inforstack.openstack.api.cinder.VolumeSnapshot;
import com.inforstack.openstack.api.cinder.VolumeType;
import com.inforstack.openstack.api.keystone.Access;
import com.inforstack.openstack.api.keystone.KeystoneService;
import com.inforstack.openstack.configuration.ConfigurationDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/test-context.xml"})
public class CinderTest {
	
	@Autowired
	private CinderService cinderService;
	
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
	public void testListVolumeTypes() {
		try {
			VolumeType[] types = this.cinderService.listVolumeTypes();
			Assert.assertNotNull(types);
			Assert.assertTrue(types.length > 0);
			System.out.println("\n\n\n");
			System.out.println("======= Volume Types =======");
			for (VolumeType type : types) {
				System.out.println("---------------------------");
				System.out.println("ID:           " + type.getId());
				System.out.println("Name:         " + type.getName());
				Map<String, String> extra = type.getExtra();
				System.out.println("Extra:        " + (extra != null ? extra.size() : 0));
				if (extra != null) {
					Iterator<Entry<String, String>> it = extra.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, String> entry = it.next();
						System.out.println(entry.getKey() + ":\t\t" + entry.getValue());
					}
				}
				System.out.println("\n\n");
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}

	@Test
	public void testCreateAndRemoveVolumeType() {
		try {
			VolumeType[] types = this.cinderService.listVolumeTypes();
			Assert.assertNotNull(types);
			int size = types.length;
			VolumeType type = this.cinderService.createVolumeType("testVolumeType");
			Assert.assertNotNull(type);
			Assert.assertTrue(type.getName().equals("testVolumeType"));
			Assert.assertFalse(type.getId() == null || type.getId().isEmpty());
			Assert.assertTrue((this.cinderService.listVolumeTypes().length == (size + 1)));
			this.cinderService.removeVolumeType(type.getId());
			Assert.assertTrue((this.cinderService.listVolumeTypes().length == size));
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListVolumes() {
		try {
			Volume[] volumes = this.cinderService.listVolumes(this.access);
			Assert.assertNotNull(volumes);
			Assert.assertTrue(volumes.length > 0);
			System.out.println("\n\n\n");
			System.out.println("======= Volumes =======");
			for (Volume volume : volumes) {
				System.out.println("-----------------------");
				System.out.println("ID:           " + volume.getId());
				System.out.println("Name:         " + volume.getName());
				System.out.println("Description:  " + volume.getDescription());
				System.out.println("Size:         " + volume.getSize());
				System.out.println("Bootable:     " + volume.isBootable());
				System.out.println("Type:         " + volume.getType());
				System.out.println("Snapshot:     " + volume.getSnapshot());
				System.out.println("Source:       " + volume.getSource());
				System.out.println("Zone:         " + volume.getZone());
				System.out.println("Created At:   " + volume.getCreated());
				System.out.println("Status:       " + volume.getStatus());
				Map<String, String> metadata = volume.getMetadata();
				System.out.println("Metadata:     " + (metadata != null ? metadata.size() : 0));
				if (metadata != null) {
					Iterator<Entry<String, String>> it = metadata.entrySet().iterator();
					while (it.hasNext()) {
						Entry<String, String> entry = it.next();
						System.out.println(entry.getKey() + ":\t\t" + entry.getValue());
					}
				}
				Attachment[] attachments = volume.getAttachments();
				System.out.println("Attachments:  " + (attachments != null ? attachments.length : 0));
				if (attachments != null) {
					for (Attachment attachment : attachments) {
						System.out.println("{");
						System.out.println("ID:           " + attachment.getId());
						System.out.println("Device:       " + attachment.getDevice());
						System.out.println("Server:       " + attachment.getServer());
						System.out.println("Volume:       " + attachment.getVolume());
						System.out.println("}");
					}
				}
				System.out.println("\n\n");
			}
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateAndRemoveVolume() {
		try {
			VolumeType[] types = this.cinderService.listVolumeTypes();
			Assert.assertNotNull(types);
			//Assert.assertTrue(types.length > 0);
			
			Volume[] volumes = this.cinderService.listVolumes(this.access);
			Assert.assertNotNull(volumes);
			int size = volumes.length;
			String type = "";
			if (types.length > 0) type = types[0].getId();
			Volume volume1 = this.cinderService.createVolume(this.access, "testVolume1", "", 1, false, type, "nova");
			Assert.assertNotNull(volume1);
			Assert.assertTrue(volume1.getName().equals("testVolume1"));
			Assert.assertFalse(volume1.getId() == null || volume1.getId().isEmpty());
			Assert.assertTrue((this.cinderService.listVolumes(this.access).length == (size + 1)));
			volume1 = this.cinderService.getVolume(this.access, volume1.getId());
			while (volume1 != null && !(volume1.getStatus().equalsIgnoreCase("available") || volume1.getStatus().equalsIgnoreCase("error"))) {
				volume1 = this.cinderService.getVolume(this.access, volume1.getId());
			}
			this.cinderService.removeVolume(this.access, volume1.getId());
			volume1 = this.cinderService.getVolume(this.access, volume1.getId());
			while (volume1 != null) {
				volume1 = this.cinderService.getVolume(this.access, volume1.getId());
			}
			Assert.assertTrue((this.cinderService.listVolumes(this.access).length == size));
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateAndRemoveVolumeFromSnapshot() {
		try {
			VolumeType[] types = this.cinderService.listVolumeTypes();
			Assert.assertNotNull(types);
			Assert.assertTrue(types.length > 0);
			
			Volume[] volumes = this.cinderService.listVolumes(this.access);
			Assert.assertNotNull(volumes);
			int size = volumes.length;
			Volume volume2 = this.cinderService.createVolumeFromSnapshot(this.access, "testVolume2", "", 1, false, types[0].getId(), "6b0a6566-a6af-4f1b-a42b-c4c833988b77", "nova");
			Assert.assertNotNull(volume2);
			Assert.assertTrue(volume2.getName().equals("testVolume2"));
			Assert.assertFalse(volume2.getId() == null || volume2.getId().isEmpty());
			Assert.assertTrue((this.cinderService.listVolumes(this.access).length == (size + 1)));
			volume2 = this.cinderService.getVolume(this.access, volume2.getId());
			while (volume2 != null && !(volume2.getStatus().equalsIgnoreCase("available") || volume2.getStatus().equalsIgnoreCase("error"))) {
				volume2 = this.cinderService.getVolume(this.access, volume2.getId());
			}
			this.cinderService.removeVolume(this.access, volume2.getId());
			volume2 = this.cinderService.getVolume(this.access, volume2.getId());
			while (volume2 != null) {
				volume2 = this.cinderService.getVolume(this.access, volume2.getId());
			}
			Assert.assertTrue((this.cinderService.listVolumes(this.access).length == size));
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateAndRemoveVolumeFroVolume() {
		try {
			VolumeType[] types = this.cinderService.listVolumeTypes();
			Assert.assertNotNull(types);
			Assert.assertTrue(types.length > 0);
			
			Volume[] volumes = this.cinderService.listVolumes(this.access);
			Assert.assertNotNull(volumes);
			int size = volumes.length;
			Volume volume3 = this.cinderService.createVolumeFromVolume(this.access, "testVolume3", "", 1, false, types[0].getId(), volumes[0].getId(), "nova");
			Assert.assertNotNull(volume3);
			Assert.assertTrue(volume3.getName().equals("testVolume3"));
			Assert.assertFalse(volume3.getId() == null || volume3.getId().isEmpty());
			Assert.assertTrue((this.cinderService.listVolumes(this.access).length == (size + 1)));
			volume3 = this.cinderService.getVolume(this.access, volume3.getId());
			while (volume3 != null && !(volume3.getStatus().equalsIgnoreCase("available") || volume3.getStatus().equalsIgnoreCase("error"))) {
				volume3 = this.cinderService.getVolume(this.access, volume3.getId());
			}
			this.cinderService.removeVolume(this.access, volume3.getId());
			volume3 = this.cinderService.getVolume(this.access, volume3.getId());
			while (volume3 != null) {
				volume3 = this.cinderService.getVolume(this.access, volume3.getId());
			}
			Assert.assertTrue((this.cinderService.listVolumes(this.access).length == size));
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListVolumesFromServer() {
		try {
			VolumeAttachment[] volumes = this.cinderService.listAttachedVolumes(access, "054aaa09-afaa-4043-908c-139e6ebf745e");
			Assert.assertNotNull(volumes);

			Assert.assertTrue(volumes.length == 1);
			VolumeAttachment volume = volumes[0];
			
			Assert.assertTrue(volume.getVolumeId().equals("1ba9e243-3103-496f-b3aa-01e05ed9f7de"));
			Assert.assertTrue(volume.getDevice().equals("/dev/vdttached"));
			Assert.assertTrue(volume.getServerId().equals("054aaa09-afaa-4043-908c-139e6ebf745e"));
			
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		}
	}
	
	@Test
	public void testAttachDetatchVolumeFromServer() {
		try {
			VolumeAttachment volume = this.cinderService.attachVolume(access, "054aaa09-afaa-4043-908c-139e6ebf745e", "1ba9e243-3103-496f-b3aa-01e05ed9f7de", "/dev/vdttached");
			
			Assert.assertTrue(volume.getVolumeId().equals("1ba9e243-3103-496f-b3aa-01e05ed9f7de"));
			Assert.assertTrue(volume.getDevice().equals("/dev/vdttached"));
			Assert.assertTrue(volume.getServerId().equals("054aaa09-afaa-4043-908c-139e6ebf745e"));
			
			this.cinderService.detachVolume(access, "054aaa09-afaa-4043-908c-139e6ebf745e", volume.getId());
			
			Thread.sleep(5000);
			
			VolumeAttachment[] volumes = this.cinderService.listAttachedVolumes(access, "054aaa09-afaa-4043-908c-139e6ebf745e");
			Assert.assertNotNull(volumes);

			Assert.assertTrue(volumes.length == 0);
			
			
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testCreateSnapshotFromVolume() {
		try {
			String volumeID = "fbe3b847-1976-4f3b-8ad4-8f728df439c7";
			String snapshotName = "unitTest";
			String snapshotDesc = "for unit Test Purpose";
					
			VolumeSnapshot volumeSnapshot = this.cinderService.createVolumeSnapshot(access, volumeID,snapshotName,snapshotDesc,true);
			
			Assert.assertTrue(volumeSnapshot.getVolume_id().equals(volumeID));
			Assert.assertTrue(volumeSnapshot.getDisplay_name().equals(snapshotName));
			Assert.assertTrue(volumeSnapshot.getDisplay_description().equals(snapshotDesc));
			
			this.cinderService.deletelVolumeSnapshot(access, volumeSnapshot.getId());
						
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListVolumeSnapshot() {
		try {
			String volumeID = "fbe3b847-1976-4f3b-8ad4-8f728df439c7";
			String snapshotName = "Unit Test";
			String snapshotDesc = "for unit Test Purpose";
					
			VolumeSnapshot volumeSnapshot = this.cinderService.createVolumeSnapshot(access, volumeID,snapshotName,snapshotDesc,true);
			
			Assert.assertTrue(volumeSnapshot.getVolume_id().equals(volumeID));
			Assert.assertTrue(volumeSnapshot.getDisplay_name().equals(snapshotName));
			Assert.assertTrue(volumeSnapshot.getDisplay_description().equals(snapshotDesc));
			
			VolumeSnapshot[] volumeSnapshots = this.cinderService.listVolumeSnapshots(access);
			
			Assert.assertTrue(volumeSnapshots.length == 2);
			
			Assert.assertTrue(volumeSnapshots[1].getSize() == 10);
			
			Date now = new Date();
			
			Assert.assertTrue(volumeSnapshots[1].getCreated_at().getYear() == now.getYear());
			
			Assert.assertTrue(volumeSnapshots[1].getCreated_at().getMonth() == now.getMonth());

			Assert.assertTrue(volumeSnapshots[1].getCreated_at().getDate() == now.getDate());
			
			volumeSnapshot = this.cinderService.getDetailVolumeSnapshot(access, volumeSnapshots[1].getId());
			
			Assert.assertTrue(volumeSnapshots[1].equals(volumeSnapshot));

			this.cinderService.deletelVolumeSnapshot(access, volumeSnapshot.getId());
						
		} catch (OpenstackAPIException e) {
			fail(e.getMessage());
			e.printStackTrace();
		} catch (Exception e){
			e.printStackTrace();
		}
	}

}
