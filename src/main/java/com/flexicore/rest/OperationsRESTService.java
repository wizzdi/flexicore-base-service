/*******************************************************************************
 *  Copyright (C) FlexiCore, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Avishay Ben Natan And Asaf Ben Natan, October 2015
 ******************************************************************************/
package com.flexicore.rest;

import com.flexicore.annotations.IOperation;
import com.flexicore.annotations.IOperation.Access;
import com.flexicore.annotations.OperationsInside;
import com.flexicore.annotations.Protected;
import com.flexicore.data.jsoncontainers.PaginationResponse;
import com.flexicore.interfaces.RESTService;
import com.flexicore.model.Operation;
import com.flexicore.model.QueryInformationHolder;
import com.flexicore.request.OperationFiltering;
import com.flexicore.security.SecurityContext;
import com.flexicore.service.impl.BaselinkService;
import com.flexicore.service.impl.OperationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pf4j.Extension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;


@Path("/operations")
@RequestScoped
@Component
@OperationsInside
@Protected
@Tag(name = "Core")
@Tag(name = "Operations")
@Extension
public class OperationsRESTService implements RESTService {
    
    @Autowired
    private OperationService operationService;


    @Autowired
    private BaselinkService service;

   
   
    @GET
    @Produces(MediaType.APPLICATION_JSON)
	@IOperation(access=Access.allow,Name="listAllOperations",Description="lists all operations")
    public List<Operation> listAllOperations(@HeaderParam("authenticationkey") String authenticationkey,
    		@HeaderParam("pagesize") Integer pagesize,
    		@HeaderParam("currentpage") Integer currentpage,@Context SecurityContext securityContext) {
    	QueryInformationHolder<Operation> queryInformationHolder= new QueryInformationHolder<>( Operation.class,securityContext);
        return operationService.getAllFiltered(queryInformationHolder);
    }

    @Path("getAllOperations")
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @IOperation(access=Access.allow,Name="getAllOperations",Description="lists all operations")
    public PaginationResponse<Operation> getAllOperations(@HeaderParam("authenticationkey") String authenticationkey,
                                                           OperationFiltering operationFiltering,
                                                           @Context SecurityContext securityContext) {
        operationService.validate(operationFiltering,securityContext);
        return operationService.getAllOperations(operationFiltering,securityContext);
    }

    @PUT
    @Path("setOperationAuditable/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @IOperation(access=Access.allow,Name="setOperationAuditable",Description="Set Operation Auditable")
    public Operation setOperationAuditable(@HeaderParam("authenticationkey") String authenticationkey,
                                                 @PathParam("id") String id,
                                                 @HeaderParam("auditable") boolean auditable,
                                                 @Context SecurityContext securityContext) {
       Operation operation= operationService.findById(id);
       if(operation.isAuditable()!=auditable){
           operation.setAuditable(auditable);
           service.merge(operation);
           operationService.updateCahce(operation);
       }
       return operation;
    }

    @GET
    @Path("/{id:[^/]+?}")
    @Produces(MediaType.APPLICATION_JSON)
    public Operation lookupOperationById(@PathParam("id") String id) {
	    Operation operation = operationService.findById(id);
        if (operation == null) {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
        return operation;
    }


  

    
   
}
