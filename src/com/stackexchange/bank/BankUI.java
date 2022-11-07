package com.stackexchange.bank;

import java.io.Console;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.InputMismatchException;
import java.util.Scanner;

import static java.lang.System.out;

public class BankUI {
    private final Bank bank = new Bank();
    private final Scanner in = new Scanner(System.in);
    private final Console console = System.console();
    private final NumberFormat currency = NumberFormat.getCurrencyInstance();
    private boolean shouldExit = false;
    private User user;

    public BankUI() throws IOException, ClassNotFoundException
    {if (console == null) {
            throw new UnsupportedOperationException("Refusing to run in an insecure terminal.");
        }
    }

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new BankUI().mainMenu();
    }

    public void mainMenu() {
        while (!shouldExit) {
            if (user == null) {
                out.printf(
                        "Welcome to my Bank System"
                                + "%n"
                                + "%n1: Open existing account."
                                + "%n2: Create new account."
                                + "%n"
                );
                menuInput(
                        this::openAccount,
                        this::createAccount
                );
            }
            else {
                out.printf(
                        "Choose an action of the following options."
                                + "%n"
                                + "%n1: Display Account Information."
                                + "%n2: Deposit."
                                + "%n3: Withdraw."
                                + "%n4: Check Balance."
                                + "%n5: Change Password."
                                + "%n6: Delete Account."
                                + "%n7: Exit."
                                + "%n"
                );
                menuInput(
                        this::displayUserInfo,
                        this::deposit,
                        this::withdraw,
                        this::checkBalance,
                        this::changePassword,
                        this::deleteAccount,
                        this::exit
                );
            }

            out.println();
        }
    }

    public void menuInput(Runnable... options) {
        int choice;

        while (true) {
            out.print("Enter: ");

            try {
                choice = in.nextInt() - 1;
                in.nextLine();
            }
            catch (InputMismatchException e) {
                out.println("Please enter a valid integer.");
                in.nextLine();
                continue;
            }

            if (choice >= 0 && choice < options.length)
                break;
            out.println("Please enter a valid choice.");
        }

        options[choice].run();
    }

    public void displayUserInfo() {
        out.printf("User Name: %s%n", user.getUserName());

        String kind;
        switch (user.getLastTransactionKind()) {
            case 'D' -> kind = "Deposit";
            case 'W' -> kind = "Withdrawal";
            default -> { return; }
        }

        out.printf(
                "Last Transaction: %s, Amount: %s%n",
                kind, currency.format(user.getLastTransactionAmount())
        );
    }

    public void deposit() {
        out.print("Please enter any amount to deposit: ");
        double deposit = in.nextDouble();
        in.nextLine();
        user.deposit(deposit);
        out.printf("You have successfully deposited %s%n", currency.format(deposit));
        bank.saveHashMap();
    }

    public void withdraw() {
        out.print("Please enter any amount to withdraw: ");
        double withdraw = in.nextDouble();
        in.nextLine();
        user.withdraw(withdraw);
        out.printf("You have successfully withdrawn %s%n", currency.format(withdraw));
        bank.saveHashMap();
    }

    public void checkBalance() {
        out.printf("Current Balance: %s%n", currency.format(user.getBalance()));
    }

    public void changePassword() {
        out.println("To change the password please enter the current password.");
        if (validatePassword()) {
            out.print("Please enter the new password: ");
            user.changePassword(console.readPassword());
            bank.saveHashMap();
        }
        else {
            user = null;
        }
    }

    public void openAccount() {
        out.print("Please enter the user name of the account: ");
        String userName = in.nextLine();

        user = bank.getAccount(userName);
        if (user == null) {
            out.println("Please enter an existing account.");
            return;
        }

        if (!validatePassword())
            user = null;
    }

    public void createAccount() {
        out.print("Enter a new user name for the account: ");
        String userName = in.nextLine();

        if (bank.getAccount(userName) != null) {
            out.println("This user name already exists.");
            return;
        }

        out.print("Enter a new password for the account: ");
        user = new User(userName, console.readPassword());

        bank.addAccount(user);
        bank.saveHashMap();
    }

    public void deleteAccount() {
        out.println("Please enter password to delete account.");
        if (validatePassword()) {
            bank.deleteAccount(user.getUserName());
            out.printf("You have successfuly deleted account '%s'.%n", user.getUserName());
            bank.saveHashMap();
        }

        user = null;
    }

    public boolean validatePassword() {
        try {
            for (int tries = 0; tries < 3; tries++) {
                out.print("Please enter the password: ");
                if (user.validatePassword(console.readPassword()))
                    return true;

                Thread.sleep(1000);
                out.println("Incorrect password.");
            }

            out.println("You have entered too many incorrect passwords.");
            Thread.sleep(3000);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return false;
    }

    public void exit() {
        shouldExit = true;
    }
}