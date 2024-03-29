/**
 * Copyright (C) FlexiCore, Inc - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Avishay Ben Natan And Asaf Ben Natan, October 2015
 */
/**
 *
 */
package com.flexicore.rest;

import com.flexicore.annotations.Baseclassroot;
import com.flexicore.annotations.IOperation;
import com.flexicore.annotations.IOperation.Access;
import com.flexicore.annotations.OperationsInside;
import com.flexicore.annotations.Protected;
import com.flexicore.data.BaseclassRepository;
import com.flexicore.data.jsoncontainers.PaginationResponse;
import com.flexicore.data.jsoncontainers.SetBaseclassTenantRequest;
import com.flexicore.interfaces.RESTService;
import com.flexicore.model.*;
import com.flexicore.model.nosql.BaseclassNoSQL;
import com.flexicore.request.*;
import com.flexicore.response.ClassInfo;
import com.flexicore.response.MassDeleteResponse;
import com.flexicore.security.SecurityContext;
import com.flexicore.service.impl.BaseclassNoSQLService;
import com.flexicore.service.impl.BaseclassService;
import com.flexicore.utils.InheritanceUtils;
import com.wizzdi.flexicore.boot.dynamic.invokers.response.ParameterInfo;
import com.wizzdi.flexicore.security.data.BasicRepository;
import com.wizzdi.flexicore.security.data.SecuredBasicRepository;
import com.wizzdi.flexicore.file.model.FileResource;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.csv.CSVFormat;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ClassUtils;

import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Protected
@RequestScoped
@Component
@Extension
@OperationsInside
@Path("/baseclass")
@OpenAPIDefinition(
        servers = {
                @Server(url = "/FlexiCore")
        },
        info = @Info(description = "Flexicore REST API", version = "V3.2.0",
                title = "REST API for Flexicore filtered by your access rights",
                termsOfService = "share and care", contact = @Contact(name = "Flexicore Support", email = "avishay@flexi-core.com", url = "http://wizzdi.com"), license = @License(name = "Contact for license", url = "")), tags = {
        @Tag(name = "Core", description = "Flexicore core functions",externalDocs = @ExternalDocumentation(description = "definition docs desc",url = "wwww.wizzdi.com")),
        @Tag(name = "Baseclasses", description = "Handles Baseclasses generic operations"),
        @Tag(name = "PermissionGroup", description = "Permission Groups Api"),

        @Tag(name = "Authentication", description = "Manage Sign-in and Sign-up"),
        @Tag(name = "AuthenticationNew", description = "Manage Sign-in and Sign-up"),
        @Tag(name = "TenantToUser", description = "TenantToUser API's"),

        @Tag(name = "Plugins", description = "Manage Plugins"),
        @Tag(name = "Tenants", description = "Manage Tenants"),
        @Tag(name = "Upload", description = "Upload Files"),
        @Tag(name = "Download", description = "Download Files"),
        @Tag(name = "tokenBased", description = "Token Based Api"),
        @Tag(name = "Operations", description = "Operations Api"),
        @Tag(name = "UIPlugin", description = "UIPlugin Api"),
        @Tag(name = "Security" ,description = "Security related api's")





}, externalDocs = @ExternalDocumentation(description = "Flexicore", url = "http://Flexicore-tech/docs.html"))

@Tag(name = "Core")
@Tag(name = "Baseclasses")
public class BaseclassRESTService implements RESTService {
    @Autowired
    @Baseclassroot
    private BaseclassRepository repository;


    @Autowired
    private BaseclassService baseclassService;

    @Autowired
    private BaseclassNoSQLService baseclassNoSQLService;

   private static final Logger logger = LoggerFactory.getLogger(BaseclassRESTService.class);


    @POST
    @Path("/listAllBaseclassGeneric")
    @Consumes("application/json")
    @Operation(summary = "listAllBaseclassGeneric",
            description = "lists all baseclass generically")

    public <T extends Baseclass, E extends FilteringInformationHolder> PaginationResponse<T> listAllBaseclassGeneric(@HeaderParam("authenticationkey") String authenticationkey,
                                                                                                                     E filteringInformationHolder,
                                                                                                                     @Context SecurityContext securityContext) {
        if (filteringInformationHolder.getResultType() == null) {
            throw new BadRequestException("for generic listing result type must be non null");
        }
        return baseclassService.listAllBaseclassGeneric(filteringInformationHolder, securityContext);

    }


    @POST
    @Path("/exportBaseclassGeneric")
    @Consumes("application/json")
    @Operation(summary = "exportBaseclassGeneric",
            description = "exports all baseclass generically")

    public <T extends Baseclass, E extends FilteringInformationHolder> FileResource exportBaseclassGeneric(@HeaderParam("authenticationkey") String authenticationkey,
                                                                                                           ExportBaseclassGeneric<E> baseclassGeneric,
                                                                                                           @Context SecurityContext securityContext) {
        if(baseclassGeneric.getFilter()==null){
            throw new BadRequestException("Filter must be non null");
        }
        if(baseclassGeneric.getFieldToName()==null || baseclassGeneric.getFieldToName().isEmpty()){
            throw new BadRequestException("field to name must be non null and not empty");
        }
        if (baseclassGeneric.getFilter().getResultType() == null) {
            throw new BadRequestException("for generic listing result type must be non null");
        }
        if(baseclassGeneric.getCsvFormat()==null){
            baseclassGeneric.setCsvFormat(CSVFormat.EXCEL);
        }
        return baseclassService.exportBaseclassGeneric(baseclassGeneric, securityContext);

    }


    @PUT
    @Path("/recover/{id}")
    @Consumes("application/json")
    @Operation(summary = "recover", description = "recover deleted baseclass")

    public Baseclass recover(@HeaderParam("authenticationkey") String authenticationkey,
                             @PathParam("id") String id,
                             @Context SecurityContext securityContext) {
        Baseclass b = baseclassService.getByIdOrNull(id, Baseclass.class, null, securityContext);
        if (b == null) {
            throw new BadRequestException("No Baseclass with id " + id);
        }
        b.setSoftDelete(false);
        baseclassService.merge(b);
        return b;

    }

    @POST
    @Path("/createBaseclasNoSQL")
    @Consumes("application/json")
    @Operation(summary = "createBaseclasNoSQL", description = "Create BaseclassNoSQL")

    public BaseclassNoSQL createBaseclasNoSQL(@HeaderParam("authenticationkey") String authenticationkey,
                                              BaseclassNoSQLCreate baseclassNoSQLCreate,
                                              @Context SecurityContext securityContext) {
       return baseclassNoSQLService.createBaseclassNoSQL(baseclassNoSQLCreate);

    }

    @POST
    @Path("/updateBaseclasNoSQL")
    @Consumes("application/json")
    @Operation(summary = "updateBaseclasNoSQL", description = "Update BaseclassNoSQL")

    public BaseclassNoSQL updateBaseclasNoSQL(@HeaderParam("authenticationkey") String authenticationkey,
                                              BaseclassNoSQLUpdate baseclassNoSQLUpdate,
                                              @Context SecurityContext securityContext) {
        String id = baseclassNoSQLUpdate.getId();
        BaseclassNoSQL baseclassNoSQL= id !=null?baseclassNoSQLService.getByIdOrNull(BaseclassNoSQL.class, id):null;
        if(baseclassNoSQL==null){
            throw new BadRequestException("No BaseclassNoSQL with id"+id);
        }
        baseclassNoSQLUpdate.setBaseclassNoSQL(baseclassNoSQL);
        return baseclassNoSQLService.updateBaseclassNoSQL(baseclassNoSQLUpdate);

    }


    @POST
    @Path("/listInheritingClasses")
    @Consumes("application/json")
    @Operation(summary = "listInheritingClasses",
            description = "lists inheriting classes")

    public PaginationResponse<ClassInfo> listInheritingClasses(@HeaderParam("authenticationkey") String authenticationkey,
                                                               GetClassInfo getClassInfo,
                                                               @Context SecurityContext securityContext) {
        return InheritanceUtils.listInheritingClassesWithFilter(getClassInfo);

    }

    @POST
    @Path("/getFilterClassInfo")
    @Consumes("application/json")
    @Operation(summary = "getFilterClassInfo",
            description = "returns filter class info")

    public ParameterInfo getFilterClassInfo(@HeaderParam("authenticationkey") String authenticationkey,
                                            GetClassInfo getClassInfo,
                                            @Context SecurityContext securityContext) {
        Set<ClassInfo> infos = com.flexicore.service.BaseclassService.getFilterClass(getClassInfo.getClassName());
        ClassInfo filterClass = infos!=null?infos.stream().findFirst().orElse(null):null;
        return filterClass != null  ? new ParameterInfo(filterClass.getClazz(),filterClass.getDisplayName()) : null;

    }


    @POST
    @Path("/getClassInfo")
    @Consumes("application/json")
    @Operation(summary = "getClassInfo",
            description = "getClassInfo")

    public ParameterInfo getClassInfo(@HeaderParam("authenticationkey") String authenticationkey,
                                      GetClassInfo filteringInformationHolder,
                                      @Context SecurityContext securityContext) {
        if (filteringInformationHolder.getClassName() == null) {
            throw new BadRequestException("Class Name cannot be null");
        }
        Class<?> c;
        try {
            c = Class.forName(filteringInformationHolder.getClassName());
        } catch (ClassNotFoundException e) {
            logger.error( "cannot find class ", e);
            throw new BadRequestException("No Class " + filteringInformationHolder.getClassName());
        }
        return new ParameterInfo(c);

    }


    @POST
    @Path("/getExample")
    @Consumes("application/json")
    @Operation(summary = "getExample",
            description = "getExample")

    public Object getExample(@HeaderParam("authenticationkey") String authenticationkey,
                                      GetClassInfo filteringInformationHolder,
                                      @Context SecurityContext securityContext) {
     return baseclassService.getExample(filteringInformationHolder);

    }




    @DELETE
    @Path("softDelete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @IOperation(access = Access.allow, Name = "softDelete", Description = "soft delete baseclass")
    @Operation(summary = "softDelete", description = "soft delete baseclass")
    public void softDelete(@HeaderParam("authenticationkey") String authenticationkey,
                           @HeaderParam("type") String type,
                           @PathParam("id") String id,
                           @Context SecurityContext securityContext) {
        SoftDeleteRequest softDeleteRequest = new SoftDeleteRequest()
                .setId(id)
                .setType(type);
        baseclassService.validate(softDeleteRequest,securityContext);
        baseclassService.softDelete(softDeleteRequest, securityContext);
    }


    @PUT
    @Path("importBaseclass/{className}")
    @Consumes(MediaType.TEXT_PLAIN)
    @IOperation(access = Access.allow, Name = "importBaseclass", Description = "import baseclass")
    @Operation(summary = "import baseclass",
            description = "import baseclass")

    public <T extends Baseclass> boolean importBaseclass(@HeaderParam("authenticationkey") String authenticationkey,
                                                         @PathParam("className") String className,
                                                         String json,
                                                         @Context SecurityContext securityContext) {
        try {
            Class<T> c = (Class<T>) Class.forName(className);
            T t = baseclassService.deserializeBaseclassForImport(json, c, securityContext);
            repository.merge(t);
            return true;

        } catch (ClassNotFoundException e) {
            logger.error( "unable to find type", e);
            throw new BadRequestException("no type " + className);
        } catch (IOException e) {
            logger.error( "could not deserialize ", e);

        }
        return false;
    }


    @PUT
    @Path("setBaseclassTenant")
    @Consumes(MediaType.APPLICATION_JSON)
    @Operation(summary = "setBaseclassTenant", description = "set Baseclass Tenant")
    public long setBaseclassTenant(
            @HeaderParam("authenticationkey") String authenticationkey,
            SetBaseclassTenantRequest setBaseclassTenantRequest,
            @Context SecurityContext securityContext) {
        if (setBaseclassTenantRequest.getIds() == null || setBaseclassTenantRequest.getIds().isEmpty()) {
            throw new BadRequestException("ids field cannot be empty");
        }
        if (setBaseclassTenantRequest.getTenantId() == null) {
            throw new BadRequestException("tenant Id must be non null");
        }
        List<Baseclass> baseclassList = baseclassService.listByIds(Baseclass.class, setBaseclassTenantRequest.getIds(), securityContext);
        Map<String, Baseclass> baseclasses = baseclassList.parallelStream().collect(Collectors.toMap(f -> f.getId(), f -> f));
        setBaseclassTenantRequest.getIds().removeAll(baseclasses.keySet());
        if (!setBaseclassTenantRequest.getIds().isEmpty()) {
            throw new BadRequestException("no baseclass with ids " + setBaseclassTenantRequest.getIds().parallelStream().collect(Collectors.joining(",")));
        }
        setBaseclassTenantRequest.setBaseclasses(baseclassList);
        Tenant tenant = baseclassService.getByIdOrNull(setBaseclassTenantRequest.getTenantId(), Tenant.class, null, securityContext);
        if (tenant == null) {
            throw new BadRequestException("No Tenant with id " + setBaseclassTenantRequest.getTenantId());
        }
        setBaseclassTenantRequest.setTenant(tenant);

        return baseclassService.setBaseclassTenant(setBaseclassTenantRequest, securityContext);

    }


    @POST
    @Path("connected/{wantedClazzName}/{id}/{linkClazzName}")
    @Consumes("application/json")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "get connected", Description = "gets connected instances to the instance supplied (by ID) filtered by Link type")
    public List<Baseclass> getConnected(@HeaderParam("authenticationkey") String authenticationkey,
                                        FilteringInformationHolder filteringInformationHolder,
                                        @HeaderParam("pagesize") @DefaultValue("-1") Integer pagesize,
                                        @HeaderParam("currentpage") @DefaultValue("-1") Integer currentpage,
                                        @PathParam("wantedClazzName") String wantedClazzName,
                                        @PathParam("id") String id,
                                        @PathParam("linkClazzName") String linkClazzName,
                                        @HeaderParam("valueId") String valueId,
                                        @HeaderParam("simpleValue") String simpleValue,
                                        @Context SecurityContext securityContext) {
        Clazz wantedClazz = Baseclass.getClazzByName(wantedClazzName);
        Clazz linkClazz = Baseclass.getClazzByName(linkClazzName);
        Baseclass value = valueId == null ? null : baseclassService.findByIdOrNull(Baseclass.class, valueId);
        if (value == null && valueId != null) {
            throw new BadRequestException("No Value with id " + valueId);
        }

        List<Baseclass> baseclasses;
        baseclasses = baseclassService.getConnected(wantedClazz, id, linkClazz, filteringInformationHolder, pagesize, currentpage, value, simpleValue,
                securityContext);

        return baseclasses;
    }


    @POST
    @Path("connectedClasses/{wantedClazzName}/{id}/{linkClazzName}")
    @Consumes("application/json")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "get connected", Description = "gets connected instances to the instance supplied (by ID) filtered by Link type")
    public List<String> getConnectedClasses(@HeaderParam("authenticationkey") String authenticationkey,
                                        FilteringInformationHolder filteringInformationHolder,
                                        @HeaderParam("pagesize") @DefaultValue("-1") Integer pagesize,
                                        @HeaderParam("currentpage") @DefaultValue("-1") Integer currentpage,
                                        @PathParam("wantedClazzName") String wantedClazzName,
                                        @PathParam("id") String id,
                                        @PathParam("linkClazzName") String linkClazzName,
                                        @HeaderParam("valueId") String valueId,
                                        @HeaderParam("simpleValue") String simpleValue,
                                        @Context SecurityContext securityContext) {
        Clazz wantedClazz = Baseclass.getClazzByName(wantedClazzName);
        Clazz linkClazz = Baseclass.getClazzByName(linkClazzName);
        Baseclass value = valueId == null ? null : baseclassService.findByIdOrNull(Baseclass.class, valueId);
        if (value == null && valueId != null) {
            throw new BadRequestException("No Value with id " + valueId);
        }

        List<String> baseclasses;
        baseclasses = baseclassService.getConnectedClasses(wantedClazz, id, linkClazz, filteringInformationHolder, pagesize, currentpage, value, simpleValue,
                securityContext);

        return baseclasses;
    }



    @POST
    @Path("getDisconnected")
    @Consumes("application/json")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "getDisconnected", Description = "gets disconnected instances to the instance supplied (by ID) filtered by Link type")
    public PaginationResponse<Baseclass> getDisconnected(@HeaderParam("authenticationkey") String authenticationkey,
                                                         GetDisconnected getDisconnected,
                                                         @Context SecurityContext securityContext) {
        long start=System.currentTimeMillis();
        baseclassService.validate(getDisconnected, securityContext);
        logger.info("validation took "+(System.currentTimeMillis()-start)+" ms");
        start=System.currentTimeMillis();
        PaginationResponse<Baseclass> disconnected = baseclassService.getDisconnected(getDisconnected, securityContext);
        logger.info("fetching took "+(System.currentTimeMillis()-start)+" ms");

        return disconnected;
    }


    @POST
    @Path("getConnected")
    @Consumes("application/json")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "getConnected", Description = "gets connected instances to the instance supplied (by ID) filtered by Link type")
    public PaginationResponse<Baseclass> getConnected(@HeaderParam("authenticationkey") String authenticationkey,
                                                      GetConnected getConnected,
                                                      @Context SecurityContext securityContext) {
        baseclassService.validate(getConnected, securityContext);
        return baseclassService.getConnected(getConnected, securityContext);
    }

    @POST
    @Path("countConnected/{wantedClazzName}/{id}/{linkClazzName}")
    @Consumes("application/json")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "get connected", Description = "gets connected instances to the instance supplied (by ID) filtered by Link type")
    public long countConnected(@HeaderParam("authenticationkey") String authenticationkey,

                               FilteringInformationHolder filteringInformationHolder,
                               @HeaderParam("pagesize") @DefaultValue("-1") Integer pagesize,
                               @HeaderParam("currentpage") @DefaultValue("-1") Integer currentpage,
                               @PathParam("wantedClazzName") String wantedClazzName,
                               @PathParam("id") String id,
                               @PathParam("linkClazzName") String linkClazzName,
                               @HeaderParam("valueId") String valueId,
                               @HeaderParam("simpleValue") String simpleValue,
                               @Context SecurityContext securityContext) {
        Clazz wantedClazz = Baseclass.getClazzByName(wantedClazzName);
        Clazz linkClazz = Baseclass.getClazzByName(linkClazzName);
        Baseclass value = valueId == null ? null : baseclassService.findByIdOrNull(Baseclass.class, valueId);
        if (value == null && valueId != null) {
            throw new BadRequestException("No Value with id " + valueId);
        }

        long baseclasses;
        baseclasses = baseclassService.countConnected(wantedClazz, id, linkClazz, filteringInformationHolder, pagesize, currentpage,
                value, simpleValue,
                securityContext);

        return baseclasses;
    }

    @POST
    @Path("countDisconnected/{wantedClazzName}/{id}/{linkClazzName}")
    @Consumes("application/json")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "get connected", Description = "gets connected instances to the instance supplied (by ID) filtered by Link type")
    public long countDisconnected(@HeaderParam("authenticationkey") String authenticationkey,

                                  FilteringInformationHolder filteringInformationHolder,
                                  @HeaderParam("pagesize") @DefaultValue("-1") Integer pagesize,
                                  @HeaderParam("currentpage") @DefaultValue("-1") Integer currentpage,
                                  @PathParam("wantedClazzName") String wantedClazzName,
                                  @PathParam("id") String id,
                                  @PathParam("linkClazzName") String linkClazzName,
                                  @HeaderParam("valueId") String valueId,
                                  @HeaderParam("simpleValue") String simpleValue,
                                  @Context SecurityContext securityContext) {
        Clazz wantedClazz = Baseclass.getClazzByName(wantedClazzName);
        Clazz linkClazz = Baseclass.getClazzByName(linkClazzName);
        Baseclass value = valueId == null ? null : baseclassService.findByIdOrNull(Baseclass.class, valueId);
        if (value == null && valueId != null) {
            throw new BadRequestException("No Value with id " + valueId);
        }

        long baseclasses;
        try {
            baseclasses = baseclassService.countDisconnected(wantedClazz, id, linkClazz, filteringInformationHolder, pagesize, currentpage, value, simpleValue,
                    securityContext);
        } catch (Exception e) {
            throw new BadRequestException(e);
        }
        return baseclasses;
    }


    @POST
    @Path("count/{type}")
    @Consumes("application/json")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "count type", Description = "gets connected instances to the instance supplied (by ID) filtered by Link type")
    public <T extends Baseclass> long count(@HeaderParam("authenticationkey") String authenticationkey,
                                            @PathParam("type") String type,
                                            FilteringInformationHolder filteringInformationHolder, @Context SecurityContext securityContext) {

        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(type);
        } catch (ClassNotFoundException e) {
            throw new BadRequestException("no type with name: " + type);
        }
        return baseclassService.count(clazz, filteringInformationHolder, securityContext);

    }

    /**
     * get all instances of a certain type which are NOT connected to an
     * instance filtered by a link type. An example: Create a list of all Roles
     * NOT connected to an instance of a User. This is required when we need to
     * connect unconnected instances. In the generic view, disconnected Role
     * instances can be dragged and dropped to be connected.
     * @param authenticationkey authentication key
     * @param id id
     * @param wantedClazzName wanted type
     * @param linkClazzName link type
     * @param filteringInformationHolder filter
     * @param pagesize page size
     * @param currentpage current page
     * @param valueId value id
     * @param simpleValue simple value
     * @param securityContext security context
     * @return list of disconnected
     */
    @POST
    @Path("disconnected/{wantedClazzName}/{id}/{linkClazzName}")
    @Consumes("application/json")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "getDisconnected", Description = "gets disconnected instances to the instance supplied (by ID) filtered by Link type")
    public List<Baseclass> getDisconnected(@HeaderParam("authenticationkey") String authenticationkey,
                                           @PathParam("id") String id,
                                           @PathParam("wantedClazzName") String wantedClazzName, @PathParam("linkClazzName") String linkClazzName,
                                           FilteringInformationHolder filteringInformationHolder,
                                           @HeaderParam("pagesize") @DefaultValue("-1") Integer pagesize,
                                           @HeaderParam("currentpage") @DefaultValue("-1") Integer currentpage,
                                           @HeaderParam("valueId") String valueId,
                                           @HeaderParam("simpleValue") String simpleValue,
                                           @Context SecurityContext securityContext) {

        Clazz wantedClazz = Baseclass.getClazzByName(wantedClazzName);
        Clazz linkClazz = Baseclass.getClazzByName(linkClazzName);
        Baseclass value = valueId == null ? null : baseclassService.findByIdOrNull(Baseclass.class, valueId);
        if (value == null && valueId != null) {
            throw new BadRequestException("No Value with id " + valueId);
        }

        List<Baseclass> baseclasses;
        try {
            baseclasses = baseclassService.getDisconnected(wantedClazz, id, linkClazz, filteringInformationHolder, pagesize, currentpage, value, simpleValue,
                    securityContext);
        } catch (Exception e) {
            throw new BadRequestException(e);
        }

        return baseclasses;
    }



    @GET
    @Path("/{id}")
    @Produces("application/json")
    @Consumes("application/json")
    @IOperation(access = Access.allow, Name = "Get Baseclass by ID", Description = "find any base class by its id")
    @Operation(summary = "Find an instance by ID", description = "Find an instance of a Baseclass extender by its id, returns an instance of a Baseclass")
    public Baseclass findById(@HeaderParam("authenticationkey") String authenticationkey,
                              @PathParam("id") final String id,
                              @Context SecurityContext securitycontext) {

        return repository.getById(id, Baseclass.class, null, securitycontext);
    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/{name}/{classname}")
    @Produces("application/json")
    @Consumes("application/json")
    @IOperation(access = Access.allow, Name = "List Baseclass byname", Description = "get all base classes in the system having the provided name")
    @Operation(summary = "Find an instance by its name", description = "returns a list of instances of the type requested")

    public <T extends Baseclass> List<T> findByName(@HeaderParam("authenticationkey") String authenticationkey,
                                                    @PathParam("name") final String name,
                                                    @PathParam("classname") final String classname,
                                                    @Context SecurityContext securityContext) {
        String converted;
        try {
            converted = java.net.URLDecoder.decode(name, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            logger.error( "Error while converting name" + classname, e1);
            throw new ClientErrorException(Response.Status.BAD_REQUEST, e1);
        }
        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(classname);
            long start = System.currentTimeMillis();
            List<T> baseclass = repository.getByName(converted, clazz, null, securityContext);
            logger.info( "Find by name took: " + (System.currentTimeMillis() - start) + " MS");
            return baseclass;
        } catch (ClassNotFoundException e) {
            logger.error( "unable to find class: " + classname, e);
            throw new ClientErrorException(Response.Status.BAD_REQUEST, e);
        }

    }

    @SuppressWarnings("unchecked")
    @GET
    @Path("/getbyid/{id}/{classname}")
    @Produces("application/json")
    @Consumes("application/json")
    @IOperation(access = Access.allow, Name = "Get T extends Baseclass by ID", Description = "get a baseclass (generic) based on Id and canonical class name")
    @Operation(summary = "find entity by id and class", description = "Find an entity by Id and type, returns null if not found")
    public <T extends Basic> T findById(@HeaderParam("authenticationkey") String authenticationkey,
                                            @PathParam("id") final String id,
                                            @PathParam("classname") final String classname,
                                            @Context SecurityContext securityContext) {

        return baseclassService.findById(id, classname, securityContext);

    }




    @SuppressWarnings("unchecked")
    @POST
    @Path("like/name/{classname}")
    @Produces("application/json")
    @Consumes("application/json")
    @IOperation(access = Access.allow, Name = "List Baseclass byname", Description = "get all base classes in the system having the provided name")
    @Operation(summary = "Find an instance by its name with wildcard", description = "returns a list of instances of the type requested")

    public <T extends Baseclass> List<T> nameLike(@HeaderParam("authenticationkey") String authenticationkey,
                                                  @PathParam("classname") final String classname,
                                                  FilteringInformationHolder filteringInformationHolder,
                                                  @Context SecurityContext securityContext) {


        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(classname);
            long start = System.currentTimeMillis();
            QueryInformationHolder<T> queryInformationHolder = new QueryInformationHolder<>(filteringInformationHolder, clazz, securityContext);
            List<T> baseclass = baseclassService.getAllByKeyWordAndCategory(queryInformationHolder);
            logger.info( "Find by name took: " + (System.currentTimeMillis() - start) + " MS");
            return baseclass;
        } catch (ClassNotFoundException e) {
            logger.error( "unable to find class: " + classname, e);
            throw new ClientErrorException(Response.Status.BAD_REQUEST, e);
        }

    }



    /**
     * @param securityContext security context
     * @param authenticationkey authentication key
     * @param id id to delete
     */
    @DELETE
    @Path("{id}")
    @IOperation(access = Access.allow, Name = "delete Baseclass", Description = "deletes baseclass")
    @Operation(summary = "delete", description = "deletes an entity by id")
    public void deleteById(@HeaderParam("authenticationkey") String authenticationkey,
                           @PathParam("id") final String id,
                           @Context SecurityContext securityContext) {
        Baseclass b = repository.getById(id, Baseclass.class, null, securityContext);
        if (b == null) {
            throw new ClientErrorException("no entity with id: " + id, Response.Status.BAD_REQUEST);
        }
        repository.removeById(id, Baseclass.class);
    }


    @POST
    @Path("massDelete")
    @IOperation(access = Access.allow, Name = "mass delete Baseclass", Description = "mass deletes baseclass")
    @Operation(summary = "massDelete", description = "deletes an entity by id")
    public MassDeleteResponse massDelete(@HeaderParam("authenticationkey") String authenticationkey,
                                         MassDeleteRequest massDeleteRequest,
                                         @Context SecurityContext securityContext) {
        baseclassService.validate(massDeleteRequest, securityContext);
        baseclassService.massDelete(massDeleteRequest, securityContext);
        Set<String> ids = massDeleteRequest.getBaseclass().stream().map(f -> f.getId()).collect(Collectors.toSet());
        return new MassDeleteResponse().setDeletedIds(ids);
    }


    /**
     * @param authenticationkey authentication key
     * @param securityContext security context
     * @param <T> type to delete
     * @param clazzName type to delete
     * @param id id to delete
     */
    @DELETE
    @Path("{class_name}/{id}")
    @IOperation(access = Access.allow, Name = "delete typed Baseclass", Description = "deletes baseclass")
    @Operation(summary = "delete", description = "deletes an entity by id")
    public <T extends Baseclass> void deleteById(@HeaderParam("authenticationkey") String authenticationkey,
                                                 @PathParam("id") final String id,
                                                 @PathParam("class_name") final String clazzName,
                                                 @Context SecurityContext securityContext) {
        Class<T> clazz;
        try {
            clazz = (Class<T>) Class.forName(clazzName);

        } catch (ClassNotFoundException e) {
            throw new ClientErrorException("no entity type: " + clazzName, Response.Status.BAD_REQUEST);
        }
        T b = repository.getById(id, clazz, null, securityContext);
        if (b == null) {
            throw new ClientErrorException("no entity with id: " + id, Response.Status.BAD_REQUEST);
        }
        repository.removeById(id, clazz);
    }


}
