package it.cfalchi.alfrescorest.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PagesController {
	
	private static final Logger logger = LoggerFactory.getLogger(PagesController.class);
	
	@RequestMapping("/")
    public ModelAndView root() {
		ModelAndView model = new ModelAndView();
		model.setViewName("home");
		return model;
    }
	
	@RequestMapping("/createDoc")
    public ModelAndView createDocPage() {
		ModelAndView model = new ModelAndView();
		model.setViewName("createDoc");
		return model;
    }
}
