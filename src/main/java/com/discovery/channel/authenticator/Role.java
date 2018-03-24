package com.discovery.channel.authenticator;

public enum Role {
    STANDARD(0, "Standard"), ADMINISTRATOR(1, "Administrator"), RMC(2, "RMC");

    private int roleId;
    private String roleName;
    Role(int roleId, String roleName) {
        this.roleId = roleId;
        this.roleName = roleName;
    }

    public static Role fromRoleId(int roleId) {
        switch (roleId) {
            case 0:
                return STANDARD;
            case 1:
                return ADMINISTRATOR;
            case 2:
                return RMC;
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
