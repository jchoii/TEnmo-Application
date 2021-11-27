package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.User;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@PreAuthorize("isAuthenticated()")
@RestController
public class UserController {

    private UserDao userDao;
    private static final String API_BASE_URL = "http://localhost:8080";

    /********** Constructor ************/
    public UserController(UserDao userDao) {
        this.userDao = userDao;
    }

    @RequestMapping(path = "/users",method = RequestMethod.GET)
    public List<User> getListOfUsers(){
        return userDao.findAll();
    }

    @PreAuthorize("permitAll")
    @RequestMapping(path = "/users/{username}", method = RequestMethod.GET)
    public int getUserIdByUsername(@PathVariable String username) {
        return userDao.findIdByUsername(username);
    }


}
