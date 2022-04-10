/*
 *  Copyright (c) 2017-2019, bruce.ge.
 *    This program is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU General Public License
 *    as published by the Free Software Foundation; version 2 of
 *    the License.
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *    GNU General Public License for more details.
 *    You should have received a copy of the GNU General Public License
 *    along with this program;
 */

package com.bruce.intellijplugin.generatesetter.actions;

import com.bruce.intellijplugin.generatesetter.CommonConstants;
import com.bruce.intellijplugin.generatesetter.GenerateAllHandlerAdapter;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTypesUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * @author bruce ge 2020/9/23
 */
public class GenerateWithBuilderAction extends GenerateAllSetterBase {
    public GenerateWithBuilderAction() {
        super(new GenerateAllHandlerAdapter() {
            @Override
            public boolean forBuilder() {
                return true;
            }
        });
    }

    @Override
    public void invoke(@NotNull Project project, Editor editor, @NotNull PsiElement element) throws IncorrectOperationException {
        PsiClass localVarialbeContainingClass = GenerateAllSetterBase.getLocalVarialbeContainingClass(element);
        for (PsiMethod method : localVarialbeContainingClass.getMethods()) {
            if (method.getName().equals(CommonConstants.BUILDER_METHOD_NAME)) {
                PsiType returnType = method.getReturnType();
                PsiClass psiClass = PsiTypesUtil.getPsiClass(returnType);
                String[] splits = localVarialbeContainingClass.getQualifiedName().split("\\.");
                int count = splits.length;
                StringBuilder builder = new StringBuilder("\t\t" + splits[count - 1] + ".builder()\n");
                for (PsiMethod psiClassMethod : psiClass.getMethods()) {

                    if (!psiClassMethod.isConstructor() && !psiClassMethod.getName().equals("toString") && !psiClassMethod.getName().equals("build")) {
                        builder.append("\t\t\t\t.").append(psiClassMethod.getName()).append("()\n");
                    }
                }
                builder.append("\t\t\t\t.build();\n");

//                StringBuilder result = new StringBuilder(builder.toString().replace(";", " = ").replace("\n", ""));

                // insert into the element.
                Document document = PsiDocumentManager.getInstance(project).getDocument(element.getContainingFile());

                WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                    @Override
                    public void run() {
                        document.insertString(element.getTextRange().getEndOffset() + 2, builder.toString());
                        PsiDocumentManager.getInstance(project).commitDocument(document);
                    }
                });

            }
        }
    }

    @NotNull
    @Override
    public String getText() {
        return CommonConstants.GENERATE_BUILDER_METHOD;
    }
}
