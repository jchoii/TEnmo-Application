package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import org.apiguardian.api.API;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;

public class AccountService {

    private static final String API_BASE_URL = "http://localhost:8080/";
    private final RestTemplate restTemplate = new RestTemplate();
    private AuthenticatedUser currentUser;

    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public void setCurrentUser(AuthenticatedUser currentUser) {
        this.currentUser = currentUser;
    }

    public HttpEntity updateAccountBalance(int userId, BigDecimal requestedAmount){
        Account account = createAccountObject(userId, requestedAmount);
        return makeAccountEntity(account);
    }

    private Account createAccountObject(int userId, BigDecimal requestedAmount) {

        Account account = new Account();
        account.setUserId(userId);
        account.setAccountId(restTemplate.getForObject(API_BASE_URL + "/accounts/" + userId, Integer.class, makeAccountEntity(account)));
        account.setBalance(restTemplate.getForObject(API_BASE_URL+ "/accounts/" + account.getAccountId() + "/balance", BigDecimal.class, makeAccountEntity(account)));
        if (currentUser.getUser().getId() == userId) {
            account.setBalance(account.getBalance().subtract(requestedAmount));
        } else {
            account.setBalance(account.getBalance().add(requestedAmount));
        }
        return account;
    }

    private HttpEntity<Account> makeAccountEntity(Account account) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(account,headers);
    }
}
