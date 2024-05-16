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
package org.fenixedu.cms.ui;

import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.cms.domain.*;
import org.fenixedu.cms.domain.PermissionsArray.Permission;

public class AdminPosts {

    /***
     * Use {@link Post#ensureCanEditPost()} instead
     * 
     * @param post
     */
    @Deprecated
    public static void ensureCanEditPost(Post post) {
        ensureCanEditPost(post.getSite(), post);
    }

    /***
     * Use {@link Post#ensureCanEditPost(Site)} instead
     * 
     * @param post
     */
    @Deprecated
    public static void ensureCanEditPost(Site site, Post post) {
        PermissionEvaluation.ensureCanDoThis(site, Permission.EDIT_POSTS);
        if (!Authenticate.getUser().equals(post.getCreatedBy())) {
            PermissionEvaluation.ensureCanDoThis(site, Permission.EDIT_OTHERS_POSTS);
        }
        if (post.isVisible()) {
            PermissionEvaluation.ensureCanDoThis(site, Permission.EDIT_POSTS_PUBLISHED);
        }
    }

}
