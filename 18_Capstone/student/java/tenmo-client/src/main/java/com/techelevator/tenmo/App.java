package com.techelevator.tenmo;

import com.techelevator.tenmo.model.*;
import com.techelevator.tenmo.services.*;
import com.techelevator.view.ConsoleService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Scanner;

public class App {

private static final String API_BASE_URL = "http://localhost:8080/";
	private RestTemplate restTemplate = new RestTemplate();
    
    private static final String MENU_OPTION_EXIT = "Exit";
    private static final String LOGIN_MENU_OPTION_REGISTER = "Register";
	private static final String LOGIN_MENU_OPTION_LOGIN = "Login";
	private static final String[] LOGIN_MENU_OPTIONS = { LOGIN_MENU_OPTION_REGISTER, LOGIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	private static final String MAIN_MENU_OPTION_VIEW_BALANCE = "View your current balance";
	private static final String MAIN_MENU_OPTION_SEND_BUCKS = "Send TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS = "View your past transfers";
	private static final String MAIN_MENU_OPTION_REQUEST_BUCKS = "Request TE bucks";
	private static final String MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS = "View your pending requests";
	private static final String MAIN_MENU_OPTION_LOGIN = "Login as different user";
	private static final String[] MAIN_MENU_OPTIONS = { MAIN_MENU_OPTION_VIEW_BALANCE, MAIN_MENU_OPTION_SEND_BUCKS, MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS, MAIN_MENU_OPTION_REQUEST_BUCKS, MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS, MAIN_MENU_OPTION_LOGIN, MENU_OPTION_EXIT };
	
    private AuthenticatedUser currentUser;
    private ConsoleService console;
    private AuthenticationService authenticationService;
    private TransferService transferService;
    private UserService userService;
    private AccountService accountService;

    public static void main(String[] args) {
    	App app = new App(new ConsoleService(System.in, System.out), new AuthenticationService(API_BASE_URL),new TransferService(), new UserService(), new AccountService());
    	app.run();
    }

    public App(ConsoleService console, AuthenticationService authenticationService, TransferService transferService, UserService userService, AccountService accountService) {
		this.console = console;
		this.authenticationService = authenticationService;
		this.transferService = transferService;
		this.userService = userService;
		this.accountService = accountService;

	}


	public void run() {
		System.out.println("*********************");
		System.out.println("* Welcome to TEnmo! *");
		System.out.println("*********************");
		
		registerAndLogin();
		mainMenu();
	}

	private void mainMenu() {
		while(true) {
			String choice = (String)console.getChoiceFromOptions(MAIN_MENU_OPTIONS);
			if(MAIN_MENU_OPTION_VIEW_BALANCE.equals(choice)) {
				viewCurrentBalance();
			} else if(MAIN_MENU_OPTION_VIEW_PAST_TRANSFERS.equals(choice)) {
				viewTransferHistory();
			} else if(MAIN_MENU_OPTION_VIEW_PENDING_REQUESTS.equals(choice)) {
				viewPendingRequests();
			} else if(MAIN_MENU_OPTION_SEND_BUCKS.equals(choice)) {
				sendBucks();
			} else if(MAIN_MENU_OPTION_REQUEST_BUCKS.equals(choice)) {
				requestBucks();
			} else if(MAIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else {
				// the only other option on the main menu is to exit
				exitProgram();
			}
		}
	}

	/************ Menu Option Methods ****************/

	private void viewCurrentBalance() {
		userService.setCurrentUser(currentUser);
		int accountId = userService.getAccountId();
		System.out.println("Your current account balance is: $" + userService.showBalance(accountId));
		System.out.println("");
		goBackToMainMenu();
	}


	private void viewTransferHistory() {
		displayTransfers();
		int choice = console.getUserInputInteger("Please enter transfer ID to view details (0 to cancel)");
		while (choice != 0) {
			String type = null;
			String status = null;
			Transfer transfer = transferService.getTransferById(choice);
			if (transfer.getTransferId() != 0) {
				type = setTransferTypeWithId(transfer);
				status = setTransferStatusWithId(transfer);
				displayTransferDetails(type, status, transfer);
			}
			break;
		}
	}

	private String setTransferTypeWithId(Transfer transfer){
    	String type = null;
		if (transfer.getTransferTypeId() == 1) {
				type = "Request";
			} else if (transfer.getTransferTypeId() == 2) {
				type = "Send";
			} else {
				type = null;
			}
		return type;
	}

	private  String setTransferStatusWithId(Transfer transfer) {
		String status = null;
		if (transfer.getTransferStatusId() == 1) {
			status = "Pending";
		} else if (transfer.getTransferStatusId() == 2) {
			status = "Approved";
		} else if (transfer.getTransferStatusId() == 3) {
			status = "Rejected";
		} else {
			status = null;
		}
		return status;
	}

	private void viewPendingRequests() {
		accountService.setCurrentUser(currentUser);
		userService.setCurrentUser(currentUser);
		displayPendingTransfers();
		int transferId = console.getUserInputInteger("Please enter transfer ID to approve/reject (0 to cancel)");
		while(transferId != 0){
			Transfer transfer = transferService.getTransferById(transferId); // create a transfer object that we can send to the server
			int accountId = userService.getAccountId();
			BigDecimal currentUserBalance = userService.showBalance(accountId);
			if (transfer.getTransferId() != 0 && !(transfer.getToName().equals(currentUser.getUser().getUsername()))) { //transfer id must be valid, and cannot be yourself
				System.out.println("1: Approve \n2: Reject \n0: Don't approve or reject");
				System.out.println("----------");
				int choice = console.getUserInputInteger("Please choose an option");
				while(choice !=0) {
					if((currentUserBalance).compareTo(transfer.getAmount()) >=0 || choice == 2) { // validate sufficient balance, or check if choice is 2;
						int userToId = 0;
						String toName = "";
						Transfer[] transfers = transferService.listTransfers(); // get array of all transfers
						for (Transfer eachTransfer : transfers) { // when we find a matching transfer from the server
							if (eachTransfer.getTransferId() == transferId) {
								toName = eachTransfer.getToName(); // give our transfer object a To Name and a  user To id;
								userToId = userService.findIdByUsername(toName);
							}
						}
						HttpEntity<Account> HttpEntityTo = accountService.updateAccountBalance(currentUser.getUser().getId(), transfer.getAmount());
						HttpEntity<Account> HttpEntityFrom = accountService.updateAccountBalance(userToId, transfer.getAmount());
						transferService.updatePendingRequest(choice, transfer.getTransferId(), transfer.getAmount(), HttpEntityTo, HttpEntityFrom);
					} else if ((currentUserBalance).compareTo(transfer.getAmount()) == -1 ){
						System.out.println("Insufficient funds");
					} break;
				}
			}
			break;
		}
	}

	private void sendBucks() {
		userService.setCurrentUser(currentUser);
		displayListOfUsers();
		int userId = console.getUserInputInteger("Enter ID of user to whom you are sending TE bucks");
    	int amount = console.getUserInputInteger("Enter amount");
		transferService.sendBucks(userId,amount,currentUser);
		goBackToMainMenu();
	}


	private void requestBucks() {
		userService.setCurrentUser(currentUser);
		displayListOfUsers();
		int userId = console.getUserInputInteger("\nEnter ID of user from whom you are requesting TE bucks (0 to cancel)");
		int amount = console.getUserInputInteger("Enter amount");
		transferService.requestBucks(userId,amount,currentUser);
	}
	
	private void exitProgram() {
		System.exit(0);
	}

	private void goBackToMainMenu() {
		Scanner scanner = new Scanner(System.in);
		System.out.print("Press any key to return to the main menu >>> ");
		scanner.nextLine();
	}

	/******** Display Methods *******/

	private void displayTransfers() {
		Transfer[] transfers = transferService.listTransfers();
		String currentUserUsername = currentUser.getUser().getUsername();
		System.out.println("--------------------------");
		System.out.println("Transfers");
		System.out.println("Id     To/From     Amount");
		System.out.println("--------------------------");
		for (Transfer transfer : transfers) {
			String senderUsername = transfer.getFromName();
			System.out.println(transfer.getTransferId() +
					"   " + ((currentUserUsername.equals(senderUsername)) ? "To: " + transfer.getToName() : "From: " + transfer.getFromName()) +
					"   " + "$" + transfer.getAmount());
		}
		System.out.println("---------");
	}

	private void displayTransferDetails(String type, String status, Transfer transfer) {
		System.out.println("--------------------------");
		System.out.println("Transfer Details");
		System.out.println("--------------------------");
		System.out.println("Id: " + transfer.getTransferId());
		System.out.println("From: " + transfer.getFromName());
		System.out.println("To: " + transfer.getToName());
		System.out.println("Type: " + type);
		System.out.println("Status: " + status);
		System.out.println("Amount: " + transfer.getAmount());
		System.out.println("--------------------------");
		goBackToMainMenu();
	}

	private void displayPendingTransfers() {
		userService.setCurrentUser(currentUser);
		Transfer[] transfers = transferService.listTransfers();
		String currentUserUsername = currentUser.getUser().getUsername();
		System.out.println("--------------------------------");
		System.out.println("Pending Transfers");
		System.out.println("Id     To                Amount");
		System.out.println("--------------------------------");
		for (Transfer transfer : transfers) {
			String requesterUsername = transfer.getToName();
			if (transfer.getTransferStatusId() == 1 && (!transfer.getToName().equals(currentUser.getUser().getUsername()))) {
				System.out.println(transfer.getTransferId() + "  " + requesterUsername + "          " + "$" + transfer.getAmount());
			}
		}
		System.out.println("--------------------------------");
	}


	private void displayListOfUsers() {
		User[] users = userService.listUsers();
		for (User user : users) {
			if(!user.getUsername().equals(currentUser.getUser().getUsername())) {
				System.out.println(user.getId() + ": " + user.getUsername());
			}
		}
	}


	/****** methods to login and register ******/

	private void registerAndLogin() {
		while(!isAuthenticated()) {
			String choice = (String)console.getChoiceFromOptions(LOGIN_MENU_OPTIONS);
			if (LOGIN_MENU_OPTION_LOGIN.equals(choice)) {
				login();
			} else if (LOGIN_MENU_OPTION_REGISTER.equals(choice)) {
				register();
			} else {
				// the only other option on the login menu is to exit
				exitProgram();
			}
		}
	}

	private boolean isAuthenticated() {
		return currentUser != null;
	}

	private void register() {
		System.out.println("Please register a new user account");
		boolean isRegistered = false;
        while (!isRegistered) //will keep looping until user is registered
        {
            UserCredentials credentials = collectUserCredentials();
            try {
            	authenticationService.register(credentials);
            	isRegistered = true;
            	System.out.println("Registration successful. You can now login.");
            } catch(AuthenticationServiceException e) {
            	System.out.println("REGISTRATION ERROR: "+e.getMessage());
				System.out.println("Please attempt to register again.");
            }
        }
	}

	private void login() {
		System.out.println("Please log in");
		currentUser = null;
		while (currentUser == null) //will keep looping until user is logged in
		{
			UserCredentials credentials = collectUserCredentials();
		    try {
				currentUser = authenticationService.login(credentials);
				transferService.setAuthToken(currentUser.getToken());
				accountService.setAuthToken(currentUser.getToken());
			} catch (AuthenticationServiceException e) {
				System.out.println("LOGIN ERROR: "+e.getMessage());
				System.out.println("Please attempt to login again.");
			}
		}
	}
	
	private UserCredentials collectUserCredentials() {
		String username = console.getUserInput("Username");
		String password = console.getUserInput("Password");
		return new UserCredentials(username, password);
	}
}
