package org.fenixedu.cms.domain;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.io.domain.GroupBasedFile;

import com.qubit.terra.framework.services.ServiceProvider;
import com.qubit.terra.framework.services.accessControl.AccessControlExpressionService;
import com.qubit.terra.framework.services.accessControl.AccessControlService;
import com.qubit.terra.framework.services.context.ApplicationUser;

/**
 * Bridges CMS access rules to the shared access-control service while retaining legacy Bennu groups.
 */
public final class CmsAccessControl {

    private CmsAccessControl() {
    }

    public static Optional<Group> parseLegacyExpression(final String expression) {
        if (StringUtils.isBlank(expression) || isOmnisExpression(expression)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Group.parse(expression));
        } catch (final RuntimeException e) {
            return Optional.empty();
        }
    }

    public static void setFileAccessRule(final GroupBasedFile file, final String expression) {
        dispatchFileAccessRule(expression, file::setAccessGroup, file::setAccessControlExpression);
    }

    public static void dispatchFileAccessRule(final String expression, final Consumer<Group> setLegacyGroup,
            final Consumer<String> setOmnisExpression) {
        final Optional<Group> legacyGroup = parseLegacyExpression(expression);
        if (legacyGroup.isPresent()) {
            setLegacyGroup.accept(legacyGroup.get());
        } else {
            setOmnisExpression.accept(expression);
        }
    }

    public static String unionExpressions(final Collection<String> expressions) {
        final List<String> nonBlankExpressions = expressions.stream().filter(StringUtils::isNotBlank).toList();
        if (nonBlankExpressions.isEmpty()) {
            return Group.nobody().getExpression();
        }

        Boolean omnisExpression = null;
        Group fenixGroup = null;
        for (final String expression : nonBlankExpressions) {
            final boolean currentIsOmnis = isOmnisExpression(expression);
            if (omnisExpression != null && omnisExpression.booleanValue() != currentIsOmnis) {
                throw new IllegalArgumentException("Fenix and OMNIS access expressions cannot be combined in one string");
            }
            omnisExpression = currentIsOmnis;
            if (currentIsOmnis) {
                continue;
            }

            try {
                final Group currentGroup = Group.parse(expression);
                if (fenixGroup == null) {
                    fenixGroup = currentGroup;
                } else {
                    fenixGroup = fenixGroup.or(currentGroup);
                }
            } catch (final RuntimeException e) {
                throw new IllegalArgumentException("Invalid access expression: " + expression, e);
            }
        }

        if (Boolean.TRUE.equals(omnisExpression)) {
            return nonBlankExpressions.stream().map(expression -> "(" + expression + ")").collect(Collectors.joining(" | "));
        }
        return fenixGroup.getExpression();
    }

    public static boolean isOmnisExpression(final String expression) {
        if (StringUtils.isBlank(expression) || !ServiceProvider.isServiceAvailable(AccessControlExpressionService.class)) {
            return false;
        }
        try {
            return ServiceProvider.getService(AccessControlExpressionService.class).isValid(expression);
        } catch (final RuntimeException e) {
            return false;
        }
    }

}
