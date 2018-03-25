package com.discovery.channel.authenticator;

public enum Role {
    ADMINISTRATOR(1, "Administrator"), RMC(2, "RMC"), GENERAL(3, "General");

    private int roleId;
    private String roleName;
    Role(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public static Role fromRoleId(int roleId) {
        switch (roleId) {
            case 1:
                return ADMINISTRATOR;
            case 2:
                return RMC;
            case 3:
                return GENERAL;
            default:
                return null;
        }
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
