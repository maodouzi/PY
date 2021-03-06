package com.inforstack.openstack.controller.user;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.util.WebUtils;

import com.inforstack.openstack.controller.model.CartItemModel;
import com.inforstack.openstack.controller.model.CartModel;
import com.inforstack.openstack.controller.model.CategoryModel;
import com.inforstack.openstack.controller.model.I18nModel;
import com.inforstack.openstack.controller.model.ItemSpecificationModel;
import com.inforstack.openstack.controller.model.ProfileModel;
import com.inforstack.openstack.i18n.I18n;
import com.inforstack.openstack.i18n.I18nService;
import com.inforstack.openstack.item.Category;
import com.inforstack.openstack.item.ItemService;
import com.inforstack.openstack.item.ItemSpecification;
import com.inforstack.openstack.item.Profile;
import com.inforstack.openstack.log.Logger;
import com.inforstack.openstack.order.Order;
import com.inforstack.openstack.order.OrderService;
import com.inforstack.openstack.order.period.OrderPeriodService;
import com.inforstack.openstack.payment.method.PaymentMethod;
import com.inforstack.openstack.payment.method.PaymentMethodService;
import com.inforstack.openstack.rule.RuleService;
import com.inforstack.openstack.tenant.TenantService;
import com.inforstack.openstack.user.UserService;
import com.inforstack.openstack.utils.JSONUtil;
import com.inforstack.openstack.utils.OpenstackUtil;

@Controller
@RequestMapping(value = "/user/cart")
public class CartController {

	private static final Logger log = new Logger(CartController.class);

	public static final String CART_SESSION_ATTRIBUTE_NAME = CartController.class
			.getName() + ".Cart";

	@Autowired
	private UserService userService;

	@Autowired
	private I18nService i18nService;

	@Autowired
	private TenantService tenantService;

	@Autowired
	private ItemService itemService;

	@Autowired
	private RuleService ruleService;

	@Autowired
	private OrderService orderService;

	@Autowired
	private OrderPeriodService orderPeriodService;
	
	@Autowired
	private PaymentMethodService paymentMethodService;

	private final String CART_MODULE_HOME = "user/modules/Cart";

	@RequestMapping(value = "/modules/index", method = RequestMethod.GET)
	public String redirectModule(Model model, HttpServletRequest request) {
		clear(request, model);

		List<ItemSpecificationModel> dataCenterList = new ArrayList<ItemSpecificationModel>();
		dataCenterList = listProductsForUser(ItemSpecification.OS_TYPE_DATACENTER_ID);
		model.addAttribute("dataCenterList", dataCenterList);

		List<ItemSpecificationModel> imgList = new ArrayList<ItemSpecificationModel>();
		imgList = listProductsForUser(ItemSpecification.OS_TYPE_IMAGE_ID);
		model.addAttribute("imgList", imgList);

		List<ItemSpecificationModel> flavorModels = new ArrayList<ItemSpecificationModel>();
		flavorModels = listProductsForUser(ItemSpecification.OS_TYPE_FLAVOR_ID);
		model.addAttribute("flavorList", flavorModels);

		List<ItemSpecificationModel> planModels = new ArrayList<ItemSpecificationModel>();
		planModels = listProductsForUser(ItemSpecification.OS_TYPE_PERIOD_ID);
		model.addAttribute("planList", planModels);

		List<ItemSpecificationModel> volumeTypeModels = new ArrayList<ItemSpecificationModel>();
		volumeTypeModels = listProductsForUser(ItemSpecification.OS_TYPE_VOLUME_ID);
		model.addAttribute("volumeTypeList", volumeTypeModels);

		List<ItemSpecificationModel> networkModels = new ArrayList<ItemSpecificationModel>();
		networkModels = listProductsForUser(ItemSpecification.OS_TYPE_NETWORK_ID);
		model.addAttribute("networkList", networkModels);

		return CART_MODULE_HOME + "/index";
	}

	@RequestMapping(value = "/scripts/bootstrap", method = RequestMethod.GET)
	public String bootstrap(Model model) {
		return CART_MODULE_HOME + "/scripts/bootstrap";
	}

	@RequestMapping(value = "/modules/ip", method = RequestMethod.GET)
	public String buyIp(Model model) {
		List<ItemSpecificationModel> networkModels = new ArrayList<ItemSpecificationModel>();
		networkModels = listProductsForUser(ItemSpecification.OS_TYPE_NETWORK_ID);
		model.addAttribute("networkList", networkModels);

		List<ItemSpecificationModel> dataCenterList = new ArrayList<ItemSpecificationModel>();
		dataCenterList = listProductsForUser(ItemSpecification.OS_TYPE_DATACENTER_ID);
		model.addAttribute("dataCenterList", dataCenterList);

		return CART_MODULE_HOME + "/ip";
	}

	@RequestMapping(value = "/modules/volume", method = RequestMethod.GET)
	public String buyVolume(Model model) {
		List<ItemSpecificationModel> volumeTypeModels = new ArrayList<ItemSpecificationModel>();
		volumeTypeModels = listProductsForUser(ItemSpecification.OS_TYPE_VOLUME_ID);
		model.addAttribute("volumeTypeList", volumeTypeModels);

		List<ItemSpecificationModel> dataCenterList = new ArrayList<ItemSpecificationModel>();
		dataCenterList = listProductsForUser(ItemSpecification.OS_TYPE_DATACENTER_ID);
		model.addAttribute("dataCenterList", dataCenterList);

		return CART_MODULE_HOME + "/volume";
	}

	@RequestMapping(value = "/create", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> create(HttpServletRequest request, Model model) {
		CartModel cart = null;
		Object sessionAttribute = WebUtils.getSessionAttribute(request,
				CART_SESSION_ATTRIBUTE_NAME);
		if (sessionAttribute == null) {
			cart = new CartModel();
			cart.setItems(new CartItemModel[0]);
			cart.setAmount(0f);
		} else {
			cart = (CartModel) sessionAttribute;
		}
		WebUtils.setSessionAttribute(request, CART_SESSION_ATTRIBUTE_NAME, cart);
		return JSONUtil.jsonSuccess(cart);
	}

	@RequestMapping(value = "/clear", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> clear(HttpServletRequest request, Model model) {
		CartModel cart = new CartModel();
		cart.setItems(new CartItemModel[0]);
		cart.setAmount(0f);
		WebUtils.setSessionAttribute(request, CART_SESSION_ATTRIBUTE_NAME, cart);
		return JSONUtil.jsonSuccess(cart);
	}

	@RequestMapping(value = "/add", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> add(HttpServletRequest request, Model model,
			CartItemModel cartItem) {

		if (cartItem != null) {

			ItemSpecification itemSpecification = this.itemService
					.getItemSpecification(cartItem.getItemSpecificationId());
			if (itemSpecification != null) {
				cartItem.setUuid(UUID.randomUUID().toString());
				cartItem.setStatus(0);

				CartModel cart = null;
				Object sessionAttribute = WebUtils.getSessionAttribute(request,
						CART_SESSION_ATTRIBUTE_NAME);
				if (sessionAttribute == null) {
					cart = new CartModel();
					cart.setItems(new CartItemModel[1]);
					cart.getItems()[0] = cartItem;
				} else {
					cart = (CartModel) sessionAttribute;
					CartItemModel[] oldItems = cart.getItems();
					CartItemModel[] items = Arrays.copyOf(oldItems,
							oldItems.length + 1);
					cartItem.setPrice(itemSpecification.getDefaultPrice());
					items[oldItems.length] = cartItem;
					cart.setItems(items);
				}

				this.runRules(cart, itemSpecification);

				float amount = 0;
				CartItemModel[] items = cart.getItems();
				for (CartItemModel item : items) {
					amount += (item.getNumber().intValue() * item.getPrice()
							.floatValue());
				}
				cart.setAmount(amount);
				cart.setCurrentItemUUID(cartItem.getUuid());
				WebUtils.setSessionAttribute(request,
						CART_SESSION_ATTRIBUTE_NAME, cart);
				return JSONUtil.jsonSuccess(cart);
			}
		}

		return JSONUtil.jsonError(null);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> update(HttpServletRequest request, Model model,
			CartItemModel cartItem) {

		if (cartItem != null) {
			ItemSpecification itemSpecification = this.itemService
					.getItemSpecification(cartItem.getItemSpecificationId());
			if (itemSpecification != null) {
				CartItemModel existItem = null;
				CartModel cart = null;
				Object sessionAttribute = WebUtils.getSessionAttribute(request,
						CART_SESSION_ATTRIBUTE_NAME);
				if (sessionAttribute != null) {
					cart = (CartModel) sessionAttribute;
					CartItemModel[] items = cart.getItems();
					for (CartItemModel item : items) {
						if (item.getUuid().equalsIgnoreCase(cartItem.getUuid())) {
							existItem = item;
							break;
						}
					}
				}

				if (existItem != null) {
					if (cartItem.getName() != null) {
						existItem.setName(cartItem.getName());
					}
					if (cartItem.getItemSpecificationId() != null) {
						existItem.setItemSpecificationId(cartItem
								.getItemSpecificationId());
					}
					if (cartItem.getNumber() != null) {
						existItem.setNumber(cartItem.getNumber());
					}
					if (cartItem.getPrice() != null) {
						existItem.setPrice(cartItem.getPrice());
					}
					existItem.setStatus(0);

					this.runRules(cart, null);

					float amount = 0;
					CartItemModel[] items = cart.getItems();
					for (CartItemModel item : items) {
						amount += (item.getNumber().intValue() * item
								.getPrice().floatValue());
					}
					cart.setAmount(amount);
					cart.setCurrentItemUUID(cartItem.getUuid());
					WebUtils.setSessionAttribute(request,
							CART_SESSION_ATTRIBUTE_NAME, cart);
					return JSONUtil.jsonSuccess(cart);
				}
			}
		}

		return JSONUtil.jsonError(null);
	}

	@RequestMapping(value = "/remove", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> remove(HttpServletRequest request, Model model,
			CartItemModel cartItem) {

		if (cartItem != null) {
			CartModel cart = null;
			Object sessionAttribute = WebUtils.getSessionAttribute(request,
					CART_SESSION_ATTRIBUTE_NAME);
			if (sessionAttribute != null) {
				cart = (CartModel) sessionAttribute;
				CartItemModel[] items = cart.getItems();
				ArrayList<CartItemModel> itemList = new ArrayList<CartItemModel>();
				for (CartItemModel item : items) {
					if (!item.getUuid().equalsIgnoreCase(cartItem.getUuid())) {
						itemList.add(item);
					}
				}
				cart.setItems(itemList.toArray(new CartItemModel[0]));

				this.runRules(cart, null);

				float amount = 0;
				items = cart.getItems();
				for (CartItemModel item : items) {
					amount += (item.getNumber().intValue() * item.getPrice()
							.floatValue());
				}
				cart.setAmount(amount);
				cart.setCurrentItemUUID(null);
				WebUtils.setSessionAttribute(request,
						CART_SESSION_ATTRIBUTE_NAME, cart);
				return JSONUtil.jsonSuccess(cart);
			}
		}

		return JSONUtil.jsonError(null);
	}

	private void runRules(CartModel cart, ItemSpecification itemSpecification) {
		// User user = this.userService.findByName(SecurityUtils.getUserName());
		// Tenant tenant = null;
		// if (user != null) {
		// tenant =
		// this.tenantService.findTenantById(SecurityUtils.getTenant().getId());
		// }

		CartItemModel[] items = cart.getItems();

		for (CartItemModel item : items) {
			ItemSpecification is = this.itemService.getItemSpecification(item
					.getItemSpecificationId());
			item.setPrice(is.getDefaultPrice());
		}
		String period = null;
		float priceFactor = 1;
		if (itemSpecification != null
				&& itemSpecification.getOsType() == ItemSpecification.OS_TYPE_PERIOD_ID) {
			period = itemSpecification.getRefId();
			priceFactor = itemSpecification.getDefaultPrice();
		} else {
			ItemSpecification planItem = this.getPlan(cart);
			if (planItem != null) {
				period = planItem.getRefId();
				priceFactor = planItem.getDefaultPrice();
			}
		}

		Integer periodId = ((period != null) ? Integer.parseInt(period) : 3);
		for (CartItemModel item : items) {
			ItemSpecification is = this.itemService.getItemSpecification(item
					.getItemSpecificationId());
			switch (is.getOsType()) {
			case ItemSpecification.OS_TYPE_PERIOD_ID:
			case ItemSpecification.OS_TYPE_DATACENTER_ID:
				item.setPeriodId(periodId);
				item.setPrice(0f);
				break;
			case ItemSpecification.OS_TYPE_IMAGE_ID:
			case ItemSpecification.OS_TYPE_FLAVOR_ID:
			case ItemSpecification.OS_TYPE_USAGE_ID:
				item.setPeriodId(periodId);
				item.setPrice(item.getPrice() * priceFactor);
				break;
			case ItemSpecification.OS_TYPE_NETWORK_ID:
				item.setPeriodId(3);
				break;
			case ItemSpecification.OS_TYPE_VOLUME_ID:
				item.setPeriodId(2);
				break;
			}
		}

		// List<Rule> ruleList = this.ruleService.listRuleByTypeName("cart");
		// if (ruleList != null) {
		// for (Rule rule : ruleList) {
		// try {
		// KnowledgeBase kbase = RuleUtils.readKnowledgeBase(
		// rule.getName(), rule.getLocationType(),
		// rule.getLocation());
		// FactType userType = kbase.getFactType("troposphere", "User");
		// Object userFact = userType.newInstance();
		// StatefulKnowledgeSession ksession = kbase
		// .newStatefulKnowledgeSession();
		// ksession.insert(userFact);
		// ksession.fireAllRules();
		// } catch (InstantiationException e) {
		// } catch (IllegalAccessException e) {
		// }
		// }
		// }
	}

	private ItemSpecification getPlan(CartModel cart) {
		ItemSpecification planItem = null;
		CartItemModel[] cartItems = cart.getItems();
		for (CartItemModel cartItem : cartItems) {
			ItemSpecification item = this.itemService
					.getItemSpecification(cartItem.getItemSpecificationId());
			if (item.getOsType() == ItemSpecification.OS_TYPE_PERIOD_ID) {
				planItem = item;
				break;
			}
		}
		return planItem;
	}

	private List<ItemSpecificationModel> listProductsForUser(int osType) {
		Integer languageId = OpenstackUtil.getLanguage().getId();
		List<ItemSpecificationModel> models = new ArrayList<ItemSpecificationModel>();

		List<ItemSpecification> itemSpecifications = this.itemService
				.listAllItemSpecification();

		for (ItemSpecification itemSpecification : itemSpecifications) {
			if (itemSpecification.getAvailable() == true
					&& itemSpecification.getOsType() == osType) {
				I18nModel[] itemName = new I18nModel[1];
				itemName[0] = new I18nModel();
				itemName[0].setLanguageId(languageId);
				itemName[0].setContent(itemSpecification.getName()
						.getI18nContent());

				ItemSpecificationModel itemSpecificationModel = new ItemSpecificationModel();
				itemSpecificationModel.setId(itemSpecification.getId());
				itemSpecificationModel.setName(itemName);
				itemSpecificationModel.setOsType(itemSpecification.getOsType());
				itemSpecificationModel.setRefId(itemSpecification.getRefId());

				if (osType == ItemSpecification.OS_TYPE_FLAVOR_ID
						|| osType == ItemSpecification.OS_TYPE_VOLUME_ID
						|| osType == ItemSpecification.OS_TYPE_IMAGE_ID
						|| osType == ItemSpecification.OS_TYPE_PERIOD_ID) {
					Map<String, String> details = itemService
							.getItemSpecificationDetail(itemSpecification
									.getId());
					Map<String, String> i18Details = new HashMap<String, String>();
					Iterator<Entry<String, String>> it = details.entrySet()
							.iterator();
					while (it.hasNext()) {
						Entry<String, String> entry = (Entry<String, String>) it
								.next();
						String key = entry.getKey();
						String value = entry.getValue();
						if (key.equals("os_cpu")) {
							i18Details.put(OpenstackUtil
									.getMessage("admin.flavor.vcpus"), value);
						}
						if (key.equals("os_memory")) {
							i18Details.put(OpenstackUtil
									.getMessage("admin.flavor.ram"), value);
						}
						if (key.equals("os_disk")) {
							i18Details.put(OpenstackUtil
									.getMessage("admin.flavor.rdisk"), value);
						}
						if (key.equals("os_periodname")) {
							i18Details
									.put(OpenstackUtil
											.getMessage("plan.name.label"),
											value);
						}
						if (key.equals("os_size")) {
							i18Details.put(
									OpenstackUtil.getMessage("volume.size"),
									value);
						}
						if (key.equals("os_imagename")) {
							i18Details.put(OpenstackUtil
									.getMessage("admin.image.name"), value);
						}
						if (key.equals("os_family")) {
							i18Details.put("os_family", value);
						}

					}
					itemSpecificationModel.setDetails(i18Details);
				}

				List<Category> cList = itemSpecification.getCategories();
				CategoryModel[] cModelArray = new CategoryModel[cList.size()];
				for (int i = 0; i < cList.size(); i++) {
					Category c = cList.get(i);
					CategoryModel cModel = new CategoryModel();
					cModel.setEnable(c.getEnable());
					cModel.setSystem(c.getSystem());
					cModel.setId(c.getId());
					I18nModel[] cName = new I18nModel[1];
					cName[0] = new I18nModel();
					cName[0].setLanguageId(languageId);

					int cNameId = c.getNameId();
					if (cNameId > 0) {
						log.info("item name ID:" + cNameId);
						I18n i18n = null;
						i18n = i18nService.findByLinkAndLanguage(cNameId,
								languageId);
						if (i18n != null) {
							cName[0].setContent(i18n.getContent());
						}

					} else {
						log.warn("category name id is null ,item id:"
								+ c.getId());
					}

					cModel.setName(cName);
					cModelArray[i] = cModel;
				}
				itemSpecificationModel.setCategories(cModelArray);
				itemSpecificationModel.setDefaultPrice(itemSpecification
						.getDefaultPrice());
				itemSpecificationModel.setAvailable(itemSpecification
						.getAvailable());
				itemSpecificationModel.setCreated(DateFormat
						.getDateTimeInstance(DateFormat.SHORT,
								DateFormat.SHORT, OpenstackUtil.getLocale())
						.format(itemSpecification.getCreated()));
				itemSpecificationModel.setUpdated(DateFormat
						.getDateTimeInstance(DateFormat.SHORT,
								DateFormat.SHORT, OpenstackUtil.getLocale())
						.format(itemSpecification.getUpdated()));

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
				models.add(itemSpecificationModel);
			}
		}
		return models;
	}

	@RequestMapping(value = "/checkout", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> checkout(Model model, HttpServletRequest request) {
		Object sessionAttribute = WebUtils.getSessionAttribute(request,
				CART_SESSION_ATTRIBUTE_NAME);
		if (sessionAttribute == null
				|| (sessionAttribute instanceof CartModel) == false) {
			return OpenstackUtil.buildErrorResponse(OpenstackUtil
					.getMessage("cart.empty"));
		}

		CartModel cartModel = (CartModel) sessionAttribute;
		if (cartModel.getItems() == null || cartModel.getItems().length == 0) {
			return OpenstackUtil.buildErrorResponse(OpenstackUtil
					.getMessage("cart.empty"));
		}

		Order order = null;
		try {
			order = orderService.createOrder(cartModel);
		} catch (RuntimeException re) {
			log.error("create order failed", re);
		}

		if (order == null) {
			return OpenstackUtil.buildErrorResponse(OpenstackUtil
					.getMessage("cart.checkout.failed"));
		} else {
			WebUtils.setSessionAttribute(request, CART_SESSION_ATTRIBUTE_NAME,
					null);
			return OpenstackUtil.buildSuccessResponse(order.getId());
		}
	}

	@RequestMapping(value = "/showPayMethods", method = RequestMethod.POST)
	public String showPayMethod(Model model, HttpServletRequest request,
			String orderId) {
		model.addAttribute("orderId", orderId);
		List<PaymentMethod> paymethods = paymentMethodService.listAll();
		model.addAttribute("paymethods", paymethods);
		
		return CART_MODULE_HOME + "/payMethods";
	}
	
	@RequestMapping(value = "/showPayMethodNoBtn", method = RequestMethod.POST)
	public String showPayMethodNoBtn(Model model, HttpServletRequest request,
			String orderId) {
		model.addAttribute("orderId", orderId);
		List<PaymentMethod> paymethods = paymentMethodService.listAll();
		model.addAttribute("paymethods", paymethods);
		
		return CART_MODULE_HOME + "/paysMethodNoBtn";
	}
}
