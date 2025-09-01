package com.ryanshiun.seniorscare.activity.controller;

import com.ryanshiun.seniorscare.activity.dao.ActivityCategoryDao;
import com.ryanshiun.seniorscare.activity.model.ActivityCategory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/activity-categories")
@CrossOrigin
public class ActivityCategoryController {

    @Autowired
    private ActivityCategoryDao activityCategoryDao;

    @GetMapping
    public List<ActivityCategory> list(){
        return activityCategoryDao.findAllActive();
    }
}
