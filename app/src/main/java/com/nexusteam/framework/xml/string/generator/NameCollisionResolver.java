package com.nexusteam.framework.xml.string.generator;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public final class NameCollisionResolver {

    private final Set<String> existingNames;
    private final NameGenerator generator;

    public NameCollisionResolver(Set<String> existingNames, NameGenerator generator) {
        this.existingNames = new HashSet<>(Objects.requireNonNull(existingNames));
        this.generator = Objects.requireNonNull(generator);
    }

    public String nextUnique() {
        String candidate;
        do {
            candidate = generator.next();
        } while (existingNames.contains(candidate));
        existingNames.add(candidate);
        return candidate;
    }

    public Set<String> getKnownNames() {
        return Set.copyOf(existingNames);
    }
}
