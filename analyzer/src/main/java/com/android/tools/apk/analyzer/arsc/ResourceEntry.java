package com.android.tools.apk.analyzer.arsc;

import com.google.common.base.Preconditions;

import java.util.Objects;

/**
 * Describes a single resource entry.
 */
public class ResourceEntry {
    private final String packageName;
    private final String typeName;
    private final String entryName;

    static ResourceEntry create(TypeChunk.Entry entry) {
        PackageChunk packageChunk = Preconditions.checkNotNull(entry.parent().getPackageChunk());
        String packageName = packageChunk.getPackageName();
        String typeName = entry.typeName();
        String entryName = entry.key();
        return new ResourceEntry(packageName, typeName, entryName);
    }

    private ResourceEntry(String packageName, String typeName, String entryName) {
        this.packageName = packageName;
        this.typeName = typeName;
        this.entryName = entryName;
    }

    public String packageName() {
        return packageName;
    }

    public String typeName() {
        return typeName;
    }

    public String entryName() {
        return entryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceEntry that = (ResourceEntry) o;
        return Objects.equals(packageName, that.packageName) &&
                Objects.equals(typeName, that.typeName) &&
                Objects.equals(entryName, that.entryName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, typeName, entryName);
    }
}
