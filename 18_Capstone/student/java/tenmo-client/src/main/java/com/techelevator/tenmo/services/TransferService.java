package com.techelevator.tenmo.services;

import com.techelevator.tenmo.model.Account;
import com.techelevator.tenmo.model.AuthenticatedUser;
import com.techelevator.tenmo.model.Transfer;
import okhttp3.internal.Internal;
import org.springframework.http.*;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.security.auth.login.AccountException;
import java.math.BigDecimal;
import java.util.Scanner;

public class TransferService {

    private static final String API_BASE_URL = "http://localhost:8080/";
    private final RestTemplate restTemplate = new RestTemplate();

    private String authToken = null;

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }


    public Transfer[] listTransfers() {
        Transfer[] transfers = null;
        try {
            ResponseEntity<Transfer[]> response = restTemplate.exchange(API_BASE_URL + "accounts/transfers", HttpMethod.GET, makeAuthEntity(), Transfer[].class);
            transfers = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }
        return transfers;
    }

    public Transfer getTransferById(int id) {
        Transfer transfer = new Transfer();
        try {
            ResponseEntity<Transfer> response = restTemplate.exchange(API_BASE_URL + "accounts/transfers/" + id, HttpMethod.GET, makeAuthEntity(), Transfer.class);
            transfer = response.getBody();
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println("Invalid Transfer.");
            Scanner scanner = new Scanner(System.in);
            System.out.print("Press any key to return to the main menu >>> ");
            scanner.nextLine();
        }
        return transfer;
    }

    public void sendBucks(int userId, int amount, AuthenticatedUser authenticatedUser) {
        Transfer transfer = createNewTransferObject(userId, amount, authenticatedUser);
        if (transfer.getAccountTo() !=0) {
            try {
                ResponseEntity<Boolean> response = restTemplate.exchange(API_BASE_URL + "accounts/transfers", HttpMethod.POST, makeTransferEntity(transfer), Boolean.class);
            } catch (RestClientResponseException | ResourceAccessException e) {
                System.out.println("\nInvalid Transfer.");
            }
        }
    }

    public void requestBucks(int userId, int amount, AuthenticatedUser authenticatedUser) {
        Transfer transfer = createTransferRequestObject(userId,amount,authenticatedUser);
        try{
            ResponseEntity<Boolean> response = restTemplate.exchange(API_BASE_URL + "accounts/transfers/request",HttpMethod.POST,makeTransferEntity(transfer),Boolean.class);
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println("\nInvalid Transfer.");
        }
    }

    public boolean updatePendingRequest(int choice, int transferId, BigDecimal amount, HttpEntity<Account> httpEntityFrom, HttpEntity<Account> httpEntityTo) {
        ResponseEntity<Transfer> response = restTemplate.exchange(API_BASE_URL + "accounts/transfers/" + transferId, HttpMethod.GET, makeAuthEntity(), Transfer.class);
        Transfer transfer = response.getBody();
        transfer.setTransferStatusId(2);
        boolean success = false;
        try {
        if (choice == 1) {
            transfer.setTransferStatusId(2);
                restTemplate.put(API_BASE_URL + "accounts/transfers/update/" + transferId, makeTransferEntity(transfer));
                success = true;

                restTemplate.put(API_BASE_URL + "accounts/" + transfer.getAccountFrom() + "/balance", httpEntityFrom); // updating sender's account balance
                restTemplate.put(API_BASE_URL + "accounts/" + transfer.getAccountTo() + "/balance", httpEntityTo); // increasing balance of recipient
            } else {
                transfer.setTransferStatusId(3);
                restTemplate.put(API_BASE_URL + "accounts/transfers/update/" + transferId, makeTransferEntity(transfer));
                success = true;
            }
        } catch (RestClientResponseException | ResourceAccessException e) {
            System.out.println(e.getMessage());
        }
//        System.out.println(success);
        return success;
    }
    // gather information, send to server, write all this on the server.
    // do they have enough money on server side
    // security issue: could figure out a way to update my (current user) balance. Put this on server
    // ability to update account balance should NOT happen on the client side.
    // could look at network on dev tools and see the path where we're updating the account.



    private Transfer createTransferRequestObject(int userId, int amount, AuthenticatedUser authenticatedUser) {
        Transfer transfer = new Transfer();
        transfer.setTransferStatusId(1); // transferStatusId
        transfer.setTransferTypeId(1); // transferTypeId
        transfer.setAmount(new BigDecimal(amount));// amount

        Integer accountIdTo = restTemplate.getForObject(API_BASE_URL + "/accounts/" + authenticatedUser.getUser().getId(),Integer.class,makeAuthEntity());
        transfer.setAccountTo(accountIdTo);
        try {
            Integer accountIdFrom = restTemplate.getForObject(API_BASE_URL + "/accounts/" + userId, Integer.class,makeAuthEntity());
            transfer.setAccountFrom(accountIdFrom);
        } catch (Exception e) {
            System.out.println("\nInvalid user id.");
        }

        return transfer;
    }

    private Transfer createNewTransferObject(int userId, int amount, AuthenticatedUser authenticatedUser) {
        Transfer transfer = new Transfer();
        transfer.setTransferStatusId(2); // transferStatusId
        transfer.setTransferTypeId(2); // transferTypeId
        transfer.setAmount(new BigDecimal(amount));// amount
        Integer accountIdFrom = restTemplate.getForObject(API_BASE_URL + "/accounts/" + authenticatedUser.getUser().getId(),Integer.class,makeAuthEntity());
        transfer.setAccountFrom(accountIdFrom);
        try {
            Integer accountIdTo = restTemplate.getForObject(API_BASE_URL + "/accounts/" + userId, Integer.class, makeAuthEntity());
            transfer.setAccountTo(accountIdTo);
        } catch (Exception e) {
            System.out.println("\nYou have entered an invalid account number.\n");
        }
        return transfer;
    }

    private HttpEntity<Void> makeAuthEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(authToken); //
        return new HttpEntity<>(headers);
    }

    private HttpEntity<Transfer> makeTransferEntity(Transfer transfer) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(authToken);
        return new HttpEntity<>(transfer,headers);
    }



}
