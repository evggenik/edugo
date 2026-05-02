package com.evggenn.edugo.user.exception;

import com.evggenn.edugo.user.RoleName;

public class SchoolRoleNotFoundException extends RuntimeException {
    public SchoolRoleNotFoundException(RoleName schoolRole) {
        super("SchoolRole not found: " + schoolRole.name());
    }
}