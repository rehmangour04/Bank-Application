package com.stackexchange.bank;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.Serial;
import java.io.Serializable;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;

public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final SecretKeyFactory secrets;
    private static final SecureRandom rand;
    private static final int keyLength = 512;

    static {
        try {
            secrets = SecretKeyFactory.getInstance(
                    String.format("PBKDF2WithHmacSHA%d", keyLength)
            );
            rand = SecureRandom.getInstanceStrong();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private final String userName;
    private final byte[] salt = new byte[16];
    private byte[] passwordHash;
    private double balance;
    private double lastTransaction;
    private char transactionKind;

    User (String userName, char[] password) {
        this.userName = userName;
        this.passwordHash = setPassword(password);
    }

    public String getUserName() {
        return userName;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double deposit) {
        balance += deposit;
        lastTransaction = deposit;
        transactionKind = 'D';
    }
    public void withdraw(double withdraw) {
        balance -= withdraw;
        lastTransaction = withdraw;
        transactionKind = 'W';
    }
    public double getLastTransactionAmount() { return lastTransaction; }

    public char getLastTransactionKind() { return transactionKind; }

    private byte[] setPassword(char[] password) {
        rand.nextBytes(salt);
        return hash(password);
    }

    private byte[] hash(char[] password) {
        final int iterCount = 65536;
        KeySpec spec = new PBEKeySpec(password, salt, iterCount, keyLength);
        byte[] encoded;
        try {
            encoded = secrets.generateSecret(spec).getEncoded();
        }
        catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }

        return encoded;
    }

    public boolean validatePassword(char[] candidate) {
        return Arrays.equals(passwordHash, hash(candidate));
    }

    public void changePassword(char[] newPassword) {
        passwordHash = setPassword(newPassword);
    }
}