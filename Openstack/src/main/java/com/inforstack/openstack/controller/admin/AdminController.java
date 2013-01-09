package com.inforstack.openstack.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value = "/admin")
public class AdminController {

  @RequestMapping(method = RequestMethod.GET)
  public String home(Model model) {
    return "admin/home";
  }

  // for test
  @RequestMapping(value = "/home", method = RequestMethod.GET)
  public String navi(Model model) {
    return "admin/home";
  }

  @RequestMapping(value = "/modules/entry/index", method = RequestMethod.GET)
  public String entry(Model model) {
    return "admin/modules/Entry/index";
  }

  @RequestMapping(value = "/scripts/navinit", method = RequestMethod.GET)
  public String naviInit(Model model) {
    return "admin/scripts/navinit";
  }

  @RequestMapping(value = "/scripts/bootloader", method = RequestMethod.GET)
  public String bootloader(Model model) {
    return "admin/scripts/bootloader";
  }

  @RequestMapping(value = "/vmlist", method = RequestMethod.GET)
  public String loadVMDetails(Model model) {
    return "admin/scripts/bootloader";
  }

}
