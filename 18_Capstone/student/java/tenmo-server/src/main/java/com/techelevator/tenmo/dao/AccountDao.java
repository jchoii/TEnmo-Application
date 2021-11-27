package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Account;

import java.math.BigDecimal;
import java.util.List;

public interface AccountDao {

    BigDecimal getAccountBalance(int id);

    int findAccountIdByUserId(int id);

    Account updateBalance(Account account, int id);

}
