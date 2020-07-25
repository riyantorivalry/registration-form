package com.technolins.registrationform.controller;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.technolins.registrationform.dto.Gender;
import com.technolins.registrationform.dto.SignupForm;
import com.technolins.registrationform.entity.User;
import com.technolins.registrationform.repository.UserRepository;
import com.technolins.registrationform.service.SignupService;
import com.technolins.registrationform.util.Ajax;
import com.technolins.registrationform.util.MessageHelper;

@Controller
public class SignupController {
	private static final String SIGNUP_VIEW_NAME = "signup";

	@Autowired
	SignupService signUpService;
	@Autowired
	UserRepository userRepository;

	@ModelAttribute(name = "module")
	String module() {
		return "signup";
	}

	//	@ModelAttribute("listgender")
	//	public Map<Integer, String> listGender() {
	//		Map<Integer, String> gender = new HashMap<>();
	//		gender.put(0,"Female");
	//		gender.put(1,"Male");
	//
	//		return gender;
	//	}
	@ModelAttribute("listgender")
	public List<Gender> listGender() {

		return Arrays.asList(new Gender(0, "Female"), new Gender(1, "Male"));
	}

	@GetMapping("signup")
	String signup(Model model, @RequestHeader(value = "X-Requested-With", required = false) String requestedWith) {
		SignupForm form = new SignupForm();
		model.addAttribute(form);
		if (Ajax.isAjaxRequest(requestedWith)) {
			return SIGNUP_VIEW_NAME.concat(" :: signupForm");
		}
		return SIGNUP_VIEW_NAME;
	}

	@PostMapping("signup")
	public String save( @Valid @ModelAttribute SignupForm userDto, Errors errors, RedirectAttributes ra, BindingResult bindingResult) throws Exception {
		System.out.println("userDto : "+userDto.toString());
		if (errors.hasErrors()) {
			System.out.println("error");
			MessageHelper.addErrorAttribute(ra, "error");
			return SIGNUP_VIEW_NAME;
		}
		if(bindingResult.hasErrors()) {
			System.out.println("error f");
			MessageHelper.addErrorAttribute(ra, "error field");
			return SIGNUP_VIEW_NAME;
		}
		try {
			if (signUpService.isExistByMobileNumber(userDto.getMobileNumber())) {
				System.out.println("phone");
				MessageHelper.addErrorAttribute(ra, "user.save.phone.exist");
			}else if(signUpService.isExistByEmail(userDto.getEmail())) {
				System.out.println("phone");
				MessageHelper.addErrorAttribute(ra, "user.save.email.exist");
			}else {
				User user = new User();
				BeanUtils.copyProperties(userDto, user);
				Date now = new Date();
				user.setId(1L);
				user.setDateOfBirth(now);
				//				user.setGender(1);
				user.setCreatedDate(now);
				user.setCreatedBy("SYSTEM");
				user.setCreatedTerminal("127.0.0.1");
				User saveNewUser=null;
				try {
					//					saveNewUser = signUpService.save(user);
					saveNewUser = userRepository.save(user);
				}catch (Exception e) {
					e.getStackTrace();
				}

				MessageHelper.addSuccessAttribute(ra, "user.save.success");
				System.out.println("saved: " + saveNewUser);
			}
		}
		catch (Exception e) {
			MessageHelper.addErrorAttribute(ra, "user.save.error");
		}

		return "redirect:signup/";
	}

}
