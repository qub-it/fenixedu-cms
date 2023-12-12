package org.fenixedu.bennu.core.groups;

import org.fenixedu.bennu.core.groups.CustomGroupRegistry.BooleanParser;
import org.fenixedu.bennu.core.groups.CustomGroupRegistry.DateTimeParser;
import org.fenixedu.bennu.core.groups.CustomGroupRegistry.StringParser;
import org.fenixedu.cms.domain.SiteViewersGroup;
import org.fenixedu.cms.domain.SiteViewersGroup.SiteArgumentParser;

public class ManualGroupRegister {
    private static boolean done = false;

    public static void ensure() {
        if (!done) {
            CustomGroupRegistry.registerCustomGroup(AnonymousGroup.class);
            CustomGroupRegistry.registerCustomGroup(AnyoneGroup.class);
            CustomGroupRegistry.registerCustomGroup(SiteViewersGroup.class);
            CustomGroupRegistry.registerCustomGroup(LoggedGroup.class);
            CustomGroupRegistry.registerCustomGroup(NobodyGroup.class);
            CustomGroupRegistry.registerCustomGroup(UserGroup.class);
            CustomGroupRegistry.registerArgumentParser(UserGroup.UserArgumentParser.class);
            CustomGroupRegistry.registerArgumentParser(SiteArgumentParser.class);
            CustomGroupRegistry.registerArgumentParser(BooleanParser.class);
            CustomGroupRegistry.registerArgumentParser(StringParser.class);
            CustomGroupRegistry.registerArgumentParser(DateTimeParser.class);
            done = true;
        }
    }
}
