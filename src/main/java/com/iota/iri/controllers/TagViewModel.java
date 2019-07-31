package com.iota.iri.controllers;

import com.iota.iri.model.Hash;
import com.iota.iri.model.TagHash;
import com.iota.iri.model.persistables.ObsoleteTag;
import com.iota.iri.model.persistables.Tag;
import com.iota.iri.storage.Indexable;
import com.iota.iri.storage.Persistable;
import com.iota.iri.storage.Tangle;
import com.iota.iri.utils.Pair;

import java.util.Set;

/**
 * Acts as a controller interface for a {@link Tag} set. These controllers are used within a
 * {@link TransactionViewModel} to manipulate a {@link Tag} set.
 */
public class TagViewModel implements HashesViewModel {
    private Tag self;
    private Indexable hash;

    /**
     * Creates an empty <tt>Tag</tt> set controller. This controller is created using a given hash identifier.
     *
     * @param hash The hash identifier that the {@link TagViewModel} will be referenced by
     */
    public TagViewModel(Hash hash) {
        this.hash = hash;
    }

    /**
     * Constructor for a {@link Tag} set controller from an existing {@link Tag} set. If the set is empty, a new
     * {@link Tag} set is created.
     *
     * @param hashes The {@link Tag} set that the controller will be created from
     * @param hash The {@link Hash} identifier that acts as a reference for the {@link Tag} set
     */
    private TagViewModel(Tag hashes, Indexable hash) {
        self = hashes == null || hashes.set == null ? new Tag(): hashes;
        this.hash = hash;
    }

    /**
     * Creates a new {@link Tag} set controller by converting a {@link com.iota.iri.model.persistables.Hashes}
     * referenced by the provided {@link Hash} identifer. This controller is generated by extracting the
     * {@link com.iota.iri.model.persistables.Hashes} set from the database using the {@link Hash} identifier
     * and casting this set to a {@link Tag} set. This set is then paired with the {@link Hash} identifier to create
     * and return a new {@link TagViewModel}.
     *
     *
     * @param tangle The tangle reference for the database to find the {@link Tag} set in
     * @param hash The hash identifier for the {@link Tag} set that needs to be found
     * @param model The provided {@link Hash} set to be converted
     * @return The {@link TagViewModel} controller generated
     * @throws Exception Thrown if the database cannot load an {@link Tag} set from the reference {@link Hash}
     */
    private static TagViewModel load(Tangle tangle, Indexable hash, Class<? extends Tag> model) throws Exception {
        return new TagViewModel((Tag) tangle.load(model, hash), hash);
    }

    /**
     * Creates a new {@link Tag} set controller. This controller is created by extracting the {@link Tag} set
     * from the database using the provided {@link Hash} identifier.
     *
     * @param tangle The tangle reference for the database to find the {@link Tag} set in
     * @param hash The hash identifier for the {@link Tag} set that needs to be found
     * @return The {@link TagViewModel} controller generated
     * @throws Exception Thrown if the database cannot load an {@link Tag} set from the reference {@link Hash}
     */
    public static TagViewModel load(Tangle tangle, Indexable hash) throws Exception {
        return load(tangle, hash, Tag.class);
    }

    public static TagViewModel loadExternal(Tangle tangle, Indexable hash) throws Exception {
        return new TagViewModel((Tag) tangle.loadExternal(Tag.class, hash), hash);
    }

    /**
     * Creates a new {@link ObsoleteTag} set controller. This controller is created by loading the {@link ObsoleteTag}
     * set referenced by the {@link Hash} identifier from the database, and loading a new {@link TagViewModel} for it.
     *
     * @param tangle The tangle reference for the database to find the {@link Tag} set in
     * @param hash The hash identifier for the {@link Tag} set that needs to be found
     * @return The {@link TagViewModel} controller generated
     * @throws Exception Thrown if the database cannot load an {@link Tag} set from the reference {@link Hash}
     */
    public static TagViewModel loadObsolete(Tangle tangle, Indexable hash) throws Exception {
        return load(tangle, hash, ObsoleteTag.class);
    }

    /**
     * Fetches the first persistable {@link Tag} set from the database and generates a new
     * {@link TagViewModel} from it. If no {@link Tag} sets exist in the database, it will return null.
     *
     * @param tangle the tangle reference for the database
     * @return The new {@link TagViewModel}
     * @throws Exception Thrown if the database fails to return a first object
     */
    public static TagViewModel first(Tangle tangle) throws Exception {
        Pair<Indexable, Persistable> tagPair = tangle.getFirst(Tag.class, TagHash.class);
        if(tagPair != null && tagPair.hi != null) {
            return new TagViewModel((Tag) tagPair.hi, (Hash) tagPair.low);
        }
        return null;
    }

    @Override
    public boolean store(Tangle tangle) throws Exception {
        return tangle.save(self, hash);
    }

    @Override
    public int size() {
        return self.set.size();
    }

    @Override
    public boolean addHash(Hash theHash) {
        return getHashes().add(theHash);
    }

    @Override
    public Indexable getIndex() {
        return hash;
    }

    @Override
    public Set<Hash> getHashes() {
        return self.set;
    }

    @Override
    public void delete(Tangle tangle) throws Exception {
        tangle.delete(Tag.class,hash);
    }

    @Override
    public TagViewModel next(Tangle tangle) throws Exception {
        Pair<Indexable, Persistable> tagPair = tangle.next(Tag.class, hash);
        if(tagPair != null && tagPair.hi != null) {
            return new TagViewModel((Tag) tagPair.hi, (Hash) tagPair.low);
        }
        return null;
    }
}
