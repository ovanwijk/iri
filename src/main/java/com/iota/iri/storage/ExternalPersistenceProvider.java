package com.iota.iri.storage;

import com.iota.iri.utils.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Set;


/***
 * Abstract class that implements non useful functionality when storing transactions externally
 */
public abstract class ExternalPersistenceProvider implements PersistenceProvider {
    @Override
    public Pair<Indexable, Persistable> latest(Class<?> model, Class<?> indexModel) throws Exception {
        return null;
    }

    @Override
    public Set<Indexable> keysWithMissingReferences(Class<?> modelClass, Class<?> otherClass) throws Exception {
        return null;
    }

    @Override
    public boolean mayExist(Class<?> model, Indexable index) throws Exception {
        return true;
    }

    @Override
    public long count(Class<?> model) throws Exception {
        return 0;
    }

    @Override
    public void delete(Class<?> model, Indexable index) throws Exception {
        //db.delete(classTreeMap.get(model), index.bytes());
    }
    @Override
    public Set<Indexable> keysStartingWith(Class<?> modelClass, byte[] value) {
        return null;
    }


    @Override
    public Pair<Indexable, Persistable> next(Class<?> model, Indexable index) throws Exception {
        return null;
    }

    @Override
    public Pair<Indexable, Persistable> previous(Class<?> model, Indexable index) throws Exception {
        return null;
    }

    @Override
    public Pair<Indexable, Persistable> first(Class<?> model, Class<?> indexModel) throws Exception {
        return null;
    }

    @Override
    public void deleteBatch(Collection<Pair<Indexable, ? extends Class<? extends Persistable>>> models) throws Exception {
        // permanent datastore, we are not deleting anything
    }

    @Override
    public void clear(Class<?> column) throws Exception {

    }

    @Override
    public void clearMetadata(Class<?> column) throws Exception {

    }

    @Override
    public List<byte[]> loadAllKeysFromTable(Class<? extends Persistable> model) {
        return null;
    }
}
