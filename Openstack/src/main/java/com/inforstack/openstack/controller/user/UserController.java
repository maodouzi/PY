package com.inforstack.openstack.controller.user;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.validator.constraints.impl.EmailValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.inforstack.openstack.api.OpenstackAPIException;
import com.inforstack.openstack.api.keystone.KeystoneService;
import com.inforstack.openstack.controller.RootController;
import com.inforstack.openstack.controller.model.PaginationModel;
import com.inforstack.openstack.controller.model.RegisterModel;
import com.inforstack.openstack.controller.model.UserModel;
import com.inforstack.openstack.i18n.dict.Dictionary;
import com.inforstack.openstack.i18n.lang.Language;
import com.inforstack.openstack.i18n.lang.LanguageService;
import com.inforstack.openstack.log.Logger;
import com.inforstack.openstack.payment.account.Account;
import com.inforstack.openstack.payment.account.AccountService;
import com.inforstack.openstack.tenant.Tenant;
import com.inforstack.openstack.user.User;
import com.inforstack.openstack.user.UserService;
import com.inforstack.openstack.utils.Constants;
import com.inforstack.openstack.utils.CryptoUtil;
import com.inforstack.openstack.utils.OpenstackUtil;
import com.inforstack.openstack.utils.SecurityUtils;
import com.inforstack.openstack.utils.StringUtil;
import com.inforstack.openstack.utils.ValidateUtil;

@Controller
@RequestMapping(value = "/user")
public class UserController {

	private static final Logger log = new Logger(UserController.class);
	private static final String BASE = "user/";
	private static final String USER_BASE_HOME = "user/modules/Userinfo/";

	@Autowired
	private RootController rootController;
	@Autowired
	private Validator validator;

	@Autowired
	private UserService userService;

	@Autowired
	private KeystoneService keystoneService;
	
	@Autowired
	private LanguageService languageService;
	
	@Autowired
	private AccountService accountService;
	
	
	@RequestMapping(value = "/reg", method = RequestMethod.GET)
	public String register(Model model) {
		log.debug("visit register page");
		model.addAttribute("users", userService.listAll());
		return BASE + "register";
	}

	@RequestMapping(method = RequestMethod.GET)
	public String home(Model model, HttpServletRequest request, HttpServletResponse response) {
		String destFilePath = request.getServletContext().getRealPath("/resources/common/entry");
		File htmlFile = new File(destFilePath, "html.txt");
		String html = StringUtil.file2String(htmlFile, "UTF-8");
		model.addAttribute("content", html);
		Account account = accountService.findActiveAccount(SecurityUtils.getTenantId(), null);
		model.addAttribute("balance", account != null ? account.getBalance() : 0);
		return BASE + "home";
	}

	@RequestMapping(value = "/modules/entry/index", method = RequestMethod.GET)
	public String entry(Model model, HttpServletRequest request, HttpServletResponse response) {
		
		String destFilePath = request.getServletContext().getRealPath("/resources/common/entry");
		File htmlFile = new File(destFilePath, "html.txt");
		String html = StringUtil.file2String(htmlFile, "UTF-8");
		model.addAttribute("content", html);
		Account account = accountService.findActiveAccount(SecurityUtils.getTenantId(), null);
		model.addAttribute("balance", account != null ? account.getBalance() : 0);
		return "user/modules/Entry/index";
	}

	@RequestMapping(value = "/scripts/navinit", method = RequestMethod.GET)
	public String naviInit(Model model) {
		return "user/scripts/navinit";
	}
	
	@RequestMapping(value = "/showedit", method = RequestMethod.GET)
	public String showUserInfoEdit(Model model) {
		Integer userId = SecurityUtils.getUserId();
		User user = userService.findUserById(userId);
		model.addAttribute("user", user);
		List<Language> lList = languageService.list();
		model.addAttribute("lList", lList);
		List<Dictionary> dictList = new ArrayList<Dictionary>();
		for(Language l: lList){
			Dictionary d = new Dictionary();
			d.setKey(l.getId().toString());
			d.setValue(l.getName());
			dictList.add(d);
		}
		model.addAttribute("languages", dictList);
		return USER_BASE_HOME + "index";
	}
	
	@RequestMapping(value = "/edit", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> updateUser(UserModel userModel, BindingResult result){
		Map<String, Object> ret = new HashMap<String, Object>();
		
		String errorMsg = ValidateUtil.validModel(validator, "user", userModel);
		if (errorMsg != null) {
			ret.put(Constants.JSON_ERROR_STATUS, errorMsg);
			return ret;
		}
		
		Integer userId = SecurityUtils.getUserId();
		User user = userService.findUserById(userId);
		user.setFirstname(userModel.getFirstname());
		user.setLastname(userModel.getLastname());
		user.setPhone(userModel.getPhone());
		user.setMobile(userModel.getMobile());
		user.setCountry(userModel.getCountry());
		user.setProvince(userModel.getProvince());
		user.setCity(userModel.getCity());
		user.setAddress(userModel.getAddress());
		user.setPostcode(userModel.getPostcode());
		Integer languageId = userModel.getDefaultLanguage();
		if(languageId != null){
			if(languageService.findById(languageId) != null){
				user.setDefaultLanguage(userModel.getDefaultLanguage());
			}
		}
		
		try {
			userService.updateUser(user);
			log.debug("Register user successfully");
			return OpenstackUtil.buildSuccessResponse(OpenstackUtil.getMessage("update.success"));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return OpenstackUtil.buildErrorResponse(OpenstackUtil.getMessage("update.failed"));
		}
	}
	
	@RequestMapping(value = "/changePassword", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> changePassword(String password, String newPassword){
		if(StringUtil.isNullOrEmpty(password) || password.length()<6 || password.length()>45){
			return OpenstackUtil.buildErrorResponse(OpenstackUtil.getMessage("user.password.lable") + 
					OpenstackUtil.getMessage("not.valid"));
		}
		if(StringUtil.isNullOrEmpty(newPassword) || newPassword.length()<6 || newPassword.length()>45){
			return OpenstackUtil.buildErrorResponse(OpenstackUtil.getMessage("newPassword.label") + 
					OpenstackUtil.getMessage("not.valid"));
		}
		
		Integer userId = SecurityUtils.getUserId();
		User user = userService.findUserById(userId);
		
		if(!CryptoUtil.md5(password).equals(user.getPassword())){
			return OpenstackUtil.buildErrorResponse(OpenstackUtil.getMessage("user.password.lable") + 
					OpenstackUtil.getMessage("not.valid"));
		}
		user.setPassword(CryptoUtil.md5(newPassword));
		
		try {
			userService.updateUser(user);
			log.debug("Change user password successfully");
			return OpenstackUtil.buildSuccessResponse(OpenstackUtil.getMessage("update.success"));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return OpenstackUtil.buildErrorResponse(OpenstackUtil.getMessage("update.failed"));
		}
	}
	
	@RequestMapping(value = "/changemail", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody Map<String, Object> changeMail(String mail, HttpServletRequest req){
		if(StringUtil.isNullOrEmpty(mail) || !new EmailValidator().isValid(mail, null)){
			return OpenstackUtil.buildErrorResponse(OpenstackUtil.getMessage("email.label") + 
					OpenstackUtil.getMessage("not.valid"));
		}
		
		try {
			userService.sendChangeMailEmail(SecurityUtils.getUserId(), mail,
					OpenstackUtil.getHost(req)+"/user/showresetemail");
			return OpenstackUtil.buildSuccessResponse(OpenstackUtil.getMessage("changeemail.submit.success"));
		} catch (RuntimeException e) {
			log.error(e.getMessage(), e);
			return OpenstackUtil.buildErrorResponse(OpenstackUtil.getMessage("changeemail.submit.failed"));
		}
	}

	@RequestMapping(value = "/list", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> listUser(int pageIndex, int pageSize, Model model,
			HttpServletRequest request, HttpServletResponse response) {
		PaginationModel<User> pm = userService.pagination(pageIndex, pageSize);

		Map<String, Object> conf = new LinkedHashMap<String, Object>();
		conf.put("grid.username", "[plain]");
		conf.put("grid.email", "[button]test,delete");
		conf.put("test.label", "测试");
		conf.put(
				"test.onclick",
				"if(!this.testForm){this.testForm=new customForm();}this.testForm.show({title:'test',container:$('#fc'),url:'/Openstack/user/form?id={id}', buttons: [{text: 'aaa', click:function(){alert('In form')}}]});");
		conf.put("delete.label", "delete");
		conf.put("delete.onclick", "alert('{a : {username}}')");
		conf.put(".datas", "users");

		model.addAttribute("users", pm.getData());
		model.addAttribute("configuration", conf);

		String jspString = OpenstackUtil.getJspPage(
				"/templates/grid.jsp?grid.configuration=configuration&type=",
				model.asMap(), request, response);

		if (jspString == null) {
			return OpenstackUtil.buildErrorResponse("error message");
		} else {
			Map<String, Object> result = new HashMap<String, Object>();
			result.put("recordTotal", pm.getRecordTotal());
			result.put("html", jspString);

			return OpenstackUtil.buildSuccessResponse(result);
		}

	}

	@RequestMapping(value = "/form", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> form(Model model, HttpServletRequest request,
			HttpServletResponse response) {
		User user = userService.listAll().get(0);

		Map<String, Object> conf = new LinkedHashMap<String, Object>();
		conf.put(".form", "start_end");
		conf.put("form.username", "[text]" + user.getUsername());
		conf.put("username.tip", "test");
		conf.put("form.email",
				"[text]" + OpenstackUtil.nulltoEmpty(user.getEmail()));

		model.addAttribute("configuration", conf);

		String jspString = OpenstackUtil.getJspPage(
				"/templates/form.jsp?form.configuration=configuration&type=",
				model.asMap(), request, response);

		if (jspString == null) {
			return OpenstackUtil.buildErrorResponse("error message");
		} else {
			return OpenstackUtil.buildSuccessResponse(jspString);
		}
	}

	@RequestMapping(value = "/regForm", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> showRegForm(Model model, HttpServletRequest request,
			HttpServletResponse response) {

		Map<String, Object> conf = new LinkedHashMap<String, Object>();
		conf.put(".form", "start_end");
		conf.put("form.username", "[text]");
		conf.put("username.title", OpenstackUtil.getMessage("username.tip"));
		conf.put("form.password", "[password]");
		conf.put("password.title", OpenstackUtil.getMessage("password.tip"));
		conf.put("form.confirmPassword", "[password]");
		conf.put("form.email", "[text]");
		model.addAttribute("configuration", conf);

		String jspString = OpenstackUtil.getJspPage(
				"/templates/form.jsp?form.configuration=configuration&type=",
				model.asMap(), request, response);

		if (jspString == null) {
			return OpenstackUtil.buildErrorResponse("error message");
		} else {
			return OpenstackUtil.buildSuccessResponse(jspString);
		}
	}

	@RequestMapping(value = "/forgetPswForm", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> showForgetPasswordForm(Model model,
			HttpServletRequest request, HttpServletResponse response) {

		Map<String, Object> conf = new LinkedHashMap<String, Object>();
		conf.put(".form", "start_end");
		conf.put("form.username", "[text]");
		conf.put("form.email", "[text]");

		model.addAttribute("configuration", conf);

		String jspString = OpenstackUtil.getJspPage(
				"/templates/form.jsp?form.configuration=configuration&type=",
				model.asMap(), request, response);

		if (jspString == null) {
			return OpenstackUtil.buildErrorResponse("error message");
		} else {
			return OpenstackUtil.buildSuccessResponse(jspString);
		}
	}
	
	@RequestMapping(value = "/forgetpassword", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> forgetPassword(String username, String email,
			Model model, HttpServletRequest request) {

		Map<String, Object> ret = new HashMap<String, Object>();
		if (StringUtil.isNullOrEmpty(username)) {
			ret.put("error", OpenstackUtil.getMessage("username.label")
					+ OpenstackUtil.getMessage("not.null.empty"));
		} else if (StringUtil.isNullOrEmpty(email)) {
			ret.put("error", OpenstackUtil.getMessage("email.label")
					+ OpenstackUtil.getMessage("not.null.empty"));
		}

		if (ret.isEmpty() == false) {
			return ret;
		}
		
		User user = null;
		try{
			user = userService.findByNameAndEmail(username, email);
			if(user != null){
				userService.sendResetPasswordEmail(user.getId(), OpenstackUtil.getHost(request)+"/user/resetpassword");
			}
		}catch(RuntimeException	re){
			log.error("reset password failed", re);
		}
		if (user == null) {
			ret.put(Constants.JSON_ERROR_STATUS,
					OpenstackUtil.getMessage("user.email.notexist"));
			return ret;
		} else {
			ret.put(Constants.JSON_SUCCESS_STATUS,
					OpenstackUtil.getMessage("user.password.emailsend"));
			// TODO new password email
			return ret;
		}
	}
	
	@RequestMapping(value = "/resetpassword")
	public String showResetPassword(String random, Model model, HttpServletRequest request) {
		if(!StringUtil.isNullOrEmpty(random)){
			model.addAttribute("random", random);
		}else{
			model.addAttribute("random", "undefined");
		}
		return "user/forgetpassword";
	}
	
	@RequestMapping(value = "/doresetpassword", method = RequestMethod.POST)
	public String doResetPassword(String random, String password, Model model, HttpServletRequest req, HttpServletResponse resp){
		User user = null;
		
		String errorMsg = null;
		if(password != null){
			RegisterModel rm = new RegisterModel();
			rm.setPassword(password);
			errorMsg = ValidateUtil.validModel(validator, "user", rm);
			
			if(errorMsg == null){
				try{
					user = userService.resetPassword(Constants.MAIL_CODE_RESET_PASSWORD, random, password);
				}catch(RuntimeException re){
					log.error("reset password failed", re);
				}
			}
		}
		
		if(user != null){
			model.addAttribute("message", OpenstackUtil.getMessage("password.reset.success"));
			return rootController.visitUser(model);
		}else{
			model.addAttribute("errorMessage", errorMsg!=null?errorMsg:OpenstackUtil.getMessage("password.reset.fail"));
			return showResetPassword(random,model ,req);
		}
		
		
	}

	@RequestMapping(value = "/scripts/bootstrap", method = RequestMethod.GET)
	public String bootstrap(Model model) {
		return "user/scripts/bootstrap";
	}

	@RequestMapping(value = "/scripts/template", method = RequestMethod.GET)
	public String template(Model model) {
		return "user/scripts/template";
	}

	@RequestMapping(value = "/userReg", method = RequestMethod.POST, produces = "application/json")
	public @ResponseBody
	Map<String, Object> doUserRegister(RegisterModel regModel, BindingResult result, 
			Model model, HttpServletRequest req) {
		log.debug("register user");

		Map<String, Object> ret = new HashMap<String, Object>();
		if (StringUtil.isNullOrEmpty(regModel.getUsername())) {
			ret.put("error", OpenstackUtil.getMessage("username.label")
					+ OpenstackUtil.getMessage("not.null.empty"));
		} else if (StringUtil.isNullOrEmpty(regModel.getPassword())) {
			ret.put("error", OpenstackUtil.getMessage("password.label")
					+ OpenstackUtil.getMessage("not.null.empty"));
		} else if (StringUtil.isNullOrEmpty(regModel.getEmail())) {
			ret.put("error", OpenstackUtil.getMessage("email.label")
					+ OpenstackUtil.getMessage("not.null.empty"));
		}

		if (ret.isEmpty() == false) {
			return ret;
		}

		String errorMsg = ValidateUtil.validModel(validator, "user", regModel);
		if (errorMsg != null) {
			ret.put(Constants.JSON_ERROR_STATUS, errorMsg);
			return ret;
		}

		User user = new User();
		user.setEmail(regModel.getEmail());
		user.setLastname(regModel.getUsername());
		user.setPassword(regModel.getPassword());
		user.setUsername(regModel.getUsername());
		user.setDefaultLanguage(OpenstackUtil.getLanguage().getId());
		Tenant tenant = new Tenant();
		tenant.setDefaultLanguage(OpenstackUtil.getLanguage().getId());
		tenant.setEmail(regModel.getEmail());
		tenant.setDipalyName(regModel.getUsername());
		tenant.setName(regModel.getUsername());

		User tempUser = userService.findByName(user.getUsername());
		if (tempUser != null) {
			log.warn("Register user failed for user name already exist");
			ret.put("error",
					OpenstackUtil.getMessage("user.alreadyexist",
							user.getUsername()));
			return ret;
		}
		boolean success = true;
		try {
			userService.registerUser(user, tenant, Constants.ROLE_USER);
			userService.sendActiveUserEmail(user.getId(), OpenstackUtil.getHost(req)+"/user/activeUser");
		} catch (OpenstackAPIException e) {
			success = false;
			if (e.getCode() == 409) {
				ret.put("error",
						OpenstackUtil.getMessage("user.alreadyexist",
								user.getUsername()));
			}
			log.error(e.getMessage());

		} catch (RuntimeException re) {
			success = false;
			log.error(re.getMessage(), re);
		}

		if (success == false) {
			try {
				keystoneService.removeUserAndTenant(user.getOpenstackUser()
						.getId(), tenant.getOpenstatckTenant().getId());
			} catch (OpenstackAPIException e) {
				log.error(e.getMessage(), e);
			}
		}

		if (success) {
			log.debug("Register user successfully");
			ret.put("success", OpenstackUtil.getMessage("user.reg.success"));
		}

		return ret;
	}
	
	@RequestMapping(value = "/activeUser", method = RequestMethod.GET)
	public String activeUser(String random, Model model, HttpServletRequest req, HttpServletResponse resp){
		User user = null;
		try{
			user = userService.active(Constants.MAIL_CODE_VALIDATE_USER, random);
		}catch(RuntimeException re){
			log.error("active user failed", re);
		}
		
		if(user != null && Constants.USER_STATUS_VALID.equals(user.getStatus())){
			model.addAttribute("message", OpenstackUtil.getMessage("active.success"));
		}else{
			model.addAttribute("errorMessage", OpenstackUtil.getMessage("active.fail"));
		}
		
		return rootController.visitUser(model);
	}
	
	@RequestMapping(value = "/doresetmail")
	public String doResetMail(String random, String verifyCode, Model model){
		User user = null;
		try{
			user = userService.resetMail(Constants.MAIL_CODE_RESET_EMAIL, random, verifyCode);
		}catch(RuntimeException re){
			log.error("reset email failed", re);
		}
		
		if(user != null){
			model.addAttribute("message", OpenstackUtil.getMessage("email.reset.success"));
		}else{
			model.addAttribute("errorMessage", OpenstackUtil.getMessage("email.reset.fail"));
		}
		return "user/showactiveemail";
	}
	
	@RequestMapping(value = "/showresetemail")
	public String showResetEmail(String random, Model model, HttpServletRequest request) {
		if(!StringUtil.isNullOrEmpty(random)){
			model.addAttribute("random", random);
		}else{
			model.addAttribute("random", "undefined");
		}
		return "user/showactiveemail";
	}
}
