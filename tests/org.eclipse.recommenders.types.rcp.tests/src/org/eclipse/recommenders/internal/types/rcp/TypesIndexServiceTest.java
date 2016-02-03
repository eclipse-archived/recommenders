package org.eclipse.recommenders.internal.types.rcp;

import static com.google.common.base.Optional.of;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.recommenders.utils.names.ITypeName;
import org.eclipse.recommenders.utils.names.VmTypeName;
import org.hamcrest.Matchers;
import org.junit.Test;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;

public class TypesIndexServiceTest {

    private static final Optional<IProjectTypesIndex> NO_INDEX = Optional.<IProjectTypesIndex>absent();

    private static final ITypeName JAVA_UTIL_LIST = VmTypeName.get("Ljava/util/List");
    private static final String JAVA_UTIL_LIST_NAME = "List";

    private static final int ELEMENT_DELTA_KIND_UNSET = 0;
    private static final int ELEMENT_DELTA_FLAGS_UNSET = 0;

    @Test
    public void testSubtypesOfUnseenProject() {
        IIndexProvider provider = mock(IIndexProvider.class);
        IJavaProject project = mock(IJavaProject.class);
        mockProspectiveIndex(project, provider);

        TypesIndexService sut = new TypesIndexService(provider);
        Set<String> subtypes = sut.subtypes(JAVA_UTIL_LIST, project);

        assertThat(subtypes.isEmpty(), is(true));
    }

    @Test
    public void testSubtypesOfSeenProject() {
        IIndexProvider provider = mock(IIndexProvider.class);
        IJavaProject project = mock(IJavaProject.class);
        mockExistingIndex(project, provider, JAVA_UTIL_LIST_NAME);

        TypesIndexService sut = new TypesIndexService(provider);
        Set<String> subtypes = sut.subtypes(JAVA_UTIL_LIST, project);

        assertThat(subtypes, Matchers.contains(JAVA_UTIL_LIST_NAME));
        assertThat(subtypes.size(), is(1));
    }

    @Test
    public void testEventForNewProject() {
        IJavaProject newJavaProject = mockProject();
        IJavaElement element = mockElement(newJavaProject);
        IJavaElementDelta newProjectDelta = mockElementDelta(newJavaProject, IJavaElementDelta.ADDED,
                ELEMENT_DELTA_FLAGS_UNSET);

        IJavaElementDelta javaModelDelta = mockElementDelta(element, ELEMENT_DELTA_KIND_UNSET,
                IJavaElementDelta.F_CHILDREN, newProjectDelta);
        ElementChangedEvent projectAddedEvent = mockElementChangedEvent(javaModelDelta);

        IIndexProvider provider = mock(IIndexProvider.class);
        IProjectTypesIndex index = mockProspectiveIndex(newJavaProject, provider);

        TypesIndexService sut = new TypesIndexService(provider);
        sut.elementChanged(projectAddedEvent);

        verifyZeroInteractions(index);
    }

    @Test
    public void testEventForClasspathChangeOfUnseenProject() {
        IJavaProject project = mockProject();
        IJavaElementDelta changedProjectDelta = mockElementDelta(project, IJavaElementDelta.CHANGED,
                IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED);

        ElementChangedEvent projectChangedEvent = mockElementChangedEvent(changedProjectDelta);
        IIndexProvider provider = mock(IIndexProvider.class);

        IProjectTypesIndex index = mockProspectiveIndex(project, provider);

        TypesIndexService sut = new TypesIndexService(provider);
        sut.elementChanged(projectChangedEvent);

        verifyZeroInteractions(index);
    }

    @Test
    public void testEventForClasspathChangeOfSeenProject() {
        IJavaProject project = mockProject();
        IJavaElementDelta changedProjectDelta = mockElementDelta(project, IJavaElementDelta.CHANGED,
                IJavaElementDelta.F_RESOLVED_CLASSPATH_CHANGED);

        ElementChangedEvent projectChangedEvent = mockElementChangedEvent(changedProjectDelta);
        IIndexProvider provider = mock(IIndexProvider.class);

        IProjectTypesIndex index = mockExistingIndex(project, provider);

        TypesIndexService sut = new TypesIndexService(provider);
        sut.elementChanged(projectChangedEvent);

        verify(index).suggestRebuild();
    }

    @Test
    public void testEventForContentChangeOfPackageFragmentRootInSeenProject() {
        IJavaProject project = mockProject();
        IPackageFragmentRoot fragmentRoot = mockPackageFragmentRoot(project);
        IJavaElementDelta changedFragmentRootDelta = mockElementDelta(fragmentRoot, IJavaElementDelta.CHANGED,
                IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED);

        ElementChangedEvent projectChangedEvent = mockElementChangedEvent(changedFragmentRootDelta);
        IIndexProvider provider = mock(IIndexProvider.class);

        IProjectTypesIndex index = mockExistingIndex(project, provider);

        TypesIndexService sut = new TypesIndexService(provider);
        sut.elementChanged(projectChangedEvent);

        verify(index).suggestRebuild();
    }

    @Test
    public void testEventForReorderOfPackageFragmentRootInSeenProject() {
        IJavaProject project = mockProject();
        IPackageFragmentRoot fragmentRoot = mockPackageFragmentRoot(project);
        IJavaElementDelta changedFragmentRootDelta = mockElementDelta(fragmentRoot, IJavaElementDelta.CHANGED,
                IJavaElementDelta.F_REORDER);

        ElementChangedEvent projectChangedEvent = mockElementChangedEvent(changedFragmentRootDelta);
        IIndexProvider provider = mock(IIndexProvider.class);

        IProjectTypesIndex index = mockExistingIndex(project, provider);

        TypesIndexService sut = new TypesIndexService(provider);
        sut.elementChanged(projectChangedEvent);

        verifyZeroInteractions(index);
    }

    @Test
    public void testEventForChangeOfCompilationUnitInSeenProject() {
        IJavaProject project = mockProject();
        ICompilationUnit cu = mockCompilationUnit(project);
        IJavaElementDelta changedCompilationUnitDelta = mockElementDelta(cu, IJavaElementDelta.CHANGED, 0);

        ElementChangedEvent projectChangedEvent = mockElementChangedEvent(changedCompilationUnitDelta);
        IIndexProvider provider = mock(IIndexProvider.class);

        IProjectTypesIndex index = mockExistingIndex(project, provider);

        TypesIndexService sut = new TypesIndexService(provider);
        sut.elementChanged(projectChangedEvent);

        verifyZeroInteractions(index);
    }

    @Test
    public void testEventForProjectRemoved() {
        IJavaProject project = mockProject();
        IJavaElementDelta removedProjectDelta = mockElementDelta(project, IJavaElementDelta.REMOVED, 0);

        ElementChangedEvent projectRemovedEvent = mockElementChangedEvent(removedProjectDelta);
        IIndexProvider provider = mock(IIndexProvider.class);

        TypesIndexService sut = new TypesIndexService(provider);
        sut.elementChanged(projectRemovedEvent);

        verify(provider).deleteIndex(project);
    }

    public IProjectTypesIndex mockProspectiveIndex(IJavaProject newJavaProject, IIndexProvider provider,
            String... subtypes) {
        IProjectTypesIndex index = mock(IProjectTypesIndex.class);
        when(index.subtypes(any(ITypeName.class))).thenReturn(ImmutableSet.copyOf(subtypes));
        when(provider.findIndex(newJavaProject)).thenReturn(NO_INDEX);
        when(provider.findOrCreateIndex(newJavaProject)).thenReturn(index);
        return index;
    }

    public IProjectTypesIndex mockExistingIndex(IJavaProject newJavaProject, IIndexProvider provider,
            String... subtypes) {
        IProjectTypesIndex index = mock(IProjectTypesIndex.class);
        when(index.subtypes(any(ITypeName.class))).thenReturn(ImmutableSet.copyOf(subtypes));
        when(provider.findIndex(newJavaProject)).thenReturn(of(index));
        when(provider.findOrCreateIndex(newJavaProject)).thenReturn(index);
        return index;
    }

    private IJavaProject mockProject() {
        IJavaProject project = mock(IJavaProject.class);
        when(project.getJavaProject()).thenReturn(project);
        when(project.getElementType()).thenReturn(IJavaElement.JAVA_PROJECT);
        return project;
    }

    private IJavaElement mockElement(IJavaProject project) {
        IJavaElement element = mock(IJavaElement.class);
        when(element.getJavaProject()).thenReturn(project);
        return element;
    }

    private IPackageFragmentRoot mockPackageFragmentRoot(IJavaProject project) {
        IPackageFragmentRoot root = mock(IPackageFragmentRoot.class);
        when(root.getJavaProject()).thenReturn(project);
        when(root.getElementType()).thenReturn(IJavaElement.PACKAGE_FRAGMENT_ROOT);
        return root;
    }

    private ICompilationUnit mockCompilationUnit(IJavaProject project) {
        ICompilationUnit cu = mock(ICompilationUnit.class);
        when(cu.getJavaProject()).thenReturn(project);
        when(cu.getElementType()).thenReturn(IJavaElement.COMPILATION_UNIT);
        return cu;
    }

    private ElementChangedEvent mockElementChangedEvent(IJavaElementDelta javaElementDelta) {
        ElementChangedEvent event = mock(ElementChangedEvent.class);
        when(event.getDelta()).thenReturn(javaElementDelta);
        return event;
    }

    private IJavaElementDelta mockElementDelta(IJavaElement element, int kind, int flags,
            IJavaElementDelta... affectedChildren) {
        IJavaElementDelta delta = mock(IJavaElementDelta.class);

        when(delta.getFlags()).thenReturn(flags);
        when(delta.getElement()).thenReturn(element);
        when(delta.getKind()).thenReturn(kind);
        when(delta.getAffectedChildren()).thenReturn(affectedChildren);

        return delta;
    }

}
