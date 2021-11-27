package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.User;
import io.cucumber.java.bs.A;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class UserService {
    private static final String API_BASE_URL = "http://localhost:8080/";
    private final RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;

    public UserService() {
    }

    public void setCurrentUser(AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
    }

    public int getAccountId() {
        int userId = currentUser.getUser().getId();
        int accountId = 0;
        try {
            ResponseEntity<Integer> response = restTemplate.exchange(API_BASE_URL + "accounts/" + userId, HttpMethod.GET,makeAuthEntity(),Integer.class);
            accountId = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }
        return accountId;
    }

    public BigDecimal showBalance(int id) {
        BigDecimal balance = null;
        try {
            ResponseEntity<BigDecimal> response = restTemplate.exchange(API_BASE_URL + "accounts/" + id + "/balance", HttpMethod.GET,makeAuthEntity(),BigDecimal.class);
            balance = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }
        return balance;
        // timing.
        // more trips back and forth goes slower
        // make one call and do the lookup on the server side
        // get the information from the token
        // use principal
    }

    public User[] listUsers() {
        User[] users = null;
        try {
            ResponseEntity<User[]> response = restTemplate.exchange(API_BASE_URL + "/users", HttpMethod.GET,makeAuthEntity(),User[].class);
            users = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }
        return users;
    }
    public int findIdByUsername(String username) {
        int userId = restTemplate.getForObject(API_BASE_URL + "/users/" + username,int.class, HttpMethod.GET,makeAuthEntity());
        return userId;
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(currentUser.getToken());
        return new HttpEntity<>(headers);
    }


}


