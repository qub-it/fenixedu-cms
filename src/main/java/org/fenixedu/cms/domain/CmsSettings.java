/**
 * Copyright © 2014 Instituto Superior Técnico
 *
 * This file is part of FenixEdu CMS.
 *
 * FenixEdu CMS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FenixEdu CMS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with FenixEdu CMS.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.fenixedu.cms.domain;

import org.apache.commons.lang3.StringUtils;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.domain.groups.PersistentDynamicGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.exceptions.CmsDomainException;

import com.qubit.terra.framework.services.ServiceProvider;
import com.qubit.terra.framework.services.accessControl.AccessControlService;

public class CmsSettings extends CmsSettings_Base {

    private CmsSettings() {
        super();
        setBennu(Bennu.getInstance());

        PersistentDynamicGroup managers = Group.managers().toPersistentGroup();
        setFoldersManagers(managers);
        setRolesManagers(managers);
        setSettingsManagers(managers);
        setThemesManagers(managers);
    }

    public boolean canManageFolders() {
        String expression =
                StringUtils.defaultIfBlank(getFoldersManagersAccessExpression(), getFoldersManagers().toGroup().getExpression());
        return ServiceProvider.getService(AccessControlService.class).hasAccess(Authenticate.getUser().getUsername(), expression);
    }

    public boolean canManageRoles() {
        String expression =
                StringUtils.defaultIfBlank(getRolesManagersAccessExpression(), getRolesManagers().toGroup().getExpression());
        return ServiceProvider.getService(AccessControlService.class).hasAccess(Authenticate.getUser().getUsername(), expression);
    }

    public boolean canManageSettings() {
        String expression = StringUtils.defaultIfBlank(getSettingsManagersAccessExpression(),
                getSettingsManagers().toGroup().getExpression());
        return ServiceProvider.getService(AccessControlService.class).hasAccess(Authenticate.getUser().getUsername(), expression);
    }

    public boolean canManageThemes() {
        String expression =
                StringUtils.defaultIfBlank(getThemesManagersAccessExpression(), getThemesManagers().toGroup().getExpression());
        return ServiceProvider.getService(AccessControlService.class).hasAccess(Authenticate.getUser().getUsername(), expression);
    }

    @Override
    public void setFoldersManagers(PersistentGroup group) {
        super.setFoldersManagers(group);
        setFoldersManagersAccessExpression(null);
    }

    @Override
    public void setRolesManagers(PersistentGroup group) {
        super.setRolesManagers(group);
        setRolesManagersAccessExpression(null);
    }

    @Override
    public void setSettingsManagers(PersistentGroup group) {
        super.setSettingsManagers(group);
        setSettingsManagersAccessExpression(null);
    }

    @Override
    public void setThemesManagers(PersistentGroup group) {
        super.setThemesManagers(group);
        setThemesManagersAccessExpression(null);
    }

    public boolean canManageGlobalPermissions() {
        return Group.managers().isMember(Authenticate.getUser());
    }

    public void ensureCanManageFolders() {
        if (!canManageFolders()) {
            throw CmsDomainException.forbiden();
        }
    }

    public void ensureCanManageRoles() {
        if (!canManageRoles()) {
            throw CmsDomainException.forbiden();
        }
    }

    public void ensureCanManageSettings() {
        if (!canManageSettings()) {
            throw CmsDomainException.forbiden();
        }
    }

    public void ensureCanManageThemes() {
        if (!canManageThemes()) {
            throw CmsDomainException.forbiden();
        }
    }

    public void ensureCanManageGlobalPermissions() {
        if (!canManageGlobalPermissions()) {
            throw CmsDomainException.forbiden();
        }
    }

    public static CmsSettings getInstance() {
        if(Bennu.getInstance().getCmsSettings()==null){
            init();
        }
        
        return Bennu.getInstance().getCmsSettings();
    }
    
    public static void init() {
        if(Bennu.getInstance().getCmsSettings()==null){
            Bennu.getInstance().setCmsSettings(new CmsSettings());
        }
    }
}
