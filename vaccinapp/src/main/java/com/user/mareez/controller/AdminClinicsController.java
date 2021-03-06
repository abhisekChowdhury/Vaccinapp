package com.user.mareez.controller;

import java.util.List;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.user.mareez.dao.AdminDao;
import com.user.mareez.dao.UserDao;
import com.user.mareez.model.AdminVaccinationInfo;
import com.user.mareez.model.ClinicInfo;
import com.user.mareez.model.DailyNews;
import com.user.mareez.model.Enquiry;
import com.user.mareez.model.User;

@SessionAttributes("user")
@Controller
public class AdminClinicsController {

	@Autowired
	UserDao userDao;
	
	@Autowired
	AdminDao adminDao;

	@ModelAttribute("AdminVaccinationInfo")
	public AdminVaccinationInfo adminVaccinationInfo() {
		return new AdminVaccinationInfo();
	}

	@ModelAttribute("DailyNews")
	public DailyNews dailyNews() {
		return new DailyNews();
	}
	
	@ModelAttribute("ClinicInfo")
	public ClinicInfo clinicInfo() {
		return new ClinicInfo();
	}
	
	

	@PostMapping("/addNewClinic")
	public String addNewClinic(HttpSession session,
			@ModelAttribute("ClinicInfo") ClinicInfo clinicInfo, Model model) {
		User user = (User) session.getAttribute("user");
		
		int result = adminDao.insertNewClinic(clinicInfo.getClinicName(), clinicInfo.getClinicAddress(), clinicInfo.getClinicContact(), clinicInfo.getClinicEmail());

		if (result > 0) {
			model.addAttribute("approvedClinic", clinicInfo.getClinicName() + " is added!");
		}

		model.addAttribute("user", user);
		model.addAttribute("userName", user.getFirstName());
		model.addAttribute("message", "Welcome, " + user.getFirstName() + "!");
		//Generate notifications
		String adminNotifications = "You have no notifications";
		List<User> userInfo = adminDao.findUnapprovedUsers();
		if(userInfo.size() > 0) {
			adminNotifications = "\nYou have "+userInfo.size()+ " pending user approval(s)";
		}
		List<Enquiry> userEnq = adminDao.findEnquiryByUser();
		int count = 0;
		for(int i=0; i< userEnq.size(); i++) {
			if(userEnq.get(i).getResponse().equalsIgnoreCase("Waiting Response!")) {
				count++;
			}
		}
		if((count > 0) && !(userInfo.size() > 0))  {
			adminNotifications = "\nYou have "+count+ " pending enquiry(s)";
		}
		
		if((count > 0) && (userInfo.size() >0))  {
			adminNotifications += "<br>You have "+count+ " pending enquiry(s)";
		}
		
		
		model.addAttribute("adminNotifications", adminNotifications);
		return "login-success-admin";
	}
	
	
	@GetMapping("/viewClinics")
	public String viewClinic(HttpSession session, Model model) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			List<ClinicInfo> clinicInfo = userDao.findClinics();
			model.addAttribute("clinicInfo", clinicInfo);
		}
		return "view-all-clinics-admin";
	}
	
	@GetMapping("/deleteClinic")
	public String deleteClinic(@RequestParam("clinicId") String clinicId, Model model, HttpSession session) {
		User user = (User) session.getAttribute("user");
		if (user != null) {
			adminDao.deleteClinic(Integer.parseInt(clinicId));
			List<ClinicInfo> clinicInfo = userDao.findClinics();
			model.addAttribute("clinicInfo", clinicInfo);
		}
		return "view-all-clinics-admin";
	}
}
