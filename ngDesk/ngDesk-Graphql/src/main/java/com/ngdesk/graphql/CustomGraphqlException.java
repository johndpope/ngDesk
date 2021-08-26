package com.ngdesk.graphql;

import java.util.LinkedHashMap; 
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ngdesk.commons.Global;

import graphql.ErrorType;
import graphql.GraphQLError;
import graphql.language.SourceLocation;

public class CustomGraphqlException extends RuntimeException implements GraphQLError {
    private final int errorCode;
    private final String[] vars;

    public CustomGraphqlException(int errorCode, String errorMessage, String[] vars) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.vars = vars;
    }

    @Override
    public Map<String, Object> getExtensions() {
        Map<String, Object> customAttributes = new LinkedHashMap<>();
        
        customAttributes.put("errorCode", this.errorCode);
        customAttributes.put("errorMessage", this.getMessage());
        customAttributes.put("vars", this.vars);
        return customAttributes;
    }

    @Override
    public List<SourceLocation> getLocations() {
        return null;
    }

    @Override
    public ErrorType getErrorType() {
        return null;
    }
}
