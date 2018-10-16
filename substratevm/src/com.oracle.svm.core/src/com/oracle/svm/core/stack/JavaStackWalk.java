package com.oracle.svm.core.stack;

import org.graalvm.nativeimage.c.function.CodePointer;
import org.graalvm.nativeimage.c.struct.RawField;
import org.graalvm.nativeimage.c.struct.RawStructure;
import org.graalvm.word.Pointer;
import org.graalvm.word.PointerBase;

/**
 * An in-progress Java stack walk.
 */
@RawStructure
public interface JavaStackWalk extends PointerBase {
    @RawField
    Pointer getSP();

    @RawField
    void setSP(Pointer sp);

    @RawField
    CodePointer getIP();

    @RawField
    void setIP(CodePointer ip);

    @RawField
    JavaFrameAnchor getAnchor();

    @RawField
    void setAnchor(JavaFrameAnchor anchor);

    @RawField
    long getTotalFrameSize();

    @RawField
    void setTotalFrameSize(long size);

    // these fields are for diagnostics

    @RawField
    Pointer getStartSP();

    @RawField
    void setStartSP(Pointer sp);

    @RawField
    CodePointer getStartIP();

    @RawField
    void setStartIP(CodePointer ip);
}
