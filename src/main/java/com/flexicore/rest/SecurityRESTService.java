package com.flexicore.rest;

import com.flexicore.annotations.IOperation;
import com.flexicore.annotations.IOperation.Access;
import com.flexicore.annotations.OperationsInside;
import com.flexicore.annotations.Protected;
import com.flexicore.interfaces.RESTService;
import com.flexicore.model.Operation;
import com.flexicore.request.PermissionSummaryRequest;
import com.flexicore.response.PermissionSummaryResponse;
import com.flexicore.security.SecurityContext;
import com.flexicore.service.impl.OperationService;
import com.flexicore.service.impl.SecurityService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

@Path("/security")
@RequestScoped
@Component
@OperationsInside
@Protected
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Tag(name = "Security")
@Extension
public class SecurityRESTService implements RESTService {
    @Autowired
    @Lazy
    private SecurityService securityService;
    @Autowired
    private OperationService operationRepository;



    @GET
    @Path("{id}")
    @IOperation(access = Access.allow, Name = "checkIfOperationIsAllowed", Description = "checks if operation is allowed")
    public boolean checkIfOperationIsAllowed(@HeaderParam("authenticationkey") String authenticationkey, @PathParam("id") String id, @Context SecurityContext securityContext) {
        Operation operation = operationRepository.findById(id);
        Access access = operation.getDefaultaccess();
        return securityService.checkIfAllowed(securityContext.getUser(), securityContext.getTenants(), operation, access);


    }


    @POST
    @Path("getPermissionsSummary")
    @io.swagger.v3.oas.annotations.Operation(description = "getPermissionsSummary", summary = "get permissions summary")
    public PermissionSummaryResponse getPermissionsSummary(@HeaderParam("authenticationkey") String authenticationkey,
                                                           PermissionSummaryRequest permissionSummaryRequest,
                                                           @Context SecurityContext securityContext) {
        securityService.validate(permissionSummaryRequest, securityContext);
    	return securityService.getPermissionsSummary(permissionSummaryRequest,securityContext);

    }

}
