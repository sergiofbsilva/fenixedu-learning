package org.fenixedu.cms.domain.executionCourse;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import net.sourceforge.fenixedu.domain.ExecutionCourse;
import net.sourceforge.fenixedu.domain.accessControl.StudentGroup;
import net.sourceforge.fenixedu.domain.accessControl.StudentSharingDegreeOfCompetenceOfExecutionCourseGroup;
import net.sourceforge.fenixedu.domain.accessControl.StudentSharingDegreeOfExecutionCourseGroup;
import net.sourceforge.fenixedu.domain.accessControl.TeacherGroup;
import org.fenixedu.bennu.core.domain.Bennu;
import org.fenixedu.bennu.core.groups.AnyoneGroup;
import org.fenixedu.bennu.core.groups.Group;
import org.fenixedu.bennu.core.groups.LoggedGroup;
import org.fenixedu.bennu.portal.domain.MenuContainer;
import org.fenixedu.bennu.portal.domain.PortalConfiguration;
import org.fenixedu.cms.domain.CMSFolder;
import org.fenixedu.commons.i18n.LocalizedString;
import pt.ist.fenixframework.Atomic;
import pt.utl.ist.fenix.tools.util.i18n.MultiLanguageString;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.google.common.base.Joiner.on;
import static com.google.common.base.Preconditions.checkNotNull;

public class ExecutionCourseSite extends ExecutionCourseSite_Base {

    public ExecutionCourseSite(ExecutionCourse executionCourse) {
        checkNotNull(executionCourse);
        setExecutionCourse(executionCourse);
        setPublished(true);
        setFolder(folderForPath(PortalConfiguration.getInstance().getMenu(), "courses", getDescription()));
        setSlug(on("-").join(getExecutionCourse().getSigla(), getExecutionCourse().getExternalId()));
        setBennu(Bennu.getInstance());
    }

    @Override
    public LocalizedString getName() {
        return getExecutionCourse().getNameI18N().toLocalizedString();
    }

    @Override
    public LocalizedString getDescription() {
        return getObjectives().orElseGet(this::getName);
    }

    private Optional<LocalizedString> getObjectives() {
        return getExecutionCourse().getCompetenceCourses().stream()
                .map(competenceCourse -> competenceCourse.getObjectivesI18N(getExecutionCourse().getExecutionPeriod()))
                .filter(Objects::nonNull).map(MultiLanguageString::toLocalizedString).findFirst();
    }

    @Override
    @Atomic
    public void delete() {
        setExecutionCourse(null);
        super.delete();
    }

    private CMSFolder folderForPath(MenuContainer parent, String path, LocalizedString description) {
        return parent.getOrderedChild().stream().filter(item -> item.getPath().equals(path))
                .map(item -> item.getAsMenuFunctionality().getCmsFolder()).findAny()
                .orElseGet(() -> new CMSFolder(parent, path, description));
    }

    public List<Group> getContextualPermissionGroups() {
        List<Group> groups = Lists.newArrayList();
        groups.add(AnyoneGroup.get());
        groups.add(LoggedGroup.get());
        groups.add(TeacherGroup.get(getExecutionCourse()));
        groups.add(TeacherGroup.get(getExecutionCourse()).or(StudentGroup.get(getExecutionCourse())));
        groups.add(StudentSharingDegreeOfExecutionCourseGroup.get(getExecutionCourse()));
        groups.add(StudentSharingDegreeOfCompetenceOfExecutionCourseGroup.get(getExecutionCourse()));
        return groups;
    }

}