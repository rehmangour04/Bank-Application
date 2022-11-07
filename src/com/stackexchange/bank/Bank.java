package com.stackexchange.bank;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class Bank {
    private final String filename;
    private final Map<String, User> userAccounts;

    public Bank() throws IOException, ClassNotFoundException {
        this("Accounts.dat");
    }

    public Bank(String filename) throws IOException, ClassNotFoundException {
        this.filename = filename;

        Map<String, User> diskAccounts;

        try (
                FileInputStream fis = new FileInputStream(filename);
                ObjectInputStream ois = new ObjectInputStream(fis);
        ) {
            diskAccounts = (Map<String, User>) ois.readObject();
        }
        catch (EOFException | FileNotFoundException e) {
            diskAccounts = new HashMap<>();
        }

        userAccounts = diskAccounts;
    }

    public void addAccount(User user) {
        userAccounts.put(user.getUserName(), user);
    }

    public User getAccount(String username) {
        return userAccounts.get(username);
    }

    public void deleteAccount(String username) {
        userAccounts.remove(username);
    }

    public void saveHashMap() {
        try(
                FileOutputStream fos = new FileOutputStream(filename);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
        ) {
            oos.writeObject(userAccounts);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}