package com.techelevator.tenmo.model;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;

public class Account {

    @NotBlank (message = "Account id cannot be blank.")
    private int accountId;
    @NotBlank (message = "User id cannot be blank.")
    private int userId;
    @Min(value = 0, message = "You may not have a negative balance.")
    private BigDecimal balance;
    private String username;
    private final BigDecimal STARTING_BALANCE = new BigDecimal("1000");


    /************* Getters and Setters ******************/


    public int getId() {
        return accountId;
    }

    public void setId(int accountId) {
        this.accountId = accountId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public BigDecimal getSTARTING_BALANCE() {
        return STARTING_BALANCE;
    }
}
