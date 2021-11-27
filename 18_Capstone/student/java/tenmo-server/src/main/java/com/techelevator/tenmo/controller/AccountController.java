package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.JdbcAccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;


@RestController
public class AccountController {
    private AccountDao accountDao;
    private static final String API_BASE_URL = "http://localhost:8080";


    /************* Constructor ***********/
    public AccountController(AccountDao accountDao) {
        this.accountDao = accountDao;

    }

    @RequestMapping(path = "/accounts/{id}/balance", method = RequestMethod.GET)
    public BigDecimal getBalanceById(@PathVariable int id) {
        return accountDao.getAccountBalance(id);
    }

    @RequestMapping(path = "/accounts/{userId}", method = RequestMethod.GET)
    public int getAccountIdByUserId(@PathVariable int userId) {
        return accountDao.findAccountIdByUserId(userId);
    }
    // account is coming in with an account Id of 0, even though the account
    // object on the server side in the request HttpEntity DOES contain a valid, correct account id
    @RequestMapping(path = "/accounts/{id}/balance", method = RequestMethod.PUT)
    public Account updateBalance(@PathVariable int id, @RequestBody Account account) {
        return accountDao.updateBalance(account, id);
    }

}
