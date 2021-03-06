package com.inforstack.openstack.controller.item;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.inforstack.openstack.api.nova.flavor.FlavorService;
import com.inforstack.openstack.api.nova.image.ImageService;
import com.inforstack.openstack.controller.model.CategoryModel;
import com.inforstack.openstack.controller.model.I18nModel;
import com.inforstack.openstack.controller.model.ItemSpecificationModel;
import com.inforstack.openstack.controller.model.PriceModel;
import com.inforstack.openstack.controller.model.ProfileModel;
import com.inforstack.openstack.exception.ApplicationException;
import com.inforstack.openstack.item.Category;
import com.inforstack.openstack.item.ItemService;
import com.inforstack.openstack.item.ItemSpecification;
import com.inforstack.openstack.item.Profile;
import com.inforstack.openstack.log.Logger;
import com.inforstack.openstack.utils.OpenstackUtil;

@Controller
@RequestMapping(value = "/item")
public class ItemController {
	
	private static final Logger log = new Logger(ItemController.class);
	
	@Autowired
	private ItemService itemService;
	
	@Autowired
	private ImageService imageService;

	@Autowired
	private FlavorService flavorService;
	
	@RequestMapping(value = "/category/list", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody List<CategoryModel> listCategory(Model model, boolean excludeDisabled, boolean withItems) {
		ArrayList<CategoryModel> models = new ArrayList<CategoryModel>();
		Integer languageId = OpenstackUtil.getLanguage().getId();
		List<Category> categories = this.itemService.listAllCategory(excludeDisabled);
		for (Category category : categories) {
			I18nModel[] name = new I18nModel[1];
			name[0] = new I18nModel();
			name[0].setLanguageId(languageId);
			name[0].setContent(category.getName().getI18nContent());
			
			CategoryModel cm = new CategoryModel();
			cm.setId(category.getId());
			cm.setName(name);
			cm.setEnable(category.getEnable());
			cm.setSystem(category.getSystem());
			
			if (withItems) {
				List<ItemSpecification> itemSpecifications = this.itemService.listItemSpecificationByCategory(category);
				ItemSpecificationModel[] itemSpecificationModels = new ItemSpecificationModel[itemSpecifications.size()];
				int idx = 0;
				for (ItemSpecification itemSpecification : itemSpecifications) {
					I18nModel[] itemName = new I18nModel[1];
					itemName[0] = new I18nModel();
					itemName[0].setLanguageId(languageId);
					itemName[0].setContent(itemSpecification.getName().getI18nContent());
					
					ItemSpecificationModel itemSpecificationModel = new ItemSpecificationModel();
					itemSpecificationModel.setId(itemSpecification.getId());
					itemSpecificationModel.setName(itemName);
					itemSpecificationModel.setOsType(itemSpecification.getOsType());
					itemSpecificationModel.setRefId(itemSpecification.getRefId());
					itemSpecificationModel.setDefaultPrice(itemSpecification.getDefaultPrice());
					itemSpecificationModel.setAvailable(itemSpecification.getAvailable());
					itemSpecificationModel.setCreated(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, OpenstackUtil.getLocale()).format(itemSpecification.getCreated()));
					itemSpecificationModel.setUpdated(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, OpenstackUtil.getLocale()).format(itemSpecification.getUpdated()));
					
					Profile profile = itemSpecification.getProfile();
					if (profile != null) {
						ProfileModel profileModel = new ProfileModel();
						profileModel.setItem(itemSpecification.getId());
						if (profile.getCpu() != null) {
							profileModel.setCpu(profile.getCpu().getId());
						}
						if (profile.getMemory() != null) {
							profileModel.setMemory(profile.getMemory().getId());
						}
						if (profile.getDisk() != null) {
							profileModel.setDisk(profile.getDisk().getId());
						}
						if (profile.getNetwork() != null) {
							profileModel.setNetwork(profile.getNetwork().getId());
						}
						
						itemSpecificationModel.setProfile(profileModel);
					}
					
					itemSpecificationModels[idx++] = itemSpecificationModel;
				}
				cm.setItemSpecifications(itemSpecificationModels);
			}
			models.add(cm);
		}
		return models;
	}
	
	@RequestMapping(value = "/category", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> createCategory(Model model, @RequestBody CategoryModel categoryModel) {
		Map<String, Object> returnValue = new HashMap<String, Object>();
		if (categoryModel != null) {
			try {
				Category category = this.itemService.createCategory(categoryModel);
				if (category != null) {
					returnValue.put("success", "success");
				}
			} catch (ApplicationException e) {
				log.error(e.getMessage(), e);
			}
			
		}
		return returnValue;
	}
	
	@RequestMapping(value = "/category", method = RequestMethod.PUT, produces = "application/json")
	public @ResponseBody Map<String, Object> updateCategory(Model model, @RequestBody CategoryModel categoryModel) {
		Map<String, Object> returnValue = new HashMap<String, Object>();
		if (categoryModel != null) {
			try {
				this.itemService.updateCategory(categoryModel);
				returnValue.put("success", "success");
			} catch (ApplicationException e) {
				log.error(e.getMessage(), e);
			}
		}
		return returnValue;
	}
	
	@RequestMapping(value = "/category", method = RequestMethod.DELETE, produces = "application/json")
	public @ResponseBody Map<String, Object> removeCategory(Model model, @RequestParam("id") Integer categoryId) {
		Map<String, Object> returnValue = new HashMap<String, Object>();
		if (categoryId != null) {
			try {
				this.itemService.removeCategory(categoryId);
				returnValue.put("success", "success");
			} catch (ApplicationException e) {
				log.error(e.getMessage(), e);
			}
		}
		return returnValue;
	}
	
	@RequestMapping(value = "/", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> createItemSpecification(Model model, @RequestBody ItemSpecificationModel itemSpecificationModel) {
		Map<String, Object> returnValue = new HashMap<String, Object>();
		if (itemSpecificationModel != null) {
			try {
				ItemSpecification itemSpecification = this.itemService.createItem(itemSpecificationModel);
				if (itemSpecification != null) {
					returnValue.put("success", "success");
				}
			} catch (ApplicationException e) {
				log.error(e.getMessage(), e);
			}
			
		}
		return returnValue;
	}
	
	@RequestMapping(value = "/editPrice", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> editPrice(Model model, @RequestBody PriceModel priceModel) {
		Map<String, Object> returnValue = new HashMap<String, Object>();
		if (priceModel != null) {
			try {
				this.itemService.updateItemSpecificationPrice(priceModel);
				returnValue.put("success", "success");
			} catch (ApplicationException e) {
				log.error(e.getMessage(), e);
			}
			
		}
		return returnValue;
	}

}
