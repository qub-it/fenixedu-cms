package org.fenixedu.cms.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.FenixFrameworkRunner;

import com.qubit.terra.framework.services.ServiceProvider;
import com.qubit.terra.framework.services.accessControl.AccessControlService;
import com.qubit.terra.framework.services.accessControl.AccessControlExpressionService;
import com.qubit.terra.framework.services.accessControl.Permission;
import com.qubit.terra.framework.services.context.ApplicationUser;


@RunWith(FenixFrameworkRunner.class)
public class TestExpressionAccessControl extends TestCMS {

    private static final String CMS_EDITOR_EXPRESSION = "code[example.CmsEditorsGroup]";

    @BeforeClass
    public static void registerTestAccessControlService() {
        if (!ServiceProvider.isServiceAvailable(AccessControlService.class)) {
            ServiceProvider.registerService(AccessControlService.class, TestAccessControlService.class);
        }
        if (!ServiceProvider.isServiceAvailable(AccessControlExpressionService.class)) {
            ServiceProvider.registerService(AccessControlExpressionService.class, TestAccessControlExpressionService.class);
        }
    }

    @Test
    public void cmsObjectsStoreRawExpressionsAndRetainLegacyGroups() {
        final User user = CmsTestUtils.createAuthenticatedUser("cms-editor-" + UUID.randomUUID());
        final Site site = CmsTestUtils.createSite(user, "expressionAccessSite");
        final Page page = CmsTestUtils.createPage(site, "expressionAccessPage");
        final Post post = CmsTestUtils.createPost(site, "expressionAccessPost");
        final String legacyExpression = Group.users(user).getExpression();

        site.setCanViewGroup(Group.users(user));
        assertNull(site.getViewAccessExpression());
        assertEquals(legacyExpression, site.getCanViewGroup().getExpression());
        site.setViewAccessExpression(legacyExpression);
        assertNull(site.getViewAccessExpression());
        assertEquals(legacyExpression, site.getCanViewGroup().getExpression());
        page.setViewAccessExpression(legacyExpression);
        post.setViewAccessExpression(legacyExpression);
        assertNull(page.getViewAccessExpression());
        assertNull(post.getViewAccessExpression());
        assertEquals(legacyExpression, page.getCanViewGroup().getExpression());
        assertEquals(legacyExpression, post.getCanViewGroup().getExpression());
        site.setViewAccessExpression(CMS_EDITOR_EXPRESSION);
        page.setViewAccessExpression(CMS_EDITOR_EXPRESSION);
        post.setViewAccessExpression(CMS_EDITOR_EXPRESSION);

        assertEquals(CMS_EDITOR_EXPRESSION, site.getViewAccessExpression());
        assertEquals(CMS_EDITOR_EXPRESSION, page.getViewAccessExpression());
        assertEquals(CMS_EDITOR_EXPRESSION, post.getViewAccessExpression());
        assertEquals(Group.nobody().getExpression(), site.getCanViewGroup().getExpression());
    }

    @Test
    public void accessControlServiceEvaluatesOmnisAndLegacyExpressions() {
        final User user = CmsTestUtils.createAuthenticatedUser("cms-editor-" + UUID.randomUUID());
        final User otherUser = CmsTestUtils.createAuthenticatedUser("cms-viewer-" + UUID.randomUUID());
        final String legacyExpression = Group.users(user).getExpression();

        assertTrue(CmsAccessControl.hasAccess(user, CMS_EDITOR_EXPRESSION, Group.nobody()));
        assertFalse(CmsAccessControl.hasAccess(otherUser, CMS_EDITOR_EXPRESSION, Group.nobody()));
        assertTrue(CmsAccessControl.hasAccess(user, legacyExpression, null));
        assertFalse(CmsAccessControl.hasAccess(otherUser, legacyExpression, null));
    }

    @Test
    public void whitespaceOnlyExpressionFallsBackToLegacyGroup() {
        assertTrue(CmsAccessControl.hasAccess(null, "\u2003", Group.anyone()));
    }

    @Test
    public void rolesAndCmsManagerGroupsUseExpressionAccess() {
        final User user = CmsTestUtils.createAuthenticatedUser("cms-editor-" + UUID.randomUUID());
        final Site site = CmsTestUtils.createSite(user, "expressionRoleSite");
        final Role role = site.getRolesSet().iterator().next();
        role.setAccessControlExpression(CMS_EDITOR_EXPRESSION);
        CmsSettings.getInstance().setFoldersManagersAccessExpression(CMS_EDITOR_EXPRESSION);

        assertTrue(role.hasAccess(user));
        assertTrue(CmsSettings.getInstance().canManageFolders());
    }

    @Test
    public void permissionUnionPreservesExpressionLanguageAndRejectsMixedSyntax() {
        final String secondOmnisExpression = "code[example.CmsAuthorsGroup]";
        assertEquals("(" + CMS_EDITOR_EXPRESSION + ") | (" + secondOmnisExpression + ")",
                CmsAccessControl.unionExpressions(List.of(CMS_EDITOR_EXPRESSION, secondOmnisExpression)));
        assertEquals(Group.anyone().or(Group.logged()).getExpression(),
                CmsAccessControl.unionExpressions(List.of(Group.anyone().getExpression(), Group.logged().getExpression())));
    }

    @Test(expected = IllegalArgumentException.class)
    public void permissionUnionRejectsMixedFenixAndOmnisSyntax() {
        CmsAccessControl.unionExpressions(List.of(Group.anyone().getExpression(), CMS_EDITOR_EXPRESSION));
    }

    @Test
    public void importedFileRulesPreserveFenixAndOmnisModes() {
        final AtomicReference<Group> legacyGroup = new AtomicReference<>();
        final AtomicReference<String> expression = new AtomicReference<>();

        CmsAccessControl.dispatchFileAccessRule(Group.anyone().getExpression(), legacyGroup::set, expression::set);
        assertEquals(Group.anyone().getExpression(), legacyGroup.get().getExpression());
        assertNull(expression.get());

        legacyGroup.set(null);
        expression.set(null);
        CmsAccessControl.dispatchFileAccessRule(CMS_EDITOR_EXPRESSION, legacyGroup::set, expression::set);
        assertNull(legacyGroup.get());
        assertEquals(CMS_EDITOR_EXPRESSION, expression.get());
    }

    @Test
    public void cmsObjectClonesPreserveExpressionBackedVisibility() {
        final User user = CmsTestUtils.createAuthenticatedUser("cms-clone-" + UUID.randomUUID());
        final Site site = CmsTestUtils.createSite(user, "expressionCloneSite");
        final Page page = CmsTestUtils.createPage(site, "expressionClonePage");
        final Post post = CmsTestUtils.createPost(site, "expressionClonePost");
        site.setViewAccessExpression(CMS_EDITOR_EXPRESSION);
        page.setViewAccessExpression(CMS_EDITOR_EXPRESSION);
        post.setViewAccessExpression(CMS_EDITOR_EXPRESSION);

        assertEquals(CMS_EDITOR_EXPRESSION, site.clone(new CloneCache()).getViewAccessExpression());
        assertEquals(CMS_EDITOR_EXPRESSION, page.clone(new CloneCache()).getViewAccessExpression());
        assertEquals(CMS_EDITOR_EXPRESSION, post.clone(new CloneCache()).getViewAccessExpression());
    }

    @Test
    public void cmsObjectClonesKeepLegacyGroupsOutOfExpressionFields() {
        final User user = CmsTestUtils.createAuthenticatedUser("cms-legacy-clone-" + UUID.randomUUID());
        final Site site = CmsTestUtils.createSite(user, "legacyCloneSite");
        final Page page = CmsTestUtils.createPage(site, "legacyClonePage");
        final Post post = CmsTestUtils.createPost(site, "legacyClonePost");
        final Group legacyGroup = Group.users(user);

        site.setCanViewGroup(legacyGroup);
        page.setCanViewGroup(legacyGroup);
        post.setCanViewGroup(legacyGroup);

        final Site siteClone = site.clone(new CloneCache());
        final Page pageClone = page.clone(new CloneCache());
        final Post postClone = post.clone(new CloneCache());

        assertNull(siteClone.getViewAccessExpression());
        assertNull(pageClone.getViewAccessExpression());
        assertNull(postClone.getViewAccessExpression());
        assertEquals(legacyGroup.getExpression(), siteClone.getCanViewGroup().getExpression());
        assertEquals(legacyGroup.getExpression(), pageClone.getCanViewGroup().getExpression());
        assertEquals(legacyGroup.getExpression(), postClone.getCanViewGroup().getExpression());
    }

    public static class TestAccessControlService implements AccessControlService {

        @Override
        public Collection<ApplicationUser> getUsersWithAccess(final String accessExpression) {
            return Collections.emptyList();
        }

        @Override
        public boolean hasAccess(final String accessExpression) {
            return false;
        }

        @Override
        public boolean hasAccess(final String username, final String accessExpression) {
            if (CMS_EDITOR_EXPRESSION.equals(accessExpression)) {
                return username != null && username.startsWith("cms-editor-");
            }
            try {
                return Group.parse(accessExpression).isMember(User.findByUsername(username));
            } catch (RuntimeException e) {
                return false;
            }
        }
    }

    public static class TestAccessControlExpressionService implements AccessControlExpressionService {

        @Override
        public boolean isValid(final String expression) {
            return expression != null && expression.startsWith("code[") && expression.endsWith("]");
        }

        @Override
        public String getExpression(final Permission permission) {
            return "permission[" + permission.getCode() + "]";
        }
    }
}
