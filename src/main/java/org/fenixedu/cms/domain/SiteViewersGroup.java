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
import org.fenixedu.bennu.core.annotation.GroupArgument;
import org.fenixedu.bennu.core.annotation.GroupArgumentParser;
import org.fenixedu.bennu.core.annotation.GroupOperator;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.domain.groups.PersistentGroup;
import org.fenixedu.bennu.core.groups.ArgumentParser;
import org.fenixedu.bennu.core.groups.CustomGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.joda.time.DateTime;

import java.util.*;
import java.util.stream.Stream;

import com.qubit.terra.framework.services.ServiceProvider;
import com.qubit.terra.framework.services.accessControl.AccessControlService;
import com.qubit.terra.framework.services.context.ApplicationUser;

@GroupOperator("siteViewers")
public class SiteViewersGroup extends CustomGroup {

    private static final long serialVersionUID = -2690871317679895561L;

    @GroupArgumentParser
    public static final class SiteArgumentParser implements ArgumentParser<Site> {

        @Override
        public Site parse(String argument) {
            return Site.fromSlug(argument);
        }

        @Override
        public String serialize(Site argument) {
            return argument.getSlug();
        }

        @Override
        public Class<Site> type() {
            return Site.class;
        }

    }

    @GroupArgument("")
    private final Site site;

    private SiteViewersGroup() {
        this.site = null;
    }

    private SiteViewersGroup(Site site) {
        this.site = site;
    }

    public static Group get(Site site) {
        return new SiteViewersGroup(Objects.requireNonNull(site));
    }

    @Override
    public String getPresentationName() {
        return "Site Viewers for site: " + site;
    }

    @Override
    public PersistentGroup toPersistentGroup() {
        return site.getViewerGroup();
    }

    @Override
    public Stream<User> getMembers() {
        if (!site.getPublished()) {
            return Stream.empty();
        }

        String expression = StringUtils.defaultIfBlank(site.getViewAccessExpression(), site.getCanViewGroup().getExpression());
        return ServiceProvider.getService(AccessControlService.class).getUsersWithAccess(expression).stream()
                .map(ApplicationUser::getUsername).map(User::findByUsername);
    }

    @Override
    public Stream<User> getMembers(DateTime when) {
        return getMembers();
    }

    @Override
    public boolean isMember(User user) {
        if (Optional.ofNullable(site.getPublished()).orElse(false)) {
            return site.canView(user);
        }
        return false;
    }

    @Override
    public boolean isMember(User user, DateTime when) {
        return isMember(user);
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof SiteViewersGroup) {
            SiteViewersGroup other = (SiteViewersGroup) object;
            return site.equals(other.site);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return site.hashCode();
    }
}
