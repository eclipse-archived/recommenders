package org.eclipse.recommenders.mining.extdocs.couch;

import org.eclipse.recommenders.commons.utils.names.ITypeName;
import org.eclipse.recommenders.internal.commons.analysis.codeelements.CompilationUnit;
import org.eclipse.recommenders.mining.extdocs.ICompilationUnitProvider;
import org.eclipse.recommenders.mining.extdocs.couch.CouchGuiceModule.Input;

import com.google.inject.Inject;

public class CouchCompilationUnitProvider implements ICompilationUnitProvider {

    private final CouchDbDataAccess db;

    @Inject
    public CouchCompilationUnitProvider(@Input final CouchDbDataAccess db) {
        this.db = db;
    }

    @Override
    public Iterable<CompilationUnit> getCompilationUnits(final ITypeName superclass) {
        return db.getCompilationUnitsForSuperclass(superclass);
    }

}
