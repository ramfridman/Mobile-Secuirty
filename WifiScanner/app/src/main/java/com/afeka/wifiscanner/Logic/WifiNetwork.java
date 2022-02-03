package com.afeka.wifiscanner.Logic;

public class WifiNetwork {
    private String name;
    private String macAddress;
    private int strength;

    public WifiNetwork(String name, String macAddress, int strength) {
        this.name = name;
        this.macAddress = macAddress;
        this.strength = strength;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }
}
