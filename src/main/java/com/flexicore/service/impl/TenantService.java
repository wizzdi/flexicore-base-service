/*******************************************************************************
 *  Copyright (C) FlexiCore, Inc - All Rights Reserved
 *  Unauthorized copying of this file, via any medium is strictly prohibited
 *  Proprietary and confidential
 *  Written by Avishay Ben Natan And Asaf Ben Natan, October 2015
 ******************************************************************************/
package com.flexicore.service.impl;

import com.flexicore.annotations.rest.All;
import com.flexicore.data.TenantRepository;
import com.flexicore.data.jsoncontainers.PaginationResponse;
import com.flexicore.events.TenantCreatedEvent;
import com.flexicore.model.*;
import com.flexicore.request.*;
import com.flexicore.security.SecurityContext;
import com.wizzdi.flexicore.file.model.FileResource;
import com.wizzdi.flexicore.file.model.FileResource_;
import com.wizzdi.flexicore.file.service.FileResourceService;
import com.wizzdi.flexicore.security.service.SecurityTenantService;
import org.pf4j.Extension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.ws.rs.BadRequestException;
import java.util.ArrayList;
import java.util.List;

@Primary
@Component
@Extension
public class TenantService implements com.flexicore.service.TenantService {
    @Autowired
    private TenantRepository tenantRepository;

    @Autowired
    private FileResourceService fileResourceService;
    @Autowired
    private UserService userService;
    @Autowired
    private RoleService roleService;
    @Autowired
    private BaselinkService baselinkService;

    @Autowired
    private SecurityTenantService securityTenantService;
    @Autowired
    private BaseclassNewService baseclassService;
    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
   private static final Logger logger = LoggerFactory.getLogger(TenantService.class);


    @Override
    public PaginationResponse<Tenant> getTenants(TenantFilter tenantFilter, SecurityContext securityContext) {
        if (tenantFilter == null) {
            tenantFilter = new TenantFilter();
        }
        List<Tenant> list = listAllTenants(tenantFilter, securityContext);
        long count = tenantRepository.countAllTenants(tenantFilter,securityContext);
        return new PaginationResponse<>(list, tenantFilter, count);
    }

    @Override
    public List<Tenant> listAllTenants(TenantFilter tenantFilter, SecurityContext securityContext) {
        return tenantRepository.getAllTenants(tenantFilter, securityContext);
    }

    @Override
    public Tenant createTenant(TenantCreate tenantCreate, SecurityContext securityContext) {
        List<Object> toMergeUser = new ArrayList<>();
        List<Object> toMergeRole=new ArrayList<>();
        validate(tenantCreate,securityContext);
        UserCreate tenantAdmin = tenantCreate.getTenantAdmin();
        if(tenantAdmin!=null){
            userService.validateUserForCreate(tenantAdmin,securityContext);
        }
        Tenant tenant = createTenantNoMerge(tenantCreate, securityContext);
        tenantRepository.merge(tenant);
        Role role=null;
        User user=null;
        if (tenantAdmin != null) {
            Operation allOp = baselinkService.findById(Baseclass.generateUUIDFromString(All.class.getCanonicalName()));
            Clazz SecurityWildcard = Baseclass.getClazzByName(SecurityWildcard.class.getCanonicalName());
            user = userService.createUserNoMerge(tenantAdmin, securityContext);
            toMergeUser.add(user);
            TenantToUser tenantToUser=userService.createTenantToUserNoMerge(new TenantToUserCreate().setDefaultTenant(true).setUser(user).setTenant(tenant),securityContext);
            toMergeUser.add(tenantToUser);
            role = roleService.createRoleNoMerge(new RoleCreate().setName(tenant.getName()+" "+TENANT_ADMINISTRATOR_NAME), securityContext);
            role.setTenant(tenant);
            toMergeRole.add(role);
            RoleToUser roleToUser=userService.createRoleToUserNoMerge(new RoleToUserCreate().setUser(user).setRole(role),securityContext);
            toMergeRole.add(roleToUser);
            RoleToBaseclass roleToBaseclass=roleService.createRoleToBaseclassNoMerge(new RoleToBaseclassCreate().setRole(role).setBaseclass(SecurityWildcard).setOperation(allOp),securityContext);
            toMergeRole.add(roleToBaseclass);


        }
        userService.massMerge(toMergeUser);
        roleService.massMerge(toMergeRole);
        applicationEventPublisher.publishEvent(new TenantCreatedEvent(tenant,role,user));
        return tenant;

    }

    @Override
    public void validate(TenantCreate tenantCreate, SecurityContext securityContext) {
        securityTenantService.validate(tenantCreate,securityContext);
        FileResource icon=tenantCreate.getIconId()!=null?fileResourceService.getByIdOrNull(tenantCreate.getIconId(),FileResource.class, FileResource_.security,securityContext):null;
        if(icon==null && tenantCreate.getIconId()!=null){
            throw new BadRequestException("No Icon with id "+tenantCreate.getIconId());
        }
        tenantCreate.setIcon(icon);
    }

    @Override
    public void validateUpdate(TenantUpdate tenantUpdate, SecurityContext securityContext) {
        Tenant tenant=tenantUpdate.getId()!=null?tenantRepository.getByIdOrNull(tenantUpdate.getId(),Tenant.class,null,securityContext):null;
        if (tenant == null) {
            throw new BadRequestException("No Tenant with id "+tenantUpdate.getId());
        }
        tenantUpdate.setTenantToUpdate(tenant);
        String iconId = tenantUpdate.getIconId();
        FileResource icon= iconId !=null?fileResourceService.getByIdOrNull(iconId,FileResource.class,FileResource_.security,securityContext):null;
        if(icon==null && iconId !=null){
            throw new BadRequestException("No Icon with id "+ iconId);
        }
        tenantUpdate.setIcon(icon);
    }

    public Tenant createTenantNoMerge(TenantCreate tenantCreate, SecurityContext securityContext) {
        Tenant tenant=new Tenant(tenantCreate.getName(),securityContext);
        updateTenantNoMerge(tenant,tenantCreate);
        return tenant;
    }

    public boolean updateTenantNoMerge(Tenant tenant, TenantCreate tenantCreate) {
        boolean update=securityTenantService.updateTenantNoMerge(tenantCreate,tenant);
        if(tenantCreate.getIcon()!=null && (tenant.getIcon()==null||!tenantCreate.getIcon().getId().equals(tenant.getIcon().getId()))){
            tenant.setIcon(tenantCreate.getIcon());
            update=true;
        }
        return update;
    }

    @Transactional
    public void merge(Object base) {
        tenantRepository.merge(base);
    }


    @Override
    public Tenant updateTenant(TenantUpdate tenantUpdate, SecurityContext securityContext) {
        Tenant tenant=tenantUpdate.getTenantToUpdate();
        if(updateTenantNoMerge(tenant,tenantUpdate)){
            tenantRepository.merge(tenant);
        }
        return tenant;
    }
}
