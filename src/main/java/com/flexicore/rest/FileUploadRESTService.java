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
import com.flexicore.interfaces.RESTService;
import com.wizzdi.flexicore.file.model.FileResource;
import com.flexicore.model.Job;
import com.flexicore.model.Tenant;
import com.flexicore.model.User;
import com.flexicore.request.FinallizeFileResource;
import com.flexicore.response.FinalizeFileResourceResponse;
import com.flexicore.security.SecurityContext;
import com.flexicore.service.impl.FileResourceService;
import com.flexicore.service.impl.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.enterprise.context.RequestScoped;
import jakarta.persistence.NoResultException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;

@Path("/resources")
@RequestScoped
@Component
@Extension
@OperationsInside
@Protected
@Tag(name = "Upload")
@Tag(name = "Core")

public class FileUploadRESTService implements RESTService {

    @Autowired
    FileResourceService fileResourceService;
    @Autowired
    UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(FileUploadRESTService.class);


    /**
     * retreives file resource by md5
     *
     * @param authenticationkey authentication key
     * @param md5               md5 of requested file
     * @param securityContext security context
     * @return FileResource requested file
     */
    @GET
    @Path("{md5}")
    @Produces(MediaType.APPLICATION_JSON)
    @IOperation(access = Access.allow, Name = "gets file resource", Description = "gets a fileResource by MD5")

    public FileResource getFileResource(@HeaderParam("authenticationkey") String authenticationkey,
                                        @PathParam("md5") String md5, @Context SecurityContext securityContext) {
        FileResource fileResource = null;
        try {
            fileResource = fileResourceService.getExistingFileResource(md5,securityContext);
        } catch (NoResultException e) {
            logger.info( "no file resource with md5: " + md5);
        }

        if (fileResource != null) {
            File file = new File(fileResource.getFullPath());
            if (!file.exists()) {
                fileResource.setDone(false);
                fileResource.setOffset(0);
                fileResourceService.merge(fileResource);
            }
        }
        return fileResource;

    }

    @POST
    @Path("/upload")
    @Produces("application/json")
    @Consumes(MediaType.APPLICATION_OCTET_STREAM)
    @IOperation(access = Access.allow, Name = "uploadOctet", Description = "uploads a file octat way")
    public FileResource uploadFile(@HeaderParam("authenticationkey") String authenticationkey,
                                   @HeaderParam("md5") String md5, @HeaderParam("name") String name,
                                   @HeaderParam("chunkMd5") String chunkMd5,
                                   @HeaderParam("lastChunk") @DefaultValue("false") boolean lastChunk,
                                   InputStream stream, @Context SecurityContext securityContext) {
        return fileResourceService.uploadFileResource(name, securityContext, md5,chunkMd5,lastChunk, stream);

    }

    private String getFileName(MultivaluedMap<String, String> header) {

        String[] contentDisposition = header.getFirst("Content-Disposition").split(";");

        for (String filename : contentDisposition) {
            if ((filename.trim().startsWith("filename"))) {

                String[] name = filename.split("=");

                return name[1].trim().replaceAll("\"", "");
            }
        }
        return "unknown";
    }

    /**
     * finalize upload of a file and start analyzing and processing
     *
     * @param md5 finalized file md5
     * @param securityContext security context
     * @param authenticationkey authentication key
     * @param dontProcess true if no proccessing should be done
     * @param fileType file type
     * @param headers headers
     * @param hint hint for finalizing
     * @return processing/analyzing job to track the long running process
     */
    @GET
    @Path("finalize/{md5}")
    @Produces(MediaType.APPLICATION_JSON)
    @IOperation(access = Access.allow, Name = "Create New User", Description = "Create new user in the system")
    @Operation(summary = "finalize", description = "finalize FileResource , sending it for processing")
    public Job finalizeUpload(@HeaderParam("authenticationkey") String authenticationkey, @PathParam("md5") String md5,
                              @HeaderParam("hint") @DefaultValue("") String hint,
                              @HeaderParam("fileType") String fileType,
                              @HeaderParam("dontProcess") @DefaultValue("false") boolean dontProcess,
                              @Context HttpHeaders headers, @Context SecurityContext securityContext) {
        MultivaluedMap<String, String> headersMap = headers.getRequestHeaders();
        Properties props = multivaluedMapToProperties(headersMap);
        return fileResourceService.finalizeUpload(md5, securityContext, hint, props);

    }

    private Properties multivaluedMapToProperties(MultivaluedMap<String, String> map) {
        Properties props = new Properties();
        for (Entry<String, List<String>> entry : map.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            props.setProperty(entry.getKey(), entry.getValue().get(0));
        }
        return props;
    }

    /**
     * finalize upload of a file and start analyzing and processing
     * @param authenticationkey authentication key
     * @param finallizeFileResource finalize file resource
     * @param securityContext security context
     * @return finalized response
     */
    @POST
    @Path("finalize")
    @Produces(MediaType.APPLICATION_JSON)
    @Operation(summary = "finalize", description = "finalize FileResource",ignoreJsonView = true)
    public FinalizeFileResourceResponse finalizeUpload(@HeaderParam("authenticationkey") String authenticationkey,
                                                       FinallizeFileResource finallizeFileResource,
                                                       @Context SecurityContext securityContext) {
        fileResourceService.validate(finallizeFileResource, securityContext);
        return fileResourceService.finalize(finallizeFileResource, securityContext);

    }




    @DELETE
    @Path("{md5}")
    @IOperation(access = Access.allow, Name = "gets file resource", Description = "deletes file resource")
    public void deleteFileResource(@HeaderParam("authenticationkey") String authenticationkey, @PathParam("md5") String md5,@Context SecurityContext securityContext) {
        User user = userService.getUser(authenticationkey);
        List<Tenant> tenant = userService.getUserTenants(authenticationkey);
        FileResource fr = fileResourceService.getExistingFileResource(md5,securityContext);
        if (fr != null) {
            fileResourceService.deleteFileResource(fr, user, tenant);
        }

    }

    @GET
    @Path("validate")
    @Produces(MediaType.APPLICATION_JSON)
    @IOperation(access = Access.allow, Name = "Validate", Description = "Validates all FileResources")
    public boolean Validate(@HeaderParam("authenticationkey") String authenticationkey, @Context SecurityContext securityContext) {
        return fileResourceService.validate(securityContext).isEmpty();

    }

    @POST
    @Path("/register")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "registerFile", Description = "registers a local file")
    public FileResource registerFile(@HeaderParam("authenticationkey") String authenticationkey,
                                     @HeaderParam("path") String path,
                                     @HeaderParam("calcMd5") @DefaultValue("false") boolean calculateMd5,
                                     @Context SecurityContext securityContext) {
        if (path == null || path.isEmpty()) {
            throw new ClientErrorException("path header cannot be null", Response.Status.BAD_REQUEST);
        }
        try {
            return fileResourceService.registerFile(path, calculateMd5, securityContext);
        } catch (FileNotFoundException e) {
            throw new ClientErrorException(Response.Status.BAD_REQUEST, e);
        }


    }


    @POST
    @Path("/registerAndFinalize")
    @Produces("application/json")
    @IOperation(access = Access.allow, Name = "registerFileAndFinalize", Description = "registers a local file and then finalizes it")
    public Job registerFileAndFinlize(@HeaderParam("authenticationkey") String authenticationkey,
                                      @HeaderParam("path") String path,
                                      @HeaderParam("hint") @DefaultValue("") String hint,
                                      @HeaderParam("fileType") String fileType,
                                      @HeaderParam("dontProcess") @DefaultValue("false") boolean dontProcess,
                                      @HeaderParam("calcMd5") @DefaultValue("false") boolean calculateMd5,
                                      @Context HttpHeaders headers,
                                      @Context SecurityContext securityContext) {
        if (path == null || path.isEmpty()) {
            throw new ClientErrorException("path header cannot be null", Response.Status.BAD_REQUEST);
        }
        try {
            FileResource f = fileResourceService.registerFile(path, calculateMd5, securityContext);
            if (f != null) {
                MultivaluedMap<String, String> headersMap = headers.getRequestHeaders();
                Properties props = multivaluedMapToProperties(headersMap);
                return fileResourceService.finalizeUpload(f.getMd5(), securityContext, hint, props);
            } else {
                throw new ServerErrorException("registered file resource was null", Response.Status.INTERNAL_SERVER_ERROR);
            }
        } catch (FileNotFoundException e) {
            throw new ClientErrorException(Response.Status.BAD_REQUEST, e);
        }


    }
}
