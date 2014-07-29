package org.fenixedu.bennu.cms.domain;

import java.util.Comparator;
import java.util.HashSet;

import org.fenixedu.bennu.core.domain.User;
import org.fenixedu.bennu.core.security.Authenticate;
import org.fenixedu.bennu.core.util.CoreConfiguration;
import org.fenixedu.commons.i18n.LocalizedString;
import org.joda.time.DateTime;

import pt.ist.fenixframework.Atomic;

/**
 * A post models a given content to be presented to the user.
 */
public class Post extends Post_Base {

    public static final Comparator<? super Post> CREATION_DATE_COMPARATOR = (o1, o2) -> o1.getCreationDate().compareTo(
            o2.getCreationDate());

    /**
     * The logged {@link User} creates a new Post.
     */
    public Post() {
        super();
        if (Authenticate.getUser() == null) {
            throw new RuntimeException("Needs Login");
        }
        this.setCreatedBy(Authenticate.getUser());
        this.setCreationDate(new DateTime());
        this.setActive(true);
    }

    /**
     * saves the name of the post and creates a new slug for the post.
     */
    @Override
    public void setName(LocalizedString name) {
        LocalizedString prevName = getName();
        super.setName(name);

        if (prevName == null) {
            setSlug(Site.slugify(name.getContent()));
        }
    }

    /**
     * @return the URL link to the slug's page.
     */
    public String getAddress() {
        Page page = this.getSite().getViewPostPage();;
        if (page == null && !this.getComponentSet().isEmpty()) {
            page = this.getComponentSet().iterator().next().getPage();
        }
        if (page != null) {
            String path = CoreConfiguration.getConfiguration().applicationUrl();
            if (path.charAt(path.length() - 1) != '/') {
                path += "/";
            }
            path += this.getSite().getSlug() + "/" + page.getSlug() + "?q=" + this.getSlug();
            return path;
        }
        return null;
    }

    @Atomic
    public void delete() {
        for (Component c : this.getComponentSet()) {
            c.delete();
        }
        for (Category c : this.getCategoriesSet()) {
            removeCategories(c);
        }

        this.setCreatedBy(null);
        this.setSite(null);
        this.deleteDomainObject();
    }

    public void removeCategories() {
        new HashSet<>(getCategoriesSet()).forEach(c -> removeCategories(c));
    }

    public boolean hasPublicationPeriod() {
        return getPublicationBegin() != null && getPublicationEnd() != null;
    }

    public boolean isVisible() {
        boolean inPublicationPeriod =
                !hasPublicationPeriod() || (getPublicationBegin().isAfterNow() && getPublicationEnd().isBeforeNow());
        return getActive() && inPublicationPeriod;
    }
}