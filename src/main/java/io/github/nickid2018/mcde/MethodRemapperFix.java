package io.github.nickid2018.mcde;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.MethodRemapper;
import org.objectweb.asm.commons.Remapper;

public class MethodRemapperFix extends MethodRemapper {

    public MethodRemapperFix(MethodVisitor methodVisitor, Remapper remapper) {
        super(methodVisitor, remapper);
    }

    @Override
    public void visitLocalVariable(String name, String descriptor, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(null, descriptor, signature, start, end, index);
    }
}
